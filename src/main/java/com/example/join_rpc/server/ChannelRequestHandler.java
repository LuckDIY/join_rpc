package com.example.join_rpc.server;

import com.example.join_rpc.common.constants.RpcConstant;
import com.example.join_rpc.common.model.RpcMessage;
import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;
import com.example.join_rpc.server.handle.RequestBaseHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelRequestHandler extends ChannelInboundHandlerAdapter {

    private RequestBaseHandler requestBaseHandler;

    public ChannelRequestHandler(RequestBaseHandler requestBaseHandler) {
        this.requestBaseHandler = requestBaseHandler;
    }

    /**
     * 自定义事件
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            log.info("event state:{}", state);
            if (state == IdleState.READER_IDLE) {
                log.debug("read idle happen, so close the connection");
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("读到的数据：{}", msg);
        if (msg instanceof RpcMessage rpcMessage) {

            byte type = rpcMessage.getType();
            if (type == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
                //是心跳包,回复pong
                RpcMessage respMessage = new RpcMessage();
                respMessage.setType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);

                //回复消息失败，关闭这个连接
                ctx.channel().writeAndFlush(respMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                return;
            }

            //请求消息
            Object data = rpcMessage.getData();
            RpcResponse rpcResponse = requestBaseHandler.handleRequest((RpcRequest) data);
            RpcMessage respMessage = new RpcMessage();
            respMessage.setType(RpcConstant.RESPONSE_TYPE);
            respMessage.setData(rpcResponse);
            log.debug("Send Response:{}", rpcResponse);
            ctx.channel().writeAndFlush(respMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

        }
    }
}

