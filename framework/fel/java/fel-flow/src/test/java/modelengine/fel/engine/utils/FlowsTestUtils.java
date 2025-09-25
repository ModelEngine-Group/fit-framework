/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.utils;

import modelengine.fit.waterflow.domain.utils.SleepUtil;

import java.util.function.Supplier;

/**
 * 异步流程测试等待工具方法。
 *
 * @author 刘信宏
 * @since 2024-04-29
 */
public final class FlowsTestUtils {
    /**
     * 阻塞等待直到指定条件满足。
     *
     * @param stop 表示等待条件 {@link Supplier}{@code <}{@link Boolean}{@code >}。
     * @param most 表示最大等待时间，单位为 ms，最小有效值为20， {@code int}。
     * @throws IllegalStateException 当前等待时间超过设置的最大等待时间时。
     */
    public static void waitUntil(Supplier<Boolean> stop, int most) {
        int time = 0;
        int step = 20;
        while (!stop.get() && time < most) {
            SleepUtil.sleep(step);
            time += step;
        }
        if (time > most) {
            throw new IllegalStateException("ai flows waiting timeout");
        }
    }

    /**
     * 阻塞等待指定条件满足，最大等待 500ms
     *
     * @param stop 表示等待条件 {@link Supplier}{@code <}{@link Boolean}{@code >}。
     * @throws IllegalStateException 当前等待时间超过设置的最大等待时间时。
     */
    public static void waitUntil(Supplier<Boolean> stop) {
        waitUntil(stop, 500);
    }
}

