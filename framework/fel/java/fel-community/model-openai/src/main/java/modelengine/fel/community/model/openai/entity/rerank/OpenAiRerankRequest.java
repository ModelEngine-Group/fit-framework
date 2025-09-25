/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.rerank;

import lombok.Data;
import lombok.NoArgsConstructor;
import modelengine.fel.core.rerank.RerankOption;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.serialization.annotation.SerializeStrategy;

import java.util.List;

/**
 * 表示 OpenAI API 格式的重排请求。
 *
 * @author 马朝阳
 * @since 2024-09-27
 */
@Data
@SerializeStrategy(include = SerializeStrategy.Include.NON_NULL)
@NoArgsConstructor
public class OpenAiRerankRequest {
    private String model;
    private String query;
    private List<String> documents;
    @Property(name = "top_n")
    private Integer topN;

    /**
     * 创建 {@link OpenAiRerankRequest} 的实体。
     *
     * @param rerankOption 表示重排模型参数。
     * @param documents 表示要重新排序的文档对象。
     */
    public OpenAiRerankRequest(RerankOption rerankOption, List<String> documents) {
        Validation.notNull(rerankOption, "The rerankOption cannot be null.");
        this.model = rerankOption.model();
        this.query = rerankOption.query();
        this.documents = Validation.notNull(documents, "The documents cannot be null.");
        this.topN = rerankOption.topN();
    }
}
