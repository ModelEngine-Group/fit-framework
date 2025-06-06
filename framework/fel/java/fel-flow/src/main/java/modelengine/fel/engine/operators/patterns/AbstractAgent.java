/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.patterns;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import lombok.Getter;
import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.operators.models.ChatFlowModel;
import modelengine.fit.waterflow.domain.context.StateContext;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 基类。
 *
 * @author 刘信宏
 * @since 2024-04-12
 */
public abstract class AbstractAgent extends AbstractFlowPattern<Prompt, ChatMessage> {
    private static final String AGENT_MEMORY = "agent_memory";
    private static final String CHECK_POINT = "check_point";

    @Getter
    private final ChatFlowModel model;
    private final String memoryId;

    /**
     * 使用工具提供者和大模型服务对象初始化 {@link AbstractAgent}。
     *
     * @param flowModel 表示模型推理服务的 {@link ChatFlowModel}。
     * @throws IllegalArgumentException 当 {@code toolProvider} 、 {@code chatStreamModel} 和 {@code options}
     * 任一个为 {@code null} 时。
     */
    protected AbstractAgent(ChatFlowModel flowModel) {
        this(flowModel, AGENT_MEMORY);
    }

    /**
     * 使用工具提供者和大模型服务对象初始化 {@link AbstractAgent}。
     *
     * @param flowModel 表示模型推理服务的 {@link ChatFlowModel}。
     * @param memoryId agentMsgKey 表示 Agent 响应的所在自定义键的 {@link String}。
     * @throws IllegalArgumentException <ln>
     * <li>当 {@code toolProvider} 、 {@code chatStreamModel} 和 {@code options} 任一个为 {@code null} 时。</li>
     * <li>当 {@code agentMsgKey} 为 {@code null} 、空字符串或只有空白字符的字符串时。</li>
     * </ln>
     */
    protected AbstractAgent(ChatFlowModel flowModel, String memoryId) {
        this.model = notNull(flowModel, "The flow model cannot be null.");
        this.memoryId = notBlank(memoryId, "The agent message key cannot be blank.");
    }

    /**
     * 执行工具调用。
     *
     * @param toolCalls 表示工具调用的 {@link List}{@code <}{@link ToolCall}{@code >}。
     * @param ctx 表示工具调用上下文的 {@link StateContext}。
     * @return 表示工具调用结果的 {@link Prompt}。
     */
    protected abstract Prompt doToolCall(List<ToolCall> toolCalls, StateContext ctx);

    @Override
    protected AiProcessFlow<Prompt, ChatMessage> buildFlow() {
        return AiFlows.<Prompt>create()
                .just((input, ctx) -> ctx.setState(this.memoryId, ChatMessages.from(input)))
                .generate(this.model)
                .id(CHECK_POINT)
                .conditions()
                .matchTo(ChatMessage::isToolCall,
                        node -> node.reduce(() -> new AiMessage(StringUtils.EMPTY, new ArrayList<>()), (acc, input) -> {
                                    acc.toolCalls().addAll(input.toolCalls());
                                    return acc;
                                })
                                .just(this::handleTool)
                                .id("call tool")
                                .map((ignored, ctx) -> ctx.getState(this.memoryId))
                                .to(CHECK_POINT))
                .others(node -> node)
                .close();
    }

    private void handleTool(ChatMessage message, StateContext ctx) {
        ChatMessages lastRequest = ctx.getState(this.memoryId);
        lastRequest.add(message);
        lastRequest.addAll(this.doToolCall(message.toolCalls(), ctx).messages());
    }
}
