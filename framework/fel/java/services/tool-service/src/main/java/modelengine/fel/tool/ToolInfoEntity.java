/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool;

import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.info.entity.ToolEntity;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.util.StringUtils;

import java.util.Map;

/**
 * 表示工具的实体类。
 *
 * @since 2024-08-14
 */
public class ToolInfoEntity implements Tool.Info {
    private final ToolEntity toolEntity;

    /**
     * 创建 {@link ToolEntity} 的实例。
     *
     * @param toolEntity 表示工具实体类的 {@link ToolEntity}。
     */
    public ToolInfoEntity(ToolEntity toolEntity) {
        this.toolEntity = toolEntity;
    }

    @Nonnull
    @Override
    public String namespace() {
        return this.toolEntity.getNamespace();
    }

    @Nonnull
    @Override
    public String name() {
        return this.toolEntity.getSchema().getName();
    }

    @Nonnull
    @Override
    public String description() {
        return this.toolEntity.getSchema().getDescription();
    }

    @Nonnull
    @Override
    public Map<String, Object> parameters() {
        return this.toolEntity.parameters();
    }

    @Nonnull
    @Override
    public Map<String, Object> extensions() {
        return this.toolEntity.getExtensions();
    }

    @Override
    public String uniqueName() {
        return ToolInfo.identify(this.namespace(), this.name());
    }

    @Override
    public String groupName() {
        return StringUtils.EMPTY;
    }

    @Override
    public String definitionName() {
        return StringUtils.EMPTY;
    }

    @Override
    public String definitionGroupName() {
        return StringUtils.EMPTY;
    }

    @Override
    public Map<String, Object> schema() {
        return this.toolEntity.schema();
    }

    @Override
    public Map<String, Object> runnables() {
        return this.toolEntity.getRunnables();
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public boolean isLatest() {
        return true;
    }

    @Override
    public String returnConverter() {
        return StringUtils.EMPTY;
    }

    @Override
    public Map<String, Object> defaultParameterValues() {
        return Map.of();
    }
}