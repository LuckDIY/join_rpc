package com.example.join_rpc.testDemo;

import com.example.join_rpc.annotation.RpcService;

@RpcService
public interface Demo2Service {

    String hello(String var1, Integer var2);

    String hello(String var1);
}
