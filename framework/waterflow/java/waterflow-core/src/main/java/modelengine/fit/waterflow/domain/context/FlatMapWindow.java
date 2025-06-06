/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import lombok.Getter;
import modelengine.fit.waterflow.domain.stream.nodes.To;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;

import java.util.UUID;

/**
 * Flatmap window
 * 对应flatmap一条数据flatmap后的流window
 * 一个session window到达flatmap节点将生成多个flatmap window：一条source data一个
 * 一个session window叨叨flatmap对应一个flatmap source window
 * flatmap source window 得到flatmap window的数据并进行对应的保序操作
 *
 * @author 宋永坦
 * @since 1.0
 */
public class FlatMapWindow extends Window {
    /**
     * source是flatmap中emitter的window
     * from是对应的flatmap节点的整个window
     * 注意三个window的关系：source，from，this
     */
    @Getter
    private Window source;

    public FlatMapWindow(FlatMapSourceWindow from) {
        super();
        this.from = from;
        from.addFlatMapWindow(this);
    }

    /**
     * 为保序生成序号
     * flatmap window的序号要与其他同session的flatmap window共同保序
     * 所以该序号生成委托给对应的flatmap source window
     */
    @Override
    public void generateIndex(FlowContext context, Publisher publisher) {
        ((FlatMapSourceWindow) from).generateIndex(context, this);
    }

    @Override
    public UUID key() {
        return this.from.key();
    }

    /**
     * 同session的window token转移与generate index类似，
     * 确保下一个节点得到是统一的flatmap source window token
     *
     * @return 选中一个空闲的 {@link WindowToken}
     */
    @Override
    public WindowToken peekAndConsume() {
        return this.from.peekAndConsume();
    }

    @Override
    public boolean isDone() {
        return this.from.isDone();
    }

    @Override
    public boolean isComplete() {
        return this.from.isComplete();
    }

    @Override
    public Integer tokenCount() {
        return this.from.tokenCount();
    }

    @Override
    public String debugTokens() {
        return this.from.debugTokens();
    }

    @Override
    public boolean accept() {
        return this.from.accept();
    }

    /**
     * 完成自己的window后通知flatmap source window，尝试完成该window
     */
    @Override
    public void complete() {
        super.complete();
        this.from.complete();
    }

    /**
     * 同session的window 侦听也转移到flatmap source window
     *
     * @param to 下一个节点window
     */
    @Override
    public void addTo(Window to) {
        this.from.addTo(to);
    }

    @Override
    public Object acc() {
        return this.from.acc();
    }

    @Override
    public void setAcc(Object acc) {
        this.from.setAcc(acc);
    }

    @Override
    public <T, R> void setCompleteHook(To<T, R> to, FlowContext context) {
        this.from.setCompleteHook(to, context);
    }

    /**
     * set source window.
     *
     * @param source the source window of {@link Window}.
     */
    public void setSource(Window source) {
        this.source = source;
        source.onDone(this.id(), this::complete);
    }
}
