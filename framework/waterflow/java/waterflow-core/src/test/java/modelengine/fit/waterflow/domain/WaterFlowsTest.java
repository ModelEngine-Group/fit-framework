/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain;

import static modelengine.fit.waterflow.domain.enums.FlowNodeStatus.READY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import modelengine.fit.waterflow.FlowTestException;
import modelengine.fit.waterflow.FlowsTestUtil;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.Window;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMemoMessenger;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMemoRepo;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMessenger;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowlock.FlowLocks;
import modelengine.fit.waterflow.domain.context.repo.flowlock.FlowLocksMemo;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;
import modelengine.fit.waterflow.domain.enums.FlowNodeStatus;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.flow.Flows;
import modelengine.fit.waterflow.domain.flow.ProcessFlow;
import modelengine.fit.waterflow.domain.states.State;
import modelengine.fit.waterflow.domain.stream.nodes.BlockToken;
import modelengine.fit.waterflow.domain.utils.Mermaid;
import modelengine.fit.waterflow.domain.utils.SleepUtil;
import modelengine.fit.waterflow.domain.utils.Tuple;
import modelengine.fitframework.util.ObjectUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * FlowsTest
 * 流程流转框架测试类
 *
 * @author 高诗意
 * @since 1.0
 */
@DisplayName("流程实例核心引擎原始测试用例集合")
class WaterFlowsTest {
    static class TestData {
        private int first = 0;

        private int second = 0;

        private int third = 0;

        TestData first(int value) {
            this.first = value;
            return this;
        }

        TestData second(int value) {
            this.second = value;
            return this;
        }

        TestData third(int value) {
            this.third = value;
            return this;
        }

        /**
         * 总数
         *
         * @return 返回三个元素总和
         */
        public int total() {
            return first + second + third;
        }
    }

    @Nested
    @DisplayName("流程流转引擎基础测试用例集合")
    class WaterFlowBaseLineTest {
        private FlowContextRepo repo;

        private FlowContextMessenger messenger;

        private FlowLocks locks;

        @BeforeEach
        void setUp() {
            repo = new FlowContextMemoRepo(true);
            messenger = new FlowContextMemoMessenger();
            locks = new FlowLocksMemo();
        }

        @Test
        @DisplayName("流程实例map节点流转逻辑")
        void test_map_with_single_data() {
            long[] data = {1, 0};
            List<String> result = new ArrayList<>();
            ProcessFlow<Integer> flowTest = Flows.<Integer>create(repo, messenger, locks)
                    .map(i -> i + data[0]++)
                    .map(i -> i + data[0]++)
                    .close(r -> {
                        data[1] = r.get().getData();
                        result.add(r.get().getData().toString());
                    });
            flowTest.setId("flow test");
            flowTest.offer(1);

            FlowsTestUtil.waitUntil(() -> result.size() == 5);
            assertEquals("flow test", flowTest.getId());
            assertEquals(3, data[0]);
            assertEquals(4, data[1]);
        }

        @Test
        void test_map_with_array_data() {
            List<String> result = new ArrayList<>();
            Flows.<Integer>create(repo, messenger, locks)
                    .map(i -> i + 1)
                    .map(i -> i * 2)
                    .map(i -> i.toString() + "-okay")
                    .close(r -> {
                        result.addAll(r.getAll().stream().map(FlowContext::getData).collect(Collectors.toList()));
                    })
                    .offer(new Integer[] {1, 2, 3, 4, 5});
            FlowsTestUtil.waitUntil(() -> result.size() == 5);
            assertTrue(result.contains("12-okay"));
        }

        @Test
        void test_map_with_preserved_order() {
            List<Integer> result = new ArrayList<>();
            AtomicReference<String> sessionId = new AtomicReference<>();
            //应该乱序
            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks).id("start")
                    .map(i -> {
                        SleepUtil.sleep((6 - i) * 20L);
                        return i * 10;
                    })
                    .map(i -> i + 10)
                    .close(r -> result.add(r.get().getData()))
                    .onComplete(id -> sessionId.set(id));
            FlowSession session = new FlowSession(true);
            Window window = session.begin();
            for (int i = 1; i < 6; i++) {
                flow.offer(i, session);
                SleepUtil.sleep(10);
            }
            SleepUtil.sleep(100);
            window.complete();

            FlowsTestUtil.waitUntil(() -> result.size() == 5, 10000);
            assertEquals(session.getId(), sessionId.get());
            assertEquals(20, result.get(0));
            assertEquals(30, result.get(1));
            assertEquals(40, result.get(2));
            assertEquals(50, result.get(3));
            assertEquals(60, result.get(4));
        }

        @Test
        @DisplayName("流程实例map节点结合block节点流转逻辑")
        void testFitStreamMapComputationWithBlock() {
            int j = 0;
            while (j < 100) {
                j++;
                List<String> result = new ArrayList<>();
                BlockToken<Integer> block = new BlockToken<Integer>() {
                    @Override
                    public boolean verify(Integer data) {
                        return data > 6;
                    }
                };
                Flows.<Integer>create(repo, messenger, locks)
                        .map(i -> i + 1)
                        .map(i -> i * 2)
                        .block(block)
                        .map(i -> i.toString() + "-okay")
                        .close(r -> {
                            result.addAll(r.getAll().stream().map(FlowContext::getData).collect(Collectors.toList()));
                        })
                        .offer(new Integer[] {1, 2, 3, 4, 5});
                FlowsTestUtil.waitUntil(() -> block.data().size() == 5);
                assertEquals(5, block.data().size());
                block.resume();
                FlowsTestUtil.waitUntil(() -> result.size() == 3);
                assertEquals(3, result.size());
            }
        }

        @Test
        @DisplayName("流程实例produce节点流转逻辑")
        void testFitStreamProduceComputation() {
            List<String> result = new ArrayList<>();
            Flows.<Integer>create(repo, messenger, locks)
                    .map((value -> value - 1))
                    .produce(list -> list.stream()
                            .sorted()
                            .limit(2)
                            .map(i -> i.toString() + "-okay")
                            .collect(Collectors.toList()))
                    .close(i -> result.addAll(i.get().getData()))
                    .offer(new Integer[] {1, 2, 3, 4, 5});
            FlowsTestUtil.waitSize(() -> result, 2);
            assertEquals(2, result.size());
        }

        /**
         * 当前的window在计算origin时存在问题，待修复 @zhangqunhui
         */
        @Disabled
        @Test
        @DisplayName("流程实例produce节点结合block节点流转逻辑")
        void testFitStreamProduceComputationWithBlock() {
            List<String> result = new ArrayList<>();
            BlockToken<List<String>> block = new BlockToken<List<String>>() {
                @Override
                public boolean verify(List<String> data) {
                    return true;
                }
            };
            Flows.create(repo, messenger, locks)
                    .produce(list -> list.stream()
                            .limit(2)
                            .map(value -> value.toString() + "-okay")
                            .collect(Collectors.toList()))
                    .block(block)
                    .close(callback -> {
                        result.addAll(callback.get().getData());
                    })
                    .offer(new Integer[] {1, 2, 3, 4, 5});
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals(2, block.data().get(0).getData().size());
            assertEquals(0, result.size());

            block.resume();
            FlowsTestUtil.waitSize(() -> result, 2);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("流程实例reduce节点流转逻辑")
        void testFitStreamReduceComputation() {
            AtomicInteger counter = new AtomicInteger();
            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks)
                    .map(i -> i)
                    .reduce(() -> 0, Integer::sum)
                    .just(i -> counter.set(counter.get() + 1))
                    .close();
            // 数据发送为一个window
            flow.offer(new Integer[] {1, 2, 3, 4, 5});
            FlowsTestUtil.waitUntil(() -> counter.get() == 1);
            assertEquals(1, counter.get());

            // 单个发送没有window
            counter.set(0);
            for (int i = 0; i < 2; i++) {
                flow.offer(i);
            }
            flow.complete();
            FlowsTestUtil.waitUntil(() -> counter.get() == 1);
            assertEquals(1, counter.get());
        }

        @Test
        void test_reduce_with_complete_event() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger();
            AtomicInteger result = new AtomicInteger();

            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks)
                    .reduce(() -> 0, (acc, value) -> acc + value)
                    .just(i -> {
                        result.set(i);
                        counter.set(counter.get() + 1);
                    })
                    .close();
            FlowSession session = new FlowSession();
            Window window = session.begin();
            for (int i = 0; i < 6; i++) {
                flow.offer(i + 1, session);
            }
            Thread.sleep(400);
            window.complete();

            FlowsTestUtil.waitUntil(() -> counter.get() == 1, 1000);
            assertEquals(21, result.get());
            FlowsTestUtil.waitUntil(() -> counter.get() == 1);
            assertEquals(1, counter.get());
        }

        @Test
        void test_reduce_with_complete_event_and_window() throws InterruptedException {
            List<Integer> result = new ArrayList<>();

            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks)
                    .window(2)
                    .reduce(() -> 0, (acc, value) -> acc + 1)
                    .just(i -> result.add(i))
                    .close();
            FlowSession session = new FlowSession();
            Window window = session.begin();
            for (int i = 0; i < 5; i++) {
                flow.offer(i + 1, session);
                Thread.sleep(10);
            }
            Thread.sleep(500);//todo:注意这里延迟有时错误 （for xiafei）
            window.complete();

            FlowsTestUtil.waitUntil(() -> result.size() == 5);
            assertEquals(3, result.size());
            assertEquals(5, result.stream().reduce(0, (acc, i) -> acc + i));
        }

        @Test
        @Disabled("概率性无法通过")
        void test_reduce_with_window_and_keyBy() {
            final List<Tuple<String, Integer>> reduced = new ArrayList<>();
            Flows.<Tuple<String, Integer>>create(repo, messenger, locks)
                    .keyBy(Tuple::first)
                    .window(2)
                    .reduce(() -> ObjectUtils.<Tuple<String, Integer>>cast(Tuple.from("", 0)),
                            (acc, data) -> Tuple.from(data.first(), acc.second() + data.second().second()))
                    .just(data -> {
                        synchronized (reduced) {
                            reduced.add(ObjectUtils.cast(data));
                        }
                    })
                    .close()
                    .offer(new Tuple[] {
                            Tuple.from("will", 1), Tuple.from("evan", 2), Tuple.from("will", 3), Tuple.from("evan", 4),
                            Tuple.from("will", 5), Tuple.from("evan", 6)
                    });

            FlowsTestUtil.waitUntil(() -> reduced.size() == 4);

            assertEquals(4, reduced.size());
            assertEquals(4, reduced.get(0).second());
            assertEquals(6, reduced.get(1).second());
            assertEquals(5, reduced.get(2).second());
            assertEquals(6, reduced.get(3).second());
        }

        @Test
        @Disabled("概率性无法通过")
        void test_reduce_with_window_and_keyBy_complete_event() {
            final List<Tuple<String, Integer>> reduced = new ArrayList<>();
            Flow flow = Flows.<Tuple<String, Integer>>create(repo, messenger, locks)
                    .keyBy(Tuple::first)
                    .window(3)
                    .reduce(() -> ObjectUtils.<Tuple<String, Integer>>cast(Tuple.from("", 0)),
                            (acc, data) -> Tuple.from(data.first(), acc.second() + data.second().second()))
                    .just(data -> {
                        reduced.add((Tuple<String, Integer>)data);
                    })
                    .close();
            FlowSession session = new FlowSession();
            Window token = session.begin();
            flow.offer(Tuple.from("will", 1), session);
            flow.offer(Tuple.from("evan", 2), session);
            flow.offer(Tuple.from("will", 3), session);
            flow.offer(Tuple.from("evan", 4), session);
            flow.offer(Tuple.from("will", 5), session);
            token.complete();
            FlowsTestUtil.waitUntil(() -> reduced.size() == 2);
            assertEquals(9, reduced.stream().filter(t -> t.first().equals("will")).findAny().get().second());
            assertEquals(6, reduced.stream().filter(t -> t.first().equals("evan")).findAny().get().second());
        }

        @Test
        void test_reduce_under_preserved_order() {
            StringBuilder result = new StringBuilder();
            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks)
                    .map(i -> {
                        SleepUtil.sleep((6 - i) * 20L);
                        return i * 10;
                    })
                    .reduce(() -> "", (acc, i) -> acc + "|" + i)
                    .map(i -> i.substring(1))
                    .map(i -> i + "|reduce resuming")
                    .close(r -> result.append(r.get().getData()));
            FlowSession session = new FlowSession(true);
            Window token = session.begin();
            for (int i = 1; i < 6; i++) {
                flow.offer(i, session);
                SleepUtil.sleep(10);
            }
            SleepUtil.sleep(100);
            token.complete();

            FlowsTestUtil.waitUntil(() -> result.length() == 14, 1000);
            assertEquals("10|20|30|40|50|reduce resuming", result.toString());
        }

        @Test
        void test_unified_reduce() {
            List<Integer> result = new ArrayList<>();
            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks)
                    .reduce((acc, i) -> acc + i)
                    .unify()//go back to unbounded stream
                    .window(2)
                    .reduce((acc, i) -> acc + i)
                    .just(i -> result.add(i))
                    .close();
            flow.offer(new Integer[] {1, 1, 1, 1});
            flow.offer(new Integer[] {2, 20, 200, 2000});
            flow.offer(new Integer[] {3, 30, 300, 3000});
            flow.offer(new Integer[] {4, 40, 400, 4000});
            FlowsTestUtil.waitUntil(() -> result.size() == 4, 1000);
            //            waitUntil(() -> result.size()==2, 1000);
            assertEquals(2226, result.get(0));
            assertEquals(7777, result.get(1));
        }

        @Test
        void test_nested_reduce() {
            List<Integer> result = new ArrayList<>();
            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks)
                    .window(2)
                    .reduce((acc, i) -> acc + i)
                    .window(2)
                    .reduce((acc, i) -> acc + i)
                    .just(i -> result.add(i))
                    .close();
            flow.offer(new Integer[] {1, 1, 2, 2, 3, 3, 4, 4, 5});
            FlowsTestUtil.waitUntil(() -> result.size() == 3, 1000);
            assertEquals(6, result.get(0));
            assertEquals(14, result.get(1));
            assertEquals(5, result.get(2));
        }

        @Test
        void test_buffer() {
            AtomicInteger counter = new AtomicInteger();
            Flows.<Integer>create(repo, messenger, locks)
                    .window(3)
                    .buffer()
                    .flatMap(i -> {
                        return Flows.flux(i.toArray(new Integer[0]));
                    })
                    .just(value -> counter.set(counter.get() + 1))
                    .close()
                    .offer(new Integer[] {1, 2, 3, 4, 5, 6});
            FlowsTestUtil.waitUntil(() -> counter.get() == 6);
        }

        @Test
        void should_get_one_result_when_reduce_given_multi_flatmap_data() {
            AtomicInteger counter = new AtomicInteger();
            Flows.<Integer>create(repo, messenger, locks)
                    .flatMap(i -> {
                        return Flows.flux(i * 10, i * 10 + 1);
                    })
                    .reduce(() -> 0, (acc, value) -> {
                        System.out.println("reduce value=" + value + ", acc=" + (acc + value));
                        return acc + value;
                    })
                    .just(value -> {
                        System.out.println("value=" + value);
                    })
                    .close(r -> counter.set(r.get().getData()))
                    .offer(new Integer[] {1, 2});
            FlowsTestUtil.waitUntil(() -> counter.get() != 0);

            Assertions.assertEquals(62, counter.get());
        }

        @Test
        void should_get_ordered_result_when_reduce_given_multi_flatmap_data_and_preserved() {
            AtomicReference<String> result = new AtomicReference<>();
            FlowSession flowSession = new FlowSession(true);
            Window window = flowSession.begin();
            Flows.<Integer>create(repo, messenger, locks)
                    .flatMap(i -> Flows.flux(i * 10, i * 10 + 1))
                    .reduce(() -> "", (acc, value) -> {
                        System.out.println("reduce value=" + value + ", acc=" + (acc + value));
                        return acc + value;
                    })
                    .close(r -> result.set(r.get().getData()))
                    .offer(new Integer[] {1, 2, 3}, flowSession);
            window.complete();
            FlowsTestUtil.waitUntil(() -> result.get() != null);

            Assertions.assertNotNull(result.get());
            Assertions.assertEquals("101120213031", result.get());
        }

        @Test
        @DisplayName("流程实例parallel节点以及fork节点以及join节点all模式流转逻辑")
        void testFitStreamWithForkJoinAll() {
            TestData input = new TestData();
            List<TestData> output1 = new ArrayList<>();
            Flows.<TestData>create(repo, messenger, locks)
                    // 执行后: f=1;s=50;t=100
                    .just(i -> i.first(1).second(50).third(100))
                    // 执行后: f=0;s=50;t=100
                    .just(i -> i.first--)
                    // 执行后: f=4;s=50;t=100
                    .parallel()
                    .fork(node -> node.just(i -> i.first += 4))
                    // 执行后: f=4;s=49;t=100
                    .fork(node -> node.just(i -> i.second--)
                            // 执行后: f=4;s=54;t=100
                            .just(i -> i.second += 5))
                    // 执行后: f=4;s=54;t=102
                    .fork(node -> node.just(i -> i.third += 2))
                    .join(() -> input, (acc, data) -> data)
                    .close(r -> output1.add(r.get().getData()))
                    .offer(input);
            FlowsTestUtil.waitSingle(() -> output1);
            assertEquals(160, output1.get(0).total());
        }

        @Test
        @DisplayName("流程实例parallel节点以及fork节点以及join节点either模式流转逻辑")
        void testFitStreamWithForkJoinEither() {
        }

        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testConditionsMatchTo() {
            List<TestData> output = new ArrayList<>();
            ProcessFlow<TestData> flow = Flows.<TestData>create(repo, messenger, locks)
                    .just(i -> i.first++)
                    .id("plusF")
                    .just(i -> i.second++)
                    .id("plusS")
                    .conditions()
                    .matchTo(i -> i.first < 20, node -> node.to("plusF"))
                    .matchTo(i -> i.second < 20, node -> node.to("plusS"))
                    .others(i -> i)
                    .close(r -> output.add(r.get().getData()));
            TestData input = new TestData();
            flow.offer(input.first(11).second(0).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(20, output.get(0).first);
            assertEquals(20, output.get(0).second);
            assertEquals(0, output.get(0).third);
            output.clear();
        }

        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testConditionsMatchToAndMatch() {
            List<TestData> output = new ArrayList<>();
            ProcessFlow<TestData> flow = Flows.<TestData>create(repo, messenger, locks)
                    .just(i -> i.third++)
                    .id("plusT")
                    .just(i -> i.first++)
                    .id("plusF")
                    .just(i -> i.second++)
                    .id("plusS")
                    .conditions()
                    .matchTo(i -> i.first < 20, node -> node.to("plusF"))
                    .matchTo(i -> i.second < 20, node -> node.to("plusS"))
                    .match(i -> i.third < 5, node -> node.just(i -> i.third += 5))
                    .matchTo(i -> i.third < 20, node -> node.to("plusT"))
                    .others(i -> i)
                    .close(r -> output.add(r.get().getData()));
            TestData input = new TestData();
            flow.offer(input.first(11).second(0).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(20, output.get(0).first);
            assertEquals(20, output.get(0).second);
            assertEquals(6, output.get(0).third);
            output.clear();

            flow.offer(input.first(11).second(0).third(12));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(27, output.get(0).first);
            assertEquals(27, output.get(0).second);
            assertEquals(20, output.get(0).third);
            output.clear();
        }

        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testFitStreamWithConditionNewJust() {
            List<TestData> output = new ArrayList<>();
            // test conditions and others for just
            ProcessFlow<TestData> flow = Flows.<TestData>create(repo, messenger, locks)
                    .conditions()
                    .match(i -> i.first > 10, flowNode -> flowNode.just(value -> {
                        value.first++;
                    }).map(value -> {
                        value.first += 10;
                        return value;
                    }))
                    .match(i -> i.second > 10, flowNode -> flowNode.just(value -> {
                        value.second++;
                    }))
                    .others(flowNode -> flowNode.map(value -> {
                        value.third++;
                        return value;
                    }))
                    .close(r -> output.add(r.get().getData()));
            TestData input = new TestData();
            flow.offer(input.first(11).second(0).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(22, output.get(0).first);
            assertEquals(0, output.get(0).second);
            assertEquals(0, output.get(0).third);
            output.clear();

            flow.offer(input.first(0).second(11).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(0, output.get(0).first);
            assertEquals(12, output.get(0).second);
            assertEquals(0, output.get(0).third);
            output.clear();

            flow.offer(input.first(0).second(0).third(11));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(0, output.get(0).first);
            assertEquals(0, output.get(0).second);
            assertEquals(12, output.get(0).third);
        }

        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testFitStreamWithConditionsTo() {
            List<Integer> output = new ArrayList<>();
            // test conditions and others for just
            ProcessFlow<TestData> flow = Flows.<TestData>create(repo, messenger, locks)
                    .just(i -> i.third++)
                    .id("tPlus")
                    .just(i -> i.third += 20)
                    .id("tPlusPlus")
                    .conditions()
                    .matchTo(i -> i.third < 20, node -> node.to("tPlus"))
                    .match(i -> i.first > 10, flowNode -> flowNode.map(value -> ++value.first))
                    .match(i -> i.second > 10,
                            flowNode -> flowNode.conditions().match(i -> i.first > 0, node -> node.just(value -> {
                                value.third++;
                            })).others(node -> node.just(value -> {
                                value.third--;
                            })).map(value -> value.third))
                    .matchTo(i -> i.third < 50, node -> node.just(i -> i.first++).to("tPlusPlus"))
                    .others(flowNode -> flowNode.map(value -> {
                        return ++value.third;
                    }))
                    .close(r -> output.add(r.get().getData()));
            TestData input = new TestData();
            flow.offer(input.first(11).second(0).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(12, output.get(0));
            output.clear();

            flow.offer(input.first(-10).second(12).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(20, output.get(0));
            output.clear();

            flow.offer(input.first(0).second(0).third(-5));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(58, output.get(0));
        }

        /**
         * 1. dataStart 需要wrapper
         * 2. match 去掉context
         * 3. close里面的context也去掉，不暴露
         */
        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testFitStreamWithConditions() {
            List<Integer> output = new ArrayList<>();
            // test conditions and others for just
            ProcessFlow<TestData> flow = Flows.<TestData>create(repo, messenger, locks)
                    .conditions()
                    .match(i -> i.first > 10, flowNode -> flowNode.map(value -> ++value.first).just(i -> {}))
                    .match(i -> i.second > 10,
                            flowNode -> flowNode.conditions().match(i -> i.first > 0, node -> node.just(value -> {
                                value.third++;
                            })).others(node -> node.just(value -> {
                                value.third--;
                            })).map(value -> value.third))
                    .others(flowNode -> flowNode.map(value -> {
                        return ++value.third;
                    }))
                    .close(r -> output.add(r.get().getData()));
            TestData input = new TestData();
            flow.offer(input.first(11).second(0).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(12, output.get(0));
            output.clear();

            flow.offer(input.first(10).second(12).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(1, output.get(0));
            output.clear();

            flow.offer(input.first(-10).second(12).third(0));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(-1, output.get(0));
            output.clear();

            flow.offer(input.first(0).second(0).third(13));
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(14, output.get(0));
        }

        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testFitStreamWithConditionsEmptyOthers() {
            TestData input = new TestData();
            List<Integer> output = new ArrayList<>();
            // test conditions and others for just
            ProcessFlow<TestData> flow = Flows.<TestData>create(repo, messenger, locks)
                    .conditions()
                    .match(i -> i.first > 10, flowNode -> flowNode.map(value -> ++value.first))
                    .others()
                    .close(r -> output.add(r.get().getData()));

            flow.offer(input.first(0).second(0).third(0));
            FlowsTestUtil.waitUntil(() -> false);
            assertEquals(0, output.size());

            TestData input2 = new TestData();
            flow.offer(input2.first(11).second(0).third(0));
            FlowsTestUtil.waitUntil(() -> false);
            assertEquals(1, output.size());
            assertEquals(12, output.get(0));
        }

        @Test
        @DisplayName("流程实例source数据源")
        void testDataDrivenSource() {
            Emitter<Integer, FlowSession> emitter = new Emitter<Integer, FlowSession>() {
                private EmitterListener<Integer, FlowSession> handler;

                @Override
                public void register(EmitterListener<Integer, FlowSession> handler) {
                    this.handler = handler;
                }

                @Override
                public void unregister(EmitterListener<Integer, FlowSession> listener) {
                    this.handler = null;
                }

                @Override
                public void emit(Integer data, FlowSession trance) {
                    this.handler.handle(data, trance);
                }
            };
            AtomicInteger counter = new AtomicInteger();
            // source: 数据发射源
            Flows.source(emitter).map(value -> value + 10).just(counter::set).offer();
            emitter.emit(10);
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals(20, counter.get());
        }

        @Test
        @DisplayName("流程实例just节点mono模式流转逻辑")
        void testFitStreamCreateFromSingleDataProducer() {
            List<TestData> output = new ArrayList<>();
            Flows.mono(new TestData().first(10).second(10).third(10))
                    .just(i -> i.first++)
                    .just(i -> output.add(i))
                    .offer();
            FlowsTestUtil.waitSingle(() -> output);
            assertEquals(11, output.get(0).first);
        }

        @Test
        @DisplayName("流程实例reduce节点flux模式流转逻辑")
        void testFitStreamCreateFromListDataProducer() {
        }

        @Test
        @Disabled(
                "流程之间跳转存在两个问题：1、版本streamId不一样，导致跳转到目标流程根据版本查不到数据；2、跳转的节点存在多个to，并发无法控制")
        @DisplayName("流程实例just节点以及alias节点流转逻辑")
        void testFitStreamCrossStream() {
            AtomicBoolean f1Result = new AtomicBoolean(false);
            AtomicInteger f2Result = new AtomicInteger(0);
            AtomicInteger f3Result = new AtomicInteger(0);

            Flows.<TestData>create(repo, messenger, locks)
                    .just(data -> data.third++)
                    .just(data -> data.first++)
                    .just(data -> data.second++)
                    .close(r -> {
                        f1Result.set(true);
                        f3Result.set(r.get().getData().total());
                    });

            ProcessFlow<Integer> flow2 = Flows.<Integer>create(repo, messenger, locks)
                    .map(i -> i + 100)
                    .close(r -> f2Result.set(r.get().getData()));

            flow2.offer(100);
            FlowsTestUtil.waitMillis(Collections::emptyList, 50);
            assertTrue(f1Result.get());
            assertEquals(0, f2Result.get());
            assertEquals(103, f3Result.get());

            // offer data with act2 come back
            f1Result.set(false);
            flow2.offer(100);
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertFalse(f1Result.get());
            assertEquals(202, f2Result.get());
        }

        /**
         * act1.get()的节点在其自身流程中有一个to节点(just(d -> d.f++))
         * 在跳转流程中有一个to节点(close(r -> f2Result.set(r.get())))
         * 并发会导致一个context同时发送到两个节点处理，中间状态错乱
         * when.to的方式同时只允许一条边是满足条件的，以下用例不正确
         * .when(i -> new TestData().first(i))
         * .to(act1.get())
         * .when(i -> i, i -> true)
         */
        @Test
        @Disabled(
                "流程之间跳转存在两个问题：1、版本streamId不一样，导致跳转到目标流程根据版本查不到数据；2、跳转的节点存在多个to，并发无法控制")
        @DisplayName("流程实例垮流程节点流转逻辑")
        void testFitStreamNotifyCrossStream() {
            AtomicInteger f1Result = new AtomicInteger(0);
            AtomicInteger f2Result = new AtomicInteger(0);

            Flows.<TestData>create(repo, messenger, locks)
                    .just(data -> data.third++)
                    .just(data -> data.first++)
                    .just(data -> data.second++)
                    .close(callback -> f1Result.set(callback.get().getData().total()));

            Flows.<Integer>create(repo, messenger, locks)
                    .map(i -> i + 100)
                    .close(r -> f2Result.set(r.get().getData()))
                    .offer(100);

            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals(103, f1Result.get());
            assertEquals(200, f2Result.get());
        }

        @Test
        @DisplayName("流程实例异常处理流转逻辑")
        void testExceptionHandleForFitStream() {
            AtomicReference<TestData> output = new AtomicReference<>();
            // 单节点错误处理
            Flows.<TestData>create(repo, messenger, locks).just(data -> data.first(100)).just(data -> {
                if (data.first < 120) {
                    throw new IllegalArgumentException();
                } else {
                    data.second(100);
                }
            }).error((error, retryable, contexts) -> {
                contexts.get(0).getData().first(120);
                contexts.forEach(context -> context.setStatus(READY));
                retryable.retry(contexts);
            }).close(callback -> output.set(callback.get().getData())).offer(new TestData());
            FlowsTestUtil.waitUntil(() -> output.get() != null, 2000);
            assertEquals(120, output.get().first);
            assertEquals(100, output.get().second);

            // 整体错误处理
            Flows.<TestData>create(repo, messenger, locks).just(data -> data.first(100)).just(data -> {
                if (data.first < 120) {
                    throw new IllegalArgumentException();
                } else {
                    data.second(100);
                }
            }).close(callback -> output.set(callback.get().getData()), (exception, retryable, contexts) -> {
                ObjectUtils.<TestData>cast(contexts.get(0).getData()).first(120);
                contexts.forEach(context -> context.setStatus(READY));
                retryable.retry(contexts);
            }).offer(new TestData());
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals(120, output.get().first);
            assertEquals(100, output.get().second);
        }

        @Test
        void test_flow_subscribe_flow() {
            AtomicInteger out1 = new AtomicInteger();
            // 第一条流:start->node1->end
            ProcessFlow<Integer> flow1 = Flows.<Integer>create().map(Object::toString).close();
            // 第二条流:start->node->handle_node->end
            ProcessFlow<String> flow2 = Flows.<String>create()
                    .map(value -> value + "00")
                    .id("sender")
                    .map(value -> Integer.parseInt(value) + 100)
                    .close(callback -> out1.set(callback.get().getData()));
            // 从中间节点offer数据flow1.end节点的数据
            flow2.offer("sender", flow1);
            flow1.offer(100);
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals(200, out1.get());

            AtomicReference<String> out2 = new AtomicReference<>();
            AtomicReference<String> out3 = new AtomicReference<>();
            State<Integer, Integer, Integer, ProcessFlow<Integer>> node1 = Flows.<Integer>create().map(i -> i + 100);
            flow1 = node1.just(i -> out2.set(i.toString() + "flow1")).close();
            Flows.<Integer>create().just(i -> out3.set(i.toString() + "flow2")).close().offer(node1);
            flow1.offer(100);
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals("200flow1", out2.get());
            assertEquals("200flow2", out3.get());
        }

        @Test
        void test_node_subscribe_data() {
            AtomicInteger out1 = new AtomicInteger();
            ProcessFlow<String> flow2 = Flows.<String>create()
                    .map(value -> value + "00")
                    .id("sender")
                    .map(value -> Integer.parseInt(value) + 100)
                    .close(callback -> out1.set(callback.get().getData()));
            flow2.offer("2");
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals(300, out1.get());
            out1.set(0);

            // 从中间节点offer数据
            flow2.offer("sender", "100");
            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals(200, out1.get());
        }

        @Test
        void test_process() {
            AtomicInteger counter = new AtomicInteger();
            Flows.<Integer>create()
                    .<Integer>process((data, ctx, collector) -> {
                        ctx.setState("me", "will");
                        collector.collect(10);
                        collector.collect(20);
                        collector.collect(30);
                        collector.collect(40);
                        collector.collect(50);
                    })
                    .map(Object::toString)
                    .map((data, ctx) -> data + ctx.getState("me").toString())
                    .close(callback -> counter.set(counter.get() + 1))
                    .offer(0);
            FlowsTestUtil.waitUntil(() -> counter.get() >= 5);
            assertEquals(5, counter.get());
        }

        @Test
        void test_mermaid() {
            ProcessFlow<Integer> flow = Flows.<Integer>create()
                    .map(i -> i * 10)
                    .conditions()
                    .match(i -> i > 10, p -> p.map(i -> i - 10))
                    .match(i -> i == 9, p -> p.map(i -> i + 9))
                    .others()
                    .parallel()
                    .fork(p -> p.map(i -> i + 1))
                    .fork(p -> p.map(i -> i * 2))
                    .join(() -> "", (acc, i) -> acc + i.toString())
                    .close();
            assertEquals("start((Start))"
                    + System.lineSeparator() + "start-->node0(map)"
                    + System.lineSeparator() + "node9-->node3"
                    + System.lineSeparator() + "node8-->node6"
                    + System.lineSeparator() + "node6-->end7((End))"
                    + System.lineSeparator() + "node5-->node6([+])"
                    + System.lineSeparator() + "node4-->node8(map)"
                    + System.lineSeparator() + "node4-->node5(map)"
                    + System.lineSeparator() + "node3-->node4{{=}}"
                    + System.lineSeparator() + "node2-->node3([+])"
                    + System.lineSeparator() + "node10-->node3"
                    + System.lineSeparator() + "node1-->node9(map)"
                    + System.lineSeparator() + "node1-->node2(map)"
                    + System.lineSeparator() + "node1-->node10(map)"
                    + System.lineSeparator() + "node0-->node1{?}", new Mermaid(flow).get());
        }

        @Test
        void test_flow_flat_map() {
            List<String> result = new ArrayList<>();
            Flows.<Integer>create().flatMap(num -> {
                String[] maps = new String[num];
                for (int i = 0; i < num; i++) {
                    maps[i] = "flat map ";
                }
                return Flows.flux(maps);
            }).just(value -> result.add(value)).close().offer(4);
            FlowsTestUtil.waitUntil(() -> result.size() == 4);
            assertEquals(4, result.size());
        }

        private <T> Supplier<List<FlowContext<T>>> contextSupplier(FlowContextRepo repo, String traceId,
                String metaId, FlowNodeStatus status) {
            return () -> {
                List<FlowContext<T>> all = repo.getContextsByTrace(traceId);
                return all.stream()
                        .filter(context -> context.getPosition().equals(metaId))
                        .filter(context -> context.getStatus() == status)
                        .collect(Collectors.toList());
            };
        }

        @Test
        @DisplayName("执行异常时，从repo中清除错误的context")
        void testCleanErrorContext() {
            long[] data = {1};
            FlowContextMemoRepo testRepo = new FlowContextMemoRepo(false);
            ProcessFlow<Integer> flowTest = Flows.<Integer>create(testRepo, messenger, locks)
                    .id("flow test start node")
                    .map(i -> {
                        if (i == 1) {
                            throw new FlowTestException();
                        }
                        return i;
                    })
                    .close(r -> data[0] = r.get().getData());
            flowTest.setId("flow test");
            String traceId = flowTest.offer(1);

            FlowsTestUtil.waitFortyMillis(Collections::emptyList);
            assertEquals("flow test", flowTest.getId());
            assertEquals(0, testRepo.getContextsByTrace(traceId).size());
        }

        @Test
        void test_flow_flat_map_with_reduce_under_cold_stream() {
            List<Integer> result = new ArrayList<>();
            ProcessFlow<Integer> flow = Flows.<Integer>create()
                    .flatMap(num -> {
                        Integer[] maps = new Integer[num];
                        for (int i = 0; i < num; i++) {
                            maps[i] = i * 10;
                        }
                        return Flows.flux(maps);
                    })
                    .map(i -> i * 10)
                    .reduce(() -> 0, Integer::sum)
                    .just(i -> result.add(i))
                    .close();
            flow.offer(new Integer[] {2, 3});
            FlowsTestUtil.waitUntil(() -> result.size() == 1);
            assertEquals(1, result.size());
            assertEquals(400, result.get(0));
        }

        @Test
        void test_flow_flat_map_with_reduce_under_hot_stream() throws InterruptedException {
            List<Integer> result = new ArrayList<>();
            ProcessFlow<Integer> flow = Flows.<Integer>create()
                    .flatMap(num -> {
                        Integer[] maps = new Integer[num];
                        for (int i = 0; i < num; i++) {
                            maps[i] = i * 10;
                        }
                        return Flows.flux(maps);
                    })
                    .map(i -> i * 10)
                    .reduce(() -> 0, Integer::sum)
                    .just(i -> result.add(i))
                    .close();
            FlowSession session = new FlowSession();
            Window window = session.begin();
            flow.offer(2, session);
            Thread.sleep(10);
            flow.offer(3, session);
            Thread.sleep(400);

            window.complete();
            FlowsTestUtil.waitUntil(() -> result.size() == 1, 1000);
            assertEquals(1, result.size());
            assertEquals(400, result.get(0));
        }

        @Test
        void test_flatMap_under_preserved_order() throws InterruptedException {
            StringBuilder result = new StringBuilder();
            final int count = 4;
            final int flatmapSize = 2;
            ProcessFlow<Integer> flow = Flows.<Integer>create(repo, messenger, locks)
                    .flatMap(num -> {
                        SleepUtil.sleep((count - num) * 20L);
                        String[] maps = new String[flatmapSize];
                        for (int i = 0; i < flatmapSize; i++) {
                            maps[i] = num + "-" + i;
                        }
                        return Flows.flux(maps);
                    })
                    .map(s -> "|" + s)
                    .reduce((acc, i) -> acc + i)
                    .map(i -> result.append(i.substring(1)))
                    .close();
            FlowSession session = new FlowSession(true);
            Window window = session.begin();
            for (int i = 0; i < count; i++) {
                flow.offer(i + 1, session);
                Thread.sleep(10);
            }
            window.complete();

            FlowsTestUtil.waitUntil(() -> !result.isEmpty(), 1000);
            assertEquals("1-0|1-1|2-0|2-1|3-0|3-1|4-0|4-1", result.toString());
        }

        @Test
        void try_a_complicated_map_reduce() throws InterruptedException {
            List<String> result = new ArrayList<>();
            ProcessFlow<Integer> flow = Flows.<Integer>create()
                    .map(i -> i * 10)
                    .reduce(Integer::sum)
                    .map(i -> i + "unit")
                    .just(i -> result.add(i))
                    .close();
            flow.offer(new Integer[] {1, 2, 3, 4, 5});

            FlowSession session = new FlowSession();
            Window window = session.begin();
            for (int i = 5; i <= 6; i++) {
                flow.offer(new Integer[] {i, i + 1}, session);
            }
            Thread.sleep(400);
            window.complete();

            FlowsTestUtil.waitUntil(() -> result.size() == 2, 1000);
            assertEquals("150unit", result.get(0));
            assertEquals("240unit", result.get(1));
        }
    }
}
