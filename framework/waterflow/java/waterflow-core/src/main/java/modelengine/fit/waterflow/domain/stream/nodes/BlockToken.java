/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.nodes;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fit.waterflow.domain.utils.IdGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 区别block，专门在stream代码中供block关键字调用
 *
 * @author huizi
 * @since 1.0
 */
public abstract class BlockToken<T> extends IdGenerator {
    /**
     * 需要中断的目标节点
     */
    private Publisher<T> publisher;

    private List<FlowContext<T>> data = new ArrayList<>();

    /**
     * 外部触发执行目标节点中某一个流程实例进行数据处理
     */
    public synchronized void resume() {
        List<FlowContext<T>> verified = new ArrayList<>();
        Iterator<FlowContext<T>> iterator = data.iterator();
        while (iterator.hasNext()) {
            FlowContext<T> next = iterator.next();
            if (this.verify(next.getData())) {
                verified.add(next);
                iterator.remove();
            }
        }
        List<FlowContext<T>> cloned = verified.stream()
                .map(context -> context.generate(context.getData(), context.getPosition(), context.getCreateAt())
                        .batchId(context.getBatchId()))
                .collect(Collectors.toList());
        this.publisher.getFlowContextRepo().save(cloned);
        cloned.stream()
                .collect(Collectors.groupingBy(item -> item.getSession().getId(), LinkedHashMap::new,
                        Collectors.toList()))
                .values()
                .forEach(contexts -> {
                    this.publisher.offer(contexts, __ -> {});
                });
    }

    /**
     * 校验是否需要block
     *
     * @param data 当前需判定是否block的数据
     * @return 是否block
     */
    public abstract boolean verify(T data);

    /**
     * 设置
     *
     * @param data block的data
     */
    public void setHost(FlowContext<T> data) {
        this.data.add(data);
    }

    /**
     * 被block的data
     *
     * @return data
     */
    public List<FlowContext<T>> data() {
        return this.data;
    }

    /**
     * 设置publisher
     *
     * @param publisher publisher
     */
    public void setPublisher(Publisher<T> publisher) {
        this.publisher = publisher;
    }
}
