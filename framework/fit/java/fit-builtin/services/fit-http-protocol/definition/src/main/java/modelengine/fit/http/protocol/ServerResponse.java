/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.protocol;

import static modelengine.fitframework.inspection.Validation.notNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents an HTTP response on the server side.
 *
 * @author 季聿阶
 * @since 2022-07-05
 */
public interface ServerResponse
        extends Message<ConfigurableStatusLine, ConfigurableMessageHeaders, WritableMessageBody> {
    /**
     * Writes the start line and headers of the HTTP message.
     *
     * @throws IOException If an I/O error occurs.
     */
    void writeStartLineAndHeaders() throws IOException;

    /**
     * Writes a single byte to the HTTP message body.
     *
     * @param b The data to be written, represented as an int. Only the least significant byte is used.
     * @throws IOException If an I/O error occurs.
     */
    void writeBody(int b) throws IOException;

    /**
     * Writes the entire contents of the specified byte array to the HTTP message body.
     *
     * @param bytes The byte array containing the data to be written.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException If {@code bytes} is null.
     * @see #writeBody(byte[], int, int)
     */
    default void writeBody(byte[] bytes) throws IOException {
        this.writeBody(notNull(bytes, "The bytes to write cannot be null."), 0, bytes.length);
    }

    /**
     * Writes up to {@code len} bytes from the specified byte array,
     * starting at offset {@code off}, to the HTTP message body.
     *
     * @param bytes The byte array containing the data to be written.
     * @param off The start offset in the source array {@code bytes}.
     * @param len The number of bytes to write.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException If {@code bytes} is null.
     * @throws IndexOutOfBoundsException If {@code off} or {@code len} is negative,
     * or if {@code off + len} exceeds the length of {@code bytes}.
     */
    void writeBody(byte[] bytes, int off, int len) throws IOException;

    /**
     * Forces any buffered data to be written out immediately and sends the response end marker.
     *
     * @throws IOException If an I/O error occurs.
     */
    void flush() throws IOException;

    /**
     * Gets the output stream for writing the body of the HTTP message.
     *
     * @return An {@link OutputStream} representing the body content of the HTTP response.
     */
    OutputStream getBodyOutputStream();

    /**
     * Checks whether the current response is active.
     *
     * <p>An inactive response typically indicates that the underlying connection has been closed or reset.</p>
     *
     * @return true if the response is active; false otherwise.
     */
    boolean isActive();
}
