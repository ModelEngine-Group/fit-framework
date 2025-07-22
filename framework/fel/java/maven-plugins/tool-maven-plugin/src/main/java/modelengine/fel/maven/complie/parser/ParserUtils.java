/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.parser;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.annotation.Attribute;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 表示通用的解析工具类。
 *
 * @since 2024-12-12
 */
public class ParserUtils {
    /**
     * 解析属性。
     *
     * @param params 表示属性的 {@link Attribute}。
     * @return 表示解析结果的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public static Map<String, Object> parseAttributes(Attribute[] params) {
        if (params == null) {
            return new LinkedHashMap<>();
        }
        List<Attribute> attributeList = new LinkedList<>(Arrays.asList(params));
        Map<String, List<String>> attributes = new LinkedHashMap<>();
        attributeList.forEach(attribute -> attributes.computeIfAbsent(attribute.key(), k -> new LinkedList<>())
                .add(attribute.value()));
        return cast(attributes);
    }
}
