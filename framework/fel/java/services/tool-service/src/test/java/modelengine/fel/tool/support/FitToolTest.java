/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fel.tool.ToolInfoEntity;
import modelengine.fel.tool.Tool;
import modelengine.fel.tool.info.entity.ToolEntity;
import modelengine.fel.tool.ToolFactory;
import modelengine.fel.tool.ToolSchema;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.IoUtils;
import modelengine.fitframework.util.TypeUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 表示 {@link FitTool} 的测试集。
 *
 * @author 易文渊
 * @author 杭潇
 * @since 2024-08-16
 */
@DisplayName("测试 FitTool")
public class FitToolTest {
    private Tool tool;

    @BeforeEach
    void setUp() throws IOException {
        BrokerClient client = mock(BrokerClient.class, RETURNS_DEEP_STUBS);
        when(client.getRouter(eq("test")).route(any()).communicationType(any()).invoke(any(Object[].class))).thenAnswer(
                invocation -> {
                    if (Objects.equals(invocation.getArgument(0), "1")) {
                        return "OK";
                    }
                    throw new IllegalStateException("Error");
                });
        ObjectSerializer serializer = new JacksonObjectSerializer(null, null, null);
        List<ToolEntity> toolEntities =
                serializer.<Map<String, List<ToolEntity>>>deserialize(IoUtils.content(this.getClass().getClassLoader(),
                        ToolSchema.TOOL_MANIFEST), TypeUtils.parameterized(Map.class, new Type[] {
                        String.class, TypeUtils.parameterized(List.class, new Type[] {ToolEntity.class})
                })).get("tools");
        ToolEntity testEntity = toolEntities.get(0);
        ToolFactory toolFactory = new FitToolFactory(client, serializer);
        ToolInfoEntity toolInfoEntity = new ToolInfoEntity(testEntity);
        this.tool = toolFactory.create(toolInfoEntity, Tool.Metadata.fromSchema("Common", testEntity.schema()));
    }

    @Test
    @DisplayName("FIT 工具调用成功")
    void shouldReturnOk() {
        assertThat(this.tool.executeWithJson("{\"orderId\": \"1\"}")).isEqualTo("OK");
    }

    @Test
    @DisplayName("FIT 工具调用失败")
    void shouldThrowException() {
        assertThatThrownBy(() -> this.tool.execute("{\"config\": \"2\"}")).isInstanceOf(IllegalStateException.class)
                .hasMessage("Error");
    }
}