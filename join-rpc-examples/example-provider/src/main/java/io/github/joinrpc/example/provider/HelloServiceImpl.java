package io.github.joinrpc.example.provider;

import io.github.joinrpc.example.api.HelloService;
import io.github.joinrpc.example.api.User;
import io.github.joinrpc.spring.annotation.RpcService;

import java.util.List;
import java.util.stream.LongStream;

@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello, " + name + "!";
    }

    @Override
    public User findUser(long id) {
        return new User(id, "user-" + id);
    }

    @Override
    public List<User> listUsers(int size) {
        return LongStream.rangeClosed(1, size).mapToObj(this::findUser).toList();
    }

    @Override
    public void fail(String message) {
        throw new IllegalArgumentException(message);
    }
}
