/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template.support;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.support.SystemMessage;
import modelengine.fel.core.template.StringTemplate;
import modelengine.fitframework.resource.web.Media;

import java.util.List;

/**
 * 系统消息模板实现。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public class SystemMessageTemplate extends AbstractMessageTemplate {
    /**
     * 使用 mustache 模板语法创建 {@link SystemMessageTemplate} 的实例。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @see <a href="https://mustache.github.io/">mustache</a>。
     */
    public SystemMessageTemplate(String template) {
        this(StringTemplate.create(template));
    }

    /**
     * 使用字符串模板创建 {@link SystemMessageTemplate} 的实例。
     *
     * @param template 表示字符串模板的 {@link StringTemplate}。
     */
    public SystemMessageTemplate(StringTemplate template) {
        super(template);
    }

    @Override
    protected ChatMessage collect(String text, List<Media> ignore) {
        return new SystemMessage(text);
    }
}