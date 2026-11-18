package io.github.chaoleiwang.rpc.test.impl;

import io.github.chaoleiwang.rpc.test.api.HelloService;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }

    @Override
    public String hello(String name, Integer age) {
        return name + age;
    }

    @Override
    public Integer helloInteger(Integer value) {
        return value * 2;
    }

    @Override
    public int helloInt(int value) {
        return value + 1;
    }

}
