/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import modelengine.fit.waterflow.domain.utils.SleepUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link Conversation} 的核心机制的测试。
 *
 * @author 刘信宏
 * @since 2024-04-29
 */
@DisplayName("测试对话核心机制")
public class ConversationTest {
    @Nested
    @DisplayName("对话异常处理")
    class ConversationException {
        String errorMsg = "test exception.";
        AiProcessFlow<String, String> exceptionFlow = AiFlows.<String>create().map(input -> input).just(input -> {
            throw new IllegalStateException(errorMsg);
        }).close();

        @Test
        @DisplayName("流程节点异常处理")
        void shouldFailWhenAsyncFlowThrowException() {
            final StringBuilder answer = new StringBuilder();
            ConverseLatch<String> latch = exceptionFlow.converse()
                    .doOnError(throwable -> answer.append(throwable.getMessage()))
                    .doOnFinally(() -> answer.append(" finally"))
                    .offer("test data");
            IllegalStateException exception =
                    assertThrows(IllegalStateException.class, () -> latch.await(500, TimeUnit.MILLISECONDS));
            assertEquals(errorMsg, exception.getMessage());
            assertEquals(errorMsg + " finally", answer.toString());
        }

        @Test
        @DisplayName("对话未完成异常处理")
        void shouldFailWhenOfferRepeated() {
            AiProcessFlow<String, String> flow = AiFlows.<String>create().just(input -> SleepUtil.sleep(50)).close();

            Conversation<String, String> converse = flow.converse();
            // 第一次对话
            converse.offer("test data");
            // 第二次对话
            IllegalStateException exception =
                    assertThrows(IllegalStateException.class, () -> converse.offer("test data1"));

            assertEquals("conversation is running.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("对话正常回调")
    class ConversationCallBack {
        AiProcessFlow<Integer, String> flow = AiFlows.<Integer>create().map(input -> String.valueOf(++input)).close();

        @Test
        @DisplayName("异步对话设置对话回调")
        void shouldOkWhenASyncFlowWithCustomCallback() {
            StringBuilder callbackAnswer = new StringBuilder();
            Conversation<Integer, String> converse = flow.converse();
            String flowAnswer = converse.doOnConsume(data -> callbackAnswer.append("answer ").append(data))
                    .doOnFinally(() -> callbackAnswer.append(" finally"))
                    .offer(5)
                    .await(500, TimeUnit.MILLISECONDS);
            String expected = "answer 6 finally";
            assertThat(callbackAnswer.toString()).isEqualTo(expected);
            assertThat(flowAnswer).isEqualTo("6");

            // 相同 Conversation 的新请求
            AtomicBoolean end = new AtomicBoolean(false);
            flowAnswer = converse.doOnFinally(() -> end.set(true)).offer(6).await();
            assertThat(end).isTrue();
            assertThat(flowAnswer).isEqualTo("7");
            // 不影响相同 Conversation 的上一次请求
            assertThat(callbackAnswer.toString()).isEqualTo(expected);
        }
    }
}
