package com.example.join_rpc.server.register.processor;

import com.example.join_rpc.annotation.RpcProcessorAno;
import com.example.join_rpc.annotation.RpcService;
import com.example.join_rpc.server.register.bean.ServiceObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;
import org.springframework.context.ApplicationContext;

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

        RpcService annotation = beanClass.getAnnotation(RpcService.class);

        //不是只实现一个接口，则使用注解中的value
        String value = annotation.value();
        if (beanInterfaces.length != 1 && StringUtils.isBlank(annotation.value())) {
            throw new UnsupportedOperationException("The exposed interface is not specific with '" + bean.getClass().getName() + "'");
        }

        if (beanInterfaces.length == 1) {
            value = beanInterfaces[0].getName();
        }
        return value;
    }
}
