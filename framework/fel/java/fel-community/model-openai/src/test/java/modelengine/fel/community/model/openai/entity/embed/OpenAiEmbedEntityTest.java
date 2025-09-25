/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.embed;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.serialization.ObjectSerializer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * 测试 {@link modelengine.fel.community.model.openai.entity.embed} 下对象的序列化和反序列化。
 *
 * @author 易文渊
 * @since 2024-08-18
 */
@DisplayName("测试 openai 嵌入请求相关数据结构序列化与反序列化")
public class OpenAiEmbedEntityTest {
    private static final ObjectSerializer SERIALIZER = new JacksonObjectSerializer(null, null, null, true);

    @Test
    @DisplayName("测试序列化嵌入请求成功")
    void giveOpenAiEmbeddingRequestThenSerializeOk() {
        OpenAiEmbeddingRequest request = new OpenAiEmbeddingRequest(Collections.singletonList("test"), "test_model");
        String excepted = "{\"model\":\"test_model\",\"input\":[\"test\"]}";
        assertThat(SERIALIZER.serialize(request)).isEqualTo(excepted);
    }

    @Test
    @DisplayName("测试反序列化嵌入响应成功")
    void giveOpenAiEmbeddingResponseThenDeserializeOk() {
        String json = "{\"object\":\"list\","
                + "\"data\":[{\"index\":0,\"object\":\"embedding\",\"embedding\":[0.1,0.2,0.3]}],"
                + "\"usage\":{\"prompt_tokens\":1,\"total_tokens\":2}}";
        OpenAiEmbeddingResponse response = SERIALIZER.deserialize(json, OpenAiEmbeddingResponse.class);
        assertThat(response).extracting(r -> r.data().get(0).embedding()).isEqualTo(Arrays.asList(0.1F, 0.2F, 0.3F));
    }
}