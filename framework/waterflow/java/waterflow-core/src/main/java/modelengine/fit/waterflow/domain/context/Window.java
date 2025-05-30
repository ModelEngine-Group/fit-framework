/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import lombok.Getter;
import lombok.Setter;
import modelengine.fit.waterflow.domain.emitters.Completable;
import modelengine.fit.waterflow.domain.stream.nodes.To;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.stream.operators.WindowArg;
import modelengine.fit.waterflow.domain.stream.reactive.Processor;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * window与session相关，在没有keyby关键字介入情况下，一个window对应一个session
 * window在reduce操作中作用，确保了reduce操作的边界
 * window对reduce聚合操作由两个因素因素决定：windowcondition和session边界
 * 满足一次windowcondition，一次聚合操作完成
 * 满足session边界，最后一次觉和操作完成
 *
 * @author 宋永坦
 * @since 1.0
 */
public class Window implements Completable {
    /**
     * window最后更新时间
     */
    protected AtomicReference<LocalDateTime> now = new AtomicReference<>(LocalDateTime.now());

    /**
     * 窗口的完成状态
     */
    protected AtomicBoolean isComplete = new AtomicBoolean(false);

    /**
     * from
     */
    @Getter
    protected Window from = null;

    private final UUID id;
    private final List<WindowToken> tokens = new ArrayList<>(16);
    @Getter
    private final Set<Window> tos = new CopyOnWriteArraySet<>();
    private final Map<String, Runnable> onDoneHandlers = new ConcurrentHashMap<>();

    private Boolean isFinished = false;
    /**
     * accumulator for reduce
     */
    @Setter
    private Object acc = null;

    @Getter
    private Operators.WindowCondition condition;

    private FlowSession session;

    private CompleteContext completeContext;

    private To node = null;

    public Window(Operators.WindowCondition condition, UUID id) {
        this.condition = condition;
        this.id = id;
    }

    public Window(Operators.WindowCondition condition) {
        this(condition, UUID.randomUUID());
    }

    public Window() {
        this(arg -> false);
    }

    /**
     * 待删除
     *
     * @return
     */
    public int getTosSize() {
        return tos.size();
    }

    /**
     * 确认window是否完成
     *
     * @return 完成状态
     */
    public synchronized boolean accept() {
        if (this.fulfilled()) {
            this.now.set(LocalDateTime.now());
            for (WindowToken token : this.tokens) {
                token.reduce();
            }
            return true;
        }
        return false;
    }

    /**
     * 获取id
     *
     * @return 得到id
     */
    public String id() {
        return this.id.toString();
    }

    /**
     * 是否window内满足聚合终止条件
     * 一个window可以根据window condition多次聚合
     * 最后一次聚合为session数据边界
     *
     * @return 是否到达
     */
    public boolean fulfilled() {
        WindowArg arg = new WindowArg(this.isComplete(), this.tokens.size(),
                this.tokens.stream().filter(t -> !t.initialized() && !t.isReduced()).count(),
                Duration.between(this.now.get(), LocalDateTime.now()));
        // consuming and consumed are all counted
        return this.condition.fulfilled(arg) || this.isOngoing();
    }

    /**
     * window是否完成
     *
     * @return window状态
     */
    public boolean isComplete() {
        return this.isComplete.get();
    }

    /**
     * 监听窗口完成事件。
     *
     * @param handlerId 表示监听者的唯一标识的 {@link String}。
     * @param handler 表示监听者接收处理的 {@link Runnable}。
     */
    public synchronized void onDone(String handlerId, Runnable handler) {
        synchronized (this) {
            if (!this.isDone()) {
                this.onDoneHandlers.put(handlerId, handler);
                return;
            }
        }
        handler.run();
    }

    /**
     * 获取顶层窗口。
     *
     * @return 表示顶层窗口的 {@link Window}。
     */
    public Window getRootWindow() {
        if (this.from == null) {
            return this;
        }
        return this.from.getRootWindow();
    }

    /**
     * 获取该窗口以及后续所有窗口是否全部结束。
     *
     * @return 表示该窗口以及后续所有窗口是否全部结束的 {@code boolean}。
     */
    public boolean isAllDone() {
        return this.isDone() && this.tos.stream().allMatch(Window::isAllDone);
    }

    /**
     * 创建window token
     *
     * @return window token
     */
    public synchronized WindowToken createToken() {
        WindowToken token = new WindowToken(this);
        this.tokens.add(token);
        return token;
    }

    /**
     * 添加一个token
     *
     * @param source window token
     * @return window token
     */
    public synchronized WindowToken acceptToken(WindowToken source) {
        WindowToken token = this.createToken();
        if (source != null) {
            source.finishConsume();
            source.accepted();
        }
        return token;
    }

    /**
     * 为后续流变换的保序index要预留处理
     *
     * @param context 运行上下文信息
     * @param publisher 数据发布者
     */
    public void generateIndex(FlowContext context, Publisher publisher) {
    }

    @Override
    public void complete() {
        synchronized (this) {
            if (this.isComplete()) {
                return;
            }
            this.isComplete.set(true);
        }
        this.fire();
        this.tryFinish();
    }

    private void fire() {
        // only when all elements are consumed(done), fire the possible reduce
        if (completeContext != null && (session.isAccumulator() || this.acc != null) && this.isDone()) {
            List<FlowContext<Object>> cs = new ArrayList<>();
            cs.add(completeContext);
            List contexts = node.getProcessMode().process(node, cs);
            if (node instanceof Processor) {
                ((Processor<?, ?>) node).offer(contexts);
            }
            completeContext = null;
        }
    }

    private void completed() {
        for (Window listener : this.tos) {
            listener.complete();
        }
    }

    /**
     * session中所有数据都消费结束
     *
     * @return 是否消费完成
     */
    public synchronized boolean isDone() {
        return this.isComplete.get() && !this.tokens.stream().anyMatch(t -> !t.isConsumed());
    }

    /**
     * session中所有数据都消费完了，只有一个数据正在消费中
     * 该方法在process时发挥作用，此时正在消费最后一个数据
     *
     * @return 是否在消费最后一个数据
     */
    public synchronized boolean isOngoing() {
        boolean ongoing = this.isComplete.get() && !this.tokens.stream().anyMatch(t -> t.initialized())
                && this.tokens.stream().filter(t -> t.isConsuming()).count() <= 1;
        return ongoing || isDone();
    }

    /**
     * 添加window
     *
     * @param toWindow window
     */
    public void addTo(Window toWindow) {
        this.tos.add(toWindow);
    }

    /**
     * 添加window满足条件
     *
     * @param condition window条件
     */
    public void setCondition(Operators.WindowCondition condition) {
        this.condition = condition;
    }

    /**
     * 聚合结果
     *
     * @return 聚合结果
     */
    public Object acc() {
        return this.acc;
    }

    /**
     * 设置完成时的钩子
     *
     * @param to 处理节点
     * @param context 上下文数据
     * @param <T> 入参类型
     * @param <R> 出参类型
     */
    public <T, R> void setCompleteHook(To<T, R> to, FlowContext context) {
        this.node = to;
        this.completeContext = new CompleteContext(context, to.getId());
    }

    /**
     * if this session window is closed and all elements have been consumed, then notify listener stream that i'm totally consumed
     **/
    public void tryFinish() {
        synchronized (this) {
            if (this.isFinished) {
                return;
            }
            if (!this.isDone()) {
                return;
            }
            this.isFinished = true;
        }
        this.completed();
        this.onDoneHandlers.values().forEach(Runnable::run);
    }

    /**
     * 获取token数量
     *
     * @return token数量
     */
    public Integer tokenCount() {
        return this.tokens.size();
    }

    /**
     * 待删除
     *
     * @return
     */
    public synchronized String debugTokens() {
        return this.tokens.hashCode() + "-" + this.tokens.stream()
                .map(t -> t.getStatus().name() + "-" + t.hashCode())
                .collect(Collectors.joining("|"));
    }

    /**
     * 获取一个闲置token,标志为cusuming
     * token全部消费完才标志该session在该节点complete
     *
     * @return 闲置token
     */
    public synchronized WindowToken peekAndConsume() {
        Optional<WindowToken> peeked = this.tokens.stream().filter(WindowToken::initialized).findFirst();
        if (peeked.isPresent()) {
            peeked.get().beginConsume();
            return peeked.get();
        }
        return null;
    }

    /**
     * 获取session
     *
     * @return 当前的session
     */
    public FlowSession getSession() {
        return this.session;
    }

    /**
     * 设置关联session
     * 该操作在session.setWindow时发生，确保了session与window的一一对应关系
     *
     * @param session 关联的session
     */
    public void setSession(FlowSession session) {
        this.session = session;
        if (session.getWindow() != this) {
            session.setWindow(this);
        }
    }

    /**
     * 同session产生的flatmap在下一个节点归纳为同一window
     * 即其flatmap source window
     *
     * @return 窗口唯一标识
     */
    public UUID key() {
        return this.id;
    }

    /**
     * 设置from window
     *
     * @param fromWindow from window
     */
    public void setFrom(Window fromWindow) {
        this.from = fromWindow;
    }

    /**
     * 重置聚合结果
     */
    public void resetAcc() {
        this.acc = null;
    }
}


