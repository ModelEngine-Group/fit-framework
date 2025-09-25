/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.memory.support;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.chat.support.HumanMessage;
import modelengine.fel.core.memory.Memory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 表示 {@link CacheMemory} 的测试集。
 *
 * @author 易文渊
 * @since 2024-08-09
 */
@DisplayName("测试 CacheMemory")
public class CacheMemoryTest {
    private final List<ChatMessage> messages = Arrays.asList(new HumanMessage("hello"), new AiMessage("hello"));
    private final Memory memory = new CacheMemory();

    @BeforeEach
    void setUp() {
        this.messages.forEach(this.memory::add);
    }

    @AfterEach
    void tearDown() {
        this.memory.clear();
    }

    @Test
    @DisplayName("测试插入消息后，读取消息成功。")
    void giveMessageThenReadOk() {
        assertThat(this.memory.messages()).isEqualTo(this.messages);
    }

    @Test
    @DisplayName("测试插入消息后，读取格式化文本成功。")
    void giveMessageThenReadTextOk() {
        assertThat(this.memory.text()).isEqualTo("human:hello\nai:hello");
    }

    @Test
    @DisplayName("测试插入消息后，设置消息成功。")
    void giveMessagesThenReset() {
        List<ChatMessage> newMessages = Arrays.asList(new HumanMessage("你好"), new AiMessage("你好"));
        this.memory.set(newMessages);
        assertThat(this.memory.messages()).isEqualTo(newMessages);
    }
}