/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 流程实例节点状态
 * 状态流转顺序：NEW -> PENDING(停留在EVENT边上) -> READY(进入到节点) -> PROCESSING(开始处理) -> ARCHIVED(处理完成)
 *
 * @author 高诗意
 * @since 1.0
 */
public enum FlowNodeStatus {
    /**
     * 新创建的节点状态
     */
    NEW,

    /**
     * 节点处于等待状态，停留在事件边上
     */
    PENDING,

    /**
     * 节点已准备好，但尚未更新数据库
     */
    READY, // 未更新数据库

    /**
     * 节点正在处理中，但尚未更新数据库
     */
    PROCESSING, // 未更新数据库

    /**
     * 节点已完成处理
     */
    ARCHIVED,

    /**
     * 节点已终止
     */
    TERMINATE,

    /**
     * 节点处理过程中发生错误
     */
    ERROR,

    /**
     * 可重试
     */
    RETRYABLE;

    private static final Set<FlowNodeStatus> END_STATUS = new HashSet<>(Arrays.asList(ARCHIVED, ERROR, TERMINATE));
    private static final Set<FlowNodeStatus> RUNNING_STATUS = new HashSet<>(Arrays.asList(NEW, PENDING, READY, PROCESSING));

    /**
     * 判断是否在运行中的状态。
     *
     * @return 是否在运行中的状态。
     */
    public boolean isRunningStatus() {
        return RUNNING_STATUS.contains(this);
    }

    /**
     * 是否是终态。
     *
     * @return 是否是终态。
     */
    public boolean isEndStatus() {
        return END_STATUS.contains(this);
    }
}