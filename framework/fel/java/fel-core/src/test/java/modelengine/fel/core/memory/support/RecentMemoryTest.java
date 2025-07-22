/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.memory.support;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.support.AiMessage;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 表示 {@link RecentMemory} 的测试。
 *
 * @since 2025-07-04
 */
class RecentMemoryTest {
    private final List<ChatMessage> inputChatMessages =
            Arrays.asList(new AiMessage("1"), new AiMessage("2"), new AiMessage("3"));

    @Test
    void shouldKeepAllMessagesWhenAddGivenLessMessage() {
        RecentMemory recentMemory = new RecentMemory(4);
        this.inputChatMessages.forEach(recentMemory::add);
        List<ChatMessage> messages = recentMemory.messages();

        assertEquals(inputChatMessages.size(), messages.size());
        for (int i = 0; i < inputChatMessages.size(); ++i) {
            assertEquals(inputChatMessages.get(i).text(), messages.get(i).text());
        }
    }

    @Test
    void shouldKeepMaxCountMessagesWhenAddGivenOverMaxCountMessages() {
        RecentMemory recentMemory = new RecentMemory(2);
        this.inputChatMessages.forEach(recentMemory::add);
        List<ChatMessage> messages = recentMemory.messages();

        assertEquals(2, messages.size());
        assertEquals(inputChatMessages.get(1).text(), messages.get(0).text());
        assertEquals(inputChatMessages.get(2).text(), messages.get(1).text());
    }

    @Test
    void shouldKeepMaxCountMessagesWhenSetGivenOverMaxCountMessages() {
        RecentMemory recentMemory = new RecentMemory(2);
        recentMemory.set(this.inputChatMessages);
        List<ChatMessage> messages = recentMemory.messages();

        assertEquals(2, messages.size());
        assertEquals(inputChatMessages.get(1).text(), messages.get(0).text());
        assertEquals(inputChatMessages.get(2).text(), messages.get(1).text());
    }
}