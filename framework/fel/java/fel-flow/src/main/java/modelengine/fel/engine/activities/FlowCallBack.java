/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities;

import modelengine.fel.engine.flows.Action;
import modelengine.fitframework.inspection.Validation;

import java.util.function.Consumer;

/**
 * 流程回调对象。
 *
 * @param <O> 表示流程输出数据类型。
 * @author 刘信宏
 * @since 2024-05-28
 */
public class FlowCallBack<O> {
    private final Consumer<O> consumeCb;
    private final Consumer<Throwable> errorCb;
    private final Action completedCb;

    /**
     * 使用构造器初始化 {@link FlowCallBack}。
     *
     * @param builder 表示 {@link FlowCallBack} 构造器的 {@link FlowCallBack.Builder}{@code <}{@link O}{@code >}。
     */
    public FlowCallBack(FlowCallBack.Builder<O> builder) {
        Validation.notNull(builder, "Flow callback builder can not be null.");
        this.consumeCb = builder.consumeCb;
        this.errorCb = builder.errorCb.andThen(__ -> builder.finallyCb.exec());
        this.completedCb = builder.completedCb.andThen(builder.finallyCb);
    }

    /**
     * 创建 {@link FlowCallBack} 的构造器。
     *
     * @param <T> 表示流程输出数据类型。
     * @return 表示 {@link FlowCallBack} 构造器的 {@link FlowCallBack.Builder}{@code <}{@link T}{@code >}。
     */
    public static <T> FlowCallBack.Builder<T> builder() {
        return new FlowCallBack.Builder<>();
    }

    /**
     * 获取空实现的回调对象。
     *
     * @param <T> 表示流程输出数据类型。
     * @return 表示流程回调对象的 {@link FlowCallBack}{@code <}{@link T}{@code >}。
     */
    public static <T> FlowCallBack<T> emptyCallBack() {
        return FlowCallBack.<T>builder().build();
    }

    /**
     * 获取数据消费回调。
     *
     * @return 表示数据消费回调的 {@link Consumer}{@code <}{@link O}{@code >}。
     */
    public Consumer<O> consumeCb() {
        return this.consumeCb;
    }

    /**
     * 获取异常处理回调。
     *
     * @return 表示异常处理回调的 {@link Consumer}{@code <}{@link Throwable}{@code >}。
     */
    public Consumer<Throwable> errorCb() {
        return this.errorCb;
    }

    /**
     * 获取流程结束回调。
     *
     * @return 表示流程结束回调的 {@link Action}。
     */
    public Action completedCb() {
        return this.completedCb;
    }

    /**
     * {@link FlowCallBack} 的构造器。
     *
     * @param <O> 表示流程输出数据类型。
     */
    public static class Builder<O> {
        private Consumer<O> consumeCb = EmptyCallBack.doNothingOnConsume();
        private Consumer<Throwable> errorCb = EmptyCallBack.doNothingOnError();
        private Action completedCb = EmptyCallBack.doNothingAction();
        private Action finallyCb = EmptyCallBack.doNothingAction();

        /**
         * 设置数据消费回调。
         *
         * @param consumeCb 表示流程消费一次数据后操作的 {@link Consumer}{@code <}{@link O}{@code >}。
         * @return 表示 {@link FlowCallBack} 构造器的 {@link FlowCallBack.Builder}{@code <}{@link O}{@code >}。
         * @throws IllegalArgumentException 当 {@code consumeCb} 为 {@code null} 时。
         */
        public Builder<O> doOnConsume(Consumer<O> consumeCb) {
            this.consumeCb = Validation.notNull(consumeCb, "Success processor can not be null.");
            return this;
        }

        /**
         * 设置异常回调。
         *
         * @param errorCb 表示流程异常后操作的 {@link Consumer}{@code <}{@link Throwable}{@code >}。
         * @return 表示 {@link FlowCallBack} 构造器的 {@link FlowCallBack.Builder}{@code <}{@link O}{@code >}。
         * @throws IllegalArgumentException 当 {@code errorCb} 为 {@code null} 时。
         */
        public Builder<O> doOnError(Consumer<Throwable> errorCb) {
            this.errorCb = Validation.notNull(errorCb, "error handler can not be null.");
            return this;
        }

        /**
         * 设置成功结束回调，流程成功结束时会触发该回调。
         *
         * @param completedCb 表示流程成功结束后操作的 {@link Action}。
         * @return 表示 {@link FlowCallBack} 构造器的 {@link FlowCallBack.Builder}{@code <}{@link O}{@code >}。
         * @throws IllegalArgumentException 当 {@code finallyCb} 为 {@code null} 时。
         */
        public Builder<O> doOnCompleted(Action completedCb) {
            this.completedCb = Validation.notNull(completedCb, "The completed action can not be null.");
            return this;
        }

        /**
         * 设置结束回调，流程成功结束或异常时都会触发该回调。
         *
         * @param finallyCb 表示流程结束后操作的 {@link Action}。
         * @return 表示 {@link FlowCallBack} 构造器的 {@link FlowCallBack.Builder}{@code <}{@link O}{@code >}。
         * @throws IllegalArgumentException 当 {@code finallyCb} 为 {@code null} 时。
         */
        public Builder<O> doOnFinally(Action finallyCb) {
            this.finallyCb = Validation.notNull(finallyCb, "Finally action can not be null.");
            return this;
        }

        /**
         * 构造 {@link FlowCallBack}。
         *
         * @return 表示流程回调对象的 {@link FlowCallBack}{@code <}{@link O}{@code >}。
         */
        public FlowCallBack<O> build() {
            return new FlowCallBack<>(this);
        }
    }

    private static class EmptyCallBack {
        static <R> Consumer<R> doNothingOnConsume() {
            return input -> {};
        }

        static Consumer<Throwable> doNothingOnError() {
            return exception -> {};
        }

        static Action doNothingAction() {
            return () -> {};
        }
    }
}
