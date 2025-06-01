/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.entity;

import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fitframework.annotation.Property;

/**
 * Represents educational information, including bachelor's and master's degrees.
 * This class is used to encapsulate educational details and is annotated with {@link RequestHeader}
 * to map specific fields to HTTP request headers.
 *
 * @author 季聿阶
 * @since 2025-06-01
 */
public class Education {
    /**
     * Represents the bachelor's degree information.
     * This field is mapped to the HTTP request header named "bachelor".
     */
    @Property(description = "Indicates the bachelor's degree", example = "PKU")
    @RequestHeader(name = "bachelor")
    private String bachelor;

    /**
     * Represents the master's degree information.
     * This field is mapped to the HTTP request header named "master".
     */
    @Property(description = "Indicates the master's degree", example = "THU")
    @RequestHeader(name = "master")
    private String master;

    /**
     * Gets the bachelor's degree information.
     *
     * @return The bachelor's degree.
     */
    public String getBachelor() {
        return this.bachelor;
    }

    /**
     * Sets the bachelor's degree information.
     *
     * @param bachelor The bachelor's degree to set.
     */
    public void setBachelor(String bachelor) {
        this.bachelor = bachelor;
    }

    /**
     * Gets the master's degree information.
     *
     * @return The master's degree.
     */
    public String getMaster() {
        return this.master;
    }

    /**
     * Sets the master's degree information.
     *
     * @param master The master's degree to set.
     */
    public void setMaster(String master) {
        this.master = master;
    }
}
