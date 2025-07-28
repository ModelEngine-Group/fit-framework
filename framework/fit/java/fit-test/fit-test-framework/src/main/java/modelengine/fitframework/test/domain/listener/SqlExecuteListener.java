/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain.listener;

import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.test.annotation.Sql;
import modelengine.fitframework.test.domain.TestContext;
import modelengine.fitframework.test.domain.resolver.TestContextConfiguration;
import modelengine.fitframework.util.IoUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * 用于执行 SQL 脚本。
 *
 * @author 易文渊
 * @author 季聿阶
 * @since 2024-07-21
 */
public class SqlExecuteListener implements TestListener {
    private static final ClassLoader CLASS_LOADER = TestListener.class.getClassLoader();

    private Sql globalSql;

    @Override
    public Optional<TestContextConfiguration> config(Class<?> clazz) {
        this.globalSql = clazz.getAnnotation(Sql.class);
        if (this.globalSql == null) {
            return Optional.empty();
        }
        TestContextConfiguration configuration =
                TestContextConfiguration.custom().testClass(clazz).actions(List.of(this::executeAction)).build();
        return Optional.of(configuration);
    }

    private void executeAction(Plugin plugin) {
        if (this.globalSql == null) {
            return;
        }
        executeSql(plugin, this.globalSql, Sql.Position.BEFORE);
    }

    @Override
    public void beforeTestMethod(TestContext context) {
        execMethodSql(context, Sql.Position.BEFORE);
    }

    @Override
    public void afterTestMethod(TestContext context) {
        execMethodSql(context, Sql.Position.AFTER);
    }

    private static void execMethodSql(TestContext context, Sql.Position position) {
        Method method = context.testMethod();
        Sql sql = method.getAnnotation(Sql.class);
        executeSql(context.plugin(), sql, position);
    }

    @Override
    public void afterTestClass(TestContext context) {
        Class<?> testClass = context.testClass();
        Sql sql = testClass.getAnnotation(Sql.class);
        executeSql(context.plugin(), sql, Sql.Position.AFTER);
    }

    private static void executeSql(Plugin plugin, Sql sql, Sql.Position position) {
        if (sql == null) {
            return;
        }
        DataSource dataSource = plugin.container().beans().get(DataSource.class);
        try (Connection connection = dataSource.getConnection()) {
            String[] scripts = getScripts(sql, position);
            for (String script : scripts) {
                connection.createStatement().execute(IoUtils.content(CLASS_LOADER, script));
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to execute sql.", e);
        }
    }

    private static String[] getScripts(Sql sql, Sql.Position position) {
        if (position == Sql.Position.BEFORE) {
            return sql.before();
        } else {
            return sql.after();
        }
    }
}