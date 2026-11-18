package io.github.chaoleiwang.example.provider;

import io.github.chaoleiwang.example.api.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 服务提供方实现
 */
@Slf4j
@Service
public class DemoServiceImpl implements DemoService {

    @Override
    public String hello(String name) {
        log.info("hello({}) 被远程调用", name);
        return "hello " + name;
    }

    @Override
    public String hello(String name, Integer age) {
        log.info("hello({}, {}) 被远程调用", name, age);
        return name + age;
    }

    @Override
    public Integer helloInteger(Integer value) {
        log.info("helloInteger({}) 被远程调用", value);
        return value * 2;
    }

    @Override
    public int helloInt(int value) {
        log.info("helloInt({}) 被远程调用", value);
        return value + 1;
    }

}
