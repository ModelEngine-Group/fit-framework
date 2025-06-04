/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.serialization.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.serialization.test.box.Box;
import modelengine.fit.serialization.test.person.PersonAlias;
import modelengine.fit.serialization.test.person.PersonName;
import modelengine.fitframework.conf.Config;
import modelengine.fitframework.conf.runtime.SerializationFormat;
import modelengine.fitframework.conf.support.PropertiesConfig;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

/**
 * 为 {@link JacksonMessageSerializer} 提供单元测试。
 *
 * @author 梁济时
 * @since 2020-11-23
 */
@DisplayName("测试 JacksonMessageSerializer")
public class JacksonMessageSerializerTest {
    private final Properties properties = new Properties();
    private final Config config = new PropertiesConfig("test", properties);
    private JacksonMessageSerializer messageSerializer;

    @BeforeEach
    void setup() {
        this.messageSerializer =
                new JacksonMessageSerializer(new JacksonObjectSerializer(null, null, null, true), this.config);
    }

    @Test
    @DisplayName("序列化和反序列化字符串需要使用 UTF-8 编码")
    void shouldUseUtf8Charset() {
        Type[] argumentTypes = new Type[] {String.class};
        Object[] expected = new Object[] {"用以UTF-8编码的中文"};
        byte[] bytes = this.messageSerializer.serializeRequest(argumentTypes, expected);
        Object[] actual = this.messageSerializer.deserializeRequest(argumentTypes, bytes);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("支持所有方法")
    void shouldReturnTrue() {
        boolean actual = this.messageSerializer.isSupported(null);
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("返回支持 Json 的格式")
    void shouldReturnJsonFormat() {
        int actual = this.messageSerializer.getFormat();
        assertThat(actual).isEqualTo(SerializationFormat.JSON.code());
    }

    @Nested
    @DisplayName("当给定的参数为泛型时")
    class GivenParamsAreGeneric {
        /**
         * 测试方法。
         *
         * @param b1 简单测试参数。
         * @param b2 复杂测试参数。
         */
        private void testFun1(Box<String> b1, Box<Box<Integer>> b2) {}

        @Test
        @DisplayName("当响应为 null 时，序列化和反序列化结果正确")
        void givenNullWhenSerializeAndDeserializeResponseThenReturnCorrectResult() {
            byte[] bs = JacksonMessageSerializerTest.this.messageSerializer.serializeResponse(String.class, null);
            assertThat(bs).isNotEmpty();
            String actual = JacksonMessageSerializerTest.this.messageSerializer.deserializeResponse(String.class, bs);
            assertThat(actual).isNull();
        }

        @Test
        @DisplayName("当反序列 null 时，结果为 null")
        void givenNullWhenDeserializeThenReturnNull() {
            String actual = JacksonMessageSerializerTest.this.messageSerializer.deserializeResponse(String.class, null);
            assertThat(actual).isNull();
        }

        @Nested
        @DisplayName("当给定的参数为简单类型 Box<String> 时")
        class GivenSimpleGenericParam {
            private Type simpleGenericType;

            @BeforeEach
            void setup() throws NoSuchMethodException {
                Method method = GivenParamsAreGeneric.class.getDeclaredMethod("testFun1", Box.class, Box.class);
                this.simpleGenericType = method.getGenericParameterTypes()[0];
            }

            @Test
            @DisplayName("当请求为 Box<String> 时，序列化和反序列化结果正确")
            void givenSimpleGenericParamWhenSerializeAndDeserializeRequestThenReturnCorrectResult() {
                Box<String> box = Box.<String>builder()
                        .value("hello")
                        .tList(Arrays.asList("s1", "s2"))
                        .tMap(MapBuilder.<String, String>get().put("k", "v").build())
                        .build();

                byte[] bs = JacksonMessageSerializerTest.this.messageSerializer.serializeRequest(new Type[] {
                        this.simpleGenericType
                }, new Object[] {box});
                assertThat(bs).isNotEmpty();
                Object[] params = JacksonMessageSerializerTest.this.messageSerializer.deserializeRequest(new Type[] {
                        this.simpleGenericType
                }, bs);
                assertThat(params).isNotEmpty().hasSize(1);
                assertThat(params[0]).isNotNull();
                Box<String> actualBox = ObjectUtils.cast(params[0]);
                assertThat(actualBox.getValue()).isEqualTo("hello");
                assertThat(actualBox.getTList()).hasSize(2).contains("s1", "s2");
                assertThat(actualBox.getTMap()).hasSize(1).containsEntry("k", "v");
            }

            @Test
            @DisplayName("当响应为 Box<String> 时，序列化和反序列化结果正确")
            void givenSimpleGenericParamWhenSerializeAndDeserializeResponseThenReturnCorrectResult() {
                Box<String> box = Box.<String>builder()
                        .value("hello")
                        .tList(Arrays.asList("s1", "s2"))
                        .tMap(MapBuilder.<String, String>get().put("k", "v").build())
                        .build();

                Type type = this.simpleGenericType;
                byte[] bs = JacksonMessageSerializerTest.this.messageSerializer.serializeResponse(type, box);
                assertThat(bs).isNotEmpty();
                Box<String> actualBox =
                        JacksonMessageSerializerTest.this.messageSerializer.deserializeResponse(type, bs);
                assertThat(actualBox).isNotNull();
                assertThat(actualBox.getValue()).isEqualTo("hello");
                assertThat(actualBox.getTList()).hasSize(2).contains("s1", "s2");
                assertThat(actualBox.getTMap()).hasSize(1).containsEntry("k", "v");
            }
        }

        @Nested
        @DisplayName("当给定的参数为复杂类型 Box<Box<Integer>> 时")
        class GivenComplexGenericParam {
            private Type complexGenericType;

            @BeforeEach
            void setup() throws NoSuchMethodException {
                Method method = GivenParamsAreGeneric.class.getDeclaredMethod("testFun1", Box.class, Box.class);
                this.complexGenericType = method.getGenericParameterTypes()[1];
            }

            @Test
            @DisplayName("当请求为 Box<Box<Integer>> 时，序列化和反序列化结果正确")
            void givenComplexGenericParamWhenSerializeAndDeserializeRequestThenReturnCorrectResult() {
                Box<Integer> inner = Box.<Integer>builder()
                        .value(1)
                        .tList(Arrays.asList(1, 2))
                        .tMap(MapBuilder.<String, Integer>get().put("k", 1).build())
                        .build();
                Box<Box<Integer>> box = Box.<Box<Integer>>builder()
                        .value(inner)
                        .tList(Collections.singletonList(inner))
                        .tMap(MapBuilder.<String, Box<Integer>>get().put("k", inner).build())
                        .build();

                byte[] bs = JacksonMessageSerializerTest.this.messageSerializer.serializeRequest(new Type[] {
                        this.complexGenericType
                }, new Object[] {box});
                assertThat(bs).isNotEmpty();
                Object[] params = JacksonMessageSerializerTest.this.messageSerializer.deserializeRequest(new Type[] {
                        this.complexGenericType
                }, bs);
                assertThat(params).isNotEmpty().hasSize(1);
                assertThat(params[0]).isNotNull();
                Box<Box<Integer>> actualBox = ObjectUtils.cast(params[0]);
                assertThat(actualBox.getValue().getValue()).isEqualTo(1);
                assertThat(actualBox.getTList()).hasSize(1);
                assertThat(actualBox.getTMap()).hasSize(1);
            }

            @Test
            @DisplayName("当响应为 Box<Box<Integer>> 时，序列化和反序列化结果正确")
            void givenComplexGenericParamWhenSerializeAndDeserializeResponseThenReturnCorrectResult() {
                Box<Integer> inner = Box.<Integer>builder()
                        .value(1)
                        .tList(Arrays.asList(1, 2))
                        .tMap(MapBuilder.<String, Integer>get().put("k", 1).build())
                        .build();
                Box<Box<Integer>> box = Box.<Box<Integer>>builder()
                        .value(inner)
                        .tList(Collections.singletonList(inner))
                        .tMap(MapBuilder.<String, Box<Integer>>get().put("k", inner).build())
                        .build();

                Type type = this.complexGenericType;
                byte[] bs = JacksonMessageSerializerTest.this.messageSerializer.serializeResponse(type, box);
                assertThat(bs).isNotEmpty();
                Box<Box<Integer>> actualBox =
                        JacksonMessageSerializerTest.this.messageSerializer.deserializeResponse(type, bs);
                assertThat(actualBox).isNotNull();
                assertThat(actualBox.getValue().getValue()).isEqualTo(1);
                assertThat(actualBox.getTList()).hasSize(1);
                assertThat(actualBox.getTMap()).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("当给定的参数存在别名时")
    class GivenParamsHasAlias {
        @Test
        @DisplayName("当请求存在别名时，反序列化结果正确")
        void givenRequestWithAliasWhenDeserializeThenReturnCorrectResult() {
            String expected = "[{\"first_name\":\"foo\",\"person_name\":{\"first\":\"f\"}}]";
            Object[] actual = JacksonMessageSerializerTest.this.messageSerializer.deserializeRequest(new Type[] {
                    PersonAlias.class
            }, expected.getBytes(StandardCharsets.UTF_8));
            assertThat(actual).hasSize(1).singleElement().isInstanceOf(PersonAlias.class);
            PersonAlias personAlias = ObjectUtils.cast(actual[0]);
            assertThat(personAlias).returns("foo", PersonAlias::firstName).returns(null, PersonAlias::lastName);
            PersonName name = personAlias.getName();
            assertThat(name).isNotNull()
                    .returns("f", PersonName::getFirst)
                    .returns(null, PersonName::getMiddle)
                    .returns(null, PersonName::getLast);
        }
    }
}