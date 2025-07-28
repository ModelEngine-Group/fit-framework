/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static java.nio.charset.StandardCharsets.UTF_8;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.getIfNull;

import modelengine.fel.tool.Tool;
import modelengine.fitframework.json.schema.type.OneOfType;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示 {@link Tool} 的抽象实现类。
 *
 * @since 2024-04-25
 */
public abstract class AbstractTool implements Tool {
    private final Info info;
    private final Metadata metadata;
    private final ObjectSerializer serializer;

    /**
     * 通过 Json 序列化器、工具的基本信息和工具元数据来初始化 {@link AbstractTool} 的新实例。
     *
     * @param serializer 表示 Json 序列化器的 {@link ObjectSerializer}。
     * @param itemInfo 表示工具的基本信息的 {@link Info}。
     * @param metadata 表示工具的元数据的 {@link Metadata}。
     */
    protected AbstractTool(ObjectSerializer serializer, Info itemInfo, Metadata metadata) {
        this.info = notNull(itemInfo, "The item info cannot be null.");
        this.metadata = notNull(metadata, "The tool metadata cannot be null.");
        this.serializer = notNull(serializer, "The serializer cannot be null.");
    }

    @Override
    public Info info() {
        return this.info;
    }

    @Override
    public Metadata metadata() {
        return this.metadata;
    }

    @Override
    public Object executeWithJson(String jsonArgs) {
        Map<String, Object> jsonObjectArgs = this.serializer.deserialize(jsonArgs.getBytes(UTF_8),
                UTF_8,
                TypeUtils.parameterized(Map.class, new Type[] {String.class, Object.class}));
        return this.executeWithJsonObject(jsonObjectArgs);
    }

    @Override
    public Object executeWithJsonObject(Map<String, Object> jsonObject) {
        Map<String, Object> actualArgs = getIfNull(jsonObject, HashMap::new);
        List<String> params = this.metadata().parameterOrder();
        List<Type> types = this.metadata().parameterTypes();
        Object[] args = new Object[params.size()];
        for (int i = 0; i < args.length; ++i) {
            String paramName = params.get(i);
            Object value = actualArgs.get(paramName);
            if (value == null && this.info.defaultParameterValues() != null) {
                value = this.info.defaultParameterValues().get(paramName);
            }
            if (value == null && this.metadata().requiredParameters().contains(paramName)) {
                throw new IllegalStateException(StringUtils.format("Value cannot be null. [name={0}]",
                        paramName));
            }
            args[i] = getArg(value, types.get(i));
        }
        return this.execute(args);
    }

    private static Object getArg(Object value, Type type) {
        if (type instanceof OneOfType oneOfType) {
            List<Type> types = oneOfType.types();
            for (Type actualType : types) {
                try {
                    return ObjectUtils.toCustomObject(value, actualType);
                } catch (Exception e) {
                    // 在使用 OneOf 匹配其中的一种类型时失败，需要继续匹配下一种类型。
                }
            }
            throw new IllegalStateException(StringUtils.format("No matched type. [type={0}]", type));
        }
        return ObjectUtils.toCustomObject(value, type);
    }

    @Override
    public String prettyExecute(Object... args) {
        return this.serializeResult(this.execute(args));
    }

    @Override
    public String prettyExecuteWithJson(String jsonArgs) {
        return this.serializeResult(this.executeWithJson(jsonArgs));
    }

    @Override
    public String prettyExecuteWithJsonObject(Map<String, Object> jsonObject) {
        return this.serializeResult(this.executeWithJsonObject(jsonObject));
    }

    @Override
    public String prettyExecute(Tool converter, Object... args) {
        return this.convert(converter, this.execute(args));
    }

    @Override
    public String prettyExecuteWithJson(Tool converter, String jsonArgs) {
        return this.convert(converter, this.executeWithJson(jsonArgs));
    }

    @Override
    public String prettyExecuteWithJsonObject(Tool converter, Map<String, Object> jsonObject) {
        return this.convert(converter, this.executeWithJsonObject(jsonObject));
    }

    private String convert(Tool converter, Object result) {
        return converter == null
                ? this.serializeResult(result)
                : converter.execute(result) == null ? StringUtils.EMPTY : converter.execute(result).toString();
    }

    private String serializeResult(Object result) {
        return new String(this.serializer.serialize(result, UTF_8), UTF_8);
    }
}
