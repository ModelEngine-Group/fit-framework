/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format.json;

import static modelengine.fitframework.util.ObjectUtils.getIfNull;

import modelengine.fitframework.json.schema.JsonSchemaManager;
import modelengine.fitframework.json.schema.support.DefaultJsonSchemaManager;
import modelengine.fitframework.serialization.ObjectSerializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 表示 java bean 输出解析器。
 *
 * @param <O> 表示输出对象类型。
 * @author 易文渊
 * @since 2024-04-28
 */
public class BeanJsonOutputParser<O> extends AbstractJsonOutputParser<O> {
    private final String jsonSchema;

    /**
     * 创建 {@link BeanJsonOutputParser} 的实例。
     *
     * @param serializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @param type 表示输出类型 {@link O} 的 {@link Type}。
     * @param schemaManager 表示 jsonSchema 管理器的 {@link JsonSchemaManager}。
     */
    public BeanJsonOutputParser(ObjectSerializer serializer, Type type, JsonSchemaManager schemaManager) {
        super(serializer, type);
        JsonSchemaManager usedSchemaManager = getIfNull(schemaManager, DefaultJsonSchemaManager::new);
        this.jsonSchema = new String(serializer.serialize(usedSchemaManager.createSchema(type).toJsonObject(),
                StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    @Override
    protected String jsonSchema() {
        return this.jsonSchema;
    }
}