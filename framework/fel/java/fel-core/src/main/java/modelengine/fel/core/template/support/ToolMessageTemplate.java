/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template.support;

import static modelengine.fitframework.inspection.Validation.notBlank;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.support.ToolMessage;
import modelengine.fel.core.template.StringTemplate;
import modelengine.fitframework.resource.web.Media;

import java.util.List;

/**
 * 工具消息模板实现。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public class ToolMessageTemplate extends AbstractMessageTemplate {
    private final String id;

    /**
     * 使用 mustache 模板语法创建 {@link ToolMessageTemplate} 的实例。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @param id 表示工具调用唯一编号的 {@link String}。
     * @see <a href="https://mustache.github.io/">mustache</a>。
     */
    public ToolMessageTemplate(String template, String id) {
        this(StringTemplate.create(template), id);
    }

    /**
     * 使用字符串模板创建 {@link ToolMessageTemplate} 的实例。
     *
     * @param template 表示字符串模板的 {@link StringTemplate}。
     * @param id 表示工具调用唯一编号的 {@link String}。
     */
    public ToolMessageTemplate(StringTemplate template, String id) {
        super(template);
        this.id = notBlank(id, "The id cannot be blank.");
    }

    @Override
    protected ChatMessage collect(String text, List<Media> ignore) {
        return new ToolMessage(this.id, text);
    }
}