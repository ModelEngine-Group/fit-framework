/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner;

import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fit.http.client.HttpClassicClientRequest;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.client.HttpClientErrorException;
import modelengine.fit.http.client.HttpClientException;
import modelengine.fit.http.client.HttpClientResponseException;
import modelengine.fit.http.client.HttpServerErrorException;
import modelengine.fit.http.client.proxy.PropertyValueApplier;
import modelengine.fit.http.client.proxy.RequestBuilder;
import modelengine.fit.http.client.proxy.scanner.entity.Address;
import modelengine.fit.http.client.proxy.scanner.entity.HttpInfo;
import modelengine.fit.http.entity.ObjectEntity;
import modelengine.fit.http.entity.TextEntity;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Handles HTTP invocations for proxy interfaces by mapping method calls to HTTP requests.
 * This class implements the {@link InvocationHandler} interface and is responsible for
 * constructing and executing HTTP requests based on the annotations and parameters of the
 * invoked methods.
 *
 * @author 王攀博
 * @since 2025-01-07
 */
public class HttpInvocationHandler implements InvocationHandler {
    private final HttpClassicClientFactory factory;
    private final Map<Method, HttpInfo> httpInfoMap;
    private final BeanContainer container;

    /**
     * Constructs an HttpInvocationHandler with the specified HTTP client factory, HTTP info map, and bean container.
     *
     * @param httpInfoMap The map of methods to their corresponding HTTP information.
     * @param container The bean container used to retrieve beans for address resolution.
     * @param factory The HTTP client factory used to create HTTP clients.
     */
    public HttpInvocationHandler(Map<Method, HttpInfo> httpInfoMap, BeanContainer container,
            HttpClassicClientFactory factory) {
        this.httpInfoMap = notNull(httpInfoMap, "The http info cannot be null.");
        this.container = notNull(container, "The bean container cannot be null.");
        this.factory = notNull(factory, "The HTTP client factory cannot be null.");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        HttpInfo httpInfo = this.httpInfoMap.get(method);
        if (httpInfo == null) {
            throw new HttpClientException("No method http info.");
        }

        // 处理 args 为 null 的情况
        Object[] actualArgs = args != null ? args : new Object[0];

        // 获取分离的应用器列表
        List<PropertyValueApplier> staticAppliers = httpInfo.getStaticAppliers();
        List<PropertyValueApplier> paramAppliers = httpInfo.getParamAppliers();

        // 检查参数数量与参数应用器数量是否匹配
        if (actualArgs.length != paramAppliers.size()) {
            throw new HttpClientException("Args length not equals to param appliers size.");
        }

        HttpClassicClient client = this.factory.create();
        RequestBuilder requestBuilder = RequestBuilder.create()
                .client(client)
                .method(httpInfo.getMethod())
                .pathPattern(httpInfo.getPathPattern());
        Address address = this.updateAddress(httpInfo.getAddress());
        if (address != null) {
            requestBuilder.protocol(address.getProtocol()).host(address.getHost()).port(address.getPort());
        }

        // 先应用静态应用器（不需要参数）
        for (PropertyValueApplier staticApplier : staticAppliers) {
            staticApplier.apply(requestBuilder, null);
        }

        // 再应用参数应用器（需要对应参数）
        for (int i = 0; i < paramAppliers.size(); i++) {
            paramAppliers.get(i).apply(requestBuilder, actualArgs[i]);
        }
        HttpClassicClientRequest request = requestBuilder.build();
        try (HttpClassicClientResponse<?> response = client.exchange(request, method.getReturnType())) {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (method.getReturnType() == String.class) {
                    return cast(response.textEntity().map(TextEntity::content).orElse(StringUtils.EMPTY));
                }
                return cast(response.objectEntity().map(ObjectEntity::object).orElse(null));
            } else if (response.statusCode() >= 400 && response.statusCode() < 500) {
                throw new HttpClientErrorException(request, response);
            } else if (response.statusCode() >= 500 && response.statusCode() < 600) {
                throw new HttpServerErrorException(request, response);
            } else {
                throw new HttpClientResponseException(request, response);
            }
        }
    }

    private Address updateAddress(Address addressIn) {
        if (addressIn == null || StringUtils.isNotEmpty(addressIn.getHost())) {
            return addressIn;
        }
        if (addressIn.getLocator() != null) {
            AddressLocator addressSource = this.container.beans().get(addressIn.getLocator());
            if (addressSource != null) {
                Address address = addressSource.address();
                addressIn.setProtocol(address.getProtocol());
                addressIn.setHost(address.getHost());
                addressIn.setPort(address.getPort());
            }
        }
        return addressIn;
    }
}