package io.github.joinrpc.core.codec;

import io.github.joinrpc.core.protocol.RpcMessage;
import io.github.joinrpc.core.protocol.RpcProtocol;
import io.github.joinrpc.core.serialize.Serializers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器，协议格式见 {@link RpcProtocol}。
 * 序列化失败时抛出的异常会让本次 write 的 future 失败，由发送方处理。
 */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final byte[] EMPTY = new byte[0];

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) {
        byte[] body = msg.isHeartbeat() || msg.getBody() == null
                ? EMPTY
                : Serializers.get(msg.getSerializer()).serialize(msg.getBody());

        out.writeInt(RpcProtocol.MAGIC);
        out.writeByte(RpcProtocol.VERSION);
        out.writeInt(RpcProtocol.HEADER_LENGTH + body.length);
        out.writeByte(msg.getType());
        out.writeByte(msg.getSerializer());
        out.writeInt(msg.getRequestId());
        out.writeBytes(body);
    }
}
