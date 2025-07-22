/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support.http.server.controller;

import modelengine.fit.http.annotation.RequestCookie;
import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fitframework.annotation.Property;

/**
 * 测试 Http 提供的人的相关信息。
 *
 * @since 2024-06-17
 */
public class Person {
    @RequestHeader("name")
    @Property(description = "表示姓名")
    private String name;

    @Property(description = "表示年龄", example = "12")
    @RequestHeader("age")
    private Integer age;

    @Property(description = "表示爱好")
    @RequestCookie("hobby")
    private String hobby;

    @RequestCookie("phoneNumber")
    @Property(description = "表示电话列表")
    private String phoneNumber;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getHobby() {
        return this.hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}