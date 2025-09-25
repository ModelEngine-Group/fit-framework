/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import modelengine.fel.engine.activities.AiDataStart;
import modelengine.fel.engine.activities.AiStart;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.flow.Flows;
import modelengine.fit.waterflow.domain.flow.ProcessFlow;
import modelengine.fit.waterflow.domain.states.Start;

/**
 * AI 流程创建入口的 API 集合。
 *
 * @author 刘信宏
 * @since 2024-04-29
 */
public class AiFlows {
    /**
     * 建立流程的头结点。
     *
     * @param <D> 表示流程的输入数据类型的 {@link D}。
     * @return 表示流程头结点的 {@link AiStart}{@code <}{@link D}{@code , }{@link D}{@code , }{@link D}{@code ,
     * }{@link ProcessFlow}{@code <}{@link D}{@code >, }{@link AiProcessFlow}{@code <}{@link D}{@code , ?>>}。
     */
    public static <D> AiStart<D, D, D, ProcessFlow<D>, AiProcessFlow<D, ?>> create() {
        Start<D, D, D, ProcessFlow<D>> start = Flows.create();
        AiProcessFlow<D, ?> flow = new AiProcessFlow<>(start.getFlow());
        return new AiStart<>(start, flow);
    }

    /**
     * 通过指定的单条数据来构造一个数据前置流。
     *
     * @param data 表示单个数据的 {@link D}。
     * @param <D> 表示数据类型。
     * @return 表示数据前置流的 {@link AiDataStart}{@code <}{@link D}{@code , }{@link D}{@code , }{@link D}{@code >}。
     */
    public static <D> AiDataStart<D, D, D> mono(D data) {
        AiStart<D, D, D, ProcessFlow<D>, AiProcessFlow<D, ?>> start = AiFlows.create();
        return new AiDataStart<>(start, data);
    }

    /**
     * 通过指定的数据数组来构造一个数据前置流。
     *
     * @param data 表示数据数组的 {@link D}{@code []}。
     * @param <D> 表示数据类型。
     * @return 表示数据前置流的 {@link AiDataStart}{@code <}{@link D}{@code , }{@link D}{@code , }{@link D}{@code >}。
     */
    @SafeVarargs
    public static <D> AiDataStart<D, D, D> flux(D... data) {
        AiStart<D, D, D, ProcessFlow<D>, AiProcessFlow<D, ?>> start = AiFlows.create();
        return new AiDataStart<>(start, data);
    }

    /**
     * 通过指定的发射源来构造一个数据前置流。
     *
     * @param emitter 表示数据源的 {@link Emitter}{@code <}{@link D}{@code , }{@link FlowSession}{@code >}。
     * @param <D> 表示数据类型。
     * @return 表示数据前置流的 {@link AiDataStart}{@code <}{@link D}{@code , }{@link D}{@code , }{@link D}{@code >}。
     */
    public static <D> AiDataStart<D, D, D> source(Emitter<D, FlowSession> emitter) {
        AiStart<D, D, D, ProcessFlow<D>, AiProcessFlow<D, ?>> start = AiFlows.create();
        return new AiDataStart<>(start, emitter);
    }
}
