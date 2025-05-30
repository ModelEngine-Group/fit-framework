/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fel.core.document.DocumentPostProcessor;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fit.http.client.HttpClassicClientRequest;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.ObjectEntity;
import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fitframework.exception.FitException;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.resource.UrlUtils;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.LazyLoader;
import modelengine.fitframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示检索文档的后置重排序接口。
 *
 * @author 马朝阳
 * @since 2024-09-14
 */
public class RerankDocumentProcessor implements DocumentPostProcessor {
    private static final Logger log = Logger.get(RerankDocumentProcessor.class);

    private final LazyLoader<HttpClassicClient> httpClient;
    private final RerankOption rerankOption;

    /**
     * 创建 {@link RerankDocumentProcessor} 的实体。
     *
     * @param httpClientFactory 表示 {@link HttpClassicClientFactory} 的实例。
     * @param rerankOption 表示 rerank 模型参数的 {@link  RerankOption}
     */
    public RerankDocumentProcessor(HttpClassicClientFactory httpClientFactory, RerankOption rerankOption) {
        Validation.notNull(httpClientFactory, "The httpClientFactory cannot be null.");
        this.httpClient =
                new LazyLoader<>(() -> httpClientFactory.create(HttpClassicClientFactory.Config.builder().build()));
        this.rerankOption = Validation.notNull(rerankOption, "The rerankOption cannot be null.");
    }

    /**
     * 对检索结果进行重排序。
     *
     * @param documents 表示输入文档的 {@link List}{@code <}{@link MeasurableDocument}{@code >}。
     * @return 表示处理后文档的 {@link List}{@code <}{@link MeasurableDocument}{@code >}。
     */
    public List<MeasurableDocument> process(List<MeasurableDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return Collections.emptyList();
        }
        List<String> docs = documents.stream().map(MeasurableDocument::text).collect(Collectors.toList());
        RerankRequest fields = new RerankRequest(this.rerankOption, docs);

        HttpClassicClientRequest request = this.httpClient.get()
                .createRequest(HttpRequestMethod.POST,
                        UrlUtils.combine(this.rerankOption.baseUri(), RerankApi.RERANK_ENDPOINT));
        request.entity(Entity.createObject(request, fields));
        RerankResponse rerankResponse = this.rerankExchange(request);

        return rerankResponse.results()
                .stream()
                .map(result -> new MeasurableDocument(documents.get(result.index()), result.relevanceScore()))
                .sorted((document1, document2) -> (int) (document2.score() - document1.score()))
                .collect(Collectors.toList());
    }

    private RerankResponse rerankExchange(HttpClassicClientRequest request) {
        try (HttpClassicClientResponse<Object> response = request.exchange(RerankResponse.class)) {
            if (response.statusCode() != HttpResponseStatus.OK.statusCode()) {
                log.error("Failed to get rerank model response. [code={}, reason={}]",
                        response.statusCode(),
                        response.reasonPhrase());
                throw new FitException("Failed to get rerank model response.");
            }
            return ObjectUtils.cast(response.objectEntity()
                    .map(ObjectEntity::object)
                    .orElseThrow(() -> new FitException("The response body is abnormal.")));
        } catch (IOException e) {
            log.error("Failed to request rerank model.", e);
            throw new FitException(e);
        }
    }
}