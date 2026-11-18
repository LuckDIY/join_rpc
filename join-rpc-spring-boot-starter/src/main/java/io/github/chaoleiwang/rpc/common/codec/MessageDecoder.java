package io.github.chaoleiwang.rpc.common.codec;

import io.github.chaoleiwang.rpc.common.constants.RpcConstant;
import io.github.chaoleiwang.rpc.common.model.RpcMessage;
import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.github.chaoleiwang.rpc.common.serialize.MessageSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 传输解码协议,与 {@link MessageEncoder} 对应
 */
@Slf4j
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    private final MessageSerialize messageSerialize;

    public MessageDecoder(MessageSerialize messageSerialize) {
        // lengthFieldOffset: magic(4B) + version(1B) = 5
        // lengthFieldLength: full length 4B
        // lengthAdjustment: fullLength 包含全部数据,已读 9 字节,剩余 fullLength - 9
        // initialBytesToStrip: 0,魔数和版本需要手动校验
        super(RpcConstant.MAX_FRAME_LENGTH, 5, 4, -9, 0);
        this.messageSerialize = messageSerialize;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf frame) {
            if (frame.readableBytes() >= RpcConstant.HEAD_LENGTH) {
                try {
                    return decodeFrame(frame);
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private RpcMessage decodeFrame(ByteBuf in) throws Exception {
        // 必须按顺序读取
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        int requestId = in.readInt();

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setType(messageType);
        rpcMessage.setRequestId(requestId);
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
            byte[] bodyBytes = new byte[bodyLength];
            in.readBytes(bodyBytes);
            if (messageType == RpcConstant.REQUEST_TYPE) {
                RpcRequest request = messageSerialize.unmarshallingRequest(bodyBytes);
                rpcMessage.setData(request);
            } else {
                RpcResponse response = messageSerialize.unmarshallingResponse(bodyBytes);
                rpcMessage.setData(response);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcConstant.VERSION) {
            throw new RuntimeException("版本不兼容: " + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        byte[] tmp = new byte[RpcConstant.MAGIC_NUMBER.length];
        in.readBytes(tmp);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != RpcConstant.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("未知魔数: " + Arrays.toString(tmp));
            }
        }
    }

}
