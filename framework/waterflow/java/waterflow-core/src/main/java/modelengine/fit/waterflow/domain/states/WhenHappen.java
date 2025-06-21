/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.states;

import modelengine.fit.waterflow.domain.enums.SpecialDisplayNode;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.stream.operators.Operators;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent conditional branches (when clause) in a processing flow.
 *
 * @param <O> The output data type of this node.
 * @param <D> The initial data type of the containing flow.
 * @param <F> The flow type used for generic type inference.
 *
 * @author 宋永坦
 * @since 2025-06-12
 */
public class WhenHappen<O, D, I, F extends Flow<D>> {
    private final State<I, D, I, F> node;
    private final List<State<O, D, ?, F>> branches = new ArrayList<>();

    WhenHappen(Operators.Whether<I> whether, Operators.Then<I, O> processor, Conditions<D, I, F> conditions) {
        this.node = conditions.node;
        this.when(whether, processor);
    }

    /**
     * Creates a conditional branch that executes when the specified condition is met.
     *
     * @param whether The condition predicate.
     * @param processor The transformation logic.
     * @return The current WhenHappen instance for method chaining.
     */
    public WhenHappen<O, D, I, F> when(Operators.Whether<I> whether, Operators.Then<I, O> processor) {
        State<O, D, I, F> branch = new State<>(this.node.publisher()
                .map(input -> processor.process(input.getData()), whether)
                .displayAs(SpecialDisplayNode.BRANCH.name()), this.node.getFlow());
        this.branches.add(branch);
        return this;
    }

    /**
     * Provides a default processing logic and terminates the conditional node.
     *
     * @param processor The handler to process unmatched inputs.
     * @return An {@link State} representing the join node of the conditional flow.
     * @throws IllegalArgumentException if processor is null.
     */
    public State<O, D, O, F> others(Operators.Then<I, O> processor) {
        this.when(null, processor);
        State<O, D, O, F> joinState = this.branches.get(0).just(any -> {});
        joinState.processor.displayAs(SpecialDisplayNode.OTHERS.name());
        this.branches.stream().skip(1).forEach(branch -> branch.publisher().subscribe(joinState.subscriber()));
        return joinState;
    }
}
