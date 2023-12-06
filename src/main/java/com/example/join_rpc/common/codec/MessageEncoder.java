
package com.example.join_rpc.common.codec;


import com.example.join_rpc.common.constants.RpcConstant;
import com.example.join_rpc.common.model.RpcMessage;
import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;
import com.example.join_rpc.common.serialize.JsonMessageSerialize;
import com.example.join_rpc.common.serialize.MessageSerialize;
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
 *   |   magic   code        | version |      full length      | type |       RequestId        |
 *   +-----------------------+---------+-----------------------+------+------------------------+
 *   |                                                                                         |
 *   |                                             body                                        |
 *   |                                                                                         |
 *   |                                            ... ...                                      |
 *   +-----------------------------------------------------------------------------------------+
 * 4B  magic code（魔数）      1B version（协议版本）   4B full length（消息长度）    1B type（消息类型）
 *  4B requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 */

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        try {
            byteBuf.writeBytes(RpcConstant.MAGIC_NUMBER);
            byteBuf.writeByte(RpcConstant.VERSION);
            // leave a place to write the value of full length
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);
            byte messageType = rpcMessage.getType();
            byteBuf.writeByte(messageType);

            //请求id todo 暂时不填写
            byteBuf.writeInt(0);

            // build full length
            byte[] bodyBytes = null;
            int fullLength = 14;
            // if messageType is not heartbeat message,fullLength = head length + body length
            if (messageType != RpcConstant.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the object
                MessageSerialize messageSerialize = new JsonMessageSerialize();
                if (messageType == RpcConstant.REQUEST_TYPE) {
                    bodyBytes = messageSerialize.marshallingRequest((RpcRequest) rpcMessage.getData());
                } else {
                    bodyBytes = messageSerialize.marshallingResponse((RpcResponse) rpcMessage.getData());
                }

                //todo 暂时不压缩

                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                byteBuf.writeBytes(bodyBytes);
            }
            int writeIndex = byteBuf.writerIndex();
            //写入位置改为长度位置
            byteBuf.writerIndex(writeIndex - fullLength + 4 + 1);
            byteBuf.writeInt(fullLength);
            byteBuf.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("encode request error!", e);
        }

    }
}
