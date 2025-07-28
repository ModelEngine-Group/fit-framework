/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities;

import modelengine.fel.engine.activities.processors.AiBranchProcessor;
import modelengine.fel.engine.activities.processors.AiBranchToProcessor;
import modelengine.fel.engine.flows.AiFlow;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.states.MatchHappen;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fitframework.inspection.Validation;

/**
 * 条件分支。
 *
 * @param <O> 表示当前节点的输出数据类型。
 * @param <D> 表示当前节点所属流程的初始数据类型。
 * @param <I> 表示入参数据类型。
 * @param <RF> 表示内部的数据流程类型，是 {@link Flow}{@code <}{@link D}{@code >} 的扩展。
 * @param <F> AI 流程类型，是 {@link AiFlow}{@code <}{@link D}{@code , }{@link RF}{@code >} 的扩展。
 * @since 2024-04-28
 */
public class AiMatchHappen<O, D, I, RF extends Flow<D>, F extends AiFlow<D, RF>> {
    private final MatchHappen<O, D, I, RF> matchHappen;

    private final F flow;

    /**
     * 创建一个 AI 流程匹配发生器。
     *
     * @param matchHappen 匹配发生器。
     * @param flow AI 流程。
     */
    public AiMatchHappen(MatchHappen<O, D, I, RF> matchHappen, F flow) {
        this.matchHappen = Validation.notNull(matchHappen, "MatchHappen cannot be null.");
        this.flow = Validation.notNull(flow, "Flow cannot be null.");
    }

    /**
     * 指定条件和对应的处理器创建条件分支。
     *
     * @param whether 表示匹配条件的 {@link Operators.Whether}{@code <}{@link I}{@code >}。
     * @param processor 表示分支处理器的 {@link AiBranchProcessor}{@code <}{@link O}{@code , }{@link D}{@code ,
     * }{@link I}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @return 表示条件分支的 {@link AiMatchHappen}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public AiMatchHappen<O, D, I, RF, F> match(Operators.Whether<I> whether,
            AiBranchProcessor<O, D, I, RF, F> processor) {
        Validation.notNull(processor, "Ai branch processor cannot be null.");
        return new AiMatchHappen<>(this.matchHappen.match(whether,
                node -> processor.process(new AiState<>(node, this.flow)).state), this.flow);
    }

    /**
     * 指定条件和对应的处理器创建条件跳转分支。
     *
     * @param whether 表示匹配条件的 {@link Operators.Whether}{@code <}{@link I}{@code >}。
     * @param processor 表示分支跳转处理器的 {@link AiBranchToProcessor}{@code <}{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @return 表示条件分支的 {@link AiMatchHappen}{@code <}{@link D}{@code , }{@link I}{@code , }{@link RF}{@code ,
     * }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public AiMatchHappen<O, D, I, RF, F> matchTo(Operators.Whether<I> whether,
            AiBranchToProcessor<D, I, RF, F> processor) {
        Validation.notNull(processor, "Ai branchTo processor cannot be null.");
        this.matchHappen.matchTo(whether, node -> processor.process(new AiState<>(node, this.flow)));
        return this;
    }

    /**
     * 提供一个默认的处理逻辑，并结束条件节点。
     *
     * @param processor 表示分支处理器的 {@link AiBranchProcessor}{@code <}{@link O}{@code , }{@link D}{@code ,
     * }{@link I}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @return 表示条件节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public AiState<O, D, O, RF, F> others(AiBranchProcessor<O, D, I, RF, F> processor) {
        Validation.notNull(processor, "Ai branch processor cannot be null.");
        return new AiState<>(this.matchHappen.others(node -> processor.process(new AiState<>(node, this.flow)).state),
                this.flow);
    }

    /**
     * 结束条件节点。放弃不满足上述分支条件的数据。
     *
     * @return 表示条件节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     */
    public AiState<O, D, O, RF, F> others() {
        return new AiState<>(this.matchHappen.others(), this.flow);
    }
}
