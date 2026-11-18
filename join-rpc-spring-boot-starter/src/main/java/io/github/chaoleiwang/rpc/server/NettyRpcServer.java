package io.github.chaoleiwang.rpc.server;

import io.github.chaoleiwang.rpc.common.codec.MessageDecoder;
import io.github.chaoleiwang.rpc.common.codec.MessageEncoder;
import io.github.chaoleiwang.rpc.common.serialize.MessageSerialize;
import io.github.chaoleiwang.rpc.server.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Netty RPC 服务端。
 * <p>
 * 注意:start() 只做端口绑定(绑定是本地操作,毫秒级返回),不会阻塞调用线程,
 * 适合在 Spring 生命周期回调中调用。
 */
@Slf4j
public class NettyRpcServer implements RpcServer {

    private final int port;
    private final RpcRequestHandler requestHandler;
    private final MessageSerialize messageSerialize;

    private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;
    private volatile Channel channel;

    public NettyRpcServer(int port, RpcRequestHandler requestHandler, MessageSerialize messageSerialize) {
        this.port = port;
        this.requestHandler = requestHandler;
        this.messageSerialize = messageSerialize;
    }

    @Override
    public synchronized void start() {
        if (channel != null) {
            return;
        }
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // 三次握手连接队列最大长度
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 不合并包
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new MessageEncoder(messageSerialize));
                        ch.pipeline().addLast(new MessageDecoder(messageSerialize));
                        ch.pipeline().addLast(new ChannelRequestHandler(requestHandler));
                    }
                });

        try {
            // 只同步等待 bind 完成,不阻塞 closeFuture,避免卡死 Spring 启动流程
            Channel futureChannel = serverBootstrap.bind(port).syncUninterruptibly().channel();
            channel = futureChannel;
            log.info("join-rpc Netty 服务端启动成功,监听端口: {}", port);
        } catch (Exception e) {
            log.error("join-rpc Netty 服务端启动失败, 端口: {}", port, e);
            shutdownGroups();
            throw new IllegalStateException("join-rpc Netty 服务端启动失败, 端口: " + port, e);
        }
    }

    @Override
    public synchronized void stop() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        shutdownGroups();
        log.info("join-rpc Netty 服务端已停止");
    }

    private void shutdownGroups() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }

}
