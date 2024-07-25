package com.example.join_rpc.client;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class RpcServiceFactoryBean implements FactoryBean<Object> {

    private final Class<?> infaceClass;

    public RpcServiceFactoryBean(String beanClassName) throws ClassNotFoundException {
        this.infaceClass = Class.forName(beanClassName);
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(infaceClass.getClassLoader(), new Class[]{infaceClass}, new RpcServiceProxy(infaceClass));
    }

    @Override
    public Class<?> getObjectType() {
        return infaceClass;
    }
}
