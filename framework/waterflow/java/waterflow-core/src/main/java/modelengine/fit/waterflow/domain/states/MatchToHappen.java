/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.states;

import modelengine.fit.waterflow.domain.enums.SpecialDisplayNode;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.stream.reactive.Processor;

/**
 * 代表了条件分支的when
 * 有判定条件，满足条件的才会缓存
 *
 * @param <D> 这个节点对应flow的初始数据类型
 * @param <F> 对应的是处理流，还是生产流，用于泛型推演
 * @since 1.0
 */
public class MatchToHappen<D, I, F extends Flow<D>> {
    /**
     * node
     */
    protected final Conditions<D, I, F> conditions;

    MatchToHappen(Operators.Whether<I> whether, Operators.BranchToProcessor<D, I, F> processor,
            Conditions<D, I, F> conditions) {
        this.conditions = conditions;
        this.to(whether, processor);
    }

    /**
     * 创建一个分支，在满足该分支条件时，进行跳转操作，用于实现goto的循环效果
     * <p>
     * 通常在跳转操作里面使用 {@link State#to(String)} 或者 {@link State#to(State)} 实现循环
     * </p>
     *
     * @param whether 分支条件
     * @param processor 分支逻辑，通常是跳转
     * @return 本节点，继续用于判定动作
     */
    public MatchToHappen<D, I, F> matchTo(Operators.Whether<I> whether,
            Operators.BranchToProcessor<D, I, F> processor) {
        this.to(whether, processor);
        return this;
    }

    /**
     * 创建一个分支，在满足该分支条件时，执行该分支的逻辑
     * 该分支的处理后的类型必须与conditions的首个分支条件的返回值类型一致
     *
     * @param <O> 分支处理后的数据类型
     * @param whether 分支条件
     * @param processor 分支的处理逻辑
     * @return 新的处理节点
     */
    public <O> MatchHappen<O, D, I, F> match(Operators.Whether<I> whether,
            Operators.BranchProcessor<O, D, I, F> processor) {
        return new MatchHappen<>(whether, processor, this.conditions);
    }

    /**
     * 提供一个默认的处理逻辑，类似于switch的default语法，并结束conditions节点
     *
     * @param <O> 输出数据类型
     * @param processor 默认的处理逻辑
     * @return conditions后续的节点
     */
    public <O> State<O, D, ?, F> others(Operators.BranchProcessor<O, D, I, F> processor) {
        Processor<I, I> node = this.conditions.node.publisher().just(any -> {
        }, null).displayAs(SpecialDisplayNode.OTHERS.name());
        State<I, D, I, F> branchStart = new State<>(node, this.conditions.node.getFlow());
        return processor.process(branchStart);
    }

    private void to(Operators.Whether<I> whether, Operators.BranchToProcessor<D, I, F> processor) {
        State<I, D, I, F> branchStart = new State<>(this.conditions.node.publisher().just(any -> {
        }, whether).displayAs(SpecialDisplayNode.BRANCH.name()), this.conditions.node.getFlow());
        processor.process(branchStart);
    }
}
