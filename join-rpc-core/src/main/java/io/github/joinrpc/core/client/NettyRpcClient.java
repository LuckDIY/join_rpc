package io.github.joinrpc.core.client;

import io.github.joinrpc.core.RpcAddress;
import io.github.joinrpc.core.RpcException;
import io.github.joinrpc.core.codec.RpcMessageDecoder;
import io.github.joinrpc.core.codec.RpcMessageEncoder;
import io.github.joinrpc.core.protocol.RpcMessage;
import io.github.joinrpc.core.protocol.RpcProtocol;
import io.github.joinrpc.core.protocol.RpcRequest;
import io.github.joinrpc.core.protocol.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty RPC 客户端。每个服务端地址维持一条长连接，请求通过 requestId 在这条连接上多路复用。
 * 线程安全，整个应用共用一个实例即可。
 */
public class NettyRpcClient implements AutoCloseable {

    private final Duration connectTimeout;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final PendingRequests pending = new PendingRequests();
    private final AtomicInteger requestIds = new AtomicInteger();

    private final Map<RpcAddress, Channel> channels = new ConcurrentHashMap<>();
    private final Map<RpcAddress, Object> connectLocks = new ConcurrentHashMap<>();

    private volatile boolean closed;

    public NettyRpcClient() {
        this(Duration.ofSeconds(3));
    }

    public NettyRpcClient(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        this.group = new NioEventLoopGroup(0, new DefaultThreadFactory("join-rpc-client", true));
        this.bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(RpcProtocol.IDLE_TIMEOUT_SECONDS,
                                        RpcProtocol.HEARTBEAT_INTERVAL_SECONDS, 0, TimeUnit.SECONDS))
                                .addLast(new RpcMessageEncoder())
                                .addLast(new RpcMessageDecoder())
                                .addLast(new ClientChannelHandler(pending));
                    }
                });
    }

    /**
     * 异步发送请求。返回的 future 在收到响应、连接断开或超时（{@link java.util.concurrent.TimeoutException}）时完成。
     *
     * @throws RpcConnectException 连接服务端失败（请求未发出）
     */
    public CompletableFuture<RpcResponse> send(RpcAddress address, RpcRequest request, byte serializer, Duration timeout) {
        if (closed) {
            throw new RpcException("client is closed");
        }
        Channel channel = getChannel(address);
        int requestId = requestIds.incrementAndGet();
        CompletableFuture<RpcResponse> future = new CompletableFuture<RpcResponse>()
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        pending.add(requestId, channel, future);
        channel.writeAndFlush(RpcMessage.request(requestId, serializer, request)).addListener(f -> {
            if (!f.isSuccess()) {
                future.completeExceptionally(new RpcException("failed to send " + request + " to " + address, f.cause()));
            }
        });
        return future;
    }

    private Channel getChannel(RpcAddress address) {
        Channel channel = channels.get(address);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        synchronized (connectLocks.computeIfAbsent(address, a -> new Object())) {
            channel = channels.get(address);
            if (channel != null && channel.isActive()) {
                return channel;
            }
            ChannelFuture future = bootstrap.connect(address.host(), address.port());
            if (!future.awaitUninterruptibly(connectTimeout.toMillis() + 500)) {
                future.cancel(false);
                throw new RpcConnectException("connect to " + address + " timed out", null);
            }
            if (!future.isSuccess()) {
                throw new RpcConnectException("failed to connect to " + address, future.cause());
            }
            Channel newChannel = future.channel();
            channels.put(address, newChannel);
            newChannel.closeFuture().addListener(f -> channels.remove(address, newChannel));
            return newChannel;
        }
    }

    @Override
    public void close() {
        closed = true;
        pending.failAll(new RpcException("client is closed"));
        channels.values().forEach(Channel::close);
        channels.clear();
        group.shutdownGracefully(0, 2, TimeUnit.SECONDS);
    }
}
