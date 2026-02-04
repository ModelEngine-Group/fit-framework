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
    private static final Logger log = LoggerFactory.getLogger(FelSpringBootAutoConfiguration.class);

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
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {}

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            log.info("Installing FEL Bean auto-registration processor...");
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
            if (!this.registered && bean instanceof FitRuntime) {
                this.registered = true;
                this.registerFelBeansToSpring((FitRuntime) bean);
            }
            return bean;
        }

        /**
         * 将 FIT 容器中符合 {@link FelSpringBootAutoConfiguration#REQUIRED_BEAN_TYPE_NAMES} 的 FEL Bean 注册到 Spring 容器。
         *
         * @param fitRuntime 表示 FIT 运行时实例的 {@link FitRuntime}。
         */
        private void registerFelBeansToSpring(FitRuntime fitRuntime) {
            log.info("Auto-registering FEL beans to Spring container...");
            try {
                BeanContainer container = fitRuntime.root().container();
                List<BeanFactory> allFactories = container.all();
                log.debug("Found {} beans in FIT container", allFactories.size());
                int registeredCount = 0;

                for (BeanFactory factory : allFactories) {
                    BeanMetadata metadata = factory.metadata();
                    String beanName = metadata.name();
                    String typeName = metadata.type().getTypeName();
                    if (!this.isRequiredBeanType(typeName) || this.isBeanAlreadyRegisteredInSpring(beanName)) {
                        continue;
                    }

                    // 注册单例 Bean
                    if (this.registerSingletonBean(factory, metadata, beanName, typeName)) {
                        registeredCount++;
                    }
                }

                log.info("FEL Bean auto-registration completed: {} registered", registeredCount);
            } catch (Exception e) {
                log.error("Failed to auto-register FEL beans", e);
            }
        }

        /**
         * 检查指定类型是否在白名单中（支持子类匹配）。
         *
         * @param typeName 表示类型全限定名的 {@link String}。
         * @return 如果类型在白名单中则返回 {@code true}，否则返回 {@code false}。
         */
        private boolean isRequiredBeanType(String typeName) {
            return REQUIRED_BEAN_TYPE_NAMES.stream().anyMatch(requiredTypeName -> {
                try {
                    Class<?> actualType = Class.forName(typeName);
                    Class<?> requiredType = Class.forName(requiredTypeName);
                    return requiredType.isAssignableFrom(actualType);
                } catch (ClassNotFoundException e) {
                    // 某些可选依赖的类可能不在 classpath 中，忽略
                    return false;
                }
            });
        }

        /**
         * 检查指定名称的 Bean 是否已在 Spring 容器中注册。
         *
         * @param beanName 表示 Bean 名称的 {@link String}。
         * @return 如果 Bean 已注册则返回 {@code true}，否则返回 {@code false}。
         */
        private boolean isBeanAlreadyRegisteredInSpring(String beanName) {
            return this.beanFactory.containsSingleton(beanName) || this.beanFactory.containsBeanDefinition(beanName);
        }

        /**
         * 注册单例 Bean 到 Spring 容器。
         *
         * @param factory 表示 Bean 工厂的 {@link BeanFactory}。
         * @param metadata 表示 Bean 元数据的 {@link BeanMetadata}。
         * @param beanName 表示 Bean 名称的 {@link String}。
         * @param typeName 表示类型全限定名的 {@link String}。
         * @return 如果注册成功则返回 {@code true}，否则返回 {@code false}。
         */
        private boolean registerSingletonBean(BeanFactory factory, BeanMetadata metadata, String beanName,
                String typeName) {
            try {
                if (metadata.singleton()) {
                    Object instance = factory.get();
                    this.beanFactory.registerSingleton(beanName, instance);
                    log.info("Registered FEL bean '{}' (type: {}) to Spring container", beanName, typeName);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to register FEL bean '{}' of type {}: {}", beanName, typeName, e.getMessage());
            }
            return false;
        }
    }
}
