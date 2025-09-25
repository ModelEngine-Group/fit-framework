/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.chat.support.HumanMessage;
import modelengine.fel.core.document.Content;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.core.memory.support.CacheMemory;
import modelengine.fel.core.util.Tip;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.flows.Conversation;
import modelengine.fel.engine.operators.prompts.Prompts;
import modelengine.fitframework.resource.web.Media;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 提示词测试。
 *
 * @author 刘信宏
 * @since 2024-05-08
 */
@DisplayName("测试提示词")
public class PromptTest {
    @Test
    @DisplayName("测试具名占位符")
    void shouldOkWhenPromptWithNamedPlaceholder() {
        final StringBuilder answer = new StringBuilder();
        AiProcessFlow<Tip, Prompt> flow = AiFlows.<Tip>create()
                .prompt(Prompts.sys("{{someone}}"), Prompts.human("{{question}}"))
                .close(r -> answer.append(r.text()));

        Conversation<Tip, Prompt> conversation = flow.converse();
        conversation.offer(new Tip().add("someone", "will").add("question", "my question")).await();

        assertThat(answer.toString()).isEqualTo("will\nmy question");
    }

    @Test
    @DisplayName("测试数字占位符")
    void shouldOkWhenPromptWithNumberPlaceholder() {
        final StringBuilder answer = new StringBuilder();
        AiProcessFlow<Tip, Prompt> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("introduce me books about {{0}} written by {{1}}"))
                .close(r -> answer.append(r.text()));

        Conversation<Tip, Prompt> conversation = flow.converse();
        conversation.offer(Tip.fromArray("javascript", "will")).await();

        assertThat(answer.toString()).isEqualTo("introduce me books about javascript written by will");
    }

    @Test
    @DisplayName("测试历史记录占位符")
    void shouldOkWhenPromptWithHistoryPlaceholder() {
        StringBuilder sb = new StringBuilder();
        AiProcessFlow<Tip, Prompt> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("{{someone}}"), Prompts.history())
                .just(input -> sb.append(input.text()).append("\n"))
                .close();

        Memory memory = new CacheMemory() {
            @Override
            public List<ChatMessage> messages() {
                return Arrays.asList(new HumanMessage("question1"), new AiMessage("answer1"));
            }
        };
        Conversation<Tip, Prompt> conversation = flow.converse().bind(memory);
        conversation.offer(Tip.from("someone", "will")).await();
        assertThat(sb.toString()).isEqualTo("will\nquestion1\nanswer1\n");
    }

    @Test
    @DisplayName("测试多媒体内容")
    void shouldOkWhenPromptWithMedia() throws MalformedURLException {
        ChatMessages chatMessages = new ChatMessages();
        AiProcessFlow<Tip, Prompt> flow = AiFlows.<Tip>create()
                .prompt(Prompts.human("answer: {{someone}}"))
                .just(input -> chatMessages.addAll(input.messages()))
                .close();

        Conversation<Tip, Prompt> conversation = flow.converse();
        Content content =
                Content.from("will", new Media(new URL("http://localhost/1.png")), new Media("image/png", "url1"));
        conversation.offer(Tip.from("someone", content)).await();

        assertThat(chatMessages.messages()).hasSize(1);
        assertThat(chatMessages.messages().get(0).text()).isEqualTo("answer: will");
        assertThat(chatMessages.messages().get(0).medias()).hasSize(2);
    }
}
