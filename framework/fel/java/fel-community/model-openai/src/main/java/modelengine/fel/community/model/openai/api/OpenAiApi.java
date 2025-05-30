/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.api;

/**
 * 提供 OpenAI 客户端接口：发送 OpenAI API 格式的请求并接收响应。
 *
 * @author 易文渊
 * @author 张庭怿
 * @since 2024-04-30
 */
public interface OpenAiApi {
    /**
     * 会话补全请求的端点。
     */
    String CHAT_ENDPOINT = "/chat/completions";

    /**
     * 嵌入请求的端点。
     */
    String EMBEDDING_ENDPOINT = "/embeddings";

    /**
     * 图像生成请求的端点。
     */
    String IMAGE_ENDPOINT = "/images/generations";

    /**
     * 请求头模型密钥字段。
     */
    String AUTHORIZATION = "Authorization";
}