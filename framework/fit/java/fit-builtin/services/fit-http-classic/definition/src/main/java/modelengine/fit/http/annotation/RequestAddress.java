/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.annotation;

import modelengine.fit.http.client.proxy.scanner.AddressLocator;
import modelengine.fitframework.annotation.Forward;
import modelengine.fitframework.util.StringUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the address information for HTTP requests.
 * This annotation is used to specify the protocol, host, port, and address locator for an HTTP request.
 * It can be applied to interfaces annotated with {@link HttpProxy} to configure the base URL and other connection
 * details.
 *
 * @author 王攀博
 * @since 2025-01-24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestAddress {
    /**
     * Specifies the class used to locate the address dynamically.
     * This property is optional and defaults to {@link AddressLocator}.
     *
     * @return The class used to locate the address.
     */
    @Forward(annotation = RequestAddress.class,
            property = "source") Class<? extends AddressLocator> value() default AddressLocator.class;

    /**
     * Specifies the protocol for the HTTP request.
     * This property is optional and defaults to an empty string.
     *
     * @return The protocol for the HTTP request.
     */
    String protocol() default StringUtils.EMPTY;

    /**
     * Specifies the host for the HTTP request.
     * This property is optional and defaults to an empty string.
     *
     * @return The host for the HTTP request.
     */
    String host() default StringUtils.EMPTY;

    /**
     * Specifies the port for the HTTP request.
     * This property is optional and defaults to an empty string.
     *
     * @return The port for the HTTP request.
     */
    String port() default StringUtils.EMPTY;

    /**
     * Specifies the class used to locate the address dynamically.
     * This property is optional and defaults to {@link AddressLocator}.
     *
     * @return The class used to locate the address.
     */
    Class<? extends AddressLocator> address() default AddressLocator.class;
}