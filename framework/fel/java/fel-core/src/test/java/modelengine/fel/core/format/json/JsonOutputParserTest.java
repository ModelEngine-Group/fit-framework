/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format.json;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.core.format.MarkdownCompatibleParser;
import modelengine.fel.core.format.OutputParser;
import modelengine.fel.core.pattern.Parser;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.TypeUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 表示 {@link JsonOutputParser} 的单元测试。
 *
 * @since 2024-04-28
 */
@DisplayName(("测试 JsonOutputParser"))
public class JsonOutputParserTest {
    private static final ObjectSerializer TEST_SERIALIZER = new JacksonObjectSerializer(null, null, null, true);

    static class Joke {
        @Property(description = "question to set up a joke")
        private String setup;

        @Property(description = "answer to resolve the joke")
        private String punchline;

        public String getSetup() {
            return setup;
        }

        public void setSetup(String setup) {
            this.setup = setup;
        }

        public String getPunchline() {
            return punchline;
        }

        public void setPunchline(String punchline) {
            this.punchline = punchline;
        }
    }

    @Nested
    @DisplayName("测试 DefaultJsonOutputParser")
    class Default {
        @Test
        @DisplayName("获取提示词，返回正确结果")
        void getInstructionThenReturnOk() {
            OutputParser<Joke> outputParser = JsonOutputParser.create(TEST_SERIALIZER, Joke.class);
            assertThat(outputParser.instruction()).contains("question to set up a joke")
                    .contains("answer to resolve the joke");
        }

        @Test
        @DisplayName("解析对象成功，返回正确结果")
        void giveJsonThenParseOk() {
            OutputParser<Joke> outputParser = JsonOutputParser.create(TEST_SERIALIZER, Joke.class);
            Joke joke = outputParser.parse("{\"setup\" : \"foo\", \"punchline\": \"bar\"}");
            assertThat(joke.getSetup()).isEqualTo("foo");
            assertThat(joke.getPunchline()).isEqualTo("bar");
        }

        @Test
        @DisplayName("解析markdown代码块成功，返回正确结果")
        void giveMdJsonThenParseOk() {
            Parser<String, Joke> outputParser =
                    new MarkdownCompatibleParser<>(JsonOutputParser.create(TEST_SERIALIZER, Joke.class), "json");
            Joke joke = outputParser.parse("```json\n{\"setup\" : \"foo\", \"punchline\": \"bar\"}\n```");
            assertThat(joke.getSetup()).isEqualTo("foo");
            assertThat(joke.getPunchline()).isEqualTo("bar");
        }
    }

    static class JsonStringProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(Arguments.of("{\"bar\":{\"foo\":\"bar\"}", "{\"bar\":{\"foo\":\"bar\"}}"),
                    Arguments.of("{\"\nfoo\": \"bar\", \"bar\": \"foo\"}", "{\"\\nfoo\":\"bar\",\"bar\":\"foo\"}"),
                    Arguments.of("{\"foo\": \"bar\", \"bar\": \"foo", "{\"foo\":\"bar\",\"bar\":\"foo\"}"),
                    Arguments.of("{\"foo\": \"bar\", \"bar\": \"foo}", "{\"foo\":\"bar\",\"bar\":\"foo}\"}"),
                    Arguments.of("{\"foo\": \"bar\", \"bar\": \"foo[", "{\"foo\":\"bar\",\"bar\":\"foo[\"}"),
                    Arguments.of("{\"foo\": \"bar\", \"bar\": \"foo\\\\\"", "{\"foo\":\"bar\",\"bar\":\"foo\\\\\"}"),
                    Arguments.of("{\"foo\": \"bar\", \"bar\":", "{\"foo\":\"bar\"}"),
                    Arguments.of("{\"foo\": \"bar\", \"bar\"", "{\"foo\":\"bar\"}"),
                    Arguments.of("{\"foo\": \"bar\", ", "{\"foo\":\"bar\"}"),
                    Arguments.of("```json\n{\"foo\": \"bar\", ", "{\"foo\":\"bar\"}"),
                    Arguments.of("\"foo\": \"bar\"}", "{}"),
                    Arguments.of("foo", "{}"));
        }
    }

    @Nested
    @DisplayName("测试 PartialJsonOutputParser")
    class Partial {
        @Test
        @DisplayName("获取提示词，返回正确结果")
        void getInstructionThenReturnOk() {
            OutputParser<Joke> outputParser = JsonOutputParser.createPartial(TEST_SERIALIZER, Joke.class);
            assertThat(outputParser.instruction()).contains("question to set up a joke")
                    .contains("answer to resolve the joke");
        }

        @ParameterizedTest
        @ArgumentsSource(JsonStringProvider.class)
        @DisplayName("测试各种partial json，返回正确结果")
        void givePartialJsonThenParseOk(String input, String except) {
            OutputParser<Map<String, Object>> outputParser = JsonOutputParser.createPartial(TEST_SERIALIZER,
                    TypeUtils.parameterized(Map.class, new Type[] {String.class, Object.class}));
            String output = TEST_SERIALIZER.serialize(outputParser.parse(input));
            assertThat(output).isEqualTo(except);
        }
    }
}