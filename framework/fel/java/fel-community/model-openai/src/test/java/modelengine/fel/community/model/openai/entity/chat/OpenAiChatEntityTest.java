/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.chat;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.MessageType;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.chat.support.HumanMessage;
import modelengine.fel.core.chat.support.SystemMessage;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.tool.ToolInfo;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.serialization.ObjectSerializer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 测试 {@link modelengine.fel.community.model.openai.entity.chat} 下对象的序列化和反序列化。
 *
 * @author 易文渊
 * @since 2024-08-17
 */
@DisplayName("测试 openai 聊天请求相关数据结构序列化与反序列化")
public class OpenAiChatEntityTest {
    private static final ObjectSerializer SERIALIZER = new JacksonObjectSerializer(null, null, null, true);

    @Nested
    class ChatMessageTest {
        @Test
        @DisplayName("测试序列化系统消息成功")
        void giveSystemMessageThenSerializeOk() {
            String expected = "{\"role\":\"system\",\"content\":\"test\"}";
            ChatMessage chatMessage = new SystemMessage("test");
            assertThat(SERIALIZER.serialize(OpenAiChatMessage.from(chatMessage))).isEqualTo(expected);
        }

        @Test
        @DisplayName("测试序列化用户消息成功")
        void giveBasicUserMessageThenSerializeOk() {
            String expected = "{\"role\":\"user\",\"content\":\"test\"}";
            ChatMessage chatMessage = new HumanMessage("test");
            assertThat(SERIALIZER.serialize(OpenAiChatMessage.from(chatMessage))).isEqualTo(expected);
        }

        @Test
        @DisplayName("测试序列化多模态用户消息成功")
        void giveMultimodalUserMessageThenSerializeOk() {
            String expected =
                    "{\"role\":\"user\",\"content\":[{\"type\":\"image_url\",\"image_url\":{\"url\":\"data:image/png;"
                            + "base64,1.png\"}},{\"type\":\"text\",\"text\":\"test\"}]}";
            ChatMessage chatMessage =
                    new HumanMessage("test", Collections.singletonList(new Media("image/png", "1.png")));
            assertThat(SERIALIZER.serialize(OpenAiChatMessage.from(chatMessage))).isEqualTo(expected);
        }

        @Test
        @DisplayName("测试序列化人工智能消息成功")
        void giveBasicAssistantMessageThenSerializeOk() {
            String expected = "{\"role\":\"assistant\",\"content\":\"test\"}";
            ChatMessage chatMessage = new AiMessage("test");
            assertThat(SERIALIZER.serialize(OpenAiChatMessage.from(chatMessage))).isEqualTo(expected);
        }

        @Test
        @DisplayName("测试序列化工具调用成功")
        void giveToolCallThenSerializeOk() {
            ToolCall toolCall = ToolCall.custom().id("id_1").name("test_function").arguments("test_arguments").build();
            OpenAiToolCall openAiToolCall = OpenAiToolCall.from(toolCall);
            String expected = "{\"id\":\"id_1\",\"type\":\"function\",\"function\":{\"name\":\"test_function\","
                    + "\"arguments\":\"test_arguments\"}}";
            assertThat(SERIALIZER.serialize(openAiToolCall)).isEqualTo(expected);
        }

        @Test
        @DisplayName("测试序列化携带工具的人工智能消息成功")
        void giveAssistantMessageWithToolCallThenSerializeOk() {
            ToolCall toolCall = ToolCall.custom().id("id_1").name("test_function").arguments("test_arguments").build();
            ChatMessage chatMessage = new AiMessage("test", Collections.singletonList(toolCall));
            OpenAiChatMessage openAiMessage = OpenAiChatMessage.from(chatMessage);
            String expected = "{\"role\":\"assistant\",\"content\":\"test\",\"tool_calls\":[{\"id\":\"id_1\","
                    + "\"type\":\"function\","
                    + "\"function\":{\"name\":\"test_function\",\"arguments\":\"test_arguments\"}}]}";
            assertThat(SERIALIZER.serialize(openAiMessage)).isEqualTo(expected);
        }

        @Test
        @DisplayName("测试反序列化人工智能消息成功")
        void giveBasicAssistantMessageThenDeserializeOk() {
            String json = "{\"role\":\"assistant\",\"content\":\"test\"}";
            OpenAiChatMessage openaiMessage = SERIALIZER.deserialize(json, OpenAiChatMessage.class);
            assertThat(openaiMessage).hasFieldOrPropertyWithValue("role", "assistant")
                    .hasFieldOrPropertyWithValue("content", "test");
        }

        @Test
        @DisplayName("测试反序列化携带工具调用的人工智能消息成功")
        void giveAssistantMessageWithToolCallThenDeserializeOk() {
            String json = "{\"role\":\"assistant\",\"content\":\"test\",\"tool_calls\":[{\"id\":\"id_1\","
                    + "\"type\":\"function\","
                    + "\"function\":{\"name\":\"test_function\",\"arguments\":\"test_arguments\"}}]}";
            OpenAiChatMessage openaiMessage = SERIALIZER.deserialize(json, OpenAiChatMessage.class);
            assertThat(openaiMessage).hasFieldOrPropertyWithValue("role", "assistant")
                    .hasFieldOrPropertyWithValue("content", "test")
                    .satisfies(chatMessage -> assertThat(chatMessage.toolCalls()).extracting(ToolCall::id)
                            .containsExactly("id_1"));
        }
    }

    @Nested
    class ChatCompletionTest {
        @Test
        @DisplayName("测试序列化聊天请求成功")
        void giveRequestThenSerializeOk() {
            Prompt prompt = ChatMessages.from(new SystemMessage("You are a helpful assistant."),
                    new HumanMessage("hello", Collections.singletonList(new Media("image/jpg", "test_base64"))));
            String toolParameters =
                    "{\"parameters\": {\"type\": \"object\"," + "\"properties\": {\"location\": {\"type\": \"string\","
                            + "\"description\": \"location_description\"},"
                            + "\"unit\": {\"type\": \"string\",\"enum\": [\"celsius\", \"fahrenheit\"]}},"
                            + "\"required\": [\"location\"]}}";
            ToolInfo toolInfo = ToolInfo.custom()
                    .namespace("test")
                    .name("test_tool")
                    .description("tool_description")
                    .parameters(SERIALIZER.deserialize(toolParameters, Map.class))
                    .extensions(Collections.emptyMap())
                    .build();
            ChatOption chatOption = ChatOption.custom()
                    .model("test_model")
                    .stream(false)
                    .temperature(0.2)
                    .frequencyPenalty(1.0)
                    .maxTokens(512)
                    .presencePenalty(1.0)
                    .topP(1.0)
                    .tools(Collections.singletonList(toolInfo))
                    .build();
            OpenAiChatCompletionRequest request = new OpenAiChatCompletionRequest(prompt, chatOption);
            String expected = "{\"messages\":[{\"role\":\"system\",\"content\":\"You are a helpful assistant.\"},"
                    + "{\"role\":\"user\",\"content\":[{\"type\":\"image_url\","
                    + "\"image_url\":{\"url\":\"data:image/jpg;base64,test_base64\"}},{\"type\":\"text\","
                    + "\"text\":\"hello\"}]}],\"model\":\"test_model\",\"stream\":false,\"temperature\":0.2,"
                    + "\"tools\":[{\"type\":\"function\",\"function\":{\"name\":\"test_tool\","
                    + "\"description\":\"tool_description\"," + "\"parameters\":{\"parameters\":{\"type\":\"object\","
                    + "\"properties\":{\"location\":{\"type\":\"string\","
                    + "\"description\":\"location_description\"},\"unit\":{\"type\":\"string\","
                    + "\"enum\":[\"celsius\",\"fahrenheit\"]}},\"required\":[\"location\"]}}}}],"
                    + "\"frequency_penalty\":1.0,\"max_tokens\":512,\"presence_penalty\":1.0,\"top_p\":1.0,"
                    + "\"tool_choice\":\"auto\"}";
            assertThat(SERIALIZER.serialize(request)).isEqualTo(expected);
        }

        @Test
        @DisplayName("测试反序列化聊天响应成功")
        void giveResponseThenDeserializeOk() {
            String json = "{\"id\":\"id_1\"," + "\"object\":\"chat.completion\",\"created\":123,"
                    + "\"model\":\"test_model\",\"choices\":[{" + "\"index\":0,\"message\":"
                    + "{\"role\":\"assistant\"," + "\"content\":\"test\"," + "\"tool_calls\":[{"
                    + "\"id\":\"call_abc123\",\"type\":\"function\","
                    + "\"function\":{\"name\":\"test_function_name\",\"arguments\":\"test_arguments\"}}]},"
                    + "\"logprobs\":null,\"finish_reason\":\"tool_calls\"}],"
                    + "\"usage\":{\"prompt_tokens\":12,\"completion_tokens\":34,\"total_tokens\":56}}";
            OpenAiChatCompletionResponse response = SERIALIZER.deserialize(json, OpenAiChatCompletionResponse.class);
            assertThat(response.message()).extracting(ChatMessage::id, ChatMessage::type, ChatMessage::text)
                    .containsExactly(Optional.empty(), MessageType.AI, "test");
        }
    }
}