/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.rerank.RerankModel;
import modelengine.fel.core.rerank.RerankOption;

import java.util.List;

/**
 * 重排模型服务的打桩实现。
 *
 * @since 2025-07-28
 */
class RerankModelStub implements RerankModel {
    @Override
    public List<MeasurableDocument> generate(List<MeasurableDocument> documents, RerankOption rerankOption) {
        return documents;
    }
}
