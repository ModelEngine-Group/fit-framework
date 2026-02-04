/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2026 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.example.ai.chat.model;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatModel;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.chat.support.HumanMessage;
import modelengine.fitframework.flowable.Choir;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 聊天模型样例控制器（Spring Boot 版本）。
 *
 * @author 黄可欣
 * @since 2026-01-20
 */
@RestController
@RequestMapping("/ai/example")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ChatModelExampleController {
    private final ChatModel chatModel;
    @Value("${example.model}")
    private String modelName;

    public ChatModelExampleController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 聊天接口。
     *
     * @param query 表示用户输入查询的 {@link String}。
     * @return 表示聊天模型生成的回复的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam("query") String query) {
        ChatOption option = ChatOption.custom().model(this.modelName).stream(false).build();
        ChatMessage aiMessage =
                this.chatModel.generate(ChatMessages.from(new HumanMessage(query)), option).first().block().get();
        return Map.of("content", aiMessage.text(), "toolCalls", aiMessage.toolCalls());
    }

    /**
     * 流式聊天接口。
     *
     * @param query 表示用户输入查询的 {@link String}。
     * @return 表示聊天模型生成的流式回复的 {@link Flux}{@code <}{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >>}。
     */
    @GetMapping(value = "/chat-stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<Map<String, Object>> chatStream(@RequestParam("query") String query) {
        ChatOption option = ChatOption.custom().model(this.modelName).stream(true).build();
        Choir<ChatMessage> choir = this.chatModel.generate(ChatMessages.from(new HumanMessage(query)), option);
        return JdkFlowAdapter.flowPublisherToFlux(choir.map(chatMessage -> Map.of("content", chatMessage.text())));
    }
}