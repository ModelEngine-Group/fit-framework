/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.retriever.filter;

import static modelengine.fitframework.inspection.Validation.notNull;

import java.util.Collection;

/**
 * 表示过滤器的 {@link Filter}。
 *
 * @author 易文渊
 * @since 2024-08-10
 */
public class Filter {
    private Operand.Expression expression;

    /**
     * 创建检视过滤器的实例。
     *
     * @param expression 表示过滤表达式的 {@link Operand.Expression}。
     * @throws IllegalArgumentException 当 {@code expression} 为 {@code null} 时。
     */
    public Filter(Operand.Expression expression) {
        this.expression = notNull(expression, "The expression cannot be null.");
    }

    /**
     * 创建 {@link Operator#EQ} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Object}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter eq(String key, Object value) {
        return new Filter(Operand.expression(Operator.EQ, key, value));
    }

    /**
     * 创建 {@link Operator#NE} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Object}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter ne(String key, Object value) {
        return new Filter(Operand.expression(Operator.NE, key, value));
    }

    /**
     * 创建 {@link Operator#LT} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Object}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter lt(String key, Object value) {
        return new Filter(Operand.expression(Operator.LT, key, value));
    }

    /**
     * 创建 {@link Operator#GT} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Object}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter gt(String key, Object value) {
        return new Filter(Operand.expression(Operator.GT, key, value));
    }

    /**
     * 创建 {@link Operator#LE} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Object}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter le(String key, Object value) {
        return new Filter(Operand.expression(Operator.LE, key, value));
    }

    /**
     * 通过 {@link Operator#GE} 连接两个操作数。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Object}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter ge(String key, Object value) {
        return new Filter(Operand.expression(Operator.GE, key, value));
    }

    /**
     * 创建 {@link Operator#IN} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Collection}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter in(String key, Collection<?> value) {
        return new Filter(Operand.expression(Operator.IN, key, value));
    }

    /**
     * 创建 {@link Operator#NIN} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Collection}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter notIn(String key, Collection<?> value) {
        return new Filter(Operand.expression(Operator.NIN, key, value));
    }

    /**
     * 创建 {@link Operator#LIKE} 表达式过滤器。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link String}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public static Filter like(String key, String value) {
        return new Filter(Operand.expression(Operator.LIKE, key, value));
    }

    /**
     * 通过 {@link Operator#AND} 连接两个表达式。
     *
     * @param other 表示右表达式的 {@link Filter}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public Filter and(Filter other) {
        this.expression = new Operand.Expression(Operator.AND, this.expression, other.expression);
        return this;
    }

    /**
     * 通过 {@link Operator#OR} 连接两个表达式。
     *
     * @param other 表示右表达式的 {@link Filter}。
     * @return 表示过滤器的 {@link Filter}。
     */
    public Filter or(Filter other) {
        this.expression = new Operand.Expression(Operator.OR, this.expression, other.expression);
        return this;
    }

    /**
     * 获取过滤器表达式。
     *
     * @return 表示表达式的 {@link Operand.Expression}。
     */
    public Operand.Expression expression() {
        return this.expression;
    }
}