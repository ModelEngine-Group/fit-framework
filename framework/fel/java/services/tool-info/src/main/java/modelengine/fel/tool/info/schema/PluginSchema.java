/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.schema;

/**
 * 表示插件的字段集合。
 *
 * @author 李金绪
 * @since 2024-10-29
 */
public interface PluginSchema {
    /**
     * 表示插件的校验和字段。
     */
    String CHECKSUM = "checksum";

    /**
     * 表示插件的名称字段。
     */
    String PLUGIN_NAME = "name";

    /**
     * 表示插件的描述字段。
     */
    String DESCRIPTION = "description";

    /**
     * 表示插件的类型字段。
     */
    String TYPE = "type";

    /**
     * 表示插件类型为 JAVA。
     */
    String JAVA = "java";

    /**
     * 表示插件类型为 PYTHON。
     */
    String PYTHON = "python";

    /**
     * 表示 JAVA 插件的坐标信息。
     */
    String GROUP_ID = "groupId";

    /**
     * 表示 JAVA 插件的坐标信息。
     */
    String ARTIFACT_ID = "artifactId";

    /**
     * 表示 PYTHON 插件的名称。
     */
    String PYTHON_NAME = "name";

    /**
     * 表示插件的全名字段。
     */
    String PLUGIN_FULL_NAME = "pluginFullName";

    /**
     * 表示插件的 JSON 文件名。
     */
    String PLUGIN_JSON = "plugin.json";

    /**
     * 表示插件.
     */
    String PLUGINS = "plugins";

    /**
     * 表示插件的临时目录。
     */
    String TEMP_DIR = "tempDir";

    /**
     * 表示插件的 JAR 文件后缀名。
     */
    String JAR = ".jar";

    /**
     * 表示 HTTP 类型的插件。
     */
    String HTTP = "HTTP";

    /**
     * 表示插件的语言类型字段。
     */
    String LANGUAGE = "language";

    /**
     * 表示插件的唯一性字段。
     */
    String UNIQUENESS = "uniqueness";

    /**
     * 表示分隔符。
     */
    char DOT = '.';
}
