/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.auth;

import modelengine.fit.http.client.proxy.Authorization;
import modelengine.fit.http.client.proxy.auth.AuthProvider;
import modelengine.fitframework.annotation.Component;

/**
 * 动态 Token 提供器示例。
 * <p>模拟从某个 Token 管理器获取动态 Token 的场景。
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
@Component
public class DynamicTokenProvider implements AuthProvider {
    @Override
    public Authorization provide() {
        // 模拟动态获取 token
        String dynamicToken = "dynamic-token-" + System.currentTimeMillis();
        return Authorization.createBearer(dynamicToken);
    }
}