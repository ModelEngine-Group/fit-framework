/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.emitters;

import modelengine.fit.waterflow.domain.common.Constants;
import modelengine.fit.waterflow.domain.context.FlowSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 流程数据发布器
 *
 * @param <D> 数据类型
 * @since 1.0
 */
public class FlowEmitter<D> implements Emitter<D, FlowSession> {
    /**
     * Emitter的监听器
     */
    protected Set<EmitterListener<D, FlowSession>> listeners = new LinkedHashSet<>();

    /**
     * 关联的 session 信息
     */
    protected FlowSession flowSession;

    /**
     * 启动发射器后才能发射数据
     */
    private boolean isStart = false;

    /**
     * 标识完成状态，完成后才能关闭窗口
     */
    private boolean isComplete = false;

    private final List<D> data = new ArrayList<>();

    /**
     * 构造空数据的发射器，具体数据由用户自己投递。
     */
    public FlowEmitter() {}

    /**
     * 构造单个数据的Emitter
     *
     * @param data 单个数据
     */
    protected FlowEmitter(D data) {
        this.data.add(data);
    }

    /**
     * 构造一组数据的Emitter
     *
     * @param data 一组数据
     */
    protected FlowEmitter(D... data) {
        this.data.addAll(Arrays.asList(data));
    }

    /**
     * 构造一个mono类型的发布器
     *
     * @param data 待发布的批量数据
     * @param <I> 待发布的数据类型
     * @return 构造一个数据发布器
     */
    public static <I> FlowEmitter<I> mono(I data) {
        return new FlowEmitter<>(data);
    }

    /**
     * 构造一个flux类型的发布器
     *
     * @param data 待发布的批量数据
     * @param <I> 待发布的数据类型
     * @return 构造一个批量数据发布器
     */
    public static <I> FlowEmitter<I> flux(I... data) {
        return new FlowEmitter<>(data);
    }

    /**
     * 从已有的发射器创建一个新的发射器
     *
     * @param emitter 已有的发射器
     * @param <I> 待发布的数据类型
     * @return 新的发射器
     */
    public static <I> FlowEmitter<I> from(Emitter<I, FlowSession> emitter) {
        FlowEmitter<I> cachedEmitter = new AutoCompleteEmitter<>();
        emitter.register(cachedEmitter::emit);
        return cachedEmitter;
    }

    @Override
    public synchronized void register(EmitterListener<D, FlowSession> listener) {
        if (listener == null) {
            return;
        }
        this.listeners.add(listener);
        if (this.isStart) {
            this.fire();
        }
    }

    @Override
    public synchronized void unregister(EmitterListener<D, FlowSession> listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public synchronized void emit(D data, FlowSession trans) {
        if (!this.isStart) {
            this.data.add(data);
            return;
        }
        this.listeners.forEach(listener -> listener.handle(data, this.flowSession));
    }

    @Override
    public synchronized void start(FlowSession session) {
        if (this.isStart) {
            return;
        }
        if (session != null) {
            session.begin();
        }
        this.flowSession = session;
        this.isStart = true;
        this.fire();
        this.isComplete = true;
        this.tryCompleteWindow();
    }

    @Override
    public synchronized void complete() {
        this.isComplete = true;
        this.tryCompleteWindow();
    }

    /**
     * 设置开始。
     */
    protected void setStarted() {
        this.isStart = true;
    }

    /**
     * 查询是否完成。
     *
     * @return true-完成, false-未完成
     */
    protected boolean isComplete() {
        return this.isComplete;
    }

    /**
     * 设置关联的 session。
     *
     * @param flowSession 关联的session
     */
    protected void setFlowSession(FlowSession flowSession) {
        this.flowSession = flowSession;
    }

    /**
     * 发射缓存的数据。
     */
    protected void fire() {
        for (D d : this.data) {
            this.listeners.forEach(listener -> listener.handle(d,
                    (this.flowSession == null || this.flowSession.getId().equals(Constants.FROM_FLATMAP))
                            ? null
                            : this.flowSession));
        }
        this.data.clear();
    }

    /**
     * 尝试完成对应的 window。
     */
    protected void tryCompleteWindow() {
        if (this.flowSession == null) {
            return;
        }
        if (this.isStart && this.isComplete) {
            this.flowSession.getWindow().complete();
        }
    }

    /**
     * An emitter implementation that automatically completes based on emission conditions.
     * This emitter subclass handles automatic completion logic when certain emission
     * criteria are met, reducing the need for manual completion management.
     *
     * @param <D> the type of data processed by this emitter.
     */
    public static class AutoCompleteEmitter<D> extends FlowEmitter<D> {
        @Override
        public synchronized void start(FlowSession session) {
            if (session != null) {
                session.begin();
            }
            this.setFlowSession(session);
            this.setStarted();
            this.fire();
        }

        @Override
        public synchronized void emit(D data, FlowSession session) {
            session.getWindow().onDone(getOnDoneHandlerId(session), this::complete);
            this.listeners.forEach(listener -> listener.handle(data, this.flowSession));
        }

        private static String getOnDoneHandlerId(FlowSession session) {
            return "AutoCompleteEmitter" + session.getWindow().id();
        }
    }
}
