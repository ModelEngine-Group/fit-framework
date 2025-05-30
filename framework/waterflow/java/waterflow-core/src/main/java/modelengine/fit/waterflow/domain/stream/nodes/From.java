/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.nodes;

import static modelengine.fit.waterflow.ErrorCodes.FLOW_ENGINE_INVALID_MANUAL_TASK;

import modelengine.fit.waterflow.domain.context.repo.flowsession.FlowSessionRepo;
import modelengine.fit.waterflow.exceptions.WaterflowException;
import modelengine.fit.waterflow.domain.context.FlatMapSourceWindow;
import modelengine.fit.waterflow.domain.context.FlatMapWindow;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.FlowTrace;
import modelengine.fit.waterflow.domain.context.Window;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMessenger;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowlock.FlowLocks;
import modelengine.fit.waterflow.domain.enums.FlowNodeStatus;
import modelengine.fit.waterflow.domain.enums.FlowTraceStatus;
import modelengine.fit.waterflow.domain.enums.ParallelMode;
import modelengine.fit.waterflow.domain.enums.SpecialDisplayNode;
import modelengine.fit.waterflow.domain.states.DataStart;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.stream.reactive.Processor;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fit.waterflow.domain.stream.reactive.Subscriber;
import modelengine.fit.waterflow.domain.stream.reactive.Subscription;
import modelengine.fit.waterflow.domain.stream.reactive.When;
import modelengine.fit.waterflow.domain.utils.IdGenerator;
import modelengine.fit.waterflow.domain.utils.UUIDUtil;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.ObjectUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 数据发送者
 * offer到数据后就通过when将数据推给接受者
 * streamId第一次由from自动生成
 * streamId后续恢复执行时由用户传入
 *
 * @author 高诗意
 * @since 2023/08/14
 */
public class From<I> extends IdGenerator implements Publisher<I> {
    /**
     * contextRepo
     */
    protected final FlowContextRepo repo;

    /**
     * messenger
     */
    protected final FlowContextMessenger messenger;

    /**
     * locks
     */
    protected final FlowLocks locks;

    private final List<Subscription<I>> whens = new ArrayList<>();

    private final String streamId;

    public From(FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
        this(null, repo, messenger, locks);
        // 单纯的from的id就是stream id，因为单纯的from是数据的起点，其他都是subscriber
    }

    public From(String streamId, FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
        this.streamId = streamId != null && !"".equals(streamId.trim()) ? streamId : this.id;
        this.repo = repo;
        this.messenger = messenger;
        this.locks = locks;
    }

    /**
     * From
     *
     * @param streamId streamId
     * @param nodeId nodeId
     * @param repo contextRepo
     * @param messenger messenger
     * @param locks 流程锁
     */
    public From(String streamId, String nodeId, FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
        this(streamId, repo, messenger, locks);
        this.id = nodeId;
    }

    /**
     * 数据发送给条件processor
     *
     * @param whether whether
     * @return 新的条件processor
     */
    @Override
    public Processor<I, I> conditions(Operators.Whether<I> whether) {
        ConditionsNode<I> node = new ConditionsNode<>(this.getStreamId(), repo, messenger, locks);
        this.subscribe(node, whether);
        return node.displayAs(SpecialDisplayNode.CONDITION.name());
    }

    /**
     * 数据发送给平行（广播式）processor
     *
     * @param mode either还是all
     * @param whether whether
     * @return 新的条件processor
     */
    @Override
    public Processor<I, I> parallel(ParallelMode mode, Operators.Whether<I> whether) {
        ParallelNode<I> node = new ParallelNode<>(this.getStreamId(), mode, repo, messenger, locks);
        this.subscribe(node, whether);
        return node.displayAs(SpecialDisplayNode.PARALLEL.name());
    }

    /**
     * 数据发送给join processor
     * 这里的限制不够，要在上层做更多的限制，只有parallel后面的节点才可以join
     *
     * @param processor map处理器
     * @param whether whether
     * @return 新的join processor
     */
    @Override
    public <O> Processor<I, O> join(Operators.Map<FlowContext<I>, O> processor, Operators.Whether<I> whether) {
        JoinNode<I, O> node = new JoinNode<>(this.getStreamId(), processor, repo, messenger, locks);
        this.subscribe(node, i -> true);
        return node.displayAs(SpecialDisplayNode.JOIN.name());
    }

    /**
     * 数据发送给 just processor，数据只处理，不转换
     *
     * @param processor just处理器
     * @param whether whether
     * @return 新的join processor
     */
    @Override
    public Processor<I, I> just(Operators.Just<FlowContext<I>> processor, Operators.Whether<I> whether) {
        // just的实现就是一个返回自己的map
        Operators.Map<FlowContext<I>, I> wrapper = input -> {
            processor.process(input);
            return input.getData();
        };
        return this.map(wrapper, whether).displayAs("just");
    }

    /**
     * 接收者是1:1数据处理：进1条A数据，返回一条B数据
     *
     * @param processor 处理函数
     * @param whether whether
     * @return 返回一个node，这个node是接收者，同时可以继续发送数据
     */
    @Override
    public <O> Processor<I, O> map(Operators.Map<FlowContext<I>, O> processor, Operators.Whether<I> whether) {
        Node<I, O> node = new Node<>(this.getStreamId(), processor, repo, messenger, locks);
        this.subscribe(node, whether);
        return node.displayAs("map");
    }

    @Override
    public <O> Processor<I, O> flatMap(Operators.FlatMap<FlowContext<I>, O> processor, Operators.Whether<I> whether) {
        Validation.notNull(processor, "Flat map processor can not be null.");
        AtomicReference<Node<I, O>> processRef = new AtomicReference<>();
        Operators.Map<FlowContext<I>, O> wrapper = input -> {
            FlatMapSourceWindow fWindow = FlatMapSourceWindow.from(this.streamId, input.getWindow(), this.repo);

            FlatMapWindow flatMapWindow = new FlatMapWindow(fWindow);
            final FlowSession session = FlowSession.from(input.getSession(), flatMapWindow);
            session.begin();

            DataStart<O, O, ?> start = processor.process(input);

            FlowSession startSession = new FlowSession(input.getSession().preserved());
            flatMapWindow.setSource(startSession.begin());
            startSession.onError(exception -> {
                processRef.get().fail(exception, Collections.singletonList(input));
            });
            start.just(data -> {
                processRef.get().offer(data, session);
            }).offer(startSession);
            return null;
        };
        Node<I, O> node = new FlatMapNode<>(this.getStreamId(), wrapper, repo, messenger, locks);
        processRef.set(node);
        this.subscribe(node, whether);
        return node.displayAs("flat map");
    }

    @Override
    public <O> Processor<I, O> process(Operators.Process<FlowContext<I>, O> processor, Operators.Whether<I> whether) {
        AtomicReference<Node<I, O>> processRef = new AtomicReference<>();
        Operators.Map<FlowContext<I>, O> wrapper = input -> {
            FlowSession nextSession = FlowSessionRepo.getNextToSession(this.streamId, input.getSession());
            processor.process(input, input, data -> processRef.get().offer(data, nextSession));
            if (input.getSession().getWindow().isOngoing()) {
                nextSession.getWindow().complete();
            }
            return null;
        };
        Node<I, O> node = new Node<>(this.getStreamId(), wrapper, repo, messenger, locks);
        processRef.set(node);
        this.subscribe(node, whether);
        return node;
    }

    /**
     * 指定session来offer数据
     *
     * @param data 待offer的数据
     * @param session 指定的session
     * @return traceId
     */
    public String offer(I data, FlowSession session) {
        I[] array = ObjectUtils.cast(new Object[1]);
        array[0] = data;
        return this.offer(array, session);
    }

    /**
     * 指定trans来offer数据
     *
     * @param data 待offer的数据
     * @param session 指定的session
     * @return traceId
     */
    public String offer(I[] data, FlowSession session) {
        FlowTrace trace = new FlowTrace();
        Set<String> traceId = new HashSet<>();
        traceId.add(trace.getId());
        Window window = session.begin();
        List<FlowContext<I>> contexts = Arrays.stream(data)
                .map(d -> {
                    FlowContext<I> context = new FlowContext<>(this.getStreamId(), this.getId(), d, traceId,
                            this.getId(), session);
                    window.createToken();
                    return context;
                })
                .collect(Collectors.toList());
        List<FlowContext<I>> after = this.startNodeMarkAsHandled(contexts, trace);
        after.forEach(this::generateIndex);
        this.offer(after);
        return trace.getId();
    }

    /**
     * 生成一个index
     *
     * @param context 给定的context
     */
    protected void generateIndex(FlowContext context) {

    }

    /**
     * publish单条数据
     * 外部使用：controller中的start flow有使用，且返回给了前端
     * 内部使用：全部是单元测试在使用
     *
     * @param data 单条数据
     * @return 流程事务ID
     */
    @Override
    public String offer(I data) {
        FlowSession session = new FlowSession();
        Window token = session.begin();
        String traceId = this.offer(data, session);
        token.complete();
        return traceId;
    }

    /**
     * publish 多条数据
     * 外部使用：暂无
     * 内部使用：全部是单元测试在使用
     *
     * @param data 多条数据
     * @return 流程事务ID
     */
    @Override
    public String offer(I... data) {
        FlowSession session = new FlowSession();
        Window token = session.begin();
        String trace = this.offer(data, session);
        token.complete();
        return trace;
    }

    /**
     * publish stream context，通常这些数据来源于上一个publisher
     * 这个数据可以来源于数据最开始，也可以是接受者处理完的数据
     *
     * @param contexts 数据上下文，里面有要处理的数据，还有其他流处理状态信息
     */
    @Override
    public void offer(List<FlowContext<I>> contexts) {
        if (CollectionUtils.isEmpty(contexts)) {
            return;
        }
        // 每一次offer新数据,atom是不一样的，但是context的streamId是一样的,flowTransId标记是否属于同一次流程运行实例
        if (contexts.stream().map(context -> context.getSession().getId()).distinct().count() != 1) {
            return;
        }

        FlowContext<I> firstContext = contexts.get(0); // 用第一个元素做同源判断
        List<Subscription<I>> qualifiedWhens = this.getSubscriptions();

        // 处理从A流程跳出到B流程,再从B流程调回A流程的指定节点并从该指定节点继续执行A的后续流程
        List<Subscription<I>> sourceWhens = this.getSubscriptions()
                .stream()
                .filter(when -> !firstContext.getStreamId().equals(this.getStreamId()) && when.getTo()
                        .getStreamId()
                        .equals(firstContext.getStreamId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sourceWhens)) {
            qualifiedWhens = sourceWhens;
        }

        // qualifiedWhens表示的与from节点连接的所有事件，条件节点符合条件的事件在这里筛选，在事件上处理需要下发的context
        qualifiedWhens.forEach(when -> when.cache(contexts.stream()
                .filter(context -> when.getWhether().is(context.getData()))
                .collect(Collectors.toList())));
    }

    /**
     * 是否有publisher目标
     * 用于stream闭环时将没有subscribed的publisher关闭到close subscriber
     *
     * @return 是否有
     */
    @Override
    public boolean subscribed() {
        return this.getSubscriptions().size() > 0;
    }

    @Override
    public List<Subscription<I>> getSubscriptions() {
        return new ArrayList<>(this.whens);
    }

    @Override
    public FlowContextRepo getFlowContextRepo() {
        return this.repo;
    }

    /**
     * publish到某subscriber
     * just,map,reduce,produce可以生成subscriber，这个是直接指定subscriber
     *
     * @param subscriber 订阅者
     */
    @Override
    public <O> void subscribe(Subscriber<I, O> subscriber) {
        this.subscribe(subscriber, any -> true);
    }

    /**
     * 确定了发送者的目标接收者是谁,默认只能publish给一个subscription
     * 通过when关联了发送者和接受者
     *
     * @param subscriber 订阅者
     * @param whether whether
     */
    @Override
    public <O> void subscribe(Subscriber<I, O> subscriber, Operators.Whether<I> whether) {
        // 默认只能将数据发给一个subscriber
        this.whens.add(new When<>(this.streamId, subscriber, whether, repo, messenger));
    }

    /**
     * publish到某subscriber
     * just,map,reduce,produce可以生成subscriber，这个是直接指定subscriber
     *
     * @param eventId 事件ID
     * @param subscriber 订阅者
     */
    @Override
    public <O> void subscribe(String eventId, Subscriber<I, O> subscriber) {
        this.subscribe(eventId, subscriber, i -> true);
    }

    /**
     * 确定了发送者的目标接收者是谁,默认只能publish给一个subscription
     * 通过when关联了发送者和接受者
     *
     * @param eventId 事件ID
     * @param subscriber 订阅者
     * @param whether whether
     */
    @Override
    public <O> void subscribe(String eventId, Subscriber<I, O> subscriber, Operators.Whether<I> whether) {
        this.whens.add(new When<>(this.streamId, eventId, subscriber, whether, repo, messenger));
    }

    @Override
    public String getStreamId() {
        return this.streamId;
    }

    /**
     * 通过边的ID获取下一个节点的block，人工节点阻塞在边上
     *
     * @param eventId 事件ID
     * @return block
     */
    public Blocks.Block<I> getBlock(String eventId) {
        ArrayDeque<Subscriber<?, ?>> nodesQueue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        for (Subscription<?> s : this.getSubscriptions()) {
            nodesQueue.addLast(s.getTo());
            visited.add(s.getTo().getId());
            if (s.getId().equals(eventId)) {
                return ObjectUtils.cast(s.getTo().block());
            }
        }

        while (!nodesQueue.isEmpty()) {
            Node<?, ?> curNode = ObjectUtils.cast(nodesQueue.removeFirst());
            for (Subscription<?> s : curNode.getSubscriptions()) {
                if (!visited.contains(s.getTo().getId())) {
                    nodesQueue.offer(s.getTo());
                    visited.add(s.getTo().getId());
                }
                if (s.getId().equals(eventId)) {
                    return ObjectUtils.cast(s.getTo().block());
                }
            }
        }
        throw new WaterflowException(FLOW_ENGINE_INVALID_MANUAL_TASK);
    }

    // 开始节点无需处理直接标记结束
    private List<FlowContext<I>> startNodeMarkAsHandled(List<FlowContext<I>> preList, FlowTrace trace) {
        String fromBatchId = UUIDUtil.uuid();
        String toBatchId = UUIDUtil.uuid();
        trace.setStartNode(this.getId());
        trace.setStreamId(this.streamId);
        trace.setStatus(FlowTraceStatus.RUNNING);
        preList.forEach(context -> {
            trace.getContextPool().add(context.getId());
            context.batchId(fromBatchId);
            context.toBatch(toBatchId);
            context.setStatus(FlowNodeStatus.ARCHIVED);
        });
        List<FlowContext<I>> afterList = preList.stream().map(pre -> {
            FlowContext<I> after = pre.generate(pre.getData(), pre.getPosition()).batchId(toBatchId);
            trace.getContextPool().add(after.getId());
            return after;
        }).collect(Collectors.toList());
        repo.save(trace, preList.get(0));
        repo.save(afterList);
        repo.save(preList);
        return afterList;
    }
}
