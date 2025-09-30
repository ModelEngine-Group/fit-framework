/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.client;

import modelengine.fit.example.auth.ApiKeyProvider;
import modelengine.fit.example.auth.CustomSignatureProvider;
import modelengine.fit.example.auth.DynamicTokenProvider;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.HttpProxy;
import modelengine.fit.http.annotation.RequestAddress;
import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.server.handler.Source;

/**
 * 鉴权测试客户端实现。
 * <p>演示各种 @RequestAuth 注解的使用方式。
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
@HttpProxy
@RequestAddress(protocol = "http", host = "localhost", port = "8080")
@RequestMapping(path = "/http-server/auth")
/**
 * 接口级别的默认鉴权：API Key
 */
@RequestAuth(type = AuthType.API_KEY, name = "X-Service-Key", value = "service-default-key")
public interface TestAuthClient extends TestAuthInterface {
    @Override
    @GetMapping(path = "/bearer-static")
    /**
     * 方法级别覆盖：使用 Bearer Token
     */
    @RequestAuth(type = AuthType.BEARER, value = "static-bearer-token-12345")
    String testBearerStatic();

    @Override
    @GetMapping(path = "/bearer-dynamic")
    /**
     * 方法级别覆盖：使用参数驱动的 Bearer Token
     */
    String testBearerDynamic(@RequestAuth(type = AuthType.BEARER) String token);

    @Override
    @GetMapping(path = "/basic-static")
    /**
     * 方法级别覆盖：使用 Basic Auth
     */
    @RequestAuth(type = AuthType.BASIC, username = "admin", password = "secret123")
    String testBasicStatic();

    @Override
    @GetMapping(path = "/apikey-header-static")
    /**
     * 方法级别覆盖：API Key 在 Header 中
     */
    @RequestAuth(type = AuthType.API_KEY, name = "X-API-Key", value = "static-api-key-67890")
    String testApiKeyHeaderStatic();

    @Override
    @GetMapping(path = "/apikey-query-static")
    /**
     * 方法级别覆盖：API Key 在 Query 参数中
     */
    @RequestAuth(type = AuthType.API_KEY, name = "api_key", value = "query-api-key-111", location = Source.QUERY)
    String testApiKeyQueryStatic();

    @Override
    @GetMapping(path = "/apikey-dynamic")
    /**
     * 参数驱动的 API Key
     */
    String testApiKeyDynamic(@RequestAuth(type = AuthType.API_KEY, name = "X-Dynamic-Key") String apiKey);

    @Override
    @GetMapping(path = "/dynamic-provider")
    /**
     * 方法级别覆盖：使用动态 Token Provider
     */
    @RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
    String testDynamicProvider();

    @Override
    @GetMapping(path = "/custom-provider")
    /**
     * 方法级别覆盖：使用自定义签名 Provider
     */
    @RequestAuth(type = AuthType.CUSTOM, provider = CustomSignatureProvider.class)
    String testCustomProvider();

    @Override
    @GetMapping(path = "/method-override")
    /**
     * 方法级别覆盖：使用 API Key Provider
     */
    @RequestAuth(type = AuthType.API_KEY, provider = ApiKeyProvider.class)
    String testMethodOverride();

    @Override
    @GetMapping(path = "/combined-auth")
    /**
     * 组合鉴权：服务级 API Key + 用户 Token
     */
    @RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
    String testCombinedAuth(@RequestAuth(type = AuthType.API_KEY, name = "X-User-Context") String userToken);
}