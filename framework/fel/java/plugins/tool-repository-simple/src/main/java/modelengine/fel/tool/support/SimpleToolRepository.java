/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.support;

import static modelengine.fitframework.inspection.Validation.notBlank;

import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.ToolInfoEntity;
import modelengine.fel.tool.service.ToolRepository;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 表示 {@link ToolRepository} 的简单实现。
 *
 * @author 易文渊
 * @author 杭潇
 * @since 2024-08-15
 */
@Component
public class SimpleToolRepository implements ToolRepository {
    private static final Logger log = Logger.get(SimpleToolRepository.class);

    private final Map<String, ToolInfoEntity> toolCache = new ConcurrentHashMap<>();

    @Override
    public void addTool(ToolInfoEntity tool) {
        if (tool == null) {
            return;
        }
        String uniqueName = ToolInfo.identify(tool);
        toolCache.put(uniqueName, tool);
        log.info("Register tool[uniqueName={}] success.", uniqueName);
    }

    @Override
    public void deleteTool(String namespace, String toolName) {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(toolName)) {
            return;
        }
        String uniqueName = ToolInfo.identify(namespace, toolName);
        toolCache.remove(uniqueName);
        log.info("Unregister tool[uniqueName={}] success.", uniqueName);
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