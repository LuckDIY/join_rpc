package io.github.chaoleiwang.rpc.client;

import io.github.chaoleiwang.rpc.common.constants.RpcConstant;
import io.github.chaoleiwang.rpc.common.model.RpcMessage;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端入站处理器:响应匹配 + 心跳保活
 */
@Slf4j
public class ClientRequestHandler extends ChannelInboundHandlerAdapter {

    private final RpcNettyClient rpcNettyClient;

    public ClientRequestHandler(RpcNettyClient rpcNettyClient) {
        this.rpcNettyClient = rpcNettyClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof RpcMessage rpcMessage)) {
            ctx.fireChannelRead(msg);
            return;
        }
        byte type = rpcMessage.getType();
        if (type == RpcConstant.RESPONSE_TYPE) {
            rpcNettyClient.completeResponse(rpcMessage.getRequestId(), (RpcResponse) rpcMessage.getData());
        } else if (type == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
            // 对端心跳,回 pong
            RpcMessage respMessage = new RpcMessage();
            respMessage.setType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);
            respMessage.setRequestId(rpcMessage.getRequestId());
            ctx.channel().writeAndFlush(respMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else if (type == RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
            log.debug("收到心跳响应");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                // 写空闲,发心跳保活
                RpcMessage pingMessage = new RpcMessage();
                pingMessage.setType(RpcConstant.HEARTBEAT_REQUEST_TYPE);
                pingMessage.setRequestId(0);
                pingMessage.setData(RpcConstant.PING);
                ctx.channel().writeAndFlush(pingMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        rpcNettyClient.handleChannelClosed(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("客户端连接异常,关闭连接: {}", ctx.channel(), cause);
        ctx.close();
    }

}
