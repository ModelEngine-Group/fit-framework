/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.Cookie;
import modelengine.fit.http.header.ConfigurableCookieCollection;
import modelengine.fit.http.header.CookieCollection;
import modelengine.fit.http.header.HeaderValue;
import modelengine.fit.http.header.support.DefaultHeaderValue;
import modelengine.fit.http.header.support.DefaultParameterCollection;
import modelengine.fit.http.util.HttpUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link CookieCollection} 的默认实现。
 *
 * @author 季聿阶
 * @since 2022-07-06
 */
public class DefaultCookieCollection extends DefaultHeaderValue implements ConfigurableCookieCollection {
    private final Map<String, List<Cookie>> store = new LinkedHashMap<>();

    /**
     * 初始化 {@link DefaultCookieCollection} 的新实例。
     */
    public DefaultCookieCollection() {
        super(StringUtils.EMPTY, new DefaultParameterCollection());
    }

    /**
     * 使用指定的消息头初始化 {@link DefaultCookieCollection} 的新实例。
     *
     * @param headerValue 表示消息头的 {@link HeaderValue}。
     * @throws IllegalArgumentException 当 {@code headerValue} 为 {@code null} 时。
     */
    public DefaultCookieCollection(HeaderValue headerValue) {
        super(notNull(headerValue, "The header value cannot be null.").value(), headerValue.parameters());
        HttpUtils.parseCookies(headerValue.value()).forEach(this::add);
    }

    @Override
    public Optional<Cookie> get(String name) {
        List<Cookie> cookies = store.get(name);
        if (cookies == null || cookies.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cookies.get(0));
    }

    @Override
    public List<Cookie> all(String name) {
        return store.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public List<Cookie> all() {
        return store.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public int size() {
        return store.values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }

    @Override
    public void add(Cookie cookie) {
        if (cookie == null || StringUtils.isBlank(cookie.name())) {
            return;
        }
        if (!HttpUtils.isValidCookiePair(cookie.name(), cookie.value())) {
            throw new IllegalArgumentException("Invalid cookie: name or value is not allowed");
        }
        store.computeIfAbsent(cookie.name(), k -> new ArrayList<>());
        List<Cookie> list = store.get(cookie.name());
        list.removeIf(c ->
                Objects.equals(c.path(), cookie.path()) &&
                        Objects.equals(c.domain(), cookie.domain())
        );
        list.add(cookie);
    }

    @Override
    public String toRequestHeaderValue() {
        return all().stream()
                .map(c -> c.name() + "=" + c.value())
                .collect(Collectors.joining("; "));
    }

    @Override
    public List<String> toResponseHeadersValues() {
        return all().stream()
                .map(HttpUtils::formatSetCookie)
                .collect(Collectors.toList());
    }
}
