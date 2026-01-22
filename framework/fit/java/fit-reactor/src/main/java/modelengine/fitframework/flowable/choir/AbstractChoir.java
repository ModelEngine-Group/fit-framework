/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.flowable.choir;

import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.flowable.Publisher;
import modelengine.fitframework.flowable.Solo;
import modelengine.fitframework.flowable.Subscriber;
import modelengine.fitframework.flowable.Subscription;
import modelengine.fitframework.flowable.publisher.BufferPublisherDecorator;
import modelengine.fitframework.flowable.publisher.CountPublisherDecorator;
import modelengine.fitframework.flowable.publisher.FilterPublisherDecorator;
import modelengine.fitframework.flowable.publisher.FlatMapPublisherDecorator;
import modelengine.fitframework.flowable.publisher.MapPublisherDecorator;
import modelengine.fitframework.flowable.publisher.ReducePublisherDecorator;
import modelengine.fitframework.flowable.publisher.SkipPublisherDecorator;
import modelengine.fitframework.flowable.publisher.SubscribeOnPublisherDecorator;
import modelengine.fitframework.flowable.subscriber.BlockAllSubscriber;
import modelengine.fitframework.flowable.subscriber.FunctionalSubscriber;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.schedule.ThreadPoolExecutor;
import modelengine.fitframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 表示 {@link Choir} 的抽象实现类。
 *
 * @param <T> 表示响应式流中数据类型的 {@link T}。
 * @author 季聿阶
 * @since 2024-02-07
 */
public abstract class AbstractChoir<T> implements Choir<T> {
    @Override
    public Choir<List<T>> buffer(int size) {
        return Choir.fromPublisher(new BufferPublisherDecorator<>(this, size));
    }

    @Override
    public Choir<T> distinct() {
        Set<T> elements = new HashSet<>();
        return this.filter(value -> {
            synchronized (elements) {
                return elements.add(value);
            }
        });
    }

    @Override
    public Solo<Long> count() {
        return Solo.fromPublisher(new CountPublisherDecorator<>(this));
    }

    @Override
    public Choir<T> filter(Predicate<T> filter) {
        return Choir.fromPublisher(new FilterPublisherDecorator<>(this, filter));
    }

    @Override
    public Solo<T> first() {
        return Solo.fromPublisher(this);
    }

    @Override
    public Solo<T> first(Predicate<T> filter) {
        return this.filter(filter).first();
    }

    @Override
    public <R> Choir<R> flatMap(Function<T, Publisher<R>> flatMapper) {
        return Choir.fromPublisher(new FlatMapPublisherDecorator<>(this, flatMapper));
    }

    @Override
    public <R> Choir<R> map(Function<T, R> mapper) {
        return Choir.fromPublisher(new MapPublisherDecorator<>(this, mapper));
    }

    @Override
    public Choir<T> subscribeOn(ThreadPoolExecutor executor) {
        return Choir.fromPublisher(new SubscribeOnPublisherDecorator<>(this, executor, true));
    }

    @Override
    public Solo<T> reduce(BinaryOperator<T> reducer) {
        return Solo.fromPublisher(new ReducePublisherDecorator<>(this, reducer));
    }

    @Override
    public Choir<T> skip(int count) {
        return Choir.fromPublisher(new SkipPublisherDecorator<>(this, count));
    }

    @Override
    public void subscribe() {
        this.subscribe(Subscriber.empty());
    }

    @Override
    public void subscribe(BiConsumer<Subscription, T> consumeAction) {
        this.subscribe(null, consumeAction, null, null);
    }

    @Override
    public void subscribe(Consumer<Subscription> onSubscribedAction, BiConsumer<Subscription, T> consumeAction,
            Consumer<Subscription> completeAction, BiConsumer<Subscription, Exception> failAction) {
        this.subscribe(Subscriber.functional(ObjectUtils.nullIf(onSubscribedAction,
                        FunctionalSubscriber.DEFAULT_ON_SUBSCRIBED_CHOIR_ACTION),
                ObjectUtils.nullIf(consumeAction, ObjectUtils.cast(FunctionalSubscriber.EMPTY_CONSUME_ACTION)),
                ObjectUtils.nullIf(completeAction, FunctionalSubscriber.EMPTY_COMPLETE_ACTION),
                ObjectUtils.nullIf(failAction, FunctionalSubscriber.EMPTY_FAIL_ACTION)));
    }

    @Override
    public void subscribe(Subscriber<T> subscriber) {
        this.subscribe0(ObjectUtils.getIfNull(subscriber, Subscriber::empty));
    }

    @Override
    public void subscribe(org.reactivestreams.Subscriber<? super T> subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("Subscriber cannot be null");
        }
        // 使用现有的 Lambda subscribe 方法，适配 Reactive Streams Subscriber
        this.subscribe(
                // onSubscribedAction: 将内部 Subscription 适配为 Reactive Streams Subscription
                subscription -> subscriber.onSubscribe(new org.reactivestreams.Subscription() {
                    @Override
                    public void request(long n) {
                        subscription.request(n);
                    }

                    @Override
                    public void cancel() {
                        subscription.cancel();
                    }
                }),
                // consumeAction: 将数据传递给 Reactive Streams Subscriber
                (subscription, data) -> subscriber.onNext(data),
                // completeAction: 通知 Reactive Streams Subscriber 完成
                subscription -> subscriber.onComplete(),
                // failAction: 将异常传递给 Reactive Streams Subscriber
                (subscription, cause) -> subscriber.onError(cause)
        );
    }

    /**
     * 向发布者订阅以启动数据发送。
     *
     * @param subscriber 表示已订阅的 {@link Subscriber}{@code <}{@link T}{@code >}。
     */
    protected abstract void subscribe0(@Nonnull Subscriber<T> subscriber);

    @Override
    public List<T> blockAll() {
        BlockAllSubscriber<T> subscriber = new BlockAllSubscriber<>();
        this.subscribe(subscriber);
        return subscriber.getBlockedList();
    }
}
