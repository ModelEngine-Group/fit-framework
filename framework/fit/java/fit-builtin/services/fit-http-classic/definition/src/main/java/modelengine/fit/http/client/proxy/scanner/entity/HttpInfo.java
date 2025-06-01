/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.entity;

import modelengine.fit.http.client.proxy.PropertyValueApplier;
import modelengine.fit.http.protocol.HttpRequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the HTTP request information, including address, method, path pattern, and property value appliers.
 *
 * @author 王攀博
 * @since 2025-01-10
 */
public class HttpInfo {
    private Address address;
    private HttpRequestMethod method;
    private String pathPattern;
    private List<PropertyValueApplier> appliers = new ArrayList<>();

    /**
     * Gets the address information for the HTTP request.
     *
     * @return The address information.
     */
    public Address getAddress() {
        return this.address;
    }

    /**
     * Sets the address information for the HTTP request.
     *
     * @param address The address information to set.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Gets the HTTP request method.
     *
     * @return The HTTP request method.
     */
    public HttpRequestMethod getMethod() {
        return this.method;
    }

    /**
     * Sets the HTTP request method.
     *
     * @param method The HTTP request method to set.
     */
    public void setMethod(HttpRequestMethod method) {
        this.method = method;
    }

    /**
     * Gets the path pattern for the HTTP request.
     *
     * @return The path pattern.
     */
    public String getPathPattern() {
        return this.pathPattern;
    }

    /**
     * Sets the path pattern for the HTTP request.
     *
     * @param pathPattern The path pattern to set.
     */
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * Gets the list of property value appliers for the HTTP request.
     *
     * @return The list of property value appliers.
     */
    public List<PropertyValueApplier> getAppliers() {
        return this.appliers;
    }

    /**
     * Sets the list of property value appliers for the HTTP request.
     *
     * @param appliers The list of property value appliers to set.
     */
    public void setAppliers(List<PropertyValueApplier> appliers) {
        this.appliers = appliers;
    }
}