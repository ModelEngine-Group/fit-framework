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
 * RRF (Reciprocal Rank Fusion) Algorithm Post-Processor.
 * <p>
 * A lightweight rank fusion algorithm for merging multiple sorted lists (e.g., search results, recommendation lists).
 * Core idea: Generates a unified ranking by calculating the reciprocal weighted sum of document ranks across lists. Key
 * features:
 * </p>
 * <ul>
 *   <li>No score normalization required</li>
 *   <li>Strong robustness to anomalous rankings</li>
 *   <li>Computationally efficient</li>
 * </ul>
 *
 * <p><b>Core Formula:</b></p>
 * RRF_Score(d) = Σ [ 1 / (k + rankᵢ(d)) ]
 * <ul>
 *   <li><b>d</b>: Document to be ranked</li>
 *   <li><b>rankᵢ(d)</b>: Rank of document d in the i-th list (counting starts from 1)</li>
 *   <li><b>k</b>: Smoothing constant (default 60), adjusts low-rank document contribution</li>
 * </ul>
 *
 * <p><b>Algorithm Workflow:</b></p>
 * <ol>
 *   <li>Initialize hash table: Stores document ID → cumulative RRF score</li>
 *   <li>Traverse each sorted list:
 *     <pre>{@code
 *     for (List<Document> list : allLists) {
 *         for (int rank = 1; rank <= list.size(); rank++) {
 *             double score = 1.0 / (k + rank);
 *             map.put(docId, map.getOrDefault(docId, 0.0) + score);
 *         }
 *     }
 *     }</pre>
 *   </li>
 *   <li>Sort by total score descending to generate final ranking</li>
 * </ol>
 *
 * <p><b>Parameter Functionality:</b></p>
 * <ul>
 *   <li><b>Smaller k</b> → Amplifies top-ranked documents (e.g., k=1: rank1=0.5, rank2=0.33)</li>
 *   <li><b>Larger k</b> → Increases low-rank influence (e.g., k=100: rank1≈0.01, rank100=0.005)</li>
 *   <li><b>Default k=60</b>: Empirical value balancing rank influence</li>
 * </ul>
 *
 * <p><b>Applicable Scenarios:</b></p>
 * <ul>
 *   <li>✅ <b>Hybrid Search</b>: Fusing keyword retrieval (BM25) and vector search results</li>
 *   <li>✅ <b>RAG Systems</b>: Merging outputs from multiple retrievers (e.g., BM25/Dense Retrieval)</li>
 *   <li>✅ <b>Multi-strategy Recommendations</b>: Combining collaborative/content-based filtering lists</li>
 * </ul>
 *
 * <p><b>Important Notes:</b></p>
 * <ul>
 *   <li>Result quality depends on input ranking quality (low-quality inputs amplify bias)</li>
 *   <li>Documents missing from a list contribute no score for that list</li>
 *   <li>Final scores are only for relative ordering (not comparable across queries)</li>
 *   <li>Java implementation considerations:
 *     <ul>
 *       <li>Use {@link Map}{@code <}{@link String}{@code , }{@link Double}{@code >} for document scores</li>
 *       <li>Sort with {@code PriorityQueue} or {@code Stream.sorted()}</li>
 *     </ul>
 *   </li>
 * </ul>
 * <p>
 * ------------------------------------------------------------------------------------------
 * <p>
 * 基于 RRF(Reciprocal Rank Fusion) 算法的后处理器。
 * <p>
 * 轻量级排名融合算法，用于合并多个排序列表（如搜索结果、推荐列表）。核心思想是对文档在不同列表中的排名取倒数加权求和，生成统一排序。特点包括：
 * </p>
 * <ul>
 *   <li>无需分数归一化</li>
 *   <li>对异常排名鲁棒性强</li>
 *   <li>计算高效</li>
 * </ul>
 *
 * <p><b>核心公式：</b></p>
 * RRF_Score(d) = Σ [ 1 / (k + rankᵢ(d)) ]
 * <ul>
 *   <li><b>d</b>: 表示待排序文档</li>
 *   <li><b>rankᵢ(d)</b>: 表示文档 d 在第 i 个排序列表中的排名（从 1 开始计数）</li>
 *   <li><b>k</b>: 表示平滑常数（默认 60），用于调节低排名文档的贡献度</li>
 * </ul>
 *
 * <p><b>算法流程：</b></p>
 * <ol>
 *   <li>初始化哈希表：存储文档 ID → 累积 RRF 分数</li>
 *   <li>遍历每个排序列表：
 *     <pre>{@code
 *     for (List<Document> list : allLists) {
 *         for (int rank = 1; rank <= list.size(); rank++) {
 *             double score = 1.0 / (k + rank);
 *             map.put(docId, map.getOrDefault(docId, 0.0) + score);
 *         }
 *     }
 *     }</pre>
 *   </li>
 *   <li>按总分降序排序生成最终融合排名</li>
 * </ol>
 *
 * <p><b>参数作用：</b></p>
 * <ul>
 *   <li><b>k 值越小</b> → 高排名文档优势放大（如 k = 1 时，第 1 名得分 0.5，第 2 名 0.33）</li>
 *   <li><b>k 值越大</b> → 低排名文档影响力提升（如 k = 100 时，第 1 名得分 ≈ 0.01，第 100 名 0.005）</li>
 *   <li><b>默认 k = 60</b>：经验值，平衡高/低排名影响力</li>
 * </ul>
 *
 * <p><b>适用场景：</b></p>
 * <ul>
 *   <li>✅ <b>混合搜索</b>：融合关键词检索（BM25）与向量搜索结果</li>
 *   <li>✅ <b>RAG 系统</b>：合并多检索器（如 BM25 / Dense Retrieval）的输出</li>
 *   <li>✅ <b>多策略推荐</b>：融合协同过滤、内容过滤的推荐列表</li>
 * </ul>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>输入列表的排名质量直接影响结果（低质量输入会放大偏差）</li>
 *   <li>若文档未出现在某列表中，则忽略该列表贡献</li>
 *   <li>最终分数仅用于排序，无绝对语义（不可跨查询比较）</li>
 *   <li>Java 实现时需注意：
 *     <ul>
 *       <li>使用 {@link Map}{@code <}{@link String}{@code , }{@link Double}{@code >} 存储文档得分</li>
 *       <li>排序推荐使用 {@code PriorityQueue} 或 {@code Stream.sorted()}</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/rrf.html">Elasticsearch RRF
 * Documentation</a>
 * @see <a href="https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf">Cormack et al. (2009) "Reciprocal Rank
 * Fusion Outperforms Condorcet and Individual Rank Learning Methods"</a>
 *
 * @author 马朝阳
 * @since 2024-09-29
 */
public class RrfPostProcessor implements DocumentPostProcessor {
    private static final int DEFAULT_FACTOR = 60;

    private static final Map<RrfScoreStrategy, Function<DoubleStream, OptionalDouble>> SCORE_STRATEGY_MAP =
            MapBuilder.<RrfScoreStrategy, Function<DoubleStream, OptionalDouble>>get()
                    .put(RrfScoreStrategy.MAX, DoubleStream::max)
                    .put(RrfScoreStrategy.AVG, DoubleStream::average)
                    .build();

    private final RrfScoreStrategy scoreStrategy;
    private final int factor;

    /**
     * 创建一个默认的 RRF 后处理器。
     */
    public RrfPostProcessor() {
        this(RrfScoreStrategy.MAX, DEFAULT_FACTOR);
    }

    /**
     * 创建一个指定 RRF 策略的 RRF 后处理器。
     *
     * @param scoreStrategy 指定的 RRF 策略的 {@link RrfScoreStrategy}。
     */
    public RrfPostProcessor(RrfScoreStrategy scoreStrategy) {
        this(scoreStrategy, DEFAULT_FACTOR);
    }

    /**
     * 创建一个指定 RRF 策略和因子的 RRF 后处理器。
     *
     * @param scoreStrategy 指定的 RRF 策略的 {@link RrfScoreStrategy}。
     * @param factor 指定的 RRF 策略的因子的 {@code int}。
     */
    public RrfPostProcessor(RrfScoreStrategy scoreStrategy, int factor) {
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
