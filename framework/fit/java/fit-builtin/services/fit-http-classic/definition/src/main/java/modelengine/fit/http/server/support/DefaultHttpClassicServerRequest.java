/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.AttributeCollection;
import modelengine.fit.http.HttpResource;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.EntityReadException;
import modelengine.fit.http.header.ContentType;
import modelengine.fit.http.protocol.Address;
import modelengine.fit.http.protocol.ServerRequest;
import modelengine.fit.http.protocol.util.BodyUtils;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.InternalServerErrorException;
import modelengine.fit.http.server.UnsupportedMediaTypeException;
import modelengine.fit.http.support.AbstractHttpClassicRequest;
import modelengine.fit.http.support.DefaultAttributeCollection;
import modelengine.fitframework.util.LazyLoader;
import modelengine.fitframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * {@link HttpClassicServerRequest} 的默认实现。
 *
 * @author 季聿阶
 * @since 2022-07-08
 */
public class DefaultHttpClassicServerRequest extends AbstractHttpClassicRequest implements HttpClassicServerRequest {
    private final ServerRequest serverRequest;
    private final LazyLoader<byte[]> entityBytesLoader = new LazyLoader<>(this::actualEntityBytes);
    private final LazyLoader<Optional<Entity>> entityLoader = new LazyLoader<>(this::actualEntity);
    private final AttributeCollection attributes = new DefaultAttributeCollection();

    /**
     * 创建经典的服务端的 Http 请求对象。
     *
     * @param httpResource 表示 Http 的资源的 {@link HttpResource}。
     * @param serverRequest 表示服务端的 Http 请求的 {@link ServerRequest}。
     */
    public DefaultHttpClassicServerRequest(HttpResource httpResource, ServerRequest serverRequest) {
        super(httpResource,
                notNull(serverRequest, "The server request cannot be null.").startLine(),
                serverRequest.headers());
        this.serverRequest = serverRequest;
        this.commit();
    }

    @Override
    public Optional<Entity> entity() {
        return this.entityLoader.get();
    }

    @Override
    public AttributeCollection attributes() {
        return this.attributes;
    }

    @Override
    public Address localAddress() {
        return this.serverRequest.localAddress();
    }

    @Override
    public Address remoteAddress() {
        return this.serverRequest.remoteAddress();
    }

    @Override
    public boolean isSecure() {
        return this.serverRequest.isSecure();
    }

    @Override
    public byte[] entityBytes() {
        return this.entityBytesLoader.get();
    }

    @Override
    public boolean isActive() {
        return this.serverRequest.isActive();
    }

    private Optional<Entity> actualEntity() {
        Charset charset = this.contentType().flatMap(ContentType::charset).orElse(StandardCharsets.UTF_8);
        try {
            if (this.entityBytesLoader.isLoaded()) {
                byte[] bytes = this.entityBytes();
                return Optional.of(this.entitySerializer().deserializeEntity(bytes, charset, this));
            } else {
                InputStream inputStream = this.serverRequest.getBodyInputStream();
                return Optional.of(this.entitySerializer().deserializeEntity(inputStream, charset, this));
            }
        } catch (EntityReadException e) {
            throw new UnsupportedMediaTypeException(StringUtils.format("Unsupported media type. [mimeType='{0}']",
                    this.mimeTypeOrDefault().value()), e);
        }
    }

    private byte[] actualEntityBytes() {
        try {
            return BodyUtils.readBody(this.serverRequest.body(), this.headers());
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read body.", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.entityLoader.isLoaded()) {
            Optional<Entity> entity = this.entityLoader.get();
            if (entity.isPresent()) {
                entity.get().close();
            }
        }
    }
}
