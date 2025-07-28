/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.support;

import modelengine.fel.tool.ToolFactory;
import modelengine.fel.tool.ToolFactoryRepository;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表示 {@link ToolFactoryRepository} 的默认实现。
 *
 * @since 2024-08-15
 */
@Component
public class DefaultToolFactoryRepository implements ToolFactoryRepository {
    private static final Logger log = Logger.get(DefaultToolFactoryRepository.class);

    private final Map<String, ToolFactory> factoryCache = new ConcurrentHashMap<>();

    @Override
    public void register(ToolFactory factory) {
        if (factory == null) {
            return;
        }
        this.factoryCache.put(StringUtils.toUpperCase(factory.type()), factory);
        log.info("Register factory[type={}] success.", factory.type());
    }

    @Override
    public void unregister(ToolFactory factory) {
        if (factory == null) {
            return;
        }
        this.factoryCache.remove(StringUtils.toUpperCase(factory.type()));
        log.info("Unregister factory[type={}] success.", factory.type());
    }

    @Override
    public Optional<ToolFactory> match(Set<String> runnables) {
        return runnables.stream()
                .map(StringUtils::toUpperCase)
                .filter(this.factoryCache::containsKey)
                .map(this.factoryCache::get)
                .findFirst();
    }
}