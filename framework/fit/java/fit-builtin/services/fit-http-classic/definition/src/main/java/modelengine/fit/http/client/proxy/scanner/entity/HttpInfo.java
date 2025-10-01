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
    private List<PropertyValueApplier> staticAppliers = new ArrayList<>();
    private List<PropertyValueApplier> paramAppliers = new ArrayList<>();

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
     * Gets the list of static property value appliers (not requiring parameters).
     *
     * @return The list of static property value appliers.
     */
    public List<PropertyValueApplier> getStaticAppliers() {
        return this.staticAppliers;
    }

    /**
     * Sets the list of static property value appliers (not requiring parameters).
     *
     * @param staticAppliers The list of static property value appliers to set.
     */
    public void setStaticAppliers(List<PropertyValueApplier> staticAppliers) {
        this.staticAppliers = staticAppliers;
    }

    /**
     * Gets the list of parameter-based property value appliers.
     *
     * @return The list of parameter-based property value appliers.
     */
    public List<PropertyValueApplier> getParamAppliers() {
        return this.paramAppliers;
    }

    /**
     * Sets the list of parameter-based property value appliers.
     *
     * @param paramAppliers The list of parameter-based property value appliers to set.
     */
    public void setParamAppliers(List<PropertyValueApplier> paramAppliers) {
        this.paramAppliers = paramAppliers;
    }
}