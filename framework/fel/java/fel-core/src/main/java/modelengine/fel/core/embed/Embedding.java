/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.embed;

import java.util.List;

/**
 * 表示生成嵌入的实体。
 *
 * @author 易文渊
 * @since 2024-04-13
 */
public interface Embedding {
    /**
     * 获取嵌入向量。
     *
     * @return 表示嵌入向量的 {@link List}{@code <}{@link Float}{@code >}。
     */
    List<Float> embedding();
}