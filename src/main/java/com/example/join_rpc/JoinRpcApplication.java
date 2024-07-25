package com.example.join_rpc;

import com.example.join_rpc.annotation.RpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RpcServiceScan
@SpringBootApplication
public class JoinRpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(JoinRpcApplication.class, args);
    }

}
