/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.support;

import static modelengine.fitframework.inspection.Validation.notBlank;

import modelengine.fit.http.Cookie;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fit.http.server.handler.RequestMappingException;
import modelengine.fit.http.server.handler.SourceFetcher;
import modelengine.fit.http.server.handler.exception.RequestParamFetchException;
import modelengine.fitframework.util.StringUtils;

/**
 * 表示从 Cookie 中获取值的 {@link SourceFetcher}。
 *
 * @author 季聿阶
 * @since 2022-08-28
 */
public class CookieFetcher extends AbstractSourceFetcher {
    private final String cookieName;

    /**
     * 通过 Cookie 的名字来实例化 {@link CookieFetcher}。
     *
     * @param cookieName 表示 Cookie 名字的 {@link String}。
     * @throws IllegalArgumentException 当 {@code cookieName} 为 {@code null} 或空白字符串时。
     */
    public CookieFetcher(String cookieName) {
        super(false, null);
        this.cookieName = notBlank(cookieName, () -> new RequestParamFetchException(
                "RequestCookie 必须指定参数名。" +
                "可以通过以下方式之一指定：" +
                "1) 使用 name 属性：@RequestCookie(name = \"sessionId\")，" +
                "2) 使用 value 属性：@RequestCookie(value = \"sessionId\")，" +
                "3) 编译时添加 -parameters 参数，然后使用参数名：@RequestCookie String sessionId（sessionId 将作为参数名）"));
    }

    /**
     * 通过 Cookie 的参数元数据来实例化 {@link CookieFetcher}。
     *
     * @param paramValue 表示参数元数据的 {@link ParamValue}。
     * @throws IllegalArgumentException 当 {@code cookieName} 为 {@code null} 或空白字符串时。
     */
    public CookieFetcher(ParamValue paramValue) {
        super(paramValue.required(), paramValue.defaultValue());
        this.cookieName = notBlank(paramValue.name(), () -> new RequestParamFetchException(
                "RequestCookie 必须指定参数名。" +
                "可以通过以下方式之一指定：" +
                "1) 使用 name 属性：@RequestCookie(name = \"sessionId\")，" +
                "2) 使用 value 属性：@RequestCookie(value = \"sessionId\")，" +
                "3) 编译时添加 -parameters 参数，然后使用参数名：@RequestCookie String sessionId（sessionId 将作为参数名）"));
    }

    @Override
    public Object get(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        try {
            return this.resolveValue(request.cookies().get(this.cookieName).map(Cookie::value).orElse(null));
        } catch (RequestMappingException e) {
            throw new RequestMappingException(StringUtils.format("Invalid cookie param. [cookieName={0}]",
                    this.cookieName), e);
        }
    }
}
