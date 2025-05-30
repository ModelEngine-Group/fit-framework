/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.patterns.support;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.chat.support.ToolMessage;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.engine.operators.models.ChatFlowModel;
import modelengine.fel.engine.operators.patterns.AbstractAgent;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fit.waterflow.domain.context.StateContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示智能体的默认实现。
 *
 * @author 易文渊
 * @since 2024-09-02
 */
public class DefaultAgent extends AbstractAgent {
    private final String namespace;
    private final ToolExecuteService toolExecuteService;

    public DefaultAgent(ChatFlowModel flowModel, String namespace, ToolExecuteService toolExecuteService) {
        super(flowModel);
        this.namespace = notBlank(namespace, "The namespace cannot be blank.");
        this.toolExecuteService = notNull(toolExecuteService, "The tool execute service cannot be null.");
    }

    @Override
    protected Prompt doToolCall(List<ToolCall> toolCalls, StateContext ctx) {
        return toolCalls.stream().map(toolCall -> {
            String text = this.toolExecuteService.execute(this.namespace, toolCall.name(), toolCall.arguments());
            return (ChatMessage) new ToolMessage(toolCall.id(), text);
        }).collect(Collectors.collectingAndThen(Collectors.toList(), ChatMessages::from));
    }
}