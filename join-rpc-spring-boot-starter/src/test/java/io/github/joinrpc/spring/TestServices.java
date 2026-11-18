package io.github.joinrpc.spring;

import io.github.joinrpc.spring.annotation.RpcReference;
import io.github.joinrpc.spring.annotation.RpcService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

public class TestServices {

    public interface GreetingService {
        String greet(String name);
    }

    @RpcService
    public static class GreetingServiceImpl implements GreetingService {
        @Override
        public String greet(String name) {
            return "hello " + name;
        }
    }

    @Component
    public static class GreetingClient {

        @RpcReference(application = "${test.provider-app}")
        GreetingService greetingService;

        boolean injectedBeforeInit;

        @PostConstruct
        void init() {
            injectedBeforeInit = greetingService != null;
        }
    }
}
