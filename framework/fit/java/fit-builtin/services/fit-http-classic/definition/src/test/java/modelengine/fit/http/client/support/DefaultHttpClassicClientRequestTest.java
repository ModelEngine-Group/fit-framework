/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fit.http.HttpMessage;
import modelengine.fit.http.HttpResource;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.FileEntity;
import modelengine.fit.http.entity.support.DefaultObjectEntity;
import modelengine.fit.http.protocol.ClientRequest;
import modelengine.fit.http.protocol.ClientResponse;
import modelengine.fit.http.protocol.ConfigurableMessageHeaders;
import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fit.http.protocol.HttpVersion;
import modelengine.fit.http.protocol.QueryCollection;
import modelengine.fit.http.protocol.RequestLine;
import modelengine.fit.http.protocol.support.DefaultClientResponse;
import modelengine.fit.http.protocol.support.DefaultMessageHeaders;
import modelengine.fit.http.protocol.support.DefaultRequestLine;
import modelengine.fitframework.model.MultiValueMap;
import modelengine.fitframework.model.support.DefaultMultiValueMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 为 {@link DefaultHttpClassicClientRequest} 提供单元测试。
 *
 * @author 杭潇
 * @since 2023-02-16
 */
@DisplayName("测试 DefaultHttpClassicClientRequest 接口")
public class DefaultHttpClassicClientRequestTest {
    private final HttpResource httpResource = mock(AbstractHttpClassicClient.class);
    private final ClientRequest clientRequest = mock(ClientRequest.class);
    private DefaultHttpClassicClientRequest defaultHttpClassicClientRequest;
    private final MultiValueMap<String, String> headers = new DefaultMultiValueMap<>();
    private final InputStream responseStream =
            new ByteArrayInputStream("TestOfHttpClientErrorException".getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setup() throws IOException {
        this.headers.add("Content-Length", "30");
        ConfigurableMessageHeaders defaultMessageHeaders = new DefaultMessageHeaders();
        when(this.clientRequest.headers()).thenReturn(defaultMessageHeaders);
        RequestLine startLine = new DefaultRequestLine(HttpVersion.HTTP_1_0,
                HttpRequestMethod.CONNECT,
                "requestUri",
                QueryCollection.create());
        when(this.clientRequest.startLine()).thenReturn(startLine);
        String reasonPhrase = "testHttpClientErrorException";
        ClientResponse clientResponse = new DefaultClientResponse(200, reasonPhrase, this.headers, this.responseStream);
        when(this.clientRequest.readResponse()).thenReturn(clientResponse);
        this.defaultHttpClassicClientRequest =
                new DefaultHttpClassicClientRequest(this.httpResource, this.clientRequest);
    }

    @AfterEach
    void teardown() throws IOException {
        this.responseStream.close();
    }

    @Test
    @DisplayName("获取的消息头集合与给定值相等")
    void theHeadersShouldBeEqualsToTheGivenValue() {
        ConfigurableMessageHeaders actualHeaders = this.defaultHttpClassicClientRequest.headers();
        assertThat(actualHeaders).isEqualTo(this.clientRequest.headers());
    }

    @Test
    @DisplayName("获取的实体初始值为空")
    void theEntityStartValueIsEmpty() {
        Optional<Entity> entity = this.defaultHttpClassicClientRequest.entity();
        assertThat(entity).isEmpty();
    }

    @Nested
    @DisplayName("客户端请求已提交")
    class CommitEdTheClientRequest {
        @BeforeEach
        void setup() {
            DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.commit();
        }

        @Test
        @DisplayName("修改实体值，直接返回，修改失败")
        void invokeEntityMethodThenReturnWithoutEntityUpdate() {
            HttpMessage httpMessage = mock(HttpMessage.class);
            Entity entity = FileEntity.create(httpMessage,
                    "entityFileName",
                    new ByteArrayInputStream(new byte[0]),
                    0,
                    FileEntity.Position.INLINE,
                    null);
            DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity(entity);
            assertThat(DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity()).isEmpty();
        }

        @Test
        @DisplayName("调用 json 实体方法，直接返回，修改实体值失败")
        void invokeJsonEntityThenReturnWithoutEntityUpdate() {
            Object jsonObject = 12;
            DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.jsonEntity(jsonObject);
            assertThat(DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity()).isEmpty();
        }

        @Test
        @DisplayName("调用 form 实体方法，直接返回，修改实体值失败")
        void invokeFormEntityThenReturnWithoutEntityUpdate() {
            MultiValueMap<String, String> form = new DefaultMultiValueMap<>();
            form.add("testKey", "testValue");
            DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.formEntity(form);
            assertThat(DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity()).isEmpty();
        }
    }

    @Nested
    @DisplayName("测试 entity(Entity) 方法")
    class TestEntity {
        @Test
        @DisplayName("给定值为 null 的实体值，直接返回，不会修改实体值")
        void givenEntityIsNullThenReturnWithOutEntityUpdate() {
            DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity(null);
            assertThat(DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity()).isEmpty();
        }

        @Test
        @DisplayName("给定非空的实体值，修改实体值成功")
        void givenEntityNotNullThenUpdateEntitySuccessfully() {
            HttpMessage httpMessage = mock(HttpMessage.class);
            Entity entity = new DefaultObjectEntity<>(httpMessage, 12);
            DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity(entity);
            assertThat(DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.entity()).isPresent()
                    .get()
                    .isEqualTo(entity);
        }
    }

    @Test
    @DisplayName("调用 json 实体方法，修改实体值成功")
    void invokeJsonEntityThenThenUpdateEntitySuccessfully() {
        Object jsonObject = 12;
        this.defaultHttpClassicClientRequest.jsonEntity(jsonObject);
        assertThat(this.defaultHttpClassicClientRequest.entity()).isPresent();
    }

    @Test
    @DisplayName("调用 form 实体方法，修改实体值成功")
    void invokeFormEntityThenThenUpdateEntitySuccessfully() {
        MultiValueMap<String, String> form = new DefaultMultiValueMap<>();
        form.add("testKey", "testValue");
        this.defaultHttpClassicClientRequest.formEntity(form);
        assertThat(this.defaultHttpClassicClientRequest.entity()).isPresent();
    }

    @Test
    @DisplayName("调用交换方法，返回参数值与给定值相等")
    void invokeExchangeMethodThenReturnParameterIsEqualsToTheGivenParameter() {
        HttpClassicClientResponse<Object> exchange = this.defaultHttpClassicClientRequest.exchange();
        assertThat(exchange.contentLength()).isEqualTo(30);
    }

    @Nested
    @DisplayName("测试 exchange(Class<T>) 方法")
    class TestExchange {
        @Test
        @DisplayName("给定有效的类型，构建默认 Http 经典客户端相应实例成功")
        void givenValidClassThenBuildNewObjectSuccessfully() {
            HttpClassicClientResponse<String> exchange =
                    DefaultHttpClassicClientRequestTest.this.defaultHttpClassicClientRequest.exchange(String.class);
            assertThat(exchange).isExactlyInstanceOf(DefaultHttpClassicClientResponse.class);
        }
    }

    @Test
    @DisplayName("获取的方法值与给定的值相等")
    void theMethodShouldBeEqualsToTheGivenMethod() {
        HttpRequestMethod method = this.defaultHttpClassicClientRequest.method();
        assertThat(method.name()).isEqualTo("CONNECT");
    }

    @Test
    @DisplayName("请求 Uri 与给定的值相等")
    void theRequestUriIsEqualsToTheGivenValue() {
        String requestUri = this.defaultHttpClassicClientRequest.requestUri();
        assertThat(requestUri).isEqualTo("requestUri");
    }

    @Test
    @DisplayName("主机值为 null")
    void theHostValueShouldBeNull() {
        String host = this.defaultHttpClassicClientRequest.host();
        assertThat(host).isNull();
    }

    @Test
    @DisplayName("获取的路径值与给定的值相等")
    void thePathIsEqualsToTheGivenValue() {
        String path = this.defaultHttpClassicClientRequest.path();
        assertThat(path).isEqualTo("requestUri");
    }

    @Test
    @DisplayName("获取 Http 请求的查询参数集合为空")
    void theQueriesIsEmpty() {
        QueryCollection queries = this.defaultHttpClassicClientRequest.queries();
        assertThat(queries.queryString()).isEmpty();
    }

    @Test
    @DisplayName("获取的 Http 版本与给定的值相等")
    void theHttpVersionIsEqualsToTheGivenVersion() {
        HttpVersion httpVersion = this.defaultHttpClassicClientRequest.httpVersion();
        assertThat(httpVersion).isEqualTo(HttpVersion.HTTP_1_0);
    }

    @Test
    @DisplayName("获取的 Http 消息的传输编码方式是否为 chunked，返回值为 false")
    void checkTheEncodingModeIsChunkedIsFalse() {
        boolean isChunked = this.defaultHttpClassicClientRequest.isChunked();
        assertThat(isChunked).isFalse();
    }
}
