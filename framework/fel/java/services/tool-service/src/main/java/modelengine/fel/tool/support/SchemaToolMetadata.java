/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;
import static modelengine.fitframework.util.ObjectUtils.getIfNull;
import static modelengine.fitframework.util.ObjectUtils.nullIf;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolSchema;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.json.schema.type.OneOfType;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表示基于摘要信息构建的工具元数据。
 *
 * @since 2024-04-18
 */
public class SchemaToolMetadata implements Tool.Metadata {
    private static final Map<String, Type> JSON_SCHEMA_TYPE_TO_JAVA_TYPE = MapBuilder.<String, Type>get()
            .put("string", String.class)
            .put("integer", BigInteger.class)
            .put("number", BigDecimal.class)
            .put("boolean", Boolean.class)
            .put("object", Map.class)
            .put("array", List.class)
            .build();
    private static final String ONE_OF = "oneOf";

    private final Map<String, Object> toolSchema;
    private final String definitionGroupName;
    private final String definitionName;
    private final String description;
    private final Map<String, Object> properties;
    private final List<String> parametersOrder;
    private final List<Type> parametersType;
    private final Set<String> requiredParameters;
    private final Map<String, Object> parametersDefaultValue;
    private final Map<String, Object> returnSchema;
    private final String returnConverter;

    /**
     * 通过工具的格式规范初始化 {@link SchemaToolMetadata} 的新实例。
     *
     * @param definitionGroupName 表示定义组名称 {@link String}。
     * @param toolSchema 表示工具格式规范的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public SchemaToolMetadata(String definitionGroupName, Map<String, Object> toolSchema) {
        this.definitionGroupName = notNull(definitionGroupName, "Definition group name cannot be null.");
        this.toolSchema = notNull(toolSchema, "The tool schema cannot be null.");
        this.definitionName = notNull(cast(toolSchema.get(ToolSchema.NAME)), "Definition name cannot be null.");
        this.description = cast(toolSchema.get(ToolSchema.DESCRIPTION));
        Map<String, Object> parametersSchema =
                notNull(cast(toolSchema.get(ToolSchema.PARAMETERS)), "The parameters json schema cannot be null.");
        Map<String, Object> properties = cast(parametersSchema.get(ToolSchema.PARAMETERS_PROPERTIES));
        properties = ObjectUtils.getIfNull(properties, HashMap::new);
        // 合并额外参数
        if (toolSchema.get(ToolSchema.EXTRA_PARAMETERS) != null) {
            Map<String, Object> extraParameters = cast(toolSchema.get(ToolSchema.EXTRA_PARAMETERS));
            properties = MapUtils.merge(properties, cast(extraParameters.get(ToolSchema.PARAMETERS_PROPERTIES)));
        }
        this.properties = properties;
        this.parametersOrder = getIfNull(ObjectUtils.<List<String>>cast(toolSchema.get(ToolSchema.PARAMETERS_ORDER)),
                () -> new ArrayList<>(this.properties.keySet()));
        this.parametersType = this.extractParametersType(this.properties);
        this.requiredParameters = this.extractRequiredParameters(parametersSchema);
        this.parametersDefaultValue = this.extractParametersDefaultValue(this.properties);
        this.returnSchema = getIfNull(cast(toolSchema.get(ToolSchema.RETURN_SCHEMA)), Collections::emptyMap);
        this.returnConverter = nullIf(cast(this.returnSchema.get(ToolSchema.RETURN_CONVERTER)), StringUtils.EMPTY);
    }

    private static Type convertJsonSchemaTypeToJavaType(String schemaType) {
        Type javaType = JSON_SCHEMA_TYPE_TO_JAVA_TYPE.get(schemaType);
        return notNull(javaType,
                () -> new IllegalStateException(StringUtils.format("Unsupported json schema type. [type={0}]",
                        schemaType)));
    }

    @Override
    public List<Type> parameterTypes() {
        return Collections.unmodifiableList(this.parametersType);
    }

    @Override
    public List<String> parameterOrder() {
        return Collections.unmodifiableList(this.parametersOrder);
    }

    @Override
    public Object parameterDefaultValue(String name) {
        return this.parametersDefaultValue.get(name);
    }

    @Override
    public int parameterIndex(String name) {
        List<String> parameterNames = this.parameterOrder();
        for (int i = 0; i < parameterNames.size(); i++) {
            if (StringUtils.equalsIgnoreCase(name, parameterNames.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Set<String> requiredParameters() {
        return Collections.unmodifiableSet(this.requiredParameters);
    }

    @Override
    public Map<String, Object> returnType() {
        return Collections.unmodifiableMap(this.returnSchema);
    }

    @Override
    public String returnConverter() {
        return this.returnConverter;
    }

    @Override
    public Optional<Method> getMethod() {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> schema() {
        return this.toolSchema;
    }

    @Override
    public String definitionName() {
        return this.definitionName;
    }

    @Override
    public String definitionGroupName() {
        return this.definitionGroupName;
    }

    @Override
    public String description() {
        return this.description;
    }

    private List<Type> extractParametersType(Map<String, Object> properties) {
        int propertiesSize = properties.size();
        List<String> parameterNames = this.parameterOrder();
        Validation.isTrue(propertiesSize == parameterNames.size(),
                "The size of properties must equals to the parameter names. "
                        + "[propertiesSize={0}, parameterNamesSize={1}]",
                propertiesSize,
                parameterNames.size());
        List<Type> types = new ArrayList<>();
        for (String parameterName : parameterNames) {
            Map<String, Object> property = cast(properties.get(parameterName));
            notNull(property, "No property. [name={0}]", parameterName);
            types.add(getTypeFromProperty(property));
        }
        return types;
    }

    private static Type getTypeFromProperty(Map<String, Object> property) {
        if (property.containsKey(ONE_OF)) {
            List<Map<String, Object>> subProperties = cast(property.get(ONE_OF));
            List<Type> types = subProperties.stream()
                    .map(SchemaToolMetadata::getSingleTypeFromProperty)
                    .collect(Collectors.toList());
            return new OneOfType(types);
        }
        return getSingleTypeFromProperty(property);
    }

    private static Type getSingleTypeFromProperty(Map<String, Object> property) {
        String propertyType = cast(property.get(ToolSchema.PROPERTIES_TYPE));
        return convertJsonSchemaTypeToJavaType(propertyType);
    }

    private Set<String> extractRequiredParameters(Map<String, Object> parametersSchema) {
        if (MapUtils.isEmpty(parametersSchema)) {
            return Collections.emptySet();
        }
        List<String> required = cast(parametersSchema.get(ToolSchema.PARAMETERS_REQUIRED));
        if (CollectionUtils.isEmpty(required)) {
            return Collections.emptySet();
        }
        return new HashSet<>(required);
    }

    private Map<String, Object> extractParametersDefaultValue(Map<String, Object> properties) {
        Map<String, Object> defaultParamValue = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Map<String, Object> property = cast(entry.getValue());
            Object value = property.get(ToolSchema.PARAMETER_DEFAULT_VALUE);
            if (value != null) {
                defaultParamValue.put(entry.getKey(), value);
            }
        }
        return defaultParamValue;
    }
}
