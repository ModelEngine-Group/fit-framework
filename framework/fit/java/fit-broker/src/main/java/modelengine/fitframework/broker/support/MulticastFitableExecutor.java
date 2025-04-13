/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.broker.support;

import modelengine.fitframework.broker.Fitable;
import modelengine.fitframework.broker.FitableExecutor;
import modelengine.fitframework.broker.InvocationContext;
import modelengine.fitframework.broker.LocalExecutor;
import modelengine.fitframework.broker.LocalExecutorFactory;
import modelengine.fitframework.broker.Target;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.LazyLoader;
import modelengine.fitframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 表示 {@link FitableExecutor} 的多播调用实现。
 *
 * @author 季聿阶
 * @since 2023-03-28
 */
public class MulticastFitableExecutor implements FitableExecutor {
    private static final Logger log = Logger.get(MulticastFitableExecutor.class);

    private final BeanContainer container;
    private final FitableExecutor remoteFitableExecutor;
    private final LazyLoader<LocalExecutorFactory> localExecutorFactoryLoader;

    MulticastFitableExecutor(BeanContainer container, FitableExecutor remoteFitableExecutor) {
        this.container = container;
        this.remoteFitableExecutor = remoteFitableExecutor;
        this.localExecutorFactoryLoader = new LazyLoader<>(() -> this.container.factory(LocalExecutorFactory.class)
                .map(BeanFactory::<LocalExecutorFactory>get)
                .orElseThrow(() -> new IllegalStateException("No LocalExecutorFactory.")));
    }

    @Override
    public Object execute(Fitable fitable, List<Target> targets, InvocationContext context, Object[] args) {
        return targets.stream().map(target -> {
            try {
                return this.execute(fitable, target, context, args);
            } catch (Throwable e) {
                log.warn("Failed to execute fitable executor while multicast, return null instead. [id={}]",
                        fitable.toUniqueId());
                return null;
            }
        }).filter(Objects::nonNull).reduce(context.accumulator()).orElse(null);
    }

    private Object execute(Fitable fitable, Target target, InvocationContext context, Object[] args) {
        if (this.isLocal(target, context)) {
            LocalExecutor localExecutor = this.localExecutorFactoryLoader.get()
                    .get(fitable.toUniqueId())
                    .orElseThrow(() -> new IllegalStateException(StringUtils.format(
                            "No local fitable executor. [id={0}]",
                            fitable.toUniqueId())));
            return localExecutor.execute(args);
        }
        return this.remoteFitableExecutor.execute(fitable, Collections.singletonList(target), context, args);
    }

    private boolean isLocal(Target target, InvocationContext context) {
        return Objects.equals(target.workerId(), context.localWorkerId());
    }
}
