/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.util.i18n;

import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;

import java.util.Locale;

/**
 * 地区解析器接口，用于从 HTTP 请求中解析用户的地区设置。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
public interface LocaleResolver {
    /**
     * 表示待设置 cookie 的名称。
     */
    public static final String DEFAULT_COOKIE_NAME = "locale";

    /**
     * 表示待设置 cookie 的自动过期时间。
     */
    public static final int DEFAULT_COOKIE_MAX_AGE = 60 * 60 * 24 * 365;

    /**
     * 表示待设置 Cookie 的可见域。
     */
    public static final String DEFAULT_COOKIE_DOMAIN = "/";

    /**
     * 表示待设置 Cookie 的可见 URL 路径。
     */
    public static final String DEFAULT_COOKIE_PATH = "/";

    /**
     * 解析用户的地区设置。
     *
     * @param request 表示待解析 HTTP 请求的 {@link HttpClassicServerRequest}。
     * @return 表示解析出来地区信息的 {@link Locale}。
     */
    Locale resolveLocale(HttpClassicServerRequest request);

    /**
     * 设置地区到返回响应中。
     *
     * @param response 表示待设置地区的 HTTP 响应的 {@link HttpClassicServerResponse}。
     * @param locale 表示待设置地区的 {@link Locale}。
     */
    void setLocale(HttpClassicServerResponse response, Locale locale);
}