/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.support;

import static modelengine.fit.http.util.HttpUtils.COOKIES_FORMAT_SEPARATOR;
import static modelengine.fit.http.util.HttpUtils.COOKIE_PAIR_SEPARATOR;

import modelengine.fit.http.Cookie;
import modelengine.fit.http.header.ConfigurableCookieCollection;
import modelengine.fit.http.header.CookieCollection;
import modelengine.fit.http.util.HttpUtils;
import modelengine.fitframework.util.CollectionUtils;
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
public class DefaultCookieCollection implements ConfigurableCookieCollection {
    private final Map<String, List<Cookie>> store = new LinkedHashMap<>();

    @Override
    public Optional<Cookie> get(String name) {
        List<Cookie> cookies = this.store.get(name);
        if (CollectionUtils.isEmpty(cookies)) {
            return Optional.empty();
        }
        return Optional.of(cookies.get(0));
    }

    @Override
    public List<Cookie> all(String name) {
        return this.store.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public List<Cookie> all() {
        return this.store.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public int size() {
        return this.store.values().stream().mapToInt(List::size).sum();
    }

    @Override
    public void add(Cookie cookie) {
        if (cookie == null || StringUtils.isBlank(cookie.name())) {
            return;
        }
        if (HttpUtils.isInvalidCookiePair(cookie.name(), cookie.value())) {
            throw new IllegalArgumentException("Invalid cookie: name or value is not allowed.");
        }
        this.store.computeIfAbsent(cookie.name(), k -> new ArrayList<>());
        List<Cookie> list = this.store.get(cookie.name());
        list.removeIf(c -> Objects.equals(c.path(), cookie.path()) && Objects.equals(c.domain(), cookie.domain()));
        list.add(cookie);
    }

    @Override
    public String toRequestHeaderValue() {
        return all().stream()
                .map(c -> c.name() + COOKIE_PAIR_SEPARATOR + c.value())
                .collect(Collectors.joining(COOKIES_FORMAT_SEPARATOR));
    }

    @Override
    public List<String> toResponseHeadersValues() {
        return all().stream().map(HttpUtils::formatSetCookie).collect(Collectors.toList());
    }
}
