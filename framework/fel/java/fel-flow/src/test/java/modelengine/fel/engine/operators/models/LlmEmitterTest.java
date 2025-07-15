/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.models;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.engine.util.StateKey;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.util.StringUtils;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 表示 {@link LlmEmitter} 的测试。
 *
 * @author 宋永坦
 * @since 2025-07-05
 */
class LlmEmitterTest {
    @Test
    void shouldAddMemoryWhenCompleteGivenLlmOutput() {
        String output = "data1";
        Prompt prompt = ChatMessages.fromList(Collections.emptyList());
        Choir<ChatMessage> dataSource = Choir.create(emitter -> {
            emitter.emit(new AiMessage(output));
            emitter.complete();
        });
        FlowSession flowSession = new FlowSession();
        Memory mockMemory = Mockito.mock(Memory.class);
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        Mockito.doNothing().when(mockMemory).add(captor.capture());
        flowSession.setInnerState(StateKey.HISTORY, mockMemory);

        LlmEmitter<ChatMessage> llmEmitter = new LlmEmitter<>(dataSource, prompt, flowSession);
        llmEmitter.start(flowSession);

        List<ChatMessage> captured = captor.getAllValues();
        assertEquals(2, captured.size());
        assertEquals(StringUtils.EMPTY, captured.get(0).text());
        assertEquals(output, captured.get(1).text());
    }

    @Test
    void shouldNotAddMemoryWhenCompleteGivenLlmToolCallOutput() {
        String output = "data1";
        Prompt prompt = ChatMessages.fromList(Collections.emptyList());
        Choir<ChatMessage> dataSource = Choir.create(emitter -> {
            emitter.emit(new AiMessage(output, Arrays.asList(ToolCall.custom().id("id1").build())));
            emitter.complete();
        });
        FlowSession flowSession = new FlowSession();
        Memory mockMemory = Mockito.mock(Memory.class);
        flowSession.setInnerState(StateKey.HISTORY, mockMemory);

        LlmEmitter<ChatMessage> llmEmitter = new LlmEmitter<>(dataSource, prompt, flowSession);
        llmEmitter.start(flowSession);

        Mockito.verify(mockMemory, Mockito.times(0)).add(Mockito.any());
    }
}