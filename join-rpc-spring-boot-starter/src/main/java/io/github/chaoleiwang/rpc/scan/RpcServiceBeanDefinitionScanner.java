package io.github.chaoleiwang.rpc.scan;

import io.github.chaoleiwang.rpc.annotation.RpcService;
import io.github.chaoleiwang.rpc.client.RpcServiceFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * RPC 服务扫描器:扫描标注了 {@link RpcService} 的接口,注册为 {@link RpcServiceFactoryBean} 代理 Bean
 */
public class RpcServiceBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public RpcServiceBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        // 关闭默认过滤器,只扫描 @RpcService
        super(registry, false);
        addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
        // Bean 名用接口全限定名,避免不同包下同名接口冲突
        setBeanNameGenerator((definition, beanRegistry) -> definition.getBeanClassName());
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        // @RpcService 标注在接口上,接口都是 abstract
        return metadata.isIndependent() && metadata.isAbstract();
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        ClassLoader classLoader = getResourceLoader() != null
                ? getResourceLoader().getClassLoader()
                : ClassUtils.getDefaultClassLoader();

        for (BeanDefinitionHolder holder : beanDefinitionHolders) {
            BeanDefinition beanDefinition = holder.getBeanDefinition();
            String interfaceClassName = beanDefinition.getBeanClassName();
            try {
                Class<?> interfaceClass = ClassUtils.forName(interfaceClassName, classLoader);
                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException("@RpcService 只能标注在接口上: " + interfaceClassName);
                }
                // 替换为代理 FactoryBean,构造参数传入接口类型
                beanDefinition.setBeanClassName(RpcServiceFactoryBean.class.getName());
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(interfaceClass);
                // 接口类型注入时优先取代理(即使本地存在实现类,也保证调用走远程链路)
                beanDefinition.setPrimary(true);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("找不到 RPC 接口类: " + interfaceClassName, e);
            }
        }
        return beanDefinitionHolders;
    }

}
