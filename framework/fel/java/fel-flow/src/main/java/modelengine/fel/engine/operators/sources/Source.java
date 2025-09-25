/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.sources;

import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 数据发射源实现，支持注册多个监听器。仅支持主动逐个发射数据。
 *
 * @param <T> 表示数据源的业务数据类型。
 * @author 刘信宏
 * @since 2024-04-28
 */
public class Source<T> implements Emitter<T, FlowSession> {
    private static final Integer MAXIMUM_POOL_SIZE = 50;
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, MAXIMUM_POOL_SIZE,
            60L, TimeUnit.SECONDS, new SynchronousQueue<>());

    private final List<EmitterListener<T, FlowSession>> listeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * 注销监听器。
     *
     * @param listener 监听器。
     */
    public void unregister(EmitterListener<T, FlowSession> listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public void register(EmitterListener<T, FlowSession> listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void emit(T data, FlowSession session) {
        this.listeners.forEach(handler -> THREAD_POOL.execute(() -> handler.handle(data, session)));
    }
}
