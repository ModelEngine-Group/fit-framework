/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.states;

import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.stream.operators.Operators;

/**
 * Parallel Activity 平行节点，分all，either两种模式
 * 这个节点后面遇到join节点之前的分支都属于平行分支
 * all模式下，所有分支走完餐能触发join节点
 * either模式下，只要有一个分支到达join就会触发join节点，并作废其他任务
 *
 * @param <D> 这个节点对应flow的初始数据类型
 * @param <F> 对应的是处理流，还是生产流，用于泛型推演
 * @since 1.0
 */
public class Parallel<D, I, F extends Flow<D>> extends Activity<D, F> {
    private final State<I, D, I, F> node;

    /**
     * Parallel
     *
     * @param node node
     */
    public Parallel(State<I, D, I, F> node) {
        super(node.getFlow());
        this.node = node;
    }

    /**
     * parallel节点就是用来fork出其他子节点的
     *
     * @param <O> fork节点的输出类型
     * @param processor just处理器
     * @return 新的fork节点
     */
    public <O> Fork<O, D, I, F> fork(Operators.BranchProcessor<O, D, I, F> processor) {
        return new Fork<>(processor, this.node);
    }
}
