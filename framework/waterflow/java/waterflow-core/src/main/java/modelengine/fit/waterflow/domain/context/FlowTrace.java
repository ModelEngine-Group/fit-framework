/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import modelengine.fit.waterflow.domain.enums.FlowTraceStatus;
import modelengine.fit.waterflow.domain.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 流程实例Trace日志核心类型
 * 主要负责记录和跟踪流程实例执行过程中产生的流程链路日志
 *
 * @author 高诗意
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class FlowTrace extends IdGenerator {
    /**
     * 所在的streamId
     */
    private String streamId;

    /**
     * 流程实例启动时间
     */
    private LocalDateTime startTime;

    /**
     * 流程实例结束时间
     */
    private LocalDateTime endTime;

    /**
     * 流程启动人
     */
    private String operator;

    /**
     * 流程启动的应用，比如天舟、云核
     */
    private String application;

    /**
     * 当前流程开始节点位置
     */
    private String startNode;

    /**
     * 当前流程结束节点位置
     */
    private String endNode;

    /**
     * 当前流程到达最新节点位置
     */
    private Set<String> contextPool;

    /**
     * 流程实例状态
     */
    private FlowTraceStatus status = FlowTraceStatus.READY;

    /**
     * 默认构造函数
     */
    public FlowTrace() {
        contextPool = new HashSet<>();
    }

    /**
     * 流程的trace
     *
     * @param id 指定trace的id
     */
    public FlowTrace(String id) {
        super(id);
        contextPool = new HashSet<>();
    }
}
