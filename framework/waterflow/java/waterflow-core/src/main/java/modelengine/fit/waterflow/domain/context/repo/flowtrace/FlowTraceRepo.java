/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context.repo.flowtrace;

import modelengine.fit.waterflow.domain.context.FlowTrace;
import modelengine.fit.waterflow.domain.enums.FlowTraceStatus;

import java.util.List;

/**
 * 流程实例Trace repo核心类
 *
 * @author 杨祥宇
 * @since 1.0
 */
public interface FlowTraceRepo {
    /**
     * 保存flowTrace对象
     *
     * @param flowTrace flowTrace对象实体
     */
    void save(FlowTrace flowTrace);

    /**
     * 根据flowTrace id标识查询flowTrace对象
     *
     * @param traceId flowTrace id标识
     * @return flowTrace flowTrace对象实体
     */
    FlowTrace find(String traceId);

    /**
     * 根据 ids 查找FlowTrace
     *
     * @param ids traceId列表
     * @return List
     */
    List<FlowTrace> getByIds(List<String> ids);

    /**
     * 删除对应stream的所有trace对象
     *
     * @param streamId {@link String}
     */
    void delete(String streamId);

    /**
     * 批量更新trace
     *
     * @param flowTraces flowTraces
     */
    void batchUpdate(List<FlowTrace> flowTraces);

    /**
     * 批量保存trace
     *
     * @param flowTraces flowTraces
     */
    void batchCreate(List<FlowTrace> flowTraces);

    /**
     * 批量更新contextPool
     *
     * @param traceList 流程实例trace列表
     * @param contextList context列表
     */
    void updateContextPool(List<String> traceList, List<String> contextList);

    /**
     * 批量查询trace
     *
     * @param traceIds trace id列表
     * @return trace列表
     */
    List<FlowTrace> findTraceByIdList(List<String> traceIds);

    /**
     * 更新trace的状态
     *
     * @param ids traceIds
     * @param status trace状态 {@link FlowTraceStatus}
     */
    void updateStatus(List<String> ids, String status);
}
