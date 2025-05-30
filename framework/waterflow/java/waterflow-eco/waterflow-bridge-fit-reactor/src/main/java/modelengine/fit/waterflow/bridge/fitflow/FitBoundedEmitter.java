/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.bridge.fitflow;

import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.FlowEmitter;
import modelengine.fitframework.flowable.Publisher;
import modelengine.fitframework.flowable.Subscriber;
import modelengine.fitframework.flowable.Subscription;

import java.util.function.Function;

/**
 * 用于对接FIT的有限流的发射源。
 *
 * @author xiafei
 * @since 2024/8/16
 */
public abstract class FitBoundedEmitter<O, D> extends FlowEmitter<D> {
    private final Function<O, D> dataConverter;

    private boolean isError = false;
    private Exception exception;
    private Publisher<O> publisher;

    /**
     * 通过数据发布者和有限流数据构造器初始化 {@link FitBoundedEmitter}{@code <}{@link O}{@code , }{@link D}{@code >}。
     *
     * @param publisher 表示数据发布者的 {@link Publisher}{@code <}{@link O}{@code >}。
     * @param dataConverter 表示用于数据类型转换的 {@link Function}{@code <}{@link O}{@code >}。
     */
    public FitBoundedEmitter(Publisher<O> publisher, Function<O, D> dataConverter) {
        this.dataConverter = dataConverter;
        this.publisher = publisher;
    }

    @Override
    public synchronized void start(FlowSession session) {
        if (session != null) {
            session.begin();
        }
        this.setFlowSession(session);
        this.setStarted();
        if (this.isError) {
            this.notifyError();
            return;
        }
        // 启动时先发射缓存的数据，此时可能先缓存了数据，所以开始时发射完数据就可能结束了。
        this.fire();
        this.tryCompleteWindow();
        this.publisher.subscribe(new EmitterSubscriber<>(this));
    }

    private void doEmit(D data) {
        this.emit(data, this.flowSession);
    }

    private void doComplete() {
        this.complete();
    }

    private synchronized void doError(Exception cause) {
        if (this.isError) {
            return;
        }
        this.isError = true;
        this.exception = cause;
        this.notifyError();
    }

    private void notifyError() {
        if (this.flowSession != null) {
            this.flowSession.fail(this.exception);
        }
    }

    /**
     * 订阅FIT响应式数据流的数据发射器
     *
     * @param <O> FIT响应式数据流中的数据类型
     * @param <D> 转换后的数据类型
     */
    private static class EmitterSubscriber<O, D> implements Subscriber<O> {
        private final FitBoundedEmitter<O, D> emitter;

        /**
         * 初始化订阅者。
         *
         * @param emitter 表示有限流的发射源的 {@link FitBoundedEmitter}{@code <}{@link O}{@code , }{@link D}{@code >}。
         */
        public EmitterSubscriber(FitBoundedEmitter<O, D> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onSubscribed(Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void consume(O source) {
            D target = this.emitter.dataConverter.apply(source);
            this.emitter.doEmit(target);
        }

        @Override
        public void complete() {
            this.emitter.doComplete();
        }

        @Override
        public boolean isCompleted() {
            return this.emitter.isComplete();
        }

        @Override
        public void fail(Exception cause) {
            this.emitter.doError(cause);
        }

        @Override
        public boolean isFailed() {
            return this.emitter.isError;
        }
    }
}
