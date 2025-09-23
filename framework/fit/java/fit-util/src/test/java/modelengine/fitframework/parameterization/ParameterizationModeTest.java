/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.parameterization;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

/**
 * 为新增的参数化模式和缺失参数处理策略提供测试。
 *
 * @author 季聿阶
 * @since 2025-09-23
 */
@Nested
@DisplayName("验证新增的参数化模式功能")
class ParameterizationModeTest {
    @Nested
    @DisplayName("测试不同的参数化模式")
    class TestDifferentModes {
        @Test
        @DisplayName("严格模式：缺失参数时抛出异常")
        void strictModeShouldThrowWhenParameterMissing() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.STRICT);
            ParameterizedString parameterizedString = resolver.resolve("Hello ${name}, welcome to ${city}!");

            Map<String, Object> incompleteParams = Map.of("name", "张三");

            StringFormatException exception = catchThrowableOfType(StringFormatException.class,
                    () -> parameterizedString.format(incompleteParams));
            assertThat(exception).isNotNull().hasMessage("The provided args is not match the required args.");
        }

        @Test
        @DisplayName("宽松空字符串模式：缺失参数使用空字符串")
        void lenientEmptyModeShouldUseEmptyString() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_EMPTY);
            ParameterizedString parameterizedString = resolver.resolve("Hello ${name}, welcome to ${city}!");

            Map<String, Object> incompleteParams = Map.of("name", "张三");
            String result = parameterizedString.format(incompleteParams);

            assertThat(result).isEqualTo("Hello 张三, welcome to !");
        }

        @Test
        @DisplayName("宽松默认值模式：缺失参数使用指定默认值")
        void lenientDefaultModeShouldUseDefaultValue() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_DEFAULT);
            ParameterizedString parameterizedString = resolver.resolve("Hello ${name}, welcome to ${city}!");

            Map<String, Object> incompleteParams = Map.of("name", "张三");
            String result = parameterizedString.format(incompleteParams, "未知城市");

            assertThat(result).isEqualTo("Hello 张三, welcome to 未知城市!");
        }

        @Test
        @DisplayName("宽松默认值模式：默认值为null时使用空字符串")
        void lenientDefaultModeShouldUseEmptyWhenDefaultIsNull() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_DEFAULT);
            ParameterizedString parameterizedString = resolver.resolve("Hello ${name}, welcome to ${city}!");

            Map<String, Object> incompleteParams = Map.of("name", "张三");
            String result = parameterizedString.format(incompleteParams, null);

            assertThat(result).isEqualTo("Hello 张三, welcome to !");
        }

        @Test
        @DisplayName("宽松保持占位符模式：缺失参数保持原占位符")
        void lenientKeepPlaceholderModeShouldKeepPlaceholder() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_KEEP_PLACEHOLDER);
            ParameterizedString parameterizedString = resolver.resolve("Hello ${name}, welcome to ${city}!");

            Map<String, Object> incompleteParams = Map.of("name", "张三");
            String result = parameterizedString.format(incompleteParams);

            assertThat(result).isEqualTo("Hello 张三, welcome to ${city}!");
        }

        @Test
        @DisplayName("混合模式：允许多余参数但缺失参数仍抛异常")
        void lenientStrictParametersModeShouldAllowExtraButThrowOnMissing() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_STRICT_PARAMETERS);
            ParameterizedString parameterizedString = resolver.resolve("Hello ${name}!");

            // 测试允许多余参数
            Map<String, Object> extraParams = Map.of("name", "张三", "extra", "多余参数");
            String result1 = parameterizedString.format(extraParams);
            assertThat(result1).isEqualTo("Hello 张三!");

            // 测试缺失参数仍然抛异常
            Map<String, Object> incompleteParams = Map.of("extra", "多余参数");
            StringFormatException exception = catchThrowableOfType(StringFormatException.class,
                    () -> parameterizedString.format(incompleteParams));
            assertThat(exception).isNotNull().hasMessage("Parameter 'name' required but not supplied.");
        }
    }

    @Nested
    @DisplayName("测试语法错误处理")
    class TestSyntaxErrorHandling {
        @Test
        @DisplayName("严格模式：孤立后缀符号抛出异常")
        void strictModeShouldThrowOnInvalidSuffix() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.STRICT);

            StringFormatException exception =
                    catchThrowableOfType(StringFormatException.class, () -> resolver.resolve("Hello world}"));
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("宽松模式：孤立后缀符号当作普通字符")
        void lenientModeShouldTreatInvalidSuffixAsNormalChar() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_EMPTY);
            ParameterizedString parameterizedString = resolver.resolve("Hello world}");

            String result = parameterizedString.format(Collections.emptyMap());
            assertThat(result).isEqualTo("Hello world}");
        }
    }

    @Nested
    @DisplayName("测试复杂场景")
    class TestComplexScenarios {
        @Test
        @DisplayName("JSON配置文件场景：混合占位符和JSON语法")
        void shouldHandleJsonConfigurationCorrectly() {
            String jsonTemplate = """
                    {
                      "user": "${username}",
                      "config": {
                        "timeout": ${timeout},
                        "features": ["feature1", "feature2"],
                        "metadata": {"version": "1.0", "type": "${appType}"}
                      },
                      "missing": "${missingField}"
                    }""";

            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_DEFAULT);
            ParameterizedString parameterizedString = resolver.resolve(jsonTemplate);

            Map<String, Object> params = Map.of("username", "testUser", "timeout", "5000", "appType", "web"
                    // 注意：缺少 missingField
            );

            String result = parameterizedString.format(params, "defaultValue");

            // 验证JSON结构保持完整，只有占位符被替换
            assertThat(result).contains("\"user\": \"testUser\"");
            assertThat(result).contains("\"timeout\": 5000");
            assertThat(result).contains("\"type\": \"web\"");
            assertThat(result).contains("\"missing\": \"defaultValue\"");
            // JSON语法应该保持不变
            assertThat(result).contains("\"features\": [\"feature1\", \"feature2\"]");
            assertThat(result).contains("\"metadata\": {\"version\": \"1.0\"");
        }

        @Test
        @DisplayName("日志模板场景：允许部分参数缺失")
        void shouldHandleLogTemplateWithMissingParameters() {
            String logTemplate =
                    "[${timestamp}] ${level} ${class} - ${message} (user: ${userId}, session: ${sessionId})";

            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_EMPTY);
            ParameterizedString parameterizedString = resolver.resolve(logTemplate);

            Map<String, Object> params = Map.of("timestamp",
                    "2024-01-15 10:30:00",
                    "level",
                    "INFO",
                    "class",
                    "UserService",
                    "message",
                    "User login successful"
                    // 缺少 userId 和 sessionId
            );

            String result = parameterizedString.format(params);

            assertThat(result).isEqualTo(
                    "[2024-01-15 10:30:00] INFO UserService - User login successful (user: , session: )");
        }

        @Test
        @DisplayName("配置文件场景：使用统一默认值")
        void shouldHandleConfigFileWithUniformDefault() {
            String configTemplate = """
                    server.host=${host}
                    server.port=${port}
                    database.url=${dbUrl}
                    database.username=${dbUser}
                    database.password=${dbPassword}
                    cache.enabled=${cacheEnabled}
                    """;

            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_DEFAULT);
            ParameterizedString parameterizedString = resolver.resolve(configTemplate);

            Map<String, Object> params = Map.of("host", "localhost", "port", "8080"
                    // 其他参数缺失
            );

            String result = parameterizedString.format(params, "NOT_SET");

            assertThat(result).contains("server.host=localhost");
            assertThat(result).contains("server.port=8080");
            assertThat(result).contains("database.url=NOT_SET");
            assertThat(result).contains("database.username=NOT_SET");
            assertThat(result).contains("database.password=NOT_SET");
            assertThat(result).contains("cache.enabled=NOT_SET");
        }
    }

    @Nested
    @DisplayName("测试边界情况")
    class TestEdgeCases {
        @Test
        @DisplayName("无占位符的字符串")
        void shouldHandleStringWithoutPlaceholders() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_EMPTY);
            ParameterizedString parameterizedString = resolver.resolve("Plain text without placeholders");

            String result = parameterizedString.format(Map.of("unused", "value"));

            assertThat(result).isEqualTo("Plain text without placeholders");
        }

        @Test
        @DisplayName("空字符串模板")
        void shouldHandleEmptyTemplate() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_DEFAULT);
            ParameterizedString parameterizedString = resolver.resolve("");

            String result = parameterizedString.format(Collections.emptyMap(), "default");

            assertThat(result).isEqualTo("");
        }

        @Test
        @DisplayName("只有占位符的模板")
        void shouldHandleTemplateWithOnlyPlaceholders() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_KEEP_PLACEHOLDER);
            ParameterizedString parameterizedString = resolver.resolve("${param1}${param2}${param3}");

            String result = parameterizedString.format(Map.of("param2", "middle"));

            assertThat(result).isEqualTo("${param1}middle${param3}");
        }

        @Test
        @DisplayName("重复占位符")
        void shouldHandleDuplicatePlaceholders() {
            ParameterizedStringResolver resolver =
                    ParameterizedStringResolver.create("${", "}", '/', ParameterizationMode.LENIENT_DEFAULT);
            ParameterizedString parameterizedString = resolver.resolve("${name} says hello to ${name} again!");

            String result = parameterizedString.format(Collections.emptyMap(), "Unknown");

            assertThat(result).isEqualTo("Unknown says hello to Unknown again!");
        }
    }
}
