package com.example.join_rpc.server;

import lombok.Getter;
import lombok.Setter;

/**
 * rpc服务抽象类
 */
@Getter
@Setter
public abstract class AbsRpcServer implements RpcServer {


    /**
     * 服务端口
     */
    protected int port;

    /**
     * 服务协议
     */
    protected String protocol;





}
