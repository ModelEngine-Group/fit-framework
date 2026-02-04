/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.flowable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import modelengine.fitframework.flowable.subscriber.RecordSubscriber;
import modelengine.fitframework.schedule.ThreadPoolExecutor;
import modelengine.fitframework.schedule.ThreadPoolExecutors;
import modelengine.fitframework.util.ThreadUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 表示 {@link Choir} 的单元测试。
 *
 * @author 季聿阶
 * @since 2024-02-10
 */
@DisplayName("测试 Choir")
public class ChoirTest {
    @Nested
    @DisplayName("测试创建 Choir")
    class TestCreate {
        @Test
        @DisplayName("使用 empty 方法创建的响应式流，结果符合预期")
        void shouldReturnCorrectChoirWhenEmpty() {
            List<Integer> actual = Choir.<Integer>empty().blockAll();
            assertThat(actual).isEmpty();
        }

        @Nested
        @DisplayName("测试使用 just 创建 Choir")
        class TestJust {
            @Test
            @DisplayName("当数组不存在数据时，结果符合预期")
            void shouldReturnNothingWhenGiveEmptyAndRequestAll() {
                RecordSubscriber<Integer> subscriber = new RecordSubscriber<>();
                Choir.just((Integer[]) null).subscribe(subscriber);
                assertThat(subscriber.getElements()).isEmpty();
            }

            @Test
            @DisplayName("当数组存在三个元素并且请求所有元素时，结果符合预期")
            void shouldReturnThreeNumWhenGiveThreeAndRequestThree() {
                RecordSubscriber<Integer> subscriber = new RecordSubscriber<>();
                Choir.just(0, 1, 2).subscribe(subscriber);
                assertThat(subscriber.getElements()).hasSize(3).containsSequence(0, 1, 2);
            }

            @Test
            @DisplayName("当数组存在三个元素并且请求一个元素时，结果符合预期")
            void shouldReturnOneNumWhenGiveThreeAndRequestOne() {
                RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1);
                Choir.just(0, 1, 2).subscribe(subscriber);
                assertThat(subscriber.getElements()).hasSize(1).containsSequence(0);
                assertThat(subscriber.receivedCompleted()).isFalse();
            }

            @Test
            @DisplayName("当数组存在三个元素并且请求每一个元素时，结果符合预期")
            void shouldReturnThreeNumWhenGiveThreeAndRequestEveryOne() {
                RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1, 1);
                Choir.just(0, 1, 2).subscribe(subscriber);
                assertThat(subscriber.getElements()).hasSize(3).containsSequence(0, 1, 2);
            }

            @Test
            @DisplayName("当数组存在三个元素请求每一个元素并且在有两个元素取消订阅时，结果符合预期")
            void shouldReturnTwoNumWhenGiveThreeAndRequestEveryOneThenCancel() {
                RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1, 1, 2);
                Choir.just(0, 1, 2).subscribe(subscriber);
                assertThat(subscriber.getElements()).hasSize(2).containsSequence(0, 1);
            }
        }

        @Nested
        @DisplayName("测试使用 range 创建 Choir")
        class TestRange {
            @Test
            @DisplayName("当使用递增序列创建响应式流时，结果符合预期")
            void shouldReturnCorrectChoirWhenRangeIncrement() {
                List<Integer> actual = Choir.range(0, 5, 2).blockAll();
                assertThat(actual).hasSize(3).containsSequence(0, 2, 4);
            }

            @Test
            @DisplayName("当使用递减序列创建响应式流时，结果符合预期")
            void shouldReturnCorrectChoirWhenRangeDecrement() {
                List<Integer> actual = Choir.range(0, -5, 2).blockAll();
                assertThat(actual).hasSize(3).containsSequence(0, -2, -4);
            }

            @Test
            @DisplayName("当仅包含起始值和终止值时，结果符合预期")
            void shouldReturnCorrectChoirWhenOnlyStartAndStop() {
                List<Integer> actual = Choir.range(0, 5).blockAll();
                assertThat(actual).hasSize(5).containsSequence(0, 1, 2, 3, 4);
            }

            @Test
            @DisplayName("当仅包含终止值时，结果符合预期")
            void shouldReturnCorrectChoirWhenOnlyStop() {
                List<Integer> actual = Choir.range(5).blockAll();
                assertThat(actual).hasSize(4).containsSequence(1, 2, 3, 4);
            }

            @Test
            @DisplayName("当起始值和终止值一致时，返回空的响应式流")
            void shouldReturnEmptyChoirWhenStartEqualsToStop() {
                List<Integer> actual = Choir.range(5, 5).blockAll();
                assertThat(actual).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("测试转换 Choir")
    class TestTransform {
        @Test
        @DisplayName("使用 buffer 方法转换的响应式流，结果符合预期")
        void shouldReturnCorrectChoirWhenBuffer() {
            List<List<Integer>> lists = Choir.just(1, 2, 3).buffer(2).blockAll();
            assertThat(lists).hasSize(2);
            assertThat(lists.get(0)).hasSize(2).containsSequence(1, 2);
            assertThat(lists.get(1)).hasSize(1).containsSequence(3);
        }

        @Test
        @DisplayName("使用 map 方法转换的响应式流，结果符合预期")
        void shouldReturnCorrectChoirWhenMap() {
            List<String> actual = Choir.just(1, 2, 3).map(String::valueOf).blockAll();
            assertThat(actual).hasSize(3).containsSequence("1", "2", "3");
        }

        @Test
        @DisplayName("使用 reduce 方法转换的响应式流，结果符合预期")
        void shouldReturnCorrectSoloWhenReduce() {
            Optional<Integer> actual = Choir.just(1, 2, 3).reduce(Integer::sum).block();
            assertThat(actual).isPresent().hasValue(6);
        }

        @Test
        @DisplayName("使用 flatMap 展平 Choir 响应式流，请求足量元素，结果符合预期")
        void shouldReturnNumAsIntWithCompleteWhenFlatMapAndRequestEnough() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>();
            Choir.just(0, 1, 2, 3).flatMap(value -> Choir.just(value * 2, value * 2 + 1)).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(8).containsSequence(0, 1, 2, 3, 4, 5, 6, 7);
        }

        @Test
        @DisplayName("使用 flatMap 展平 Choir 响应式流并且元素类型发生变化，请求足量元素，结果符合预期")
        void shouldReturnNumAsStringWithCompleteWhenFlatMapAndRequestEnough() {
            RecordSubscriber<String> subscriber = new RecordSubscriber<>();
            Choir.just(0, 1, 2, 3)
                    .flatMap((Integer value) -> Choir.just(Integer.toString(value * 2),
                            Integer.toString(value * 2 + 1)))
                    .subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(8).containsSequence("0", "1", "2", "3", "4", "5", "6", "7");
        }

        @Test
        @DisplayName("使用 flatMap 展平 Choir 响应式流，请求定量元素，结果符合预期")
        void shouldReturnFiveNumWithoutCompleteWhenFlatMapAndRequestFive() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(5);
            Choir.just(0, 1, 2, 3).flatMap(value -> Choir.just(value * 2, value * 2 + 1)).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(5).containsSequence(0, 1, 2, 3, 4);
            assertThat(subscriber.receivedCompleted()).isFalse();
        }

        @Test
        @DisplayName("使用 flatMap 展平 Choir 响应式流，请求每一个元素，结果符合预期")
        void shouldReturnAllNumWithCompleteWhenFlatMapAndRequestEveryOne() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1, 1);
            Choir.just(0, 1, 2, 3).flatMap(value -> Choir.just(value * 2, value * 2 + 1)).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(8).containsSequence(0, 1, 2, 3, 4, 5, 6, 7);
            assertThat(subscriber.receivedCompleted()).isTrue();
        }

        @Test
        @DisplayName("使用 flatMap 通过错误的方式展平 Choir 响应式流，请求每一个元素，结果符合预期")
        void shouldReturnOneNumWithFailWhenFlatMapIncorrectly() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1, 1);
            Choir.just(0, 1, 2, 3).flatMap(value -> Choir.just(value / (value - 1))).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(1).containsSequence(0);
            assertThat(subscriber.receivedCompleted()).isFalse();
            assertThat(subscriber.receivedFailed()).isTrue();
        }

        @Nested
        @DisplayName("测试使用 count 方法转换 Choir")
        class TestCount {
            @Test
            @DisplayName("使用 count 方法转换的响应式流，当发送者正常发送数据并请求每一个元素后，结果符合预期")
            void shouldReturnCorrectSoloWhenCount() {
                RecordSubscriber<Long> subscriber = new RecordSubscriber<>(1, 1);
                Emitter<Integer> emitter = Emitter.create();
                Choir.fromEmitter(emitter).count().subscribe(subscriber);
                emitter.emit(1);
                emitter.emit(2);
                emitter.complete();
                assertThat(subscriber.getElements()).hasSize(1).containsSequence(2L);
                assertThat(subscriber.receivedCompleted()).isTrue();
            }

            @Test
            @DisplayName("使用 count 方法转换的响应式流，当发送者发送数据后取消时，结果符合预期")
            void shouldReturnCorrectResultAfterCancelling() {
                RecordSubscriber<Long> subscriber = new RecordSubscriber<>(1, 1);
                Emitter<Integer> emitter = Emitter.create();
                Choir.fromEmitter(emitter).count().subscribe(subscriber);
                emitter.emit(1);
                emitter.emit(2);
                subscriber.getSubscription().cancel();
                emitter.complete();
                assertThat(subscriber.getElements()).hasSize(0);
                assertThat(subscriber.receivedCompleted()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("测试过滤 Choir")
    class TestFilter {
        @Test
        @DisplayName("使用 filter 过滤响应式流并请求每一个元素，结果符合预期")
        void shouldReturnOneNumWithCompleteWhenFilterAndRequestEach() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1, 1);
            Choir.just(0, 1, 2).filter(value -> value % 2 == 1).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(1).containsSequence(1);
            assertThat(subscriber.receivedCompleted()).isTrue();
            assertThat(subscriber.receivedFailed()).isFalse();
        }

        @Test
        @DisplayName("使用 filter 不正确的过滤响应式流并请求每一个元素，结果符合预期")
        void shouldReturnOneNumWithFailWhenFilterAndRequestEach() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1, 1);
            Choir.just(1, 0, 2).filter(value -> 10 / value >= 5).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(1).containsSequence(1);
            assertThat(subscriber.receivedFailed()).isTrue();
        }

        @Test
        @DisplayName("使用 filter 过滤响应式流并请求一个元素，结果符合预期")
        void shouldReturnOneNumWithoutCompleteWhenFilterAndRequestOne() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1);
            Choir.just(0, 1, 2).filter(value -> value % 2 == 1).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(1).containsSequence(1);
            assertThat(subscriber.receivedCompleted()).isFalse();
            assertThat(subscriber.receivedFailed()).isFalse();
        }

        @Test
        @DisplayName("使用 filter 过滤响应式流并请求两个元素，结果符合预期")
        void shouldReturnOneNumWithCompleteWhenFilterAndRequestTwo() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(2, 0);
            Choir.just(0, 1, 2).filter(value -> value % 2 == 1).subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(1).containsSequence(1);
            assertThat(subscriber.receivedCompleted()).isTrue();
            assertThat(subscriber.receivedFailed()).isFalse();
        }

        @Test
        @DisplayName("使用 first 方法保留第一个元素的响应式流，结果符合预期")
        void shouldReturnCorrectSoloWhenFirst() {
            Optional<Integer> actual = Choir.just(1, 2, 3).first().block();
            assertThat(actual).isPresent().hasValue(1);
        }

        @Test
        @DisplayName("使用 first 方法获取满足条件的第一个元素的响应式流，结果符合预期")
        void shouldReturnCorrectSoloWhenFirstFilter() {
            Optional<Integer> actual = Choir.just(1, 2, 3).first(value -> value > 2).block();
            assertThat(actual).isPresent().hasValue(3);
        }

        @Test
        @DisplayName("使用 distinct 对于响应式流元素去重并请求一个元素，结果符合预期")
        void shouldReturnOneNumWithoutCompleteWhenDistinctAndRequestOne() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1);
            Choir.just(1, 2, 3, 2, 2, 7, 2, 8, 1).distinct().subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(1).containsSequence(1);
            assertThat(subscriber.receivedCompleted()).isFalse();
        }

        @Test
        @DisplayName("使用 distinct 对于响应式流元素去重并请求五个元素，结果符合预期")
        void shouldReturnFiveNumWithoutCompleteWhenDistinctAndRequestFive() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(5);
            Choir.just(1, 2, 3, 2, 2, 7, 2, 8, 1).distinct().subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(5).containsSequence(1, 2, 3, 7, 8);
            assertThat(subscriber.receivedCompleted()).isFalse();
        }

        @Test
        @DisplayName("使用 distinct 对于响应式流元素去重并请求六个元素，结果符合预期")
        void shouldReturnFiveNumWithCompleteWhenDistinctAndRequestSix() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(6);
            Choir.just(1, 2, 3, 2, 2, 7, 2, 8, 1).distinct().subscribe(subscriber);
            assertThat(subscriber.getElements()).hasSize(5).containsSequence(1, 2, 3, 7, 8);
            assertThat(subscriber.receivedCompleted()).isTrue();
        }

        @Nested
        @DisplayName("测试使用 skip 方法过滤 Choir")
        class TestSkip {
            @Test
            @DisplayName("使用 skip 方法跳过零个元素，结果符合预期")
            void shouldThrowIllegalArgumentExceptionWhenSkipZero() {
                assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Choir.just(1, 2, 3, 4, 5, 6)
                        .skip(0)).withMessage("The count to skip must be positive. [count=0]");
            }

            @Test
            @DisplayName("使用 skip 方法跳过响应式流的前三个元素，结果符合预期")
            void shouldReturnThreeNumWithCompleteWhenSkipThreeAndRequestOne() {
                RecordSubscriber<Integer> subscriber = new RecordSubscriber<>(1, 1);
                Choir.just(1, 2, 3, 4, 5, 6).skip(3).subscribe(subscriber);
                assertThat(subscriber.getElements()).hasSize(3).containsSequence(4, 5, 6);
                assertThat(subscriber.receivedCompleted()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("测试 subscribeOn 方法")
    class TestSubscribeOn {
        @Test
        @DisplayName("当在数据流转换操作执行之后通过 subscribeOn 方法切换执行器时，结果符合预期")
        void shouldFinishSubscribeImmediatelyWhenSubscribeOnAfterMap() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>();
            Semaphore semaphore = new Semaphore(0);
            Choir.just(1).map(integer -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return integer;
            }).subscribeOn(ThreadPoolExecutors.newSingle("single", true)).subscribe(subscriber);
            assertThat(subscriber.isCompleted()).isFalse();
            semaphore.release();
        }

        @Test
        @DisplayName("当在数据流转换操作执行之前通过 subscribeOn 方法切换执行器时，结果符合预期")
        void shouldFinishSubscribeImmediatelyWhenSubscribeOnBeforeMap() {
            RecordSubscriber<Integer> subscriber = new RecordSubscriber<>();
            Semaphore semaphore = new Semaphore(0);
            Choir.just(1).subscribeOn(ThreadPoolExecutors.newSingle("single", true)).map(integer -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return integer;
            }).subscribe(subscriber);
            assertThat(subscriber.isCompleted()).isFalse();
            semaphore.release();
        }

        @Test
        @DisplayName("当时响应式流经过两次 subscribeOn 操作切换执行器时，结果符合预期")
        void shouldUseFrontExecutorWhenSubscribeOnTwice() {
            ThreadPoolExecutor front = ThreadPoolExecutors.newParallel("front", 2, true);
            ThreadPoolExecutor back = ThreadPoolExecutors.newParallel("back", 2, true);
            Subscriber<Integer> subscriber = new RecordSubscriber<>();
            AtomicReference<String> threadName = new AtomicReference<>();
            Choir.just(1, 2, 3).subscribeOn(front).map(integer -> {
                threadName.set(Thread.currentThread().getName());
                return integer;
            }).subscribeOn(back).subscribe(subscriber);
            while (threadName.get() == null) {
                ThreadUtils.sleep(1);
            }
            assertThat(threadName.get().startsWith("front")).isTrue();
        }

        @Test
        @DisplayName("当两个响应式流分别通过 subscribeOn 操作切换到同一个执行器时，结果符合预期")
        void shouldUseSameThreadWhenSubscribeOnSameExecutor() {
            RecordSubscriber<Integer> subscriber1 = new RecordSubscriber<>();
            RecordSubscriber<Integer> subscriber2 = new RecordSubscriber<>();
            AtomicReference<String> threadName1 = new AtomicReference<>();
            AtomicReference<String> threadName2 = new AtomicReference<>();
            ThreadPoolExecutor executor = ThreadPoolExecutors.newSingle("single-for-test", true);
            Choir.just(1, 2, 3).subscribeOn(executor).map(integer -> {
                threadName1.set(Thread.currentThread().getName());
                return integer;
            }).subscribe(subscriber1);
            Choir.just(1, 2, 3).subscribeOn(executor).map(integer -> {
                threadName2.set(Thread.currentThread().getName());
                return integer;
            }).subscribe(subscriber2);
            while (threadName1.get() == null || threadName2.get() == null) {
                ThreadUtils.sleep(1);
            }
            assertThat(threadName1.get()).isEqualTo(threadName2.get());
        }
    }

    @Nested
    @DisplayName("测试 Flow.Subscriber 订阅")
    class TestFlowSubscriber {
        @Test
        @DisplayName("使用 Flow.Subscriber 订阅并接收所有数据，结果符合预期")
        void shouldReceiveAllDataWhenSubscribeWithFlowSubscriber() {
            List<Integer> received = new ArrayList<>();
            AtomicBoolean completed = new AtomicBoolean(false);
            AtomicReference<Throwable> error = new AtomicReference<>();

            Choir.just(1, 2, 3).subscribe(new Flow.Subscriber<>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Integer item) {
                    received.add(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                }

                @Override
                public void onComplete() {
                    completed.set(true);
                }
            });

            assertThat(received).hasSize(3).containsSequence(1, 2, 3);
            assertThat(completed.get()).isTrue();
            assertThat(error.get()).isNull();
        }

        @Test
        @DisplayName("使用 Flow.Subscriber 订阅并逐个请求数据，结果符合预期")
        void shouldReceiveDataOneByOneWhenRequestOneAtATime() {
            List<Integer> received = new ArrayList<>();
            AtomicBoolean completed = new AtomicBoolean(false);

            Choir.just(1, 2, 3).subscribe(new Flow.Subscriber<>() {
                private Flow.Subscription subscription;
                private int count = 0;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(1);
                }

                @Override
                public void onNext(Integer item) {
                    received.add(item);
                    count++;
                    if (count < 3) {
                        subscription.request(1);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                    completed.set(true);
                }
            });

            assertThat(received).hasSize(3).containsSequence(1, 2, 3);
            assertThat(completed.get()).isTrue();
        }

        @Test
        @DisplayName("使用 Flow.Subscriber 订阅并取消订阅，结果符合预期")
        void shouldStopReceivingWhenCancelSubscription() {
            List<Integer> received = new ArrayList<>();
            AtomicBoolean completed = new AtomicBoolean(false);

            Choir.just(1, 2, 3, 4, 5).subscribe(new Flow.Subscriber<>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(2);
                }

                @Override
                public void onNext(Integer item) {
                    received.add(item);
                    if (received.size() == 2) {
                        subscription.cancel();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                    completed.set(true);
                }
            });

            assertThat(received).hasSize(2).containsSequence(1, 2);
            assertThat(completed.get()).isFalse();
        }

        @Test
        @DisplayName("使用 Flow.Subscriber 订阅发生错误的流，结果符合预期")
        void shouldReceiveErrorWhenSubscribeErrorStream() {
            List<Integer> received = new ArrayList<>();
            AtomicBoolean completed = new AtomicBoolean(false);
            AtomicReference<Throwable> error = new AtomicReference<>();

            Choir.just(1, 0, 2).map(value -> 10 / value).subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Integer item) {
                    received.add(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                }

                @Override
                public void onComplete() {
                    completed.set(true);
                }
            });

            assertThat(received).hasSize(1).containsSequence(10);
            assertThat(completed.get()).isFalse();
            assertThat(error.get()).isNotNull().isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("使用 null Flow.Subscriber 订阅，应抛出 IllegalArgumentException")
        void shouldThrowIllegalArgumentExceptionWhenSubscriberIsNull() {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Choir.just(1, 2, 3)
                    .subscribe((Flow.Subscriber<Integer>) null)).withMessage("Subscriber cannot be null.");
        }
    }
}
