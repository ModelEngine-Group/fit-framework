/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.test.service;

import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.test.domain.TestContext;
import modelengine.fitframework.test.domain.TestFitRuntime;
import modelengine.fitframework.test.domain.listener.DataSourceListener;
import modelengine.fitframework.test.domain.listener.InjectFieldTestListener;
import modelengine.fitframework.test.domain.listener.MockMvcListener;
import modelengine.fitframework.test.domain.listener.MybatisTestListener;
import modelengine.fitframework.test.domain.listener.ResolverListener;
import modelengine.fitframework.test.domain.listener.ScanPackageListener;
import modelengine.fitframework.test.domain.listener.SqlExecuteListener;
import modelengine.fitframework.test.domain.listener.TestListener;
import modelengine.fitframework.test.domain.resolver.TestContextConfiguration;
import modelengine.fitframework.util.ThreadUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 默认的测试框架管理类。
 * <p>当前包括测试上下文和单测类监听类。</p>
 *
 * @author 邬涨财
 * @author 易文渊
 * @since 2023-02-02
 */
public class DefaultFitTestService implements FitTestService {
    private final TestContext testContext;
    private final FitRuntime runtime;

    /**
     * 根据测试类构造 {@link DefaultFitTestService} 的实例。
     * <p>构造流程如下：</p>
     * <ul>
     *     <li>初始化监听端口；</li>
     *     <li>注册所有的监听器；</li>
     *     <li>初始化运行时配置，调用所有监听器的配置接口；</li>
     *     <li>启动测试插件。</li>
     * </ul>
     *
     * @param clazz 表示测试类的 {@link Class}。
     */
    public DefaultFitTestService(Class<?> clazz) {
        Validation.notNull(clazz, "The test class to create fit test manager cannot be null.");
        int port = 0;
        List<TestListener> listeners = Arrays.asList(new ResolverListener(),
                new ScanPackageListener(),
                new InjectFieldTestListener.ByFit(),
                new InjectFieldTestListener.BySpy(),
                new MockMvcListener(),
                new DataSourceListener(),
                new SqlExecuteListener(),
                new MybatisTestListener());
        TestContextConfiguration configuration = TestContextConfiguration.custom().testClass(clazz).build();
        listeners.forEach(testListener -> testListener.config(clazz).ifPresent(configuration::merge));
        this.runtime = new TestFitRuntime(clazz, configuration, port);
        this.runtime.start();
        this.testContext = new TestContext(clazz, this.runtime.root(), listeners);
        // 启动服务器后需要稍作等待，确保服务端口启动成功。
        ThreadUtils.sleep(100);
    }

    @Override
    public void beforeAll() {
        this.testContext.listeners().forEach(testListener -> testListener.beforeTestClass(this.testContext));
    }

    @Override
    public void prepareTestInstance() {
        this.testContext.listeners().forEach(testListener -> testListener.prepareTestInstance(this.testContext));
    }

    @Override
    public void beforeEach() {
        this.testContext.listeners().forEach(testListener -> testListener.beforeTestMethod(this.testContext));
    }

    @Override
    public void afterEach() {
        this.testContext.listeners().forEach(testListener -> testListener.afterTestMethod(this.testContext));
    }

    @Override
    public void afterAll() {
        this.testContext.listeners().forEach(testListener -> testListener.afterTestClass(this.testContext));
        this.runtime.close();
    }

    @Override
    public void setTestInfo(Class<?> clazz, Object testInstance, Method method) {
        this.testContext.testInstance(testInstance);
        this.testContext.testMethod(method);
    }
}
