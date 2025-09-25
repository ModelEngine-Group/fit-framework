/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template.support;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.document.Content;
import modelengine.fel.core.template.MessageTemplate;
import modelengine.fel.core.template.StringTemplate;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表示 {@link MessageTemplate} 的抽象实现。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public abstract class AbstractMessageTemplate implements MessageTemplate {
    private final StringTemplate template;

    /**
     * 使用字符串模板创建 {@link AbstractMessageTemplate} 的实例。
     *
     * @param template 表示字符串模板的 {@link StringTemplate}。
     */
    protected AbstractMessageTemplate(StringTemplate template) {
        this.template = template;
    }

    @Override
    public ChatMessage render(Map<String, Content> values) {
        Map<String, Content> args = ObjectUtils.getIfNull(values, Collections::emptyMap);
        String text = args.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.collectingAndThen(Collectors.toMap(Entry::getKey, e -> e.getValue().text()),
                        this.template::render));
        List<Media> medias = this.template.placeholder()
                .stream()
                .map(args::get)
                .filter(Objects::nonNull)
                .flatMap(cs -> cs.medias().stream())
                .collect(Collectors.toList());
        return this.collect(text, medias);
    }

    @Override
    public Set<String> placeholder() {
        return this.template.placeholder();
    }

    /**
     * 收集消息内容生成 {@link ChatMessage}。
     *
     * @param text 表示文本内容的 {@link String}。
     * @param medias 表示媒体内容列表的 {@link List}{@code <}{@link String}{@code >}。
     * @return 返回表示聊天消息的 {@link ChatMessage}。
     */
    protected abstract ChatMessage collect(String text, List<Media> medias);
}