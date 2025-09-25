/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fitframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表示实现从 markdown 格式字符串解析的  {@link OutputParser}。
 *
 * @param <O> 表示输出对象类型。
 * @author 易文渊
 * @since 2024-05-10
 */
public class MarkdownCompatibleParser<O> implements OutputParser<O> {
    private final OutputParser<O> outputParser;
    private final Pattern pattern;

    /**
     * 使用输出解析器创建 {@link MarkdownCompatibleParser} 的实例。
     *
     * @param outputParser 表示对象解析器的 {@link OutputParser}。
     * @param prefix 表示代码类型的 {@link String}。
     * @throws IllegalArgumentException <ul>
     * <li>当 {@code outputParser} 为 {@code null} 时；</li>
     * <li>当 {@code prefix} 为空字符串时。</li>
     * </ul>
     */
    public MarkdownCompatibleParser(OutputParser<O> outputParser, String prefix) {
        notBlank(prefix, "The prefix cannot be blank.");
        this.outputParser = notNull(outputParser, "The parser cannot be null.");
        this.pattern = Pattern.compile("```" + prefix + "([\\s\\S]*)");
    }

    @Override
    public String instruction() {
        return outputParser.instruction();
    }

    @Override
    public O parse(String input) {
        Matcher m = this.pattern.matcher(input);
        String json = m.find() ? m.group(1) : input;
        return this.outputParser.parse(StringUtils.trimEnd(json, '`').trim());
    }
}