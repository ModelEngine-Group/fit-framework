/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.util;

import static modelengine.fitframework.inspection.Validation.notNull;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
