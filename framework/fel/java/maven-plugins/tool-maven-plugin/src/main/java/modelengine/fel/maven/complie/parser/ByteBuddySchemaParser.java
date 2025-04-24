/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.parser;

import com.fasterxml.jackson.databind.JsonNode;

import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fel.tool.info.entity.ParameterEntity;
import modelengine.fel.tool.info.entity.PropertyEntity;
import modelengine.fel.tool.info.entity.ReturnPropertyEntity;
import modelengine.fel.tool.info.entity.SchemaEntity;
import modelengine.fitframework.annotation.Property;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;

import static modelengine.fitframework.inspection.Validation.notNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 提供对摘要信息的解析。
 *
 * @author 曹嘉美
 * @author 杭潇
 * @since 2024-10-28
 */
public class ByteBuddySchemaParser {
    private static final String SCHEMA_TYPE = "object";

    /**
     * 解析方法信息。
     *
     * @param methodDescription 表示解析方法描述的 {@link MethodDescription}。
     * @return 表示描述信息实体的 {@link SchemaEntity}。
     */
    public static Optional<SchemaEntity> parseMethodSchema(MethodDescription methodDescription) {
        AnnotationDescription.Loadable<ToolMethod> toolAnnotation =
                methodDescription.getDeclaredAnnotations().ofType(ToolMethod.class);
        if (toolAnnotation == null) {
            return Optional.empty();
        }
        ToolMethod toolMethod = toolAnnotation.load();
        SchemaEntity schemaEntity = new SchemaEntity();
        schemaEntity.setName(toolMethod.name());
        schemaEntity.setDescription(toolMethod.description());
        schemaEntity.setParameters(parseParameter(methodDescription));
        schemaEntity.setOrder(new LinkedList<>(schemaEntity.getParameters().getProperties().keySet()));
        schemaEntity.setRet(getStringObjectMap(parseReturnProperty(methodDescription)));
        List<String> extraParams = Arrays.asList(toolMethod.extraParams());
        if (!extraParams.isEmpty()) {
            Map<String, Object> parameterExtensions = new LinkedHashMap<>();
            parameterExtensions.put("config", Arrays.asList(toolMethod.extraParams()));
            schemaEntity.setParameterExtensions(parameterExtensions);
        }
        return Optional.of(schemaEntity);
    }

    private static Map<String, Object> getStringObjectMap(ReturnPropertyEntity returnPropertyEntity) {
        Map<String, Object> returnProperty = new LinkedHashMap<>();
        returnProperty.put("name", returnPropertyEntity.getName());
        returnProperty.put("description", returnPropertyEntity.getDescription());
        returnProperty.put("type", returnPropertyEntity.getType());
        returnProperty.put("items", returnPropertyEntity.getItems());
        returnProperty.put("properties", returnPropertyEntity.getProperties());
        if (returnPropertyEntity.getConvertor() != null) {
            returnProperty.put("convertor", returnPropertyEntity.getConvertor());
        }
        if (returnPropertyEntity.getExamples() != null) {
            returnProperty.put("examples", returnPropertyEntity.getExamples());
        }
        return returnProperty;
    }

    private static ParameterEntity parseParameter(MethodDescription methodDescription) {
        ParameterEntity parameterEntity = new ParameterEntity();
        parameterEntity.setType(SCHEMA_TYPE);
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new LinkedList<>();
        for (ParameterDescription parameterDescription : methodDescription.getParameters()) {
            String methodName = parameterDescription.getName();
            PropertyEntity property = parseProperty(parameterDescription);
            properties.put(methodName, property);
            if (property.isRequired()) {
                required.add(methodName);
            }
        }
        parameterEntity.setProperties(properties);
        parameterEntity.setRequired(required);
        return parameterEntity;
    }

    private static PropertyEntity parseProperty(ParameterDescription parameterDescription) {
        JsonNode jsonNode = notNull(JacksonTypeParser.getParameterSchema(parameterDescription.getType()),
                "The parameter type cannot be null.");
        PropertyEntity entity = new PropertyEntity();
        setPropertyType(entity, jsonNode);
        entity.setName(parameterDescription.getName());
        AnnotationDescription.Loadable<Property> paramAnnotation =
                parameterDescription.getDeclaredAnnotations().ofType(Property.class);
        if (paramAnnotation != null) {
            Property property = paramAnnotation.load();
            entity.setDescription(property.description());
            entity.setRequired(property.required());
            entity.setDefaultValue(property.defaultValue());
            entity.setExamples(property.example());
        }
        return entity;
    }

    private static ReturnPropertyEntity parseReturnProperty(MethodDescription methodDescription) {
        ReturnPropertyEntity returnPropertyEntity = new ReturnPropertyEntity();
        AnnotationDescription.Loadable<Property> returnAnnotation =
                methodDescription.getDeclaredAnnotations().ofType(Property.class);
        if (returnAnnotation != null) {
            Property property = returnAnnotation.load();
            returnPropertyEntity.setName(property.name());
            returnPropertyEntity.setDescription(property.description());
            returnPropertyEntity.setExamples(property.example());
        }
        notNull(methodDescription.getReturnType(), "The return type cannot be null.");
        JsonNode jsonNode = JacksonTypeParser.getParameterSchema(methodDescription.getReturnType());
        setPropertyType(returnPropertyEntity, jsonNode);
        AnnotationDescription.Loadable<ToolMethod> toolMethodAnnotation =
                methodDescription.getDeclaredAnnotations().ofType(ToolMethod.class);
        if (toolMethodAnnotation != null) {
            returnPropertyEntity.setConvertor(toolMethodAnnotation.load().returnConverter());
        }
        return returnPropertyEntity;
    }

    private static void setPropertyType(PropertyEntity returnPropertyEntity, JsonNode jsonNode) {
        returnPropertyEntity.setType(jsonNode.get("type").asText());
        returnPropertyEntity.setItems(null);
        returnPropertyEntity.setProperties(null);
        if (Objects.equals(returnPropertyEntity.getType(), "array")) {
            returnPropertyEntity.setItems(jsonNode.get("items"));
        }
        if (Objects.equals(returnPropertyEntity.getType(), "object")) {
            if (jsonNode.get("properties") != null) {
                returnPropertyEntity.setProperties(jsonNode.get("properties"));
            }
        }
    }
}