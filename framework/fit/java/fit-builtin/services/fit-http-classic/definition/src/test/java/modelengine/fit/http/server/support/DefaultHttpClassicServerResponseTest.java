/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.http.server.support;

import static modelengine.fit.http.protocol.MimeType.TEXT_PLAIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fit.http.HttpResource;
import modelengine.fit.http.Serializers;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.TextEntity;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.entity.TextEventStreamEntity;
import modelengine.fit.http.protocol.ConfigurableMessageHeaders;
import modelengine.fit.http.protocol.ConfigurableStatusLine;
import modelengine.fit.http.protocol.MimeType;
import modelengine.fit.http.protocol.ServerResponse;
import modelengine.fit.http.server.InternalServerErrorException;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.ObjectUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Optional;

/**
 * 表示 {@link DefaultHttpClassicServerResponse} 的单元测试。
 *
 * @author 白鹏坤
 * @since 2023-02-15
 */
@DisplayName("测试 DefaultHttpClassicServerResponse 类")
class DefaultHttpClassicServerResponseTest {
    private final ConfigurableMessageHeaders headers = mock(ConfigurableMessageHeaders.class);
    private final Entity entity = mock(TextEntity.class);

    private DefaultHttpClassicServerResponse response;

    @BeforeEach
    void setup() {
        HttpResource httpResource = mock(HttpResource.class);
        ConfigurableStatusLine startLine = mock(ConfigurableStatusLine.class);
        ServerResponse serverResponse = mock(ServerResponse.class);
        when(serverResponse.startLine()).thenReturn(startLine);
        when(serverResponse.headers()).thenReturn(this.headers);
        when(this.entity.resolvedMimeType()).thenReturn(TEXT_PLAIN);
        this.response = new DefaultHttpClassicServerResponse(httpResource, serverResponse);
        this.response.entity(this.entity);
    }

    @Test
    @DisplayName("获取 Http 消息的消息头集合")
    void shouldReturnAttributes() {
        final ConfigurableMessageHeaders messageHeaders = this.response.headers();
        assertThat(messageHeaders).isEqualTo(this.headers);
    }

    @Test
    @DisplayName("获取 Http 消息的消息体的结构化数据")
    void shouldReturnEntity() {
        final Optional<Entity> entityOptional = this.response.entity();
        assertThat(entityOptional).isPresent().get().isEqualTo(this.entity);
    }

    @Test
    @DisplayName("获取 Http 响应的状态码")
    void shouldReturnStatusCode() {
        assertDoesNotThrow(() -> this.response.statusCode(200));
    }

    @Test
    @DisplayName("返回 Http 响应的状态信息")
    void shouldReturnReasonPhrase() {
        assertDoesNotThrow(() -> this.response.reasonPhrase("ok"));
    }

    @Test
    @DisplayName("当返回事件流遇到错误时，返回 Http 响应错误")
    void shouldThrowExceptionWhenSendTextStreamError() throws IOException {
        ObjectSerializer jsonSerializer = mock(ObjectSerializer.class);
        Serializers serializers = mock(Serializers.class);
        when(serializers.json()).thenReturn(Optional.ofNullable(jsonSerializer));
        HttpResource httpResource = mock(HttpResource.class);
        when(httpResource.serializers()).thenReturn(serializers);
        ServerResponse serverResponse = mock(ServerResponse.class);
        when(serverResponse.startLine()).thenReturn(mock(ConfigurableStatusLine.class));
        when(serverResponse.headers()).thenReturn(mock(ConfigurableMessageHeaders.class));
        doThrow(new IOException("Error")).when(serverResponse).flush();
        Choir<byte[]> mappedChoir = Choir.just(new byte[1], new byte[2]);
        Choir<TextEvent> stream = ObjectUtils.cast(mock(Choir.class));
        when(stream.map(Mockito.any())).thenReturn(ObjectUtils.cast(mappedChoir));
        TextEventStreamEntity mockedEntity = mock(TextEventStreamEntity.class);
        when(mockedEntity.resolvedMimeType()).thenReturn(MimeType.TEXT_EVENT_STREAM);
        when(mockedEntity.stream()).thenReturn(stream);
        DefaultHttpClassicServerResponse actualResponse =
                new DefaultHttpClassicServerResponse(httpResource, serverResponse);
        actualResponse.entity(mockedEntity);
        InternalServerErrorException cause =
                catchThrowableOfType(InternalServerErrorException.class, actualResponse::send);
        assertThat(cause).isNotNull().cause().isNotNull().hasMessage("Error");
    }
}
