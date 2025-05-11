/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol;

import static modelengine.fitframework.inspection.Validation.notNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an HTTP request on the server side.
 *
 * @author 季聿阶
 * @since 2022-07-05
 */
public interface ServerRequest extends Message<RequestLine, MessageHeaders, ReadableMessageBody> {
    /**
     * Gets the local address of the HTTP request.
     *
     * @return The {@link Address} representing the local address.
     */
    Address localAddress();

    /**
     * Gets the remote address of the HTTP request.
     *
     * @return The {@link Address} representing the remote address.
     */
    Address remoteAddress();

    /**
     * Checks whether the HTTP request is secure.
     *
     * @return true if the HTTP request is secure; otherwise false.
     */
    boolean isSecure();

    /**
     * Reads the next byte from the HTTP message body.
     * Returns -1 if there is no more data available to read.
     *
     * @return The byte read, represented as an int in the range 0 to 255,
     * or -1 if there is no more data.
     * @throws IOException If an I/O error occurs.
     */
    int readBody() throws IOException;

    /**
     * Reads up to {@code bytes.length} bytes from the HTTP message body into the specified buffer.
     *
     * @param bytes The byte array into which the data is read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException If {@code bytes} is null.
     */
    default int readBody(byte[] bytes) throws IOException {
        return this.readBody(notNull(bytes, "The bytes to read cannot be null."), 0, bytes.length);
    }

    /**
     * Reads up to {@code len} bytes from the HTTP message body into the specified buffer,
     * starting at offset {@code off}.
     *
     * @param bytes The byte array into which the data is read.
     * @param off The start offset in the destination array {@code bytes}.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException If {@code bytes} is null.
     * @throws IndexOutOfBoundsException If {@code off} or {@code len} is negative,
     * or if {@code off + len} exceeds the length of {@code bytes}.
     */
    int readBody(byte[] bytes, int off, int len) throws IOException;

    /**
     * Gets the input stream for reading the body of the HTTP message.
     *
     * @return An {@link InputStream} representing the body content of the HTTP request.
     */
    InputStream getBodyInputStream();

    /**
     * Checks whether the current request is active.
     *
     * <p>An inactive request typically indicates that the underlying connection has been closed or reset.</p>
     *
     * @return true if the request is active; false otherwise.
     */
    boolean isActive();
}
