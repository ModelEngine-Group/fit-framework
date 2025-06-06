/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context.repo.flowlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 流程锁，内存版本的实现
 *
 * @author 高诗意
 * @since 1.0
 */
public class FlowLocksMemo implements FlowLocks {
    private final Map<String, MemLockWrapper> locks = new ConcurrentHashMap<>();

    @Override
    public Lock getLocalLock(String key) {
        return locks.compute(key, (__, value) -> {
            if (value == null) {
                return new MemLockWrapper(key, new ReentrantLock(), this);
            }
            return value;
        });
    }

    /**
     * 获取分布式锁
     * 获取分布式锁的key值，一般是prefix-streamID-nodeID-suffixes
     * 比如key值为：flow-event-streamId-eventId-192.168.0.1; flow-node-streamId-eventId-192.168.0.1
     *
     * @param key 版本ID
     * @return {@link Lock} 锁对象
     */
    @Override
    public Lock getDistributeLock(String key) {
        return getLocalLock(key);
    }

    private void tryCleanLocalLock(String key) {
        this.locks.compute(key, (__, value) -> {
            if (value == null) {
                return null;
            }
            if (value.getRefCount() == 0) {
                return null;
            }
            return value;
        });
    }

    private static class MemLockWrapper implements Lock {
        private final String key;
        private final AtomicInteger refCount = new AtomicInteger(1);
        private final ReentrantLock target;
        private final FlowLocksMemo locksMemo;

        private MemLockWrapper(String key, ReentrantLock target, FlowLocksMemo locksMemo) {
            this.key = key;
            this.target = target;
            this.locksMemo = locksMemo;
        }

        @Override
        public void lock() {
            this.target.lock();
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            this.target.lockInterruptibly();
        }

        @Override
        public boolean tryLock() {
            return this.target.tryLock();
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return this.target.tryLock(time, unit);
        }

        @Override
        public void unlock() {
            this.target.unlock();
            this.refCount.decrementAndGet();
            this.locksMemo.tryCleanLocalLock(this.key);
        }

        @Override
        public Condition newCondition() {
            return this.target.newCondition();
        }

        private void addRef() {
            this.refCount.incrementAndGet();
        }

        private int getRefCount() {
            return this.refCount.get();
        }
    }
}
