/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import modelengine.fel.community.model.openai.config.OpenAiConfig;
import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.chat.support.HumanMessage;
import modelengine.fel.core.document.Document;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.embed.EmbedOption;
import modelengine.fel.core.embed.Embedding;
import modelengine.fel.core.image.ImageOption;
import modelengine.fel.core.rerank.RerankOption;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link OpenAiModel} 的模型测试。
 *
 * @since 2024-09-23
 */
@MvcTest(classes = TestModelController.class)
public class OpenAiModelTest {
    private static final int EXPECTED_TOP_K = 3;
    private static final String HIGHEST_RANKED_TEXT = "C++ offers high performance.";
    private static final double EXPECTED_HIGHEST_SCORE = 0.999071;
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

    @Test
    @DisplayName("测试重排模型返回：应返回按相关性排序的前 K 个文档")
    void testOpenAiRerankModel() {
        // Given: 准备输入文档
        List<MeasurableDocument> inputDocs = Arrays.asList(doc("0", "Java is a programming language."),
                doc("1", "Python is great for data science."),
                doc("2", HIGHEST_RANKED_TEXT),
                doc("3", "Rust offers high performance."),
                doc("4", "C offers high performance."));

        RerankOption rerankOption = RerankOption.custom().model("rerank-model").topN(EXPECTED_TOP_K).build();

        // When: 调用重排接口
        List<MeasurableDocument> result = this.openAiModel.generate(inputDocs, rerankOption);

        // Then: 验证结果
        assertAll(() -> assertThat(result).as("应返回 top-%d 结果", EXPECTED_TOP_K).hasSize(EXPECTED_TOP_K),

                () -> {
                    List<Double> scores = result.stream().map(MeasurableDocument::score).collect(Collectors.toList());
                    assertThat(scores).as("结果应按相关性分数降序排列").isSortedAccordingTo(Collections.reverseOrder());
                },

                () -> {
                    List<String> resultTexts =
                            result.stream().map(MeasurableDocument::text).collect(Collectors.toList());
                    List<String> inputTexts =
                            inputDocs.stream().map(MeasurableDocument::text).collect(Collectors.toList());
                    assertThat(inputTexts).as("所有返回文档必须来自输入集").containsAll(resultTexts);
                },

                () -> assertThat(result.get(0).text()).as("得分最高的文档应为 C++").isEqualTo(HIGHEST_RANKED_TEXT),

                () -> assertThat(result.get(0).score()).as("最高分应与模拟响应一致").isEqualTo(EXPECTED_HIGHEST_SCORE));
    }

    private MeasurableDocument doc(String id, String text) {
        Document document = Document.custom().id(id).text(text).metadata(new HashMap<>()).build();
        return new MeasurableDocument(document, 0.0);
    }
}