/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import modelengine.fit.http.Cookie;
import modelengine.fit.http.header.HeaderValue;
import modelengine.fitframework.util.StringUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 为 {@link HttpUtils} 提供单元测试。
 *
 * @author 杭潇
 * @since 2023-02-22
 */
@DisplayName("测试 HttpUtils 工具类")
public class HttpUtilsTest {
    @Test
    @DisplayName("格式化完整 Set-Cookie，返回正确格式化字符串")
    void givenFullCookie_thenIncludeAllAttributes() {
        Cookie cookie = Cookie.builder()
                .name("token")
                .value("abc")
                .path("/api")
                .domain("example.com")
                .maxAge(3600)
                .secure(true)
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        String result = HttpUtils.formatSetCookie(cookie);
        assertThat(result).contains("token=abc")
                .contains("Path=/api")
                .contains("Domain=example.com")
                .contains("Max-Age=3600")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }

    @Test
    @DisplayName("格式化空的 Set-Cookie，返回空字符串")
    void givenNullCookie_thenReturnEmptyString() {
        assertThat(HttpUtils.formatSetCookie(null)).isEmpty();
    }

    @Test
    @DisplayName("解析合法的 Set-Cookie 值，返回正确的 Cookie 对象")
    void givenValidSetCookieStringThenParseSuccessfully() {
        String rawCookie = "ID=ab12xy; Path=/; Domain=example.com; Max-Age=3600; Secure; SameSite=Strict";
        Cookie cookie = HttpUtils.parseSetCookie(rawCookie);

        assertThat(cookie.name()).isEqualTo("ID");
        assertThat(cookie.value()).isEqualTo("ab12xy");
        assertThat(cookie.path()).isEqualTo("/");
        assertThat(cookie.domain()).isEqualTo("example.com");
        assertThat(cookie.maxAge()).isEqualTo(3600);
        assertThat(cookie.secure()).isTrue();
        assertThat(cookie.httpOnly()).isFalse();
        assertThat(cookie.sameSite()).isEqualTo("Strict");
    }

    @Test
    @DisplayName("给定空的 Set-Cookie 值，返回空的 Cookie 对象")
    void givenEmptySetCookieThenReturnEmptyCookie() {
        Cookie cookie = HttpUtils.parseSetCookie("");
        assertThat(cookie.name()).isNull();
        assertThat(cookie.value()).isEmpty();
    }

    @Test
    @DisplayName("解析异常或不完整的 Cookie 值时，应忽略非法项并不抛异常")
    void givenMalformedCookiesThenHandleGracefully() {
        String rawCookie1 = "a=\"incomplete; b=2";
        List<Cookie> cookies1 = HttpUtils.parseCookies(rawCookie1);
        assertThat(cookies1).extracting(Cookie::name).contains("b");
        assertThat(cookies1).extracting(Cookie::name).doesNotContain("a");

        String rawCookie2 = "x=1;; ; y=2;";
        List<Cookie> cookies2 = HttpUtils.parseCookies(rawCookie2);
        assertThat(cookies2).hasSize(2);
        assertThat(cookies2.get(0).name()).isEqualTo("x");
        assertThat(cookies2.get(1).name()).isEqualTo("y");

        String rawCookie4 = ";;;";
        List<Cookie> cookies4 = HttpUtils.parseCookies(rawCookie4);
        assertThat(cookies4).isEmpty();
    }

    @Test
    @DisplayName("解析带 Expires 属性的 Set-Cookie，自动换算为 Max-Age")
    void givenExpiresAttributeThenConvertToMaxAge() {
        ZonedDateTime expiresTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        String expiresStr = expiresTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String rawCookie = "ID=xyz; Expires=" + expiresStr;

        Cookie cookie = HttpUtils.parseSetCookie(rawCookie);
        assertThat(cookie.maxAge()).isBetween(3500, 3700);
    }

    @Test
    @DisplayName("解析多个 Cookie 头部值，返回正确的 Cookie 列表")
    void givenMultipleCookiesThenReturnCookieList() {
        String rawCookie = "a=1; b=2; c=3";
        List<Cookie> cookies = HttpUtils.parseCookies(rawCookie);

        assertThat(cookies).hasSize(3);
        assertThat(cookies.get(0).name()).isEqualTo("a");
        assertThat(cookies.get(0).value()).isEqualTo("1");
        assertThat(cookies.get(1).name()).isEqualTo("b");
        assertThat(cookies.get(2).value()).isEqualTo("3");
    }

    @Test
    @DisplayName("解析非法格式的 Cookie 值，自动跳过无效项")
    void givenInvalidCookieStringThenSkipInvalidPairs() {
        String rawCookie = "a=1; invalid; b=2";
        List<Cookie> cookies = HttpUtils.parseCookies(rawCookie);

        assertThat(cookies).hasSize(2);
        assertThat(cookies.get(0).name()).isEqualTo("a");
        assertThat(cookies.get(1).name()).isEqualTo("b");
    }

    @Test
    @DisplayName("给定空的值，解析消息头的值返回为空")
    void givenEmptyValueThenReturnHeaderValueISEmpty() {
        String rawValue = "";
        HeaderValue headerValue = HttpUtils.parseHeaderValue(rawValue);
        assertThat(headerValue.value()).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    @DisplayName("给定不是消息头的数据，返回值为空")
    void givenParameterNotIsValueOfHeaderThenReturnIsEmpty() {
        String rawValue = "testKey=testValue;notContainsSeparator";
        HeaderValue headerValue = HttpUtils.parseHeaderValue(rawValue);
        assertThat(headerValue.value()).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    @DisplayName("调用 toUrl() 方法，给定一个错误的 Url 值，抛出异常")
    void givenIncorrectUrlWhenInvokeToUrlThenThrowException() {
        String incorrectUrl = "errorUrl";
        IllegalStateException illegalStateException =
                catchThrowableOfType(IllegalStateException.class, () -> HttpUtils.toUrl(incorrectUrl));
        assertThat(illegalStateException).hasMessage("The request URL is incorrect.");
    }

    @Test
    @DisplayName("调用 toUri() 方法，给定一个错误的 Url 值，抛出异常")
    void givenIncorrectUrlWhenInvokeToUriThenThrowException() throws MalformedURLException {
        URL url = new URL("jar", null, -1, "");
        IllegalStateException illegalStateException =
                catchThrowableOfType(IllegalStateException.class, () -> HttpUtils.toUri(url));
        assertThat(illegalStateException).hasMessage("The request URL is incorrect.");
    }
}
