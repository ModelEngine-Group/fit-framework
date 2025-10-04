/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.entity.serializer;

import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_DISPOSITION;
import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_TYPE;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fit.http.HttpMessage;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.EntityReadException;
import modelengine.fit.http.entity.EntitySerializer;
import modelengine.fit.http.entity.EntityWriteException;
import modelengine.fit.http.entity.FileEntity;
import modelengine.fit.http.entity.NamedEntity;
import modelengine.fit.http.entity.PartitionedEntity;
import modelengine.fit.http.entity.TextEntity;
import modelengine.fit.http.entity.support.DefaultNamedEntity;
import modelengine.fit.http.entity.support.DefaultPartitionedEntity;
import modelengine.fit.http.entity.support.DefaultTextEntity;
import modelengine.fit.http.header.ContentDisposition;
import modelengine.fit.http.header.ContentType;
import modelengine.fit.http.header.HeaderValue;
import modelengine.fit.http.util.HttpUtils;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.util.FileUtils;
import modelengine.fitframework.util.LineSeparator;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 表示消息体格式为 {@code 'multipart/*'} 的序列化器。
 *
 * @author 季聿阶
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc2046#section-5.1.1">RFC 2046</a>
 * @since 2022-10-12
 */
public class MultiPartEntitySerializer implements EntitySerializer<PartitionedEntity> {
    /** 表示 {@link MultiPartEntitySerializer} 的单例实现。 */
    public static final EntitySerializer<PartitionedEntity> INSTANCE = new MultiPartEntitySerializer();

    static final int TINY_BUFFER = 32;
    static final int SMALL_BUFFER = 64;
    static final int MEDIUM_BUFFER = 512;

    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final char HEADER_SEPARATOR = ':';
    private static final String BOUNDARY_SURROUND = "--";

    private final Map<String, Function<HeaderValue, HeaderValue>> functions =
            MapBuilder.<String, Function<HeaderValue, HeaderValue>>get()
                    .put(CONTENT_DISPOSITION.toLowerCase(Locale.ROOT), HeaderValue::toContentDisposition)
                    .put(CONTENT_TYPE.toLowerCase(Locale.ROOT), HeaderValue::toContentType)
                    .build();

    @Override
    public void serializeEntity(@Nonnull PartitionedEntity entity, Charset charset, OutputStream out) {
        String boundary = this.getBoundary(entity);
        try {
            for (NamedEntity namedEntity : entity.entities()) {
                this.writeBoundary(out, boundary, charset, false);
                this.writeHeaders(out, namedEntity, charset);
                this.writeEntityContent(out, namedEntity, charset);
            }
            this.writeBoundary(out, boundary, charset, true);
        } catch (IOException e) {
            throw new EntityWriteException("Failed to serialize entity of Content-Type 'multipart/*'.", e);
        }
    }

    /**
     * 获取 boundary 分隔符。
     *
     * @param entity 表示分块的消息体数据的 {@link PartitionedEntity}。
     * @return 表示 boundary 分隔符的 {@link String}。
     */
    private String getBoundary(PartitionedEntity entity) {
        String boundary = entity.belongTo()
                .contentType()
                .flatMap(ContentType::boundary)
                .orElseThrow(() -> new EntityWriteException("The boundary is not present in Content-Type."));
        return BOUNDARY_SURROUND + boundary;
    }

    /**
     * 写入分隔符。
     *
     * @param out 表示输出流的 {@link OutputStream}。
     * @param boundary 表示 boundary 分隔符的 {@link String}。
     * @param charset 表示字符集的 {@link Charset}。
     * @param isEnd 表示是否是终止分隔符的 {@code boolean}。
     * @throws IOException 当发生 I/O 异常时。
     */
    private void writeBoundary(OutputStream out, String boundary, Charset charset, boolean isEnd) throws IOException {
        out.write(BOUNDARY_SURROUND.getBytes(charset));
        out.write(boundary.getBytes(charset));
        if (isEnd) {
            out.write(BOUNDARY_SURROUND.getBytes(charset));
        }
        out.write(CR);
        out.write(LF);
    }

    /**
     * 写入消息头。
     *
     * @param out 表示输出流的 {@link OutputStream}。
     * @param namedEntity 表示带名字的消息体数据的 {@link NamedEntity}。
     * @param charset 表示字符集的 {@link Charset}。
     * @throws IOException 当发生 I/O 异常时。
     */
    private void writeHeaders(OutputStream out, NamedEntity namedEntity, Charset charset) throws IOException {
        // Write Content-Disposition header
        StringBuilder disposition = new StringBuilder("Content-Disposition: form-data");
        if (!StringUtils.isEmpty(namedEntity.name())) {
            disposition.append("; name=\"").append(namedEntity.name()).append("\"");
        }
        if (namedEntity.isFile()) {
            FileEntity fileEntity = namedEntity.asFile();
            disposition.append("; filename=\"").append(fileEntity.filename()).append("\"");
        }
        out.write(disposition.toString().getBytes(charset));
        out.write(CR);
        out.write(LF);

        // Write Content-Type header if it's a file
        if (namedEntity.isFile()) {
            Entity innerEntity = namedEntity.entity();
            String contentType = "Content-Type: " + innerEntity.resolvedMimeType().value();
            out.write(contentType.getBytes(charset));
            out.write(CR);
            out.write(LF);
        }

        // Write empty line
        out.write(CR);
        out.write(LF);
    }

    /**
     * 写入实体内容。
     *
     * @param out 表示输出流的 {@link OutputStream}。
     * @param namedEntity 表示带名字的消息体数据的 {@link NamedEntity}。
     * @param charset 表示字符集的 {@link Charset}。
     * @throws IOException 当发生 I/O 异常时。
     */
    private void writeEntityContent(OutputStream out, NamedEntity namedEntity, Charset charset) throws IOException {
        Entity innerEntity = namedEntity.entity();
        if (namedEntity.isText()) {
            TextEntity textEntity = cast(innerEntity);
            out.write(textEntity.content().getBytes(charset));
        } else if (namedEntity.isFile()) {
            FileEntity fileEntity = cast(innerEntity);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileEntity.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        out.write(CR);
        out.write(LF);
    }

    @Override
    public PartitionedEntity deserializeEntity(@Nonnull InputStream in, Charset charset,
            @Nonnull HttpMessage httpMessage, Type objectType) {
        String boundary = this.parseBoundary(httpMessage);
        byte[] pattern = boundary.getBytes(charset);
        try (InputStream input = new BufferedInputStream(new NonClosingInputStream(in))) {
            return this.deserializeEntity(input, charset, httpMessage, pattern);
        } catch (IOException e) {
            throw new EntityReadException("Failed to deserialize message body. [mimeType='multipart/*']", e);
        }
    }

    private PartitionedEntity deserializeEntity(InputStream in, Charset charset, HttpMessage httpMessage,
            byte[] pattern) throws IOException {
        boolean isEnd = this.findNextBoundary(in, pattern, null);
        if (isEnd) {
            return new DefaultPartitionedEntity(httpMessage, Collections.emptyList());
        }
        List<NamedEntity> namedEntities = new ArrayList<>();
        while (!isEnd) {
            List<String> headerLines = new ArrayList<>();
            String line = readMetadataHeaderLine(in, charset);
            while (!Objects.equals(line, StringUtils.EMPTY)) {
                headerLines.add(line);
                line = readMetadataHeaderLine(in, charset);
            }
            Map<String, HeaderValue> headerValues = this.parseHeaderValues(headerLines);
            ContentDisposition contentDisposition =
                    cast(headerValues.get(CONTENT_DISPOSITION.toLowerCase(Locale.ROOT)));
            Entity innerEntity;
            if (contentDisposition != null && contentDisposition.name().isPresent() && contentDisposition.fileName()
                    .isPresent()) {
                File tempFile = Files.createTempFile("entity-multipart-", ".tmp").toFile();
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    isEnd = this.findNextBoundary(in, pattern, out);
                } catch (IOException e) {
                    FileUtils.delete(tempFile);
                    throw e;
                }
                try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                    raf.setLength(tempFile.length() - 2);
                }
                innerEntity = FileEntity.create(httpMessage,
                        contentDisposition.fileName().get(),
                        new FileInputStream(tempFile),
                        tempFile.length(),
                        FileEntity.Position.INLINE,
                        tempFile);
            } else {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    isEnd = this.findNextBoundary(in, pattern, out);
                    String content = out.toString(charset.name());
                    content = content.substring(0, content.length() - 2);
                    innerEntity = new DefaultTextEntity(httpMessage, content);
                }
            }
            NamedEntity namedEntity = new DefaultNamedEntity(httpMessage, getName(contentDisposition), innerEntity);
            namedEntities.add(namedEntity);
        }
        return new DefaultPartitionedEntity(httpMessage, namedEntities);
    }

    private static String getName(ContentDisposition contentDisposition) {
        if (contentDisposition == null) {
            return StringUtils.EMPTY;
        }
        return contentDisposition.name().orElse(StringUtils.EMPTY);
    }

    private static String readMetadataHeaderLine(InputStream in, Charset charset) throws IOException {
        byte[] bytes = readMetadataHeaderLineByCrlf(in);
        String line = new String(bytes, charset);
        if (line.endsWith(LineSeparator.CRLF.value())) {
            line = line.substring(0, line.length() - 2);
        }
        return line;
    }

    /**
     * 寻找下一个分隔符。
     * <p>如果成功返回，则 {@code out} 中至少写入了 2 个字符。</p>
     *
     * @param in 表示消息体内容的输入流的 {@link InputStream}。
     * @param pattern 表示分隔符的字节数组的 {@code byte[]}。
     * @param out 表示需要保存的 Text 文本内容的输出流的 {@link OutputStream}。
     * @return 表示寻找到分隔符之后是否触及输入流的结尾的标志。不管是否寻找到下一个分隔符，只要触及输入流的结尾，则返回
     * {@code true}，否则返回 {@code false}，<b>注意：当返回 {@code false} 时，一定找到了下一个分隔符。</b>
     * @throws IOException 当发生 I/O 异常时。
     */
    private boolean findNextBoundary(InputStream in, byte[] pattern, OutputStream out) throws IOException {
        while (true) {
            byte[] bytes = readLineByCrlf(in, pattern.length + 2, false);
            if (bytes == null) {
                throw new IOException("The next boundary not found: no any content bytes.");
            }
            if (bytes.length < pattern.length + 2) {
                if (isLineBreak(bytes)) {
                    writeBytes(out, bytes);
                    continue;
                } else {
                    throw new IOException("The first boundary not found: unexpected exit.");
                }
            }
            boolean isEqualsPattern = equals(pattern, bytes);
            if (isLineBreak(bytes)) {
                if (isEqualsPattern) {
                    return false;
                } else {
                    writeBytes(out, bytes);
                    continue;
                }
            }
            if (isEqualsPattern && isEnd(bytes)) {
                return true;
            }
            if (!isEqualsPattern) {
                writeBytes(out, bytes);
            }
            this.readRemainedBytesInCurrentLine(in, out, isEqualsPattern, bytes[bytes.length - 1] == CR);
            if (isEqualsPattern) {
                return false;
            }
        }
    }

    private void readRemainedBytesInCurrentLine(InputStream in, OutputStream out, boolean isEqualsPattern,
            boolean hasPrecedingCr) throws IOException {
        // 当匹配分隔符成功时，如果当前行没有读完，后续大概率会跟少量的任意字符作为报文填充，因此选择小型缓存空间。
        int bufferSize = isEqualsPattern ? TINY_BUFFER : MEDIUM_BUFFER;
        boolean hasPrecedingByteCr = hasPrecedingCr;
        while (true) {
            byte[] bytes = readLineByCrlf(in, bufferSize, hasPrecedingByteCr);
            if (bytes == null) {
                throw new IOException("The next boundary not found: no more data and unexpected exit.");
            }
            if (isLineBreak(bytes) || this.isBoundaryLineBreak(bytes, hasPrecedingByteCr)) {
                if (isEqualsPattern) {
                    return;
                }
                writeBytes(out, bytes);
                return;
            }
            writeBytes(out, bytes);
            if (bytes.length < bufferSize) {
                throw new IOException("The next boundary not found: unexpected exit.");
            }
            hasPrecedingByteCr = bytes[bytes.length - 1] == CR;
        }
    }

    private static void writeBytes(OutputStream out, byte[] bytes) throws IOException {
        if (out != null) {
            out.write(bytes);
        }
    }

    private static boolean equals(byte[] src, byte[] dst) {
        for (int i = 0; i < src.length; i++) {
            if (src[i] != dst[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLineBreak(byte[] bytes) {
        if (bytes.length >= 2) {
            return bytes[bytes.length - 2] == CR && bytes[bytes.length - 1] == LF;
        } else {
            return false;
        }
    }

    private boolean isBoundaryLineBreak(byte[] bytes, boolean hasPrecedingCr) {
        return hasPrecedingCr && bytes.length == 1 && bytes[0] == LF;
    }

    private static boolean isEnd(byte[] bytes) {
        return bytes[bytes.length - 2] == '-' && bytes[bytes.length - 1] == '-';
    }

    private static byte[] readMetadataHeaderLineByCrlf(InputStream in) throws IOException {
        int buffer = SMALL_BUFFER;
        byte[] bytes = readLineByCrlf(in, buffer, false);
        if (bytes == null) {
            throw new IOException("The next line not found: unexpected exit.");
        }
        if (bytes.length < buffer || isLineBreak(bytes)) {
            return bytes;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeBytes(out, bytes);
            while (bytes.length == buffer && !isLineBreak(bytes)) {
                bytes = readLineByCrlf(in, buffer, bytes[bytes.length - 1] == CR);
                if (bytes == null) {
                    throw new IOException("The next line not found: unexpected exit.");
                }
                writeBytes(out, bytes);
            }
            return out.toByteArray();
        }
    }

    private static byte[] readLineByCrlf(InputStream in, int max, boolean hasPrecedingCr) throws IOException {
        boolean hasPrecedingByteCr = hasPrecedingCr;
        byte[] bytes = new byte[max];
        for (int i = 0; i < max; i++) {
            int read = in.read();
            if (read == -1) {
                if (i == 0) {
                    return null;
                }
                return Arrays.copyOf(bytes, i);
            }
            byte nextByte = (byte) read;
            bytes[i] = nextByte;
            if (nextByte == LF && hasPrecedingByteCr) {
                return Arrays.copyOf(bytes, i + 1);
            }
            hasPrecedingByteCr = nextByte == CR;
        }
        return bytes;
    }

    private String parseBoundary(HttpMessage message) {
        String boundary = message.contentType()
                .flatMap(ContentType::boundary)
                .orElseThrow(() -> new EntityReadException("The boundary is not present."));
        return BOUNDARY_SURROUND + boundary;
    }

    private Map<String, HeaderValue> parseHeaderValues(List<String> metadataLines) {
        Map<String, HeaderValue> result = new HashMap<>();
        for (String line : metadataLines) {
            int separatorIndex = line.indexOf(HEADER_SEPARATOR);
            if (separatorIndex < 0) {
                continue;
            }
            String headerName = line.substring(0, separatorIndex).trim().toLowerCase(Locale.ROOT);
            String headerRawValue = line.substring(separatorIndex + 1).trim();
            HeaderValue headerValue = HttpUtils.parseHeaderValue(headerRawValue);
            Function<HeaderValue, HeaderValue> function = this.functions.get(headerName);
            if (function == null) {
                continue;
            }
            HeaderValue parsed = function.apply(headerValue);
            result.put(headerName, parsed);
        }
        return result;
    }

    /**
     * 表示不关闭输入流的输入流。
     *
     * @author 邬涨财
     * @since 2023-12-20
     */
    private static class NonClosingInputStream extends FilterInputStream {
        public NonClosingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() {}
    }
}
