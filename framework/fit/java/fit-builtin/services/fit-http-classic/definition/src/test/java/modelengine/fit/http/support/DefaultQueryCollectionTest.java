/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.support;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.http.protocol.QueryCollection;
import modelengine.fit.http.header.ConfigurableCookieCollection;
import modelengine.fit.http.protocol.support.DefaultQueryCollection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * 为 {@link DefaultQueryCollection} 提供单元测试。
 *
 * @author 杭潇
 * @since 2023-02-20
 */
@DisplayName("测试 DefaultQueryCollection 类")
public class DefaultQueryCollectionTest {
    private QueryCollection queryCollection;

    @BeforeEach
    void setup() {
        String queryString = "12=34&2=3&5=4&2=13";
        this.queryCollection = QueryCollection.create(queryString);
    }

    @Test
    @DisplayName("获取的集合的大小值与给定值相等")
    void theSizeOfCollectionIsEqualsToTheGivenData() {
        int size = this.queryCollection.size();
        assertThat(size).isEqualTo(3);
    }

    @Test
    @DisplayName("获取集合的 String 值与给定值相等")
    void theQueryStringIsEqualsToTheGivenValue() {
        String actualQueryCollection = this.queryCollection.queryString();
        assertThat(actualQueryCollection).isEqualTo("12=34&2=3&2=13&5=4");
    }

    @Test
    @DisplayName("获取指定查询参数的第一个值与预期值相等")
    void theFirstValueOfKeyIsEqualsToTheGivenValue() {
        Optional<String> first = this.queryCollection.first("2");
        assertThat(first).isPresent().get().isEqualTo("3");
    }

    @Test
    @DisplayName("获取所有的查询参数的列表与给定值相等")
    void theKeysShouldBeEqualsToTheGivenValue() {
        List<String> keys = this.queryCollection.keys();
        assertThat(keys).hasSize(3);
    }

    @Test
    @DisplayName("获取的 Cookie 集合的大小与给定值相等")
    void theCookieSizeShouldBeEqualsToTheGivenValue() {
        ConfigurableCookieCollection configurableCookieCollection = ConfigurableCookieCollection.create();
        int size = configurableCookieCollection.size();
        assertThat(size).isEqualTo(0);
    }
}
