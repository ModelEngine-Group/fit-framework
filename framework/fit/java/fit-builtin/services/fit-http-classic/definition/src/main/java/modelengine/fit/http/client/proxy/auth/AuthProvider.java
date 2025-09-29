/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.auth;

import modelengine.fit.http.client.proxy.Authorization;

/**
 * 鉴权提供器接口。
 * 用于动态提供鉴权信息，支持复杂的鉴权逻辑和动态token获取。
 *
 * <p>实现类通常需要标记为{@code @Component}以便被框架自动发现和注入。
 *
 * <p>使用示例：
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
 * @since 2025-01-01
 */
public interface AuthProvider {

    /**
     * 提供鉴权信息。
     * 此方法会在每次HTTP请求时被调用，用于获取最新的鉴权信息。
     *
     * @return 表示鉴权信息的 {@link Authorization} 对象。
     */
    Authorization provide();
}