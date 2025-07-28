/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain;

import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.ioc.BeanMetadata;
import modelengine.fitframework.jvm.scan.PackageScanner;
import modelengine.fitframework.model.Version;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.plugin.PluginCategory;
import modelengine.fitframework.plugin.PluginKey;
import modelengine.fitframework.plugin.PluginMetadata;
import modelengine.fitframework.plugin.support.DefaultPluginKey;
import modelengine.fitframework.plugin.support.DefaultPluginMetadata;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.runtime.support.AbstractRootPlugin;
import modelengine.fitframework.test.domain.mockito.MockitoMockBean;
import modelengine.fitframework.test.domain.mockito.SpyInterceptor;
import modelengine.fitframework.test.domain.resolver.MockBean;
import modelengine.fitframework.test.domain.resolver.TestContextConfiguration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 测试框架使用的插件类。
 *
 * @author 邬涨财
 * @since 2023-01-17
 */
public class TestPlugin extends AbstractRootPlugin implements Plugin {
    private static final String UNKNOWN_GROUP = "test.group";
    private static final String PLUGIN_NAME = "fit-test-framework";
    private static final String UNKNOWN_VERSION = "0.0.0-test";

    private final FitRuntime runtime;
    private final ClassLoader loader;
    private final PluginMetadata metadata;
    private final PackageScanner packageScanner;
    private final TestContextConfiguration configuration;

    /**
     * 通过运行时对象和 Bean 配置来初始化 {@link TestPlugin} 的新实例。
     *
     * @param runtime 表示运行时对象的 {@link FitRuntime}。
     * @param configuration 表示待注册 Bean 相关的配置的 {@link TestContextConfiguration}。
     */
    public TestPlugin(FitRuntime runtime, TestContextConfiguration configuration) {
        this.runtime = Validation.notNull(runtime, "The runtime to create test plugin cannot be null.");
        this.loader = TestPlugin.class.getClassLoader();
        PluginKey pluginKey = new DefaultPluginKey(UNKNOWN_GROUP, PLUGIN_NAME, Version.parse(UNKNOWN_VERSION));
        this.metadata =
                new DefaultPluginMetadata(pluginKey, this.runtime.location(), PluginCategory.SYSTEM, Integer.MIN_VALUE);
        this.configuration =
                Validation.notNull(configuration, "The configuration to create test plugin cannot be null.");
        this.packageScanner = this.scanner((packageScanner, clazz) -> this.onClassDetected(packageScanner, clazz,
                // 包含的类已经提前注册，因此需要将包含的和排除的类进行合并。
                Stream.concat(this.configuration.includeClasses().keySet().stream(),
                        Arrays.stream(this.configuration.excludeClasses())).collect(Collectors.toSet())));
    }

    @Override
    public ClassLoader pluginClassLoader() {
        return this.loader;
    }

    @Override
    public FitRuntime runtime() {
        return this.runtime;
    }

    @Override
    public PluginMetadata metadata() {
        return this.metadata;
    }

    @Override
    protected void registerSystemBeans() {
        super.registerSystemBeans();
        this.container().registry().register(new SpyInterceptor(this.configuration.toSpyClasses()));
        this.container().registry().register(new MockitoMockBean());
    }

    @Override
    protected void scanBeans() {
        this.registerBeans(this.configuration.includeClasses());
        this.configuration.actions().forEach(action -> action.accept(this));
        this.scan(this.configuration.scannedPackages());
        this.registerMockedBeans(this.configuration.mockedBeanFields());
    }

    @Override
    protected void loadPlugins() {}

    private void onClassDetected(PackageScanner scanner, Class<?> clazz, Set<Class<?>> excludeClasses) {
        if (excludeClasses.contains(clazz)) {
            return;
        }
        List<BeanMetadata> beans = this.container().registry().register(clazz);
        for (BeanMetadata bean : beans) {
            Set<String> basePackages = this.runtime().resolverOfBeans().packages(bean);
            scanner.scan(basePackages);
        }
    }

    private void registerBeans(Map<Class<?>, Supplier<Object>> classes) {
        classes.entrySet()
                .stream()
                .filter(entry -> this.container().lookup(entry.getKey()).isEmpty())
                .forEach(entry -> {
                    if (entry.getValue() == null) {
                        this.container().registry().register(entry.getKey());
                    } else {
                        Object bean = entry.getValue().get();
                        if (bean == null) {
                            this.container().registry().register(entry.getKey());
                        } else {
                            this.container().registry().register(bean);
                        }
                    }
                });
    }

    private void scan(Set<String> basePackages) {
        this.packageScanner.scan(basePackages);
    }

    private void registerMockedBeans(Set<Field> mockedBeanFields) {
        for (Field field : mockedBeanFields) {
            Object bean = this.container()
                    .lookup(MockBean.class)
                    .map(BeanFactory::<MockBean>get)
                    .orElseThrow(() -> new IllegalStateException(
                            "Failed to register mock bean: cannot find implements of AbstractMockBean."))
                    .getBean(field);
            this.container().registry().register(bean);
        }
    }
}
