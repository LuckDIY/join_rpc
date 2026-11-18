package io.github.chaoleiwang.rpc.common.codec;

import io.github.chaoleiwang.rpc.common.constants.RpcConstant;
import io.github.chaoleiwang.rpc.common.model.RpcMessage;
import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.github.chaoleiwang.rpc.common.serialize.MessageSerialize;
import io.github.chaoleiwang.rpc.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 传输编码协议
 *
 * <pre>
 *   0     1     2     3     4         5     6     7     8     9      10   11    12     13     14
 *   +-----+-----+-----+-----+---------+-----+-----+-----+-----+------+------+-----+-----+-----+
 *   |   magic   code        | version |      full length      | type |       requestId       |
 *   +-----------------------+---------+-----------------------+------+------------------------+
 *   |                                            body                                          |
 *   +------------------------------------------------------------------------------------------+
 * 4B  magic code（魔数）   1B version（协议版本）  4B full length（消息长度）  1B type（消息类型）
 * 4B  requestId（请求 id）
 * body（序列化后的数据,心跳包无 body）
 * </pre>
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private final MessageSerialize messageSerialize;

    public MessageEncoder(MessageSerialize messageSerialize) {
        this.messageSerialize = messageSerialize;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) {
        byteBuf.writeBytes(RpcConstant.MAGIC_NUMBER);
        byteBuf.writeByte(RpcConstant.VERSION);
        // 先占位,最后回填 full length
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        byte messageType = rpcMessage.getType();
        byteBuf.writeByte(messageType);
        byteBuf.writeInt(rpcMessage.getRequestId());

        byte[] bodyBytes = null;
        int fullLength = RpcConstant.HEAD_LENGTH;
        // 心跳包没有 body
        if (messageType != RpcConstant.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
            try {
                if (messageType == RpcConstant.REQUEST_TYPE) {
                    bodyBytes = messageSerialize.marshallingRequest((RpcRequest) rpcMessage.getData());
                } else {
                    bodyBytes = messageSerialize.marshallingResponse((RpcResponse) rpcMessage.getData());
                }
            } catch (Exception e) {
                log.error("rpc 消息序列化失败, type: {}", messageType, e);
                throw new RpcException("rpc 消息序列化失败: " + e.getMessage(), e);
            }
            fullLength += bodyBytes.length;
            byteBuf.writeBytes(bodyBytes);
        }

        // 回填 full length
        int writeIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(writeIndex - fullLength + RpcConstant.HEAD_LENGTH - 9);
        byteBuf.writeInt(fullLength);
        byteBuf.writerIndex(writeIndex);
    }

}
