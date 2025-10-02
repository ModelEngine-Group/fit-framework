/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.support.applier;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.Authorization;
import modelengine.fit.http.client.proxy.PropertyValueApplier;
import modelengine.fit.http.client.proxy.RequestBuilder;
import modelengine.fit.http.client.proxy.auth.AuthProvider;
import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.util.StringUtils;

/**
 * 静态鉴权信息应用器。
 * <p>用于处理类级别和方法级别的 @RequestAuth 注解，将静态鉴权信息应用到 HTTP 请求中。</p>
 * <p>复用底层的 {@link Authorization} 机制，确保架构一致性。</p>
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
public class StaticAuthApplier implements PropertyValueApplier {
    private final Authorization authorization;

    /**
     * 使用指定的鉴权注解和 BeanContainer 初始化 {@link StaticAuthApplier} 的新实例。
     *
     * @param authAnnotation 表示鉴权注解的 {@link RequestAuth}。
     * @param beanContainer 表示 Bean 容器，用于获取 AuthProvider。
     */
    public StaticAuthApplier(RequestAuth authAnnotation, BeanContainer beanContainer) {
        notNull(beanContainer, "The bean container cannot be null.");
        this.authorization = this.createAuthorizationFromAnnotation(authAnnotation, beanContainer);
    }

    @Override
    public void apply(RequestBuilder requestBuilder, Object value) {
        // 静态鉴权不需要参数值，直接将 Authorization 对象设置到 RequestBuilder
        requestBuilder.authorization(this.authorization);
    }

    private Authorization createAuthorizationFromAnnotation(RequestAuth annotation, BeanContainer beanContainer) {
        // 如果指定了 Provider，需要 BeanContainer
        if (annotation.provider() != AuthProvider.class) {
            AuthProvider provider = beanContainer.beans().get(annotation.provider());
            if (provider == null) {
                throw new IllegalStateException(
                        "AuthProvider not found in BeanContainer: " + annotation.provider().getName());
            }
            return provider.provide();
        }

        // 基于注解类型创建 Authorization
        AuthType type = annotation.type();
        switch (type) {
            case BEARER:
                String token = annotation.value();
                if (StringUtils.isEmpty(token)) {
                    throw new IllegalArgumentException("Bearer token cannot be empty for static auth");
                }
                return Authorization.createBearer(token);

            case BASIC:
                String username = annotation.username();
                String password = annotation.password();
                if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
                    throw new IllegalArgumentException("Username and password cannot be empty for Basic auth");
                }
                return Authorization.createBasic(username, password);

            case API_KEY:
                String keyName = annotation.name();
                String keyValue = annotation.value();
                if (StringUtils.isEmpty(keyName) || StringUtils.isEmpty(keyValue)) {
                    throw new IllegalArgumentException("API Key name and value cannot be empty for static auth");
                }
                return Authorization.createApiKey(keyName, keyValue, annotation.location());

            case CUSTOM:
                throw new IllegalArgumentException("CUSTOM auth type requires a provider");

            default:
                throw new IllegalArgumentException("Unsupported auth type: " + type);
        }
    }

}