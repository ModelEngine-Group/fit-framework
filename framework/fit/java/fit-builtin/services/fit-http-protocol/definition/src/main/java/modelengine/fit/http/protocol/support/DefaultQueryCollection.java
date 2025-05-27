/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol.support;

import modelengine.fit.http.protocol.QueryCollection;
import modelengine.fit.http.protocol.util.QueryUtils;
import modelengine.fitframework.model.MultiValueMap;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 表示 {@link QueryCollection} 的默认实现。
 *
 * @author 季聿阶
 * @since 2022-08-01
 */
public class DefaultQueryCollection implements QueryCollection {
    private final MultiValueMap<String, String> queries;

    /**
     * 直接实例化一个空的 {@link DefaultQueryCollection}。
     */
    public DefaultQueryCollection() {
        this(StringUtils.EMPTY);
    }

    /**
     * 通过整个查询参数来实例化 {@link DefaultQueryCollection}。
     *
     * @param queryString 表示整个查询参数的 {@link String}。
     */
    public DefaultQueryCollection(String queryString) {
        this.queries = QueryUtils.parseQuery(queryString);
    }

    @Override
    public List<String> keys() {
        return Collections.unmodifiableList(new ArrayList<>(this.queries.keySet()));
    }

    @Override
    public Optional<String> first(String key) {
        return Optional.ofNullable(this.queries.getFirst(key));
    }

    @Override
    public List<String> all(String key) {
        return Optional.ofNullable(this.queries.get(key)).orElseGet(Collections::emptyList);
    }

    @Override
    public int size() {
        return this.queries.size();
    }

    @Override
    public String queryString() {
        List<String> keyValues = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : this.queries.entrySet()) {
            List<String> values = entry.getValue();
            for (String value : values) {
                keyValues.add(entry.getKey() + "=" + value);
            }
        }
        return String.join("&", keyValues);
    }
}
