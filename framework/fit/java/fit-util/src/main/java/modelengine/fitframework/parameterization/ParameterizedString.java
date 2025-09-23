/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.parameterization;

import java.util.List;
import java.util.Map;

/**
 * 为参数解析提供结果。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public interface ParameterizedString {
    /**
     * 获取解析该参数化字符串的解析器。
     *
     * @return 表示参数化字符串解析器的 {@link ParameterizedStringResolver}。
     */
    ParameterizedStringResolver getResolver();

    /**
     * 获取进行变量解析的源字符串。
     *
     * @return 表示源字符串的 {@link String}。
     */
    String getOriginalString();

    /**
     * 获取解析到的参数信息的列表。
     *
     * @return 表示参数信息列表的 {@link List}{@code <}{@link ResolvedParameter}{@code >}。
     */
    List<ResolvedParameter> getParameters();

    /**
     * 使用指定的参数映射格式化字符串。
     *
     * @param args 表示参数映射的 {@link Map}{@code <?, ?>}。
     * @return 表示格式化后的字符串的 {@link String}。
     * @throws StringFormatException 当需要但是未提供指定名称的参数时。
     */
    String format(Map<?, ?> args);

    /**
     * 使用指定的参数映射格式化字符串。
     *
     * @param args 表示参数映射的 {@link Map}{@code <?, ?>}。
     * @param defaultValue 表示缺少对应参数时的默认值的 {@link String}。
     * @return 表示格式化后的字符串的 {@link String}。
     * @throws StringFormatException 当需要但是未提供指定名称的参数时。
     */
    String format(Map<?, ?> args, String defaultValue);
}
