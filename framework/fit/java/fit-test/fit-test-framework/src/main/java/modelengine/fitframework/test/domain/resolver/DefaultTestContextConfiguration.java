/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain.resolver;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 默认的测试上下文的配置类。
 *
 * @author 邬涨财
 * @author 季聿阶
 * @since 2023-01-20
 */
public class DefaultTestContextConfiguration implements TestContextConfiguration {
    private final Class<?> testClass;
    private final Map<Class<?>, Supplier<Object>> includeClasses;
    private final List<Class<?>> excludeClasses;
    private final Set<String> scannedPackages;
    private final Set<Field> mockedBeanFields;
    private final Set<Class<?>> toSpyClasses;
    private final List<Consumer<Plugin>> actions;

    /**
     * 默认测试上下文的配置类构造函数。
     *
     * @param testClass 表示测试类的 {@link Class}。
     * @param includeClasses 表示注入 Bean 类型数组的 {@link Map}{@code <}{@link Class}{@code <?>, }{@link Supplier}{@code
     * <}{@link Object}{@code >>}。
     * @param excludeClasses 表示排除 Bean 类型数组的 {@code Class[]}。
     * @param scannedPackages 表示扫描包路径的 {@link Set}{@code <}{@link String}{@code >}。
     * @param mockedBeanFields 表示 mocked bean 字段的 {@link Set}{@code <}{@link Field}{@code >}。
     * @param toSpyClasses 表示需要侦听的类集合的 {@link Set}{@code <}{@link Class}{@code <?>>}。
     * @param actions 测试类初始化时执行的操作的 {@link List}{@code <}{@link Consumer}{@code <}{@link Plugin}{@code >>}。
     */
    public DefaultTestContextConfiguration(Class<?> testClass, Map<Class<?>, Supplier<Object>> includeClasses,
            Class<?>[] excludeClasses, Set<String> scannedPackages, Set<Field> mockedBeanFields,
            Set<Class<?>> toSpyClasses, List<Consumer<Plugin>> actions) {
        this.testClass = notNull(testClass, "The test class cannot be null.");
        this.includeClasses = includeClasses;
        this.excludeClasses =
                new ArrayList<>(excludeClasses == null ? Collections.emptyList() : Arrays.asList(excludeClasses));
        this.scannedPackages = notNull(scannedPackages, "The scanned packages cannot be null.");
        this.mockedBeanFields = notNull(mockedBeanFields, "The mocked bean fields cannot be null.");
        this.toSpyClasses = ObjectUtils.nullIf(toSpyClasses, Collections.emptySet());
        this.actions = notNull(actions, "The actions cannot be null.");
    }

    @Override
    public Class<?> testClass() {
        return this.testClass;
    }

    @Override
    public Map<Class<?>, Supplier<Object>> includeClasses() {
        return this.includeClasses;
    }

    @Override
    public Class<?>[] excludeClasses() {
        return this.excludeClasses.toArray(new Class[0]);
    }

    @Override
    public Set<String> scannedPackages() {
        return Collections.unmodifiableSet(this.scannedPackages);
    }

    @Override
    public Set<Field> mockedBeanFields() {
        return Collections.unmodifiableSet(this.mockedBeanFields);
    }

    @Override
    public Set<Class<?>> toSpyClasses() {
        return Collections.unmodifiableSet(this.toSpyClasses);
    }

    @Override
    public List<Consumer<Plugin>> actions() {
        return Collections.unmodifiableList(this.actions);
    }

    @Override
    public void merge(TestContextConfiguration configuration) {
        Validation.equals(this.testClass, configuration.testClass(), "The test class must equal");
        this.includeClasses.putAll(configuration.includeClasses());
        this.excludeClasses.addAll(Arrays.asList(configuration.excludeClasses()));
        this.scannedPackages.addAll(configuration.scannedPackages());
        this.mockedBeanFields.addAll(configuration.mockedBeanFields());
        this.toSpyClasses.addAll(configuration.toSpyClasses());
        this.actions.addAll(configuration.actions());
    }

    @Override
    public int hashCode() {
        return this.includeClasses.hashCode();
    }

    /**
     * 为 {@link TestContextConfiguration.Builder} 提供默认实现。
     */
    public static final class Builder implements TestContextConfiguration.Builder {
        private Class<?> testClass;
        private Map<Class<?>, Supplier<Object>> includeClasses = new HashMap<>();
        private Class<?>[] excludeClasses;
        private Set<String> scannedPackages = new HashSet<>();
        private Set<Field> mockedBeanFields = new HashSet<>();
        private Set<Class<?>> toSpyClasses = new HashSet<>();
        private List<Consumer<Plugin>> actions = new ArrayList<>();

        @Override
        public TestContextConfiguration.Builder testClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        @Override
        public TestContextConfiguration.Builder includeClasses(Map<Class<?>, Supplier<Object>> classes) {
            this.includeClasses = classes;
            return this;
        }

        @Override
        public TestContextConfiguration.Builder excludeClasses(Class<?>[] classes) {
            this.excludeClasses = classes;
            return this;
        }

        @Override
        public TestContextConfiguration.Builder scannedPackages(Set<String> scannedPackages) {
            this.scannedPackages = scannedPackages;
            return this;
        }

        @Override
        public TestContextConfiguration.Builder mockedBeanFields(Set<Field> mockedBeanFields) {
            this.mockedBeanFields = mockedBeanFields;
            return this;
        }

        @Override
        public TestContextConfiguration.Builder toSpyClasses(Set<Class<?>> toSpyClasses) {
            this.toSpyClasses = toSpyClasses;
            return this;
        }

        @Override
        public TestContextConfiguration.Builder actions(List<Consumer<Plugin>> actions) {
            this.actions = actions;
            return this;
        }

        @Override
        public TestContextConfiguration build() {
            return new DefaultTestContextConfiguration(this.testClass,
                    this.includeClasses,
                    this.excludeClasses,
                    this.scannedPackages,
                    this.mockedBeanFields,
                    this.toSpyClasses,
                    this.actions);
        }
    }
}
