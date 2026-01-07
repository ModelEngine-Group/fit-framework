/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.callbacks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.stream.reactive.Subscription;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据向下一个节点发送前触发回调的入参
 *
 * @author 杨祥宇
 * @since 2024/10/21
 */
@Getter
@Setter
@AllArgsConstructor
public class PreSendCallbackInfo<I> {
    /**
     * 命中的context列表
     */
    private Map<Subscription<I>, List<FlowContext<I>>> matchedContexts;

    /**
     * 未命中的context列表
     */

    private List<FlowContext<I>> unMatchedContexts;

    /**
     * 获取命中和未命中的所有context列表
     *
     * @return context列表
     */
    public List<FlowContext<I>> getAllContext() {
        List<FlowContext<I>> mergeContexts = this.getMatchedContexts().values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        mergeContexts.addAll(this.getUnMatchedContexts());
        return mergeContexts;
    }
}
