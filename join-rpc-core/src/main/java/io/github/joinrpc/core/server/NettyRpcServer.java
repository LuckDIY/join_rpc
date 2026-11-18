package io.github.joinrpc.core.server;

import io.github.joinrpc.core.RpcException;
import io.github.joinrpc.core.codec.RpcMessageDecoder;
import io.github.joinrpc.core.codec.RpcMessageEncoder;
import io.github.joinrpc.core.protocol.RpcProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Netty RPC 服务端。{@link #start()} 绑定端口后立即返回，不阻塞调用线程。
 */
public class NettyRpcServer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(NettyRpcServer.class);

    private final int port;
    private final RequestHandler requestHandler;
    private final int businessThreads;
    private final int businessQueueSize;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ExecutorService businessExecutor;
    private Channel serverChannel;

    /**
     * @param port 监听端口，0 表示随机端口（通过 {@link #getPort()} 获取实际端口）
     */
    public NettyRpcServer(int port, ServiceProviderRegistry registry) {
        this(port, registry, 200, 1000);
    }

    public NettyRpcServer(int port, ServiceProviderRegistry registry, int businessThreads, int businessQueueSize) {
        this.port = port;
        this.requestHandler = new RequestHandler(registry);
        this.businessThreads = businessThreads;
        this.businessQueueSize = businessQueueSize;
    }

    public synchronized void start() {
        if (serverChannel != null) {
            return;
        }
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("join-rpc-boss"));
        workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("join-rpc-worker"));
        businessExecutor = new ThreadPoolExecutor(businessThreads, businessThreads, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(businessQueueSize), new DefaultThreadFactory("join-rpc-biz"),
                new ThreadPoolExecutor.AbortPolicy());

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(RpcProtocol.IDLE_TIMEOUT_SECONDS, 0, 0, TimeUnit.SECONDS))
                                .addLast(new RpcMessageEncoder())
                                .addLast(new RpcMessageDecoder())
                                .addLast(new ServerChannelHandler(requestHandler, businessExecutor));
                    }
                });
        try {
            serverChannel = bootstrap.bind(port).syncUninterruptibly().channel();
        } catch (Exception e) {
            shutdownResources();
            throw new RpcException("failed to start join-rpc server on port " + port, e);
        }
        log.info("join-rpc server started on port {}", getPort());
    }

    /**
     * @return 实际监听的端口，未启动时返回构造参数中的端口
     */
    public int getPort() {
        Channel channel = serverChannel;
        return channel == null ? port : ((InetSocketAddress) channel.localAddress()).getPort();
    }

    public boolean isRunning() {
        return serverChannel != null && serverChannel.isActive();
    }

    /**
     * 停止接收新连接，等待已收到的请求处理完（最多 10 秒）后关闭。
     */
    public synchronized void stop() {
        if (serverChannel == null) {
            return;
        }
        serverChannel.close().syncUninterruptibly();
        serverChannel = null;
        businessExecutor.shutdown();
        try {
            if (!businessExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("join-rpc business threads did not finish in 10s, forcing shutdown");
                businessExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            businessExecutor.shutdownNow();
        }
        shutdownResources();
        log.info("join-rpc server stopped");
    }

    private void shutdownResources() {
        if (businessExecutor != null) {
            businessExecutor.shutdownNow();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(0, 2, TimeUnit.SECONDS);
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(0, 2, TimeUnit.SECONDS);
        }
    }

    @Override
    public void close() {
        stop();
    }
}
