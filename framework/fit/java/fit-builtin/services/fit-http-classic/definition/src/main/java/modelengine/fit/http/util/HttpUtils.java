/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.util;

import static modelengine.fit.http.protocol.CookieAttributeNames.DOMAIN;
import static modelengine.fit.http.protocol.CookieAttributeNames.HTTP_ONLY;
import static modelengine.fit.http.protocol.CookieAttributeNames.MAX_AGE;
import static modelengine.fit.http.protocol.CookieAttributeNames.PATH;
import static modelengine.fit.http.protocol.CookieAttributeNames.SAME_SITE;
import static modelengine.fit.http.protocol.CookieAttributeNames.SECURE;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.Cookie;
import modelengine.fit.http.header.HeaderValue;
import modelengine.fit.http.header.ParameterCollection;
import modelengine.fit.http.header.support.DefaultHeaderValue;
import modelengine.fit.http.header.support.DefaultParameterCollection;
import modelengine.fit.http.protocol.util.QueryUtils;
import modelengine.fitframework.model.MultiValueMap;
import modelengine.fitframework.resource.UrlUtils;
import modelengine.fitframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Http 协议相关的工具类。
 *
 * @author 季聿阶
 * @author 邬涨财
 * @since 2022-07-22
 */
public class HttpUtils {

    private static final char STRING_VALUE_SURROUNDED = '\"';

    /**
     * 将给定的 {@link Cookie} 对象格式化为符合 HTTP 协议的 {@code Set-Cookie} 头部字符串。
     * <p>生成结果遵循 RFC 6265 规范，如果 cookie 对象为空，则返回空字符串</p>
     *
     * @param cookie 表示待格式化的 {@link Cookie} 对象。
     * @return 表示生成的 {@code Set-Cookie} 头部字符串的 {@link String}。
     */
    public static String formatSetCookie(Cookie cookie) {
        if (cookie == null || StringUtils.isBlank(cookie.name())) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.name()).append("=").append(cookie.value() != null ? cookie.value() : "");
        if (cookie.path() != null && !cookie.path().isEmpty()) {
            sb.append("; ").append(PATH).append("=").append(cookie.path());
        }
        if (cookie.domain() != null && !cookie.domain().isEmpty()) {
            sb.append("; ").append(DOMAIN).append("=").append(cookie.domain());
        }
        if (cookie.maxAge() >= 0) {
            sb.append("; ").append(MAX_AGE).append("=").append(cookie.maxAge());
        }
        if (cookie.secure()) {
            sb.append("; ").append(SECURE);
        }
        if (cookie.httpOnly()) {
            sb.append("; ").append(HTTP_ONLY);
        }
        if (cookie.sameSite() != null && !cookie.sameSite().isEmpty()) {
            sb.append("; ").append(SAME_SITE).append("=").append(cookie.sameSite());
        }
        return sb.toString();
    }

    /**
     * 从消息头 Set-Cookie 的字符串值中解析 Cookie 的值以及属性。
     * <p>若包含 Expires 属性，则会自动换算为 Max-Age。</p>
     *
     * @param rawCookie 表示待解析的 Set-Cookie 字符串值的 {@link String}。
     * @return 表示解析后的 {@link Cookie}。
     */
    public static Cookie parseSetCookie(String rawCookie) {
        if (StringUtils.isBlank(rawCookie)) {
            return Cookie.builder().build();
        }

        Cookie.Builder builder = Cookie.builder();

        String[] parts = rawCookie.split(";");

        String[] nameValue = parts[0].split("=", 2);
        builder.name(nameValue[0].trim());
        builder.value(nameValue.length > 1 ? nameValue[1].trim() : "");

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }
            String[] kv = part.split("=", 2);
            String key = kv[0].trim().toLowerCase(Locale.ROOT);
            String val = kv.length > 1 ? kv[1].trim() : "";

            switch (key) {
                case "path":
                    builder.path(val);
                    break;
                case "domain":
                    builder.domain(val);
                    break;
                case "max-age":
                    try {
                        builder.maxAge(Integer.parseInt(val));
                    } catch (NumberFormatException ignore) {
                    }
                    break;
                case "expires":
                    int maxAge = convertExpiresToMaxAge(val);
                    builder.maxAge(maxAge);
                    break;
                case "secure":
                    builder.secure(true);
                    break;
                case "httponly":
                    builder.httpOnly(true);
                    break;
                case "samesite":
                    builder.sameSite(val);
                    break;
                default:
                    break;
            }
        }
        return builder.build();
    }

    /**
     * 从 Cookie 头部字符串解析多个 Cookie。
     * <p>示例：{@code "a=1; b=2; c=3"} → List[Cookie(a=1), Cookie(b=2), Cookie(c=3)]</p>
     *
     * @param rawCookie 表示原始 Cookie 头的字符串的 {@link String}（例如 "a=1; b=2; c=3"）。
     * @return 表示解析得到的 Cookie 列表的 {@link List}{@code <}{@link Cookie}{@code >}。
     */
    public static List<Cookie> parseCookies(String rawCookie) {
        if (rawCookie == null || rawCookie.isEmpty()) {
            return Collections.emptyList();
        }
        String[] pairs = rawCookie.split(";");
        List<Cookie> cookies = new ArrayList<>();

        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int eqIndex = trimmed.indexOf('=');
            if (eqIndex <= 0) {
                continue;
            }

            String name = trimmed.substring(0, eqIndex).trim();
            String value = trimmed.substring(eqIndex + 1).trim();

            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }

            cookies.add(Cookie.builder().name(name).value(value).build());
        }

        return cookies;
    }

    private static int convertExpiresToMaxAge(String expiresString) {
        if (StringUtils.isBlank(expiresString)) {
            return -1;
        }

        try {
            ZonedDateTime expires =
                    ZonedDateTime.parse(expiresString, DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US));
            long seconds = Duration.between(ZonedDateTime.now(ZoneOffset.UTC), expires).getSeconds();
            return (int) Math.max(seconds, 0);
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    /**
     * 从消息头的字符串值中解析消息头的值。
     *
     * @param rawValue 表示待解析的消息头的字符串值的 {@link String}。
     * @return 表示解析后的消息头值的 {@link HeaderValue}。
     */
    public static HeaderValue parseHeaderValue(String rawValue) {
        if (StringUtils.isBlank(rawValue)) {
            return HeaderValue.create(StringUtils.EMPTY);
        }
        List<String> splits =
                StringUtils.split(rawValue, DefaultHeaderValue.SEPARATOR, ArrayList::new, StringUtils::isNotBlank);
        if (!isValueOfHeader(splits.get(0))) {
            return HeaderValue.create(StringUtils.EMPTY, parseParameters(splits));
        }
        String value = StringUtils.trim(splits.get(0), STRING_VALUE_SURROUNDED);
        return HeaderValue.create(value, parseParameters(splits.subList(1, splits.size())));
    }

    private static boolean isValueOfHeader(String splitValue) {
        return isValueSurrounded(splitValue) || !splitValue.contains(DefaultParameterCollection.SEPARATOR);
    }

    private static boolean isValueSurrounded(String splitValue) {
        return splitValue.length() > 1 && splitValue.charAt(0) == STRING_VALUE_SURROUNDED
                && splitValue.charAt(splitValue.length() - 1) == STRING_VALUE_SURROUNDED;
    }

    private static ParameterCollection parseParameters(List<String> parameterStrings) {
        ParameterCollection parameterCollection = ParameterCollection.create();
        for (String parameterString : parameterStrings) {
            int index = parameterString.indexOf(DefaultParameterCollection.SEPARATOR);
            if (index < 0) {
                continue;
            }
            String key = parameterString.substring(0, index).trim();
            String value = parameterString.substring(index + 1).trim();
            parameterCollection.set(key, StringUtils.trim(value, STRING_VALUE_SURROUNDED));
        }
        return parameterCollection;
    }

    /**
     * 将 Http 表单参数的内容解析成为一个键和多值的映射。
     * <p>该映射的实现默认为 {@link LinkedHashMap}，即键是有序的。表单参数的样式为 {@code k1=v1&k2=v2}。</p>
     *
     * @param keyValues 表示待解析的查询参数或表单参数的 {@link String}。
     * @return 表示解析后的键与多值的映射的 {@link MultiValueMap}{@code <}{@link String}{@code ,
     * }{@link String}{@code >}。
     */
    public static MultiValueMap<String, String> parseForm(String keyValues) {
        return QueryUtils.parseQuery(keyValues, UrlUtils::decodeForm);
    }

    /**
     * 将指定的 URL 信息转为 {@link URL} 对象。
     *
     * @param url 表示指定的 URL 信息的 {@link String}。
     * @return 表示 URL 对象的 {@link URL}。
     * @throws IllegalStateException 当 URL 对象不合法时。
     */
    public static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("The request URL is incorrect.", e);
        }
    }

    /**
     * 将指定的 URL 信息转为 {@link URI} 对象。
     *
     * @param url 表示指定的 URL 信息的 {@link URL}。
     * @return 表示 URI 对象 {@link URI}。
     * @throws IllegalArgumentException 当 {@code url} 为 {@code null} 时。
     * @throws IllegalStateException 当 URL 对象不合法时。
     */
    public static URI toUri(URL url) {
        notNull(url, "The url cannot be null.");
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("The request URL is incorrect.", e);
        }
    }
}
