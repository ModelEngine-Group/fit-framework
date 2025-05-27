/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol.support;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fit.http.protocol.HttpVersion;
import modelengine.fit.http.protocol.QueryCollection;
import modelengine.fit.http.protocol.RequestLine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link DefaultRequestLine} 的单元测试。
 *
 * @author 杭潇
 * @since 2023-02-15
 */
@DisplayName("测试 DefaultRequestLine 类")
public class DefaultRequestLineTest {
    private final HttpVersion httpVersion = HttpVersion.HTTP_1_0;
    private final HttpRequestMethod method = HttpRequestMethod.CONNECT;
    private final String requestUri = "testRequestUri";
    private final QueryCollection queries = QueryCollection.create();
    private final RequestLine defaultRequestLine = RequestLine.create(httpVersion, method, requestUri, queries);

    @Test
    @DisplayName("获取的方法与给定的方法值相等")
    void theMethodShouldBeEqualsToTheGivenMethod() {
        HttpRequestMethod actualMethod = this.defaultRequestLine.method();
        assertThat(actualMethod).isEqualTo(this.method);
    }

    @Test
    @DisplayName("获取的请求 Uri 值与给定值相等")
    void theRequestUriShouldBeEqualsToTheGivenUri() {
        String actualUri = this.defaultRequestLine.requestUri();
        assertThat(actualUri).isEqualTo(this.requestUri);
    }

    @Test
    @DisplayName("获取的 Http 版本值与给定的值相等")
    void theHttpVersionShouldBeEqualsToTheGivenVersion() {
        HttpVersion actualHttpVersion = this.defaultRequestLine.httpVersion();
        assertThat(actualHttpVersion).isEqualTo(this.httpVersion);
    }

    @Test
    @DisplayName("获取的查询集合与给定的查询集合相等")
    void theQueriesShouldBeEqualsToTheGivenQueries() {
        QueryCollection actualQueries = this.defaultRequestLine.queries();
        assertThat(actualQueries).isEqualTo(this.queries);
    }
}
