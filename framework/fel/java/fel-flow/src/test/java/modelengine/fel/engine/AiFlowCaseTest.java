/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.util.Tip;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.flows.ConverseLatch;
import modelengine.fel.engine.operators.models.ChatFlowModel;
import modelengine.fel.engine.operators.prompts.Prompts;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.StateContext;
import modelengine.fit.waterflow.domain.utils.SleepUtil;
import modelengine.fitframework.flowable.Choir;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test cases demonstrating different flow control scenarios in AI processing pipelines.
 * Contains nested test classes for specific flow control mechanisms.
 *
 * @author 宋永坦
 * @since 2025-06-11
 */
public class AiFlowCaseTest {
    @Nested
    class DesensitizeCase {
        private final ChatFlowModel model = new ChatFlowModel((prompt, chatOption) -> Choir.create(emitter -> {
            emitter.emit(new AiMessage("<think>"));
            for (int i = 0; i < 10; i++) {
                emitter.emit(new AiMessage(String.valueOf(i)));
                SleepUtil.sleep(100);
            }
            emitter.emit(new AiMessage("</think>"));
            for (int i = 100; i < 110; i++) {
                emitter.emit(new AiMessage(String.valueOf(i)));
                SleepUtil.sleep(100);
            }
            emitter.complete();
        }), ChatOption.custom().model("modelName").stream(true).build());

        private final AiProcessFlow<Tip, String> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("{{0}}"))
                .generate(model)
                .map(this::classic)
                .conditions()
                .when(chunk -> chunk.isThinkContent, input -> input)
                .others(input -> {
                    this.log(input);
                    return input;
                })
                .map(this::mockDesensitize)
                .close();

        @Test
        void run() {
            AtomicInteger counter = new AtomicInteger(0);
            long startTime = System.currentTimeMillis();
            System.out.printf("time:%s, start.\n", startTime);
            ConverseLatch<String> result = flow.converse(new FlowSession(true)).doOnConsume(answer -> {
                System.out.printf("time:%s, chunk=%s\n", System.currentTimeMillis(), answer);
                counter.incrementAndGet();
            }).offer(Tip.fromArray("hi"));
            result.await();
            System.out.printf("time:%s, cost=%s\n", System.currentTimeMillis(), System.currentTimeMillis() - startTime);
            Assertions.assertEquals(22, counter.get());
        }

        private Chunk classic(ChatMessage message, StateContext ctx) {
            if (message.text().trim().equals("<think>")) {
                ctx.setState("isThinking", true);
                return new Chunk(true, message.text());
            }
            if (message.text().trim().equals("</think>")) {
                ctx.setState("isThinking", false);
                return new Chunk(true, message.text());
            }
            if (Boolean.TRUE.equals(ctx.getState("isThinking"))) {
                return new Chunk(true, message.text());
            }
            return new Chunk(false, message.text());
        }

        private String mockDesensitize(Chunk chunk) {
            return chunk.content.replace("3", "*");
        }

        private void log(Chunk chunk) {
            System.out.println("log content:" + chunk.content);
        }

        private static class Chunk {
            private final boolean isThinkContent;
            private final String content;

            private Chunk(boolean isThinkContent, String content) {this.isThinkContent = isThinkContent;
                this.content = content;
            }
        }
    }

    /**
     * Simulates a backpressure scenario where:
     * <ol>
     *     <li>The LLM generates data (50ms per item) faster than the TTS can process it.</li>
     *     <li>TTS processing is constrained to a single thread.</li>
     *     <li>TTS processing speed is artificially slowed (100ms per item).</li>
     * </ol>
     */
    @Nested
    class BackPressureCase {
        private final ChatFlowModel model = new ChatFlowModel((prompt, chatOption) -> Choir.create(emitter -> {
            for (int i = 0; i < 10; i++) {
                emitter.emit(new AiMessage(String.valueOf(i)));
                SleepUtil.sleep(50);
            }
            emitter.complete();
            System.out.printf("time:%s, generate completed.\n", System.currentTimeMillis());
        }), ChatOption.custom().model("modelName").stream(true).build());

        private final AiProcessFlow<Tip, String> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("{{0}}"))
                .generate(model)
                .map(this::mockTTS).concurrency(1) // Limit processing to 1 concurrent thread
                .close();

        @Test
        void run() {
            AtomicInteger counter = new AtomicInteger(0);
            long startTime = System.currentTimeMillis();
            System.out.printf("time:%s, start.\n", startTime);
            ConverseLatch<String> result = flow.converse(new FlowSession(false)).doOnConsume(answer -> {
                System.out.printf("time:%s, chunk=%s\n", System.currentTimeMillis(), answer);
                counter.incrementAndGet();
            }).offer(Tip.fromArray("hi"));
            result.await();
            System.out.printf("time:%s, cost=%s\n", System.currentTimeMillis(), System.currentTimeMillis() - startTime);
            Assertions.assertEquals(10, counter.get());
        }

        private String mockTTS(ChatMessage chunk) {
            // Simulate time-consuming operation with a delay.
            SleepUtil.sleep(100);
            return chunk.text();
        }
    }

    /**
     * Demonstrates concurrent processing with balanced throughput where:
     * <ol>
     *     <li>LLM generates data at moderate pace (50ms per item)</li>
     *     <li>Downstream processing runs with 3 concurrent threads</li>
     *     <li>Processing speed is slightly slower than generation (150ms vs 50ms)</li>
     * </ol>
     */
    @Nested
    class ConcurrencyCase {
        private final ChatFlowModel model = new ChatFlowModel((prompt, chatOption) -> Choir.create(emitter -> {
            for (int i = 0; i < 10; i++) {
                emitter.emit(new AiMessage(String.valueOf(i)));
                SleepUtil.sleep(50);
            }
            emitter.complete();
        }), ChatOption.custom().model("modelName").stream(true).build());

        private final AiProcessFlow<Tip, String> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("{{0}}"))
                .generate(model)
                .map(this::mockDesensitize).concurrency(3) // Set processing to 3 concurrent thread
                .close();

        @Test
        void run() {
            AtomicInteger counter = new AtomicInteger(0);
            long startTime = System.currentTimeMillis();
            System.out.printf("time:%s, start.\n", startTime);
            ConverseLatch<String> result = flow.converse(new FlowSession(false)).doOnConsume(answer -> {
                System.out.printf("time:%s, chunk=%s\n", System.currentTimeMillis(), answer);
                counter.incrementAndGet();
            }).offer(Tip.fromArray("hi"));
            result.await();
            System.out.printf("time:%s, cost=%s\n", System.currentTimeMillis(), System.currentTimeMillis() - startTime);
            Assertions.assertEquals(10, counter.get());
        }

        private String mockDesensitize(ChatMessage chunk) {
            // Simulate slower processing at 1/3 speed of LLM generation.
            SleepUtil.sleep(150);
            return chunk.text().replace("3", "*");
        }
    }
}
