/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.utils;

import lombok.Data;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.thread.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 带有任务优先级的线程池。
 *
 * @author 夏斐
 * @since 2024/3/6
 */
public class PriorityThreadPool {
    private static final Logger LOG = Logger.get(PriorityThreadPool.class);

    private final ExecutorService executorService;

    private PriorityThreadPool(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * 构造一个线程池。
     *
     * @param key 线程池名称。
     * @param coreSize 核心线程数。
     * @param maxSize 最大线程数。
     * @return 带有优先队列的线程池。
     */
    public static PriorityThreadPool build(String key, int coreSize, int maxSize) {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (thread, throwable) -> {
            LOG.error("[node-pool-{}]:  The node pool run failed, error cause: {}, message: {}.", key,
                    throwable.getCause(), throwable.getMessage());
            LOG.error("The node pool run failed details: ", throwable);
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                coreSize, maxSize, 60L, SECONDS, new PriorityBlockingQueue<>(),
                new DefaultThreadFactory(key, true, uncaughtExceptionHandler),
                new ThreadPoolExecutor.AbortPolicy());
        return new PriorityThreadPool(threadPoolExecutor);
    }

    /**
     * 提交任务。
     *
     * @param task 任务。
     */
    public void submit(PriorityTask task) {
        this.executorService.execute(task);
    }

    /**
     * 关闭。
     */
    public void shutdown() {
        this.executorService.shutdown();
    }

    /**
     * 优先队列提交的任务。
     */
    public abstract static class PriorityTask implements Runnable, Comparable<PriorityTask> {
        /**
         * 任务构造器。
         *
         * @return 返回一个构造器实例。
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * 得到优先级。
         *
         * @return 优先级。
         */
        public abstract PriorityInfo getPriority();

        @Override
        public int compareTo(PriorityTask other) {
            PriorityInfo thisPriority = this.getPriority();
            PriorityInfo otherPriority = other.getPriority();
            // 从trace粒度比较，越早的trace执行优先级越高
            int traceTimeCompare = Long.compare(thisPriority.getTraceTime(), otherPriority.getTraceTime());
            if (traceTimeCompare != 0) {
                return traceTimeCompare;
            }
            // 从节点优先级比较，越往后的节点执行优先级越高
            int orderCompare = Long.compare(otherPriority.getOrder(), thisPriority.getOrder());
            if (orderCompare != 0) {
                return orderCompare;
            }
            // 从当前任务的创建实践比较，越早创建执行优先级越高
            return Long.compare(thisPriority.getCreateTime(), otherPriority.getCreateTime());
        }

        @Override
        public String toString() {
            return getPriority().toString();
        }

        /**
         * 优先级信息。
         *
         * @author 夏斐
         * @since 2024/3/6
         */
        @lombok.Builder
        @Data
        public static class PriorityInfo {
            private int order;

            private long createTime;

            // 流程时间
            private long traceTime = 0L;
        }

        /**
         * 构造器
         */
        public static class Builder {
            private PriorityInfo priority;

            private Runnable runnable;

            /**
             * 设置优先级。
             *
             * @param priority 优先级。
             * @return 构造器。
             */
            public Builder priority(PriorityInfo priority) {
                this.priority = priority;
                return this;
            }

            /**
             * 设置 runner。
             *
             * @param runnable runner。
             * @return 构造器。
             */
            public Builder runner(Runnable runnable) {
                this.runnable = runnable;
                return this;
            }

            /**
             * 构造任务。
             *
             * @return 任务。
             */
            public PriorityTask build() {
                return new PriorityTask() {
                    @Override
                    public PriorityInfo getPriority() {
                        return Builder.this.priority;
                    }

                    @Override
                    public void run() {
                        Builder.this.runnable.run();
                    }
                };
            }
        }
    }
}
