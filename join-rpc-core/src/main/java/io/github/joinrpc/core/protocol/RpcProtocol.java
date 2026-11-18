package io.github.joinrpc.core.protocol;

/**
 * 传输协议常量。
 *
 * <pre>
 *  0      4        5             9      10           11           15
 *  +------+--------+-------------+------+------------+------------+-----------+
 *  | magic| version| full length | type | serializer | request id |   body    |
 *  |  4B  |   1B   |     4B      |  1B  |     1B     |     4B     |  变长     |
 *  +------+--------+-------------+------+------------+------------+-----------+
 * </pre>
 * full length = 头部长度 + body 长度。
 */
public final class RpcProtocol {

    /** 魔数 "jrpc" */
    public static final int MAGIC = 0x6A727063;

    public static final byte VERSION = 1;

    public static final int HEADER_LENGTH = 15;

    public static final int LENGTH_FIELD_OFFSET = 5;

    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    public static final byte TYPE_REQUEST = 1;
    public static final byte TYPE_RESPONSE = 2;
    public static final byte TYPE_HEARTBEAT_REQUEST = 3;
    public static final byte TYPE_HEARTBEAT_RESPONSE = 4;

    /** 客户端写空闲多久发一次心跳（秒） */
    public static final int HEARTBEAT_INTERVAL_SECONDS = 30;

    /** 多久没读到任何数据就断开连接（秒） */
    public static final int IDLE_TIMEOUT_SECONDS = HEARTBEAT_INTERVAL_SECONDS * 3;

    private RpcProtocol() {
    }
}
