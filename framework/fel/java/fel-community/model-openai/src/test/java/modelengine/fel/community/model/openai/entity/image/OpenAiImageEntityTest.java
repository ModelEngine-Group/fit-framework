/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.image;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.serialization.ObjectSerializer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 测试 {@link modelengine.fel.community.model.openai.entity.image} 下对象的序列化和反序列化。
 *
 * @author 何嘉斌
 * @since 2024-12-18
 */
public class OpenAiImageEntityTest {
    private static final ObjectSerializer SERIALIZER = new JacksonObjectSerializer(null, null, null);

    @Test
    @DisplayName("测试序列化图片生成请求成功")
    void giveOpenAiImageRequestThenSerializeOk() {
        OpenAiImageRequest request = new OpenAiImageRequest("model", "256x256", "prompt");
        String excepted = "{\"model\":\"model\",\"size\":\"256x256\",\"prompt\":\"prompt\"}";
        assertThat(SERIALIZER.serialize(request)).isEqualTo(excepted);
    }

    @Test
    @DisplayName("测试反序列化图片生成响应成功")
    void giveOpenAiImageResponseThenDeserializeToMediaOk() {
        String json = "{\"object\":\"list\"," + "\"data\":[{\"url\":\"https://huawei.com\"}, {\"b64_json\":\"456\"}]}";
        OpenAiImageResponse response = SERIALIZER.deserialize(json, OpenAiImageResponse.class);
        assertThat(response).extracting(r -> r.media().stream().map(Media::getMime).collect(Collectors.toList()))
                .isEqualTo(Arrays.asList(null, "image/jpeg"));
        assertThat(response).extracting(r -> r.media().stream().map(Media::getData).collect(Collectors.toList()))
                .isEqualTo(Arrays.asList("https://huawei.com", "456"));
    }
}