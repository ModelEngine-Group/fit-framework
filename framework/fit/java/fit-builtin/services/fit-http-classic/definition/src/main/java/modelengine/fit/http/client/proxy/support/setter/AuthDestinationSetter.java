/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.support.setter;

import modelengine.fit.http.annotation.RequestAuth;
import modelengine.fit.http.client.proxy.Authorization;
import modelengine.fit.http.client.proxy.DestinationSetter;
import modelengine.fit.http.client.proxy.RequestBuilder;
import modelengine.fit.http.client.proxy.auth.AuthProvider;
import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.server.handler.Source;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.util.StringUtils;

/**
 * 表示向 HTTP 请求设置鉴权信息的 {@link DestinationSetter}。
 * <p>支持多种鉴权类型和动态 Provider。</p>
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
public class AuthDestinationSetter implements DestinationSetter {
    private final RequestAuth authAnnotation;
    private final BeanContainer beanContainer;

    /**
     * 使用指定的鉴权注解初始化 {@link AuthDestinationSetter} 的新实例。
     *
     * @param authAnnotation 表示鉴权注解的 {@link RequestAuth}。
     */
    public AuthDestinationSetter(RequestAuth authAnnotation) {
        this(authAnnotation, null);
    }

    /**
     * 使用指定的鉴权注解和 Bean 容器初始化 {@link AuthDestinationSetter} 的新实例。
     *
     * @param authAnnotation 表示鉴权注解的 {@link RequestAuth}。
     * @param beanContainer 表示 Bean 容器的 {@link BeanContainer}。
     */
    public AuthDestinationSetter(RequestAuth authAnnotation, BeanContainer beanContainer) {
        this.authAnnotation = authAnnotation;
        this.beanContainer = beanContainer;
    }

    @Override
    public void set(RequestBuilder requestBuilder, Object value) {
        Authorization authorization = createAuthorization(value);
        if (authorization != null) {
            authorization.assemble(requestBuilder);
        }
    }

    private Authorization createAuthorization(Object value) {
        // 如果指定了 Provider，优先使用 Provider
        if (authAnnotation.provider() != AuthProvider.class) {
            if (beanContainer != null) {
                AuthProvider provider = beanContainer.beans().get(authAnnotation.provider());
                if (provider != null) {
                    return provider.provide();
                } else {
                    throw new IllegalStateException("AuthProvider " + authAnnotation.provider().getName() + " not found in container");
                }
            } else {
                // TODO: MVP 版本暂时不支持 Provider，后续版本再实现
                throw new UnsupportedOperationException("AuthProvider support is not implemented in this version");
            }
        }

        // 基于注解类型创建 Authorization
        AuthType type = authAnnotation.type();
        switch (type) {
            case BEARER:
                String token = getBearerToken(value);
                if (StringUtils.isNotEmpty(token)) {
                    return Authorization.createBearer(token);
                }
                break;
            case BASIC:
                String username = getBasicUsername();
                String password = getBasicPassword();
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    return Authorization.createBasic(username, password);
                }
                break;
            case API_KEY:
                String keyName = getApiKeyName();
                String keyValue = getApiKeyValue(value);
                Source location = authAnnotation.location();
                if (StringUtils.isNotEmpty(keyName) && StringUtils.isNotEmpty(keyValue)) {
                    return Authorization.createApiKey(keyName, keyValue, location);
                }
                break;
            case CUSTOM:
                // CUSTOM 类型必须使用 Provider
                throw new IllegalArgumentException("CUSTOM auth type requires a provider");
        }

        return null;
    }

    private String getBearerToken(Object value) {
        // 如果是参数驱动，使用参数值
        if (value instanceof String) {
            return (String) value;
        }
        // 否则使用注解中的静态值
        return StringUtils.isNotEmpty(authAnnotation.value()) ? authAnnotation.value() : null;
    }

    private String getBasicUsername() {
        return StringUtils.isNotEmpty(authAnnotation.username()) ? authAnnotation.username() : null;
    }

    private String getBasicPassword() {
        return StringUtils.isNotEmpty(authAnnotation.password()) ? authAnnotation.password() : null;
    }

    private String getApiKeyName() {
        return StringUtils.isNotEmpty(authAnnotation.name()) ? authAnnotation.name() : null;
    }

    private String getApiKeyValue(Object value) {
        // 如果是参数驱动，使用参数值
        if (value instanceof String) {
            return (String) value;
        }
        // 否则使用注解中的静态值
        return StringUtils.isNotEmpty(authAnnotation.value()) ? authAnnotation.value() : null;
    }
}