/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import static modelengine.fel.core.document.support.TestRerankModelController.FAIL_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import modelengine.fel.core.document.Document;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.exception.FitException;
import modelengine.fitframework.test.annotation.MvcTest;
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
@MvcTest(classes = TestRerankModelController.class)
public class RerankDocumentProcessorTest {
    private static final String[] DOCS = new String[] {"Burgers", "Carson", "Shanghai", "Beijing", "Test"};

    private RerankDocumentProcessor client;

    @Fit
    private HttpClassicClientFactory httpClientFactory;

    @Fit
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.client = new RerankDocumentProcessor(httpClientFactory,
                RerankOption.custom()
                        .baseUri("http://localhost:" + mockMvc.getPort())
                        .model("rerank1")
                        .query("What is the capital of the united states?")
                        .topN(3)
                        .build());
    }

    @Test
    @DisplayName("测试 Rerank 接口调用响应成功")
    public void testWhenCallRerankModelThenSuccess() {
        List<String> texts = Arrays.asList(DOCS[3], DOCS[4], DOCS[0]);
        List<Double> scores = Arrays.asList(0.999071, 0.7867867, 0.32713068);
        List<MeasurableDocument> docs = this.client.process(this.getRequest());
        assertThat(docs).extracting(MeasurableDocument::text).isEqualTo(texts);
        assertThat(docs).extracting(MeasurableDocument::score).isEqualTo(scores);
    }

    @Test
    @DisplayName("测试 Rerank 接口调用响应异常")
    public void testWhenCallRerankModelThenResponseException() {
        RerankDocumentProcessor client1 = new RerankDocumentProcessor(httpClientFactory,
                RerankOption.custom().baseUri("http://localhost:" + mockMvc.getPort() + FAIL_ENDPOINT).build());
        assertThatThrownBy(() -> client1.process(this.getRequest())).isInstanceOf(FitException.class);
    }

    @Test
    @DisplayName("测试 Rerank 接口参数为空响应异常")
    public void testWhenCallRerankModelNullParamThenResponseException() {
        assertThatThrownBy(() -> new RerankDocumentProcessor(this.httpClientFactory, null)).isInstanceOf(
                IllegalArgumentException.class);
        assertThatThrownBy(() -> new RerankDocumentProcessor(null, RerankOption.custom().build())).isInstanceOf(
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

    private String getMockReRankResponseBody() {
        return "{\"results\":[{\"index\":3,\"relevance_score\":0.999071},{\"index\":4,\"relevance_score\":0.7867867},"
                + "{\"index\":0,\"relevance_score\":0.32713068}]}";
    }
}