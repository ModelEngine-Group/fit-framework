/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.header;

import modelengine.fit.http.Cookie;

import java.util.List;
import java.util.Optional;

/**
 * 表示 Http 中只读的 Cookie 集合。
 *
 * @author 季聿阶
 * @since 2022-07-06
 */
public interface CookieCollection {
    /**
     * 获取指定名字的 {@link Cookie}。
     * <p>如果存在多个同名 Cookie，返回第一个匹配的 Cookie。</p>
     *
     * @param name 表示 Cookie 名字的 {@link Optional}{@code <}{@link String}{@code >}。
     * @return 表示指定名字的 {@link Cookie}。
     */
    Optional<Cookie> get(String name);

    /**
     * 根据名字查找所有匹配的 {@link Cookie}。
     *
     * @param name 表示 Cookie 名字的 {@link String}。
     * @return 返回所有匹配名字的 {@link Cookie} 列表。
     */
    List<Cookie> all(String name);

    /**
     * 获取集合中所有的 {@link Cookie}。
     *
     * @return 表示所有 {@link Cookie} 列表的 {@link List}{@code <}{@link Cookie}{@code >}。
     */
    List<Cookie> all();

    /**
     * 获取所有 {@link Cookie} 的数量。
     *
     * @return 表示所有 {@link Cookie} 的数量的 {@code int}。
     */
    int size();

    /**
     * 将集合转换为 HTTP 请求头中 Cookie 形式的字符串。
     * <p>格式为 {@code name1=value1; name2=value2; ...}。</p>
     *
     * @return 表示请求头的字符串。
     */
    String toRequestHeaderValue();

    /**
     * 将集合转换为 HTTP 响应头形式的字符串列表。
     * <p>每个 Cookie 对应一个 {@code Set-Cookie: ...} 头。</p>
     *
     * @return 表示响应头列表的 {@link List}{@code <}{@link String}{@code >}。
     */
    List<String> toResponseHeadersValues();
}
