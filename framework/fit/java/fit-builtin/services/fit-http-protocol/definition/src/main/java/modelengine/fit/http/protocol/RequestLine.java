/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol;

import modelengine.fit.http.protocol.support.DefaultRequestLine;

/**
 * Represents the start line of an HTTP request.
 *
 * @author 季聿阶
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-5.1">RFC 2616</a>
 * @since 2022-07-07
 */
public interface RequestLine extends StartLine {
    /**
     * Retrieves the HTTP request method.
     *
     * @return The HTTP request method as a {@link HttpRequestMethod}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-5.1.1">RFC 2616</a>
     */
    HttpRequestMethod method();

    /**
     * Retrieves the URI of the HTTP request.
     *
     * @return The URI of the HTTP request as a {@link String}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-5.1.2">RFC 2616</a>
     */
    String requestUri();

    /**
     * Retrieves the query parameters of the HTTP request.
     *
     * @return The query parameters of the HTTP request as a {@link QueryCollection}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-5.1.2">RFC 2616</a>
     */
    QueryCollection queries();

    /**
     * Creates a new request line with the specified HTTP version, method, request URI, and query parameters.
     *
     * @param httpVersion The HTTP version as a {@link HttpVersion}.
     * @param method The request method as a {@link HttpRequestMethod}.
     * @param requestUri The request URI as a {@link String}.
     * @param queries The query parameters as a {@link QueryCollection}.
     * @return A new instance of {@link RequestLine}.
     * @throws IllegalArgumentException If {@code httpVersion} is {@code null}.
     * @throws IllegalArgumentException If {@code method} is {@code null}.
     * @throws IllegalArgumentException If {@code requestUri} is {@code null} or a blank string.
     */
    static RequestLine create(HttpVersion httpVersion, HttpRequestMethod method, String requestUri,
            QueryCollection queries) {
        return new DefaultRequestLine(httpVersion, method, requestUri, queries);
    }
}