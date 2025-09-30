/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.resolver;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.scanner.ParamResolver;
import modelengine.fit.http.client.proxy.support.setter.AuthDestinationSetter;
import modelengine.fit.http.client.proxy.support.setter.DestinationSetterInfo;

/**
 * 解析 {@link RequestAuth} 注解的解析器。
 * <p>负责将 {@link RequestAuth} 注解转换为可用于设置 HTTP 请求鉴权信息的 {@link DestinationSetterInfo} 对象。</p>
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
public class RequestAuthResolver implements ParamResolver<RequestAuth> {
    @Override
    public DestinationSetterInfo resolve(RequestAuth annotation, String jsonPath) {
        return new DestinationSetterInfo(new AuthDestinationSetter(annotation), jsonPath);
    }
}