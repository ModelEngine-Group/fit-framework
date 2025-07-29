/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fel.core.document.DocumentPostProcessor;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.rerank.RerankApi;
import modelengine.fel.core.rerank.RerankModel;
import modelengine.fel.core.rerank.RerankOption;
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
 * @since 2024-09-14
 */
public class RerankDocumentProcessor implements DocumentPostProcessor {
    private final RerankOption rerankOption;
    private final RerankModel rerankModel;

    /**
     * 创建 {@link RerankDocumentProcessor} 的实体。
     *
     * @param rerankOption 表示 rerank 模型参数的 {@link  RerankOption}
     * @param rerankModel 表示 rerank 模型接口的 {@link  RerankModel}
     */
    public RerankDocumentProcessor(RerankOption rerankOption, RerankModel rerankModel) {
        this.rerankOption = Validation.notNull(rerankOption, "The rerankOption cannot be null.");
        this.rerankModel = Validation.notNull(rerankModel, "The rerankModel cannot be null.");
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
        return rerankModel.generate(documents, rerankOption);
    }
}