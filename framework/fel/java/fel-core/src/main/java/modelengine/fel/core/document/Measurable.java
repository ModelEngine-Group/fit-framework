/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document;

import modelengine.fitframework.inspection.Nonnull;

/**
 * 表示具有量化能力的对象。
 *
 * @author 易文渊
 * @since 2024-08-08
 */
public interface Measurable {
    /**
     * 获取当前对象的量化分数。
     *
     * @return 表示当前对象的量化分数的 {@code double}。
     */
    double score();

    /**
     * 获取文档的分组标识。
     *
     * @return 表示文档分组标识的 {@link String}。
     */
    @Nonnull
    String group();
}