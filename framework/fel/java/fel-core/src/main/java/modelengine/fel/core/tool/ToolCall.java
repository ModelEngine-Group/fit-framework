/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.tool;

import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.pattern.builder.BuilderFactory;

/**
 * 表示工具调用请求的实体。
 *
 * @author 易文渊
 * @since 2024-04-16
 */
public interface ToolCall {
    /**
     * 获取工具调用的唯一标识。
     *
     * @return 表示工具调用唯一编号的 {@link String}。
     */
    String id();

    /**
     * 获取工具调用的索引标号。
     *
     * @return 表示索引标号的 {@link Integer}。
     */
    Integer index();

    /**
     * 获取调用的工具名称。
     *
     * @return 表示工具名称的 {@link String}。
     */
    String name();

    /**
     * 获取工具调用参数。
     *
     * @return 表示工具调用参数的 {@link String}。
     */
    String arguments();

    /**
     * 表示 {@link ToolCall} 的构建器。
     */
    interface Builder {
        /**
         * 设置工具调用的唯一编号。
         *
         * @param id 表示工具调用唯一编号的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder id(String id);

        /**
         * 设置工具调用的索引标号。
         *
         * @param index 表示索引标号的 {@code Integer}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder index(Integer index);

        /**
         * 设置调用的工具名称。
         *
         * @param name 表示工具名称的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder name(String name);

        /**
         * 设置工具调用参数。
         *
         * @param arguments 表示工具调用参数的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder arguments(String arguments);

        /**
         * 构建一个新的工具调用实体。
         *
         * @return 表示 {@link ToolCall} 实例。
         */
        ToolCall build();
    }

    /**
     * 创建 {@link Builder} 的实例。
     *
     * @return 表示创建成功的 {@link Builder}。
     */
    static Builder custom() {
        return BuilderFactory.get(ToolCall.class, Builder.class).create(null);
    }
}