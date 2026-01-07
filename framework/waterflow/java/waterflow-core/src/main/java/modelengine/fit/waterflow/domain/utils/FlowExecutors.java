/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.utils;

import static java.util.concurrent.TimeUnit.SECONDS;

import modelengine.fitframework.log.Logger;
import modelengine.fitframework.schedule.ThreadPoolExecutor;
import modelengine.fitframework.thread.DefaultThreadFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流程引擎的节点线程池
 *
 * @author 高诗意
 * @since 1.0
 */
public final class FlowExecutors {
    private static final int MAX_THREAD_COUNT = 64;

    private static final int CORE_THREAD_COUNT = 8;

    private static final Logger LOG = Logger.get(FlowExecutors.class);

    private static final PriorityThreadPool THREAD_POOL;

    private static AtomicInteger currentConcurrency = new AtomicInteger(0);

    static {
        THREAD_POOL = PriorityThreadPool.build("flow-node-thread-pool", CORE_THREAD_COUNT, MAX_THREAD_COUNT);
    }

    /**
     * 获取执行线程池
     *
     * @return 线程池对象
     */
    public static PriorityThreadPool getThreadPool() {
        return THREAD_POOL;
    }

    /**
     * 增加一个并发
     *
     * @return 并发持有对象。如果无法增加并发则返回null对象
     */
    public static synchronized Optional<ConcurrencyHolder> incrementConcurrency() {
        if (currentConcurrency.incrementAndGet() > MAX_THREAD_COUNT) {
            currentConcurrency.decrementAndGet();
            return Optional.empty();
        }
        return Optional.of(new ConcurrencyHolder());
    }

    private static synchronized void decrementConcurrency() {
        currentConcurrency.decrementAndGet();
    }

    /**
     * 提交任务到固定键线程池
     *
     * @param data 数据对象，此参数暂未使用，可以用于扩展
     * @param key 键值，用于决定任务执行的线程
     * @param task 需要执行的任务
     */
    public static void submit(Object data, String key, Runnable task) {
        FixedKeyThreadPool.get().execute(key, task);
    }

    /**
     * 并发持有对象
     *
     * @author s00558940
     * @since 2024/8/12
     */
    public static class ConcurrencyHolder {
        private boolean isReleased;

        private ConcurrencyHolder() {
            this.isReleased = false;
        }

        /**
         * 释放并发
         */
        public void release() {
            if (!this.isReleased) {
                this.isReleased = true;
                FlowExecutors.decrementConcurrency();
            }
        }
    }

    private static class FixedKeyThreadPool {
        private static FixedKeyThreadPool threadPool;

        private final ExecutorService[] executors;

        private final int poolSize;

        private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

        private FixedKeyThreadPool(int poolSize) {
            this.poolSize = poolSize;
            this.executors = new ExecutorService[poolSize];

            for (int i = 0; i < poolSize; i++) {
                this.executors[i] = Executors.newSingleThreadExecutor(
                        new DefaultThreadFactory("flow-fixed-key", true, (thread, ex) -> {
                            LOG.error("The node pool run failed, ex: {}, message: {}.", ex, ex.getMessage());
                        }));
            }
        }

        private static FixedKeyThreadPool get() {
            if (threadPool != null) {
                return threadPool;
            }
            return init();
        }

        private static synchronized FixedKeyThreadPool init() {
            if (threadPool != null) {
                return threadPool;
            }
            threadPool = new FixedKeyThreadPool(32);
            return threadPool;
        }

        /**
         * 跟定key和task，找到线程执行
         *
         * @param key 任务的key
         * @param task 待执行的任务
         */
        public void execute(String key, Runnable task) {
            // 通过hash值获取固定线程池
            int threadIndex = Math.abs(key.hashCode() % poolSize);
            Integer current = threadLocal.get();
            if (current != null && current == threadIndex) {
                task.run();
                return;
            }
            executors[threadIndex].submit(new Runnable() {
                @Override
                public void run() {
                    threadLocal.set(threadIndex);
                    task.run();
                    threadLocal.set(-1);
                }
            });
        }

        /**
         * 关闭固定键线程池
         * 关闭所有的固定键线程池，并且释放所有的线程资源
         */
        public void shutdown() {
            for (ExecutorService executor : executors) {
                executor.shutdown();
            }
        }
    }
}
