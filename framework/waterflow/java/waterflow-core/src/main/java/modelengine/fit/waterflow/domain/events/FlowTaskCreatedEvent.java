/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.events;

import modelengine.fitframework.event.Event;

import java.util.List;

/**
 * flow任务创建了事件
 *
 * @author 杨祥宇
 * @since 1.0
 */
public class FlowTaskCreatedEvent implements Event {
    private final List<String> flowContextId;

    private final String streamId;

    private final String nodeId;

    private final Object publisher;

    /**
     * flow任务创建事件的构造方法
     *
     * @param flowContextId 流上下文ID列表
     * @param streamId 流ID
     * @param nodeId 节点ID
     * @param publisher 发布者对象
     */
    public FlowTaskCreatedEvent(List<String> flowContextId, String streamId, String nodeId, Object publisher) {
        this.flowContextId = flowContextId;
        this.streamId = streamId;
        this.nodeId = nodeId;
        this.publisher = publisher;
    }

    @Override
    public Object publisher() {
        return this.publisher;
    }

    /**
     * 获取流上下文ID列表
     *
     * @return 流上下文ID列表
     */
    public List<String> getFlowContextId() {
        return flowContextId;
    }

    /**
     * 获取流ID
     *
     * @return 流ID
     */
    public String getStreamId() {
        return streamId;
    }

    /**
     * 获取节点ID
     *
     * @return 节点ID
     */
    public String getNodeId() {
        return nodeId;
    }
}
