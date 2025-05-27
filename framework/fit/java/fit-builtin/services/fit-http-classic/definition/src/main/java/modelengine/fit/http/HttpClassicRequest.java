/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http;

import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fit.http.protocol.QueryCollection;

/**
 * 表示经典的 Http 请求。
 *
 * @author 季聿阶
 * @since 2022-11-25
 */
public interface HttpClassicRequest extends HttpMessage {
    /**
     * 获取 Http 请求的方法。
     *
     * @return 表示 Http 请求的方法的 {@link HttpRequestMethod}。
     */
    HttpRequestMethod method();

    /**
     * 获取 Http 请求的 URI。
     *
     * @return 表示 Http 请求 URI 的 {@link String}。
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-5.1.2">RFC 2616</a>
     */
    String requestUri();

    /**
     * 获取 Http 请求的主机地址。
     *
     * @return 表示 Http 请求的主机地址的 {@link String}。
     */
    String host();

    /**
     * 获取 Http 请求的路径。
     * <p>请求路径表示 Http 请求的域名之后，查询参数之前的部分。</p>
     * <p>例如：请求为 {@code http://www.demo.com/a/b?x=1}，其路径为 {@code /a/b}。</p>
     *
     * @return 表示 Http 请求路径的 {@link String}。
     */
    String path();

    /**
     * 获取 Http 请求的查询参数集合。
     *
     * @return 表示 Http 请求的查询参数集合的 {@link QueryCollection}。
     */
    QueryCollection queries();
}
