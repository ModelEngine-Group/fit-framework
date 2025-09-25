/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool;

/**
 * 表示工具元数据中的键。
 *
 * @author 刘信宏
 * @since 2024-06-26
 */
public interface ToolSchema {
    /**
     * 表示工具参数。
     */
    String PARAMETERS = "parameters";

    /**
     * 表示工具参数的属性列表。
     */
    String PARAMETERS_PROPERTIES = "properties";

    /**
     * 表示必填参数的列表。
     */
    String PARAMETERS_REQUIRED = "required";

    /**
     * 表示参数顺序。
     */
    String PARAMETERS_ORDER = "order";

    /**
     * 表示参数的默认值。
     */
    String DEFAULT_PARAMETER = "default";

    /**
     * 表示参数的拓展属性。
     */
    String PARAMETERS_EXTENSIONS = "parameterExtensions";

    /**
     * 表示配置输入的参数列表。
     */
    String CONFIG_PARAMETERS = "config";

    /**
     * 表示工具的返回值格式。
     */
    String RETURN_SCHEMA = "return";

    /**
     * 表示工参数类型的描述。
     */
    String PROPERTIES_TYPE = "type";

    /**
     * 表示返回值的转换器。
     */
    String RETURN_CONVERTER = "converter";

    /**
     * 表示工具的命名空间。
     */
    String NAME_SPACE = "namespace";

    /**
     * 表示工具描述的键。
     */
    String SCHEMA = "schema";

    /**
     * 表示工具的运行规范描述。
     */
    String RUNNABLE = "runnables";

    /**
     * 表示工具的扩展属性。
     */
    String EXTENSIONS = "extensions";

    /**
     * 表示工具或定义名字。
     */
    String NAME = "name";

    /**
     * 表示工具描述。
     */
    String DESCRIPTION = "description";

    /**
     * 表示工具额外参数的属性列表。
     */
    String EXTRA_PARAMETERS = "extraParameters";

    /**
     * 表示参数的默认值。
     */
    String PARAMETER_DEFAULT_VALUE = "default";

    /**
     * 表示插件对外提供的工具名单文件。
     */
    String TOOL_MANIFEST = "tools.json";
}
