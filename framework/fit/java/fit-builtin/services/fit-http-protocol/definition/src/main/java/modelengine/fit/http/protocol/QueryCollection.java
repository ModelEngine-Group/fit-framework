/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol;

import modelengine.fit.http.protocol.support.DefaultQueryCollection;

import java.util.List;
import java.util.Optional;

/**
 * 表示查询参数的集合。
 *
 * @author 季聿阶
 * @since 2022-08-01
 */
public interface QueryCollection {
    /**
     * 获取所有的查询参数的列表。
     *
     * @return 表示所有的查询参数列表的 {@link List}{@code <}{@link String}{@code >}。
     */
    List<String> keys();

    /**
     * 获取指定查询参数的第一个值。
     *
     * @param key 表示指定查询参数的 {@link String}。
     * <p>查询参数<b>大小写敏感</b>。</p>
     * @return 表示指定查询参数的第一个值的 {@link Optional}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code key} 为 {@code null} 或空白字符串时。
     */
    Optional<String> first(String key);

    /**
     * 获取指定查询参数的所有值的列表。
     *
     * @param key 表示指定查询参数的 {@link String}。
     * <p>查询参数<b>大小写敏感</b>。</p>
     * @return 表示指定查询参数的所有值的列表的 {@link List}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code key} 为 {@code null} 或空白字符串时。
     */
    List<String> all(String key);

    /**
     * 获取所有的查询参数的数量。
     *
     * @return 表示所有的查询参数数量的 {@code int}。
     */
    int size();

    /**
     * 获取当前查询参数的字符串表示形式。
     *
     * @return 表示当前查询参数的字符串表示形式的 {@link String}。
     */
    String queryString();

    /**
     * 创建一个新的查询参数集合。
     *
     * @return 表示创建出来的查询参数集合的 {@link QueryCollection}。
     */
    static QueryCollection create() {
        return new DefaultQueryCollection();
    }

    /**
     * 根据指定的查询参数，创建一个新的查询参数集合。
     *
     * @param queryString 表示指定的查询参数的 {@link String}。
     * @return 表示创建出来的查询参数集合的 {@link QueryCollection}。
     */
    static QueryCollection create(String queryString) {
        return new DefaultQueryCollection(queryString);
    }
}
