package com.example.join_rpc.common.constants;

public class RpcConstant {

    /**
     * Zookeeper服务注册公共前缀
     */
    public static final String ZK_SERVICE_PATH = "/rpc";

    /**
     * zk路径分隔符
     */
    public static final String ZK_PATH_DELIMITER = "/";


    /**
     * MAX_FRAME_LENGTH
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;



    /**
     * REQUEST_TYPE
     */
    public static final byte REQUEST_TYPE = 1;

    /**
     * RESPONSE_TYPE
     */
    public static final byte RESPONSE_TYPE = 2;

    /**
     * HEARTBEAT_REQUEST_TYPE 心跳
     */
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;

    /**
     * HEARTBEAT_RESPONSE_TYPE
     */
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;


    /**
     * MAGIC_NUMBER
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};

    /**
     * VERSION
     */
    public static final byte VERSION = 1;


    /**
     * PING
     */
    public static final String PING = "ping";

    /**
     * PONG
     */
    public static final String PONG = "pong";


    /**
     * HEAD_LENGTH
     */
    public static final int HEAD_LENGTH = 14;

    /**
     * 服务代理类型 reflect： 反射调用
     */
    public static final String SERVER_PROXY_TYPE_REFLECT = "reflect";
}
