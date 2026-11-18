package io.github.chaoleiwang.rpc.test.api;

import io.github.chaoleiwang.rpc.annotation.RpcService;

@RpcService
public interface HelloService {

    String hello(String name);

    String hello(String name, Integer age);

    Integer helloInteger(Integer value);

    int helloInt(int value);

}
