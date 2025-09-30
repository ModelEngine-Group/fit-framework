/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.support.applier;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.PropertyValueApplier;
import modelengine.fit.http.client.proxy.RequestBuilder;
import modelengine.fit.http.client.proxy.support.setter.AuthDestinationSetter;

/**
 * 静态鉴权信息应用器。
 * <p>用于处理类级别和方法级别的 @RequestAuth 注解，将静态鉴权信息应用到 HTTP 请求中。</p>
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
public class StaticAuthApplier implements PropertyValueApplier {
    private final AuthDestinationSetter authSetter;

    /**
     * 使用指定的鉴权注解初始化 {@link StaticAuthApplier} 的新实例。
     *
     * @param authAnnotation 表示鉴权注解的 {@link RequestAuth}。
     */
    public StaticAuthApplier(RequestAuth authAnnotation) {
        this.authSetter = new AuthDestinationSetter(authAnnotation);
    }

    @Override
    public void apply(RequestBuilder requestBuilder, Object value) {
        // 静态鉴权不需要参数值，传入 null 即可
        authSetter.set(requestBuilder, null);
    }
}