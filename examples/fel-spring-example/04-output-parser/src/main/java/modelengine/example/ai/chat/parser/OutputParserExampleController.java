/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2026 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.example.ai.chat.parser;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatModel;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.format.OutputParser;
import modelengine.fel.core.format.json.JsonOutputParser;
import modelengine.fel.core.template.MessageTemplate;
import modelengine.fel.core.template.support.DefaultStringTemplate;
import modelengine.fel.core.template.support.HumanMessageTemplate;
import modelengine.fel.core.util.Tip;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.serialization.ObjectSerializer;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * 输出解析器样例控制器（Spring Boot 版本）。
 *
 * @author 黄可欣
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/ai/example")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class OutputParserExampleController {
    private final ChatModel chatModel;
    private final OutputParser<Demo> outputParser;
    private final MessageTemplate template;
    @Value("${example.model}")
    private String modelName;

    public OutputParserExampleController(ChatModel chatModel, @Qualifier("json") ObjectSerializer serializer) {
        this.chatModel = chatModel;
        this.outputParser = JsonOutputParser.createPartial(serializer, Demo.class);
        this.template = new HumanMessageTemplate(new DefaultStringTemplate(
                "从用户输入中提取时间，当前时间 {{ctime}}\n\n{{format}}\n\nInput: {{query}}\nOutput:\n").partial("ctime",
                LocalDate.now().toString()).partial("format", this.outputParser.instruction()));
    }

    /**
     * 聊天接口。
     *
     * @param query 表示用户输入查询的 {@link String}。
     * @return 表示聊天模型生成的回复的 {@link String}。
     */
    @GetMapping("/chat")
    public String chat(@RequestParam("query") String query) {
        ChatOption option = ChatOption.custom().model(this.modelName).stream(false).build();
        ChatMessage aiMessage =
                this.chatModel.generate(ChatMessages.from(this.template.render(Tip.from("query", query).freeze())),
                        option).first().block().get();
        return outputParser.parse(aiMessage.text()).getDate();
    }

    /**
     * 流式聊天接口。
     *
     * @param query 表示用户输入查询的 {@link String}。
     * @return 表示聊天模型生成的回复的 {@link Flux}{@code <}{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >>}。
     */
    @GetMapping(value = "/chat-stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<Map<String, Object>> chatStream(@RequestParam("query") String query) {
        ChatOption option = ChatOption.custom().model(this.modelName).stream(true).build();
        StringBuffer sb = new StringBuffer();
        Choir<String> choir =
                this.chatModel.generate(ChatMessages.from(this.template.render(Tip.from("query", query).freeze())),
                        option).map(ChatMessage::text).map(t -> {
                    sb.append(t);
                    return sb.toString();
                }).map(text -> outputParser.parse(text).getDate());
        return JdkFlowAdapter.flowPublisherToFlux(choir.map(text -> Map.of("content", text != null ? text : "")));
    }

    public static class Demo {
        @Property(description = "时间，格式为 yyyy-MM-dd")
        private String date;

        public String getDate() {
            return date;
        }
    }
}
