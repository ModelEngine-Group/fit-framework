/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fit.http.HttpResource;
import modelengine.fit.http.client.support.AbstractHttpClassicClient;
import modelengine.fit.http.client.support.DefaultHttpClassicClientRequest;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 为 {@link HttpClassicClient} 提供单元测试。
 *
 * @author 杭潇
 * @since 2023-02-16
 */
@DisplayName("测试 HttpClassicClient 接口")
public class HttpClassicClientTest {
    @Nested
    @DisplayName("测试 exchangeForEntity() 方法")
    class TestExchangeForEntity {
        private final HttpClassicClient httpClassicClient = new HttpClassicClientImplement();
        private HttpClassicClientRequest httpClassicClientRequest;
        private final String reasonPhrase = "testHttpClassicClient";
        private final MultiValueMap<String, String> headers = new DefaultMultiValueMap<>();
        private final InputStream responseStream =
                new ByteArrayInputStream("TestOfHttpClassicClient".getBytes(StandardCharsets.UTF_8));
        private final ClientRequest clientRequest = mock(ClientRequest.class);
        private final HttpResource httpResource = mock(AbstractHttpClassicClient.class);

        @BeforeEach
        void setup() {
            this.headers.add("Content-Length", "23");
            ConfigurableMessageHeaders defaultMessageHeaders = new DefaultMessageHeaders();
            when(this.clientRequest.headers()).thenReturn(defaultMessageHeaders);
            RequestLine startLine = new DefaultRequestLine(HttpVersion.HTTP_1_0,
                    HttpRequestMethod.CONNECT,
                    "requestUri",
                    QueryCollection.create());
            when(this.clientRequest.startLine()).thenReturn(startLine);
        }

        @AfterEach
        void teardown() throws IOException {
            this.responseStream.close();
        }

        @Nested
        @DisplayName("给定状态码值以 2 开头")
        class StatusCodeStartWithTwo {
            @BeforeEach
            void setup() throws IOException {
                ClientResponse clientResponse = new DefaultClientResponse(200,
                        TestExchangeForEntity.this.reasonPhrase,
                        TestExchangeForEntity.this.headers,
                        TestExchangeForEntity.this.responseStream);
                when(TestExchangeForEntity.this.clientRequest.readResponse()).thenReturn(clientResponse);
                TestExchangeForEntity.this.httpClassicClientRequest = new DefaultHttpClassicClientRequest(
                        TestExchangeForEntity.this.httpResource,
                        TestExchangeForEntity.this.clientRequest);
            }

            @Test
            @DisplayName("给定期待返回值类型为 String，返回值与给定值相等")
            void givenResponseTypeStringThenReturnShouldBeEqualsToTheGivenValue() {
                String exchangeForEntity = TestExchangeForEntity.this.httpClassicClient.exchangeForEntity(
                        TestExchangeForEntity.this.httpClassicClientRequest,
                        String.class);
                assertThat(exchangeForEntity).isEqualTo("finishTextEntity");
            }

            @Test
            @DisplayName("给定期待返回值类型不为 String，返回值与给定值相等")
            void givenResponseTypeNotStringThenReturnShouldBeEqualsToTheGivenValue() {
                Integer exchangeForEntity = TestExchangeForEntity.this.httpClassicClient.exchangeForEntity(
                        TestExchangeForEntity.this.httpClassicClientRequest,
                        Integer.class);
                assertThat(exchangeForEntity).isEqualTo(24);
            }
        }

        @Test
        @DisplayName("给定状态码值以 4 开头，抛出异常")
        void givenStatusCodeStartWithFourThenThrowException() throws IOException {
            ClientResponse clientResponse =
                    new DefaultClientResponse(401, this.reasonPhrase, this.headers, this.responseStream);
            when(this.clientRequest.readResponse()).thenReturn(clientResponse);
            this.httpClassicClientRequest = new DefaultHttpClassicClientRequest(this.httpResource, this.clientRequest);
            HttpClientErrorException httpClientErrorException = catchThrowableOfType(HttpClientErrorException.class,
                    () -> this.httpClassicClient.exchangeForEntity(this.httpClassicClientRequest, Integer.class));
            assertThat(httpClientErrorException).isNotNull();
        }

        @Test
        @DisplayName("给定状态码值以 5 开头，抛出异常")
        void givenStatusCodeStartWithFiveThenThrowException() throws IOException {
            ClientResponse clientResponse =
                    new DefaultClientResponse(501, this.reasonPhrase, this.headers, this.responseStream);
            when(this.clientRequest.readResponse()).thenReturn(clientResponse);
            this.httpClassicClientRequest = new DefaultHttpClassicClientRequest(this.httpResource, this.clientRequest);
            HttpServerErrorException httpServerErrorException = catchThrowableOfType(HttpServerErrorException.class,
                    () -> this.httpClassicClient.exchangeForEntity(this.httpClassicClientRequest, Integer.class));
            assertThat(httpServerErrorException).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(ints = {101, 102, 603, 705})
        @DisplayName("给定状态码值以其他数字开头，抛出异常")
        void givenStatusCodeStartWithOtherNumberThenThrowException(int statusCode) throws IOException {
            ClientResponse clientResponse =
                    new DefaultClientResponse(statusCode, this.reasonPhrase, this.headers, this.responseStream);
            when(this.clientRequest.readResponse()).thenReturn(clientResponse);
            this.httpClassicClientRequest = new DefaultHttpClassicClientRequest(this.httpResource, this.clientRequest);
            HttpClassicClientFactory.Config.builder().build();
            HttpClientResponseException httpClientResponseException =
                    catchThrowableOfType(HttpClientResponseException.class,
                            () -> this.httpClassicClient.exchangeForEntity(this.httpClassicClientRequest,
                                    Integer.class));
            assertThat(httpClientResponseException).isNotNull();
        }
    }
}
