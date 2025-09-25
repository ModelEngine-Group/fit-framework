/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.tansfer;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.tool.model.transfer.ToolData;
import modelengine.fel.tool.model.transfer.ToolGroupData;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示添加工具组数据的单元测试。
 *
 * @author 王攀博
 * @since 2024-10-28
 */
public class ToolGroupDataTest {
    private static final String DEFINITION_GROUP_NAME = "defGroup.weather.Rain";

    private ToolData data;
    private ToolGroupData groupData;

    @BeforeEach
    void setUp() {
        this.data = this.buildTool();
        this.groupData = new ToolGroupData();
        this.groupData.setTools(Collections.singletonList(this.data));
        this.groupData.setDefGroupName(DEFINITION_GROUP_NAME);
        this.groupData.setName("test_group_name");
    }

    @AfterEach
    void tearDown() {
    }

    private ToolData buildTool() {
        ToolData toolData = new ToolData();
        Map<String, Object> schema = new HashMap<>();
        schema.put("test_key", "test_value");
        toolData.setSchema(schema);
        toolData.setName("test_tool_name");
        toolData.setDefName("test_definition_name");
        toolData.setUniqueName("test_tool_name");
        toolData.setDescription("test des.");
        return toolData;
    }

    @Test
    @DisplayName("测试定义组的数据结构")
    void shouldReturnRightValueWhenBuildDefinition() {
        String groupName = this.groupData.getDefGroupName();
        List<ToolData> definitionDataList = this.groupData.getTools();

        assertThat(groupName).isEqualTo(DEFINITION_GROUP_NAME);
        assertThat(definitionDataList.size()).isEqualTo(1);
        assertThat(definitionDataList.get(0)).isEqualTo(this.data);
        assertThat(this.data.getName()).isEqualTo("test_tool_name");
        assertThat(this.data.getDefName()).isEqualTo("test_definition_name");
        assertThat(this.data.getUniqueName()).isEqualTo("test_tool_name");
        assertThat(this.data.getDescription()).isEqualTo("test des.");
        assertThat(this.data.getSchema().get("test_key")).isEqualTo("test_value");
    }
}
