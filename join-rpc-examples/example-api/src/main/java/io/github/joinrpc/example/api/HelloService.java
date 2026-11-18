package io.github.joinrpc.example.api;

import java.util.List;

public interface HelloService {

    String hello(String name);

    User findUser(long id);

    List<User> listUsers(int size);

    /** 演示远程异常 */
    void fail(String message);
}
