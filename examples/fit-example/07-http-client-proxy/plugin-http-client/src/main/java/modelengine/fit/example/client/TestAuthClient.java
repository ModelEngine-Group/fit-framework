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
@RequestAuth(type = AuthType.API_KEY, name = "X-Service-Key", value = "service-default-key")
public interface TestAuthClient extends TestAuthInterface {
    /**
     * 方法级别覆盖：使用 Bearer Token
     */
    @Override
    @GetMapping(path = "/bearer-static")
    @RequestAuth(type = AuthType.BEARER, value = "static-bearer-token-12345")
    String testBearerStatic();

    /**
     * 方法级别覆盖：使用参数驱动的 Bearer Token
     */
    @Override
    @GetMapping(path = "/bearer-dynamic")
    String testBearerDynamic(@RequestAuth(type = AuthType.BEARER) String token);

    /**
     * 方法级别覆盖：使用 Basic Auth
     */
    @Override
    @GetMapping(path = "/basic-static")
    @RequestAuth(type = AuthType.BASIC, username = "admin", password = "secret123")
    String testBasicStatic();

    /**
     * 方法级别覆盖：API Key 在 Header 中
     */
    @Override
    @GetMapping(path = "/apikey-header-static")
    @RequestAuth(type = AuthType.API_KEY, name = "X-API-Key", value = "static-api-key-67890")
    String testApiKeyHeaderStatic();

    /**
     * 方法级别覆盖：API Key 在 Query 参数中
     */

    @Override
    @GetMapping(path = "/apikey-query-static")
    @RequestAuth(type = AuthType.API_KEY, name = "api_key", value = "query-api-key-111", location = Source.QUERY)
    String testApiKeyQueryStatic();

    /**
     * 参数驱动的 API Key
     */
    @Override
    @GetMapping(path = "/apikey-dynamic")
    String testApiKeyDynamic(@RequestAuth(type = AuthType.API_KEY, name = "X-Dynamic-Key") String apiKey);

    /**
     * 方法级别覆盖：使用动态 Token Provider
     */
    @Override
    @GetMapping(path = "/dynamic-provider")
    @RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
    String testDynamicProvider();

    /**
     * 方法级别覆盖：使用自定义签名 Provider
     */
    @Override
    @GetMapping(path = "/custom-provider")
    @RequestAuth(type = AuthType.CUSTOM, provider = CustomSignatureProvider.class)
    String testCustomProvider();

    /**
     * 方法级别覆盖：使用 API Key Provider
     */
    @Override
    @GetMapping(path = "/method-override")
    @RequestAuth(type = AuthType.API_KEY, provider = ApiKeyProvider.class)
    String testMethodOverride();

    /**
     * 组合鉴权：服务级 API Key + 用户 Token
     */
    @Override
    @GetMapping(path = "/combined-auth")
    @RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
    String testCombinedAuth(@RequestAuth(type = AuthType.API_KEY, name = "X-User-Context") String userToken);

    /**
     * 参数级别的 Basic Auth - 使用参数覆盖静态配置的 username
     * <p>演示：方法级别提供完整的 BASIC 认证（username + password），
     * 参数级别动态覆盖 username 字段（不指定 name 时默认更新 username）</p>
     */
    @Override
    @GetMapping(path = "/basic-dynamic-username")
    @RequestAuth(type = AuthType.BASIC, username = "static-user", password = "static-password")
    String testBasicDynamicUsername(@RequestAuth(type = AuthType.BASIC) String username);

    /**
     * 参数级别的 Basic Auth - 使用参数分别覆盖 username 和 password
     * <p>演示：方法级别提供完整的 BASIC 认证作为基础，
     * 参数级别使用 name 属性明确指定要覆盖的字段（username 或 password）</p>
     */
    @Override
    @GetMapping(path = "/basic-dynamic-both")
    @RequestAuth(type = AuthType.BASIC, username = "base-user", password = "base-password")
    String testBasicDynamicBoth(@RequestAuth(type = AuthType.BASIC, name = "username") String username,
            @RequestAuth(type = AuthType.BASIC, name = "password") String password);
}