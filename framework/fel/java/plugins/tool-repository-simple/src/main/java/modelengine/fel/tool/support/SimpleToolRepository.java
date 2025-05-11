/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.support;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.ToolInfoEntity;
import modelengine.fel.tool.service.ToolChangedObserver;
import modelengine.fel.tool.service.ToolRepository;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A simple implementation of the {@link ToolRepository} interface.
 *
 * @author 易文渊
 * @author 杭潇
 * @since 2024-08-15
 */
@Component
public class SimpleToolRepository implements ToolRepository {
    private static final Logger log = Logger.get(SimpleToolRepository.class);

    private final ToolChangedObserver toolChangedObserver;
    private final Map<String, ToolInfoEntity> toolCache = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance of the SimpleToolRepository class.
     *
     * @param toolChangedObserver The observer to be notified when tools are added or removed, as a
     * {@link ToolChangedObserver}.
     * @throws IllegalStateException If {@code toolChangedObserver} is null.
     */
    public SimpleToolRepository(ToolChangedObserver toolChangedObserver) {
        this.toolChangedObserver = notNull(toolChangedObserver, "The tool changed observer cannot be null.");
    }

    @Override
    public void addTool(ToolInfoEntity tool) {
        if (tool == null) {
            return;
        }
        String uniqueName = ToolInfo.identify(tool);
        this.toolCache.put(uniqueName, tool);
        log.info("Register tool[uniqueName={}] success.", uniqueName);
        this.toolChangedObserver.onToolAdded(uniqueName, tool.description(), tool.schema());
    }

    @Override
    public void deleteTool(String namespace, String toolName) {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(toolName)) {
            return;
        }
        String uniqueName = ToolInfo.identify(namespace, toolName);
        this.toolCache.remove(uniqueName);
        log.info("Unregister tool[uniqueName={}] success.", uniqueName);
        this.toolChangedObserver.onToolRemoved(uniqueName);
    }

    @Override
    public ToolInfoEntity getTool(String namespace, String toolName) {
        notBlank(namespace, "The namespace cannot be blank.");
        notBlank(toolName, "The toll name cannot be blank.");
        String uniqueName = modelengine.fel.core.tool.ToolInfo.identify(namespace, toolName);
        return toolCache.get(uniqueName);
    }

    @Override
    public List<ToolInfoEntity> listTool(String namespace) {
        notBlank(namespace, "The namespace cannot be blank.");
        return toolCache.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(namespace))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}