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
import modelengine.fel.core.document.support.postprocessor.RrfPostProcessor;
import modelengine.fel.core.document.support.postprocessor.RrfScoreStrategyEnum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RRF 算法测试。
 *
 * @author 马朝阳
 * @since 2024-09-29
 */
public class RrfPostProcessorTest {
    private static final String[] DOCS = new String[] {"A", "B", "C", "D", "E"};

    @Test
    @DisplayName("测试 RFF 算法最大值策略成功")
    public void testWhenCallRRFMaxThenSuccess() {
        RrfPostProcessor rrf = new RrfPostProcessor();
        List<MeasurableDocument> process = rrf.process(getDocumentList());
        assertThat(process).map(MeasurableDocument::score).containsExactly(0.94, 0.69, 0.36, 0.52, 0.32);
        assertThat(process).map(MeasurableDocument::id).containsExactly("1", "4", "2", "5", "3");
    }

    @Test
    @DisplayName("测试 RFF 算法平均值策略成功")
    public void testWhenCallRRFAvgThenSuccess() {
        RrfPostProcessor rrf = new RrfPostProcessor(RrfScoreStrategyEnum.AVG);
        List<MeasurableDocument> process = rrf.process(getDocumentList());
        assertThat(process).map(MeasurableDocument::score).containsExactly(0.84, 0.655, 0.36, 0.52, 0.32);
        assertThat(process).map(MeasurableDocument::id).containsExactly("1", "4", "2", "5", "3");
    }

    @Test
    @DisplayName("测试 RFF 算法倒数系数")
    public void testWhenCallRRFFactorThenSuccess() {
        RrfPostProcessor rrf = new RrfPostProcessor(RrfScoreStrategyEnum.AVG, 100);
        List<MeasurableDocument> process = rrf.process(getDocumentList());
        assertThat(process).map(MeasurableDocument::score).containsExactly(0.84, 0.655, 0.36, 0.52, 0.32);
        assertThat(process).map(MeasurableDocument::id).containsExactly("1", "4", "2", "5", "3");
    }

    @Test
    @DisplayName("测试 RFF 算法策略失败")
    public void testWhenCallRRFArgNullThenFail() {
        assertThatThrownBy(() -> new RrfPostProcessor(null, 60)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RrfPostProcessor(RrfScoreStrategyEnum.AVG, -1)).isInstanceOf(
                IllegalArgumentException.class);
    }

    private List<MeasurableDocument> getDocumentList() {
        List<MeasurableDocument> res = new ArrayList<>();
        res.addAll(getGroup("1", new int[] {1, 3, 4}, new double[] {0.74, 0.32, 0.69}));
        res.addAll(getGroup("2", new int[] {1, 5}, new double[] {0.94, 0.52}));
        res.addAll(getGroup("3", new int[] {2, 4}, new double[] {0.36, 0.62}));

        return res;
    }

    private List<MeasurableDocument> getGroup(String groupId, int[] ids, double[] scores) {
        List<MeasurableDocument> documents = new ArrayList<>();
        int scoreId = 0;
        for (int id : ids) {
            documents.add(new MeasurableDocument(Document.custom()
                    .text(DOCS[id - 1])
                    .id(String.valueOf(id))
                    .metadata(new HashMap<>())
                    .build(), scores[scoreId], groupId));
            scoreId++;
        }
        return documents;
    }
}
