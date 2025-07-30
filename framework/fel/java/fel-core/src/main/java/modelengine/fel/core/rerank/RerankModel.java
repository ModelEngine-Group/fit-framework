/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.rerank;

import modelengine.fel.core.document.MeasurableDocument;

import java.util.List;

/**
 * 表示重排模型服务。
 *
 * @since 2025-07-26
 */
public interface RerankModel {
    /**
     * 对检索结果进行重排序。
     *
     * @param documents 表示输入文档的 {@link List}{@code <}{@link MeasurableDocument}{@code >}。
     * @param rerankOption 表示重排模型参数的 {@link RerankOption}。
     * @return 表示处理后文档的 {@link List}{@code <}{@link MeasurableDocument}{@code >}。
     */
    List<MeasurableDocument> generate(List<MeasurableDocument> documents, RerankOption rerankOption);
}
