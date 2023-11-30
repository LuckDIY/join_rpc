package com.example.join_rpc.testDemo;


import com.example.join_rpc.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RpcService
public class DemoServiceImpl implements DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public String hello(String name, Integer age) {
        log.info("hello2 start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("hello2 end");
        return name + age;
    }

    @Override
    public String hello(String name) {
        log.info("hello1 start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("hello1 end");
        return name;
    }

    @Override
    public Integer helloInteger(Integer age) {
        return age;
    }

    @Override
    public int helloInt(int age) {
        return age;
    }

    @Override
    public byte helloByte(byte age) {
        return age;
    }


}
