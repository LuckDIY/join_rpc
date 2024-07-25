package com.example.join_rpc.scan;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class RpcServiceScanImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        RpcServiceBeanDefinitionScanner rpcServiceBeanDefinitionScanner = new RpcServiceBeanDefinitionScanner(registry);

        //先写死
        rpcServiceBeanDefinitionScanner.scan("com.example.join_rpc");
    }
}
