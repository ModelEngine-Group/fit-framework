/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Json 工具类。
 *
 * @since 2024-06-19
 */
public final class JsonUtils {
    /**
     * 全局 ObjectMapper 对象，用于 json 字符串和 java 对象之间的转换。
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> TYPE = new TypeReference<Map<String, Object>>() {};

    private JsonUtils() {}

    /**
     * 将 json 字符串转换为 Map 对象。
     *
     * @param json 表示 json 字符串的 {@link String}。
     * @return Map 表示反序列化后的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public static Map<String, Object> convertToMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, TYPE);
        } catch (IOException e) {
            throw new IllegalStateException("Can not parse json string to map value.", e);
        }
    }
}
