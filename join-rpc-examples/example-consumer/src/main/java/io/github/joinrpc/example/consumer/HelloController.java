package io.github.joinrpc.example.consumer;

import io.github.joinrpc.example.api.HelloService;
import io.github.joinrpc.example.api.User;
import io.github.joinrpc.spring.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @RpcReference(application = "example-provider")
    private HelloService helloService;

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "world") String name) {
        return helloService.hello(name);
    }

    @GetMapping("/users/{id}")
    public User user(@PathVariable long id) {
        return helloService.findUser(id);
    }

    @GetMapping("/users")
    public List<User> users(@RequestParam(defaultValue = "3") int size) {
        return helloService.listUsers(size);
    }

    @GetMapping("/fail")
    public String fail() {
        try {
            helloService.fail("something went wrong");
            return "unexpected";
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
