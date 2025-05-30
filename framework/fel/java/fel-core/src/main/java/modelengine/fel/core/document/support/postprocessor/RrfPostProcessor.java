/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support.postprocessor;

import modelengine.fel.core.document.DocumentPostProcessor;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * 基于 RRF 算法的后处理器。
 *
 * @author 马朝阳
 * @since 2024-09-29
 */
public class RrfPostProcessor implements DocumentPostProcessor {
    private static final int DEFAULT_FACTOR = 60;

    private static final Map<RrfScoreStrategyEnum, Function<DoubleStream, OptionalDouble>> SCORE_STRATEGY_MAP =
            MapBuilder.<RrfScoreStrategyEnum, Function<DoubleStream, OptionalDouble>>get()
                    .put(RrfScoreStrategyEnum.MAX, DoubleStream::max)
                    .put(RrfScoreStrategyEnum.AVG, DoubleStream::average)
                    .build();

    private final RrfScoreStrategyEnum scoreStrategy;
    private final int factor;

    public RrfPostProcessor() {
        this(RrfScoreStrategyEnum.MAX, DEFAULT_FACTOR);
    }

    public RrfPostProcessor(RrfScoreStrategyEnum scoreStrategy) {
        this(scoreStrategy, DEFAULT_FACTOR);
    }

    public RrfPostProcessor(RrfScoreStrategyEnum scoreStrategy, int factor) {
        this.scoreStrategy = Validation.notNull(scoreStrategy, "The score strategy cannot be null.");
        this.factor = Validation.greaterThanOrEquals(factor, 0, "The factor must be non-negative.");
        if (!SCORE_STRATEGY_MAP.containsKey(this.scoreStrategy)) {
            throw new IllegalArgumentException("The score strategy map not include this strategy.");
        }
    }

    /**
     * 基于 RRF 算法对检索结果去重和重排序。
     *
     * @param documents 表示输入文档的 {@link List}{@code <}{@link MeasurableDocument}{@code >}。
     * @return 表示处理后文档的 {@link List}{@code <}{@link MeasurableDocument}{@code >}。
     */
    @Override
    public List<MeasurableDocument> process(List<MeasurableDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return Collections.emptyList();
        }
        Map<String, Double> rrfDocumentScore = this.getRrfDocumentScore(documents);
        return this.getScoreByStrategy(documents)
                .stream()
                .sorted((document1, document2) -> rrfDocumentScore.get(document2.id())
                        .compareTo(rrfDocumentScore.get(document1.id())))
                .collect(Collectors.toList());
    }

    private List<MeasurableDocument> getScoreByStrategy(List<MeasurableDocument> documents) {
        Map<String, List<MeasurableDocument>> documentsMap =
                documents.stream().collect(Collectors.groupingBy(MeasurableDocument::id));
        return documentsMap.values().stream().map(measurableDocuments -> {
            DoubleStream doubleStream = measurableDocuments.stream().mapToDouble(MeasurableDocument::score);
            double score = SCORE_STRATEGY_MAP.get(this.scoreStrategy).apply(doubleStream).orElse(0.0d);
            MeasurableDocument document = measurableDocuments.get(0);
            return new MeasurableDocument(document, score, document.group());
        }).collect(Collectors.toList());
    }

    private Map<String, Double> getRrfDocumentScore(List<MeasurableDocument> documents) {
        Map<String, List<MeasurableDocument>> groupedDocuments =
                documents.stream().collect(Collectors.groupingBy(MeasurableDocument::group));
        groupedDocuments.values()
                .forEach(groupedList -> groupedList.sort(Comparator.comparingDouble(MeasurableDocument::score)
                        .reversed()));
        Map<String, Double> idScoreMap = new HashMap<>();
        for (List<MeasurableDocument> groupedDocumentList : groupedDocuments.values()) {
            for (int i = 0; i < groupedDocumentList.size(); i++) {
                MeasurableDocument curr = groupedDocumentList.get(i);
                idScoreMap.put(curr.id(), idScoreMap.getOrDefault(curr.id(), 0.0) + (1.0 / (i + 1 + this.factor)));
            }
        }
        return idScoreMap;
    }
}
