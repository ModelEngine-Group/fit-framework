/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import modelengine.fitframework.log.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 流程调用的同步器。
 *
 * @since 2024-04-10
 */
public class ConverseLatch<T> {
    private static final Logger log = Logger.get(ConverseLatch.class);

    private T data = null;
    private Throwable throwable = null;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 触发 {@link CountDownLatch#countDown()} 尝试唤醒阻塞线程。
     */
    public void countDown() {
        this.countDownLatch.countDown();
    }

    /**
     * 阻塞当前线程，直到闩锁计数减为零，除非线程被中断，或者已超过指定的等待时间。
     *
     * @param timeout 表示等待超时时间的 {@code long}。
     * @param unit 表示等待超时时间单位的 {@link TimeUnit}。
     * @return 表示对话返回数据的 {@link T}。
     * @throws IllegalStateException 当流程流转过程中状态异常、阻塞等待超时或当前线程在进入此方法时设置了其中断状态，或者在等待时被中断。
     */
    public T await(long timeout, TimeUnit unit) {
        try {
            if (!this.countDownLatch.await(timeout, unit)) {
                throw new IllegalStateException("conversation timeout");
            }
        } catch (InterruptedException exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
        if (this.throwable != null) {
            log.error("Conversation latch await throws: ", this.throwable);
            throw new IllegalStateException(this.throwable.getMessage(), this.throwable);
        }
        return this.data;
    }

    /**
     * 阻塞当前线程，直到闩锁计数减为零，除非线程被中断。
     *
     * @return 表示对话返回数据的 {@link T}。
     * @throws IllegalStateException 当流程流转过程中状态异常、阻塞等待超时或当前线程在进入此方法时设置了其中断状态，或者在等待时被中断。
     */
    public T await() {
        try {
            this.countDownLatch.await();
        } catch (InterruptedException exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
        if (this.throwable != null) {
            log.error("Conversation latch await throws: ", this.throwable);
            throw new IllegalStateException(this.throwable.getMessage(), this.throwable);
        }
        return this.data;
    }

    /**
     * Returns the data.
     *
     * @return The data.
     */
    public T data() {
        return this.data;
    }

    /**
     * Sets the data.
     *
     * @param data The data.
     */
    public void data(T data) {
        this.data = data;
    }

    /**
     * Returns the throwable.
     *
     * @return The throwable.
     */
    public Throwable throwable() {
        return this.throwable;
    }

    /**
     * Sets the throwable.
     *
     * @param throwable The throwable.
     */
    public void throwable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * Returns the count-down latch.
     *
     * @return The count-down latch.
     */
    public CountDownLatch countDownLatch() {
        return this.countDownLatch;
    }
}
