/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain.resolver;

import modelengine.fitframework.annotation.ScanPackages;
import modelengine.fitframework.test.annotation.FitTestWithJunit;
import modelengine.fitframework.test.annotation.Mock;
import modelengine.fitframework.test.annotation.Spy;
import modelengine.fitframework.test.domain.util.AnnotationUtils;
import modelengine.fitframework.util.ReflectionUtils;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 默认的单测类解析器。
 *
 * @author 邬涨财
 * @since 2023-01-17
 */
public class DefaultTestClassResolver implements TestClassResolver {
    private static final Set<String> DEFAULT_SCAN_PACKAGES = new HashSet<>(Arrays.asList("modelengine.fit.value",
            "modelengine.fit.serialization",
            "modelengine.fitframework.validation"));

    @Override
    public TestContextConfiguration resolve(Class<?> clazz) {
        Class<?> testConfigurationClass = this.getTestConfigurationClass(clazz);
        Class<?>[] includeClasses = this.resolveIncludeClasses(testConfigurationClass);
        return TestContextConfiguration.custom()
                .testClass(clazz)
                .includeClasses(Stream.of(includeClasses)
                        .collect(Collectors.toMap(Function.identity(), key -> () -> null)))
                .excludeClasses(this.resolveExcludeClasses(clazz))
                .scannedPackages(this.scanBeans(includeClasses))
                .mockedBeanFields(this.scanMockBeansFieldSet(clazz))
                .toSpyClasses(this.scanSpyBeansFieldSet(clazz))
                .build();
    }

    private Class<?>[] resolveIncludeClasses(AnnotatedElement element) {
        return AnnotationUtils.getAnnotation(element, FitTestWithJunit.class)
                .map(FitTestWithJunit::includeClasses)
                .orElseGet(() -> new Class<?>[0]);
    }

    private Class<?>[] resolveExcludeClasses(AnnotatedElement element) {
        return AnnotationUtils.getAnnotation(element, FitTestWithJunit.class)
                .map(FitTestWithJunit::excludeClasses)
                .orElseGet(() -> new Class<?>[0]);
    }

    private Class<?> getTestConfigurationClass(Class<?> clazz) {
        Class<?> superclass = clazz;
        while (superclass != null) {
            if (this.isJunit5TestClass(superclass)) {
                return superclass;
            }
            superclass = superclass.getSuperclass();
        }
        return clazz;
    }

    private boolean isJunit5TestClass(Class<?> clazz) {
        return AnnotationUtils.getAnnotation(clazz, ExtendWith.class).isPresent();
    }

    private Set<String> scanBeans(Class<?>[] classes) {
        Set<String> basePackages = Arrays.stream(classes)
                .flatMap(resolvedClass -> this.getBasePackages(resolvedClass).stream())
                .collect(Collectors.toSet());
        basePackages.addAll(DEFAULT_SCAN_PACKAGES);
        return basePackages;
    }

    private Set<String> getBasePackages(Class<?> clazz) {
        Optional<ScanPackages> opScanPackagesAnnotation = AnnotationUtils.getAnnotation(clazz, ScanPackages.class);
        if (opScanPackagesAnnotation.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> basePackages = new HashSet<>(Arrays.asList(opScanPackagesAnnotation.get().value()));
        if (basePackages.isEmpty()) {
            basePackages.add(clazz.getPackage().getName());
        }
        return basePackages;
    }

    private Set<Field> scanMockBeansFieldSet(Class<?> clazz) {
        return Arrays.stream(ReflectionUtils.getDeclaredFields(clazz))
                .filter(field -> field.isAnnotationPresent(Mock.class))
                .collect(Collectors.toSet());
    }

    private Set<Class<?>> scanSpyBeansFieldSet(Class<?> clazz) {
        return Arrays.stream(ReflectionUtils.getDeclaredFields(clazz))
                .filter(field -> field.isAnnotationPresent(Spy.class))
                .map(Field::getType)
                .collect(Collectors.toSet());
    }
}
