/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format.json;

import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 表示对象输出解析器，根据 schema，输出 java 集合或者 java 对象。
 *
 * @since 2024-08-29
 */
public class ObjectJsonOutputParser extends AbstractJsonOutputParser<Object> {
    private final String jsonSchema;

    /**
     * 创建 {@link ObjectJsonOutputParser} 的实例。
     *
     * @param serializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @param jsonSchema 表示 Json 格式的描述文件的 {@link String}。
     */
    public ObjectJsonOutputParser(ObjectSerializer serializer, String jsonSchema) {
        super(serializer, Object.class);
        this.jsonSchema = jsonSchema;
    }

    @Override
    protected String jsonSchema() {
        return this.jsonSchema;
    }
}