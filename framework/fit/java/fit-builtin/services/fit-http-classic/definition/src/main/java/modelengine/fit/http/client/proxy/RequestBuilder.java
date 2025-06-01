/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy;

import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientRequest;
import modelengine.fit.http.client.proxy.support.DefaultRequestBuilder;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.protocol.HttpRequestMethod;

/**
 * Represents a builder for HTTP requests.
 *
 * @author 王攀博
 * @since 2024-06-08
 */
public interface RequestBuilder {
    /**
     * Sets the HTTP client.
     *
     * @param httpClassicClient The HTTP client to be used.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder client(HttpClassicClient httpClassicClient);

    /**
     * Sets the HTTP request method.
     *
     * @param method The HTTP request method {@link HttpRequestMethod}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder method(HttpRequestMethod method);

    /**
     * Sets the protocol for the HTTP request.
     *
     * @param protocol The protocol for the HTTP request {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder protocol(String protocol);

    /**
     * Sets the domain for the HTTP request.
     *
     * @param domain The domain for the HTTP request {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder domain(String domain);

    /**
     * Sets the host for the HTTP request.
     *
     * @param host The host for the HTTP request {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder host(String host);

    /**
     * Sets the port for the HTTP request.
     *
     * @param port The port for the HTTP request {@link int}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder port(int port);

    /**
     * Sets the path pattern for the HTTP request.
     *
     * @param pathPattern The path pattern for the HTTP request {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder pathPattern(String pathPattern);

    /**
     * Sets the path variable for the HTTP request.
     *
     * @param key The key for the path variable {@link String}.
     * @param pathVariable The value for the path variable {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder pathVariable(String key, String pathVariable);

    /**
     * Adds a key-value pair to the query parameters of the HTTP request.
     *
     * @param key The key for the query parameter {@link String}.
     * @param value The value for the query parameter {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder query(String key, String value);

    /**
     * Sets the header for the HTTP request.
     *
     * @param name The name of the header {@link String}.
     * @param header The content of the header {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder header(String name, String header);

    /**
     * Sets the cookie for the HTTP request.
     *
     * @param key The key for the cookie {@link String}.
     * @param value The value for the cookie {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder cookie(String key, String value);

    /**
     * Sets the entity (body content) for the HTTP request.
     *
     * @param entity The entity (body content) for the HTTP request {@link Entity}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder entity(Entity entity);

    /**
     * Sets the form entity (body content) for the HTTP request.
     *
     * @param key The key for the form entity {@link String}.
     * @param value The value for the form entity {@link String}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder formEntity(String key, String value);

    /**
     * Sets the JSON entity (body content) for the HTTP request.
     *
     * @param propertyValuePath The property path for the JSON entity {@link String}.
     * @param value The value for the JSON entity {@link Object}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder jsonEntity(String propertyValuePath, Object value);

    /**
     * Sets the authorization type for the HTTP request.
     *
     * @param authorization The authorization information for the HTTP request {@link Authorization}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder authorization(Authorization authorization);

    /**
     * Sets the authorization information for the HTTP request.
     *
     * @param key The key for the authorization information {@link String}.
     * @param value The value for the authorization information {@link Object}.
     * @return The builder for HTTP request parameters {@link RequestBuilder}.
     */
    RequestBuilder authorizationInfo(String key, Object value);

    /**
     * Builds the HTTP request parameters.
     *
     * @return The built HTTP request parameters {@link HttpClassicClientRequest}.
     */
    HttpClassicClientRequest build();

    /**
     * Creates a new instance of the request builder.
     *
     * @return A new instance of the request builder {@link RequestBuilder}.
     */
    static RequestBuilder create() {
        return new DefaultRequestBuilder();
    }
}