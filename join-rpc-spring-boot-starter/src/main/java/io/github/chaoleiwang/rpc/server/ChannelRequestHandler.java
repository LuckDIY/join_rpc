package io.github.chaoleiwang.rpc.server;

import io.github.chaoleiwang.rpc.common.constants.RpcConstant;
import io.github.chaoleiwang.rpc.common.model.RpcMessage;
import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.github.chaoleiwang.rpc.server.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端入站处理器:心跳应答 + 请求分发
 */
@Slf4j
public class ChannelRequestHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler requestHandler;

    public ChannelRequestHandler(RpcRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                log.debug("读空闲超时,关闭连接: {}", ctx.channel());
                ctx.close();
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof RpcMessage rpcMessage)) {
            ctx.fireChannelRead(msg);
            return;
        }
        byte type = rpcMessage.getType();
        if (type == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
            // 心跳请求,回 pong
            RpcMessage respMessage = new RpcMessage();
            respMessage.setType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);
            respMessage.setRequestId(rpcMessage.getRequestId());
            respMessage.setData(RpcConstant.PONG);
            ctx.channel().writeAndFlush(respMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            return;
        }
        // 业务请求
        RpcResponse rpcResponse = requestHandler.handle((RpcRequest) rpcMessage.getData());
        RpcMessage respMessage = new RpcMessage();
        respMessage.setType(RpcConstant.RESPONSE_TYPE);
        respMessage.setRequestId(rpcMessage.getRequestId());
        respMessage.setData(rpcResponse);
        log.debug("发送响应: {}", rpcResponse);
        ctx.channel().writeAndFlush(respMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("服务端处理异常,关闭连接: {}", ctx.channel(), cause);
        ctx.close();
    }

}
