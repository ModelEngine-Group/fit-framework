/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.retriever.support;

import modelengine.fel.core.retriever.filter.AbstractExpressionParser;
import modelengine.fel.core.retriever.filter.ExpressionParser;
import modelengine.fel.core.retriever.filter.Operand;

/**
 * 表示 {@link ExpressionParser} 的测试实现。
 *
 * @author 易文渊
 * @since 2024-08-10
 */
public class TestExpressionParser extends AbstractExpressionParser {
    @Override
    protected String parseKey(String key) {
        return key;
    }

    @Override
    protected void parseExpression(Operand.Expression expression, StringBuilder buf) {
        this.parseOperand(expression.left(), buf);
        buf.append(" ").append(expression.op()).append(" ");
        this.parseOperand(expression.right(), buf);
    }
}