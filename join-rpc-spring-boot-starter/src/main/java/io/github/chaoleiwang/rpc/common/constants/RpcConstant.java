package io.github.chaoleiwang.rpc.common.constants;

/**
 * RPC 协议常量
 */
public class RpcConstant {

    /**
     * 最大帧长度 8MB
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    /**
     * 请求类型
     */
    public static final byte REQUEST_TYPE = 1;

    /**
     * 响应类型
     */
    public static final byte RESPONSE_TYPE = 2;

    /**
     * 心跳请求
     */
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;

    /**
     * 心跳响应
     */
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    /**
     * 魔数
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};

    /**
     * 协议版本
     */
    public static final byte VERSION = 1;

    /**
     * 帧头长度: magic(4) + version(1) + fullLength(4) + type(1) + requestId(4)
     */
    public static final int HEAD_LENGTH = 14;

    /**
     * 心跳内容
     */
    public static final String PING = "ping";

    public static final String PONG = "pong";

}
