package com.example.join_rpc.controller;

import com.example.join_rpc.testDemo.Demo2Service;
import com.example.join_rpc.testDemo.Demo3Service;
import com.example.join_rpc.testDemo.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    private Demo2Service demo2Service;

    @Autowired
    private Demo3Service demo3Service;

    @Autowired
    private DemoService demoService;

    @GetMapping("ok")
    public Object ok() {
        return demo2Service.hello("123");
    }

    @GetMapping("ok1")
    public Object ok1() {
        return demo3Service.abcd("123");
    }

    @GetMapping("ok2")
    public Object ok2() {
        return demoService.helloInteger(123);
    }
}
