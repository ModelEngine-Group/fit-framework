/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.chat;

import modelengine.fel.core.tool.ToolInfo;

import java.util.Map;

/**
 * 模型所使用的工具，用于 OpenAI 请求中 tools 字段。
 *
 * @author 易文渊
 * @author 张庭怿
 * @since 2024-4-30
 */
public class OpenAiTool {
    private final String type = "function";
    private final Function function;

    private OpenAiTool(String name, String description, Map<String, Object> parameters) {
        this.function = new Function(name, description, parameters);
    }

    /**
     * 使用 {@link ToolInfo} 构造一个新的 {@link OpenAiTool}。
     *
     * @param toolInfo 表示工具信息的 {@link ToolInfo}。
     * @return 表示 openai 工具的 {@link OpenAiTool} 。
     */
    public static OpenAiTool from(ToolInfo toolInfo) {
        return new OpenAiTool(toolInfo.name(), toolInfo.description(), toolInfo.parameters());
    }

    /**
     * 工具所使用的方法，用于序列化为 JSON 中的 function 字段。
     */
    private static class Function {
        private final String name;
        private final String description;
        private final Map<String, Object> parameters;

        private Function(String name, String description, Map<String, Object> parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }
    }
}
