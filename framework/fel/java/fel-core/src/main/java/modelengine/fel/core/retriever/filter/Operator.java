/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.retriever.filter;

/**
 * 表示过滤表达式的枚举。
 *
 * @since 2024-08-10
 */
public enum Operator {
    /** 等于。 */
    EQ,

    /** 不等于。 */
    NE,

    /** 小于。 */
    LT,

    /** 大于。 */
    GT,

    /** 小于等于。 */
    LE,

    /** 大于等于。 */
    GE,

    /** 在集合中。 */
    IN,

    /** 不在集合中。 */
    NIN,

    /** 模糊匹配。 */
    LIKE,

    /** 或运算。 */
    OR,

    /** 与运算。 */
    AND
}