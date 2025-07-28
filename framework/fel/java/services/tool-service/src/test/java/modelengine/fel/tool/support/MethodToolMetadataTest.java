/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolSchema;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.annotation.Genericable;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.broker.client.Invoker;
import modelengine.fitframework.broker.client.Router;
import modelengine.fitframework.json.schema.JsonSchemaManager;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 表示 {@link Tool} 的单元测试。
 *
 * @since 2024-04-05
 */
@DisplayName("测试 FitMethodFunctionalTool")
public class MethodToolMetadataTest {
    private final ObjectSerializer serializer;
    private final Method testMethod;

    private Map<String, Object> toolSchema;
    private Tool tool;

    private Tool.Metadata toolMetadata;

    MethodToolMetadataTest() throws NoSuchMethodException {
        this.testMethod = TestInterface.class.getDeclaredMethod("testMethod", String.class);
        this.serializer = new JacksonObjectSerializer(null, null, null, true);
    }

    @BeforeEach
    void setup() {
        BrokerClient client = mock(BrokerClient.class);
        Router router = mock(Router.class);
        Invoker invoker = mock(Invoker.class);
        when(client.getRouter(eq("t1"), eq(this.testMethod))).thenReturn(router);
        when(router.route(any())).thenReturn(invoker);
        when(invoker.communicationType(any())).thenReturn(invoker);
        when(invoker.invoke(any())).thenAnswer(invocation -> {
            if (Objects.equals(invocation.getArgument(0), "1")) {
                return "OK";
            } else {
                throw new IllegalStateException("Error");
            }
        });
        this.toolMetadata = Tool.Metadata.fromMethod(this.testMethod);
        FitToolFactory fitToolFactory = new FitToolFactory(client, serializer);
        this.tool = fitToolFactory.create(this.buildInfo(), this.toolMetadata);
        this.toolSchema = this.buildSchema();
    }

    private Tool.Info buildInfo() {
        return Tool.Info.custom()
                .namespace("Common")
                .name("test_schema_default_implementation_name")
                .description("")
                .uniqueName("schema-uuid")
                .schema(this.buildSchema())
                .parameters(new HashMap<>())
                .runnables(MapBuilder.<String, Object>get()
                        .put("FIT",
                                MapBuilder.<String, Object>get().put("genericableId", this.getGenericableId()).build())
                        .build())
                .extensions(new HashMap<>())
                .definitionGroupName(this.toolMetadata.definitionGroupName())
                .build();
    }

    private String getGenericableId() {
        Genericable toolMethodAnnotation = this.testMethod.getAnnotation(Genericable.class);
        if (toolMethodAnnotation == null) {
            return "default genericable id";
        }
        return toolMethodAnnotation.id();
    }

    private Map<String, Object> buildSchema() {
        return MapBuilder.<String, Object>get()
                .put("name", "test_method_default_implementation_name")
                .put("index", "test_method_index")
                .put("description", "This is a demo FIT function.")
                .put(ToolSchema.PARAMETERS,
                        MapBuilder.<String, Object>get()
                                .put("type", "object")
                                .put(ToolSchema.PARAMETERS_PROPERTIES,
                                        MapBuilder.<String, Object>get()
                                                .put("p1",
                                                        MapBuilder.<String, Object>get()
                                                                .put("type", "string")
                                                                .put("default", "This is the first parameter.")
                                                                .build())
                                                .build())
                                .build())
                .put(ToolSchema.PARAMETERS_ORDER, Collections.singletonList("p1"))
                .put(ToolSchema.RETURN_SCHEMA, MapBuilder.<String, Object>get().put("type", "string").build())
                .build();
    }

    private Map<String, Object> buildDefSchema() {
        return MapBuilder.<String, Object>get()
                .put("name", "test_tool_def_name")
                .put("description", "测试方法的描述信息")
                .put(ToolSchema.PARAMETERS,
                        MapBuilder.<String, Object>get()
                                .put("type", "object")
                                .put(ToolSchema.PARAMETERS_PROPERTIES,
                                        MapBuilder.<String, Object>get()
                                                .put("P1",
                                                        MapBuilder.<String, Object>get()
                                                                .put("name", "P1")
                                                                .put("type", "string")
                                                                .build())
                                                .build())
                                .build())
                .put(ToolSchema.PARAMETERS_ORDER, Collections.singletonList("P1"))
                .put(ToolSchema.RETURN_SCHEMA, MapBuilder.<String, Object>get().put("type", "string").build())
                .build();
    }

    @Test
    @DisplayName("当 FIT 调用成功，返回正确的结果")
    void shouldReturnCorrectResult() {
        Object result = this.tool.execute("1");
        assertThat(result).isEqualTo("OK");
    }

    @Test
    @DisplayName("当 FIT 调用失败，返回错误的结果")
    void shouldReturnIncorrectResult() {
        IllegalStateException cause = catchThrowableOfType(() -> this.tool.execute("2"), IllegalStateException.class);
        assertThat(cause).hasMessage("Error");
    }

    @Test
    @DisplayName("返回正确的参数类型")
    void shouldReturnParameters() {
        List<Type> parameters = this.toolMetadata.parameterTypes();
        assertThat(parameters).containsExactly(String.class);
    }

    @Test
    @DisplayName("返回正确的参数名字")
    void shouldReturnParameterNames() {
        List<String> parameterNames = this.toolMetadata.parameterOrder();
        assertThat(parameterNames).containsExactly("P1");
    }

    @Test
    @DisplayName("返回正确的参数序号")
    void shouldReturnParameterIndex() {
        int actual = this.toolMetadata.parameterIndex("P1");
        assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("返回正确的必须参数名字列表")
    void shouldReturnRequired() {
        Set<String> parameterNames = this.toolMetadata.requiredParameters();
        assertThat(parameterNames).containsExactly("P1");
    }

    @Test
    @DisplayName("返回正确的返回值类型")
    void shouldReturnReturnType() {
        Map<String, Object> type = this.toolMetadata.returnType();
        assertThat(type).isEqualTo(JsonSchemaManager.create().createSchema(String.class).toJsonObject());
    }

    @Test
    @DisplayName("返回正确的格式规范描述")
    void shouldReturnSchema() {
        Map<String, Object> schema = this.tool.info().schema();
        assertThat(schema).isEqualTo(this.toolSchema);
    }

    @Test
    @DisplayName("返回默认值为空")
    void shouldReturnDefaultValueNull() {
        Map<String, Object> schema = this.tool.metadata().schema();
        String definitionName = this.tool.metadata().definitionName();
        String definitionGroupName = this.tool.metadata().definitionGroupName();
        assertThat(schema).isEqualTo(this.buildDefSchema());
        assertThat(definitionName).isNotNull();
        assertThat(definitionGroupName).isNotNull();
    }

    @Group(name = "test_def_group_name")
    interface TestInterface {
        /**
         * 测试方法。
         *
         * @param p1 表示测试参数的 {@link String}。
         * @return 表示测试结果的 {@link String}。
         */
        @ToolMethod(name = "test_tool_def_name", description = "测试方法的描述信息")
        @Genericable(id = "t1", description = "desc")
        String testMethod(@Property(name = "P1", required = true, defaultValue = "default_value") String p1);
    }
}
