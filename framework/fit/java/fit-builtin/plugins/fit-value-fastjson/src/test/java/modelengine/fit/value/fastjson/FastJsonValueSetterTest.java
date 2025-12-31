/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.value.fastjson;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.value.ValueSetter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link FastJsonValueSetterTest} 的单元测试。
 *
 * @author 季聿阶
 * @since 2024-01-21
 */
public class FastJsonValueSetterTest {
    private ValueSetter setter;

    @BeforeEach
    void setup() {
        this.setter = new FastJsonValueHandler();
    }

    @AfterEach
    void teardown() {
        this.setter = null;
    }

    @Test
    @DisplayName("当 object 为 null 时，返回 null")
    void shouldReturnNullGivenObjectIsNull() {
        Object actual = this.setter.set(null, "k", "v");
        assertThat(actual).isNull();
    }

    @Nested
    @DisplayName("当 object 为基本类型时")
    class GivenObjectIsPrimitive {
        private Object object;

        @BeforeEach
        void setup() {
            this.object = "v";
        }

        @AfterEach
        void teardown() {
            this.object = null;
        }

        @Test
        @DisplayName("当 propertyPath 为空时，返回替换后的值")
        void shouldReturnReplacedValueGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "", "v1");
            assertThat(actual).isEqualTo("v1");
        }
    }

    @Nested
    @DisplayName("当 object 为 JSON 字符串时")
    class GivenObjectIsJsonString {
        private Object object;

        @BeforeEach
        void setup() {
            this.object = "{\"k1\":{\"k2\":\"v\"}}";
        }

        @AfterEach
        void teardown() {
            this.object = null;
        }

        @Test
        @DisplayName("当 propertyPath 不为空时，不自动解析 JSON 字符串")
        void shouldReturnOriginalStringGivenPropertyPathIsNotEmpty() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "k1.k2", "v1");
            assertThat(actual).isEqualTo(this.object);
        }
    }

    @Nested
    @DisplayName("当 object 为键值对时")
    class GivenObjectIsKeyValuePair {
        private Object object;

        @BeforeEach
        void setup() {
            this.object = MapBuilder.<String, Object>get()
                    .put("k1", MapBuilder.<String, Object>get().put("k2", "v").build())
                    .build();
        }

        @AfterEach
        void teardown() {
            this.object = null;
        }

        @Test
        @DisplayName("当 propertyPath 为空时，返回替换后的值")
        void shouldReturnReplacedValueGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "", "v1");
            assertThat(actual).isEqualTo("v1");
        }

        @Test
        @DisplayName("当 propertyPath 为第一层键时，且不以 $ 开头，返回替换第一层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsKeyWithNotRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "k1", "v1");
            assertThat(actual).isInstanceOf(Map.class).hasFieldOrPropertyWithValue("k1", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 为第一层键时，且以 $ 开头，返回替换第一层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsKeyWithRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "$.k1", "v1");
            assertThat(actual).isInstanceOf(Map.class).hasFieldOrPropertyWithValue("k1", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且不以 $ 开头，返回替换第二层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsSubKeyWithNotRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "k1.k2", "v1");
            assertThat(actual).isInstanceOf(Map.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且以 $ 开头，返回替换第二层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsSubKeyWithRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "$.k1.k2", "v1");
            assertThat(actual).isInstanceOf(Map.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，且不以 $ 开头，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsNotKeyWithNotRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "k2", "v1");
            assertThat(actual).isInstanceOf(Map.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，且以 $ 开头，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsNotKeyWithRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "$.k2", "v1");
            assertThat(actual).isInstanceOf(Map.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v");
        }
    }

    @Nested
    @DisplayName("当 object 为对象时")
    class GivenObjectIsObject {
        private Object object;

        @BeforeEach
        void setup() {
            this.object = new Outer(new Inner("v"));
        }

        @AfterEach
        void teardown() {
            this.object = null;
        }

        class Outer {
            private Inner k1;

            Outer() {}

            Outer(Inner k1) {
                this.k1 = k1;
            }

            /**
             * 获取测试的内部类对象。
             *
             * @return 表示测试的内部类对象的 {@link Inner}。
             */
            public Inner getK1() {
                return this.k1;
            }

            /**
             * 设置测试的内部类对象。
             *
             * @param k1 表示测试的内部类对象的 {@link Inner}。
             */
            public void setInner(Inner k1) {
                this.k1 = k1;
            }
        }

        class Inner {
            private String k2;

            Inner() {}

            Inner(String k2) {
                this.k2 = k2;
            }

            /**
             * 获取测试对象。
             *
             * @return 表示测试对象的 {@link String}。
             */
            public String getK2() {
                return this.k2;
            }

            /**
             * 设置测试对象。
             *
             * @param k2 表示测试对象的 {@link String}。
             */
            public void setK2(String k2) {
                this.k2 = k2;
            }
        }

        @Test
        @DisplayName("当 propertyPath 为空时，返回替换后的值")
        void shouldReturnReplacedValueGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "", "v1");
            assertThat(actual).isEqualTo("v1");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且不以 $ 开头，返回替换第二层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsSubKeyWithNotRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "k1.k2", "v1");
            assertThat(actual).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且以 $ 开头，返回替换第二层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsSubKeyWithRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "$.k1.k2", "v1");
            assertThat(actual).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，且不以 $ 开头，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsNotKeyWithNotRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "k2", "v1");
            assertThat(actual).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，且以 $ 开头，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsNotKeyWithRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "$.k2", "v1");
            assertThat(actual).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v");
        }
    }

    @Nested
    @DisplayName("当 object 为列表时")
    class GivenObjectIsList {
        private Object object;

        @BeforeEach
        void setup() {
            List<Outer> outers = new ArrayList<>();
            Outer outer1 = new Outer(new Inner("v"));
            outers.add(outer1);
            this.object = outers;
        }

        @AfterEach
        void teardown() {
            this.object = null;
        }

        class Outer {
            private Inner k1;

            Outer() {}

            Outer(Inner k1) {
                this.k1 = k1;
            }

            /**
             * 获取测试的内部类对象。
             *
             * @return 表示测试的内部类对象的 {@link GivenObjectIsObject.Inner}。
             */
            public Inner getK1() {
                return this.k1;
            }

            /**
             * 设置测试的内部类对象。
             *
             * @param k1 表示测试的内部类对象的 {@link GivenObjectIsObject.Inner}。
             */
            public void setInner(Inner k1) {
                this.k1 = k1;
            }
        }

        class Inner {
            private String k2;

            Inner() {}

            Inner(String k2) {
                this.k2 = k2;
            }

            /**
             * 获取测试对象。
             *
             * @return 表示测试对象的 {@link String}。
             */
            public String getK2() {
                return this.k2;
            }

            /**
             * 设置测试对象。
             *
             * @param k2 表示测试对象的 {@link String}。
             */
            public void setK2(String k2) {
                this.k2 = k2;
            }
        }

        @Test
        @DisplayName("当 propertyPath 为空时，返回替换后的值")
        void shouldReturnReplacedValueGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "", "v1");
            assertThat(actual).isEqualTo("v1");
        }

        @Test
        @DisplayName("当 propertyPath 为列表的某个元素的第二层键时，且不以 $ 开头，返回替换第二层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsSubKeyWithNotRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "[0].k1.k2", "v1");
            assertThat(ObjectUtils.<List<Outer>>cast(actual).get(0)).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 为列表的某个元素的第二层键时，且以 $ 开头，返回替换第二层键后的自身")
        void shouldReturnReplacedObjectGivenPropertyPathIsSubKeyWithRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "$[0].k1.k2", "v1");
            assertThat(ObjectUtils.<List<Outer>>cast(actual).get(0)).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v1");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，且不以 $ 开头，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsNotKeyWithNotRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "[0].k2", "v1");
            assertThat(ObjectUtils.<List<Outer>>cast(actual).get(0)).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，且以 $ 开头，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsNotKeyWithRootStart() {
            Object actual = FastJsonValueSetterTest.this.setter.set(this.object, "$[0].k2", "v1");
            assertThat(ObjectUtils.<List<Outer>>cast(actual).get(0)).isInstanceOf(Outer.class)
                    .hasFieldOrProperty("k1")
                    .extracting("k1")
                    .hasFieldOrPropertyWithValue("k2", "v");
        }
    }
}
