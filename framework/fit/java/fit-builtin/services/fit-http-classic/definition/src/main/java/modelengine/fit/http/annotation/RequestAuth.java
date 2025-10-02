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
 * 表示 HTTP 请求的鉴权配置注解。
 * <p>支持 Bearer Token、Basic Auth、API Key 等多种鉴权方式。
 * 可以应用于接口、方法或参数级别，支持静态配置和动态 Provider。</p>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 静态 Bearer Token
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
 * @since 2025-09-30
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
     * <ul>
     * <li>对于 Bearer Token，这是 token 值</li>
     * <li>对于 API Key，这是 key 的值</li>
     * <li>对于 Basic Auth，这个字段不使用</li>
     * </ul>
     *
     * @return 表示鉴权值的 {@link String}。
     */
    String value() default StringUtils.EMPTY;

    /**
     * 鉴权参数的名称，语义根据鉴权类型不同而不同。
     *
     * <p><b>对于 API_KEY 类型：</b></p>
     * <ul>
     * <li>指定 HTTP Header/Query/Cookie 的名称（如 "X-API-Key"）</li>
     * <li>此属性在静态配置和参数级别配置中都有效</li>
     * <li>示例：{@code @RequestAuth(type = API_KEY, name = "X-API-Key", value = "secret")}</li>
     * </ul>
     *
     * <p><b>对于 BASIC 类型（仅参数级别）：</b></p>
     * <ul>
     * <li>指定要更新的 Authorization 字段名（"username" 或 "password"）</li>
     * <li>允许在参数级别分别更新用户名和密码</li>
     * <li>如果不指定，默认更新 username 字段</li>
     * <li>示例：
     * <pre>{@code
     * String login(
     *     @RequestAuth(type = BASIC, name = "username") String user,
     *     @RequestAuth(type = BASIC, name = "password") String pwd
     * );
     * }</pre></li>
     * </ul>
     *
     * <p><b>对于 BEARER 类型：</b></p>
     * <ul>
     * <li>此属性不使用，会被忽略（默认使用 Authorization 头）</li>
     * </ul>
     *
     * <p><b>name 属性的语义总结：</b></p>
     * <table border="1">
     *   <caption>name 属性在不同鉴权类型下的含义</caption>
     *   <tr>
     *     <th>鉴权类型</th>
     *     <th>应用位置</th>
     *     <th>name 属性的含义</th>
     *     <th>示例值</th>
     *   </tr>
     *   <tr>
     *     <td>API_KEY</td>
     *     <td>静态配置/参数级别</td>
     *     <td>HTTP Header/Query/Cookie 的名称</td>
     *     <td>"X-API-Key", "api_key"</td>
     *   </tr>
     *   <tr>
     *     <td>BASIC</td>
     *     <td>仅参数级别</td>
     *     <td>Authorization 对象的字段名</td>
     *     <td>"username", "password"</td>
     *   </tr>
     *   <tr>
     *     <td>BEARER</td>
     *     <td>任意位置</td>
     *     <td>无效（被忽略）</td>
     *     <td>-</td>
     *   </tr>
     * </table>
     *
     * @return 表示鉴权参数名称的 {@link String}。
     * @see modelengine.fit.http.client.proxy.scanner.resolver.AuthFieldMapper
     */
    String name() default StringUtils.EMPTY;

    /**
     * 鉴权参数的位置。
     * <p>仅对 API Key 有效，可以是 HEADER、QUERY 或 COOKIE。
     * 默认为 HEADER。</p>
     *
     * @return 表示鉴权参数位置的 {@link Source}。
     */
    Source location() default Source.HEADER;

    /**
     * Basic Auth 的用户名，仅当 type=BASIC 时有效。
     *
     * @return 表示用户名的 {@link String}。
     */
    String username() default StringUtils.EMPTY;

    /**
     * Basic Auth 的密码，仅当 type=BASIC 时有效。
     *
     * @return 表示密码的 {@link String}。
     */
    String password() default StringUtils.EMPTY;

    /**
     * 动态鉴权提供器类。
     * <p>当指定时，将忽略静态配置（value、username、password 等），
     * 通过 Provider 动态获取鉴权信息。</p>
     *
     * @return 表示鉴权提供器类的 {@link Class}。
     */
    Class<? extends AuthProvider> provider() default AuthProvider.class;
}