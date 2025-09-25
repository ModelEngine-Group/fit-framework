/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.vectorstore;

import modelengine.fel.core.document.DocumentStore;
import modelengine.fel.core.document.MeasurableDocument;

import java.util.List;

/**
 * 表示向量数据库的接口。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public interface VectorStore extends DocumentStore {
    /**
     * 执行向量检索。
     *
     * @param query 表示用户询问的 {@link String}。
     * @param option 表示搜索参数的 {@link SearchOption}.
     * @return 表示搜索结果列表的 {@link List}{@code <}{@link MeasurableDocument}{@code >}。
     */
    List<MeasurableDocument> search(String query, SearchOption option);

    /**
     * 删除指定唯一标识的文档。
     *
     * @param ids 表示需要删除的文档标志的 {@link List}{@code <}{@link String}{@code >}。
     */
    void delete(List<String> ids);
}