/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server;

import modelengine.fit.http.AttributeCollection;
import modelengine.fit.http.HttpClassicRequest;
import modelengine.fit.http.HttpResource;
import modelengine.fit.http.protocol.Address;
import modelengine.fit.http.protocol.ServerRequest;
import modelengine.fit.http.server.support.DefaultHttpClassicServerRequest;

import java.io.Closeable;

/**
 * Represents a classic HTTP server request.
 *
 * @author 季聿阶
 * @since 2022-07-07
 */
public interface HttpClassicServerRequest extends HttpClassicRequest, Closeable {
    /**
     * Gets the collection of all attributes associated with this HTTP request.
     *
     * @return The {@link AttributeCollection} containing all request attributes.
     */
    AttributeCollection attributes();

    /**
     * Gets the local address associated with this HTTP request.
     *
     * @return The {@link Address} representing the local endpoint.
     */
    Address localAddress();

    /**
     * Gets the remote address associated with this HTTP request.
     *
     * @return The {@link Address} representing the remote endpoint.
     */
    Address remoteAddress();

    /**
     * Checks whether this HTTP request is secure (e.g., using HTTPS).
     *
     * @return true if the request is secure; false otherwise.
     */
    boolean isSecure();

    /**
     * Gets the binary content of the structured data in the message body of the HTTP request.
     *
     * @return A byte array containing the entity body data.
     */
    byte[] entityBytes();

    /**
     * Checks whether the current request is active.
     *
     * <p>An inactive request typically indicates that the underlying connection has been closed or reset.</p>
     *
     * @return true if the request is active; false otherwise.
     */
    boolean isActive();

    /**
     * Creates an instance of a classic HTTP server request.
     *
     * @param httpResource The HTTP resource associated with the request, as a {@link HttpResource}.
     * @param serverRequest The underlying server request, as a {@link ServerRequest}.
     * @return A newly created instance of {@link HttpClassicServerRequest}.
     */
    static HttpClassicServerRequest create(HttpResource httpResource, ServerRequest serverRequest) {
        return new DefaultHttpClassicServerRequest(httpResource, serverRequest);
    }
}
