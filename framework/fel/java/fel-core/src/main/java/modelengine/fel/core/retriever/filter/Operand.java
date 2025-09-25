/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.retriever.filter;

import static modelengine.fitframework.inspection.Validation.isFalse;
import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fitframework.util.ObjectUtils;

/**
 * 表示操作符的实体。
 *
 * @author 易文渊
 * @since 2024-08-08
 */
public interface Operand {
    /**
     * 创建 {@link Key} 的实例。
     *
     * @param key 表示键的 {@link String}。
     * @return 返回 {@link Key} 的实例。
     */
    static Operand.Key key(String key) {
        return new Key(key);
    }

    /**
     * 创建 {@link Value} 的实例。
     *
     * @param value 表示值的 {@link Object}。
     * @return 返回 {@link Value} 的实例。
     */
    static Operand.Value value(Object value) {
        return new Value(value);
    }

    /**
     * 创建 {@link Expression} 的实例。
     *
     * @param op 表示过滤器表达式操作符枚举的 {@link Operator}。
     * @param left 表示左操作数的 {@link Operand}。
     * @param right 表示右操作数的 {@link Operand}。
     * @return 返回 {@link Expression} 的实例。
     */
    static Operand.Expression expression(Operator op, Operand left, Operand right) {
        return new Expression(op, left, right);
    }

    /**
     * 创建 {@link Expression} 的实例。
     *
     * @param op 表示过滤器表达式操作符枚举的 {@link Operator}。
     * @param key 表示键的 {@link String}。
     * @param value 表示值的 {@link Object}。
     * @return 返回 {@link Expression} 的实例。
     */
    static Operand.Expression expression(Operator op, String key, Object value) {
        Operand k = Operand.key(key);
        Operand v = Operand.value(value);
        return Operand.expression(op, k, v);
    }

    /**
     * 表示键的 {@link Operand}。
     */
    class Key implements Operand {
        private final String payload;

        /**
         * 创建 {@link Key} 的实例。
         *
         * @param key 表示键的 {@link String}。
         */
        public Key(String key) {
            this.payload = notBlank(key, "The key cannot be blank.");
        }

        /**
         * 获取过滤器键。
         *
         * @return 表示过滤器键的 {@link String}。
         */
        public String key() {
            return this.payload;
        }
    }

    /**
     * 表示值的 {@link Operand}。
     */
    class Value implements Operand {
        private final Object payload;

        /**
         * 创建 {@link Value} 的实例。
         *
         * @param value 表示值的 {@link Object}。
         */
        public Value(Object value) {
            isFalse(ObjectUtils.isCustomObject(value), "The value cannot be custom object.");
            this.payload = notNull(value, "The value cannot be null.");
        }

        /**
         * 获取过滤器值。
         *
         * @return 表示值的 {@link Object}。
         */
        public Object payload() {
            return this.payload;
        }
    }

    /**
     * 表示表达式的实体。
     */
    class Expression implements Operand {
        private final Operator op;
        private final Operand left;
        private final Operand right;

        /**
         * 创建 {@link Expression} 的实体。
         *
         * @param op 表示过滤器表达式操作符枚举的 {@link Operator}。
         * @param left 表示左操作数的 {@link Operand}。
         * @param right 表示右操作数的 {@link Operand}。
         */
        public Expression(Operator op, Operand left, Operand right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        /**
         * 获取操作符。
         *
         * @return 表示过滤器表达式操作符枚举的 {@link Operator}。
         */
        public Operator op() {
            return this.op;
        }

        /**
         * 获取左操作数。
         *
         * @return 表示左操作数的 {@link Operand}。
         */
        public Operand left() {
            return this.left;
        }

        /**
         * 获取右操作数。
         *
         * @return 表示右操作数的 {@link Operand}。
         */
        public Operand right() {
            return this.right;
        }
    }
}