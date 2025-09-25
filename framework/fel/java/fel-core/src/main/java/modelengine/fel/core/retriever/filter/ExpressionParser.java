/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.retriever.filter;

/**
 * 表达式解析的接口定义。
 *
 * @author 易文渊
 * @since 2024-08-10
 */
public interface ExpressionParser {
    /**
     * 将表达式解析为查询语句。
     *
     * @param expression 表示过滤器表达式的 {@link Operand.Expression}。
     * @return 表示查询语句的 {@link String}。
     */
    String parse(Operand.Expression expression);
}