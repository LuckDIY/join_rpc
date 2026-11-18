package io.github.chaoleiwang.example.consumer;

import io.github.chaoleiwang.example.api.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示接口:注入 @RpcService 接口,实际走远程调用
 */
@RestController
public class DemoController {

    @Autowired
    private DemoService demoService;

    @GetMapping("/hello")
    public String hello(@RequestParam String name) {
        return demoService.hello(name);
    }

    @GetMapping("/hello2")
    public String hello2(@RequestParam String name, @RequestParam Integer age) {
        return demoService.hello(name, age);
    }

    @GetMapping("/helloInteger")
    public Integer helloInteger(@RequestParam Integer value) {
        return demoService.helloInteger(value);
    }

    @GetMapping("/helloInt")
    public int helloInt(@RequestParam int value) {
        return demoService.helloInt(value);
    }

}
