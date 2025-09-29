/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.auth;

/**
 * 表示HTTP请求的鉴权类型枚举。
 * 定义了框架支持的各种鉴权方式。
 *
 * @author 季聿阶
 * @since 2025-01-01
 */
public enum AuthType {
    /**
     * Bearer Token鉴权。
     * 通常用于JWT Token等场景，会在Authorization头中添加"Bearer {token}"。
     */
    BEARER,

    /**
     * Basic鉴权。
     * 使用用户名和密码进行基础认证，会在Authorization头中添加"Basic {base64(username:password)}"。
     */
    BASIC,

    /**
     * API Key鉴权。
     * 使用API密钥进行认证，可以放在Header、Query参数或Cookie中。
     */
    API_KEY,

    /**
     * 自定义鉴权。
     * 通过AuthProvider提供自定义的鉴权逻辑，支持复杂的鉴权场景。
     */
    CUSTOM
}