/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.reactive;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowsession.FlowSessionRepo;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;
import modelengine.fit.waterflow.domain.enums.ParallelMode;
import modelengine.fit.waterflow.domain.stream.operators.Operators;

import java.util.List;

/**
 * 数据发布者
 * 发布数据的时候同时确定接收者的数据处理方式
 * map: 1:1数据处理方式
 * reduce: n:1数据处理方式
 * produce: m:n数据处理方式
 *
 * @param <I> 要发布的数据类型
 * @since 1.0
 */
public interface Publisher<I> extends StreamIdentity, EmitterListener<I, FlowSession> {
    /**
     * 处理数据，即向后续节点offer
     *
     * @param data 待offer的数据
     */
    default void handle(I data) {
        this.offer(data);
    }

    @Override
    default void handle(I data, FlowSession flowSession) {
        FlowSession nextSession = FlowSessionRepo.getNextEmitterHandleSession(this.getStreamId(), flowSession);
        this.offer(data, nextSession);
        flowSession.getWindow().onDone(this.getStreamId(), () -> nextSession.getWindow().complete());
    }

    /**
     * conditions
     *
     * @param whether 判定条件
     * @return Processor<O, O>
     */
    Processor<I, I> conditions(Operators.Whether<I> whether);

    /**
     * parallel
     *
     * @param mode mode
     * @param whether whether
     * @return Processor<O, O>
     */
    Processor<I, I> parallel(ParallelMode mode, Operators.Whether<I> whether);

    /**
     * join
     *
     * @param processor processor
     * @param whether whether
     * @return Processor<M, O>
     */
    <O> Processor<I, O> join(Operators.Map<FlowContext<I>, O> processor, Operators.Whether<I> whether);

    /**
     * just
     *
     * @param processor processor
     * @param whether whether
     * @return Processor<O, O>
     */
    Processor<I, I> just(Operators.Just<FlowContext<I>> processor, Operators.Whether<I> whether);

    /**
     * map
     *
     * @param processor processor
     * @param whether whether
     * @return Processor<M, O>
     */
    <O> Processor<I, O> map(Operators.Map<FlowContext<I>, O> processor, Operators.Whether<I> whether);

    /**
     * 将每个数据通过指定的方式转换为一个数据流，并将数据流的数据往下发射流转。
     *
     * @param processor 表示数据转换器的 {@link Operators.FlatMap}{@code <}{@link FlowContext}{@code <}{@link I}{@code
     * >}{@code , }{@link O}{@code >}。
     * @param whether 表示判定条件的 {@link Operators.Whether}{@code <}{@link I}{@code >}。
     * @param <O> 表示数据处理器的输出数据类型。
     * @return 表示数据处理器的 {@link Processor}{@code <}{@link I}{@code , }{@link O}{@code >}。
     */
    <O> Processor<I, O> flatMap(Operators.FlatMap<FlowContext<I>, O> processor, Operators.Whether<I> whether);

    /**
     * process处理，并往下发射新的数据，支持操作 session KV状态数据
     *
     * @param processor 携带数据、KV下文和发射器的处理器
     * @param whether whether
     * @return Processor<M, O>
     */
    <O> Processor<I, O> process(Operators.Process<FlowContext<I>, O> processor, Operators.Whether<I> whether);

    /**
     * subscribe
     *
     * @param subscriber subscriber
     */
    <O> void subscribe(Subscriber<I, O> subscriber);

    /**
     * subscribe
     *
     * @param subscriber subscriber
     * @param whether whether
     */
    <O> void subscribe(Subscriber<I, O> subscriber, Operators.Whether<I> whether);

    /**
     * subscribe
     *
     * @param eventId eventId
     * @param subscriber subscriber
     */
    <O> void subscribe(String eventId, Subscriber<I, O> subscriber);

    /**
     * subscribe
     *
     * @param eventId eventId
     * @param subscriber subscriber
     * @param whether whether
     */
    <O> void subscribe(String eventId, Subscriber<I, O> subscriber, Operators.Whether<I> whether);

    /**
     * offer
     *
     * @param contexts contexts
     */
    void offer(List<FlowContext<I>> contexts);

    /**
     * offer
     *
     * @param data data
     * @return String
     */
    String offer(I data);

    /**
     * 在指定的session中offer一个数据
     *
     * @param data 待offer的数据
     * @param session 指定的session
     * @return 本次offer产生的traceId
     */
    String offer(I data, FlowSession session);

    /**
     * 在指定的session中offer一组数据
     *
     * @param data 待offer的数据
     * @param session 指定的session
     * @return 本次offer产生的traceId
     */
    String offer(I[] data, FlowSession session);

    /**
     * offer
     *
     * @param data data
     * @return String
     */
    String offer(I... data);

    /**
     * subscribed
     *
     * @return boolean
     */
    boolean subscribed();

    /**
     * getSubscriptions
     *
     * @return List<Subscription < I>>
     */
    List<Subscription<I>> getSubscriptions();

    /**
     * 获取context repo
     *
     * @return repo
     */
    FlowContextRepo getFlowContextRepo();
}
