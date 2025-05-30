/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context.repo.flowsession;

import modelengine.fit.waterflow.domain.context.FlatMapSourceWindow;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.Window;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fitframework.inspection.Validation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程运行中 session 相关的数据缓存，用于统一管理这些数据和在 session 完成时统一进行释放。
 *
 * @author 宋永坦
 * @since 2025-02-12
 */
public class FlowSessionRepo {
    /**
     * 按照流管理 session 相关资源的释放，其中键为流标识，元素中键为 session 的唯一标识。
     */
    private static final Map<String, Map<String, FlowSessionCache>> cache = new ConcurrentHashMap<>();

    /**
     * 获取该 session 的 window 对应的向下一个节点传递数据使用的 session。
     *
     * @param flowId 流标识。
     * @param session session。
     * @return 下一个 session。
     */
    public static FlowSession getNextToSession(String flowId, FlowSession session) {
        Validation.notNull(flowId, "Flow id cannot be null.");
        Validation.notNull(session, "Session cannot be null.");
        return getFlowSessionCache(flowId, session)
                .getNextToSession(session);
    }

    public static FlowSession getNextEmitterHandleSession(String flowId, FlowSession session) {
        Validation.notNull(flowId, "Flow id cannot be null.");
        Validation.notNull(session, "Session cannot be null.");
        return getFlowSessionCache(flowId, session)
                .getNextEmitterHandleSession(session);
    }

    public static int getNextAccOrder(String flowId, String nodeId, FlowSession session) {
        Validation.notNull(flowId, "Flow id cannot be null.");
        Validation.notNull(nodeId, "Node id cannot be null.");
        Validation.notNull(session, "Session cannot be null.");
        return getFlowSessionCache(flowId, session).getNextAccOrder(nodeId);
    }

    /**
     * 获取该 session 的 window 对应的向下一个 emit listener 传递数据使用的 session。
     *
     * @param flowId 流标识。
     * @param session session。
     * @return 下一个 session。
     */
    public static FlowSession getNextEmitSession(String flowId, Object listener, FlowSession session) {
        Validation.notNull(flowId, "Flow id cannot be null.");
        Validation.notNull(listener, "Listener cannot be null.");
        Validation.notNull(session, "Session cannot be null.");
        return getFlowSessionCache(flowId, session)
                .getNextEmitSession(session, listener);
    }

    /**
     * 获取 flatMap 节点生成的 {@link FlatMapSourceWindow}。
     *
     * @param flowId 流标识。
     * @param window 进入到 flatMap 节点数据对应的window。
     * @param repo 流程数据上下文的持久化对象。
     * @return 对应的 {@link FlatMapSourceWindow}。
     */
    public static FlatMapSourceWindow getFlatMapSource(String flowId, Window window, FlowContextRepo repo) {
        Validation.notNull(flowId, "Flow id cannot be null.");
        Validation.notNull(window, "Window cannot be null.");
        Validation.notNull(window.getSession(), "Session cannot be null.");
        Validation.notNull(repo, "Repo cannot be null.");
        return getFlowSessionCache(flowId, window.getSession())
                .getFlatMapSourceWindow(window, repo);
    }

    /**
     * 释放对应流 session 下的所有资源。
     *
     * @param flowId 流标识。
     * @param session 需要释放资源的 session。
     */
    public static void release(String flowId, FlowSession session) {
        Validation.notNull(flowId, "Flow id cannot be null.");
        Validation.notNull(session, "Session cannot be null.");
        cache.compute(flowId, (__, value) -> {
            if (value == null) {
                return null;
            }
            value.remove(session.getId());
            if (value.isEmpty()) {
                return null;
            }
            return value;
        });
    }

    private static FlowSessionCache getFlowSessionCache(String flowId, FlowSession session) {
        return cache.compute(flowId, (__, value) -> {
            Map<String, FlowSessionCache> sessionCacheMap = value;
            if (sessionCacheMap == null) {
                sessionCacheMap = new ConcurrentHashMap<>();
            }
            sessionCacheMap.computeIfAbsent(session.getId(), id -> new FlowSessionCache());
            return sessionCacheMap;
        }).get(session.getId());
    }

    private static class FlowSessionCache {
        /**
         * 记录每个节点向下个节点流转数据时，下个节点使用的 session，用于将同一批数据汇聚。
         * 其中索引为当前节点正在处理数据的窗口的唯一标识。
         */
        private final Map<UUID, FlowSession> nextToSessions = new ConcurrentHashMap<>();

        /**
         * 记录每个节点向 EmitterListener 流转数据时使用的 session，用于将同一批数据汇聚。
         * 其中索引为当前节点正在处理数据的窗口的唯一标识。值中数据的索引为 listener。
         */
        private final Map<UUID, Map<Object, FlowSession>> nextEmitSessions = new ConcurrentHashMap<>();

        private final Map<UUID, FlowSession> nextEmitterHandleSessions = new ConcurrentHashMap<>();

        /**
         * 记录流程中经过 flatMap 节点产生的窗口信息，用于将同一批数据汇聚。
         * 其中索引为当前节点正在处理数据的窗口的唯一标识。
         */
        private final Map<UUID, FlatMapSourceWindow> flatMapSourceWindows = new ConcurrentHashMap<>();

        private final Map<String, Integer> accOrders = new ConcurrentHashMap<>();

        /**
         * 获取该 session 的 window 对应的向下一个节点传递数据使用的 session。
         *
         * @param session session。
         * @return 下一个 session。
         */
        private FlowSession getNextToSession(FlowSession session) {
            return this.nextToSessions.computeIfAbsent(session.getWindow().key(), __ -> generateNextSession(session));
        }

        /**
         * 获取该 session 的 window 对应的向下一个节点传递数据使用的 session。
         *
         * @param session session。
         * @param listener listener。
         * @return 下一个 session。
         */
        private FlowSession getNextEmitSession(FlowSession session, Object listener) {
            return this.nextEmitSessions.computeIfAbsent(session.getWindow().key(), __ -> new ConcurrentHashMap<>())
                    .computeIfAbsent(listener, __ -> generateNextSession(session));
        }

        private FlowSession getNextEmitterHandleSession(FlowSession session) {
            return this.nextEmitterHandleSessions.computeIfAbsent(session.getWindow().key(), __ -> {
                FlowSession next = FlowSession.newRootSession(session, session.preserved());
                Window nextWindow = next.begin();
                // if the processor is not reduce, then inherit previous window condition
                if (!session.isAccumulator()) {
                    nextWindow.setCondition(session.getWindow().getCondition());
                }
                return next;
            });
        }

        /**
         * 获取 flatMap 节点生成的 {@link FlatMapSourceWindow}。
         *
         * @param window 进入到 flatMap 节点数据对应的window。
         * @param repo 流程数据上下文的持久化对象。
         * @return 对应的 {@link FlatMapSourceWindow}。
         */
        private FlatMapSourceWindow getFlatMapSourceWindow(Window window, FlowContextRepo repo) {
            return this.flatMapSourceWindows.computeIfAbsent(window.key(), __ -> {
                FlatMapSourceWindow newWindow = new FlatMapSourceWindow(window, repo);
                newWindow.setSession(new FlowSession(window.getSession().preserved()));
                newWindow.getSession().setWindow(newWindow);
                newWindow.getSession().begin();
                return newWindow;
            });
        }

        private int getNextAccOrder(String nodeId) {
            return this.accOrders.compute(nodeId, (key, value) -> {
                if (value == null) {
                    return 0;
                }
                return value + 1;
            });
        }

        private static FlowSession generateNextSession(FlowSession session) {
            FlowSession next = new FlowSession(session);
            Window nextWindow = next.begin();
            // if the processor is not reduce, then inherit previous window condition
            if (!session.isAccumulator()) {
                nextWindow.setCondition(session.getWindow().getCondition());
            }
            return next;
        }
    }
}
