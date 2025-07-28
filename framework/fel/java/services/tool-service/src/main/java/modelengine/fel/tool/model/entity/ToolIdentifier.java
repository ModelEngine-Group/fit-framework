/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.model.entity;

/**
 * 表示用于标识工具的传输层实体类。
 *
 * @since 2024-09-13
 */
public class ToolIdentifier {
    /**
     * 表示工具的唯一标识。
     * <p>构造条件时按需传入。</p>
     */
    private String uniqueName;

    /**
     * 表示工具版本。
     * <p>构造条件时按需传入。</p>
     */
    private String version;

    /**
     * 表示创建一个 {@link ToolIdentifier} 的新实例。
     *
     * @param uniqueName 表示工具的唯一标识的 {@link String}。
     * @param version 表示工具版本的 {@link String}。
     */
    public ToolIdentifier(String uniqueName, String version) {
        this.uniqueName = uniqueName;
        this.version = version;
    }

    /**
     * 获取工具的唯一标识。
     *
     * @return 表示工具的唯一标识的 {@link String}。
     */
    public String getUniqueName() {
        return this.uniqueName;
    }

    /**
     * 设置工具的唯一标识。
     *
     * @param uniqueName 表示工具的唯一标识的 {@link String}。
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    /**
     * 获取工具版本。
     *
     * @return 表示工具版本的 {@link String}。
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * 设置工具版本。
     *
     * @param version 表示工具版本的 {@link String}。
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
