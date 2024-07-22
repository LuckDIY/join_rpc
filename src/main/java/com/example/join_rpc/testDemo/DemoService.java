package com.example.join_rpc.testDemo;

import com.example.join_rpc.annotation.RpcService;

public interface DemoService {
    String hello(String var1, Integer var2);

    String hello(String var1);

    Integer helloInteger(Integer var1);

    int helloInt(int var1);

    byte helloByte(byte var1);


}