/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.support;

import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_LENGTH;
import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_TYPE;
import static modelengine.fit.http.protocol.MessageHeaderNames.COOKIE;
import static modelengine.fit.http.protocol.MessageHeaderNames.TRANSFER_ENCODING;
import static modelengine.fit.http.protocol.MessageHeaderValues.CHUNKED;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.getIfNull;

import modelengine.fit.http.HttpMessage;
import modelengine.fit.http.HttpResource;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.EntitySerializer;
import modelengine.fit.http.header.ConfigurableCookieCollection;
import modelengine.fit.http.header.ContentType;
import modelengine.fit.http.header.HeaderValue;
import modelengine.fit.http.header.ParameterCollection;
import modelengine.fit.http.header.support.DefaultContentType;
import modelengine.fit.http.protocol.ConfigurableMessageHeaders;
import modelengine.fit.http.protocol.HttpVersion;
import modelengine.fit.http.protocol.MessageHeaders;
import modelengine.fit.http.protocol.MimeType;
import modelengine.fit.http.protocol.RequestLine;
import modelengine.fit.http.protocol.StartLine;
import modelengine.fit.http.util.HttpUtils;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.StringUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示 {@link HttpMessage} 的抽象实现。
 *
 * @author 季聿阶
 * @since 2022-08-03
 */
public abstract class AbstractHttpMessage implements HttpMessage {
    private static final String COOKIE_DELIMITER = ";";

    private final ParameterCollection parameters =
            ParameterCollection.create().set(DefaultContentType.CHARSET, StandardCharsets.UTF_8.name());
    private final HttpResource httpResource;
    private final StartLine startLine;
    private final MessageHeaders headers;
    private final ConfigurableCookieCollection cookies;
    private final Map<MimeType, EntitySerializer<? extends Entity>> customEntitySerializers = new HashMap<>();
    private ObjectSerializer customJsonSerializer;
    private boolean isCommitted;

    /**
     * 创建 Http 消息对象。
     *
     * @param httpResource 表示 Http 的资源的 {@link HttpResource}。
     * @param startLine 表示 Http 请求的起始行的 {@link RequestLine}。
     * @param headers 表示只读的 Http 消息头集合的 {@link MessageHeaders}。
     */
    protected AbstractHttpMessage(HttpResource httpResource, StartLine startLine, MessageHeaders headers) {
        this.httpResource = notNull(httpResource, "The http resource cannot be null.");
        this.startLine = notNull(startLine, "The start line cannot be null.");
        this.headers = notNull(headers, "The message headers cannot be null.");
        String actualCookie = String.join(COOKIE_DELIMITER, this.headers.all(COOKIE));
        this.cookies = ConfigurableCookieCollection.create(HttpUtils.parseHeaderValue(actualCookie));
    }

    @Override
    public HttpVersion httpVersion() {
        return this.startLine.httpVersion();
    }

    @Override
    public MessageHeaders headers() {
        return this.headers;
    }

    @Override
    public HttpResource httpResource() {
        return this.httpResource;
    }

    @Override
    public Optional<String> transferEncoding() {
        return this.headers.first(TRANSFER_ENCODING);
    }

    @Override
    public boolean isChunked() {
        return this.transferEncoding()
                .filter(transferEncoding -> Objects.equals(transferEncoding, CHUNKED))
                .isPresent();
    }

    @Override
    public Optional<ContentType> contentType() {
        return this.headers.first(CONTENT_TYPE).map(HttpUtils::parseHeaderValue).map(DefaultContentType::new);
    }

    /**
     * 根据指定的数据类型设置消息头的 {@code 'Content-Type'}。
     *
     * @param headers 表示待设置的消息头的 {@link ConfigurableMessageHeaders}。
     * @param entity 表示指定的数据类型的 {@link Entity}。
     * @throws UnsupportedOperationException 当 {@code entity} 的类型为不支持的类型时。
     */
    protected void setContentTypeByEntity(ConfigurableMessageHeaders headers, Entity entity) {
        boolean isPresent = headers.first(CONTENT_TYPE).isPresent();
        if (isPresent) {
            return;
        }
        ParameterCollection mergedParameters = ParameterCollection.create();
        for (String key : this.parameters.keys()) {
            this.parameters.get(key).ifPresent(value -> mergedParameters.set(key, value));
        }
        entity.resolvedParameters().forEach(mergedParameters::set);
        ContentType contentType =
                HeaderValue.create(entity.resolvedMimeType().value(), mergedParameters).toContentType();
        notNull(contentType,
                () -> new UnsupportedOperationException(StringUtils.format(
                        "Not supported entity type. " + "[entityType={0}]", entity.getClass().getName())));
        headers.set(CONTENT_TYPE, contentType.toString());
    }

    @Override
    public int contentLength() {
        String value = this.headers.first(CONTENT_LENGTH).orElse(null);
        if (value == null) {
            return -1;
        }
        return Integer.parseInt(value);
    }

    @Override
    public ConfigurableCookieCollection cookies() {
        return this.cookies;
    }

    /**
     * 提交当前的 Http 消息。
     * <p>提交之后的 Http 消息将无法修改。</p>
     */
    protected void commit() {
        this.isCommitted = true;
    }

    @Override
    public boolean isCommitted() {
        return this.isCommitted;
    }

    @Override
    public EntitySerializer<? extends Entity> entitySerializer() {
        MimeType mimeType = this.mimeTypeOrDefault();
        return this.getEntitySerializer(mimeType, null);
    }

    @Override
    public EntitySerializer<? extends Entity> entitySerializer(Type type) {
        MimeType mimeType = this.mimeTypeOrDefault();
        return this.getEntitySerializer(mimeType, type);
    }

    private EntitySerializer<?> getEntitySerializer(MimeType mimeType, Type type) {
        if (mimeType == MimeType.APPLICATION_JSON && type != null) {
            return this.httpResource()
                    .serializers()
                    .jsonEntity(type)
                    .orElseThrow(() -> new IllegalStateException("No json serializer."));
        }
        if (mimeType == MimeType.TEXT_EVENT_STREAM && type != null) {
            return this.httpResource()
                    .serializers()
                    .textEventStreamEntity(type)
                    .orElseThrow(() -> new IllegalStateException("No json serializer."));
        }
        EntitySerializer<?> entitySerializer = this.customEntitySerializers.get(mimeType);
        if (entitySerializer == null) {
            entitySerializer = this.httpResource().serializers().entities().get(mimeType);
        }
        return getIfNull(entitySerializer, EntitySerializer::readableBinarySerializer);
    }

    @Override
    public void customEntitySerializer(MimeType mimeType, EntitySerializer<? extends Entity> serializer) {
        if (mimeType == null || serializer == null) {
            return;
        }
        this.customEntitySerializers.put(mimeType, serializer);
    }

    @Override
    public void customJsonSerializer(ObjectSerializer serializer) {
        if (serializer == null) {
            return;
        }
        this.customJsonSerializer = serializer;
    }

    @Override
    public Optional<ObjectSerializer> jsonSerializer() {
        if (this.customJsonSerializer != null) {
            return Optional.of(this.customJsonSerializer);
        }
        return this.httpResource().serializers().json();
    }
}
