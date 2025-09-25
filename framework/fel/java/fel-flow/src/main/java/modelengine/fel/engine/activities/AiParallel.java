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
import modelengine.fit.waterflow.domain.states.Parallel;
import modelengine.fitframework.inspection.Validation;

/**
 * 平行节点。
 *
 * @param <D> 表示当前节点所属流程的初始数据类型。
 * @param <I> 表示入参数据类型。
 * @param <RF> 表示内部的数据流程类型，是 {@link Flow}{@code <}{@link D}{@code >} 的扩展。
 * @param <F> AI 流程类型，是 {@link AiFlow}{@code <}{@link D}{@code , }{@link RF}{@code >} 的扩展。
 * @author 刘信宏
 * @since 2024-04-28
 */
public class AiParallel<D, I, RF extends Flow<D>, F extends AiFlow<D, RF>> extends AiActivity<D, RF, F> {
    private final Parallel<D, I, RF> parallel;

    /**
     * 初始化 {@link AiParallel}{@code <}{@link D}{@code , }{@link I}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     *
     * @param parallel 表示被装饰的 {@link Parallel}{@code <}{@link D}{@code , }{@link I}{@code , }{@link RF}{@code >}。
     * @param flow 表示 AI 流程对象的 {@link F}。
     */
    public AiParallel(Parallel<D, I, RF> parallel, F flow) {
        super(flow);
        this.parallel = Validation.notNull(parallel, "Parallel cannot be null.");
    }

    /**
     * 生成平行节点的子分支。
     *
     * @param <O> 表示分支处理器的输入参数的类型。
     * @param processor 表示分支处理器的 {@link AiBranchProcessor}{@code <}{@link O}{@code , }{@link D}{@code ,
     * }{@link I}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @return 表示平行节点子分支的 {@link AiFork}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <O> AiFork<O, D, I, RF, F> fork(AiBranchProcessor<O, D, I, RF, F> processor) {
        Validation.notNull(processor, "Ai branch processor cannot be null.");
        Fork<O, D, I, RF> fork =
                parallel.fork(input -> processor.process(new AiState<>(input, AiParallel.this.flow())).state);
        return new AiFork<>(fork, this.flow());
    }
}
