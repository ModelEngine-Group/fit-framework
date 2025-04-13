/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.broker.support;

import modelengine.fitframework.broker.Fitable;
import modelengine.fitframework.broker.GenericableExecutor;
import modelengine.fitframework.broker.InvocationContext;
import modelengine.fitframework.log.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 表示 {@link GenericableExecutor} 的多播调用实现。
 *
 * @author 季聿阶
 * @since 2023-03-28
 */
public class MulticastGenericableExecutor implements GenericableExecutor {
    private static final Logger log = Logger.get(MulticastGenericableExecutor.class);

    private final GenericableExecutor executor;

    MulticastGenericableExecutor(GenericableExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Object execute(List<Fitable> fitables, InvocationContext context, Object[] args) {
        return fitables.stream().map(fitable -> {
            try {
                return this.executor.execute(Collections.singletonList(fitable), context, args);
            } catch (Throwable t) {
                log.warn("Failed to execute genericable executor while multicast, return null instead. [id={}]",
                        fitable.toUniqueId());
                return null;
            }
        }).filter(Objects::nonNull).reduce(context.accumulator()).orElse(null);
    }
}
