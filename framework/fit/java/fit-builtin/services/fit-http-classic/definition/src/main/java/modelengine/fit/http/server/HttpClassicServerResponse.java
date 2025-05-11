/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server;

import modelengine.fit.http.HttpClassicResponse;
import modelengine.fit.http.HttpResource;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.WritableBinaryEntity;
import modelengine.fit.http.header.ConfigurableCookieCollection;
import modelengine.fit.http.protocol.ConfigurableMessageHeaders;
import modelengine.fit.http.protocol.ServerResponse;
import modelengine.fit.http.server.support.DefaultHttpClassicServerResponse;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a classic HTTP server response.
 *
 * @author 季聿阶
 * @since 2022-11-25
 */
public interface HttpClassicServerResponse extends HttpClassicResponse, Closeable {
    /**
     * Sets the status code for the HTTP response.
     *
     * @param statusCode The HTTP status code as an {@code int}.
     */
    void statusCode(int statusCode);

    /**
     * Sets the reason phrase for the HTTP response.
     *
     * @param reasonPhrase The reason phrase describing the status, as a {@link String}.
     */
    void reasonPhrase(String reasonPhrase);

    /**
     * Gets the collection of message headers for the HTTP response.
     *
     * <p><b>Note:</b> To modify cookie-related information, do not operate directly on this object.
     * Use {@link #cookies()} instead.</p>
     *
     * @return The configurable message headers of the HTTP response as a {@link ConfigurableMessageHeaders}.
     */
    @Override
    ConfigurableMessageHeaders headers();

    /**
     * Gets the collection of cookies included in the HTTP response.
     *
     * @return The configurable cookie collection of the HTTP response as a {@link ConfigurableCookieCollection}.
     */
    @Override
    ConfigurableCookieCollection cookies();

    /**
     * Sets the structured data of the HTTP response body.
     *
     * <p><b>Note:</b> This method cannot be used together with {@link #writableBinaryEntity()}.</p>
     *
     * @param entity The structured data to be set as the HTTP response body, as an {@link Entity}.
     */
    void entity(Entity entity);

    /**
     * Gets the output stream for writing binary data to the HTTP response body.
     *
     * <p><b>Note:</b> This method cannot be used together with {@link #entity(Entity)}.
     * Invoking this method will immediately send the HTTP headers.</p>
     *
     * @return A {@link WritableBinaryEntity} representing the output stream for the response body.
     * @throws IOException If an I/O error occurs during writing.
     */
    WritableBinaryEntity writableBinaryEntity() throws IOException;

    /**
     * Sends the current HTTP response to the client.
     */
    void send();

    /**
     * Checks whether the current response is active.
     *
     * <p>An inactive response typically indicates that the underlying connection has been closed or reset.</p>
     *
     * @return true if the response is active; false otherwise.
     */
    boolean isActive();

    /**
     * Creates an instance of a classic HTTP server response.
     *
     * @param httpResource The HTTP resource associated with the response, as a {@link HttpResource}.
     * @param serverResponse The underlying server response, as a {@link ServerResponse}.
     * @return A newly created instance of {@link HttpClassicServerResponse}.
     */
    static HttpClassicServerResponse create(HttpResource httpResource, ServerResponse serverResponse) {
        return new DefaultHttpClassicServerResponse(httpResource, serverResponse);
    }
}
