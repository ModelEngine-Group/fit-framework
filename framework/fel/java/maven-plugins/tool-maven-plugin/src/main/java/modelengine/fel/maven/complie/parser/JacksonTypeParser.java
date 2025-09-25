/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 将类型解析成适配 json schema 的数据结构。
 *
 * @author 杭潇
 * @since 2024-06-12
 */
public class JacksonTypeParser {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> PARSED_TYPES = new HashSet<>();
    private static final List<Class<?>> WRAPPER_TYPES =
            Arrays.asList(String.class, Date.class, LocalDate.class, LocalTime.class, LocalDateTime.class);
    private static final List<Class<?>> INTEGER_TYPES = Arrays.asList(byte.class,
            Byte.class,
            int.class,
            Integer.class,
            short.class,
            Short.class,
            long.class,
            Long.class);
    private static final List<Class<?>> NUMBER_TYPES =
            Arrays.asList(float.class, Float.class, double.class, Double.class);
    private static final List<Class<?>> BOOLEAN_TYPES = Arrays.asList(boolean.class, Boolean.class);

    /**
     * 将类型解析为 Json Schema 类型的 {@link JsonNode}。
     *
     * @param generic 表示给定的参数类型的 {@link TypeDescription.Generic}。
     * @return 解析后的 Json Schema 的值。
     */
    public static JsonNode getParameterSchema(TypeDescription.Generic generic) {
        if (generic == null || generic.represents(void.class)) {
            ObjectNode nullNode = OBJECT_MAPPER.createObjectNode();
            nullNode.put("type", "null");
            return nullNode;
        }

        TypeDescription typeDescription = generic.asErasure();
        if (typeDescription.asUnboxed().isPrimitive() || isWrapperType(typeDescription)) {
            return getPrimitiveTypeSchema(typeDescription);
        } else if (generic.isArray()) {
            return getArraySchema(Objects.requireNonNull(typeDescription.getComponentType()));
        } else if (typeDescription.isAssignableTo(Collection.class)) {
            TypeDescription.Generic collectionType = generic.getTypeArguments().get(0);
            return getCollectionSchema(getParameterSchema(collectionType));
        } else if (typeDescription.asErasure().isAssignableTo(Map.class)) {
            return getMapSchema();
        } else if (typeDescription.isEnum()) {
            return getEnumSchema();
        } else {
            return getObjectSchema(typeDescription);
        }
    }

    private static boolean isWrapperType(TypeDescription typeDescription) {
        return WRAPPER_TYPES.stream().anyMatch(typeDescription::represents);
    }

    private static JsonNode getMapSchema() {
        ObjectNode schemaNode = OBJECT_MAPPER.createObjectNode();
        schemaNode.put("type", "object");
        return schemaNode;
    }

    private static JsonNode getPrimitiveTypeSchema(TypeDescription typeDescription) {
        ObjectNode schemaNode = OBJECT_MAPPER.createObjectNode();
        schemaNode.put("type", getTypeName(typeDescription));

        if (typeDescription.represents(Date.class) || typeDescription.represents(LocalDate.class)
                || typeDescription.represents(LocalTime.class) || typeDescription.represents(LocalDateTime.class)) {
            schemaNode.put("format", "date-time");
        }
        return schemaNode;
    }

    private static String getTypeName(TypeDescription typeDescription) {
        if (isInteger(typeDescription)) {
            return "integer";
        } else if (isNumber(typeDescription)) {
            return "number";
        } else if (isBoolean(typeDescription)) {
            return "boolean";
        } else {
            return "string";
        }
    }

    private static boolean isInteger(TypeDescription typeDescription) {
        return INTEGER_TYPES.stream().anyMatch(typeDescription::represents);
    }

    private static boolean isNumber(TypeDescription typeDescription) {
        return NUMBER_TYPES.stream().anyMatch(typeDescription::represents);
    }

    private static boolean isBoolean(TypeDescription typeDescription) {
        return BOOLEAN_TYPES.stream().anyMatch(typeDescription::represents);
    }

    private static JsonNode getArraySchema(TypeDescription componentType) {
        ObjectNode schemaNode = OBJECT_MAPPER.createObjectNode();
        schemaNode.put("type", "array");
        schemaNode.set("items", getParameterSchema(componentType.asGenericType()));
        return schemaNode;
    }

    private static JsonNode getCollectionSchema(JsonNode itemsSchema) {
        ObjectNode schemaNode = OBJECT_MAPPER.createObjectNode();
        schemaNode.put("type", "array");
        schemaNode.set("items", itemsSchema);
        return schemaNode;
    }

    private static JsonNode getEnumSchema() {
        ObjectNode schemaNode = OBJECT_MAPPER.createObjectNode();
        schemaNode.put("type", "string");
        return schemaNode;
    }

    private static JsonNode getObjectSchema(TypeDescription typeDescription) {
        String typeName = typeDescription.getName();
        if (PARSED_TYPES.contains(typeName)) {
            ObjectNode schemaNode = OBJECT_MAPPER.createObjectNode();
            schemaNode.put("type", "object");
            schemaNode.put("description", typeName);
            return schemaNode;
        }
        PARSED_TYPES.add(typeName);

        ObjectNode schemaNode = OBJECT_MAPPER.createObjectNode();
        schemaNode.put("type", "object");
        ObjectNode propertiesNode = OBJECT_MAPPER.createObjectNode();

        for (FieldDescription field : typeDescription.getDeclaredFields()) {
            String fieldName = field.getName();
            JsonNode fieldSchema = getParameterSchema(field.getType());
            propertiesNode.set(fieldName, fieldSchema);
        }
        if (!propertiesNode.isEmpty()) {
            schemaNode.set("properties", propertiesNode);
        }
        PARSED_TYPES.remove(typeName);
        return schemaNode;
    }
}