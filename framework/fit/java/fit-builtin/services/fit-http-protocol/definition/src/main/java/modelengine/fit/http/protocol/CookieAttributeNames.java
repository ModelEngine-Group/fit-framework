/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol;

/**
 * RFC 6265 等规范中定义的 Set-Cookie 属性名称常量。
 * <p><a href="https://datatracker.ietf.org/doc/html/rfc6265">RFC 6265</a> 列出了很多属性名称的定义来源。</p>
 *
 * @author 徐吴昊
 * @since 2025-09-24
 */
public class CookieAttributeNames {
    /** @see <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2.1">RFC 6265</a> */
    public static final String EXPIRES = "Expires";

    /** @see <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2.2">RFC 6265</a> */
    public static final String MAX_AGE = "Max-Age";

    /** @see <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2.3">RFC 6265</a> */
    public static final String DOMAIN = "Domain";

    /** @see <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2.4">RFC 6265</a> */
    public static final String PATH = "Path";

    /** @see <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2.5">RFC 6265</a> */
    public static final String SECURE = "Secure";

    /** @see <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2.6">RFC 6265</a> */
    public static final String HTTP_ONLY = "HttpOnly";

    /** @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-rfc6265bis-20#section-4.1.2.7">RFC 6265bis</a> */
    public static final String SAME_SITE = "SameSite";
}
