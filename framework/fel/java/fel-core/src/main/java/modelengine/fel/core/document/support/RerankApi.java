/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

/**
 * 提供 Rerank 客户端接口：发送 Rerank API 格式的请求并接收响应。
 *
 * @author 马朝阳
 * @since 2024-09-27
 */
public interface RerankApi {
    /**
     * Rerank 模型请求的端点。
     */
    String RERANK_ENDPOINT = "/rerank";
}