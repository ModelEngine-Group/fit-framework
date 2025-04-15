/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.support;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fit.http.client.HttpClassicClientRequest;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.client.HttpClientResponseException;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.entity.TextEventStreamEntity;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.flowable.Subscriber;
import modelengine.fitframework.flowable.Subscription;
import modelengine.fitframework.flowable.choir.AbstractChoir;
import modelengine.fitframework.flowable.subscription.AbstractSubscription;
import modelengine.fitframework.flowable.util.worker.Worker;
import modelengine.fitframework.flowable.util.worker.WorkerObserver;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.util.LockUtils;
import modelengine.fitframework.util.ObjectUtils;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

/**
 * 表示 {@link Choir} 的事件流的实现。
 *
 * @author 季聿阶
 * @since 2024-08-17
 */
public class TextStreamChoir<T> extends AbstractChoir<T> implements Choir<T> {
    private final HttpClassicClientRequest request;
    private final Type responseType;

    /**
     * 使用指定的请求和响应类型初始化 {@link TextStreamChoir} 的新实例。
     *
     * @param request 表示请求的 {@link HttpClassicClientRequest}。
     * @param responseType 表示响应类型的 {@link Type}。
     */
    public TextStreamChoir(HttpClassicClientRequest request, Type responseType) {
        this.request = request;
        this.responseType = responseType;
    }

    @Override
    protected void subscribe0(@Nonnull Subscriber<T> subscriber) {
        subscriber.onSubscribed(new TextStreamSubscription<>(subscriber, this.request, this.responseType));
    }

    private static class TextStreamSubscription<T> extends AbstractSubscription implements WorkerObserver<T> {
        private static final int HTTP_SUCCESS_CODE_MIN = 200;
        private static final int HTTP_SUCCESS_CODE_MAX = 300;

        private final Subscriber<T> subscriber;
        private final HttpClassicClientRequest request;
        private final Type responseType;
        private final AtomicBoolean requested = new AtomicBoolean();
        private final AtomicBoolean completed = new AtomicBoolean();
        private final AtomicLong counter = new AtomicLong();
        private final Lock lock = LockUtils.newReentrantLock();
        private final Queue<T> buffer = new ArrayDeque<>();
        private volatile Exception error;

        TextStreamSubscription(Subscriber<T> subscriber, HttpClassicClientRequest request, Type responseType) {
            this.subscriber = subscriber;
            this.request = request;
            this.responseType = responseType;
        }

        @Override
        protected void request0(long count) {
            long num = this.counter.addAndGet(count);
            if (this.requested.compareAndSet(false, true)) {
                this.exchange();
            }
            synchronized (this.lock) {
                for (int i = 0; i < num; i++) {
                    if (this.buffer.isEmpty()) {
                        this.handleBufferIsEmpty();
                        return;
                    } else {
                        this.subscriber.consume(this.buffer.remove());
                        this.counter.decrementAndGet();
                    }
                }
            }
        }

        private void exchange() {
            try (HttpClassicClientResponse<T> response = this.request.exchange(this.responseType)) {
                if (!ObjectUtils.between(response.statusCode(),
                        HTTP_SUCCESS_CODE_MIN,
                        HTTP_SUCCESS_CODE_MAX,
                        true,
                        false)) {
                    throw new HttpClientResponseException(request, response);
                }
                TextEventStreamEntity entity = response.textEventStreamEntity()
                        .orElseThrow(() -> new IllegalStateException("No text event stream entity."));
                Worker.create(this, entity.stream().map(this::convert)).run();
            } catch (Exception e) {
                this.onWorkerFailed(e);
            }
        }

        private void handleBufferIsEmpty() {
            if (!this.completed.get()) {
                return;
            }
            if (this.error != null) {
                this.subscriber.fail(this.error);
            } else {
                this.subscriber.complete();
            }
        }

        private T convert(TextEvent textEvent) {
            if (this.responseType == TextEvent.class) {
                return cast(textEvent);
            } else {
                return cast(textEvent.data());
            }
        }

        @Override
        public void onWorkerSubscribed(Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onWorkerConsumed(T data, long id) {
            synchronized (this.lock) {
                this.buffer.add(data);
                if (this.counter.get() > 0) {
                    this.subscriber.consume(this.buffer.remove());
                    this.counter.decrementAndGet();
                }
            }
        }

        @Override
        public void onWorkerFailed(Exception cause) {
            this.completed.set(true);
            this.error = cause;
            synchronized (this.lock) {
                if (this.buffer.isEmpty()) {
                    this.handleBufferIsEmpty();
                }
            }
        }

        @Override
        public void onWorkerCompleted() {
            this.completed.set(true);
            synchronized (this.lock) {
                if (this.buffer.isEmpty()) {
                    this.handleBufferIsEmpty();
                }
            }
        }
    }
}
