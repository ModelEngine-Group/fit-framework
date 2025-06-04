/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain.mvc;

import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fit.http.client.okhttp.OkHttpClassicClientFactory;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fit.value.fastjson.FastJsonValueHandler;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.value.ValueFetcher;

import java.util.Map;

/**
 * 为模拟的 MVC 所使用的 http 客户端提供工厂。
 *
 * @author 王攀博
 * @since 2024-04-09
 */
public class HttpClientFactory {
    // 测试框架的默认 Config - OkHttpClient 的缓存数量。
    private static final int DEFAULT_CLIENT_CACHE_MAX_SIZE = 1;

    /**
     * 为模拟的 MVC 创建客户端。
     *
     * @return 表示用于模拟测试的 {@link HttpClassicClient}。
     */
    public static HttpClassicClient create() {
        ObjectSerializer jsonSerializer = new JacksonObjectSerializer(null, null, null, true);
        Map<String, ObjectSerializer> serializers =
                MapBuilder.<String, ObjectSerializer>get().put("json", jsonSerializer).build();
        ValueFetcher valueFetcher = new FastJsonValueHandler();
        HttpClassicClientFactory jdkFactory =
                new OkHttpClassicClientFactory(serializers, valueFetcher, DEFAULT_CLIENT_CACHE_MAX_SIZE);
        return jdkFactory.create();
    }
}
