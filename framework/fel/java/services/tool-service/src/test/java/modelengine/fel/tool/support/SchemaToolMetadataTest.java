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
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.broker.client.Invoker;
import modelengine.fitframework.broker.client.Router;
import modelengine.fitframework.json.schema.JsonSchemaManager;
import modelengine.fitframework.json.schema.type.OneOfType;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.IoUtils;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 表示 {@link Tool} 的单元测试。
 *
 * @since 2024-04-06
 */
@DisplayName("测试 FitJsonSchemaFunctionalTool")
public class SchemaToolMetadataTest {
    private static final String DEFINITION_GROUP_NAME = "test_definition_group_name";
    private static final String DEFINITION_NAME = "test_definition_name";

    private Map<String, Object> toolSchema;
    private Map<String, Object> definitionSchema;
    private Tool tool;
    private Tool.Metadata toolMetadata;

    private final ObjectSerializer jsonSerializer = new JacksonObjectSerializer(null, null, null, true);

    @BeforeEach
    void setup() {
        Router router = mock(Router.class);
        Invoker invoker = mock(Invoker.class);
        BrokerClient client = mock(BrokerClient.class);
        when(client.getRouter(eq("t1"))).thenReturn(router);
        when(router.route(any())).thenReturn(invoker);
        when(invoker.communicationType(any())).thenReturn(invoker);
        when(invoker.invoke(any(Object[].class))).thenAnswer(invocation -> {
            if (Objects.equals(invocation.getArgument(0), "1")) {
                return "1";
            } else if (Objects.equals(invocation.getArgument(0), "2")) {
                return invocation.getArgument(2).toString();
            } else {
                throw new IllegalStateException("Error");
            }
        });

        this.toolSchema = this.buildSchema();
        this.definitionSchema = this.buildDefinitionSchema();
        this.toolMetadata = Tool.Metadata.fromSchema(DEFINITION_GROUP_NAME, this.definitionSchema);
        ObjectSerializer serializer = new JacksonObjectSerializer(null, null, null, true);
        FitToolFactory fitToolFactory = new FitToolFactory(client, serializer);
        this.tool = fitToolFactory.create(this.buildInfo(), this.toolMetadata);
    }

    private Tool.Info buildInfo() {
        return Tool.Info.custom()
                .namespace("Common")
                .name("test_schema_default_implementation_name")
                .uniqueName("schema-uuid")
                .description("")
                .schema(this.buildSchema())
                .defaultParameterValues(MapBuilder.<String, Object>get()
                        .put("extraP1", "extraP1 default value")
                        .build())
                .parameters(new HashMap<>())
                .runnables(MapBuilder.<String, Object>get()
                        .put("FIT", MapBuilder.<String, Object>get().put("genericableId", "t1").build())
                        .build())
                .extensions(new HashMap<>())
                .build();
    }

    private Map<String, Object> buildSchema() {
        URL resource = SchemaToolMetadataTest.class.getClassLoader().getResource("tool-schema.json");
        assertThat(resource).isNotNull();
        try (InputStream in = resource.openStream()) {
            String content = IoUtils.content(in);
            return this.jsonSerializer.deserialize(content, Object.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> buildDefinitionSchema() {
        URL resource = SchemaToolMetadataTest.class.getClassLoader().getResource("definition.json");
        assertThat(resource).isNotNull();
        try (InputStream in = resource.openStream()) {
            String content = IoUtils.content(in);
            Map<String, Object> definition = this.jsonSerializer.deserialize(content, Object.class);
            return ObjectUtils.cast(definition.get("schema"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("当 FIT 调用成功，返回正确的结果")
    void shouldReturnCorrectResult() {
        Object result = this.tool.execute("1");
        assertThat(result).isEqualTo("1");
    }

    @Test
    @DisplayName("当参数匹配 oneOf 的第一个类型时，调用成功")
    void shouldReturnCorrectResultWhenMatchThe1stOneOfType() {
        Object result = this.tool.executeWithJsonObject(MapBuilder.<String, Object>get()
                .put("p1", "2")
                .put("p2", "string")
                .build());
        assertThat(result).isEqualTo("string");
    }

    @Test
    @DisplayName("当参数匹配 oneOf 的第二个类型时，调用成功")
    void shouldReturnCorrectResultWhenMatchThe2edOneOfType() {
        Object result = this.tool.executeWithJsonObject(MapBuilder.<String, Object>get()
                .put("p1", "2")
                .put("p2", Arrays.asList("Hello", "World"))
                .build());
        assertThat(result).isEqualTo("[Hello, World]");
    }

    @Test
    @DisplayName("当 FIT 调用失败，返回错误的结果")
    void shouldReturnIncorrectResult() {
        IllegalStateException cause =
                catchThrowableOfType(() -> this.tool.execute("Error"), IllegalStateException.class);
        assertThat(cause).hasMessage("Error");
    }

    @Test
    @DisplayName("返回正确的参数类型")
    void shouldReturnParameters() {
        List<Type> parameters = this.toolMetadata.parameterTypes();
        assertThat(parameters).hasSize(3);
        assertThat(parameters.get(0)).isEqualTo(String.class);
        assertThat(parameters.get(1)).isEqualTo(String.class);
        assertThat(parameters.get(2)).isEqualTo(new OneOfType(String.class, List.class));
    }

    @Test
    @DisplayName("返回正确的参数名字")
    void shouldReturnParameterNames() {
        List<String> parameterNames = this.toolMetadata.parameterOrder();
        assertThat(parameterNames).containsExactly("p1", "extraP1", "p2");
    }

    @Test
    @DisplayName("返回正确的参数序号")
    void shouldReturnParameterIndex() {
        int actual = this.toolMetadata.parameterIndex("p1");
        assertThat(actual).isEqualTo(0);

        actual = this.toolMetadata.parameterIndex("extraP1");
        assertThat(actual).isEqualTo(1);

        actual = this.toolMetadata.parameterIndex("p2");
        assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("返回正确的必须参数名字列表")
    void shouldReturnRequired() {
        Set<String> parameterNames = this.toolMetadata.requiredParameters();
        assertThat(parameterNames).containsExactly("p1");
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
    @DisplayName("返回正确的定义名")
    void shouldReturnDefinitionName() {
        String definitionName = this.tool.metadata().definitionName();
        assertThat(definitionName).isEqualTo(DEFINITION_NAME);
    }

    @Test
    @DisplayName("返回正确的定义组名")
    void shouldReturnDefinitionGroupName() {
        String definitionGroupName = this.tool.metadata().definitionGroupName();
        assertThat(definitionGroupName).isEqualTo(DEFINITION_GROUP_NAME);
    }

    @Test
    @DisplayName("返回正确的定义描述信息")
    void shouldReturnDefinitionSchema() {
        Map<String, Object> schema = this.tool.metadata().schema();
        assertThat(schema).isEqualTo(this.definitionSchema);
    }

    @Test
    @DisplayName("给定的order值为空，默认使用properties中参数的顺序")
    void givenEmptyOrderThenReturnPropertiesKeysInOrder() {
        Map<String, Object> map = new HashMap<>();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("b", MapBuilder.<String, Object>get().put("type", "string").build());
        parameters.put("c", MapBuilder.<String, Object>get().put("type", "string").build());
        parameters.put("d", MapBuilder.<String, Object>get().put("type", "string").build());
        parameters.put("a", MapBuilder.<String, Object>get().put("type", "string").build());
        map.put(ToolSchema.PARAMETERS,
                MapBuilder.<String, Object>get()
                        .put("type", "object")
                        .put(ToolSchema.PARAMETERS_PROPERTIES, parameters)
                        .put(ToolSchema.PARAMETERS_REQUIRED, Collections.singletonList("p1"))
                        .build());
        map.put(ToolSchema.NAME, "tool_unique_name");
        this.toolMetadata = Tool.Metadata.fromSchema(DEFINITION_GROUP_NAME, map);
        List<String> parameterNames = this.toolMetadata.parameterOrder();
        assertThat(parameterNames).containsExactly("b", "c", "d", "a");
    }
}
