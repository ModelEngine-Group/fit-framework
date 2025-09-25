/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import modelengine.fel.core.template.support.DefaultStringTemplate;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 表示 {@link StringTemplate} 的单元测试。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
@DisplayName("测试 StringTemplate")
public class StringTemplateTest {
    @Test
    @DisplayName("当模板占位符全匹配时，返回正确结果")
    void giveMatchValueThenReturnOk() {
        String template = "Tell me a {{adjective}} joke about {{content}}.";
        Map<String, String> values =
                MapBuilder.<String, String>get().put("adjective", "funny").put("content", "chickens").build();

        String output = new DefaultStringTemplate(template).render(values);
        assertThat(output).isEqualTo("Tell me a funny joke about chickens.");
    }

    @Test
    @DisplayName("当模板占位符多重匹配时，返回正确结果")
    void giveMultiMatchValueThenReturnOk() {
        String template = "{{0}} make men {{1}}, then {{1}} men make {{0}}.";
        Map<String, String> values = MapBuilder.<String, String>get().put("0", "dollars").put("1", "covetous").build();
        String output = new DefaultStringTemplate(template).render(values);
        assertThat(output).isEqualTo("dollars make men covetous, then covetous men make dollars.");
    }

    @Test
    @DisplayName("当模板占位符未全匹配时，抛出异常")
    void giveMissValueThenThrowException() {
        String template = "Tell me a {{adjective}} joke about {{content}}.";
        Map<String, String> values = MapBuilder.<String, String>get().put("adjective", "funny").build();
        assertThatThrownBy(() -> new DefaultStringTemplate(template).render(values)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("当使用partial模板时，返回正确结果")
    void givePartialThenReturnOk() {
        String template = "Tell me a {{adjective}} joke about {{content}}.";
        Map<String, String> values = MapBuilder.<String, String>get().put("adjective", "funny").build();
        StringTemplate partial = new DefaultStringTemplate(template).partial("content", "chickens");
        assertThat(partial.placeholder()).contains("adjective").doesNotContain("content");
        assertThat(partial.render(values)).isEqualTo("Tell me a funny joke about chickens.");
    }
}