/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
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
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestParam;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.serialization.ObjectSerializer;

import java.time.LocalDate;

/**
 * 输出解析器样例控制器。
 *
 * @author 易文渊
 * @since 2024-08-29
 */
@Component
@RequestMapping("/ai/example")
public class OutputParserExampleController {
    private final ChatModel chatModel;
    private final OutputParser<Demo> outputParser;
    private final MessageTemplate template;
    @Value("${example.model}")
    private String modelName;

    public OutputParserExampleController(ChatModel chatModel, @Fit(alias = "json") ObjectSerializer serializer) {
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
     * @return 表示聊天模型生成的回复的 {@link Demo}。
     */
    @GetMapping("/chat")
    public Demo chat(@RequestParam("query") String query) {
        ChatOption option = ChatOption.custom().model(this.modelName).stream(false).build();
        ChatMessage aiMessage =
                this.chatModel.generate(ChatMessages.from(this.template.render(Tip.from("query", query).freeze())),
                        option).first().block().get();
        return outputParser.parse(aiMessage.text());
    }

    /**
     * 流式聊天接口。
     *
     * @param query 表示用户输入查询的 {@link String}。
     * @return 表示聊天模型生成的回复的 @{@link Choir}{@code <}{@link Demo}{@code >}。
     */
    @GetMapping("/chat-stream")
    public Choir<Demo> chatStream(@RequestParam("query") String query) {
        ChatOption option = ChatOption.custom().model(this.modelName).stream(true).build();
        StringBuffer sb = new StringBuffer();
        return this.chatModel.generate(ChatMessages.from(this.template.render(Tip.from("query", query).freeze())),
                option).map(ChatMessage::text).map(t -> {
            sb.append(t);
            return sb.toString();
        }).map(outputParser::parse);
    }

    public static class Demo {
        @Property(description = "时间，格式为 yyyy-MM-dd")
        private String date;
    }
}