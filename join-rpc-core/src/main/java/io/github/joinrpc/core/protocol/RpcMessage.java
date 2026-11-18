package io.github.joinrpc.core.protocol;

/**
 * 一帧协议消息。body 为 {@link RpcRequest}、{@link RpcResponse} 或 null（心跳）。
 */
public class RpcMessage {

    private final byte type;
    private final byte serializer;
    private final int requestId;
    private final Object body;

    /** 仅解码端使用：body 反序列化失败的原因，不参与传输 */
    private final Throwable decodeError;

    public RpcMessage(byte type, byte serializer, int requestId, Object body) {
        this(type, serializer, requestId, body, null);
    }

    public RpcMessage(byte type, byte serializer, int requestId, Object body, Throwable decodeError) {
        this.type = type;
        this.serializer = serializer;
        this.requestId = requestId;
        this.body = body;
        this.decodeError = decodeError;
    }

    public static RpcMessage request(int requestId, byte serializer, RpcRequest request) {
        return new RpcMessage(RpcProtocol.TYPE_REQUEST, serializer, requestId, request);
    }

    public static RpcMessage response(int requestId, byte serializer, RpcResponse response) {
        return new RpcMessage(RpcProtocol.TYPE_RESPONSE, serializer, requestId, response);
    }

    public static RpcMessage heartbeatRequest() {
        return new RpcMessage(RpcProtocol.TYPE_HEARTBEAT_REQUEST, (byte) 0, 0, null);
    }

    public static RpcMessage heartbeatResponse() {
        return new RpcMessage(RpcProtocol.TYPE_HEARTBEAT_RESPONSE, (byte) 0, 0, null);
    }

    public boolean isHeartbeat() {
        return type == RpcProtocol.TYPE_HEARTBEAT_REQUEST || type == RpcProtocol.TYPE_HEARTBEAT_RESPONSE;
    }

    public byte getType() {
        return type;
    }

    public byte getSerializer() {
        return serializer;
    }

    public int getRequestId() {
        return requestId;
    }

    public Object getBody() {
        return body;
    }

    public Throwable getDecodeError() {
        return decodeError;
    }

    @Override
    public String toString() {
        return "RpcMessage{type=" + type + ", serializer=" + serializer + ", requestId=" + requestId + ", body=" + body + '}';
    }
}
