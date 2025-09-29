/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.support.setter;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.RequestBuilder;
import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.server.handler.Source;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;

import static org.mockito.Mockito.*;

/**
 * AuthDestinationSetter的单元测试。
 *
 * @author 季聿阶
 * @since 2025-01-01
 */
class AuthDestinationSetterTest {

    @Test
    void testSetBearerTokenStatic() {
        // 创建Bearer Token注解
        RequestAuth authAnnotation = createRequestAuth(AuthType.BEARER, "test-bearer-token", "",
                Source.HEADER, "", "", null);

        AuthDestinationSetter setter = new AuthDestinationSetter(authAnnotation);
        RequestBuilder mockBuilder = Mockito.mock(RequestBuilder.class);

        // 执行设置（静态token，value应该为null）
        setter.set(mockBuilder, null);

        // 验证是否调用了正确的header方法
        verify(mockBuilder).header("Authorization", "Bearer test-bearer-token");
    }

    @Test
    void testSetBearerTokenDynamic() {
        // 创建Bearer Token注解（没有静态值）
        RequestAuth authAnnotation = createRequestAuth(AuthType.BEARER, "", "",
                Source.HEADER, "", "", null);

        AuthDestinationSetter setter = new AuthDestinationSetter(authAnnotation);
        RequestBuilder mockBuilder = Mockito.mock(RequestBuilder.class);

        // 执行设置（动态token）
        setter.set(mockBuilder, "dynamic-bearer-token");

        // 验证是否调用了正确的header方法
        verify(mockBuilder).header("Authorization", "Bearer dynamic-bearer-token");
    }

    @Test
    void testSetBasicAuth() {
        // 创建Basic Auth注解
        RequestAuth authAnnotation = createRequestAuth(AuthType.BASIC, "", "",
                Source.HEADER, "admin", "secret", null);

        AuthDestinationSetter setter = new AuthDestinationSetter(authAnnotation);
        RequestBuilder mockBuilder = Mockito.mock(RequestBuilder.class);

        // 执行设置
        setter.set(mockBuilder, null);

        // 验证是否调用了正确的header方法（Basic Auth的base64编码）
        verify(mockBuilder).header(eq("Authorization"), argThat(value ->
                value.toString().startsWith("Basic ")));
    }

    @Test
    void testSetApiKeyHeader() {
        // 创建API Key Header注解
        RequestAuth authAnnotation = createRequestAuth(AuthType.API_KEY, "test-api-key", "X-API-Key",
                Source.HEADER, "", "", null);

        AuthDestinationSetter setter = new AuthDestinationSetter(authAnnotation);
        RequestBuilder mockBuilder = Mockito.mock(RequestBuilder.class);

        // 执行设置
        setter.set(mockBuilder, null);

        // 验证是否调用了正确的header方法
        verify(mockBuilder).header("X-API-Key", "test-api-key");
    }

    @Test
    void testSetApiKeyQuery() {
        // 创建API Key Query注解
        RequestAuth authAnnotation = createRequestAuth(AuthType.API_KEY, "test-api-key", "api_key",
                Source.QUERY, "", "", null);

        AuthDestinationSetter setter = new AuthDestinationSetter(authAnnotation);
        RequestBuilder mockBuilder = Mockito.mock(RequestBuilder.class);

        // 执行设置
        setter.set(mockBuilder, null);

        // 验证是否调用了正确的query方法
        verify(mockBuilder).query("api_key", "test-api-key");
    }

    // 辅助方法：创建RequestAuth注解的模拟对象
    private RequestAuth createRequestAuth(AuthType type, String value, String name, Source location,
                                         String username, String password, Class<?> provider) {
        return new RequestAuth() {
            @Override
            public AuthType type() {
                return type;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public Source location() {
                return location;
            }

            @Override
            public String username() {
                return username;
            }

            @Override
            public String password() {
                return password;
            }

            @Override
            public Class provider() {
                return provider != null ? provider : modelengine.fit.http.client.proxy.auth.AuthProvider.class;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestAuth.class;
            }
        };
    }
}