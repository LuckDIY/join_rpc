package com.example.join_rpc.testDemo;

import com.example.join_rpc.common.codec.MessageDecoder;
import com.example.join_rpc.common.codec.MessageEncoder;
import com.example.join_rpc.common.constants.RpcConstant;
import com.example.join_rpc.common.model.RpcMessage;
import com.example.join_rpc.common.model.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TcpClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        pipeline.addLast(new SimpleChannelInboundHandler<RpcMessage>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
                                System.out.println("打印结果: " + msg);
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("通道已经建立");
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("连接关闭");
                                // 在这里可以处理连接关闭后的逻辑
                            }
                        });
                    }
                });
        ChannelFuture future = bootstrap.connect("localhost", 9999).sync();
        System.out.println("连接成功");
        Channel channel = future.channel();

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setType(RpcConstant.REQUEST_TYPE);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId("123423");
        rpcRequest.setServiceName("服务名");
        rpcRequest.setServiceMethod("方法名");
        rpcRequest.setParametersTypes(new Class[]{String.class});
        rpcRequest.setParameters(new Object[]{"哈哈哈"});
        rpcMessage.setData(rpcRequest);
        channel.writeAndFlush(rpcMessage);
        channel.closeFuture().sync();
    }
}