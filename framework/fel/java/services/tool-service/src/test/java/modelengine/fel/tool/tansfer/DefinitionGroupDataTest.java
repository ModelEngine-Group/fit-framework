/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.tansfer;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.tool.model.transfer.DefinitionData;
import modelengine.fel.tool.model.transfer.DefinitionGroupData;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示添加定义组数据的单元测试。
 *
 * @since 2024-10-28
 */
public class DefinitionGroupDataTest {
    private static final String DEFINITION_GROUP_NAME = "defGroup.weather.Rain";

    private DefinitionData data;
    private DefinitionGroupData groupData;

    @BeforeEach
    void setUp() {
        this.data = this.buildDefinition();
        this.groupData = new DefinitionGroupData();
        this.groupData.setDefinitions(Collections.singletonList(this.data));
        this.groupData.setName(DEFINITION_GROUP_NAME);
    }

    @AfterEach
    void tearDown() {
    }

    private DefinitionData buildDefinition() {
        DefinitionData definitionData = new DefinitionData();
        Map<String, Object> schema = new HashMap<>();
        schema.put("test_key", "test_value");
        definitionData.setSchema(schema);
        definitionData.setName("test_definition_name");
        definitionData.setGroupName(DEFINITION_GROUP_NAME);
        return definitionData;
    }

    @Test
    @DisplayName("测试定义组的数据结构")
    void shouldReturnRightValueWhenBuildDefinition() {
        String groupName = this.groupData.getName();
        List<DefinitionData> definitionDataList = this.groupData.getDefinitions();

        assertThat(groupName).isEqualTo(DEFINITION_GROUP_NAME);
        assertThat(definitionDataList.size()).isEqualTo(1);
        assertThat(definitionDataList.get(0)).isEqualTo(this.data);

        assertThat(this.data.getName()).isEqualTo("test_definition_name");
        assertThat(this.data.getGroupName()).isEqualTo(DEFINITION_GROUP_NAME);
        assertThat(this.data.getSchema().get("test_key")).isEqualTo("test_value");
    }
}
