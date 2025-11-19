/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolSchema;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.annotation.util.PropertyHelper;
import modelengine.fitframework.json.schema.JsonSchemaManager;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 表示基于方法构建的工具元数据。
 *
 * @author 王攀博
 * @since 2024-04-18
 */
public class MethodToolMetadata implements Tool.Metadata {
    private static final String TYPE_OBJECT = "object";

    private final Method method;

    /**
     * 通过本地方法初始化 {@link MethodToolMetadata} 的新实例。
     *
     * @param method 表示本地方法的 {@link Method}。
     */
    public MethodToolMetadata(Method method) {
        this.method = notNull(method, "The functional method cannot be null.");
    }

    @Override
    public List<Type> parameterTypes() {
        return Stream.of(this.method.getGenericParameterTypes()).collect(Collectors.toList());
    }

    @Override
    public List<String> parameterOrder() {
        return Stream.of(this.method.getParameters()).map(this::parameterName).collect(Collectors.toList());
    }

    @Override
    public Object parameterDefaultValue(String name) {
        return Stream.of(this.method.getParameters()).filter(parameter -> {
            String parameterName = this.parameterName(parameter);
            return Objects.equals(parameterName, name);
        }).findFirst().map(parameter -> {
            // TODO 需要考虑注解值的类型匹配与基础类型的默认值问题
            Property annotation = parameter.getDeclaredAnnotation(Property.class);
            if (annotation != null) {
                String defaultValue = annotation.defaultValue();
                if (defaultValue != null && PropertyHelper.isCustomValue(defaultValue)) {
                    return defaultValue;
                }
            }
            return null;
        }).orElse(null);
    }

    @Override
    public int parameterIndex(String name) {
        List<String> parameterNames = this.parameterOrder();
        for (int i = 0; i < parameterNames.size(); i++) {
            String parameterName = parameterNames.get(i);
            if (StringUtils.equals(name, parameterName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Set<String> requiredParameters() {
        return Stream.of(this.method.getParameters()).filter(parameter -> {
            if (parameter.getType().isPrimitive()) {
                return true;
            }
            Property annotation = parameter.getDeclaredAnnotation(Property.class);
            if (annotation == null) {
                return false;
            }
            return annotation.required();
        }).map(this::parameterName).collect(Collectors.toSet());
    }

    @Override
    public Map<String, Object> returnType() {
        return JsonSchemaManager.create().createSchema(this.method.getReturnType()).toJsonObject();
    }

    @Override
    public String returnConverter() {
        return StringUtils.EMPTY;
    }

    @Override
    public Optional<Method> getMethod() {
        return Optional.ofNullable(this.method);
    }

    @Override
    public Map<String, Object> schema() {
        return MapBuilder.<String, Object>get()
                .put(ToolSchema.NAME, this.definitionName())
                .put(ToolSchema.DESCRIPTION, this.description())
                .put(ToolSchema.PARAMETERS_ORDER, this.parameterOrder())
                .put(ToolSchema.PARAMETERS,
                        MapBuilder.<String, Object>get()
                                .put(ToolSchema.PROPERTIES_TYPE, TYPE_OBJECT)
                                .put(ToolSchema.PARAMETERS_PROPERTIES, this.getParamsSchema())
                                .build())
                .put(ToolSchema.RETURN_SCHEMA,
                        MapBuilder.<String, Object>get()
                                .put(ToolSchema.PROPERTIES_TYPE, this.returnType().get(ToolSchema.PROPERTIES_TYPE))
                                .build())
                .build();
    }

    private Map<String, Object> getParamsSchema() {
        MapBuilder<String, Object> paramBuilder = MapBuilder.<String, Object>get();
        List<Type> typeList = this.parameterTypes();
        List<String> ordered = this.parameterOrder();
        if (ordered.size() != typeList.size()) {
            throw new IllegalStateException(StringUtils.format(
                    "Order size not equal type size. [orderSize={0}, typeSize = {1}]",
                    ordered.size(),
                    typeList.size()));
        }
        for (int i = 0; i < ordered.size(); ++i) {
            paramBuilder.put(ordered.get(i),
                    MapBuilder.<String, Object>get()
                            .put(ToolSchema.NAME, ordered.get(i))
                            .put(ToolSchema.PROPERTIES_TYPE,
                                    JsonSchemaManager.create()
                                            .createSchema(typeList.get(i))
                                            .toJsonObject()
                                            .get(ToolSchema.PROPERTIES_TYPE))
                            .build());
        }
        return paramBuilder.build();
    }

    @Override
    public String definitionName() {
        ToolMethod toolMethodAnnotation = this.method.getAnnotation(ToolMethod.class);
        if (toolMethodAnnotation == null) {
            return this.method.getName();
        }
        return toolMethodAnnotation.name();
    }

    @Override
    public String definitionGroupName() {
        Group groupAnnotation = this.method.getDeclaringClass().getAnnotation(Group.class);
        if (groupAnnotation == null) {
            return this.method.getName();
        }
        return groupAnnotation.name();
    }

    @Override
    public String description() {
        ToolMethod toolMethodAnnotation = this.method.getAnnotation(ToolMethod.class);
        if (toolMethodAnnotation == null) {
            return this.method.getName();
        }
        return toolMethodAnnotation.description();
    }

    private String parameterName(Parameter parameter) {
        Property annotation = parameter.getDeclaredAnnotation(Property.class);
        if (annotation != null && StringUtils.isNotBlank(annotation.name())) {
            return annotation.name();
        }
        return parameter.getName();
    }
}
