package io.github.joinrpc.core.codec;

import io.github.joinrpc.core.protocol.RpcMessage;
import io.github.joinrpc.core.protocol.RpcProtocol;
import io.github.joinrpc.core.protocol.RpcRequest;
import io.github.joinrpc.core.protocol.RpcResponse;
import io.github.joinrpc.core.serialize.Serializers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 解码器，协议格式见 {@link RpcProtocol}。
 * <p>
 * 头部不合法（魔数、版本不对）说明对端不是 join-rpc，直接抛异常断开连接；
 * 头部合法但 body 反序列化失败时不断开连接，而是把错误放进 {@link RpcMessage#getDecodeError()}，
 * 让上层可以针对这个 requestId 回复/结束这一次调用。
 */
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthAdjustment = -(偏移 5 + 长度字段 4)，因为 full length 包含了整个头部
        super(RpcProtocol.MAX_FRAME_LENGTH, RpcProtocol.LENGTH_FIELD_OFFSET, 4,
                -(RpcProtocol.LENGTH_FIELD_OFFSET + 4), 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        try {
            return decodeFrame(frame);
        } finally {
            frame.release();
        }
    }

    private RpcMessage decodeFrame(ByteBuf frame) {
        if (frame.readableBytes() < RpcProtocol.HEADER_LENGTH) {
            throw new CorruptedFrameException("frame too short: " + frame.readableBytes());
        }
        int magic = frame.readInt();
        if (magic != RpcProtocol.MAGIC) {
            throw new CorruptedFrameException("unknown magic number: 0x" + Integer.toHexString(magic));
        }
        byte version = frame.readByte();
        if (version != RpcProtocol.VERSION) {
            throw new CorruptedFrameException("unsupported protocol version: " + version);
        }
        frame.readInt(); // full length，帧已由父类切好
        byte type = frame.readByte();
        byte serializer = frame.readByte();
        int requestId = frame.readInt();

        if (type == RpcProtocol.TYPE_HEARTBEAT_REQUEST || type == RpcProtocol.TYPE_HEARTBEAT_RESPONSE) {
            return new RpcMessage(type, serializer, requestId, null);
        }
        if (type != RpcProtocol.TYPE_REQUEST && type != RpcProtocol.TYPE_RESPONSE) {
            throw new CorruptedFrameException("unknown message type: " + type);
        }

        byte[] body = new byte[frame.readableBytes()];
        frame.readBytes(body);
        Class<?> bodyType = type == RpcProtocol.TYPE_REQUEST ? RpcRequest.class : RpcResponse.class;
        try {
            Object value = Serializers.get(serializer).deserialize(body, bodyType);
            return new RpcMessage(type, serializer, requestId, value);
        } catch (Exception e) {
            return new RpcMessage(type, serializer, requestId, null, e);
        }
    }
}
