/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support.entity;

import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fitframework.annotation.Property;

/**
 * 表示教育信息。
 *
 * @author 何天放
 * @since 2024-06-15
 */
public class Education {
    @Property(description = "表示本科学历", example = "PKU")
    @RequestHeader("bachelor")
    private String bachelor;
    @Property(description = "表示硕士学历", example = "THU")
    @RequestHeader("master")
    private String master;

    public String getBachelor() {
        return this.bachelor;
    }

    public void setBachelor(String bachelor) {
        this.bachelor = bachelor;
    }

    public String getMaster() {
        return this.master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    /**
     * 创建表示教育信息的 {@link Education} 实例。
     *
     * @param bachelor 表示学士学位的 {@link String}。
     * @param master 表示硕士学位的 {@link String}。
     * @return 表示所创建教育信息实例的 {@link Education}。
     */
    public static Education create(String bachelor, String master) {
        Education education = new Education();
        education.setBachelor(bachelor);
        education.setMaster(master);
        return education;
    }
}