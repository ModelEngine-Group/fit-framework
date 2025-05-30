/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fit.http.annotation.PostMapping;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 表示测试使用的 Rerank 接口。
 *
 * @author 马朝阳
 * @since 2024-09-27
 */
@Component
public class TestRerankModelController {
    /**
     * Rerank 接口失败调用端口。
     */
    public static final String FAIL_ENDPOINT = "/fail";

    private final ObjectSerializer serializer;

    TestRerankModelController(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 测试成功用 Rerank 接口。
     *
     * @return 表示流式返回结果的 {@link String}。
     */
    @PostMapping(RerankApi.RERANK_ENDPOINT)
    public RerankResponse rerankSuccess() {
        String json =
                "{\"results\":[{\"index\":3,\"relevance_score\":0.999071},{\"index\":4,\"relevance_score\":0.7867867},"
                        + "{\"index\":0,\"relevance_score\":0.32713068}]}";
        return this.serializer.deserialize(json, RerankResponse.class);
    }

    /**
     * 测试用 Rerank 接口。
     *
     * @return 表示流式返回结果的 {@link String}。
     */
    @PostMapping(FAIL_ENDPOINT + RerankApi.RERANK_ENDPOINT)
    public RerankResponse rerankFail() {
        String json = "wrong json";
        return this.serializer.deserialize(json, RerankResponse.class);
    }
}
