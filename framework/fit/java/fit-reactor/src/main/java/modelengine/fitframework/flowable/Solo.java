/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.flowable;

import modelengine.fitframework.flowable.choir.FlexibleEmitterChoir;
import modelengine.fitframework.flowable.solo.AnySolo;
import modelengine.fitframework.flowable.solo.PublisherSoloAdapter;
import modelengine.fitframework.flowable.subscriber.EmptySubscriber;
import modelengine.fitframework.flowable.util.OnSubscribedObserver;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 表示拥有 {@code 0 - 1} 个数据的数据流的发布者。
 *
 * @param <T> 表示数据类型的 {@link T}。
 * @author 季聿阶
 * @since 2024-02-11
 */
public interface Solo<T> extends Publisher<T>, org.reactivestreams.Publisher<T> {
    /**
     * 通过指定的 {@link Emitter 发送器} 消费逻辑创建 {@link Solo 响应式流}。
     *
     * @param consumer 表示指定消费逻辑的 {@link Consumer}{@code <}{@link Emitter}{@code <}{@link T}{@code >>}。
     * @param <T> 表示数据发送器和响应式流中数据类型的 {@link T}。
     * @return 表示所创建的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    static <T> Solo<T> create(Consumer<Emitter<T>> consumer) {
        return fromPublisher(new FlexibleEmitterChoir<>(Emitter::create,
                consumer,
                OnSubscribedObserver::notifyOnSubscribed,
                null,
                null));
    }

    /**
     * 创建一个空的响应式流。
     *
     * @param <T> 表示响应式流中数据类型的 {@link T}。
     * @return 表示创建出来的空的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    static <T> Solo<T> empty() {
        return fromPublisher(Choir.empty());
    }

    /**
     * 通过一个 {@link Emitter 发送器} 和指定的请求元素时操作以及订阅取消时操作创建 {@link Solo 响应式流}。
     *
     * @param emitter 表示指定数据发送器的 {@link Emitter}{@code <}{@link T}{@code >}。
     * @param <T> 表示数据发送器和响应式流中数据类型的 {@link T}。
     * @return 表示转换后的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    static <T> Solo<T> fromEmitter(Emitter<T> emitter) {
        return fromPublisher(new FlexibleEmitterChoir<>(() -> emitter,
                null,
                OnSubscribedObserver::notifyOnSubscribed,
                null,
                null));
    }

    /**
     * 将一个 {@link Publisher} 适配成 {@link Solo 响应式流}。
     *
     * @param publisher 表示指定发布者的 {@link Publisher}{@code <}{@link T}{@code >}。
     * @param <T> 表示发布者中数据类型的 {@link T}。
     * @return 表示适配后的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    static <T> Solo<T> fromPublisher(Publisher<T> publisher) {
        return new PublisherSoloAdapter<>(publisher);
    }

    /**
     * 将指定的数据转换成 {@link Solo 响应式流}。
     *
     * @param data 表示指定的数据的 {@link T}。
     * @param <T> 表示发布者中数据类型的 {@link T}。
     * @return 表示转换后的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    static <T> Solo<T> just(T data) {
        return new AnySolo<>(data);
    }

    /**
     * 将每个数据按照指定方式判断是否符合要求，并将符合要求的数据继续发送。
     *
     * @param filter 表示指定判断方式的 {@link Predicate}{@code <}{@link T}{@code >}。
     * @return 表示包含符合要求数据的新的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    Solo<T> filter(Predicate<T> filter);

    /**
     * 将每个数据通过指定的方式转换为一个响应式流，并将各响应式流中的每个元素依次发送给下游。
     *
     * @param flatMapper 表示指定元素转换为响应式流方式的
     * {@link Function}{@code <}{@link T}{@code , }{@link Publisher}{@code <}{@link R}{@code >>}。
     * @param <R> 表示元素按指定方式转换后的响应式流的元素数据类型的 {@link R}。
     * @return 表示包含当前各数据转换为响应式流并依次发送元素的新响应式流的 {@link Choir}{@code <}{@link R}{@code >}。
     */
    <R> Choir<R> flatMap(Function<T, Publisher<R>> flatMapper);

    /**
     * 将每个数据通过指定的方式进行转换后继续发送。
     *
     * @param mapper 表示指定的转换方式的 {@link Function}{@code <}{@link T}{@code , }{@link R}{@code >}。
     * @param <R> 表示转换后的数据类型的 {@link R}。
     * @return 表示包含当前数据转换操作的新的响应式流的 {@link Solo}{@code <}{@link R}{@code >}。
     */
    <R> Solo<R> map(Function<T, R> mapper);

    /**
     * 将当前响应式流转换成 {@link Choir}。
     *
     * @return 表示转换后的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    Choir<T> toChoir();

    /**
     * 直接使用 {@link EmptySubscriber} 订阅响应式流。
     */
    void subscribe();

    /**
     * 使用 Lambda 表达式订阅响应式流，指定 {@link Consumer 响应式流中的数据消费时的行为}，并开始消费响应式流中的数据。
     *
     * @param consumeAction 表示响应式流中的数据消费时的行为的
     * {@link BiConsumer}{@code <}{@link Subscription}{@code , }{@link T}{@code >}。
     */
    void subscribe(BiConsumer<Subscription, T> consumeAction);

    /**
     * 使用 Lambda 表达式订阅响应式流，指定 {@link Consumer 响应式流被订阅时的行为}，{@link Consumer
     * 响应式流中的数据消费时的行为}，{@link Runnable 响应式流正常终结时的行为} 和 {@link Consumer
     * 响应式流异常终结时的行为}，并开始消费响应式流中的数据。
     *
     * @param onSubscribedAction 表示响应式流被订阅时的行为的 {@link Consumer}{@code <}{@link Subscription}{@code >}。
     * @param consumeAction 表示响应式流中的数据消费时的行为的
     * {@link BiConsumer}{@code <}{@link Subscription}{@code , }{@link T}{@code >}。
     * @param completeAction 表示响应式流正常终结时的行为的 {@link Consumer}{@code <}{@link Subscription}{@code >}。
     * @param failAction 表示响应式流异常终结时的行为的
     * {@link BiConsumer}{@code <}{@link Subscription}{@code , }{@link Exception}{@code >}。
     */
    void subscribe(Consumer<Subscription> onSubscribedAction, BiConsumer<Subscription, T> consumeAction,
            Consumer<Subscription> completeAction, BiConsumer<Subscription, Exception> failAction);

    /**
     * 订阅响应式流，并阻塞等待结果。
     *
     * @return 表示阻塞等待得到的结果的 {@link Optional}{@code <}{@link T}{@code >}。
     */
    Optional<T> block();
}
