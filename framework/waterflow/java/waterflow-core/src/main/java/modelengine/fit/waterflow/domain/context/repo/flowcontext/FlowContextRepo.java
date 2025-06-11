/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context.repo.flowcontext;

import modelengine.fit.waterflow.ErrorCodes;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowTrace;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.exceptions.WaterflowException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程上下文持久化Repo核心类型
 * 包含FlowContextMemoRepo和FlowContextPersistRepo两种实现
 *
 * @author 高诗意
 * @since 1.0
 */
public interface FlowContextRepo {
    /**
     * 人工任务节点拉取边上的上下文，在节点的preprocess中处理
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param streamId 版本ID
     * @param posIds posId列表
     * @param status 状态
     * @return 上下文列表
     */
    <T> List<FlowContext<T>> getContextsByPosition(String streamId, List<String> posIds, String status);

    /**
     * 获取节点处理完后产生的新的context，发送给下个节点处理，后续可以判断是否删除该方法
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param streamId 版本ID
     * @param posId posId
     * @param batchId 批次ID
     * @param status 状态
     * @return 上下文列表
     */
    <T> List<FlowContext<T>> getContextsByPosition(String streamId, String posId, String batchId, String status);

    /**
     * 根据traceId获取上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param traceId traceId
     * @return 上下文列表
     */
    <T> List<FlowContext<T>> getContextsByTrace(String traceId);

    /**
     * 批量保存context
     *
     * @param <I> 泛型类型，表示上下文的数据类型
     * @param contexts 上下文列表
     */
    <I> void save(List<FlowContext<I>> contexts);

    /**
     * 批量更新context的内容，不更新status和position
     *
     * @param <I> 泛型类型，表示上下文的数据类型
     * @param contexts 上下文列表
     */
    default <I> void update(List<FlowContext<I>> contexts) {
        save(contexts);
    }

    /**
     * 更新context的状态为已发送
     *
     * @param <I> 泛型类型，表示上下文的数据类型
     * @param contexts 上下文列表
     */
    <I> void updateToSent(List<FlowContext<I>> contexts);

    /**
     * 根据parallelId获取上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param parallelId 并行ID
     * @return 上下文列表
     */
    <T> List<FlowContext<T>> getContextsByParallel(String parallelId);

    /**
     * 根据id获取上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param id 上下文ID
     * @return 上下文对象
     */
    <T> FlowContext<T> getById(String id);

    /**
     * 根据ids查找FlowContext
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param ids 上下文ID列表
     * @return 上下文列表
     */
    <T> List<FlowContext<T>> getByIds(List<String> ids);

    /**
     * 查找和指定一批ID对应的状态为PENDING且SENT了的流程上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param ids 上下文ID列表
     * @return 上下文列表
     */
    <T> List<FlowContext<T>> getPendingAndSentByIds(List<String> ids);

    /**
     * 查找map节点所有from事件上待处理的上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param streamId 流程版本ID
     * @param subscriptions from事件的事件ID列表
     * @param sessions 涉及保序的sessions
     * @return 待处理的上下文列表
     */
    <T> List<FlowContext<T>> requestMappingContext(String streamId, List<String> subscriptions,
            Map<String, Integer> sessions);

    /**
     * 查找produce节点所有from事件上待处理的上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param streamId 流程版本ID
     * @param subscriptions from事件的事件ID列表
     * @param filter filter校验器
     * @return 待处理的上下文列表
     */
    <T> List<FlowContext<T>> requestProducingContext(String streamId, List<String> subscriptions,
            Operators.Filter<T> filter);

    /**
     * 查找流程对应版本所有上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param metaId 流程metaId标识
     * @param version 流程对应版本
     * @return 对应所有上下文列表
     */
    default <T> List<FlowContext<T>> findByStreamId(String metaId, String version) {
        throw new WaterflowException(ErrorCodes.FLOW_ENGINE_DATABASE_NOT_SUPPORT, "findByStreamId");
    }

    /**
     * 查找流程对应版本正在运行的上下文
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param metaId metaId 流程metaId标识
     * @param version 流程对应版本
     * @return 对应所有上下文列表
     */
    default <T> List<FlowContext<T>> findRunningContextByMetaId(String metaId, String version) {
        throw new WaterflowException(ErrorCodes.FLOW_ENGINE_DATABASE_NOT_SUPPORT, "findRunningContextByMetaId");
    }

    /**
     * 删除流程对应版本所有上下文
     *
     * @param metaId metaId 流程metaId标识
     * @param version 流程对应版本
     */
    default void delete(String metaId, String version) {
        throw new WaterflowException(ErrorCodes.FLOW_ENGINE_DATABASE_NOT_SUPPORT, "delete");
    }

    /**
     * 批量更新trace的contextPool
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param after 更新后的上下文列表
     * @param traces 需要更新的traceId列表
     */
    default <T> void updateContextPool(List<FlowContext<T>> after, Set<String> traces) {
        save(after);
    }

    /**
     * 保存contexts
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param trace 对应的trace
     * @param flowContext 待保存的contexts
     */
    <T> void save(FlowTrace trace, FlowContext<T> flowContext);

    /**
     * 批量更新context的上下文数据flowData字段
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param contexts 上下文列表
     */
    <T> void updateFlowData(List<FlowContext<T>> contexts);

    /**
     * 批量更新context的status和position
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param contexts 上下文列表
     * @param status 状态
     * @param position 位置
     */
    default <T> void updateStatus(List<FlowContext<T>> contexts, String status, String position) {
        save(contexts);
    }

    /**
     * 更新context和trace的状态
     *
     * @param traceIds traceIds列表
     */
    default void updateToTerminated(List<String> traceIds) {
    }

    /**
     * 判断trace终止
     *
     * @param traceIds traceIds列表
     * @return 是否终止
     */
    default boolean isTracesTerminate(List<String> traceIds) {
        return false;
    }

    /**
     * 更新序号
     *
     * @param <T> 泛型类型，表示上下文的数据类型
     * @param contexts 上下文信息
     */
    <T> void updateIndex(List<FlowContext<T>> contexts);
}

