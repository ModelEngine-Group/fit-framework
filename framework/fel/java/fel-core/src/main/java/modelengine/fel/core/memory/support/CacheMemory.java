/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.memory.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.core.template.BulkStringTemplate;
import modelengine.fel.core.template.support.DefaultBulkStringTemplate;
import modelengine.fitframework.util.MapBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表示 {@link Memory} 的简单内存实现，不要在生产环境中使用。
 *
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-05-08
 */
public class CacheMemory implements Memory {
    private List<ChatMessage> messages = new ArrayList<>();
    private final BulkStringTemplate bulkTemplate;
    private final Function<ChatMessage, Map<String, String>> extractor;

    /**
     * 设置内存中构建器的默认模板和关键词。
     *
     * @see <a href="https://mustache.github.io/">mustache</a>。
     */
    public CacheMemory() {
        this(new DefaultBulkStringTemplate("{{type}}:{{text}}", "\n"),
                message -> MapBuilder.<String, String>get()
                        .put("type", message.type().getRole())
                        .put("text", message.text())
                        .build());
    }

    /**
     * 设置内存中构建器的模板和关键词。
     *
     * @param bulkTemplate 表示批量字符串模板 {@link BulkStringTemplate}。
     * @param extractor 表示将 {@link ChatMessage} 转换成
     * {@link Map}{@code <}{@link String}, {@link String}{@code >} 的处理函数。
     * @throws IllegalArgumentException 当 {@code bulkTemplate}、{@code extractor} 为 {@code null} 时。
     */
    public CacheMemory(BulkStringTemplate bulkTemplate, Function<ChatMessage, Map<String, String>> extractor) {
        this.bulkTemplate = notNull(bulkTemplate, "The bulkTemplate cannot be null.");
        this.extractor = notNull(extractor, "The extractor cannot be null.");
    }

    @Override
    public void add(ChatMessage message) {
        this.messages.add(message);
    }

    @Override
    public void set(List<ChatMessage> messages) {
        this.messages = new ArrayList<>(messages);
    }

    @Override
    public void clear() {
        this.messages.clear();
    }

    @Override
    public List<ChatMessage> messages() {
        return Collections.unmodifiableList(this.messages);
    }

    @Override
    public String text() {
        return this.messages.stream()
                .map(this.extractor)
                .collect(Collectors.collectingAndThen(Collectors.toList(), this.bulkTemplate::render));
    }
}