/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators;

import static modelengine.fel.engine.utils.FlowsTestUtils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.util.Tip;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.flows.Conversation;
import modelengine.fel.engine.operators.models.ChatFlowModel;
import modelengine.fel.engine.operators.patterns.AbstractAgent;
import modelengine.fel.engine.operators.prompts.Prompts;
import modelengine.fit.waterflow.domain.context.StateContext;
import modelengine.fit.waterflow.domain.utils.SleepUtil;
import modelengine.fitframework.flowable.Choir;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 模型算子测试。
 *
 * @author 刘信宏
 * @since 2024-05-08
 */
public class ModelTest {
    private final ChatFlowModel model = new ChatFlowModel((prompt, chatOption) -> Choir.create(emitter -> {
        if (chatOption.stream()) {
            for (int i = 0; i < 4; i++) {
                emitter.emit(new AiMessage(String.valueOf(i)));
                SleepUtil.sleep(10);
            }
        } else {
            emitter.emit(new AiMessage(String.valueOf(0)));
        }
        emitter.complete();
    }), ChatOption.custom().model("modelName").stream(true).build());

    @Nested
    @DisplayName("阻塞模型测试")
    class BlockModelTest {
        private final AiProcessFlow<Tip, String> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("answer: {{0}}"))
                .generate(model)
                .map(ChatMessage::text)
                .close();

        @Test
        void shouldOkWhenFlowWithBlockModel() {
            AtomicReference<String> result = new AtomicReference<>();
            this.flow.converse()
                    .bind(ChatOption.custom().model("modelName-block").stream(false).build())
                    .doOnConsume(result::set)
                    .offer(Tip.fromArray("test block model"))
                    .await();
            assertThat(result.get()).isEqualTo("0");
        }

        @Test
        void shouldThrowWhenBlockModelWithException() {
            String expectedMsg = "test exception";
            ChatFlowModel exceptionModel = new ChatFlowModel((prompt, chatOption) -> Choir.create(emitter -> {
                emitter.fail(new Exception(expectedMsg));
            }), ChatOption.custom().model("modelName").stream(false).build());
            Conversation<Tip, String> exceptionConverse = AiFlows.<Tip>create()
                    .prompt(Prompts.human("{{0}}"))
                    .generate(exceptionModel)
                    .map(ChatMessage::text)
                    .close()
                    .converse();

            assertThatThrownBy(() -> exceptionConverse.offer(Tip.fromArray("test block exception"))
                    .await()).isInstanceOf(IllegalStateException.class).message().isEqualTo(expectedMsg);
        }
    }

    @Nested
    @DisplayName("流式模型测试")
    class StreamingModelTest {
        private final AiProcessFlow<Tip, String> boundStreamFlow = AiFlows.<Tip>create()
                .prompt(Prompts.human("answer: {{0}}"))
                .generate(model)
                .map(ChatMessage::text)
                .close();

        private final AiProcessFlow<Tip, String> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("answer: {{0}}"))
                .generate(model)
                .reduce(() -> "", (acc, input) -> acc + input.text())
                .close();

        @Test
        void shouldOkWhenStreamModelWithChoirEmitter() {
            AtomicReference<String> result = new AtomicReference<>();
            this.flow.converse().doOnConsume(result::set).offer(Tip.fromArray("test streaming model")).await();
            assertThat(result.get()).isEqualTo("0123");
        }

        @Test
        void shouldOkWhenBoundStreamFlowWithChoirEmitter() {
            StringBuffer sb = new StringBuffer();
            AtomicInteger cnt = new AtomicInteger(0);
            this.boundStreamFlow.converse()
                    .doOnConsume(sb::append)
                    .doOnFinally(cnt::getAndIncrement)
                    .offer(Tip.fromArray("test streaming model"))
                    .await();
            assertThat(sb.toString()).isEqualTo("0123");
            assertThat(cnt.get()).isEqualTo(1);
        }

        @Test
        void shouldOkWhenStreamModelWithMultiConversation() {
            AtomicReference<List<String>> result = new AtomicReference<>();
            result.set(new ArrayList<>());
            AtomicInteger counter = new AtomicInteger();

            AiProcessFlow<Tip, String> currFlow = AiFlows.<Tip>create()
                    .prompt(Prompts.human("{{0}}"))
                    .generate(model)
                    .reduce(() -> "", (acc, input) -> acc + input.text())
                    .map((m, ctx) -> {
                        result.updateAndGet(list -> {
                            list.add(m);
                            return list;
                        });
                        return m;
                    })
                    .close(r -> counter.incrementAndGet());

            currFlow.converse().offer(Tip.fromArray("test streaming model"));
            currFlow.converse().offer(Tip.fromArray("test streaming model2"));

            waitUntil(() -> counter.get() == 2, 1000);
            assertThat(result.get()).hasSize(2).containsSequence("0123", "0123");
        }

        @Test
        void shouldThrowWhenStreamModelWithException() {
            String expectedMsg = "test exception";
            ChatFlowModel exceptionModel = new ChatFlowModel((prompt, chatOption) -> Choir.create(emitter -> {
                emitter.fail(new Exception(expectedMsg));
            }), ChatOption.custom().model("modelName").stream(true).build());
            Conversation<Tip, String> exceptionConverse = AiFlows.<Tip>create()
                    .prompt(Prompts.human("{{0}}"))
                    .generate(exceptionModel)
                    .reduce(() -> "", (acc, input) -> acc + input.text())
                    .close()
                    .converse();

            assertThatThrownBy(() -> exceptionConverse.offer(Tip.fromArray("test streaming exception"))
                    .await()).isInstanceOf(IllegalStateException.class).message().isEqualTo(expectedMsg);
        }
    }
}
