/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities;

import modelengine.fel.engine.activities.processors.AiBranchProcessor;
import modelengine.fel.engine.flows.AiFlow;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.states.Fork;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fitframework.inspection.Validation;

import java.util.function.Supplier;

/**
 * 平行节点的子分支。
 *
 * @param <O> 表示当前节点的输出数据类型。
 * @param <D> 表示当前节点所属流程的初始数据类型。
 * @param <I> 表示入参数据类型。
 * @param <RF> 表示内部的数据流程类型，是 {@link Flow}{@code <}{@link D}{@code >} 的扩展。
 * @param <F> AI 流程类型，是 {@link AiFlow}{@code <}{@link D}{@code , }{@link RF}{@code >} 的扩展。
 * @author 刘信宏
 * @since 2024-04-28
 */
public class AiFork<O, D, I, RF extends Flow<D>, F extends AiFlow<D, RF>> extends AiActivity<D, RF, F> {
    private final Fork<O, D, I, RF> fork;

    /**
     * 初始化 {@link AiFork}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code , }{@link RF}{@code ,
     * }{@link F}{@code >}。
     *
     * @param fork 表示被装饰的 {@link Fork}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code >}。
     * @param flow 表示 AI 流程对象的 {@link F}。
     */
    protected AiFork(Fork<O, D, I, RF> fork, F flow) {
        super(flow);
        this.fork = Validation.notNull(fork, "Fork cannot be null.");
    }

    /**
     * 指定分支处理器创建平行节点子分支
     *
     * @param processor 表示分支处理器的 {@link AiBranchProcessor}{@code <}{@link O}{@code , }{@link D}{@code ,
     * }{@link I}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @return 表示平行节点子分支的 {@link AiFork}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public AiFork<O, D, I, RF, F> fork(AiBranchProcessor<O, D, I, RF, F> processor) {
        Validation.notNull(processor, "Ai branch processor cannot be null.");
        this.fork.fork(input -> processor.process(new AiState<>(input, this.flow())).state);
        return this;
    }

    /**
     * 生成汇聚节点。
     *
     * @return 表示汇聚节点的 {@link AiFork}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     */
    public AiState<O, D, O, RF, F> join() {
        return this.join(null, (acc, data) -> data);
    }

    /**
     * 指定初始值和汇聚逻辑，生成汇聚节点。
     *
     * @param init 表示初始值提供者的 {@link Supplier}{@code <}{@link R}{@code >}。
     * @param processor 表示数据聚合器的 {@link Operators.Reduce}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示汇聚节点的 {@link AiFork}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> join(Supplier<R> init, Operators.Reduce<O, R> processor) {
        Validation.notNull(processor, "Reduce processor cannot be null.");
        return new AiState<>(this.fork.join(init, processor), this.flow());
    }
}
