/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.support;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.http.server.handler.MockHttpClassicServerRequest;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 表示 {@link HeaderFetcher} 的单元测试。
 *
 * @author 白鹏坤
 * @since 2023-02-15
 */
@DisplayName("测试 HeaderFetcher 类")
class HeaderFetcherTest {
    private final HeaderFetcher headerFetcher = new HeaderFetcher(ParamValue.custom().name("name").build());

    @Test
    @DisplayName("判断来源数据的常用格式是否是数组")
    void shouldReturnIsArrayAble() {
        final boolean isArrayAble = this.headerFetcher.isArrayAble();
        assertThat(isArrayAble).isTrue();
    }

    @Test
    @DisplayName("从 Http 请求和响应中获取数据")
    void shouldReturnList() {
        final MockHttpClassicServerRequest serverRequest = new MockHttpClassicServerRequest();
        final Object obj = this.headerFetcher.get(serverRequest.getRequest(), null);
        assertThat(obj).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(0);
    }
}
