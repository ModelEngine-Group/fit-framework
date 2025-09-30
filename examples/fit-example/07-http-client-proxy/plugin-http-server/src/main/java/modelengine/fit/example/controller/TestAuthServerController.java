/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.controller;

import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fitframework.annotation.Component;

/**
 * 鉴权测试服务端控制器。
 * <p>用于验证各种鉴权场景的 HTTP 请求。</p>
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
@Component
@RequestMapping(path = "/http-server/auth")
public class TestAuthServerController {
    @GetMapping(path = "/bearer-static")
    public String testBearerStatic(@RequestHeader(name = "Authorization") String authorization) {
        return "Bearer Static Auth: " + authorization;
    }

    @GetMapping(path = "/bearer-dynamic")
    public String testBearerDynamic(@RequestHeader(name = "Authorization") String authorization) {
        return "Bearer Dynamic Auth: " + authorization;
    }

    @GetMapping(path = "/basic-static")
    public String testBasicStatic(@RequestHeader(name = "Authorization") String authorization) {
        return "Basic Static Auth: " + authorization;
    }

    @GetMapping(path = "/apikey-header-static")
    public String testApiKeyHeaderStatic(@RequestHeader(name = "X-API-Key") String apiKey,
        @RequestHeader(name = "X-Service-Key", required = false) String serviceKey) {
        String result = "API Key Header Static: " + apiKey;
        if (serviceKey != null) {
            result += ", Service Key: " + serviceKey;
        }
        return result;
    }

    @GetMapping(path = "/apikey-query-static")
    public String testApiKeyQueryStatic(@RequestQuery(name = "api_key") String apiKey,
        @RequestHeader(name = "X-Service-Key", required = false) String serviceKey) {
        String result = "API Key Query Static: " + apiKey;
        if (serviceKey != null) {
            result += ", Service Key: " + serviceKey;
        }
        return result;
    }

    @GetMapping(path = "/apikey-dynamic")
    public String testApiKeyDynamic(@RequestHeader(name = "X-Dynamic-Key") String apiKey,
        @RequestHeader(name = "X-Service-Key", required = false) String serviceKey) {
        String result = "API Key Dynamic: " + apiKey;
        if (serviceKey != null) {
            result += ", Service Key: " + serviceKey;
        }
        return result;
    }

    @GetMapping(path = "/dynamic-provider")
    public String testDynamicProvider(@RequestHeader(name = "Authorization") String authorization,
        @RequestHeader(name = "X-Service-Key", required = false) String serviceKey) {
        String result = "Dynamic Provider Auth: " + authorization;
        if (serviceKey != null) {
            result += ", Service Key: " + serviceKey;
        }
        return result;
    }

    @GetMapping(path = "/custom-provider")
    public String testCustomProvider(@RequestHeader(name = "X-Timestamp") String timestamp,
        @RequestHeader(name = "X-Signature") String signature,
        @RequestHeader(name = "X-App-Id") String appId,
        @RequestHeader(name = "X-Service-Key", required = false) String serviceKey) {
        String result = String.format("Custom Provider Auth - Timestamp: %s, Signature: %s, AppId: %s",
                timestamp, signature, appId);
        if (serviceKey != null) {
            result += ", Service Key: " + serviceKey;
        }
        return result;
    }

    @GetMapping(path = "/method-override")
    public String testMethodOverride(@RequestHeader(name = "X-API-Key") String apiKey) {
        return "Method Override Auth: " + apiKey;
    }

    @GetMapping(path = "/combined-auth")
    public String testCombinedAuth(@RequestHeader(name = "Authorization") String authorization,
        @RequestHeader(name = "X-User-Context") String userContext,
        @RequestHeader(name = "X-Service-Key", required = false) String serviceKey) {
        String result = String.format("Combined Auth - Authorization: %s, UserContext: %s",
                authorization, userContext);
        if (serviceKey != null) {
            result += ", Service Key: " + serviceKey;
        }
        return result;
    }
}