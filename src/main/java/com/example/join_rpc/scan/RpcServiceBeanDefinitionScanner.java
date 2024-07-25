package com.example.join_rpc.scan;

import com.example.join_rpc.annotation.RpcService;
import com.example.join_rpc.client.RpcServiceFactoryBean;
import org.apache.zookeeper.common.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

public class RpcServiceBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {


    public RpcServiceBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        //不使用默认过滤器
        super(registry, false);

        //registerDefaultFilters();
        //添加注解过滤器
        addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
    }

    @Override
    public int scan(String... basePackages) {

        //扫描到的所有BeanDefinitionHolder 类接口
        Set<BeanDefinitionHolder> beanDefinitionHolders = doScan(basePackages);

        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
            BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
            String beanName = beanDefinitionHolder.getBeanName();
            String beanClassName = beanDefinition.getBeanClassName();

            try {
                Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
                if (!beanClass.isInterface()) {
                    throw new RuntimeException("RpcService没有标在接口：" + beanClassName);
                }

                //检查是否有实现类
                checkImplementations(beanDefinitionHolder);


            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }


            //改className
            beanDefinition.setBeanClassName(RpcServiceFactoryBean.class.getName());

            //设置构造函数参数
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
        }

        return 1;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isIndependent() && metadata.isAbstract();
    }


    public void checkImplementations(BeanDefinitionHolder beanDefinitionHolder) {
        String beanNameInterface = beanDefinitionHolder.getBeanName();
        BeanDefinition beanDefinitionInterface = beanDefinitionHolder.getBeanDefinition();

        String beanClassName = beanDefinitionInterface.getBeanClassName();

        BeanDefinitionRegistry registry = getRegistry();

        assert registry != null;
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if(StringUtils.isBlank(beanDefinition.getBeanClassName())){
                continue;
            }
            try {
                Class<?> interfaceClass = Class.forName(beanClassName);
                Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
                if (interfaceClass.isAssignableFrom(beanClass) && !interfaceClass.equals(beanClass)) {
                    System.out.println("Found implementation: " + beanClass.getName());
                    //那就删掉要代理的接口
                    registry.removeBeanDefinition(beanNameInterface);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
