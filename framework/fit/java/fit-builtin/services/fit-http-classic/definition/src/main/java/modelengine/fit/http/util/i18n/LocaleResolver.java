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
 * 地区解析器接口，用于从HTTP请求中解析用户的地区设置。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
public interface LocaleResolver {

    /**
     * 解析用户的地区设置。
     *
     * @param request 表示待解析的HTTP请求 {@link HttpClassicServerRequest}。
     * @return 表示解析出来的地区信息 {@link Locale}。
     */
    Locale resolveLocale(HttpClassicServerRequest request);

    /**
     * 设置地区到返回响应中。
     *
     * @param response 表示将要设置地区的HTTP响应 {@link HttpClassicServerResponse}。
     * @param locale 表示要设置的地区 {@link Locale}。
     */
    void setLocale(HttpClassicServerResponse response, Locale locale);
}
