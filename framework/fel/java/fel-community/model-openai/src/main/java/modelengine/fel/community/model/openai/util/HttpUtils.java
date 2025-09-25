/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.util;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.community.model.openai.api.OpenAiApi;
import modelengine.fit.http.client.HttpClassicClientRequest;

/**
 * 提供 http 请求的相关工具方法。
 *
 * @author 易文渊
 * @since 2024-08-07
 */
public class HttpUtils {
    private HttpUtils() {}

    /**
     * 为 http 请求设置 Bearer 认证。
     *
     * @param request 表示 http 请求的 {@link HttpClassicClientRequest}。
     * @param apiKey 表示用于认证密钥的 {@link String}。
     * @throws IllegalArgumentException <ul>
     * <li>当 {@code request} 为 {@code null} 时；</li>
     * <li>当 {@code apiKey} 为空字符串时。</li>
     * </ul>
     */
    public static void setBearerAuth(HttpClassicClientRequest request, String apiKey) {
        notNull(request, "The request cannot be null.");
        notBlank(apiKey, "The apikey cannot be blank.");
        request.headers().set(OpenAiApi.AUTHORIZATION, "Bearer " + apiKey);
    }
}