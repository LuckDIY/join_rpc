package io.github.joinrpc.core.client;

import io.github.joinrpc.core.RpcException;
import io.github.joinrpc.core.protocol.RpcMessage;
import io.github.joinrpc.core.protocol.RpcProtocol;
import io.github.joinrpc.core.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端连接处理：按 requestId 把响应交给对应的 future；写空闲时发心跳；连接断开时让该连接上所有未完成的调用立即失败。
 */
class ClientChannelHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private static final Logger log = LoggerFactory.getLogger(ClientChannelHandler.class);

    private final PendingRequests pending;

    ClientChannelHandler(PendingRequests pending) {
        this.pending = pending;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        if (msg.getType() != RpcProtocol.TYPE_RESPONSE) {
            return;
        }
        if (msg.getDecodeError() != null) {
            pending.fail(msg.getRequestId(), new RpcException("failed to decode response", msg.getDecodeError()));
        } else {
            pending.complete(msg.getRequestId(), (RpcResponse) msg.getBody());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent e) {
            if (e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(RpcMessage.heartbeatRequest());
            } else if (e.state() == IdleState.READER_IDLE) {
                log.debug("no data from {} for a long time, closing", ctx.channel().remoteAddress());
                ctx.close();
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pending.failAll(ctx.channel(), new RpcException("connection to " + ctx.channel().remoteAddress() + " closed"));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("connection {} error, closing", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
