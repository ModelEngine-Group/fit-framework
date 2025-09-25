/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.pattern;

/**
 * 表示持久化存储的实体。
 *
 * @param <D> 表示待添加数据的泛型。
 * @author 易文渊
 * @since 2024-08-06
 */
@FunctionalInterface
public interface Store<D> extends Pattern<D, Void> {
    /**
     * 添加数据到存储中。
     *
     * @param data 表示待添加数据的 {@link D}。
     */
    void persistent(D data);

    @Override
    default Void invoke(D data) {
        this.persistent(data);
        return null;
    }
}