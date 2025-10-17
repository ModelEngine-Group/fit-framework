/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.util;

import static modelengine.fit.http.protocol.CookieAttributeNames.DOMAIN;
import static modelengine.fit.http.protocol.CookieAttributeNames.EXPIRES;
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
import java.util.regex.Pattern;

/**
 * Http 协议相关的工具类。
 *
 * @author 季聿阶
 * @author 邬涨财
 * @since 2022-07-22
 */
public class HttpUtils {
    private static final char STRING_VALUE_SURROUNDED = '\"';
    private static final String COOKIES_PARSE_SEPARATOR = ";";
    public static final String COOKIES_FORMAT_SEPARATOR = COOKIES_PARSE_SEPARATOR + " ";
    public static final String COOKIE_PAIR_SEPARATOR = "=";

    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[!#$%&'*+\\-.^_`|~0-9a-zA-Z]+$");

    private static final String PATH_KEY = PATH.toLowerCase(Locale.ROOT);
    private static final String DOMAIN_KEY = DOMAIN.toLowerCase(Locale.ROOT);
    private static final String MAX_AGE_KEY = MAX_AGE.toLowerCase(Locale.ROOT);
    private static final String EXPIRES_KEY = EXPIRES.toLowerCase(Locale.ROOT);
    private static final String SECURE_KEY = SECURE.toLowerCase(Locale.ROOT);
    private static final String HTTP_ONLY_KEY = HTTP_ONLY.toLowerCase(Locale.ROOT);
    private static final String SAME_SITE_KEY = SAME_SITE.toLowerCase(Locale.ROOT);

    /**
     * 将给定的 {@link Cookie} 对象格式化为符合 HTTP 协议的 {@code Set-Cookie} 头部字符串。
     * <p>生成结果遵循 RFC 6265 规范，如果 cookie 对象为空，则返回空字符串</p>
     *
     * @param cookie 表示待格式化的 {@link Cookie} 对象。
     * @return 表示生成的 {@code Set-Cookie} 头部字符串的 {@link String}。
     */
    public static String formatSetCookie(Cookie cookie) {
        if (cookie == null || StringUtils.isBlank(cookie.name())) {
            return StringUtils.EMPTY;
        }

        StringBuilder sb = new StringBuilder().append(cookie.name())
                .append(COOKIE_PAIR_SEPARATOR)
                .append(cookie.value() != null ? cookie.value() : StringUtils.EMPTY);

        if (StringUtils.isNotBlank(cookie.path())) {
            sb.append(COOKIES_FORMAT_SEPARATOR).append(PATH).append(COOKIE_PAIR_SEPARATOR).append(cookie.path());
        }
        if (StringUtils.isNotBlank(cookie.domain())) {
            sb.append(COOKIES_FORMAT_SEPARATOR).append(DOMAIN).append(COOKIE_PAIR_SEPARATOR).append(cookie.domain());
        }
        if (cookie.maxAge() >= 0) {
            sb.append(COOKIES_FORMAT_SEPARATOR).append(MAX_AGE).append(COOKIE_PAIR_SEPARATOR).append(cookie.maxAge());
        }
        if (cookie.secure()) {
            sb.append(COOKIES_FORMAT_SEPARATOR).append(SECURE);
        }
        if (cookie.httpOnly()) {
            sb.append(COOKIES_FORMAT_SEPARATOR).append(HTTP_ONLY);
        }
        if (StringUtils.isNotBlank(cookie.sameSite())) {
            sb.append(COOKIES_FORMAT_SEPARATOR)
                    .append(SAME_SITE)
                    .append(COOKIE_PAIR_SEPARATOR)
                    .append(cookie.sameSite());
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

        var parts = rawCookie.split(COOKIES_PARSE_SEPARATOR);
        var builder = parseCookieNameValue(parts[0]);
        if (builder == null) {
            return Cookie.builder().build();
        }

        parseCookieAttributes(parts, builder);
        return builder.build();
    }

    private static int safeParseInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int convertExpiresToMaxAge(String expiresString) {
        if (StringUtils.isBlank(expiresString)) {
            return -1;
        }
        try {
            ZonedDateTime expires =
                    ZonedDateTime.parse(expiresString, DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US));
            long seconds = Duration.between(ZonedDateTime.now(ZoneOffset.UTC), expires).getSeconds();
            if (seconds <= 0) {
                return 0;
            }
            if (seconds > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) seconds;
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    /**
     * 从 Cookie 头部字符串解析多个 Cookie。
     * <p>示例：{@code "a=1; b=2; c=3"} → List[Cookie(a=1), Cookie(b=2), Cookie(c=3)]</p>
     *
     * @param rawCookie 表示原始 Cookie 头的字符串的 {@link String}（例如 "a=1; b=2; c=3"）。
     * @return 表示解析得到的 Cookie 列表的 {@link List}{@code <}{@link Cookie}{@code >}。
     */
    public static List<Cookie> parseCookies(String rawCookie) {
        if (StringUtils.isBlank(rawCookie)) {
            return Collections.emptyList();
        }

        List<Cookie> cookies = new ArrayList<>();
        for (String part : rawCookie.split(COOKIES_PARSE_SEPARATOR)) {
            Cookie.Builder builder = parseCookieNameValue(part.trim());
            if (builder != null) {
                cookies.add(builder.build());
            }
        }
        return cookies;
    }

    private static Cookie.Builder parseCookieNameValue(String part) {
        String trimmed = part.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        int eqIndex = trimmed.indexOf(COOKIE_PAIR_SEPARATOR);
        if (eqIndex <= 0) {
            return null;
        }

        String name = trimmed.substring(0, eqIndex).trim();
        String value = trimmed.substring(eqIndex + 1).trim();
        if (isValueSurrounded(value)) {
            value = value.substring(1, value.length() - 1);
        }

        if (isInvalidCookiePair(name, value)) {
            return null;
        }
        return Cookie.builder().name(name).value(value);
    }

    private static void parseCookieAttributes(String[] parts, Cookie.Builder builder) {
        for (int i = 1; i < parts.length; i++) {
            var part = parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }

            var kv = part.split(COOKIE_PAIR_SEPARATOR, 2);
            var key = kv[0].trim().toLowerCase(Locale.ROOT);
            var val = kv.length > 1 ? kv[1].trim() : StringUtils.EMPTY;

            if (PATH_KEY.equals(key)) {
                builder.path(val);
            } else if (DOMAIN_KEY.equals(key)) {
                builder.domain(val);
            } else if (MAX_AGE_KEY.equals(key)) {
                builder.maxAge(safeParseInt(val));
            } else if (EXPIRES_KEY.equals(key)) {
                builder.maxAge(convertExpiresToMaxAge(val));
            } else if (SECURE_KEY.equals(key)) {
                builder.secure(true);
            } else if (HTTP_ONLY_KEY.equals(key)) {
                builder.httpOnly(true);
            } else if (SAME_SITE_KEY.equals(key)) {
                builder.sameSite(val);
            }
        }
    }

    /**
     * 判断给定的 Cookie 名称和值是否无效。
     *
     * @param name 表示 Cookie 的名称 {@link String}。
     * @param value 表示 Cookie 的值 {@link String}，允许为空但不允许为 {@code null}，可带双引号。
     * @return 如果 name 或 value 无效返回 {@code true}，否则返回 {@code false}。
     */
    public static boolean isInvalidCookiePair(String name, String value) {
        if (StringUtils.isEmpty(name) || !TOKEN_PATTERN.matcher(name).matches()) {
            return true;
        }
        if (value == null) {
            return true;
        }
        if (isValueSurrounded(value)) {
            value = value.substring(1, value.length() - 1);
        }
        return !value.isEmpty() && !TOKEN_PATTERN.matcher(value).matches();
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
