/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.flowable;

import modelengine.fitframework.flowable.emitter.DefaultEmitter;

/**
 * 表示数据的发送者。
 *
 * @param <T> 表示所发送的数据的类型的 {@link T}。
 * @author 季聿阶
 * @since 2024-02-13
 */
public interface Emitter<T> {
    /**
     * 发送一个指定的数据。
     *
     * @param data 表示所发送的数据的 {@link T}。
     */
    void emit(T data);

    /**
     * 发送一个正常终结信号。
     */
    void complete();

    /**
     * 发送一个异常终结信号。
     *
     * @param cause 表示所发送的异常终结的原因的 {@link Exception}。
     */
    void fail(Exception cause);

    /**
     * 添加一个观察者，用于观察数据发送者的一系列行为。
     *
     * @param observer 表示待添加的观察者的 {@link Observer}{@code <}{@link T}{@code >}。
     */
    void observe(Observer<T> observer);

    /**
     * 创建一个默认的数据发送器。
     *
     * @param <T> 表示数据发送器所发送数据类型的 {@link T}。
     * @return 表示创建出来的默认的数据发送器的 {@link Emitter}{@code <}{@link T}{@code >}。
     */
    static <T> Emitter<T> create() {
        return new DefaultEmitter<>();
    }

    /**
     * 表示 {@link Emitter} 的观察者。
     *
     * @param <T> 表示数据发送者发送数据类型的 {@link T}。
     */
    interface Observer<T> {
        /**
         * 当 {@link Emitter#emit(Object)} 方法被调用时触发的事件。
         *
         * @param data 表示发送的数据的 {@link T}。
         */
        void onEmittedData(T data);

        /**
         * 当 {@link Emitter#complete()} 方法被调用时触发的事件。
         */
        void onCompleted();

        /**
         * 当 {@link Emitter#fail(Exception)} 方法被调用时触发的事件。
         *
         * @param cause 表示失败原因的 {@link Exception}。
         */
        void onFailed(Exception cause);
    }
}
