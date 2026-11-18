package io.github.joinrpc.core.server;

import io.github.joinrpc.core.protocol.RpcMessage;
import io.github.joinrpc.core.protocol.RpcProtocol;
import io.github.joinrpc.core.protocol.RpcRequest;
import io.github.joinrpc.core.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * 服务端连接处理：心跳在 IO 线程直接回复，业务调用丢到业务线程池，避免阻塞 IO 线程。
 */
class ServerChannelHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private static final Logger log = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final RequestHandler requestHandler;
    private final Executor executor;

    ServerChannelHandler(RequestHandler requestHandler, Executor executor) {
        this.requestHandler = requestHandler;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        if (msg.getType() == RpcProtocol.TYPE_HEARTBEAT_REQUEST) {
            ctx.writeAndFlush(RpcMessage.heartbeatResponse());
            return;
        }
        if (msg.getType() != RpcProtocol.TYPE_REQUEST) {
            return;
        }
        if (msg.getDecodeError() != null) {
            log.warn("failed to decode request {} from {}", msg.getRequestId(), ctx.channel().remoteAddress(), msg.getDecodeError());
            reply(ctx, msg, RpcResponse.error(msg.getDecodeError()), false);
            return;
        }
        try {
            executor.execute(() -> reply(ctx, msg, requestHandler.handle((RpcRequest) msg.getBody()), true));
        } catch (RejectedExecutionException e) {
            reply(ctx, msg, RpcResponse.error(RejectedExecutionException.class.getName(), "server is busy"), false);
        }
    }

    private void reply(ChannelHandlerContext ctx, RpcMessage request, RpcResponse response, boolean retryOnEncodeError) {
        ctx.writeAndFlush(RpcMessage.response(request.getRequestId(), request.getSerializer(), response))
                .addListener(future -> {
                    if (future.isSuccess()) {
                        return;
                    }
                    Throwable cause = future.cause();
                    if (retryOnEncodeError && cause instanceof EncoderException) {
                        // 返回值无法序列化，至少告诉客户端出了什么错，而不是让它等到超时
                        log.warn("failed to serialize response of {}", request.getBody(), cause);
                        reply(ctx, request, RpcResponse.error(cause.getClass().getName(),
                                "failed to serialize response: " + cause.getMessage()), false);
                    } else if (cause instanceof ClosedChannelException) {
                        log.debug("client {} disconnected before response of {} was sent", ctx.channel().remoteAddress(), request.getBody());
                    } else {
                        log.warn("failed to send response to {}", ctx.channel().remoteAddress(), cause);
                    }
                });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent e && e.state() == IdleState.READER_IDLE) {
            log.debug("connection {} idle, closing", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("connection {} error, closing", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
