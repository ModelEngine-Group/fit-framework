/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.compile.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;

import modelengine.fel.maven.complie.parser.JacksonTypeParser;

import net.bytebuddy.description.type.TypeDescription.Generic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 表示 {@link JacksonTypeParser} 的单元测试。
 *
 * @author 杭潇
 * @since 2024-06-29
 */
@DisplayName("测试 JacksonTypeParser 类")
class JacksonTypeParserTest {
    @Test
    @DisplayName("解析基本类型数据，解析正确")
    void givenPrimitiveTypeSchemaThenParseSuccess() {
        JsonNode schema = JacksonTypeParser.getParameterSchema(Generic.OfNonGenericType.ForLoadedType.of(int.class));
        assertTrue(schema.has("type"));
        assertEquals("integer", schema.get("type").asText());
    }

    @Test
    @DisplayName("解析日期类型数据，解析正确")
    void givenDateTypeSchemaThenParseSuccess() {
        JsonNode schema =
                JacksonTypeParser.getParameterSchema(Generic.OfNonGenericType.ForLoadedType.of(LocalDate.class));
        assertTrue(schema.has("type"));
        assertEquals("string", schema.get("type").asText());
        assertEquals("date-time", schema.get("format").asText());
    }

    @Test
    @DisplayName("解析数组类型数据，解析正确")
    void givenCollectionTypeSchemaThenParseSuccess() {
        Generic collectionType = Generic.Builder.parameterizedType(List.class, String.class).build();
        JsonNode schema = JacksonTypeParser.getParameterSchema(collectionType);
        assertTrue(schema.has("type"));
        assertEquals("array", schema.get("type").asText());
        assertTrue(schema.has("items"));
        assertEquals("string", schema.get("items").get("type").asText());
    }

    @Test
    @DisplayName("解析 Map 类型的数据，解析正确")
    void givenMapTypeSchemaThenParseSuccess() {
        Generic mapType = Generic.Builder.parameterizedType(Map.class, String.class, Integer.class).build();
        JsonNode schema = JacksonTypeParser.getParameterSchema(mapType);
        assertTrue(schema.has("type"));
        assertEquals("object", schema.get("type").asText());
    }

    @Test
    @DisplayName("解析自定义数据，解析正确")
    void givenObjectTypeSchemaThenParseSuccess() {
        Generic objectType = Generic.OfNonGenericType.ForLoadedType.of(TestObject.class);
        JsonNode schema = JacksonTypeParser.getParameterSchema(objectType);
        assertTrue(schema.has("type"));
        assertEquals("object", schema.get("type").asText());
        assertTrue(schema.has("properties"));
        assertTrue(schema.get("properties").has("fieldName"));
        assertEquals("string", schema.get("properties").get("fieldName").get("type").asText());
    }

    static class TestObject {
        private String fieldName;

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
    }
}