/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.example.ai.chat.agent;

import static modelengine.fitframework.util.CollectionUtils.asParent;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatModel;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.core.util.Tip;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.operators.models.ChatFlowModel;
import modelengine.fel.engine.operators.patterns.support.DefaultAgent;
import modelengine.fel.engine.operators.prompts.Prompts;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fel.tool.service.ToolRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Agent 样例控制器（Spring Boot 版本）。
 *
 * @author 黄可欣
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/ai/example")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class AgentExampleController {
    private final AiProcessFlow<String, ChatMessage> agentFlow;
    private final ChatOption chatOption;
    private final ToolRepository toolRepository;

    public AgentExampleController(ChatModel chatModel, ToolExecuteService toolExecuteService,
            ToolRepository toolRepository, @Value("${example.model}") String modelName) {
        this.toolRepository = toolRepository;
        this.chatOption = ChatOption.custom().model(modelName).stream(false).build();
        DefaultAgent agent =
                new DefaultAgent(new ChatFlowModel(chatModel, this.chatOption), "example", toolExecuteService);
        this.agentFlow = AiFlows.<String>create()
                .map(query -> Tip.fromArray(query))
                .prompt(Prompts.human("{{0}}"))
                .delegate(agent)
                .close();
    }

    /**
     * 聊天接口。
     *
     * @param query 表示用户输入查询的 {@link String}。
     * @return 表示聊天模型生成的回复的 Map。
     */
    @GetMapping("/chat")
    public Object chat(@RequestParam("query") String query) {
        List<ToolInfo> toolInfos = asParent(toolRepository.listTool("example"));
        ChatMessage aiMessage = this.agentFlow.converse()
                .bind(ChatOption.custom(this.chatOption).tools(toolInfos).build())
                .offer(query)
                .await();
        return java.util.Map.of(
            "content", aiMessage.text(),
            "toolCalls", aiMessage.toolCalls()
        );
    }
}