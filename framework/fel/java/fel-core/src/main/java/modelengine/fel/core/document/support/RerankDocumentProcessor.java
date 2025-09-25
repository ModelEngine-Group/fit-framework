/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fel.core.document.DocumentPostProcessor;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.rerank.RerankModel;
import modelengine.fel.core.rerank.RerankOption;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 表示检索文档的后置重排序接口。
 *
 * @author 马朝阳
 * @since 2024-09-14
 */
public class RerankDocumentProcessor implements DocumentPostProcessor {
    private final RerankOption rerankOption;
    private final RerankModel rerankModel;

    /**
     * 创建 {@link RerankDocumentProcessor} 的实体。
     *
     * @param rerankOption 表示重排模型参数的 {@link RerankOption}。
     * @param rerankModel 表示重排模型接口的 {@link RerankModel}。
     */
    public RerankDocumentProcessor(RerankOption rerankOption, RerankModel rerankModel) {
        this.rerankOption = Validation.notNull(rerankOption, "The rerank option cannot be null.");
        this.rerankModel = Validation.notNull(rerankModel, "The rerank model cannot be null.");
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