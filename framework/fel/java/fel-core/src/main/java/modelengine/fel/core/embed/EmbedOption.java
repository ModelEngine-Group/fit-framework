/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.embed;

import modelengine.fitframework.pattern.builder.BuilderFactory;

import java.util.Map;

/**
 * 表示嵌入模型参数的实体。
 *
 * @since 2024-04-24
 */
public interface EmbedOption {
    /**
     * 获取模型名称。
     *
     * @return 表示模型名称的 {@link String}。
     */
    String model();

    /**
     * 获取服务密钥。
     *
     * @return 表示服务密钥的 {@link String}。
     */
    String apiKey();

    /**
     * 获取扩展信息。
     *
     * @return 表示扩展信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    Map<String, Object> extensions();

    /**
     * 表示 {@link EmbedOption} 的构建器。
     */
    interface Builder {
        /**
         * 设置模型名称。
         *
         * @param model 表示模型名称的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder model(String model);

        /**
         * 设置服务密钥。
         *
         * @param apiKey 表示服务密钥的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder apiKey(String apiKey);

        /**
         * 设置扩展信息。
         *
         * @param extensions 表示扩展信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder extensions(Map<String, Object> extensions);

        /**
         * 构建 {@link EmbedOption} 实例。
         *
         * @return 返回构建成功的 {@link EmbedOption} 实例。
         */
        EmbedOption build();
    }

    /**
     * 获取 {@link Builder} 的实例。
     *
     * @return 表示构建器实例的 {@link Builder}。
     */
    static Builder custom() {
        return BuilderFactory.get(EmbedOption.class, EmbedOption.Builder.class).create(null);
    }
}