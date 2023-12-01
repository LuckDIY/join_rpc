package com.example.join_rpc.server.register;

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

    public void initializeParameters(ServerRegister serverRegister) {
        //提供者服务注册
        this.serverRegister = serverRegister;
        //提供者服务启动


        //todo 消费者依赖注入,注入代理对象

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //Publish the final event.
        //根上下文事件执行完成
        if (Objects.isNull(contextRefreshedEvent.getApplicationContext().getParent())) {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();


            //提供者服务初始化，注册，启动服务
            startBaseServer(applicationContext);
        }
    }

    protected abstract void startBaseServer(ApplicationContext applicationContext);

}
