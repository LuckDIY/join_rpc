package com.example.join_rpc.register;

import com.example.join_rpc.annotation.RpcService;
import com.example.join_rpc.register.bean.ServiceObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class DefaultRpcBaseProcessor implements ApplicationListener<ContextRefreshedEvent> {

    protected ServerRegister serverRegister;

    public DefaultRpcBaseProcessor(ServerRegister serverRegister) {
        this.serverRegister = serverRegister;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //Publish the final event.
        //根上下文事件执行完成
        if (Objects.isNull(contextRefreshedEvent.getApplicationContext().getParent())) {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
            startBaseServer(applicationContext);
        }
    }

    //注册服务
    private void startBaseServer(ApplicationContext applicationContext) {

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcService.class);

        try {
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                Object bean = entry.getValue();
                String beanName = entry.getKey();
                Class<?> beanClass = bean.getClass();
                //bean实现的接口
                Class<?>[] beanInterfaces = beanClass.getInterfaces();

                RpcService annotation = beanClass.getAnnotation(RpcService.class);

                //不是只实现一个接口，则使用注解中的value
                String value = annotation.value();
                if (beanInterfaces.length != 1 && StringUtils.isBlank(annotation.value())) {
                    throw new UnsupportedOperationException("The exposed interface is not specific with '" + bean.getClass().getName() + "'");
                }

                if(beanInterfaces.length==1){
                    value = beanInterfaces[0].getName();
                }


                ServiceObject so = new ServiceObject(value, Class.forName(value), bean, annotation.group(), annotation.version());

                serverRegister.register(so);


            }
        } catch (Exception e) {
            log.error("rpc start exception :", e);
            throw new RuntimeException(e);
        }

    }
}
