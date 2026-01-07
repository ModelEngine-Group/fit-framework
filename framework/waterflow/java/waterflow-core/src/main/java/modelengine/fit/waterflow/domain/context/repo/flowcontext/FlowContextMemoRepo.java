/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context.repo.flowcontext;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowTrace;
import modelengine.fit.waterflow.domain.context.TraceOwner;
import modelengine.fit.waterflow.domain.enums.FlowNodeStatus;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fitframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 持久化{@link FlowContext}对象到内存中核心类
 *
 * @author 高诗意
 * @since 1.0
 */
public class FlowContextMemoRepo implements FlowContextRepo {
    private final ConcurrentLinkedHashMap<String, FlowContext> contextsMap = new ConcurrentLinkedHashMap<>();

    private final boolean isReserveTerminal;

    private final TraceOwner traceOwner = new TraceOwner() {
        @Override
        public void own(String traceId, String transId) {
        }

        @Override
        public boolean tryOwn(String traceId, String transId) {
            return true;
        }

        @Override
        public void release(String traceId) {
        }

        @Override
        public boolean isOwn(String traceId) {
            return true;
        }

        @Override
        public boolean isAnyOwn(Set<String> traceIds) {
            return true;
        }

        @Override
        public List<String> getTraces() {
            return List.of();
        }

        @Override
        public List<String> getTraces(String targetTransId) {
            return List.of();
        }

        @Override
        public void removeInvalidTrace(Lock invalidLock) {
        }

        @Override
        public boolean isInProtectTime(String traceId) {
            return false;
        }
    };

    @Override
    public TraceOwner getTraceOwner() {
        return this.traceOwner;
    }

    /**
     * 构造方法
     */
    public FlowContextMemoRepo() {
        this(false);
    }

    /**
     * 构造方法
     *
     * @param isReserveTerminal 是否保留结束的数据，一般只有测试才保留
     */
    public FlowContextMemoRepo(boolean isReserveTerminal) {
        this.isReserveTerminal = isReserveTerminal;
    }

    private <T> List<FlowContext<T>> query(Function<Stream<FlowContext<T>>, Stream<FlowContext<T>>> filter) {
        return filter.apply(this.contextsMap.stream()
                        .map(p -> (FlowContext<T>) p))
                .collect(Collectors.toList());
    }


    @Override
    public <T> List<FlowContext<T>> getContextsByPosition(String streamId, List<String> posIds, String status) {
        return query(stream -> stream
                .filter(context -> context.getStreamId().equals(streamId))
                .filter(context -> posIds.contains(context.getPosition()))
                .filter(context -> context.getStatus().toString().equals(status))
                .filter(context -> !context.isSent()));
    }

    @Override
    public <T> List<FlowContext<T>> getContextsByPosition(String streamId, String posId, String batchId, String status) {
        return query(stream -> stream
                .filter(context -> context.getStreamId().equals(streamId))
                .filter(context -> context.getPosition().equals(posId))
                .filter(context -> context.getBatchId().equals(batchId))
                .filter(context -> context.getStatus().toString().equals(status)));
    }

    @Override
    public <T> List<FlowContext<T>> findWithoutFlowDataByTraceId(String traceId) {
        throw new IllegalStateException("Not support");
    }

    @Override
    public <T> List<FlowContext<T>> getContextsByTrace(String traceId) {
        return query(stream -> stream
                .filter(context -> context.getTraceId().contains(traceId)));
    }

    @Override
    public synchronized <T> void save(List<FlowContext<T>> contexts) {
        contexts.forEach(context -> {
            if (this.isReserveTerminal) {
                this.contextsMap.put(context.getId(), context);
                return;
            }
            if (context.getStatus() == FlowNodeStatus.ARCHIVED || context.getStatus() == FlowNodeStatus.ERROR) {
                this.contextsMap.remove(context.getId());
            } else {
                this.contextsMap.put(context.getId(), context);
            }
        });
    }

    @Override
    public <T> void updateFlowDataAndToBatch(List<FlowContext<T>> contexts) {
        save(contexts);
    }

    @Override
    public synchronized <T> void updateFlowData(Map<String, T> flowDataList) {
        flowDataList.forEach((contextId, data) -> {
            FlowContext<T> flowContext = ObjectUtils.cast(this.contextsMap.get(contextId));
            if (flowContext != null) {
                flowContext.setData(data);
            }
        });
    }

    @Override
    public <T> void updateToSent(List<FlowContext<T>> contexts) {
        save(contexts);
    }

    @Override
    public <T> List<FlowContext<T>> getContextsByParallel(String parallelId) {
        return query(stream -> stream
                .filter(context -> context.getParallel().equals(parallelId)));
    }

    @Override
    public <T> FlowContext<T> getById(String id) {
        return this.contextsMap.stream()
                .filter(context -> context.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public <T> List<FlowContext<T>> getPendingAndSentByIds(List<String> ids) {
        return query(stream -> stream
                .filter(context -> context.getStatus().equals(FlowNodeStatus.PENDING))
                .filter(FlowContext::isSent)
                .filter(context -> ids.contains(context.getId())));
    }

    @Override
    public <T> List<FlowContext<T>> getByIds(List<String> ids) {
        return ids.stream().map(i -> (FlowContext<T>) contextsMap.get(i)).collect(Collectors.toList());
    }

    @Override
    public <T> List<FlowContext<T>> getByToBatch(List<String> toBatchIds) {
        return query(stream -> stream
                .filter(context -> context.getStatus().equals(FlowNodeStatus.PENDING))
                .filter(FlowContext::isSent)
                .filter(context -> toBatchIds.contains(context.getToBatch())));
    }

    @Override
    public <T> List<FlowContext<T>> requestMappingContext(String streamId, List<String> subscriptions,
        Map<String, Integer> sessions) {
        return query(stream -> stream
                .filter(context -> context.getStreamId().equals(streamId))
                .filter(context -> subscriptions.contains(context.getPosition()))
                .filter(context -> context.getStatus() == FlowNodeStatus.PENDING)
                .filter(context -> {
                    boolean found = false;
                    for (String s : sessions.keySet()) {
                        found = context.getSession().getId().equals(s)
                                && (Objects.equals(context.getIndex(), sessions.get(s)));
                        if (found) {
                            break;
                        }
                    }
                    return context.getIndex() == -1 || context.getIndex() == 0 || found;//找到需要保序的当前序列或者不需要保序的
                })
                .limit(1));
    }

    @Override
    public <T> List<FlowContext<T>> requestProducingContext(String streamId, List<String> subscriptions,
                                                            Operators.Filter<T> filter) {
        List<FlowContext<T>> all = query(stream -> stream
                .filter(context -> context.getStreamId().equals(streamId))
                .filter(context -> subscriptions.contains(context.getPosition()))
                .filter(context -> context.getStatus() == FlowNodeStatus.PENDING));
        return filter.process(all);
    }

    @Override
    public <T> void save(FlowTrace trace, FlowContext<T> flowContext) {
    }

    @Override
    public <T> void updateFlowData(List<FlowContext<T>> contexts) {
        save(contexts);
    }

    @Override
    public <T> void updateIndex(List<FlowContext<T>> contexts) {
        List<FlowContext<T>> updated = new ArrayList<>();
        for (FlowContext<T> context : contexts) {
            FlowContext<T> saved = this.contextsMap.get(context.getId());
            if (saved == null) {
                saved = context;
            } else {
                saved.setIndex(context.getIndex());
            }
            updated.add(saved);
        }
        this.save(updated);
    }

    /**
     * 构造一个支持并发且可以保障元素顺序的map
     *
     * @param <K> map的key类型
     * @param <V> map的value类型
     */
    private static class ConcurrentLinkedHashMap<K, V> {
        private final Map<K, V> map = new ConcurrentHashMap<>();

        private final Deque<K> order = new ConcurrentLinkedDeque<>();

        /**
         * put一个键值对
         *
         * @param key key
         * @param value value
         */
        public void put(K key, V value) {
            if (!map.containsKey(key)) {
                order.add(key);
            }
            map.put(key, value);
        }

        /**
         * 根据key获取结果
         *
         * @param key key
         * @return 对应的value，可能为null
         */
        public V get(K key) {
            return map.get(key);
        }

        /**
         * 删除一个key
         *
         * @param key key
         * @return 被删除的值
         */
        public V remove(K key) {
            order.remove(key);
            return map.remove(key);
        }

        /**
         * 获取流式的元素
         *
         * @return 流式元素
         */
        public Stream<V> stream() {
            return order.stream().map(map::get).filter(obj -> !Objects.isNull(obj));
        }
    }
}
