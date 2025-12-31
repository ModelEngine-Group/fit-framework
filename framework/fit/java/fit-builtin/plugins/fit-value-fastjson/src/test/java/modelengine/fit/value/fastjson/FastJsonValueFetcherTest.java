/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.value.fastjson;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.value.ValueFetcher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link FastJsonValueHandler} 的单元测试。
 *
 * @author 季聿阶
 * @since 2023-11-24
 */
@DisplayName("测试 fastjson 的值获取功能")
public class FastJsonValueFetcherTest {
    private ValueFetcher fetcher;

    @BeforeEach
    void setup() {
        this.fetcher = new FastJsonValueHandler();
    }

    @AfterEach
    void teardown() {
        this.fetcher = null;
    }

    @Test
    @DisplayName("当 object 为 null 时，返回 null")
    void shouldReturnNullGivenObjectIsNull() {
        Object actual = this.fetcher.fetch(null, "k");
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
        @DisplayName("当 propertyPath 为空时，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "");
            assertThat(actual).isEqualTo(this.object);
        }

        @Test
        @DisplayName("当 propertyPath 不为空时，返回 null")
        void shouldReturnNullGivenPropertyPathIsNotEmpty() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k");
            assertThat(actual).isNull();
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
        void shouldReturnNullGivenPropertyPathIsNotEmpty() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k1.k2");
            assertThat(actual).isNull();
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
        @DisplayName("当 propertyPath 为空时，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "");
            assertThat(actual).isEqualTo(this.object);
        }

        @Test
        @DisplayName("当 propertyPath 为第一层键时，且不以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsKeyWithNotRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k1");
            assertThat(actual).isInstanceOf(Map.class).hasFieldOrPropertyWithValue("k2", "v");
        }

        @Test
        @DisplayName("当 propertyPath 为第一层键时，且以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsKeyWithRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "$.k1");
            assertThat(actual).isInstanceOf(Map.class).hasFieldOrPropertyWithValue("k2", "v");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且不以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsSubKeyWithNotRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k1.k2");
            assertThat(actual).isInstanceOf(String.class).isEqualTo("v");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsSubKeyWithRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "$.k1.k2");
            assertThat(actual).isInstanceOf(String.class).isEqualTo("v");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，返回 null")
        void shouldReturnNullGivenPropertyPathIsNotKey() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k2");
            assertThat(actual).isNull();
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
            private final Inner k1;

            Outer(Inner k1) {
                this.k1 = k1;
            }

            public Inner getK1() {
                return this.k1;
            }
        }

        class Inner {
            private final String k2;

            Inner(String k2) {
                this.k2 = k2;
            }

            public String getK2() {
                return this.k2;
            }
        }

        @Test
        @DisplayName("当 propertyPath 为空时，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "");
            assertThat(actual).isEqualTo(this.object);
        }

        @Test
        @DisplayName("当 propertyPath 为第一层键时，且不以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsKeyWithNotRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k1");
            assertThat(actual).isInstanceOf(Inner.class);
            Inner inner = ObjectUtils.cast(actual);
            assertThat(inner.getK2()).isEqualTo("v");
        }

        @Test
        @DisplayName("当 propertyPath 为第一层键时，且以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsKeyWithRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "$.k1");
            assertThat(actual).isInstanceOf(Inner.class);
            Inner inner = ObjectUtils.cast(actual);
            assertThat(inner.getK2()).isEqualTo("v");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且不以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsSubKeyWithNotRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k1.k2");
            assertThat(actual).isInstanceOf(String.class).isEqualTo("v");
        }

        @Test
        @DisplayName("当 propertyPath 为第二层键时，且以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsSubKeyWithRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "$.k1.k2");
            assertThat(actual).isInstanceOf(String.class).isEqualTo("v");
        }

        @Test
        @DisplayName("当 propertyPath 不为其键时，返回 null")
        void shouldReturnNullGivenPropertyPathIsNotKey() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "k2");
            assertThat(actual).isNull();
        }
    }

    @Nested
    @DisplayName("当 object 为列表时")
    class GivenObjectIsList {
        private Object object;

        @BeforeEach
        void setup() {
            List<Outer> outers = new ArrayList<>();
            Outer outer1 = new Outer(new Inner("v1"));
            outers.add(outer1);
            this.object = outers;
        }

        @AfterEach
        void teardown() {
            this.object = null;
        }

        class Outer {
            private final Inner k1;

            Outer(Inner k1) {
                this.k1 = k1;
            }

            public Inner getK1() {
                return this.k1;
            }
        }

        class Inner {
            private final String k2;

            Inner(String k2) {
                this.k2 = k2;
            }

            public String getK2() {
                return this.k2;
            }
        }

        @Test
        @DisplayName("当 propertyPath 为空时，返回 object 自身")
        void shouldReturnObjectItselfGivenPropertyPathIsEmpty() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "");
            assertThat(actual).isEqualTo(this.object);
        }

        @Test
        @DisplayName("当 propertyPath 为列表的某个元素的第一层键时，且不以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsKeyWithNotRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "[0].k1");
            assertThat(actual).isInstanceOf(Inner.class);
            Inner inner = ObjectUtils.cast(actual);
            assertThat(inner.getK2()).isEqualTo("v1");
        }

        @Test
        @DisplayName("当 propertyPath 为列表的某个元素的第一层键时，且以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsKeyWithRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "$[0].k1");
            assertThat(actual).isInstanceOf(Inner.class);
            Inner inner = ObjectUtils.cast(actual);
            assertThat(inner.getK2()).isEqualTo("v1");
        }

        @Test
        @DisplayName("当 propertyPath 为列表的某个元素的第二层键时，且不以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsSubKeyWithNotRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "[0].k1.k2");
            assertThat(actual).isInstanceOf(String.class).isEqualTo("v1");
        }

        @Test
        @DisplayName("当 propertyPath 为列表的某个元素的第二层键时，且以 $ 开头，返回其键的值")
        void shouldReturnItsValueGivenPropertyPathIsSubKeyWithRootStart() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "$[0].k1.k2");
            assertThat(actual).isInstanceOf(String.class).isEqualTo("v1");
        }

        @Test
        @DisplayName("当 propertyPath 不为列表的元素的键时，返回 null")
        void shouldReturnNullGivenPropertyPathIsNotKey() {
            Object actual = FastJsonValueFetcherTest.this.fetcher.fetch(this.object, "[0].k2");
            assertThat(actual).isNull();
        }
    }
}
