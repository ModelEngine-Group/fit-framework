/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.states;

import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.FlowEmitter;
import modelengine.fit.waterflow.domain.flow.ProcessFlow;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.utils.Tuple;

import java.util.List;
import java.util.function.Supplier;

/**
 * 数据前置的start节点
 *
 * @param <O> 出参数据类型
 * @param <D> 原始数据类型
 * @param <I> 入参数据类型
 * @since 1.0
 */
public class DataStart<O, D, I> {
    /**
     * 流程开始节点。
     */
    protected final Start<O, D, I, ProcessFlow<D>> state;

    /**
     * 数据前置开始节点。
     */
    protected DataStart<?, D, ?> start;

    private final Emitter<D, FlowSession> emitter;

    /**
     * 使用单个数据初始化 DataStart。
     *
     * @param state 开始节点
     * @param data 单个数据
     */
    public DataStart(Start<O, D, I, ProcessFlow<D>> state, D data) {
        this(state, FlowEmitter.mono(data));
    }

    /**
     * 使用数据数组初始化 DataStart。
     *
     * @param state 开始节点
     * @param data 数据数组
     */
    public DataStart(Start<O, D, I, ProcessFlow<D>> state, D[] data) {
        this(state, FlowEmitter.flux(data));
    }

    /**
     * 使用数据发射器初始化 DataStart。
     *
     * @param state 开始节点
     * @param emitter 数据发射器
     */
    public DataStart(Start<O, D, I, ProcessFlow<D>> state, Emitter<D, FlowSession> emitter) {
        this.state = state;
        this.emitter = emitter;
        this.start = this;
    }

    /**
     * 使用开始节点初始化 DataStart，不指定发射器。
     *
     * @param state 开始节点
     */
    protected DataStart(Start<O, D, I, ProcessFlow<D>> state) {
        this(state, (Emitter<D, FlowSession>) null);
    }

    /**
     * just，只处理，不转换
     * <p>
     * {@link Start#just(Operators.Just)}的包装
     * </p>
     *
     * @param processor just转换器
     * @return 新的处理节点
     */
    public DataState<O, D, O> just(Operators.Just<O> processor) {
        return new DataState(this.state.just(processor), this.start);
    }

    /**
     * map,处理，并转换类型
     *
     * <p>
     * {@link Start#map(Operators.Map)}的包装
     * </p>
     *
     * @param processor map处理器
     * @param <R> 处理完类型
     * @return 新的处理节点
     */
    public <R> DataState<R, D, O> map(Operators.Map<O, R> processor) {
        return new DataState(this.state.map(processor), this.start);
    }

    /**
     * flat map,处理，1变多，并转换类型
     *
     * <p>
     * {@link Start#flatMap(Operators.FlatMap)}的包装
     * </p>
     *
     * @param processor flat map处理器
     * @param <R> 处理完类型
     * @return 新的处理节点
     */
    public <R> DataState<R, D, O> flatMap(Operators.FlatMap<O, R> processor) {
        return new DataState(this.state.flatMap(processor), this.start);
    }

    /**
     * 生成一个数据聚合节点，将每个数据通过指定的方式进行合并后，形成一个新的数据，并继续发送。
     * <p>
     * 处理后的数据类型是根据初始值来确认。
     * </p>
     *
     * @param init 表示聚合操作初始值提供者的 {@link Supplier}{@code <}{@link R}{@code >}。
     * @param processor 表示数据聚合器的 {@link Operators.ProcessReduce}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示输出数据类型。
     * @return 表示数据聚合节点的 {@link DataState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@code >}。
     */
    public <R> DataState<R, D, O> reduce(Supplier<R> init, Operators.Reduce<O, R> processor) {
        return new DataState(this.state.reduce(init, processor), this.start);
    }

    /**
     * 生成一个数据聚合节点，将每个数据通过指定的方式进行合并后，形成一个新的数据，并继续发送。
     * <p>
     * 不提供初始值，聚合之后的数据类型还是原数据类型。
     * </p>
     * <p>
     * {@link Start#reduce(Operators.Reduce)}的包装。
     * </p>
     *
     * @param processor 表示数据聚合器的 {@link Operators.ProcessReduce}{@code <}{@link O}{@code , }{@link O}{@code >}。
     * @return 表示数据聚合节点的 {@link State}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@code >}。
     */
    public DataState<O, D, O> reduce(Operators.Reduce<O, O> processor) {
        return new DataState(this.state.reduce(processor), this.start);
    }

    /**
     * 形成一个window，window中的数据满足条件后，将触发后续的数据聚合处理
     * <p>
     * {@link Start#window(Operators.WindowCondition)}的包装
     * </p>
     *
     * @param windowCondition window的条件
     * @return window的后续节点
     */
    public DataState<O, D, O> window(Operators.WindowCondition windowCondition) {
        return new DataState(this.state.window(windowCondition), this.start);
    }

    /**
     * 缓存流中的数
     * <p>
     * 通常出现在window后，当满足window的条件后，为后续节点提供window中缓存的数据列表
     * </p>
     * <p>
     * {@link Start#buffer()}的包装
     * </p>
     *
     * @return buffer后的节点
     */
    public DataState<List<O>, D, O> buffer() {
        return new DataState(this.state.buffer(), this.start);
    }

    /**
     * 聚合处理
     * <p>
     * {@link Start#keyBy(Operators.Map)}的包装
     * </p>
     *
     * @param keyGetter 提供聚合的key
     * @return 聚合后的节点，其数据类型为 {@link Tuple}
     */
    public DataState<List<O>, D, O> keyBy(Operators.Map<O, ? extends Object> keyGetter) {
        return new DataState(this.state.keyBy(keyGetter), this.start);
    }

    /**
     * produce处理节点：m->n
     * <p>
     * {@link Start#produce(Operators.Produce)} (Operators.Process)}的包装
     * </p>
     *
     * @param processor produce处理器
     * @param <R> 处理完类型
     * @return 新的处理节点
     */
    public <R> DataState<R, D, O> produce(Operators.Produce<O, R> processor) {
        return new DataState(this.state.produce(processor), this.start);
    }

    /**
     * process处理，并往下发射新的数据，支持操作session KV状态数据
     * <p>
     * {@link Start#process(Operators.Process)}的包装
     * </p>
     *
     * @param processor 携带数据、KV下文和发射器的处理器
     * @param <R> 返回值数据类型
     * @return 新的处理节点
     */
    public <R> DataState<R, D, O> process(Operators.Process<O, R> processor) {
        return new DataState(this.state.process(processor), this.start);
    }

    /**
     * 触发数据的发射。
     *
     * @param session 流会话
     */
    protected void offer(FlowSession session) {
        if (this.emitter == null) {
            return;
        }
        this.start.state.getFlow().offer(this.emitter);
        this.emitter.start(session);
    }

    /**
     * offer一个空数据
     */
    protected void offer() {
        this.offer(null);
    }
}
