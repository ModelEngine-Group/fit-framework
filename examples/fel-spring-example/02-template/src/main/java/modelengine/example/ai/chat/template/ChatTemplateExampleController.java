/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2026 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.example.ai.chat.template;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatModel;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.template.MessageTemplate;
import modelengine.fel.core.template.support.HumanMessageTemplate;
import modelengine.fel.core.util.Tip;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天模板样例控制器（Spring Boot 版本）。
 *
 * @author 黄可欣
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/ai/example")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ChatTemplateExampleController {
    private final ChatModel chatModel;
    private final MessageTemplate template;
    @Value("${example.model}")
    private String modelName;

    public ChatTemplateExampleController(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.template = new HumanMessageTemplate("给我讲个关于{{adjective}}的{{content}}。");
    }

    /**
     * 聊天接口。
     *
     * @param adjective 表示主题的 {@link String}。
     * @param content 表示内容的 {@link String}。
     * @return 表示聊天模型生成的回复的 Map。
     */
    @GetMapping("/chat")
    public Object chat(@RequestParam("adjective") String adjective, @RequestParam("content") String content) {
        ChatOption option = ChatOption.custom().model(this.modelName).stream(false).build();
        ChatMessage aiMessage = this.chatModel.generate(ChatMessages.from(this.template.render(Tip.from("adjective", adjective)
                .add("content", content)
                .freeze())), option).first().block().get();
        return java.util.Map.of(
            "content", aiMessage.text(),
            "toolCalls", aiMessage.toolCalls()
        );
    }
}