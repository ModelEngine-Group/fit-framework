/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import modelengine.fel.engine.activities.FlowCallBack;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.flows.Conversation;
import modelengine.fel.engine.utils.AiFlowTestData;
import modelengine.fel.engine.utils.FlowsTestUtils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI 流程基础表达式的测试。
 *
 * @author 刘信宏
 * @since 2024-04-29
 */
@DisplayName("测试 AiFlow 的基础表达式")
public class AiFlowBasicExpressionTest {
    @Nested
    @DisplayName("测试基础数据处理表达式")
    class BasicDataProcess {
        @Test
        @DisplayName("普通会话，map和just节点成功消费数据")
        void shouldOkWhenOfferDataWithNormalConverse() {
            AiProcessFlow<AiFlowTestData, String> flow = AiFlows.<AiFlowTestData>create()
                    .just(input -> input.first().set(1))
                    .map(input -> String.valueOf(input.total()))
                    .close();
            StringBuilder answer = new StringBuilder();
            flow.converse().doOnConsume(answer::append).offer(new AiFlowTestData()).await();
            assertEquals("1", answer.toString());
        }

        @Test
        @DisplayName("绑定自定义上下文，map和just节点成功消费自定义信息")
        void shouldOkWhenOfferDataWithBindingCustomContext() {
            AiProcessFlow<AiFlowTestData, String> flow = AiFlows.<AiFlowTestData>create()
                    .just((input, ctx) -> input.first().getAndAdd(ctx.<Integer>getState("key0")))
                    .map((input, ctx) -> String.valueOf(input.total()) + ctx.getState("key1"))
                    .<String>process(((input, ctx, collector) -> collector.collect(input)))
                    .close();

            StringBuilder answer = new StringBuilder();
            flow.converse()
                    .bind("key0", 2)
                    .bind("key1", "value1")
                    .doOnConsume(answer::append)
                    .offer(new AiFlowTestData())
                    .await(500, TimeUnit.MILLISECONDS);
            assertEquals("2value1", answer.toString());
        }

        @Test
        @DisplayName("测试AiFlow的flatMap能力")
        void shouldOkWhenCreateAiFlowWithFlatMap() {
            List<String> result = new ArrayList<>();
            AiProcessFlow<Integer, String> flow = AiFlows.<Integer>create().flatMap(num -> {
                String[] maps = new String[num];
                for (int i = 0; i < num; i++) {
                    maps[i] = String.valueOf(i);
                }
                return AiFlows.flux(maps);
            }).just(value -> result.add(value)).close();
            flow.converse().offer(4);
            FlowsTestUtils.waitUntil(() -> result.size() == 4, 1000);
            assertThat(result).hasSize(4).containsSequence("0", "1", "2", "3");
        }

        @Test
        @DisplayName("测试AiFlow的回调能力")
        void shouldOkWhenCreateAiFlowWithCallBack() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            List<String> result = new ArrayList<>();
            FlowCallBack<String> flowCallBack =
                    FlowCallBack.<String>builder().doOnFinally(latch::countDown).doOnConsume(result::add).build();

            AiProcessFlow<Integer, String> flow =
                    AiFlows.<Integer>create().map(num -> String.valueOf(num + 1)).close(flowCallBack);
            flow.converse().offer(4);
            // 仅为了验证流程的 doOnFinally
            latch.await();
            assertThat(result).hasSize(1).containsSequence("5");
        }
    }

    @Nested
    @DisplayName("测试数据聚合相关表达式")
    class GatherData {
        @Test
        @DisplayName("无window的reduce数据聚合")
        void shouldOkWhenReduceWithoutWindow() {
            AiProcessFlow<Integer, Integer> flow = AiFlows.<Integer>create().reduce(() -> 0, Integer::sum).close();

            List<Integer> counters = new ArrayList<>();
            // 逐个注入，reduce不起作用
            for (int i = 0; i < 4; i++) {
                flow.converse().doOnConsume(counters::add).offer(i + 1);
            }
            FlowsTestUtils.waitUntil(() -> counters.size() == 4, 1000);
            assertThat(counters).hasSize(4).contains(1, 2, 3, 4);

            counters.clear();
            // 批量注入会将同一批次的聚合为一个
            flow.converse().doOnConsume(counters::add).offer(1, 2, 3, 4).await(500, TimeUnit.MILLISECONDS);
            assertThat(counters).hasSize(1).contains(10);
        }

        @Test
        @DisplayName("带有window的reduce数据聚合")
        void shouldOkWhenReduceWithWindow() {
            AiProcessFlow<Integer, Integer> flow = AiFlows.<Integer>create()
                    .window(2)
                    .reduce(() -> 0, Integer::sum)
                    .close();

            checkInjectDataOneBatch(flow);
        }

        @Test
        @DisplayName("buffer数据聚合, 逐个注入数据")
        void shouldOkWhenGatherWithBuffer() {
            AiProcessFlow<Integer, Integer> flow =
                    AiFlows.<Integer>create().buffer(2).map(input -> input.stream().reduce(0, Integer::sum)).close();

            checkInjectDataOneBatch(flow);
        }

        @Test
        @DisplayName("带有process节点的buffer数据聚合")
        void test_process_with_custom_state() {
            StringBuffer answer = new StringBuffer(512);

            AiFlows.<Integer>create()
                    .<Integer>process((data, ctx, collector) -> {
                        ctx.setState("key1", "value1");
                        collector.collect(data);
                        collector.collect(data + 1);
                    })
                    .map((data, ctx) -> data.toString() + ctx.getState("key0"))
                    .map((data, ctx) -> data + ctx.getState("key1"))
                    .buffer(2)
                    .close()
                    .converse()
                    .bind("key0", "value0")
                    .doOnConsume(data -> answer.append(String.join("\n", data)))
                    .offer(5)
                    .await(500, TimeUnit.MILLISECONDS);

            assertEquals("5value0value1\n6value0value1", answer.toString());
        }

        @Test
        void GivingInitStringBuilderWhenRepeatedOfferWithReducingThenOk() {
            Conversation<Integer, String> converse =
                    AiFlows.<Integer>create()
                            .<Integer>process((data, ctx, collector) -> {
                                collector.collect(data);
                                collector.collect(data + 1);
                            })
                            .window(2)
                            .reduce(StringBuilder::new, (acc, input) -> acc.append(input))
                            .map(StringBuilder::toString)
                            .close()
                            .converse();

            AtomicReference<String> result = new AtomicReference<>();
            converse.doOnConsume(result::set).offer(0).await();
            assertThat(result.get()).isEqualTo("01");

            // 验证reduce初始值重新获取，不影响后续的请求。
            result.set(null);
            converse.doOnConsume(result::set).offer(0).await();
            assertThat(result.get()).isEqualTo("01");
        }

        private void checkInjectDataOneBatch(AiProcessFlow<Integer, Integer> flow) {
            // 逐个注入
            List<Integer> counters = Collections.synchronizedList(new ArrayList<>());
            flow.converse().doOnConsume(counters::add).offer(1, 2, 3, 4);
            FlowsTestUtils.waitUntil(() -> counters.size() == 2, 1000);
            assertEquals(2, counters.size());
        }
    }

    @Nested
    @DisplayName("测试条件分支")
    class ConditionBranchTest {
        @Test
        void test_create_ai_flow_conditions() {
            AtomicInteger result = new AtomicInteger(0);
            AiFlows.<String>create()
                    .map(data -> data + "1")
                    .id("plus1")
                    .map(data -> data + "2")
                    .id("plus2")
                    .conditions()
                    .matchTo(data -> data.length() < 4, node -> node.to("plus1"))
                    .match(data -> data.length() > 5, node -> node.map(i -> Integer.parseInt(i)))
                    .matchTo(data -> data.length() < 10, node -> node.just(i -> {
                    }).to("plus2"))
                    .others()
                    .close(result::set)
                    .converse()
                    .offer("3");
            FlowsTestUtils.waitUntil(() -> result.get() != 0);
            assertEquals(312122, result.get());
        }

        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testConditionsMatchTo() {
            List<AiFlowTestData> output = new ArrayList<>();
            AiProcessFlow<AiFlowTestData, AiFlowTestData> flow = AiFlows.<AiFlowTestData>create()
                    .just(data -> data.first().incrementAndGet())
                    .id("plusF")
                    .just(data -> data.second().incrementAndGet())
                    .id("plusS")
                    .conditions()
                    .matchTo(data -> data.first().get() < 20, node -> node.to("plusF"))
                    .matchTo(data -> data.second().get() < 20, node -> node.to("plusS"))
                    .others(i -> i)
                    .close(output::add);
            flow.converse().offer(new AiFlowTestData(11, 0, 0)).await();
            assertThat(output).hasSize(1);
            assertThat(output.get(0).first().get()).isEqualTo(20);
            assertThat(output.get(0).second().get()).isEqualTo(20);
            assertThat(output.get(0).third().get()).isEqualTo(0);
        }

        @Test
        @DisplayName("流程实例condition节点以及match节点以及others节点流转逻辑")
        void testConditionsMatchToAndMatch() {
            List<AiFlowTestData> output = new ArrayList<>();
            AiProcessFlow<AiFlowTestData, AiFlowTestData> flow = AiFlows.<AiFlowTestData>create()
                    .just(data -> data.third().incrementAndGet())
                    .id("plusT")
                    .just(data -> data.first().incrementAndGet())
                    .id("plusF")
                    .just(data -> data.second().incrementAndGet())
                    .id("plusS")
                    .conditions()
                    .matchTo(data -> data.first().get() < 20, node -> node.to("plusF"))
                    .matchTo(data -> data.second().get() < 20, node -> node.to("plusS"))
                    .match(data -> data.third().get() < 5, node -> node.just(data -> data.third().getAndAdd(5)))
                    .matchTo(data -> data.third().get() < 20, node -> node.to("plusT"))
                    .others(i -> i)
                    .close(output::add);
            flow.converse().offer(new AiFlowTestData(11, 0, 0)).await();

            assertThat(output).hasSize(1);
            assertThat(output.get(0).first().get()).isEqualTo(20);
            assertThat(output.get(0).second().get()).isEqualTo(20);
            assertThat(output.get(0).third().get()).isEqualTo(6);
            output.clear();

            flow.converse().offer(new AiFlowTestData(11, 0, 12)).await();
            assertThat(output).hasSize(1);
            assertThat(output.get(0).first().get()).isEqualTo(27);
            assertThat(output.get(0).second().get()).isEqualTo(27);
            assertThat(output.get(0).third().get()).isEqualTo(20);
            output.clear();
        }
    }
}
