package io.github.chaoleiwang.rpc.server;

/**
 * RPC 服务端接口
 */
public interface RpcServer {

    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务
     */
    void stop();

}
