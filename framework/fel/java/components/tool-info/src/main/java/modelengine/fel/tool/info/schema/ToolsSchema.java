/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.schema;

/**
 * 表示工具的字段集合。
 *
 * @author 李金绪
 * @author 杭潇
 * @since 2024-10-29
 */
public interface ToolsSchema {
    /**
     * 工具的 JSON 文件名。
     */
    String TOOLS_JSON = "tools.json";

    /**
     * 工具的 JSON 文件中的 tools 字段名。
     */
    String TOOLS = "tools";

    /**
     * 工具的 JSON 文件中的 definitions 字段名。
     */
    String DEFINITIONS = "definitions";

    /**
     * 工具的 JSON 文件中的 toolGroups 字段名。
     */
    String TOOL_GROUPS = "toolGroups";

    /**
     * 工具的 JSON 文件中的 definitionGroups 字段名。
     */
    String DEFINITION_GROUPS = "definitionGroups";

    /**
     * 工具的 JSON 文件中的 definitionName 字段名。
     */
    String DEFINITION_NAME = "definitionName";

    /**
     * 工具的 JSON 文件中的 toolName 字段名。
     */
    String TOOL_NAME = "toolName";

    /**
     * 工具的 JSON 文件中的 toolGroupName 字段名。
     */
    String TOOL_GROUP_NAME = "toolGroupName";

    /**
     * 工具的 JSON 文件中的 definitionGroupName 字段名。
     */
    String DEFINITION_GROUP_NAME_IN_TOOL = "definitionGroupName";

    /**
     * 工具的 JSON 文件中的 definitionGroupName 字段名。
     */
    String DEFINITION_GROUP_NAME = "definitionGroups.name";

    /**
     * 工具的 JSON 文件中的 items 字段名。
     */
    String ITEMS = "items";

    /**
     * 工具的 JSON 文件中的 object 字段名。
     */
    String OBJECT = "object";

    /**
     * 工具的 JSON 文件中的 array 字段名。
     */
    String ARRAY = "array";

    /**
     * 工具的 JSON 文件中的 tags 字段名。
     */
    String TAGS = "tags";

    /**
     * 工具的 JSON 文件中的 tag 字段的最大长度。
     */
    int MAX_TAG_LENGTH = 64;

    /**
     * 工具的 JSON 文件中的 fit 字段的最大长度。
     */
    int MAX_FIT_TAG_LENGTH = 128;

    /**
     * 工具的 JSON 文件中的 fit 或 tag 字段的最小长度。
     */
    int MIN_FIT_LENGTH = 1;

    /**
     * 工具的 JSON 文件中的 fit 字段名。
     */
    String FIT = "FIT";

    /**
     * 工具的 JSON 文件中的 fitableId 字段名。
     */
    String FITABLE_ID = "fitableId";

    /**
     * 工具的 JSON 文件中的 genericableId 字段名。
     */
    String GENERICABLE_ID = "genericableId";

    /**
     * 工具的 JSON 文件中的 selectTools 字段名。
     */
    String SELECT_TOOLS = "selectTools";

    /**
     * 工具的 JSON 文件中的 builtIn 字段名。
     */
    String BUILT_IN = "BUILTIN";

    /**
     * 工具的 JSON 文件中的列表形式的展示。
     */
    String LIST_NOTATION = "[]";

    /**
     * 工具参数的属性。
     */
    String PROPERTIES = "properties";

    /**
     * 工具参数的必填项。
     */
    String REQUIRED = "required";

    /**
     * 工具的名称。
     */
    String NAME = "name";

    /**
     * 工具的参数信息。
     */
    String PARAMETERS = "parameters";

    /**
     * 工具参数的执行顺序。
     */
    String ORDER = "order";

    /**
     * 工具的返回信息。
     */
    String RETURN = "return";

    /**
     * 工具扩展参数的信息。
     */
    String PARAMETER_EXTENSIONS = "parameterExtensions";
}
