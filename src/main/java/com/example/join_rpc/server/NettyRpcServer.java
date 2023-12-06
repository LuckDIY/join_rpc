package com.example.join_rpc.server;

import com.example.join_rpc.common.codec.MessageDecoder;
import com.example.join_rpc.common.codec.MessageEncoder;
import com.example.join_rpc.server.handle.RequestBaseHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcServer extends AbsRpcServer {


    private Channel channel;

    public NettyRpcServer(int port, String protocol, RequestBaseHandler requestBaseHandler) {
        super(port, protocol,requestBaseHandler);
    }

    @Override
    public void start() {

        //连接和数据读写
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //三次握手连接队列（accept queue） 存放已完成三次握手的请求的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //保活
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //不合并包
                    .childOption(ChannelOption.TCP_NODELAY, true)

                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(15, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new MessageEncoder());
                            pipeline.addLast(new MessageDecoder());
                            pipeline.addLast(new ChannelRequestHandler(requestBaseHandler));
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            log.debug("Server started successfully.");
            channel = future.channel();
            //等待关闭
            future.channel().closeFuture().sync();
            log.info("已关闭netty");
        } catch (InterruptedException e) {
            log.error("start netty server failed, message: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            log.debug("shutdownGracefully");
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    public void stop() {
        channel.close();
        log.debug("Server stop successfully.");
    }
}
