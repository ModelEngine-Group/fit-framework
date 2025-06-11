/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import lombok.Getter;
import lombok.Setter;
import modelengine.fit.waterflow.domain.enums.FlowNodeStatus;
import modelengine.fit.waterflow.domain.enums.ParallelMode;
import modelengine.fit.waterflow.domain.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 流里承载数据上下文。
 *
 * @since 1.0
 */
public class FlowContext<T> extends IdGenerator implements StateContext {
    /**
     * 通过 from.offer(data) 而不是 .offer(context) 发起的数据会新增一个路径，这个路径会延续到流的终点。
     */
    @Getter
    private final Set<String> traceId;

    /**
     * 数据发起的起始节点上：任何一个 Publisher 都可以发起数据，不一定是流的起始节点。
     * 只要通过 from.offer(data) 而不是 .offer(context) 的都是数据发起节点。
     */
    @Getter
    private final String rootId;

    /**
     * 得到所处流的唯一标识。
     */
    @Getter
    private final String streamId;

    /**
     * 上下文里带的数据，这是关键属性。
     */
    @Getter
    @Setter
    private T data;

    /**
     * 上下文当前所处的位置：处理节点上。
     */
    @Getter
    private String position;

    /**
     * 如果是并行节点，标识是否合并。
     */
    @Getter
    private boolean joined;

    /**
     * 上下文当前的状态值。
     */
    @Getter
    private FlowNodeStatus status = FlowNodeStatus.NEW;

    /**
     * 上下文所有的 transaction。
     * 当数据是一个数据的时候，这个数组在一个 FlowTransId 里，在 reduce，produce 时默认统一处理。
     */
    @Getter
    @Setter
    private FlowSession session;

    /**
     * 上下文所处的并行节点唯一标识：如果该上下文在并行节点才有效。
     */
    @Getter
    private String parallel;

    /**
     * 所处并行节点模式，是 either 还是 all。
     */
    @Getter
    private String parallelMode;

    /**
     * 来源于哪个 context。
     */
    @Getter
    @Setter
    private String previous;

    /**
     * context 的生产批次唯一标识。
     */
    @Getter
    protected String batchId;

    /**
     * 转向哪个 context 批次。
     */
    @Getter
    private String toBatch;

    /**
     * 标记该 context 是否已经发出事件。
     */
    @Getter
    @Setter
    private boolean sent;

    /**
     * 当前 context 创建时间。
     */
    @Getter
    @Setter
    private LocalDateTime createAt;

    /**
     * 当前 context 更新时间。
     */
    @Getter
    @Setter
    private LocalDateTime updateAt;

    /**
     * 当前 context 处理完成时间。
     */
    @Getter
    @Setter
    private LocalDateTime archivedAt;

    /**
     * 保序id,-1代表不需要保序
     */
    @Getter
    @Setter
    private Integer index;

    /**
     * 创建一个 {@link FlowContext} 实例。
     *
     * @param streamId 表示所处流唯一标识的 {@link String}。
     * @param rootId 表示数据发起的起始节点唯一标识的 {@link String}。
     * @param data 表示上下文里所带数据的 {@link T}。
     * @param traceId 表示路径唯一标识的 {@link Set}{@code <}的{@link String}{@code >}。
     * @param position 表示上下文当前所处的位置的 {@link String}。
     * @param session 表示上下文会话信息的 {@link FlowSession}。
     */
    public FlowContext(String streamId, String rootId, T data, Set<String> traceId, String position,
            FlowSession session) {
        this(streamId, rootId, data, traceId, position, "", "", session);
    }

    /**
     * 创建一个 {@link FlowContext} 实例。
     *
     * @param streamId 表示所处流唯一标识的 {@link String}。
     * @param rootId 表示数据发起的起始节点唯一标识的 {@link String}。
     * @param data 表示上下文里所带数据的 {@link T}。
     * @param traceId 表示路径唯一标识的 {@link Set}{@code <}的{@link String}{@code >}。
     * @param position 表示上下文当前所处的位置的 {@link String}。
     * @param parallel 表示并行节点唯一标识的 {@link String}。
     * @param parallelMode 表示并行模式的 {@link String}。
     * @param session 表示上下文会话信息的 {@link FlowSession}。
     */
    public FlowContext(String streamId, String rootId, T data, Set<String> traceId, String position, String parallel,
            String parallelMode, FlowSession session) {
        this.streamId = streamId;
        this.rootId = rootId;
        this.data = data;
        this.traceId = new HashSet<>();
        this.traceId.addAll(traceId);
        this.position = position;
        this.parallel = parallel;
        this.parallelMode = parallelMode;
        this.createAt = LocalDateTime.now();
        this.session = session;
        this.index = this.createIndex(); // 0起始，说明保序
    }

    private Integer createIndex() {
        return session.preserved() ? session.getWindow().tokenCount() : -1;
    }

    /**
     * 设置所在的位置，线或者节点的唯一标识，链式操作。
     *
     * @param position 表示所在的位置的 {@link String}。
     * @return 表示 context 自身的 {@link FlowContext}{@code <}{@link T}{@code >}。
     */
    public FlowContext<T> setPosition(String position) {
        this.position = position;
        return this;
    }

    /**
     * 设置节点状态，链式操作。
     * <p>
     * 参考{@link FlowNodeStatus}
     * </p>
     *
     * @param status 表示节点状态的 {@link FlowNodeStatus}。
     * @return 表示 context 自身的 {@link FlowContext}{@code <}{@link T}{@code >}。
     */
    public FlowContext<T> setStatus(FlowNodeStatus status) {
        this.status = status;
        return this;
    }

    /**
     * 设置并行节点的唯一标识，链式操作。
     *
     * @param parallel 表示并行节点唯一标识的 {@link String}。
     * @return 表示 context 自身的 {@link FlowContext}{@code <}{@link T}{@code >}。
     */
    public FlowContext<T> setParallel(String parallel) {
        this.parallel = parallel;
        return this;
    }

    /**
     * 设置并行的模式，链式操作。
     * <p>
     * 参考{@link ParallelMode}
     * </p>
     *
     * @param parallelMode 表示并行模式的 {@link String}。
     * @return 表示 context 自身的 {@link FlowContext}{@code <}{@link T}{@code >}。
     */
    public FlowContext<T> setParallelMode(String parallelMode) {
        this.parallelMode = parallelMode;
        return this;
    }

    /**
     * 设置批次唯一标识，链式操作。
     *
     * @param batchId 表示批次唯一标识的 {@link String}。
     * @return 表示 context 自身的 {@link FlowContext}{@code <}{@link T}{@code >}。
     */
    public FlowContext<T> batchId(String batchId) {
        this.batchId = batchId;
        return this;
    }

    /**
     * 设置 toBatch 的唯一标识，链式操作。
     *
     * @param toBatchId 表示 toBatchId 的 {@link String}。
     * @return 表示 context 自身的 {@link FlowContext}{@code <}{@link T}{@code >}。
     */
    public FlowContext<T> toBatch(String toBatchId) {
        this.toBatch = toBatchId;
        return this;
    }

    /**
     * 在 map，reduce，produce 的过程中把大多数上一个 context 的内容复制给下一个。
     *
     * @param <R> 表示返回值类型的泛型参数。
     * @param data 表示处理后数据的 {@link R}。
     * @param position 表示处理后所处的节点的 {@link String}。
     * @return 表示新的上下文的 {@link FlowContext}{@code <}{@link R}{@code >}。
     */
    public <R> FlowContext<R> generate(R data, String position) {
        FlowContext<R> context = new FlowContext<>(this.streamId,
                this.rootId,
                data,
                this.traceId,
                this.position,
                this.parallel,
                this.parallelMode,
                this.session);
        context.position = position;
        context.previous = this.id;
        context.batchId = this.batchId;
        context.index = this.index;
        return context;
    }

    /**
     * 在 map，reduce，produce 的过程中把大多数上一个 context 的内容复制给下一个。
     *
     * @param <R> 表示返回值类型的泛型参数。
     * @param dataList 表示处理后数据的 {@link List}{@code <}{@link R}{@code >}。
     * @param position 表示处理后所处节点的 {@link String}。
     * @return 表示新的上下文的 {@link List}{@code <}{@link FlowContext}{@code <}{@link R}{@code >}{@code >}。
     */
    public <R> List<FlowContext<R>> generate(List<R> dataList, String position) {
        return dataList.stream().map(data -> this.generate(data, position)).collect(Collectors.toList());
    }

    /**
     * 用于 when.convert 数据时候的转换 context，除了包裹的数据类型不一样，所有其他信息都一样。
     *
     * @param <R> 表示返回值类型的泛型参数。
     * @param data 表示转换后数据的 {@link R}。
     * @param id 表示 contextId 的 {@link String}。
     * @return 表示转换后的 context 的 {@link FlowContext}{@code <}{@link R}{@code >}。
     */
    public <R> FlowContext<R> convertData(R data, String id) {
        FlowContext<R> context = new FlowContext<>(this.streamId,
                this.rootId,
                data,
                this.traceId,
                this.position,
                this.parallel,
                this.parallelMode,
                this.session);
        context.previous = this.previous;
        context.status = this.status;
        context.id = id;
        context.batchId = this.batchId;
        context.toBatch = this.toBatch;
        context.createAt = this.createAt;
        context.updateAt = this.updateAt;
        context.archivedAt = this.archivedAt;
        context.index = this.index;
        return context;
    }

    /**
     * 合并节点，将 context 的状态设置为 joined。
     *
     * @param joined 表示是否合并的 {@link Boolean}。
     */
    public void join(boolean joined) {
        this.joined = joined;
    }

    @Override
    public <R> R getState(String key) {
        return this.session.getState(key);
    }

    @Override
    public void setState(String key, Object value) {
        this.session.setState(key, value);
    }

    /**
     * 获取keyBy
     *
     * @return 表示返回值的 {@link Object}。
     */
    public Object keyBy() {
        return this.session.keyBy();
    }

    /**
     * 获取window
     *
     * @return 窗口
     */
    public Window getWindow() {
        return this.getSession().getWindow();
    }
}
