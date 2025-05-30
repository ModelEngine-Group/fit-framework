/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.flow;

import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMessenger;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowlock.FlowLocks;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;
import modelengine.fit.waterflow.domain.stream.nodes.From;

/**
 * 处理数据Flow
 * 用于先定义流程，再不停传入不同数据驱动stream往下走
 *
 * @param <D> 初始传入数据类型
 * @since 1.0
 */
public class ProcessFlow<D> extends Flow<D> implements EmitterListener<D, FlowSession>, Emitter<Object, FlowSession> {
    /**
     * 流从起始节点开始
     *
     * @param repo 上下文持久化
     * @param messenger 上下文发送器
     * @param locks 流程锁
     */
    public ProcessFlow(FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
        this.start = new From<>(repo, messenger, locks);
    }

    @Override
    public void handle(D data, FlowSession session) {
        this.offer(data, session == null ? new FlowSession() : session);
    }

    @Override
    public void register(EmitterListener<Object, FlowSession> handler) {
        this.end.register(handler);
    }

    public void unregister(EmitterListener<Object, FlowSession> handler) {
        if (handler != null) {
            this.end.unregister(handler);
        }
    }

    @Override
    public void emit(Object data, FlowSession token) {
        this.end.emit(data, token);
    }

    @Override
    public void complete() {
        this.defaultSession.getWindow().complete();
        this.defaultSession = new FlowSession();
        this.defaultSession.begin();
    }
}
