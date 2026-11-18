package io.github.chaoleiwang.rpc.client;

import io.github.chaoleiwang.rpc.common.codec.MessageDecoder;
import io.github.chaoleiwang.rpc.common.codec.MessageEncoder;
import io.github.chaoleiwang.rpc.common.constants.RpcConstant;
import io.github.chaoleiwang.rpc.common.model.RpcMessage;
import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.github.chaoleiwang.rpc.common.serialize.MessageSerialize;
import io.github.chaoleiwang.rpc.exception.RpcTransportException;
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
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty RPC 客户端:
 * <ul>
 *   <li>按 host:port 懒加载并复用连接,连接断开后自动重连;</li>
 *   <li>以 requestId 匹配请求与响应(CompletableFuture);</li>
 *   <li>写空闲时发送心跳保活。</li>
 * </ul>
 */
@Slf4j
public class RpcNettyClient {

    private final EventLoopGroup group = new NioEventLoopGroup(2);
    private final Bootstrap bootstrap;
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private final Map<Integer, PendingCall> pendingCalls = new ConcurrentHashMap<>();
    private final AtomicInteger requestIdGenerator = new AtomicInteger();

    private volatile boolean closed = false;

    public RpcNettyClient(MessageSerialize messageSerialize, int connectTimeoutMillis) {
        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new MessageEncoder(messageSerialize));
                        ch.pipeline().addLast(new MessageDecoder(messageSerialize));
                        ch.pipeline().addLast(new ClientRequestHandler(RpcNettyClient.this));
                    }
                });
    }

    /**
     * 同步发送请求,超时或连接异常抛出 RpcTransportException
     */
    public RpcResponse send(String host, int port, RpcRequest request, long timeoutMillis) {
        if (closed) {
            throw new RpcTransportException("RpcNettyClient 已关闭");
        }
        String address = host + ":" + port;
        int requestId = Math.abs(requestIdGenerator.incrementAndGet());
        request.setRequestId(requestId);

        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        pendingCalls.put(requestId, new PendingCall(address, future));
        try {
            Channel channel = getChannel(host, port);
            RpcMessage message = new RpcMessage();
            message.setType(RpcConstant.REQUEST_TYPE);
            message.setRequestId(requestId);
            message.setData(request);
            channel.writeAndFlush(message).addListener(writeFuture -> {
                if (!writeFuture.isSuccess()) {
                    PendingCall removed = pendingCalls.remove(requestId);
                    if (removed != null) {
                        removed.future.completeExceptionally(new RpcTransportException("消息发送失败: " + address, writeFuture.cause()));
                    }
                }
            });
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            pendingCalls.remove(requestId);
            throw new RpcTransportException("RPC 调用超时(" + timeoutMillis + "ms): " + address, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pendingCalls.remove(requestId);
            throw new RpcTransportException("RPC 调用被中断: " + address, e);
        } catch (ExecutionException e) {
            pendingCalls.remove(requestId);
            throw new RpcTransportException("RPC 调用异常: " + address, e.getCause());
        }
    }

    /**
     * 获取或创建连接
     */
    private Channel getChannel(String host, int port) {
        String address = host + ":" + port;
        Channel channel = channels.get(address);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        channels.remove(address, channel);
        try {
            ChannelFuture connectFuture = bootstrap.connect(host, port).syncUninterruptibly();
            channels.put(address, connectFuture.channel());
            return connectFuture.channel();
        } catch (Exception e) {
            throw new RpcTransportException("连接服务端失败: " + address, e);
        }
    }

    /**
     * 收到响应,完成对应请求
     */
    public void completeResponse(int requestId, RpcResponse response) {
        PendingCall call = pendingCalls.remove(requestId);
        if (call != null) {
            call.future.complete(response);
        } else {
            log.debug("收到未知请求的响应, requestId: {}", requestId);
        }
    }

    /**
     * 连接断开:清理连接,并把该连接上所有未完成的请求置为失败
     */
    public void handleChannelClosed(Channel channel) {
        String address = null;
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            if (entry.getValue() == channel) {
                address = entry.getKey();
                break;
            }
        }
        if (address != null) {
            channels.remove(address, channel);
        }
        String closedAddress = address;
        pendingCalls.entrySet().removeIf(entry -> {
            if (closedAddress == null || entry.getValue().address.equals(closedAddress)) {
                entry.getValue().future.completeExceptionally(
                        new RpcTransportException("连接已断开: " + entry.getValue().address));
                return true;
            }
            return false;
        });
    }

    /**
     * 关闭客户端
     */
    public void shutdown() {
        closed = true;
        pendingCalls.values().forEach(call -> call.future.completeExceptionally(new RpcTransportException("客户端已关闭")));
        pendingCalls.clear();
        channels.clear();
        group.shutdownGracefully();
    }

    private static class PendingCall {

        private final String address;
        private final CompletableFuture<RpcResponse> future;

        PendingCall(String address, CompletableFuture<RpcResponse> future) {
            this.address = address;
            this.future = future;
        }
    }

}
