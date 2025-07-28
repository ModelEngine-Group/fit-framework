/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain.resolver;

import modelengine.fitframework.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 测试上下文的配置类。
 *
 * @author 邬涨财
 * @author 易文渊
 * @author 季聿阶
 * @since 2023-01-20
 */
public interface TestContextConfiguration {
    /**
     * 获取单测类的类对象。
     *
     * @return 表示单测类的类对象的 {@link Class}{@code <?>}。
     */
    Class<?> testClass();

    /**
     * 获取需要向容器上下文注入的类对象列表。
     *
     * @return 表示需要向容器上下文注入的类对象列表的 {@link Map}{@code <}{@link Class}{@code <?>, }{@link Supplier}{@code
     * <}{@link Object}{@code >>}，其中，值为生成该类型对象的方法。
     */
    Map<Class<?>, Supplier<Object>> includeClasses();

    /**
     * 获取不需要向容器上下文注入的类对象列表。
     *
     * @return 表示不需要向容器上下文注入的类对象列表的 {@link Class}{@code <?>[]}。
     */
    Class<?>[] excludeClasses();

    /**
     * 获取测试类依赖的扫描出的包。
     *
     * @return 测试类扫描出的依赖包 {@link Set}{@code <}{@link String}{@code >}。
     */
    Set<String> scannedPackages();

    /**
     * 获取测试类扫描出的模拟的 bean 字段集合。
     *
     * @return 测试类扫描出的模拟的 bean 字段集合 {@link Set}{@code <}{@link Field}{@code >}。
     */
    Set<Field> mockedBeanFields();

    /**
     * 获取需要被侦听的类对象集合。
     *
     * @return 表示需要被侦听的类对象集合的 {@link Set}{@code <}{@link Class}{@code <?>>}。
     */
    Set<Class<?>> toSpyClasses();

    /**
     * 获取需要执行的操作列表。
     *
     * @return 表示需要执行的操作列表的 {@link List}{@code <}{@link Consumer}{@code <}{@link Plugin}{@code >>}。
     */
    List<Consumer<Plugin>> actions();

    /**
     * 合并另外一个 {@link TestContextConfiguration}。
     *
     * @param configuration 表示另一个上下文配置的 {@link TestContextConfiguration}。
     */
    void merge(TestContextConfiguration configuration);

    /**
     * {@link TestContextConfiguration} 的构建器。
     */
    interface Builder {
        /**
         * 向当前构建器中设置单测类的类对象。
         *
         * @param testClass 表示待设置的单测类的类对象的 {@link Class}{@code <?>}。
         * @return 表示当前构建程序的 {@link Builder}。
         */
        Builder testClass(Class<?> testClass);

        /**
         * 向当前构建器中设置需注入的类对象列表。
         *
         * @param classes 表示待设置的需注入的类对象列表的 {@link Map}{@code <}{@link Class}{@code <?>, }{@link
         * Supplier}{@code <}{@link Object}{@code >>}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder includeClasses(Map<Class<?>, Supplier<Object>> classes);

        /**
         * 向当前构建器中设置不需注入的类对象列表。
         *
         * @param classes 表示待设置的不需注入的类对象列表的 {@link Class}{@code <?>[]}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder excludeClasses(Class<?>[] classes);

        /**
         * 向当前构建器中设置待扫描的包。
         *
         * @param basePackages 表示待设置的测试类扫描的包路径的 {@link Set}{@code <}{@link String}{@code >}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder scannedPackages(Set<String> basePackages);

        /**
         * 向当前构建器中设置 mocked bean 字段集合。
         *
         * @param mockedBeanFields 设置测试类扫描出的 mocked bean 字段集合 {@link Set}{@code <}{@link Field}{@code >}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder mockedBeanFields(Set<Field> mockedBeanFields);

        /**
         * 向当前构建器中设置需要被侦听的类对象集合。
         *
         * @param toSpyClasses 表示待设置的需要被侦听的类对象集合的 {@link Set}{@code <}{@link Class}{@code <?>>}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder toSpyClasses(Set<Class<?>> toSpyClasses);

        /**
         * 向当前构建器中设置需要执行操作列表。
         *
         * @param actions 待设置的需要执行操作列表的 {@link List}{@code <}{@link Consumer}{@code <}{@link Plugin}{@code >>}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder actions(List<Consumer<Plugin>> actions);

        /**
         * 构建对象。
         *
         * @return 表示构建出来的对象的 {@link TestContextConfiguration}。
         */
        TestContextConfiguration build();
    }

    /**
     * 获取 {@link TestContextConfiguration} 的构建器。
     *
     * @return 表示 {@link TestContextConfiguration} 的构建器的 {@link Builder}。
     */
    static Builder custom() {
        return new DefaultTestContextConfiguration.Builder();
    }
}
