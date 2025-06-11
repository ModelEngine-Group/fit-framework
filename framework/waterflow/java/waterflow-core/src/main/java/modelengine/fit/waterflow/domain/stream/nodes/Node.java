/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.nodes;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMessenger;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowlock.FlowLocks;
import modelengine.fit.waterflow.domain.enums.FlowNodeType;
import modelengine.fit.waterflow.domain.enums.ParallelMode;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.stream.reactive.NodeDisplay;
import modelengine.fit.waterflow.domain.stream.reactive.Processor;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fit.waterflow.domain.stream.reactive.Subscriber;
import modelengine.fit.waterflow.domain.stream.reactive.Subscription;
import modelengine.fit.waterflow.domain.utils.Identity;
import modelengine.fitframework.inspection.Validation;

import java.util.List;
import java.util.function.Supplier;

/**
 * 中间节点，既是数据发送者，也是数据接受者
 * 由于没有多重继承，node直接继承自To，并生成一个From，引用所有From的实现
 *
 * @param <T> 输入数据类型
 * @param <R> 输出数据类型
 * @author 高诗意
 * @since 1.0
 */
public class Node<T, R> extends To<T, R> implements Processor<T, R>, Identity {
    /**
     * 持有的publisher
     */
    protected final Publisher<R> publisher;

    private final NodeDisplay display = new NodeDisplay("operation", null, null);

    /**
     * 1->1处理节点
     *
     * @param streamId stream流程ID
     * @param processor 对应处理器
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文事件发送器，默认在内存
     * @param locks 流程锁
     */
    public Node(String streamId, Operators.Map<FlowContext<T>, R> processor, FlowContextRepo repo,
            FlowContextMessenger messenger, FlowLocks locks) {
        super(streamId, processor, repo, messenger, locks);
        this.publisher = this.initFrom(repo, messenger, locks);
    }

    /**
     * 1->1处理节点
     *
     * @param streamId stream流程ID
     * @param nodeId stream流程节点ID
     * @param processor 对应处理器
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文事件发送器，默认在内存
     * @param locks 流程锁
     * @param nodeType 节点类型
     */
    public Node(String streamId, String nodeId, Operators.Map<FlowContext<T>, R> processor, FlowContextRepo repo,
            FlowContextMessenger messenger, FlowLocks locks, FlowNodeType nodeType) {
        super(streamId, nodeId, processor, repo, messenger, locks, nodeType);
        this.publisher = this.initFrom(repo, messenger, locks);
    }

    /**
     * m->n处理节点
     *
     * @param streamId stream流程ID
     * @param nodeId stream流程节点ID
     * @param processor 对应处理器
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文发送器，默认在内存
     * @param locks 流程锁
     * @param nodeType 节点类型
     */
    public Node(String streamId, String nodeId, Operators.Produce<FlowContext<T>, R> processor, FlowContextRepo repo,
            FlowContextMessenger messenger, FlowLocks locks, FlowNodeType nodeType) {
        super(streamId, nodeId, processor, repo, messenger, locks, nodeType);
        this.publisher = this.initFrom(repo, messenger, locks);
    }

    /**
     * 1->1处理节点
     *
     * @param streamId stream流程ID
     * @param processor 对应处理器
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文事件发送器，默认在内存
     * @param publisherSupplier 由子类提供构建publisher的方法
     * @param locks 流程锁
     */
    protected Node(String streamId, Operators.Map<FlowContext<T>, R> processor, FlowContextRepo repo,
            FlowContextMessenger messenger, FlowLocks locks, Supplier<Publisher<R>> publisherSupplier) {
        super(streamId, processor, repo, messenger, locks);
        this.publisher = publisherSupplier.get();
    }

    /**
     * initFrom
     *
     * @param repo contextRepo
     * @param messenger messenger
     * @param locks 流程锁
     * @return {@link From}{@code <}{@link R}{@code >}
     */
    protected From<R> initFrom(FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
        return new From<>(this.getStreamId(), repo, messenger, locks); // node里的from跟随subscriber的streamId
    }

    @Override
    public Processor<R, R> conditions(Operators.Whether<R> whether) {
        return this.publisher.conditions(whether);
    }

    @Override
    public Processor<R, R> parallel(ParallelMode mode, Operators.Whether<R> whether) {
        return this.publisher.parallel(mode, whether);
    }

    @Override
    public <O> Processor<R, O> join(Operators.Map<FlowContext<R>, O> processor, Operators.Whether<R> whether) {
        return this.publisher.join(processor, whether);
    }

    @Override
    public Processor<R, R> just(Operators.Just<FlowContext<R>> processor, Operators.Whether<R> whether) {
        return this.publisher.just(processor, whether);
    }

    @Override
    public <O> Processor<R, O> map(Operators.Map<FlowContext<R>, O> processor, Operators.Whether<R> whether) {
        return this.publisher.map(processor, whether);
    }

    @Override
    public <O> Processor<R, O> flatMap(Operators.FlatMap<FlowContext<R>, O> processor, Operators.Whether<R> whether) {
        return this.publisher.flatMap(processor, whether);
    }

    @Override
    public <O> Processor<R, O> process(Operators.Process<FlowContext<R>, O> processor, Operators.Whether<R> whether) {
        return this.publisher.process(processor, whether);
    }

    @Override
    public <O> void subscribe(Subscriber<R, O> subscriber) {
        this.publisher.subscribe(subscriber);
    }

    @Override
    public <O> void subscribe(Subscriber<R, O> subscriber, Operators.Whether<R> whether) {
        this.publisher.subscribe(subscriber, whether);
    }

    @Override
    public <O> void subscribe(String eventId, Subscriber<R, O> subscriber) {
        this.publisher.subscribe(eventId, subscriber);
    }

    @Override
    public <O> void subscribe(String eventId, Subscriber<R, O> subscriber, Operators.Whether<R> whether) {
        this.publisher.subscribe(eventId, subscriber, whether);
    }

    @Override
    public void offer(List<FlowContext<R>> contexts) {
        this.publisher.offer(contexts);
    }

    @Override
    public String offer(R data) {
        return this.publisher.offer(data);
    }

    @Override
    public String offer(R... data) {
        return this.publisher.offer(data);
    }

    @Override
    public String offer(R data, FlowSession session) {
        return this.publisher.offer(data, session);
    }

    @Override
    public String offer(R[] data, FlowSession session) {
        return this.publisher.offer(data, session);
    }

    @Override
    public boolean subscribed() {
        return this.publisher.subscribed();
    }

    @Override
    public List<Subscription<R>> getSubscriptions() {
        return this.publisher.getSubscriptions();
    }

    /**
     * 开始处理数据
     * 把该publisher里所有的数据都publish到subscription
     *
     * @param batchId 批次id
     */
    @Override
    public void onNext(String batchId) {
        this.publisher.offer(this.nextContexts(batchId));
    }

    /**
     * 关闭到最终subscriber
     *
     * @return subscriber
     */
    @Override
    public Subscriber<R, R> close() {
        Operators.Map<FlowContext<R>, R> processor = FlowContext::getData;
        Subscriber<R, R> end = new To<>(this.getStreamId(), null, processor, getFlowContextRepo(), getMessenger(),
                getLocks(), FlowNodeType.END);
        this.subscribe(end);
        return end;
    }

    @Override
    public Processor<T, R> displayAs(String name) {
        this.display.setName(Validation.notBlank(name, "Display name can not be blank"));
        return this;
    }

    @Override
    public Processor<T, R> displayAs(String name, Flow<T> displayFlow, String nodeId) {
        this.display.setName(Validation.notBlank(name, "Display name can not be blank"));
        this.display.setFlow(Validation.notNull(displayFlow, "DisplayFlow can not be null"));
        this.display.setNodeId(Validation.notBlank(nodeId, "Node id can not be blank"));
        return this;
    }

    @Override
    public NodeDisplay display() {
        return this.display;
    }
}
