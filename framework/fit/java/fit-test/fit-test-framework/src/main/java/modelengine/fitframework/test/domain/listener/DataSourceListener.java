/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain.listener;

import modelengine.fitframework.test.annotation.EnableDataSource;
import modelengine.fitframework.test.domain.resolver.TestContextConfiguration;
import modelengine.fitframework.test.domain.util.AnnotationUtils;
import modelengine.fitframework.util.MapBuilder;

import org.h2.jdbcx.JdbcConnectionPool;

import java.util.Optional;
import java.util.function.Supplier;

import javax.sql.DataSource;

/**
 * 用于注入 dataSource 的监听器。
 *
 * @author 易文渊
 * @author 季聿阶
 * @since 2024-07-21
 */
public class DataSourceListener implements TestListener {
    @Override
    public Optional<TestContextConfiguration> config(Class<?> clazz) {
        Optional<EnableDataSource> annotationOption = AnnotationUtils.getAnnotation(clazz, EnableDataSource.class);
        if (annotationOption.isEmpty()) {
            return Optional.empty();
        }
        TestContextConfiguration customConfig = TestContextConfiguration.custom()
                .testClass(clazz)
                .includeClasses(MapBuilder.<Class<?>, Supplier<Object>>get().put(DataSource.class, () -> {
                    EnableDataSource enableDataSource = annotationOption.get();
                    return JdbcConnectionPool.create(enableDataSource.model().getUrl(), "sa", "sa");
                }).build())
                .build();
        return Optional.of(customConfig);
    }
}