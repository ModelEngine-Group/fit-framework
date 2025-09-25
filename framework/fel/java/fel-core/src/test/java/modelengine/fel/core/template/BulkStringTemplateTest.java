/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import modelengine.fel.core.template.support.DefaultBulkStringTemplate;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 表示 {@link BulkStringTemplate} 的单元测试。
 *
 * @author 何嘉斌
 * @since 2024-05-15
 */
@DisplayName("测试 BulkStringTemplateTest")
public class BulkStringTemplateTest {
    @Test
    @DisplayName("当模板占位符全匹配时，返回正确结果")
    void giveMatchValueThenReturnOk() {
        String template = "Tell me a {{adjective}} story about {{content}}.";
        List<Map<String, String>> values = new ArrayList<>();
        values.add(MapBuilder.<String, String>get().put("adjective", "funny").put("content", "chickens").build());
        values.add(MapBuilder.<String, String>get().put("adjective", "sad").put("content", "rabbits").build());
        String output = new DefaultBulkStringTemplate(template, "\n").render(values);
        assertThat(output).isEqualTo("Tell me a funny story about chickens.\nTell me a sad story about rabbits.");
    }

    @Test
    @DisplayName("当模板占位符多重匹配时，返回正确结果")
    void giveMultiMatchValueThenReturnOk() {
        String template = "{{0}} are {{1}}, but not all {{1}} are {{0}}.";
        List<Map<String, String>> values = new ArrayList<>();
        values.add(MapBuilder.<String, String>get().put("0", "dogs").put("1", "animals").build());
        values.add(MapBuilder.<String, String>get().put("0", "cats").put("1", "animals").build());
        String output = new DefaultBulkStringTemplate(template, "\n").render(values);
        assertThat(output).isEqualTo("dogs are animals, but not all animals are dogs.\n"
                + "cats are animals, but not all animals are cats.");
    }

    @Test
    @DisplayName("当输入单个案例时，返回正确结果")
    void giveOneValueThenReturnOkWithoutDelimiter() {
        String template = "Tell me a {{adjective}} story about {{content}}.";
        List<Map<String, String>> values = new ArrayList<>();
        values.add(MapBuilder.<String, String>get().put("adjective", "funny").put("content", "chickens").build());
        String output = new DefaultBulkStringTemplate(template, "\n").render(values);
        assertThat(output).isEqualTo("Tell me a funny story about chickens.");
    }

    @Test
    @DisplayName("当输入多个案例时，返回正确结果")
    void giveMultiValuesThenReturnOkWithDelimiter() {
        String template = "Tell me a {{adjective}} story about {{content}}.";
        List<Map<String, String>> values = new ArrayList<>();
        values.add(MapBuilder.<String, String>get().put("adjective", "funny").put("content", "chickens").build());
        values.add(MapBuilder.<String, String>get().put("adjective", "sad").put("content", "rabbits").build());
        values.add(MapBuilder.<String, String>get().put("adjective", "lovely").put("content", "cats").build());
        String output = new DefaultBulkStringTemplate(template, "\n\n").render(values);
        assertThat(output).isEqualTo(
                "Tell me a funny story about chickens.\n\n" + "Tell me a sad story about rabbits.\n\n"
                        + "Tell me a lovely story about cats.");
    }

    @Test
    @DisplayName("当模板占位符未全匹配时，抛出异常")
    void giveMissValueThenThrowException() {
        String template = "Tell me a {{adjective}} story about {{content}}.";
        List<Map<String, String>> values = new ArrayList<>();
        values.add(MapBuilder.<String, String>get().put("adjective", "funny").build());
        values.add(MapBuilder.<String, String>get().put("content", "rabbits").build());
        assertThatThrownBy(() -> new DefaultBulkStringTemplate(template, "\n").render(values)).isInstanceOf(
                IllegalArgumentException.class);
    }
}
