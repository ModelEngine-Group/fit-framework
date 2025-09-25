/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import modelengine.fel.core.document.Document;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.rerank.RerankOption;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.test.domain.mvc.MockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * ReRank 客户端服务测试。
 *
 * @author 马朝阳
 * @since 2024-09-14
 */
public class RerankDocumentProcessorTest {
    private static final String[] DOCS = new String[] {"Burgers", "Carson", "Shanghai", "Beijing", "Test"};

    private RerankDocumentProcessor client;

    @Fit
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.client = new RerankDocumentProcessor(RerankOption.custom()
                .model("rerank1")
                .apiKey("")
                .query("What is the capital of the united states?")
                .topN(3)
                .build(), new RerankModelStub());
    }

    @Test
    @DisplayName("测试 Rerank 接口调用响应成功")
    public void testWhenCallRerankModelThenSuccess() {
        List<String> texts = Arrays.asList(DOCS[0], DOCS[1], DOCS[2], DOCS[3], DOCS[4]);
        List<MeasurableDocument> docs = this.client.process(this.getRequest());
        assertThat(docs).extracting(MeasurableDocument::text).isEqualTo(texts);
    }

    @Test
    @DisplayName("测试 Rerank 接口参数为空响应异常")
    public void testWhenCallRerankOptionNullParamThenResponseException() {
        assertThatThrownBy(() -> new RerankDocumentProcessor(null, new RerankModelStub())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RerankDocumentProcessor(RerankOption.custom().build(), null)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("测试 Rerank 接口参数为空响应异常")
    public void testWhenCallRerankModelNullParamThenResponseException() {
        assertThatThrownBy(() -> new RerankDocumentProcessor(RerankOption.custom()
                .build(), null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RerankDocumentProcessor(RerankOption.custom().build(), null)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("测试 Rerank 接口请求参数为空响应异常")
    public void testWhenCallRerankModelNullRequestParamThenResponseException() {
        assertThat(this.client.process(new ArrayList<>())).isEqualTo(Collections.emptyList());
        assertThat(this.client.process(null)).isEqualTo(Collections.emptyList());
    }

    private List<MeasurableDocument> getRequest() {
        List<MeasurableDocument> documents = new ArrayList<>();
        Arrays.stream(DOCS)
                .forEach(doc -> documents.add(new MeasurableDocument(Document.custom()
                        .text(doc)
                        .metadata(new HashMap<>())
                        .build(), -1)));
        return documents;
    }
}