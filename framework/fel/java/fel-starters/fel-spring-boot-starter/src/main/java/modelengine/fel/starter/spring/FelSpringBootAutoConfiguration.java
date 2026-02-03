/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2026 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.starter.spring;

import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.ioc.BeanMetadata;
import modelengine.fitframework.runtime.FitRuntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FEL Spring Boot 自动配置类，自动将 FIT 容器中的 FEL Bean 注册到 Spring 容器。
 *
 * <p>注册 FEL 实际需要的 Bean，避免过度注册污染 Spring 容器。</p>
 *
 * @author 黄可欣
 * @since 2026-01-26
 */
@Configuration
public class FelSpringBootAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(FelSpringBootAutoConfiguration.class);

    /**
     * Spring 层需要注入的 FEL Bean 类型名称白名单（使用字符串避免类加载失败）。
     * 只有这些类型的 Bean 会被自动注册到 Spring 容器。
     */
    private static final Set<String> REQUIRED_BEAN_TYPE_NAMES =
            new HashSet<>(Arrays.asList("modelengine.fel.community.model.openai.OpenAiModel",        // OpenAI 模型
                    "modelengine.fitframework.serialization.ObjectSerializer",   // JSON 序列化器
                    "modelengine.fel.tool.service.ToolRepository",               // 工具仓库
                    "modelengine.fel.tool.service.ToolExecuteService"            // 工具执行服务
            ));

    /**
     * 创建 Bean 注册后处理器，自动将 FIT 容器中的 FEL Bean 注册到 Spring 容器。
     *
     * @return 表示 Bean 注册后处理器的 {@link BeanDefinitionRegistryPostProcessor}。
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor felBeanAutoRegistrar() {
        return new FelBeanAutoRegistrar();
    }

    /**
     * FEL Bean 自动注册器，负责将 FIT 容器中的 FEL Bean 注册到 Spring 容器。
     */
    private static class FelBeanAutoRegistrar implements BeanDefinitionRegistryPostProcessor {
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            // 不需要在这里处理
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            LOG.info("Installing FEL Bean auto-registration processor");
            beanFactory.addBeanPostProcessor(new FitRuntimeDetectorBeanPostProcessor(beanFactory));
        }
    }

    /**
     * FIT 运行时检测后处理器，检测 FitRuntime Bean 初始化完成后触发 FEL Bean 注册。
     */
    private static class FitRuntimeDetectorBeanPostProcessor
            implements org.springframework.beans.factory.config.BeanPostProcessor {
        private final ConfigurableListableBeanFactory beanFactory;
        private boolean registered = false;

        FitRuntimeDetectorBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (!registered && bean instanceof FitRuntime) {
                registered = true;
                registerFelBeansToSpring((FitRuntime) bean);
            }
            return bean;
        }

        /**
         * 将 FIT 容器中符合白名单的 FEL Bean 注册到 Spring 容器。
         *
         * @param fitRuntime FIT 运行时实例
         */
        private void registerFelBeansToSpring(FitRuntime fitRuntime) {
            LOG.info("Auto-registering FEL beans to Spring container (whitelist mode)");
            try {
                BeanContainer container = fitRuntime.root().container();
                List<BeanFactory> allFactories = container.all();
                LOG.debug("Found {} beans in FIT container", allFactories.size());

                int registeredCount = 0;
                int skippedCount = 0;
                int filteredCount = 0;

                for (BeanFactory factory : allFactories) {
                    BeanMetadata metadata = factory.metadata();
                    String beanName = metadata.name();
                    Type beanType = metadata.type();
                    String typeName = beanType.getTypeName();

                    // 检查是否在白名单中（使用字符串匹配，支持子类）
                    boolean isRequired = REQUIRED_BEAN_TYPE_NAMES.stream().anyMatch(requiredTypeName -> {
                        try {
                            Class<?> actualType = Class.forName(typeName);
                            Class<?> requiredType = Class.forName(requiredTypeName);
                            return requiredType.isAssignableFrom(actualType);
                        } catch (ClassNotFoundException e) {
                            // 某些可选依赖的类可能不在 classpath 中，忽略
                            return false;
                        }
                    });

                    if (!isRequired) {
                        LOG.trace("Filtered out FIT bean '{}' - not in whitelist", beanName);
                        filteredCount++;
                        continue;
                    }

                    // 避免重复注册
                    if (beanFactory.containsSingleton(beanName) || beanFactory.containsBeanDefinition(beanName)) {
                        LOG.debug("Skipping bean '{}' - already exists in Spring container", beanName);
                        skippedCount++;
                        continue;
                    }

                    try {
                        if (metadata.singleton()) {
                            Object instance = factory.get();
                            beanFactory.registerSingleton(beanName, instance);
                            registeredCount++;
                            LOG.info("Registered FEL bean '{}' (type: {}) to Spring container", beanName, typeName);
                        }
                    } catch (Exception e) {
                        LOG.warn("Failed to register FEL bean '{}' of type {}: {}",
                                beanName,
                                typeName,
                                e.getMessage());
                    }
                }

                LOG.info("FEL Bean auto-registration completed: {} registered, {} skipped, {} filtered",
                        registeredCount,
                        skippedCount,
                        filteredCount);
            } catch (Exception e) {
                LOG.error("Failed to auto-register FEL beans", e);
            }
        }
    }
}
