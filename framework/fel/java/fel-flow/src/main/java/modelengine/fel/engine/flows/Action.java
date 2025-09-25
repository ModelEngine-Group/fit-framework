/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import modelengine.fitframework.inspection.Validation;

/**
 * 结束回调接口。
 *
 * @author 刘信宏
 * @since 2024-04-23
 */
@FunctionalInterface
public interface Action {
    /**
     * 执行结束回调。
     */
    void exec();

    /**
     * 返回一个组合操作。按顺序执行当前操作，然后执行 {@code after} 操作。如果执行当前操作引发异常，则将不会执行 {@code after} 操作。
     *
     * @param after 表示在当前操作之后要执行的操作的 {@link Action}。
     * @return 表示组合操作的 {@link Action}。
     */
    default Action andThen(Action after) {
        Validation.notNull(after, "After action can not be null.");
        return () -> {
            this.exec();
            after.exec();
        };
    }
}
