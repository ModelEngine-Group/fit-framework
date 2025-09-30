/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.auth;

import modelengine.fit.http.client.proxy.Authorization;

/**
 * 鉴权提供器接口。
 * <p>用于动态提供鉴权信息，支持复杂的鉴权逻辑和动态 token 获取。</p>
 *
 * <p>实现类通常需要标记为 {@code @Component} 以便被框架自动发现和注入。</p>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * {@code @Component}
 * public class TokenProvider implements AuthProvider {
 *     {@code @Override}
 *     public Authorization provide() {
 *         String token = TokenManager.getCurrentToken();
 *         return Authorization.createBearer(token);
 *     }
 * }
 * </pre>
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
public interface AuthProvider {
    /**
     * 提供鉴权信息。
     * <p>此方法会在每次 HTTP 请求时被调用，用于获取最新的鉴权信息。</p>
     *
     * @return 表示鉴权信息的 {@link Authorization} 对象。
     */
    Authorization provide();
}