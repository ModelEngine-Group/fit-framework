/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.community.model.openai.config.OpenAiConfig;
import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.chat.support.HumanMessage;
import modelengine.fel.core.embed.EmbedOption;
import modelengine.fel.core.embed.Embedding;
import modelengine.fel.core.image.ImageOption;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.conf.Config;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.test.annotation.MvcTest;
import modelengine.fitframework.test.domain.mvc.MockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link OpenAiModel} 的模型测试。
 *
 * @author 刘信宏
 * @since 2024-09-23
 */
@MvcTest(classes = TestModelController.class)
public class OpenAiModelTest {
    private OpenAiModel openAiModel;

    @Fit
    private HttpClassicClientFactory httpClientFactory;

    @Fit
    private ObjectSerializer serializer;

    @Fit
    private Config config;

    @Fit
    private BeanContainer container;

    @Fit
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        OpenAiConfig openAiConfig = new OpenAiConfig();
        openAiConfig.setApiBase("http://localhost:" + mockMvc.getPort());
        this.openAiModel = new OpenAiModel(this.httpClientFactory, openAiConfig, this.serializer, config, container);
    }

    @Test
    @DisplayName("测试聊天流式返回")
    void testOpenAiChatModelStreamService() {
        List<String> contents = Arrays.asList("1", "2", "3");
        Choir<ChatMessage> choir = this.openAiModel.generate(ChatMessages.from(new HumanMessage("hello")),
                ChatOption.custom().stream(true).model("model").build());
        List<ChatMessage> response = choir.blockAll();
        assertThat(response).extracting(ChatMessage::text).isEqualTo(contents);
    }

    @Test
    @DisplayName("测试嵌入模型返回")
    void testOpenAiEmbeddingModel() {
        Embedding embedding = this.openAiModel.generate("1", EmbedOption.custom().model("model").build());
        assertThat(embedding.embedding()).containsExactly(1f, 2f, 3f);
    }

    @Test
    @DisplayName("测试图片生成模型返回")
    void testOpenAiImageModel() {
        List<Media> images =
                this.openAiModel.generate("prompt", ImageOption.custom().model("model").size("256x256").build());
        assertThat(images.stream().map(Media::getData).collect(Collectors.toList())).containsExactly("123",
                "456",
                "789");
    }
}