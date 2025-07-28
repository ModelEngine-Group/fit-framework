/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.netty;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeMap;
import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fit.http.server.HttpHandler;
import modelengine.fit.http.server.netty.support.DefaultNettyServerConfig;
import modelengine.fit.server.http.HttpConfig;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.value.ValueFetcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 表示 {@link HttpClassicRequestAssembler} 的单元测试。
 *
 * @author 白鹏坤
 * @since 2023-02-23
 */
@DisplayName("测试 HttpClassicRequestAssembler 类")
class HttpClassicRequestAssemblerTest {
    private HttpClassicRequestAssembler requestAssembler;
    private ChannelHandlerContext ctx;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setup() {
        ValueFetcher valueFetcher = mock(ValueFetcher.class);
        ObjectSerializer objectSerializer = mock(ObjectSerializer.class);
        Map<String, ObjectSerializer> serializersMap =
                MapBuilder.<String, ObjectSerializer>get().put("requestId", objectSerializer).build();
        HttpHandler handler = mock(HttpHandler.class);
        when(handler.pathPattern()).thenReturn("/*");
        DefaultNettyServerConfig nettyServerConfig = new DefaultNettyServerConfig();
        nettyServerConfig.setMaxThreadNum(3);
        nettyServerConfig.setDisplayError(true);
        HttpConfig httpConfig = mock(HttpConfig.class);
        BeanContainer container = mock(BeanContainer.class);
        NettyHttpClassicServer classicServer = new NettyHttpClassicServer(container,
                serializersMap,
                valueFetcher,
                nettyServerConfig,
                httpConfig,
                true);
        classicServer.httpDispatcher().register(HttpRequestMethod.GET.name(), handler);
        this.ctx = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        when(this.ctx.channel()).thenReturn(channel);
        Attribute attribute = mock(Attribute.class);
        when(((AttributeMap) this.ctx).attr(any())).thenReturn(attribute);
        ChannelId channelId = mock(ChannelId.class);
        when(channelId.asLongText()).thenReturn("requestId");
        this.requestAssembler = new HttpClassicRequestAssembler(classicServer,
                false,
                HttpClassicRequestAssembler.Config.custom()
                        .maxThreadNum(1)
                        .shouldDisplayError(true)
                        .largeBodySize(2048)
                        .build());
    }

    @Test
    @DisplayName("当读取请求头中有状态码 100 时，响应继续等待请求的其余部分")
    void givenHttpRequestWithCode100ThenContinueReadValue() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);
        DefaultHttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/a", headers);
        assertDoesNotThrow(() -> this.requestAssembler.channelRead0(this.ctx, httpRequest));
    }
}
