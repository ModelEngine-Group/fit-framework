/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.support;

import static modelengine.fit.http.protocol.MessageHeaderNames.CACHE_CONTROL;
import static modelengine.fit.http.protocol.MessageHeaderNames.CONNECTION;
import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_DISPOSITION;
import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_LENGTH;
import static modelengine.fit.http.protocol.MessageHeaderNames.COOKIE;
import static modelengine.fit.http.protocol.MessageHeaderNames.TRANSFER_ENCODING;
import static modelengine.fit.http.protocol.MessageHeaderValues.CHUNKED;
import static modelengine.fit.http.protocol.MessageHeaderValues.KEEP_ALIVE;
import static modelengine.fit.http.protocol.MessageHeaderValues.NO_CACHE;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fit.http.HttpResource;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.FileEntity;
import modelengine.fit.http.entity.ReadableBinaryEntity;
import modelengine.fit.http.entity.TextEventStreamEntity;
import modelengine.fit.http.entity.WritableBinaryEntity;
import modelengine.fit.http.entity.support.DefaultWritableBinaryEntity;
import modelengine.fit.http.header.ContentDisposition;
import modelengine.fit.http.header.ContentType;
import modelengine.fit.http.header.HeaderValue;
import modelengine.fit.http.header.ParameterCollection;
import modelengine.fit.http.protocol.ConfigurableMessageHeaders;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.http.protocol.MessageHeaderValues;
import modelengine.fit.http.protocol.ServerResponse;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fit.http.server.InternalServerErrorException;
import modelengine.fit.http.support.AbstractHttpClassicResponse;
import modelengine.fitframework.resource.UrlUtils;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 表示 {@link HttpClassicServerResponse} 的默认实现。
 *
 * @author 季聿阶
 * @author 易文渊
 * @since 2022-11-25
 */
public class DefaultHttpClassicServerResponse extends AbstractHttpClassicResponse implements HttpClassicServerResponse {
    private static final String FILENAME_PARAMETER_KEY = "filename";
    private static final String FILENAME_STAR_PARAMETER_KEY = "filename*";
    private static final String ZERO = "0";

    private final ServerResponse serverResponse;
    private Entity entity;

    /**
     * 创建经典的服务端的 Http 响应对象。
     *
     * @param httpResource 表示 Http 的资源的 {@link HttpResource}。
     * @param serverResponse 表示服务端的 Http 响应的 {@link ServerResponse}。
     */
    public DefaultHttpClassicServerResponse(HttpResource httpResource, ServerResponse serverResponse) {
        super(httpResource,
                notNull(serverResponse, "The server response cannot be null.").startLine(),
                serverResponse.headers());
        this.serverResponse = serverResponse;
    }

    @Override
    public Optional<Entity> entity() {
        return Optional.ofNullable(this.entity);
    }

    @Override
    public void statusCode(int statusCode) {
        if (this.isCommitted()) {
            return;
        }
        this.serverResponse.startLine().statusCode(statusCode);
    }

    @Override
    public void reasonPhrase(String reasonPhrase) {
        if (this.isCommitted()) {
            return;
        }
        this.serverResponse.startLine().reasonPhrase(reasonPhrase);
    }

    @Override
    public ConfigurableMessageHeaders headers() {
        return this.serverResponse.headers();
    }

    @Override
    public void entity(Entity entity) {
        if (this.isCommitted()) {
            return;
        }
        this.entity = entity;
    }

    private void setFileEntityHeaders(ConfigurableMessageHeaders headers, FileEntity fileEntity) {
        HeaderValue headerValue = fileEntity.isAttachment() ? HeaderValue.create(MessageHeaderValues.ATTACHMENT,
                createAttachedFileName(fileEntity)) : HeaderValue.create(MessageHeaderValues.INLINE);
        ContentDisposition contentDisposition = headerValue.toContentDisposition();
        headers.set(CONTENT_DISPOSITION, contentDisposition.toString());
    }

    private static ParameterCollection createAttachedFileName(FileEntity fileEntity) {
        boolean isAscii = StringUtils.isAscii(fileEntity.filename());
        if (isAscii) {
            return ParameterCollection.create().set(FILENAME_PARAMETER_KEY, "\"" + fileEntity.filename() + "\"");
        } else {
            String encodedFilename = UrlUtils.encodePath(fileEntity.filename());
            return ParameterCollection.create().set(FILENAME_STAR_PARAMETER_KEY, "UTF-8''" + encodedFilename);
        }
    }

    @Override
    public WritableBinaryEntity writableBinaryEntity() throws IOException {
        if (this.isCommitted()) {
            throw new InternalServerErrorException("The http classic server response has already committed.");
        }
        this.entity = new DefaultWritableBinaryEntity(this, this.serverResponse);
        this.statusCode(HttpResponseStatus.OK.statusCode());
        this.headers().set(TRANSFER_ENCODING, CHUNKED);
        this.commit();
        this.serverResponse.writeStartLineAndHeaders();
        return ObjectUtils.cast(this.entity);
    }

    @Override
    public void send() {
        this.commit();
        try {
            Charset charset = this.contentType().flatMap(ContentType::charset).orElse(StandardCharsets.UTF_8);
            if (this.entity == null) {
                this.headers().set(CONTENT_LENGTH, ZERO);
                this.serverResponse.writeStartLineAndHeaders();
            } else if (this.entity instanceof ReadableBinaryEntity) {
                if (this.entity instanceof FileEntity) {
                    FileEntity actual = cast(this.entity);
                    this.headers().set(CONTENT_LENGTH, String.valueOf(actual.length()));
                } else if (!this.headers().contains(CONTENT_LENGTH)) {
                    this.headers().set(TRANSFER_ENCODING, CHUNKED);
                }
                this.serverResponse.writeStartLineAndHeaders();
                ReadableBinaryEntity readableBinaryEntity = cast(this.entity);
                byte[] bytes = new byte[512];
                int read;
                while ((read = readableBinaryEntity.read(bytes)) > -1) {
                    this.serverResponse.writeBody(bytes, 0, read);
                }
            } else if (this.entity instanceof WritableBinaryEntity) {
                // WritableBinaryEntity 已经在用户代码层面进行了输出，因此此处什么都不需要处理。
            } else if (this.entity instanceof TextEventStreamEntity) {
                this.headers().set(CACHE_CONTROL, NO_CACHE);
                this.headers().set(CONNECTION, KEEP_ALIVE);
                this.headers().set(TRANSFER_ENCODING, CHUNKED);
                this.serverResponse.writeStartLineAndHeaders();
                this.sendTextEventStream(cast(this.entity));
            } else {
                byte[] entityBytes = this.entitySerializer().serializeEntity(ObjectUtils.cast(this.entity), charset);
                this.headers().set(CONTENT_LENGTH, String.valueOf(entityBytes.length));
                this.serverResponse.writeStartLineAndHeaders();
                this.serverResponse.writeBody(entityBytes);
            }
            this.serverResponse.flush();
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to write response.", e);
        }
    }

    @Override
    public boolean isActive() {
        return this.serverResponse.isActive();
    }

    private void sendTextEventStream(TextEventStreamEntity eventStreamEntity) throws IOException {
        ObjectSerializer objectSerializer = this.jsonSerializer()
                .orElseThrow(() -> new IllegalStateException("The json serializer cannot be null."));
        AtomicReference<Exception> exception = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        eventStreamEntity.stream()
                .map(sse -> sse.serialize(objectSerializer).getBytes(StandardCharsets.UTF_8))
                .subscribe(null, (subscription, bytes) -> {
                    try {
                        this.serverResponse.writeBody(bytes);
                    } catch (IOException e) {
                        subscription.cancel();
                        exception.set(e);
                        latch.countDown();
                    }
                }, subscription -> latch.countDown(), (ignore, e) -> {
                    exception.set(e);
                    latch.countDown();
                });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalServerErrorException("Failed to execute handler.", e);
        }
        Exception e = exception.get();
        if (e == null) {
            return;
        }
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        throw new InternalServerErrorException("Failed to execute handler.", e);
    }

    @Override
    protected void commit() {
        if (this.isCommitted()) {
            return;
        }
        this.headers().set(COOKIE, this.cookies().toString());
        if (this.entity != null) {
            this.setContentTypeByEntity(this.headers(), this.entity);
            if (this.entity instanceof FileEntity) {
                this.setFileEntityHeaders(this.headers(), ObjectUtils.cast(this.entity));
            }
        }
        super.commit();
    }

    @Override
    public void close() throws IOException {
        this.serverResponse.close();
        if (this.entity != null) {
            this.entity.close();
            this.entity = null;
        }
    }
}
