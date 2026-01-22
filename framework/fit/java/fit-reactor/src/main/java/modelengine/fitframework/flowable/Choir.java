/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.flowable;

import static modelengine.fitframework.inspection.Validation.greaterThan;

import modelengine.fitframework.flowable.choir.FlexibleEmitterChoir;
import modelengine.fitframework.flowable.choir.IterableChoir;
import modelengine.fitframework.flowable.choir.PublisherChoirAdapter;
import modelengine.fitframework.flowable.subscriber.EmptySubscriber;
import modelengine.fitframework.flowable.util.OnSubscribedObserver;
import modelengine.fitframework.schedule.ThreadPoolExecutor;
import modelengine.fitframework.schedule.ThreadPoolExecutors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 表示拥有 {@code 0 - n} 个数据的数据流的发布者。
 *
 * @param <T> 表示数据类型的 {@link T}。
 * @author 季聿阶
 * @since 2024-02-08
 */
public interface Choir<T> extends Publisher<T>, org.reactivestreams.Publisher<T> {
    /**
     * 通过指定的 {@link Emitter 发送器} 消费逻辑创建 {@link Choir 响应式流}。
     *
     * @param consumer 表示指定消费逻辑的 {@link Consumer}{@code <}{@link Emitter}{@code <}{@link T}{@code >>}。
     * @param <T> 表示数据发送器和响应式流中数据类型的 {@link T}。
     * @return 表示所创建的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    static <T> Choir<T> create(Consumer<Emitter<T>> consumer) {
        return new FlexibleEmitterChoir<>(Emitter::create,
                consumer,
                OnSubscribedObserver::notifyOnSubscribed,
                null,
                null);
    }

    /**
     * 创建一个空的响应式流。
     *
     * @param <T> 表示响应式流中数据类型的 {@link T}。
     * @return 表示创建出来的空的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    static <T> Choir<T> empty() {
        return fromIterable(Collections.emptyList());
    }

    /**
     * 将一个 {@link Emitter 发送器} 转换成 {@link Choir 响应式流}。
     *
     * @param emitter 表示指定数据发送器的 {@link Emitter}{@code <}{@link T}{@code >}。
     * @param <T> 表示数据发送器和响应式流中数据类型的 {@link T}。
     * @return 表示转换后的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    static <T> Choir<T> fromEmitter(Emitter<T> emitter) {
        return new FlexibleEmitterChoir<>(() -> emitter, null, OnSubscribedObserver::notifyOnSubscribed, null, null);
    }

    /**
     * 将一个 {@link Iterable 迭代器} 转换成 {@link Choir 响应式流}。
     *
     * @param iterable 表示指定迭代器的 {@link Iterable}{@code <}{@link T}{@code >}。
     * @param <T> 表示迭代器和响应式流中数据类型的 {@link T}。
     * @return 表示转换后的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    static <T> Choir<T> fromIterable(Iterable<T> iterable) {
        return new IterableChoir<>(iterable);
    }

    /**
     * 将一个 {@link Publisher} 适配成 {@link Choir 响应式流}。
     *
     * @param publisher 表示指定发布者的 {@link Publisher}{@code <}{@link T}{@code >}。
     * @param <T> 表示发布者中数据类型的 {@link T}。
     * @return 表示适配后的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    static <T> Choir<T> fromPublisher(Publisher<T> publisher) {
        return new PublisherChoirAdapter<>(publisher);
    }

    /**
     * 将指定的数组转换成 {@link Choir 响应式流}。
     *
     * @param array 表示指定的数组的 {@link T}{@code []}。
     * @param <T> 表示发布者中数据类型的 {@link T}。
     * @return 表示转换后的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    @SafeVarargs
    static <T> Choir<T> just(T... array) {
        if (array == null) {
            return empty();
        }
        return fromIterable(Arrays.asList(array));
    }

    /**
     * 从 1 开始，到指定的终止值（不包含）结束，间隔步长为 1，将所有整数转换成响应式流。
     *
     * @param stop 表示指定的终止值（<b>不包含</b>）的 {@code int}。
     * @return 表示指定范围数据组成的响应式流的 {@link Choir}{@code <}{@link Integer}{@code >}。
     */
    static Choir<Integer> range(int stop) {
        return range(1, stop, 1);
    }

    /**
     * 从指定的起始值开始，到指定的终止值（不包含）结束，间隔步长为 1，将所有整数转换成响应式流。
     *
     * @param start 表示指定的起始值的 {@code int}。
     * @param stop 表示指定的终止值（<b>不包含</b>）的 {@code int}。
     * @return 表示指定范围数据组成的响应式流的 {@link Choir}{@code <}{@link Integer}{@code >}。
     */
    static Choir<Integer> range(int start, int stop) {
        return range(start, stop, 1);
    }

    /**
     * 从指定的起始值开始，到指定的终止值（不包含）结束，间隔指定的步长，将所有整数转换成响应式流。
     *
     * @param start 表示指定的起始值的 {@code int}。
     * @param stop 表示指定的终止值（<b>不包含</b>）的 {@code int}。
     * @param step 表示指定的步长的 {@code int}。
     * @return 表示指定范围数据组成的响应式流的 {@link Choir}{@code <}{@link Integer}{@code >}。
     * @throws IllegalArgumentException 当 {@code step} 小于等于 {@code 0} 时。
     */
    static Choir<Integer> range(int start, int stop, int step) {
        greaterThan(step, 0, "The step must be positive. [step={0}]", step);
        int capacity = (int) Math.ceil((double) Math.abs(start - stop) / step);
        List<Integer> list = new ArrayList<>(capacity);
        if (start < stop) {
            for (int i = start; i < stop; i += step) {
                list.add(i);
            }
        } else if (start > stop) {
            for (int i = start; i > stop; i -= step) {
                list.add(i);
            }
        } else {
            return empty();
        }
        return Choir.fromIterable(list);
    }

    /**
     * 将数据按照指定大小进行分组，转换成列表数据后继续发送。
     *
     * @param size 表示分组大小的 {@code int}。
     * @return 表示按照指定大小进行分组后的列表数据的新的响应式流的 {@link Choir}{@code <}{@link List}{@code <}{@link T}{@code >>}。
     */
    Choir<List<T>> buffer(int size);

    /**
     * 计算响应式流中的元素数量后，将元素数量发送。
     *
     * @return 表示计算元素数量后形成的新的响应式流的 {@link Solo}{@code <}{@link Long}{@code >}。
     */
    Solo<Long> count();

    /**
     * 将上游元素去重后传递给下游。
     *
     * @return 表示包含无重复元素的新响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    Choir<T> distinct();

    /**
     * 将每个数据按照指定方式判断是否符合要求，并将符合要求的数据继续发送。
     *
     * @param filter 表示指定判断方式的 {@link Predicate}{@code <}{@link T}{@code >}。
     * @return 表示包含符合要求数据的新的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    Choir<T> filter(Predicate<T> filter);

    /**
     * 仅保留第一个元素，并继续发送。
     *
     * @return 表示仅保留第一个元素的新的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    Solo<T> first();

    /**
     * 获取满足条件的第一个元素，并继续发送。
     *
     * @param filter 表示指定的过滤方式的 {@link Predicate}{@code <}{@link T}{@code >}。
     * @return 表示保留满足条件的第一个元素的新的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    Solo<T> first(Predicate<T> filter);

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
     * @return 表示包含当前数据转换操作的新的响应式流的 {@link Choir}{@code <}{@link R}{@code >}。
     */
    <R> Choir<R> map(Function<T, R> mapper);

    /**
     * 将每个数据通过指定的方式进行合并后，形成一个新的数据，并继续发送。
     *
     * @param reducer 表示指定的合并方式的 {@link BinaryOperator}{@code <}{@link T}{@code >}。
     * @return 表示合并所有数据后转换成的新的响应式流的 {@link Solo}{@code <}{@link T}{@code >}。
     */
    Solo<T> reduce(BinaryOperator<T> reducer);

    /**
     * 从响应式流的开始跳过指定数量个元素，并继续发送。
     *
     * @param count 表示跳过元素的数量的 {@code int}。
     * @return 表示跳过指定元素转换成的新的响应式流的 {@link Choir}{@code <}{@link T}{@code >}。
     */
    Choir<T> skip(int count);

    /**
     * 直接使用 {@link EmptySubscriber} 订阅响应式流。
     */
    void subscribe();

    /**
     * 使用 Lambda 表达式订阅响应式流，指定 {@link Consumer 响应式流中的数据消费时的行为}，并开始消费响应式流中的数据。
     * <p>默认响应式流被订阅时，将向当前数据发布者请求无限量的数据。</p>
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
     * 在指定的执行器之上执行订阅、订阅响应和元素请求操作。
     *
     * @param executor 表示执行器的 {@link ThreadPoolExecutor}，可通过 {@link ThreadPoolExecutors} 获取或创建执行器。
     * @return 表示订阅、订阅响应和元素请求操作在指定的执行器之上执行的响应式流的 {@link Choir}。
     */
    Choir<T> subscribeOn(ThreadPoolExecutor executor);

    /**
     * 订阅响应式流，并阻塞等待所有结果。
     *
     * @return 表示阻塞等待得到的所有结果列表的 {@link List}{@code <}{@link T}{@code >}。
     */
    List<T> blockAll();
}
