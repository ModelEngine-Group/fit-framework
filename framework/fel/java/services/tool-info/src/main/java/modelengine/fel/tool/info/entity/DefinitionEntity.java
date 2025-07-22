/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

/**
 * 表示定义的实体类，用于存储定义信息。
 *
 * @since 2024-10-26
 */
public class DefinitionEntity {
    private SchemaEntity schema;

    /**
     * 获取 schema 对象。
     *
     * @return 表示 schema 对象的 {@link SchemaEntity}。
     */
    public SchemaEntity getSchema() {
        return this.schema;
    }

    /**
     * 设置 schema 对象。
     *
     * @param schema 表示 schema 对象的 {@link SchemaEntity}。
     */
    public void setSchema(SchemaEntity schema) {
        this.schema = schema;
    }
}
