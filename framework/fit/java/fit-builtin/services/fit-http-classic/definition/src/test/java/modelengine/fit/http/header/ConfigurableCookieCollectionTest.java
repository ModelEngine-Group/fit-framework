/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.header;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import modelengine.fit.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 表示 {@link ConfigurableCookieCollection} 的单元测试。
 *
 * @author 白鹏坤
 * @since 2023-02-15
 */
@DisplayName("测试 ConfigurableCookieCollection 类")
class ConfigurableCookieCollectionTest {
    @Test
    @DisplayName("返回所有的 Cookie")
    void shouldReturnAllCookie() {
        final Cookie cookie = Cookie.builder()
                .name("idea")
                .value("00ae-u98i")
                .domain("localhost")
                .maxAge(10)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .build();
        ConfigurableCookieCollection cookieCollection = ConfigurableCookieCollection.create();
        cookieCollection.add(cookie);
        final List<Cookie> cookies = cookieCollection.all();
        assertThat(cookies).hasSize(1);
    }

    @Test
    @DisplayName("添加非法 Cookie 应抛异常")
    void shouldThrowExceptionForInvalidCookie() {
        ConfigurableCookieCollection collection = ConfigurableCookieCollection.create();

        Cookie invalidNameCookie = Cookie.builder().name("inva;lid").value("123").build();
        assertThatThrownBy(() -> collection.add(invalidNameCookie)).isInstanceOf(IllegalArgumentException.class);

        Cookie invalidValueCookie = Cookie.builder().name("validName").value("v@lue;").build();
        assertThatThrownBy(() -> collection.add(invalidValueCookie)).isInstanceOf(IllegalArgumentException.class);

        Cookie nullValueCookie = Cookie.builder().name("someName").value(null).build();
        assertThatThrownBy(() -> collection.add(nullValueCookie)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("允许空字符串 value")
    void shouldHandleEmptyAndNullValue() {
        ConfigurableCookieCollection collection = ConfigurableCookieCollection.create();

        // 空字符串 value 是允许的
        Cookie emptyValueCookie = Cookie.builder().name("token").value("").build();
        collection.add(emptyValueCookie);
        assertThat(collection.get("token")).isPresent().get().extracting(Cookie::value).isEqualTo("");
    }

    @Test
    @DisplayName("同名 Cookie 不同路径可共存")
    void shouldAllowMultipleCookiesWithDifferentPath() {
        ConfigurableCookieCollection collection = ConfigurableCookieCollection.create();
        collection.add(Cookie.builder().name("user").value("A").path("/a").build());
        collection.add(Cookie.builder().name("user").value("B").path("/b").build());

        List<Cookie> sameNameCookies = collection.all("user");
        assertThat(sameNameCookies).hasSize(2);
    }

    @Test
    @DisplayName("同名同 path/domain 的 Cookie 应被替换")
    void shouldReplaceCookieWithSamePathAndDomain() {
        ConfigurableCookieCollection collection = ConfigurableCookieCollection.create();

        Cookie c1 = Cookie.builder().name("id").value("1").path("/").domain("a.com").build();
        Cookie c2 = Cookie.builder().name("id").value("2").path("/").domain("a.com").build();

        collection.add(c1);
        collection.add(c2);

        List<Cookie> cookies = collection.all("id");
        assertThat(cookies).hasSize(1);
        assertThat(cookies.get(0).value()).isEqualTo("2");
    }

    @Test
    @DisplayName("toRequestHeader 生成单行请求头")
    void shouldGenerateRequestHeader() {
        ConfigurableCookieCollection collection = ConfigurableCookieCollection.create();
        collection.add(Cookie.builder().name("a").value("1").build());
        collection.add(Cookie.builder().name("b").value("2").build());

        String header = collection.toRequestHeaderValue();
        assertThat(header).isEqualTo("a=1; b=2");
    }

    @Test
    @DisplayName("toResponseHeaders 生成多个 Set-Cookie 响应头")
    void shouldGenerateMultipleResponseHeaders() {
        ConfigurableCookieCollection collection = ConfigurableCookieCollection.create();
        collection.add(Cookie.builder().name("token").value("xyz").secure(true).httpOnly(true).build());
        collection.add(Cookie.builder().name("lang").value("zh-CN").sameSite("Lax").build());

        List<String> headers = collection.toResponseHeadersValues();

        assertThat(headers).hasSize(2);
        assertThat(headers.get(0)).contains("token=xyz");
        assertThat(headers.get(1)).contains("lang=zh-CN");
        assertThat(headers.get(1)).contains("SameSite=Lax");
    }
}
