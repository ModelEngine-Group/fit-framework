/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.serialization.annotation.SerializeStrategy;

import java.util.List;

/**
 * 表示 Rerank API 格式的请求。
 *
 * @author 马朝阳
 * @since 2024-09-27
 */
@SerializeStrategy(include = SerializeStrategy.Include.NON_NULL)
public class RerankRequest {
    private final String model;
    private final String query;
    private final List<String> documents;
    @Property(name = "top_n")
    private final Integer topN;

    /**
     * 创建 {@link RerankRequest} 的实体。
     *
     * @param documents 表示要重新排序的文档对象。
     * @param rerankOption 表示 rerank 模型参数。
     */
    public RerankRequest(RerankOption rerankOption, List<String> documents) {
        Validation.notNull(rerankOption, "The rerankOption cannot be null.");
        this.model = rerankOption.model();
        this.query = rerankOption.query();
        this.documents = Validation.notNull(documents, "The documents cannot be null.");
        this.topN = rerankOption.topN();
    }
}
