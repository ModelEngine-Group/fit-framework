/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support.entity;

import modelengine.fitframework.annotation.Property;

/**
 * 表示地址。
 *
 * @since 2024-06-15
 */
public class Address {
    @Property(description = "表示省份", example = "jiangsu")
    private String province;
    @Property(description = "表示城市", example = "suzhou")
    private String city;

    @Property(description = "表示编号", example = "3205")
    private Integer number;

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getNumber() {
        return this.number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * 创建表示地址的 {@link Address} 实例。
     *
     * @param province 表示省份的 {@link String}。
     * @param city 表示城市的 {@link String}。
     * @param number 表示地区编码的 {@link Integer}。
     * @return 表示所创建地址实例的 {@link Address}。
     */
    public static Address create(String province, String city, Integer number) {
        Address address = new Address();
        address.setProvince(province);
        address.setCity(city);
        address.setNumber(number);
        return address;
    }
}
