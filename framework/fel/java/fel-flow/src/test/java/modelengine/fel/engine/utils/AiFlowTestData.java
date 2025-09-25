/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于测试的数据结构。
 *
 * @author 刘信宏
 * @since 2024-04-29
 */
public class AiFlowTestData {
    private final AtomicInteger first = new AtomicInteger(0);
    private final AtomicInteger second = new AtomicInteger(0);
    private final AtomicInteger third = new AtomicInteger(0);

    /**
     * 初始化 {@link AiFlowTestData} 的实例。
     */
    public AiFlowTestData() {}

    /**
     * 初始化 {@link AiFlowTestData}。
     *
     * @param first 表示第一个值的 {@code int}。
     * @param second 表示第二个值的 {@code int}。
     * @param third 表示第三个值的 {@code int}。
     */
    public AiFlowTestData(int first, int second, int third) {
        this.first.set(first);
        this.second.set(second);
        this.third.set(third);
    }

    /**
     * 获取总数。
     *
     * @return 返回元素总和的 {@code int}。
     */
    public int total() {
        return this.first.get() + this.second.get() + this.third.get();
    }

    /**
     * 获取第一个值。
     *
     * @return 返回第一个值的 {@link AtomicInteger}。
     */
    public AtomicInteger first() {
        return this.first;
    }

    /**
     * 获取第二个值。
     *
     * @return 返回第二个值的 {@link AtomicInteger}。
     */
    public AtomicInteger second() {
        return this.second;
    }

    /**
     * 获取第三个值。
     *
     * @return 返回第三个值的 {@link AtomicInteger}。
     */
    public AtomicInteger third() {
        return this.third;
    }
}
