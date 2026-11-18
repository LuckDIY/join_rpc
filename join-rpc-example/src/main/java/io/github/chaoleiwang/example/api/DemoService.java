package io.github.chaoleiwang.example.api;

import io.github.chaoleiwang.rpc.annotation.RpcService;

/**
 * 示例 RPC 接口,provider 与 consumer 共享
 */
@RpcService
public interface DemoService {

    String hello(String name);

    String hello(String name, Integer age);

    Integer helloInteger(Integer value);

    int helloInt(int value);

}
