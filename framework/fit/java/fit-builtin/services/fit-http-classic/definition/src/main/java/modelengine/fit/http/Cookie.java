/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http;

import modelengine.fitframework.pattern.builder.BuilderFactory;
import modelengine.fitframework.util.StringUtils;

/**
 * 表示 Http 中的 Cookie。
 *
 * @author 季聿阶
 * @since 2022-07-06
 */
public interface Cookie {
    /**
     * 获取 Cookie 的名字。
     *
     * @return 表示 Cookie 名字的 {@link String}。
     */
    String name();

    /**
     * 获取 Cookie 的值。
     *
     * @return 表示 Cookie 值的 {@link String}。
     */
    String value();

    /**
     * 获取 Cookie 的版本号。
     * <p>其版本号的格式为 {@code ;Version=1 ...}，为 RFC 2109 的风格。</p>
     *
     * @return 表示 Cookie 版本号的 {@code int}。
     */
    @Deprecated
    int version();

    /**
     * 获取 Cookie 的注释。
     * <p>其注释的格式为 {@code ;Comment=VALUE ...}。</p>
     *
     * @return 表示 Cookie 注释的 {@link String}。
     */
    @Deprecated
    String comment();

    /**
     * 获取 Cookie 的可见域。
     * <p>其域的格式为 {@code ;Domain=VALUE ...}，只有指定域对该 Cookie 可见。</p>
     *
     * @return 表示 Cookie 可见域的 {@link String}。
     */
    String domain();

    /**
     * 获取 Cookie 的自动过期时间。
     * <p>其自动过期时间的格式为 {@code ;Max-Age=VALUE ...}，单位为秒。</p>
     *
     * @return 表示 Cookie 自动过期时间的 {@code int}。
     */
    int maxAge();

    /**
     * 获取 Cookie 的 URL 路径。
     * <p>其 URL 路径格式为 {@code ;Path=VALUE ...}，只有指定 URL 对该 Cookie 可见。</p>
     *
     * @return 表示 Cookie 的可见 URL 路径的 {@link String}。
     */
    String path();

    /**
     * 判断 Cookie 是否使用了 SSL。
     * <p>其是否使用 SSL 的格式为 {@code ;Secure ...}。</p>
     *
     * @return 如果 Cookie 使用了 SSL，则返回 {@code true}，否则，返回 {@code false}。
     */
    boolean secure();

    /**
     * 判断 Cookie 是否仅允许在服务端获取。
     * <p>其 HttpOnly 属性的格式为 {@code ;HttpOnly ...}，如果存在则表示仅服务端可访问。</p>
     *
     * @return 如果 Cookie 仅允许在服务端访问，则返回 {@code true}，否则返回 {@code false}。
     */
    boolean httpOnly();

    /**
     * 获取 Cookie 的 SameSite 属性。
     * <p>其 SameSite 属性的格式为 {@code ;SameSite=VALUE ...}，表示跨站请求策略。</p>
     *
     * @return SameSite 值，如 {@code "Strict"}、{@code "Lax"}、{@code "None"}。
     */
    String sameSite();

    /**
     * {@link Cookie} 的构建器。
     */
    interface Builder {
        /**
         * 向当前构建器中设置 Cookie 的名字。
         *
         * @param name 表示待设置的 Cookie 名字的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder name(String name);

        /**
         * 向当前构建器中设置 Cookie 的值。
         *
         * @param value 表示待设置的 Cookie 值的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder value(String value);

        /**
         * 向当前构建器中设置 Cookie 的版本。
         * <p>
         * 此属性源自 <a href="https://datatracker.ietf.org/doc/html/rfc2965">RFC 2965</a>，
         * 但已在 <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2">RFC 6265</a>
         * 中移出标准定义。现代浏览器会忽略该属性。
         * </p>
         *
         * @param version 表示待设置的 Cookie 版本的 {@code int}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        @Deprecated
        Builder version(int version);

        /**
         * 向当前构建器中设置 Cookie 的注释。
         * <p>
         * 此属性源自 <a href="https://datatracker.ietf.org/doc/html/rfc2965">RFC 2965</a>，
         * 但已在 <a href="https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2">RFC 6265</a>
         * 中移出标准定义。现代浏览器会忽略该属性。
         * </p>
         *
         * @param comment 表示待设置的 Cookie 注释的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        @Deprecated
        Builder comment(String comment);

        /**
         * 向当前构建器中设置 Cookie 的可见域。
         *
         * @param domain 表示待设置的 Cookie 可见域的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder domain(String domain);

        /**
         * 向当前构建器中设置 Cookie 的自动过期时间。
         *
         * @param maxAge 表示待设置的 Cookie 自动过期时间的 {@code int}，其单位为秒。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder maxAge(int maxAge);

        /**
         * 向当前构建器中设置 Cookie 的可见 URL 路径。
         *
         * @param path 表示待设置的 Cookie 可见 URL 路径的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder path(String path);

        /**
         * 向当前构建器中设置 Cookie 的安全性。
         *
         * @param secure 如果 Cookie 使用了 SSL，则为 {@code true}，否则，为 {@code false}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder secure(boolean secure);

        /**
         * 向当前构建器中设置 Cookie 是否仅允许在服务端获取。
         *
         * @param httpOnly 如果 Cookie 仅允许在服务端获取，则为 {@code true}，否则，为 {@code false}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder httpOnly(boolean httpOnly);

        /**
         * 向当前构建器中设置 Cookie 限制跨站请求时发送行为安全级别。
         * <p>
         * 该属性定义于 <a href="https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-rfc6265bis#section-4.1.2.7">
         * RFC 6265bis 草案第 4.1.2.7 节</a>，用于控制跨站请求时是否发送 Cookie。
         * 尽管该规范尚处于草案阶段，但已被主流浏览器（如 Chrome、Firefox、Safari、Edge）广泛支持。
         * </p>
         *
         * @param sameSite SameSite 值，如 "Strict", "Lax", "None"。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder sameSite(String sameSite);

        /**
         * 构建对象。
         *
         * @return 表示构建出来的对象的 {@link Cookie}。
         */
        Cookie build();
    }

    /**
     * 获取 {@link Cookie} 的构建器。
     *
     * @return 表示 {@link Cookie} 的构建器的 {@link Builder}。
     */
    static Builder builder() {
        return builder(null).value(StringUtils.EMPTY).maxAge(-1).secure(false).httpOnly(false);
    }

    /**
     * 获取 {@link Cookie} 的构建器，同时将指定对象的值进行填充。
     *
     * @param value 表示指定对象的 {@link Cookie}。
     * @return 表示 {@link Cookie} 的构建器的 {@link Builder}。
     */
    static Builder builder(Cookie value) {
        return BuilderFactory.get(Cookie.class, Builder.class).create(value);
    }
}
