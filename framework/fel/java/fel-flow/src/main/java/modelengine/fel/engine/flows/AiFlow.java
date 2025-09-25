/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fit.waterflow.domain.stream.reactive.Subscriber;
import modelengine.fit.waterflow.domain.utils.IdGenerator;
import modelengine.fitframework.inspection.Validation;

/**
 * AI 流程，定义流程的结构信息。
 *
 * @param <D> 表示流程注入的数据类型。
 * @param <F> 表示被装饰的流程类型，是 {@link Flow}{@code <}{@link D}{@code >} 的扩展。
 * @author 刘信宏
 * @since 2024-04-28
 */
public class AiFlow<D, F extends Flow<D>> extends IdGenerator {
    private final F flow;

    /**
     * 创建一个 AI 流程。
     *
     * @param flow 流程。
     */
    public AiFlow(F flow) {
        this.flow = Validation.notNull(flow, "Flow cannot be null.");
    }

    @Override
    public String getId() {
        return this.flow.getId();
    }

    @Override
    public void setId(String id) {
        Validation.notBlank(id, "Node id cannot be blank.");
        this.flow.setId(id);
    }

    /**
     * 获取流程的结束节点。
     *
     * @return 表示流程结束节点的 {@link Subscriber}{@code <?, ?>}。
     */
    public Subscriber<?, ?> end() {
        return this.flow.end();
    }

    /**
     * 获取流程的开始节点。
     *
     * @return 表示流程开始节点的 {@link Publisher}{@code <}{@link D}{@code >}。
     */
    public Publisher<D> start() {
        return this.flow.start();
    }

    /**
     * 获取被装饰的流程对象。
     *
     * @return 表示被装饰流程对象的 {@link F}。
     */
    public F origin() {
        return this.flow;
    }
}
