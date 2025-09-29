/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.auth;

import modelengine.fit.http.client.proxy.Authorization;
import modelengine.fit.http.client.proxy.auth.AuthProvider;
import modelengine.fit.http.server.handler.Source;
import modelengine.fitframework.annotation.Component;

/**
 * API Key提供器示例。
 * 提供动态的API Key鉴权。
 *
 * @author 季聿阶
 * @since 2025-01-01
 */
@Component
public class ApiKeyProvider implements AuthProvider {

    @Override
    public Authorization provide() {
        // 模拟从配置或环境变量获取API Key
        String apiKey = "api-key-" + System.currentTimeMillis();
        return Authorization.createApiKey("X-API-Key", apiKey, Source.HEADER);
    }
}