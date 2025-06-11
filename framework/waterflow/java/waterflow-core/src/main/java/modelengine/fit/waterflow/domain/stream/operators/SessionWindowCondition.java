/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.operators;

/**
 * session的window的条件
 *
 * @since 1.0
 */
public class SessionWindowCondition implements Operators.WindowCondition {
    private final String key;

    private final Operators.WindowCondition windowCondition;

    private SessionWindowCondition(String key, Operators.WindowCondition windowCondition) {
        this.windowCondition = windowCondition;
        this.key = key;
    }

    /**
     * 指定一个window条件，构造一个sessionWindow
     *
     * @param key 用于构建window的key
     * @param windowCondition 给定的window条件
     * @return 构造后的sessionWindow
     */
    public static SessionWindowCondition from(String key, Operators.WindowCondition windowCondition) {
        return new SessionWindowCondition(key, windowCondition);
    }

    /**
     * 指定一个window条件，构造一个sessionWindow
     *
     * @param windowCondition 给定的window条件
     * @return 构造后的sessionWindow
     */
    public static SessionWindowCondition from(Operators.WindowCondition windowCondition) {
        return from(null, windowCondition);
    }

    @Override
    public boolean fulfilled(WindowArg arg) {
        return this.windowCondition.fulfilled(arg);
    }
}
