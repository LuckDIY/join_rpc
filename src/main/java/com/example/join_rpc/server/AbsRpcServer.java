package com.example.join_rpc.server;

import com.example.join_rpc.server.handle.RequestBaseHandler;
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

    protected RequestBaseHandler requestBaseHandler;


    public AbsRpcServer(int port, String protocol, RequestBaseHandler requestBaseHandler) {
        this.port = port;
        this.protocol = protocol;
        this.requestBaseHandler = requestBaseHandler;
    }
}
