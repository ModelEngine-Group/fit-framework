/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolFactory;
import modelengine.fel.tool.ToolFactoryRepository;
import modelengine.fel.tool.ToolInfoEntity;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fel.tool.service.ToolRepository;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 表示 {@link ToolExecuteService} 的默认实现。
 *
 * @since 2024-08-15
 */
@Component
public class DefaultToolExecutor implements ToolExecuteService {
    private final ToolRepository toolRepository;
    private final ToolFactoryRepository toolFactoryRepository;
    private final ObjectSerializer serializer;

    /**
     * 构造一个 {@link DefaultToolExecutor} 的实例。
     *
     * @param toolRepository 表示工具仓库的 {@link ToolRepository}。
     * @param toolFactoryRepository 表示工具工厂仓库的 {@link ToolFactoryRepository}。
     * @param serializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @throws IllegalArgumentException 当 {@code toolRepository}、{@code toolFactoryRepository} 为 {@code null} 时。
     */
    public DefaultToolExecutor(ToolRepository toolRepository, ToolFactoryRepository toolFactoryRepository,
            @Fit(alias = "json") ObjectSerializer serializer) {
        this.toolRepository = notNull(toolRepository, "The tool repository cannot be null.");
        this.toolFactoryRepository = notNull(toolFactoryRepository, "The tool factory repository cannot be null.");
        this.serializer = notNull(serializer, "The serializer cannot be null.");
    }

    @Override
    @Fitable(id = "standard")
    public String execute(String group, String toolName, String jsonArgs) {
        Tool tool = this.getTool(group, toolName);
        Object output = tool.executeWithJson(jsonArgs);
        return this.convertOutput(group, tool.metadata().returnConverter(), output);
    }

    @Override
    @Fitable(id = "standard")
    public String execute(String group, String toolName, Map<String, Object> jsonObject) {
        Tool tool = this.getTool(group, toolName);
        Object output = tool.executeWithJsonObject(jsonObject);
        return this.convertOutput(group, tool.metadata().returnConverter(), output);
    }

    @Override
    @Fitable(id = "standard")
    public String execute(String uniqueName, String jsonArgs) {
        String[] strings = ToolInfo.parseIdentifier(uniqueName);
        return this.execute(strings[0], strings[1], jsonArgs);
    }

    @Override
    @Fitable(id = "standard")
    public String execute(String uniqueName, Map<String, Object> jsonObject) {
        String[] strings = ToolInfo.parseIdentifier(uniqueName);
        return this.execute(strings[0], strings[1], jsonObject);
    }

    private Tool getTool(String group, String toolName) {
        ToolInfoEntity tool = notNull(toolRepository.getTool(group, toolName),
                () -> new IllegalStateException(StringUtils.format("The tool cannot be found. [group={0}, tool={1}]",
                        group,
                        toolName)));
        Set<String> runnables = tool.runnables().keySet();
        Optional<ToolFactory> factory = this.toolFactoryRepository.match(runnables);
        if (factory.isEmpty()) {
            throw new IllegalStateException(StringUtils.format("No tool factory to create tool. [runnables={0}]",
                    runnables));
        }
        Tool.Metadata metadata = Tool.Metadata.fromSchema(group, tool.schema());
        return factory.get().create(tool, metadata);
    }

    private String convertOutput(String group, String convertor, Object output) {
        if (StringUtils.isBlank(convertor)) {
            return serializer.serialize(output);
        }
        Tool convertorTool = this.getTool(group, convertor);
        return convertorTool.execute(output).toString();
    }
}