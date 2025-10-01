/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.resolver;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.client.proxy.scanner.ParamResolver;
import modelengine.fit.http.client.proxy.support.setter.AuthorizationDestinationSetter;
import modelengine.fit.http.client.proxy.support.setter.DestinationSetterInfo;

/**
 * 解析 {@link RequestAuth} 注解的解析器。
 * <p>负责将 {@link RequestAuth} 注解转换为可用于设置 HTTP 请求鉴权信息的 {@link DestinationSetterInfo} 对象。</p>
 * <p>复用底层的 {@link AuthorizationDestinationSetter} 机制，确保与 FEL Tool 系统架构一致。</p>
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
public class RequestAuthResolver implements ParamResolver<RequestAuth> {
    @Override
    public DestinationSetterInfo resolve(RequestAuth annotation, String jsonPath) {
        // 根据鉴权类型确定对应的 Authorization 字段 key
        String authKey = this.getAuthorizationKey(annotation);
        return new DestinationSetterInfo(new AuthorizationDestinationSetter(authKey), jsonPath);
    }

    /**
     * 根据鉴权注解确定 Authorization 对象中对应的字段 key。
     *
     * @param annotation 鉴权注解
     * @return Authorization 对象中的字段 key
     */
    private String getAuthorizationKey(RequestAuth annotation) {
        AuthType type = annotation.type();
        switch (type) {
            case BEARER:
                // BearerAuthorization.AUTH_TOKEN = "token"
                return "token";

            case BASIC:
                // BasicAuthorization 有 username 和 password 两个字段
                // 这里返回第一个字段，实际上参数级别的 Basic Auth 比较复杂
                // 建议使用静态配置或者拆分为两个参数
                return "username";

            case API_KEY:
                // ApiKeyAuthorization 使用注解中指定的 key name
                return annotation.name();

            default:
                throw new IllegalArgumentException("Unsupported auth type for parameter-level auth: " + type);
        }
    }
}