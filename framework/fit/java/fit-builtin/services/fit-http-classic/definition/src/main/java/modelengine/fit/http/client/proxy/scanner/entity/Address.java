/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.entity;

import modelengine.fit.http.client.proxy.scanner.AddressLocator;

/**
 * Represents the address information for an HTTP request.
 *
 * @author 王攀博
 * @since 2025-01-10
 */
public class Address {
    private String protocol;
    private String host;
    private int port;
    private Class<? extends AddressLocator> locator;

    /**
     * Gets the protocol for the HTTP request.
     *
     * @return The protocol for the HTTP request.
     */
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Sets the protocol for the HTTP request.
     *
     * @param protocol The protocol for the HTTP request.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the host for the HTTP request.
     *
     * @return The host for the HTTP request.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Sets the host for the HTTP request.
     *
     * @param host The host for the HTTP request.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port for the HTTP request.
     *
     * @return The port for the HTTP request.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Sets the port for the HTTP request.
     *
     * @param port The port for the HTTP request.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the class used to locate the address.
     *
     * @return The class used to locate the address.
     */
    public Class<? extends AddressLocator> getLocator() {
        return this.locator;
    }

    /**
     * Sets the class used to locate the address.
     *
     * @param locator The class used to locate the address.
     */
    public void setLocator(Class<? extends AddressLocator> locator) {
        this.locator = locator;
    }
}