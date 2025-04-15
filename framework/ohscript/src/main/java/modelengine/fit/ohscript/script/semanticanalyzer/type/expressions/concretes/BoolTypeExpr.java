/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.ohscript.script.semanticanalyzer.type.expressions.concretes;

import modelengine.fit.ohscript.script.parser.nodes.SyntaxNode;
import modelengine.fit.ohscript.script.parser.nodes.TerminalNode;
import modelengine.fit.ohscript.script.semanticanalyzer.Type;
import modelengine.fit.ohscript.script.semanticanalyzer.type.expressions.base.SimpleTypeExpr;
import modelengine.fit.ohscript.script.semanticanalyzer.type.expressions.base.TypeExpr;

/**
 * boolean类型表达式
 *
 * @since 1.0
 */
public class BoolTypeExpr extends SimpleTypeExpr {
    /**
     * 通过语法节点初始化 {@link BoolTypeExpr} 的新实例。
     *
     * @param node 表示语法节点的 {@link SyntaxNode}。
     */
    public BoolTypeExpr(SyntaxNode node) {
        super(node);
    }

    @Override
    public Type type() {
        return Type.BOOLEAN;
    }

    @Override
    public TypeExpr duplicate(TerminalNode node) {
        return new BoolTypeExpr(node);
    }
}
