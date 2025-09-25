/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities.processors;

import modelengine.fel.engine.activities.AiDataStart;

/**
 * 将每个数据转换为一个数据流，并往下发射流转。
 *
 * @param <T> 表示输入数据类型。
 * @param <R> 表示数据流的数据类型。
 * @author 夏斐
 * @since 2024-05-18
 */
@FunctionalInterface
public interface AiFlatMap<T, R> {
    /**
     * process
     *
     * @param input 表示输入数据的 {@link T}。
     * @return 表示数据前置开始节点的 {@link AiDataStart}{@code <}{@link R}{@code , }{@link R}{@code , ?>}。
     */
    AiDataStart<R, R, ?> process(T input);
}
