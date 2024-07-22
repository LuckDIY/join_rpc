package com.example.join_rpc.common.codec;


import com.example.join_rpc.common.constants.RpcConstant;
import com.example.join_rpc.common.model.RpcMessage;
import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;
import com.example.join_rpc.common.serialize.FastJsonMessageSerialize;
import com.example.join_rpc.common.serialize.JsonMessageSerialize;
import com.example.join_rpc.common.serialize.MessageSerialize;
import com.example.join_rpc.common.serialize.ProtoBufSerializeMessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/**
 * 传输解码协议
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
public class MessageDecoder extends LengthFieldBasedFrameDecoder {
    public MessageDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstant.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            //等于的话是心跳包
            if (frame.readableBytes() >= 14) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }

    private RpcMessage decodeFrame(ByteBuf in) throws Exception {
        // note: must read ByteBuf in order
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // build RpcMessage object
        byte messageType = in.readByte();
        //todo 暂时无用
        int requestId = in.readInt();
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setType(messageType);
        if (messageType == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstant.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstant.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstant.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);

            // deserialize the object
            MessageSerialize messageSerialize = new ProtoBufSerializeMessageProtocol();
            if (messageType == RpcConstant.REQUEST_TYPE) {
                RpcRequest tmpValue = messageSerialize.unmarshallingRequest(bs);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = messageSerialize.unmarshallingResponse(bs);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstant.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstant.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstant.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}
