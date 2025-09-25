/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import modelengine.fel.core.fewshot.Example;
import modelengine.fel.core.fewshot.ExampleSelector;
import modelengine.fel.core.fewshot.support.DefaultExample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

/**
 * 表示 {@link ExampleSelector} 的单元测试。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
@DisplayName("测试 ExampleSelector")
public class ExampleSelectorTest {
    private static final Example[] TEST_EXAMPLES = {new DefaultExample("2+2", "4"), new DefaultExample("2+3", "5")};

    @Test
    @DisplayName("无过滤器, 返回正确结果")
    void giveDefaultFilterThenReturnOk() {
        ExampleSelector exampleSelector =
                ExampleSelector.builder().template("Human: {{q}}\nAI: {{a}}", "q", "a").example(TEST_EXAMPLES).build();
        assertThat(exampleSelector.select("2+2")).isEqualTo("Human: 2+2\nAI: 4\n\nHuman: 2+3\nAI: 5");
    }

    @Test
    @DisplayName("有过滤器, 返回正确结果")
    void giveCustomFilterThenReturnOk() {
        ExampleSelector exampleSelector = ExampleSelector.builder()
                .template("Human: {{q}}\nAI: {{a}}", "q", "a")
                .example(TEST_EXAMPLES)
                .filter((es, q) -> es.stream().filter(e -> e.question().equals(q)).collect(Collectors.toList()))
                .build();
        assertThat(exampleSelector.select("2+2")).isEqualTo("Human: 2+2\nAI: 4");
    }

    @Test
    @DisplayName("自定义分割符, 返回正确结果")
    void giveCustomDelimiterThenReturnOk() {
        ExampleSelector exampleSelector = ExampleSelector.builder()
                .template("Human: {{q}}\nAI: {{a}}", "q", "a")
                .example(TEST_EXAMPLES)
                .delimiter("o(*≥▽≤)ツ")
                .build();
        assertThat(exampleSelector.select("2+2")).isEqualTo("Human: 2+2\nAI: 4o(*≥▽≤)ツHuman: 2+3\nAI: 5");
    }

    @Test
    @DisplayName("输入错误模板，抛出异常")
    void giveWrongTemplateThenReturnThrowException() {
        assertThatThrownBy(() -> ExampleSelector.builder()
                .template("Human: {{q}}\nAI: {{a}}", "q", "w")
                .example(TEST_EXAMPLES)
                .build()).isInstanceOf(IllegalArgumentException.class);
    }
}