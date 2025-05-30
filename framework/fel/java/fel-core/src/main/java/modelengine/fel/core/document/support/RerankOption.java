/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fitframework.pattern.builder.BuilderFactory;

/**
 * 表示 rerank 模型参数的实体。
 *
 * @author 马朝阳
 * @since 2024-09-23
 */
public interface RerankOption {
    /**
     * 获取调用模型的名字。
     *
     * @return 表示模型名字的 {@link String}。
     */
    String model();

    /**
     * 获取搜索查询。
     *
     * @return 表示搜索查询的 {@link String}。
     */
    String query();

    /**
     * 获取 Rerank 接口的 Uri。
     *
     * @return 表示 Rerank 接口 Uri 的 {@link String}。
     */
    String baseUri();

    /**
     * 获取返回的最相关的文档数量。
     *
     * @return 表示返回的最相关的文档数量的 {@link Integer}。
     */
    Integer topN();

    /**
     * {@link RerankOption} 的构建器。
     */
    interface Builder {
        /**
         * 设置调用模型的名字。
         *
         * @param model 表示模型名字的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder model(String model);

        /**
         * 设置搜索查询。
         *
         * @param query 表示搜索查询的 {@code String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder query(String query);

        /**
         * 设置 Rerank 接口的 Uri。
         *
         * @param baseUri 表示 Rerank 接口 Uri 的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder baseUri(String baseUri);

        /**
         * 设置返回的最相关的文档数量。
         *
         * @param topN 表示返回的最相关的文档数量的 {@link Integer}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder topN(Integer topN);

        /**
         * 构建对象。
         *
         * @return 表示构建出来的对象的 {@link RerankOption}。
         */

        RerankOption build();
    }

    /**
     * 获取 {@link RerankOption} 的构建器。
     *
     * @return 表示 {@link RerankOption} 的构建器的 {@link Builder}。
     */
    static Builder custom() {
        return custom(null);
    }

    /**
     * 获取 {@link RerankOption} 的构建器，同时将指定对象的值进行填充。
     *
     * @param value 表示指定对象的 {@link RerankOption}。
     * @return 表示 {@link RerankOption} 的构建器的 {@link Builder}
     */
    static Builder custom(RerankOption value) {
        return BuilderFactory.get(RerankOption.class, Builder.class).create(value);
    }
}
