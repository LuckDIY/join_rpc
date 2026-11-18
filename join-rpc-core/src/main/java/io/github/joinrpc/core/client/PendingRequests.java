package io.github.joinrpc.core.client;

import io.github.joinrpc.core.protocol.RpcResponse;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 已发出、尚未收到响应的请求。
 */
class PendingRequests {

    private record Call(Channel channel, CompletableFuture<RpcResponse> future) {
    }

    private final Map<Integer, Call> calls = new ConcurrentHashMap<>();

    void add(int requestId, Channel channel, CompletableFuture<RpcResponse> future) {
        calls.put(requestId, new Call(channel, future));
        // 无论成功、失败还是超时都移除，防止泄漏
        future.whenComplete((r, e) -> calls.remove(requestId));
    }

    void complete(int requestId, RpcResponse response) {
        Call call = calls.remove(requestId);
        if (call != null) {
            call.future().complete(response);
        }
    }

    void fail(int requestId, Throwable cause) {
        Call call = calls.remove(requestId);
        if (call != null) {
            call.future().completeExceptionally(cause);
        }
    }

    void failAll(Channel channel, Throwable cause) {
        calls.values().stream()
                .filter(call -> call.channel() == channel)
                .forEach(call -> call.future().completeExceptionally(cause));
    }

    void failAll(Throwable cause) {
        calls.values().forEach(call -> call.future().completeExceptionally(cause));
    }

    int size() {
        return calls.size();
    }
}
