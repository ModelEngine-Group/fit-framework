/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.eco;

import static modelengine.fitframework.inspection.Validation.isInstanceOf;
import static modelengine.fitframework.inspection.Validation.isTrue;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.support.AbstractTool;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 表示 {@link TaskTool} 的抽象实现父类。
 *
 * @since 2024-06-04
 */
public abstract class AbstractTaskTool extends AbstractTool implements TaskTool {
    /**
     * 通过 Json 序列化器、工具的基本信息和工具元数据来初始化 {@link AbstractTaskTool} 的新实例。
     *
     * @param serializer 表示 Json 序列化器的 {@link ObjectSerializer}。
     * @param itemInfo 表示工具的基本信息的 {@link Info}。
     * @param metadata 表示工具的元数据的 {@link Metadata}。
     */
    protected AbstractTaskTool(ObjectSerializer serializer, Info itemInfo, Metadata metadata) {
        super(serializer, itemInfo, metadata);
    }

    @Override
    public Object execute(Object... args) {
        notNull(args, "The call args cannot be null.");
        isTrue(args.length >= 1, "The call args must have 1 arg at least.");
        String taskId = isInstanceOf(args[0], String.class, "The first arg must be String.class.");
        Object[] actual = new Object[args.length - 1];
        System.arraycopy(args, 1, actual, 0, actual.length);
        return this.executeWithTask(taskId, actual);
    }
}
