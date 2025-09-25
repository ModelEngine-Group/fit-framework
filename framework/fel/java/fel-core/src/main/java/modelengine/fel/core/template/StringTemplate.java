/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template;

import modelengine.fel.core.template.support.DefaultStringTemplate;

import java.util.Map;

/**
 * 字符串模板接口定义。
 *
 * @author 易文渊
 * @since 2024-04-27
 */
public interface StringTemplate extends GenericTemplate<Map<String, String>, String> {
    /**
     * 创建一个默认的字符串模板实例。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @return 表示字符串模板的 {@link StringTemplate}。
     */
    static StringTemplate create(String template) {
        return new DefaultStringTemplate(template);
    }
}