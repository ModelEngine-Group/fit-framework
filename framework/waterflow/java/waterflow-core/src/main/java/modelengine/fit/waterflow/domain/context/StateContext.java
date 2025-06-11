/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

/**
 * 节点的上下文
 *
 * @since 1.0
 */
public interface StateContext {
    /**
     * 获取指定key的上下文数据
     *
     * @param key 指定key
     * @param <R> 返回值的类型
     * @return 上下文数据
     */
    <R> R getState(String key);

    /**
     * 设置上下文数据
     *
     * @param key 指定key
     * @param value 待设置的上下文数据
     */
    void setState(String key, Object value);
}
