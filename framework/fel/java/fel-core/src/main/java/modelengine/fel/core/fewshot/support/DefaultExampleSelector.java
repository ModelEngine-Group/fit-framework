/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.fewshot.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.fewshot.Example;
import modelengine.fel.core.fewshot.ExampleSelector;
import modelengine.fel.core.template.BulkStringTemplate;
import modelengine.fel.core.template.StringTemplate;
import modelengine.fel.core.template.support.DefaultBulkStringTemplate;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.MapBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表示 {@link ExampleSelector} 的默认实现。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public class DefaultExampleSelector implements ExampleSelector {
    private final BulkStringTemplate bulkTemplate;
    private final List<Example> examples;
    private final BiFunction<List<Example>, String, List<Example>> filter;
    private final Function<Example, Map<String, String>> processor;

    private DefaultExampleSelector(DefaultExampleSelector.Builder builder) {
        notNull(builder.template, "The template cannot be null.");
        this.bulkTemplate = new DefaultBulkStringTemplate(builder.template, builder.delimiter);
        this.filter = builder.filter;
        this.examples = builder.examples;
        this.processor = e -> MapBuilder.<String, String>get()
                .put(builder.input.get(0), e.question())
                .put(builder.input.get(1), e.answer())
                .build();
    }

    @Override
    public String select(String question) {
        return this.filter.apply(this.examples, question)
                .stream()
                .map(this.processor)
                .collect(Collectors.collectingAndThen(Collectors.toList(), this.bulkTemplate::render));
    }

    /**
     * {@link DefaultExampleSelector} 的构建器。
     */
    public static class Builder {
        private final List<Example> examples = new ArrayList<>();
        private StringTemplate template;
        private List<String> input;
        private BiFunction<List<Example>, String, List<Example>> filter = (examples, ignore) -> examples;
        private String delimiter = "\n\n";

        /**
         * 向构建器中设置模板和关键词。
         *
         * @param template 表示使用 mustache 模板语法的 {@link String}。
         * @param questionKey 表示用户问题占位符的 {@link String}。
         * @param answerKey 表示用户答案占位符的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         * @throws IllegalArgumentException 当模板和输入占位符不匹配时。
         * @see <a href="https://mustache.github.io/">mustache</a>。
         */
        public Builder template(String template, String questionKey, String answerKey) {
            return this.template(StringTemplate.create(template), questionKey, answerKey);
        }

        /**
         * 向构建器中设置模板和关键词。
         *
         * @param template 表示字符串模板的 {@link StringTemplate}。
         * @param questionKey 表示用户问题占位符的 {@link String}。
         * @param answerKey 表示用户答案占位符的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         * @throws IllegalArgumentException 当模板和输入占位符不匹配时。
         */
        public Builder template(StringTemplate template, String questionKey, String answerKey) {
            notNull(template, "The template cannot be null.");
            this.input = Arrays.asList(questionKey, answerKey);
            Set<String> placeholder = template.placeholder();
            for (String arg : input) {
                Validation.isTrue(placeholder.contains(arg),
                        "Template '{0}' not match question key '{1}'.",
                        template,
                        arg);
            }
            this.template = template;
            return this;
        }

        /**
         * 向构建器中添加例子。
         *
         * @param example 表示例子数组的 {@link Example}{@code []}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        public Builder example(Example... example) {
            return this.example(Arrays.asList(example));
        }

        /**
         * 向构建器中添加例子。
         *
         * @param examples 表示例子列表的 {@link Example}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        public Builder example(List<Example> examples) {
            this.examples.addAll(examples);
            return this;
        }

        /**
         * 向构建器中设置过滤器。
         *
         * @param filter 表示例子过滤器的 {@link BiFunction}。
         * @return 表示当前构建器的 {@link Builder}。
         * @throws IllegalArgumentException 当 {@code filter} 为 {@code null} 时。
         */
        public Builder filter(BiFunction<List<Example>, String, List<Example>> filter) {
            this.filter = notNull(filter, "The filter cannot be null.");
            return this;
        }

        /**
         * 向构建器中设置分隔符。
         *
         * @param delimiter 表示分割符的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         * @throws IllegalArgumentException 当 {@code delimiter} 为 {@code null} 时。
         */
        public Builder delimiter(String delimiter) {
            this.delimiter = notNull(delimiter, "The delimiter cannot be null.");
            return this;
        }

        /**
         * 构建对象。
         *
         * @return 表示构建出来的 {@link DefaultExampleSelector}。
         */
        public DefaultExampleSelector build() {
            return new DefaultExampleSelector(this);
        }
    }
}