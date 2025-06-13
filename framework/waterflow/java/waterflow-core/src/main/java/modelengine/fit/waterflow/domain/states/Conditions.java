/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.states;

import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.stream.operators.Operators;

/**
 * 条件节点，用于表示一个条件判断，在流程中，可以有多个条件节点，每个条件节点后面可以跟一个或多个条件分支。
 *
 * @param <D> 表示原始数据类型。
 * @param <I> 表示入参数据类型。
 * @param <F> 表示内部流程类型。
 * @since 1.0
 */
public class Conditions<D, I, F extends Flow<D>> extends Activity<D, F> {
    /**
     * 条件节点，用于表示一个条件判断，在流程中，可以有多个条件节点，每个条件节点后面可以跟一个或多个条件分支。
     */
    protected final State<I, D, I, F> node;

    /**
     * 定义流程的分支，类似于流程图中的菱形节点。
     *
     * @param node 表示条件节点的 {@link State}{@code <} {@link I}{@code ,} {@link D}
     *             {@code ,} {@link I}{@code ,} {@link F}{@code >}。
     */
    protected Conditions(State<I, D, I, F> node) {
        super(node.getFlow());
        this.node = node;
    }

    /**
     * 创建一个分支，在满足该分支条件时执行逻辑。
     *
     * @param <O> 表示返回值类型的泛型参数。
     * @param whether 表示条件判定函数的 {@link Operators.Whether}{@code <} {@link I}{@code >}。
     * @param processor 表示处理器的 {@link Operators.BranchProcessor}{@code <} {@link O}{@code ,} {@link D}{@code ,}
     *                  {@link I}{@code ,} {@link F}{@code >}。
     * @return 表示条件关系的 {@link MatchHappen}{@code <} {@link O}{@code ,}I}{@code ,}
     *         {@link D}{@code ,} {@link I}{@code ,} {@link F}{@code >}。
     */
    public <O> MatchHappen<O, D, I, F> match(Operators.Whether<I> whether,
            Operators.BranchProcessor<O, D, I, F> processor) {
        return new MatchHappen<>(whether, processor, this);
    }

    /**
     * Creates a conditional branch that executes when the specified condition is met.
     * This establishes a processing path that will only be followed if the predicate
     * evaluates to true for the input data.
     *
     * @param <O> The output type of the branch processor.
     * @param whether The condition predicate that determines branch activation.
     * @param processor The transformation to apply when the condition is met.
     * @return A {@link WhenHappen} instance representing the conditional relationship,
     *         allowing for further chaining of operations.
     */
    public <O> WhenHappen<O, D, I, F> when(Operators.Whether<I> whether, Operators.Then<I, O> processor) {
        return new WhenHappen<>(whether, processor, this);
    }

    /**
     * 在满足条件时跳转到指定节点。
     *
     * @param whether 表示条件判定函数的 {@link Operators.Whether}{@code <} {@link I}{@code >}。
     * @param processor 表示处理器的 {@link Operators.BranchToProcessor}{@code <} {@link D}{@code ,}
     *                  {@link I}{@code ,} {@link F}{@code >}。
     * @return 表示条件关系的 {@link MatchHappen}{@code <} {@link I}{@code ,}
     *         {@link D}{@code ,} {@link I}{@code ,} {@link F}{@code >}。
     */
    public MatchToHappen<D, I, F> matchTo(Operators.Whether<I> whether,
            Operators.BranchToProcessor<D, I, F> processor) {
        return new MatchToHappen<>(whether, processor, this);
    }
}
