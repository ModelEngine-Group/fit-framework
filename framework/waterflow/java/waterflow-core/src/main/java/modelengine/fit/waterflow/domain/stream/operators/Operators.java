/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.operators;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.StateContext;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.states.DataStart;
import modelengine.fit.waterflow.domain.states.State;
import modelengine.fit.waterflow.domain.stream.Collector;
import modelengine.fit.waterflow.domain.stream.nodes.Retryable;
import modelengine.fit.waterflow.domain.stream.nodes.To;

import java.util.List;

/**
 * 数据处理器类型
 * 总共有：只处理不改变类型；处理并改变类型；m条数据处理生成n条其他数据；m条数据处理生成1条其他数据
 * 辉子 2019.10.31
 *
 * @author 高诗意
 * @since 1.0
 */
public final class Operators {
    /**
     * process处理，并往下发射新的数据，支持操作session KV状态数据
     *
     * @param <T>原材料类型
     * @param <R>发射新数据类型
     */
    @FunctionalInterface
    public interface Process<T, R> {
        /**
         * process
         *
         * @param input input
         * @param context KV 上下文
         * @param collector 新数据发射器
         */
        void process(T input, StateContext context, Collector<R> collector);
    }

    /**
     * 节点携带session KV状态数据处理一个原材料->一个产品的生产过程
     *
     * @param <T>原材料类型
     * @param <R>产品类型
     */
    @FunctionalInterface
    public interface ProcessMap<T, R> {
        /**
         * process
         *
         * @param input input
         * @param context KV 上下文
         * @return R
         */
        R process(T input, StateContext context);
    }

    /**
     * 节点处理一个原材料->一个产品的生产过程
     *
     * @param <T>原材料类型
     * @param <R>产品类型
     */
    @FunctionalInterface
    public interface Map<T, R> {
        /**
         * process
         *
         * @param input input
         * @return R
         */
        R process(T input);
    }

    /**
     * 节点处理1个原材料到m个产品的过程
     *
     * @param <T> 原材料类型
     * @param <R> 产品类型
     */
    @FunctionalInterface
    public interface FlatMap<T, R> {
        /**
         * process
         *
         * @param input input
         * @return 用于发射的新数据
         */
        DataStart<R, R, ?> process(T input);
    }

    /**
     * 节点处理m个原材料->一个产品的生产过程
     *
     * @param <T>原材料类型
     * @param <R>产品类型
     */
    @FunctionalInterface
    public interface Reduce<T, R> {
        /**
         * process
         *
         * @param input input
         * @param acc 累加器
         * @return R
         */
        R process(R acc, T input);
    }

    /**
     * 节点处理m个原材料->一个产品的生产过程, 支持操作session KV状态数据
     *
     * @param <T>原材料类型
     * @param <R>产品类型
     */
    @FunctionalInterface
    public interface ProcessReduce<T, R> {
        /**
         * process
         *
         * @param input input
         * @param acc 累加器
         * @param context session KV状态数据
         * @return R
         */
        R process(R acc, T input, StateContext context);
    }

    /**
     * 节点处理m个原材料到n个产品的过程
     *
     * @param <T> 原材料类型
     * @param <R> 产品类型
     */
    @FunctionalInterface
    public interface Produce<T, R> {
        /**
         * process
         *
         * @param input input
         * @return R
         */
        List<R> process(List<T> input);
    }

    /**
     * 对原材料进行加工
     *
     * @param <T> 需要加工的原材料类型
     */
    @FunctionalInterface
    public interface Just<T> {
        /**
         * process
         *
         * @param input input
         */
        void process(T input);
    }

    /**
     * window接口，提供window结束的判定
     *
     * @since 1.0
     */
    @FunctionalInterface
    public interface WindowCondition {
        /**
         * window是否完成
         *
         * @param arg 判定window完成的参数
         * @return 是，完成
         */
        boolean fulfilled(WindowArg arg);
    }

    /**
     * 对原材料进行加工
     *
     * @param <T> 需要加工的原材料类型
     */
    @FunctionalInterface
    public interface ProcessJust<T> {
        /**
         * process
         *
         * @param input 需要加工的原材料类型
         * @param context session KV状态数据
         */
        void process(T input, StateContext context);
    }

    /**
     * Error
     *
     * @since 1.0
     */
    @FunctionalInterface
    public interface ErrorHandler<T> {
        /**
         * handle
         *
         * @param exception exception
         * @param retryable retryable
         * @param contexts contexts
         */
        void handle(Exception exception, Retryable<T> retryable, List<FlowContext<T>> contexts);
    }

    /**
     * 用于when的条件判定
     *
     * @param <T> 输入类型
     */
    @FunctionalInterface
    public interface Whether<T> {
        /**
         * 判断输入是否满足条件
         *
         * @param input 输入值
         * @return 如果满足条件返回true，否则返回false
         */
        boolean is(T input);
    }

    /**
     * conditions在match时，指定该match分支的处理逻辑
     * parallel在fork时，指定该fork分支的处理逻辑
     */
    @FunctionalInterface
    public interface BranchProcessor<O, D, I, F extends Flow<D>> {
        /**
         * is
         *
         * @param input input
         * @return boolean
         */
        State<O, D, ?, F> process(State<I, D, I, F> input);
    }

    /**
     * conditions在match时，指定该match分支的处理逻辑
     * parallel在fork时，指定该fork分支的处理逻辑
     */
    @FunctionalInterface
    public interface BranchToProcessor<D, I, F extends Flow<D>> {
        /**
         * is
         *
         * @param input input
         */
        void process(State<I, D, I, F> input);
    }

    /**
     * 根据筛选条件选取符合要求的原材料，用于群体筛选
     *
     * @param <T> 原材料类型
     */
    @FunctionalInterface
    public interface Filter<T> {
        /**
         * process
         *
         * @param input input
         * @return 筛选结果
         */
        List<FlowContext<T>> process(List<FlowContext<T>> input);
    }

    /**
     * 用于单条验证原材料是否符合生产标准
     *
     * @param <T> 原材料类型
     */
    @FunctionalInterface
    public interface Validator<T> {
        /**
         * 过滤符合标准的context
         *
         * @param repo context的repo
         * @param to 目标
         * @return 符合条件的context的列表
         */
        List<FlowContext<T>> validate(FlowContextRepo repo, To<T, ?> to);
    }

    /**
     * Represents a conditional node's transformation from raw material to product.
     * This functional interface defines the processing operation that converts an
     * input of one type to an output of potentially different type.
     *
     * @param <T> The type of raw material (input) to be processed.
     * @param <R> The type of product (output) to be produced.
     */
    @FunctionalInterface
    public interface Then<T, R> {
        /**
         * Transforms the input raw material into a processed product.
         *
         * @param input The raw material to be processed.
         * @return The transformed product result.
         */
        R process(T input);
    }
}

