/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.resolver;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.scanner.ParamResolver;
import modelengine.fit.http.client.proxy.support.setter.AuthorizationDestinationSetter;
import modelengine.fit.http.client.proxy.support.setter.DestinationSetterInfo;

/**
 * 解析 {@link RequestAuth} 注解的解析器。
 * <p>负责将 {@link RequestAuth} 注解转换为可用于设置 HTTP 请求鉴权信息的 {@link DestinationSetterInfo} 对象。</p>
 * <p>复用底层的 {@link AuthorizationDestinationSetter} 机制，确保与 FEL Tool 系统架构一致。</p>
 *
 * <p><b>工作原理</b></p>
 * <p>参数级别的鉴权通过 {@link AuthorizationDestinationSetter} 动态更新已存在的 Authorization 对象。
 * 使用 {@link AuthFieldMapper} 确定应该更新 Authorization 对象的哪个字段。</p>
 *
 * <p><b>使用示例</b></p>
 * <pre>{@code
 * // Bearer Token
 * String api(@RequestAuth(type = BEARER) String token);
 * // → 更新 BearerAuthorization.token 字段
 *
 * // API Key
 * String search(@RequestAuth(type = API_KEY, name = "X-API-Key") String apiKey);
 * // → 更新 ApiKeyAuthorization.value 字段
 * // → ApiKeyAuthorization.key 从注解的 name 属性获取
 * }</pre>
 *
 * @author 季聿阶
 * @since 2025-09-30
 * @see AuthFieldMapper
 */
public class RequestAuthResolver implements ParamResolver<RequestAuth> {
    @Override
    public DestinationSetterInfo resolve(RequestAuth annotation, String jsonPath) {
        // 使用 AuthFieldMapper 获取应该更新的 Authorization 字段名
        // 传入 name 属性以支持 BASIC 类型的字段选择（username 或 password）
        String authField = AuthFieldMapper.getParameterAuthField(annotation.type(), annotation.name());
        return new DestinationSetterInfo(new AuthorizationDestinationSetter(authField), jsonPath);
    }
}