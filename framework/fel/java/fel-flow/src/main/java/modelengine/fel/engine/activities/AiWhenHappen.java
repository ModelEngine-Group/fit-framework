/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities;

import modelengine.fel.engine.flows.AiFlow;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.states.WhenHappen;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fitframework.inspection.Validation;

/**
 * Represents a conditional branch that matches when conditions in an AI processing flow.
 * This class handles the branching logic when specific conditions are met in the workflow.
 *
 * @param <O> The output data type of the current node.
 * @param <D> The initial data type of the containing flow.
 * @param <I> The input parameter data type.
 * @param <RF> The internal flow type, extending {@link Flow}{@code <D>}.
 * @param <F> The AI flow type, extending {@link AiFlow}{@code <D, RF>}.
 *
 * @author 宋永坦
 * @since 2025-06-12
 */
public class AiWhenHappen<O, D, I, RF extends Flow<D>, F extends AiFlow<D, RF>> {
    private final WhenHappen<O, D, I, RF> matchHappen;

    private final F flow;

    /**
     * Creates a new AI flow matching generator that handles conditional branching.
     * This constructor initializes a stateful processor for when/then style pattern matching
     * within AI workflows.
     *
     * @param matchHappen The core matching generator that evaluates conditions.
     * @param flow The parent AI flow.
     * @throws NullPointerException If either parameter is null.
     */
    public AiWhenHappen(WhenHappen<O, D, I, RF> matchHappen, F flow) {
        this.matchHappen = Validation.notNull(matchHappen, "WhenHappen cannot be null.");
        this.flow = Validation.notNull(flow, "Flow cannot be null.");
    }

    /**
     * Creates a conditional branch with the specified predicate and handler.
     *
     * @param whether The condition predicate that determines branch activation.
     * @param processor The transformation handler to execute when condition is met.
     * @return A new {@link AiWhenHappen} instance representing the conditional branch.
     * @throws IllegalArgumentException if processor is null.
     */
    public AiWhenHappen<O, D, I, RF, F> when(Operators.Whether<I> whether, Operators.Then<I, O> processor) {
        Validation.notNull(processor, "Ai branch processor cannot be null.");
        return new AiWhenHappen<>(this.matchHappen.when(whether, processor), this.flow);
    }

    /**
     * Provides a default processing logic and terminates the conditional node.
     *
     * @param processor The handler to process unmatched inputs.
     * @return An {@link AiState} representing the terminal node of the conditional flow.
     * @throws IllegalArgumentException if processor is null.
     */
    public AiState<O, D, O, RF, F> others(Operators.Then<I, O> processor) {
        Validation.notNull(processor, "Ai branch processor cannot be null.");
        return new AiState<>(this.matchHappen.others(processor), this.flow);
    }
}
