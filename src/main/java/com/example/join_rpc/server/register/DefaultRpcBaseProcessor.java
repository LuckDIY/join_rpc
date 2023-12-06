package com.example.join_rpc.server.register;

import com.example.join_rpc.server.NettyRpcServer;
import com.example.join_rpc.server.ShutdownHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Objects;

/**
 * 注册服务处理器
 */
@Slf4j
public abstract class DefaultRpcBaseProcessor implements ApplicationListener<ContextRefreshedEvent> {

    protected ServerRegister serverRegister;

    protected NettyRpcServer nettyRpcServer;

    public void initializeParameters(ServerRegister serverRegister, NettyRpcServer nettyRpcServer) {
        //提供者服务注册
        this.serverRegister = serverRegister;
        //提供者服务启动
        this.nettyRpcServer = nettyRpcServer;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //Publish the final event.
        //根上下文事件执行完成
        if (Objects.isNull(contextRefreshedEvent.getApplicationContext().getParent())) {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();

            //服务端初始化
            initServer(applicationContext);

            //todo 消费者依赖注入,注入代理对象

        }
    }

    protected abstract void registerServer(ApplicationContext applicationContext);


    private void initServer(ApplicationContext applicationContext){
        //提供者服务初始化，注册，启动服务
        registerServer(applicationContext);

        ShutdownHook.registerShutdownHook(serverRegister,nettyRpcServer);
        //提供者服务启动
        nettyRpcServer.start();
    }

}
