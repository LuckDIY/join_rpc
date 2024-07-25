package com.example.join_rpc.server.register.processor;

import com.example.join_rpc.annotation.RpcProcessorAno;
import com.example.join_rpc.annotation.RpcService;
import com.example.join_rpc.client.RpcServiceProxy;
import com.example.join_rpc.server.register.bean.ServiceObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@RpcProcessorAno("reflect")
public class DefaultRpcReflectProcessor extends DefaultRpcBaseProcessor {


    //注册服务
    @Override
    protected void registerServer(ApplicationContext applicationContext) {

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcService.class);


        try {
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                Object bean = entry.getValue();
                if(bean.hashCode()==0){
                    log.info("消费者接口代理对象，生产者注册不注册:{}",bean);
                    continue;
                }
                String value = getClassPath(bean);
                ServiceObject so = new ServiceObject(value, Class.forName(value), bean);

                //注册
                serverRegister.register(so);

            }
        } catch (Exception e) {
            log.error("rpc start exception :", e);
            throw new RuntimeException(e);
        }
    }

    private static String getClassPath(Object bean) {

        Class<?> beanClass = bean.getClass();
        //bean实现的接口
        Class<?>[] beanInterfaces = beanClass.getInterfaces();

        //实现RpcService的第一个接口
        Class<?> beanInterface = Arrays.stream(beanInterfaces)
                .filter(x -> null != x.getAnnotation(RpcService.class))
                .findFirst().orElse(null);

        assert beanInterface != null;

        return beanInterface.getName();
    }
}
