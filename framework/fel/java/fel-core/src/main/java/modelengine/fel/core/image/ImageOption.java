/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.image;

import modelengine.fitframework.pattern.builder.BuilderFactory;

/**
 * 表示图像生成模型参数的实体。
 *
 * @author 何嘉斌
 * @since 2024-12-17
 */
public interface ImageOption {
    /**
     * 获取调用模型的名字。
     *
     * @return 表示模型名字的 {@link String}。
     */
    String model();

    /**
     * 大模型服务端地址。
     *
     * @return 表示大模型服务端地址的 {@link String}。
     */
    String baseUrl();

    /**
     * 获取图片规格。
     *
     * @return 表示图片规格的 {@link String}。
     */
    String size();

    /**
     * 获取服务密钥。
     *
     * @return 表示服务密钥的 {@link String}。
     */
    String apiKey();

    /**
     * 表示 {@link ImageOption} 的构建器。
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
         * 设置图片规格。
         *
         * @param size 表示图片规格的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder size(String size);

        /**
         * 设置模型服务端地址。
         *
         * @param baseUrl 表示大模型服务端地址的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder baseUrl(String baseUrl);

        /**
         * 构建 {@link ImageOption} 实例。
         *
         * @return 返回构建成功的 {@link ImageOption} 实例。
         */
        ImageOption build();
    }

    /**
     * 获取 {@link Builder} 的实例。
     *
     * @return 表示构建器实例的 {@link Builder}。
     */
    static Builder custom() {
        return BuilderFactory.get(ImageOption.class, Builder.class).create(null);
    }
}