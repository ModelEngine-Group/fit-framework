/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.annotation;

import modelengine.fit.http.client.proxy.auth.AuthProvider;
import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.server.handler.Source;
import modelengine.fitframework.util.StringUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示HTTP请求的鉴权配置注解。
 * 支持Bearer Token、Basic Auth、API Key等多种鉴权方式。
 * 可以应用于接口、方法或参数级别，支持静态配置和动态Provider。
 *
 * <p>使用示例：
 * <pre>
 * // 静态Bearer Token
 * {@code @RequestAuth(type = AuthType.BEARER, value = "token_value")}
 *
 * // API Key in Header
 * {@code @RequestAuth(type = AuthType.API_KEY, name = "X-API-Key", value = "key_value")}
 *
 * // API Key in Query
 * {@code @RequestAuth(type = AuthType.API_KEY, name = "api_key", value = "key_value", location = Source.QUERY)}
 *
 * // Basic Auth
 * {@code @RequestAuth(type = AuthType.BASIC, username = "user", password = "pass")}
 *
 * // Dynamic Provider
 * {@code @RequestAuth(type = AuthType.BEARER, provider = TokenProvider.class)}
 *
 * // Parameter-driven
 * public User getUser({@code @RequestAuth(type = AuthType.BEARER)} String token);
 * </pre>
 *
 * @author 季聿阶
 * @since 2025-01-01
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Repeatable(RequestAuths.class)
public @interface RequestAuth {

    /**
     * 鉴权类型。
     *
     * @return 表示鉴权类型的 {@link AuthType}。
     */
    AuthType type();

    /**
     * 鉴权值，用于静态配置。
     * 对于Bearer Token，这是token值；
     * 对于API Key，这是key的值；
     * 对于Basic Auth，这个字段不使用。
     *
     * @return 表示鉴权值的 {@link String}。
     */
    String value() default StringUtils.EMPTY;

    /**
     * 鉴权参数的名称。
     * 对于API Key，这是key的名称（如"X-API-Key"）；
     * 对于Bearer Token，这个字段不使用（默认使用Authorization头）；
     * 对于Basic Auth，这个字段不使用。
     *
     * @return 表示鉴权参数名称的 {@link String}。
     */
    String name() default StringUtils.EMPTY;

    /**
     * 鉴权参数的位置。
     * 仅对API Key有效，可以是HEADER、QUERY或COOKIE。
     * 默认为HEADER。
     *
     * @return 表示鉴权参数位置的 {@link Source}。
     */
    Source location() default Source.HEADER;

    /**
     * Basic Auth的用户名，仅当type=BASIC时有效。
     *
     * @return 表示用户名的 {@link String}。
     */
    String username() default StringUtils.EMPTY;

    /**
     * Basic Auth的密码，仅当type=BASIC时有效。
     *
     * @return 表示密码的 {@link String}。
     */
    String password() default StringUtils.EMPTY;

    /**
     * 动态鉴权提供器类。
     * 当指定时，将忽略静态配置（value、username、password等），
     * 通过Provider动态获取鉴权信息。
     *
     * @return 表示鉴权提供器类的 {@link Class}。
     */
    Class<? extends AuthProvider> provider() default AuthProvider.class;
}