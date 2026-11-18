package io.github.chaoleiwang.rpc.scan;

import io.github.chaoleiwang.rpc.annotation.RpcServiceScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * {@link RpcServiceScan} 的注册器:解析扫描路径并触发 RPC 接口扫描
 */
public class RpcServiceScanImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(RpcServiceScan.class.getName()));
        String[] basePackages = attributes != null ? attributes.getStringArray("basePackages") : new String[0];
        if (basePackages.length == 0) {
            // 未指定扫描路径时,默认扫描标注类所在的包
            basePackages = new String[]{ClassUtils.getPackageName(importingClassMetadata.getClassName())};
        }
        new RpcServiceBeanDefinitionScanner(registry).scan(basePackages);
    }

}
