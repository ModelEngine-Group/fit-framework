/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import modelengine.fit.waterflow.domain.common.Constants;

/**
 * 结束context
 * 在session window complete的时候发出一个结束context，通知reduce节点结束累积操作
 *
 * @author 宋永坦
 * @since 1.0
 */
public class CompleteContext extends FlowContext {
    /**
     * 构造一个 {@link CompleteContext} 实例。
     * <p>
     * 该构造函数用于在 session window complete 时创建一个结束上下文，通知 reduce 节点结束累积操作。
     * </p>
     *
     * @param context 表示当前上下文的 {@link FlowContext}。
     * @param position 表示上下文当前所处位置的 {@link String}。
     */
    public CompleteContext(FlowContext context, String position) {
        super(context.getStreamId(), context.getRootId(), null, context.getTraceId(), position,
                context.getParallel(), context.getParallelMode(), context.getSession());
        this.batchId = context.getBatchId();
        this.setIndex(Constants.NOT_PRESERVED_INDEX);
    }
}
