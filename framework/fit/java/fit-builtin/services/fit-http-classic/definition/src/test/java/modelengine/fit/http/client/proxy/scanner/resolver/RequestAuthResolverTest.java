/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.resolver;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.client.proxy.support.setter.AuthDestinationSetter;
import modelengine.fit.http.client.proxy.support.setter.DestinationSetterInfo;
import modelengine.fit.http.server.handler.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RequestAuthResolver 的单元测试。
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
class RequestAuthResolverTest {
    private RequestAuthResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new RequestAuthResolver();
    }

    @Test
    void testResolveBearerAuth() {
        // 创建Bearer Token注解
        RequestAuth authAnnotation = createRequestAuth(AuthType.BEARER, "test-token", "", Source.HEADER, "", "", null);
        String jsonPath = "$.token";

        // 解析注解
        DestinationSetterInfo setterInfo = resolver.resolve(authAnnotation, jsonPath);

        // 验证结果
        assertNotNull(setterInfo);
        assertInstanceOf(AuthDestinationSetter.class, setterInfo.destinationSetter());
        assertEquals(jsonPath, setterInfo.sourcePath());
    }

    @Test
    void testResolveApiKeyAuth() {
        // 创建API Key注解
        RequestAuth authAnnotation = createRequestAuth(AuthType.API_KEY, "api-key-value", "X-API-Key",
                Source.HEADER, "", "", null);
        String jsonPath = "$.apiKey";

        // 解析注解
        DestinationSetterInfo setterInfo = resolver.resolve(authAnnotation, jsonPath);

        // 验证结果
        assertNotNull(setterInfo);
        assertInstanceOf(AuthDestinationSetter.class, setterInfo.destinationSetter());
        assertEquals(jsonPath, setterInfo.sourcePath());
    }

    @Test
    void testResolveBasicAuth() {
        // 创建Basic Auth注解
        RequestAuth authAnnotation = createRequestAuth(AuthType.BASIC, "", "", Source.HEADER,
                "admin", "password", null);
        String jsonPath = "$";

        // 解析注解
        DestinationSetterInfo setterInfo = resolver.resolve(authAnnotation, jsonPath);

        // 验证结果
        assertNotNull(setterInfo);
        assertInstanceOf(AuthDestinationSetter.class, setterInfo.destinationSetter());
        assertEquals(jsonPath, setterInfo.sourcePath());
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