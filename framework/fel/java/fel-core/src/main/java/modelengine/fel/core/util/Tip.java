/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.util;

import modelengine.fel.core.document.Content;
import modelengine.fel.core.template.MessageTemplate;
import modelengine.fitframework.inspection.Validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 为 {@link MessageTemplate} 提供参数数据。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public class Tip {
    private final Map<String, Content> values = new HashMap<>();
    private int index = 0;

    @Override
    public String toString() {
        return this.values.toString();
    }

    /**
     * 从键值对创建 {@link Tip} 的实例。
     *
     * @param key 表示占位符的 {@link String}。
     * @param value 表示替换值的 {@link String}。
     * @return 返回创建完成的 {@link Tip}。
     */
    public static Tip from(String key, String value) {
        return new Tip().add(key, value);
    }

    /**
     * 从键值对创建 {@link Tip} 的实例。
     *
     * @param key 表示占位符的 {@link String}。
     * @param value 表示替换值的 {@link Content}。
     * @return 返回创建完成的 {@link Tip}。
     */
    public static Tip from(String key, Content value) {
        return new Tip().add(key, value);
    }

    /**
     * 从字符串数组创建 {@link Tip} 的实例，占位符为加入时的序列号。
     *
     * @param tips 表示替换值数组的 {@link String}{@code []}。
     * @return 返回创建完成的 {@link Tip}。
     */
    public static Tip fromArray(String... tips) {
        return Arrays.stream(tips).reduce(new Tip(), Tip::add, Tip::merge);
    }

    /**
     * 添加字符串值，占位符为加入时的序列号。
     *
     * @param value 表示替换值的 {@link String}。
     * @return 表示当前的 {@link Tip}。
     */
    public Tip add(String value) {
        return this.add(String.valueOf(this.index), value);
    }

    /**
     * 添加字符串值。
     *
     * @param key 表示占位符的 {@link String}。
     * @param value 表示替换值的 {@link String}。
     * @return 表示当前的 {@link Tip}。
     */
    public Tip add(String key, String value) {
        return this.add(key, Content.from(value));
    }

    /**
     * 添加消息内容。
     *
     * @param key 表示占位符的 {@link String}。
     * @param value 表示替换值的 {@link Content}。
     * @return 表示当前的 {@link Tip}。
     */
    public Tip add(String key, Content value) {
        Validation.notBlank(key, "The key cannot be blank");
        Validation.notNull(this.values, () -> new IllegalStateException("The tip has been freeze."));
        this.values.put(key, value);
        this.index++;
        return this;
    }

    /**
     * 批量添加 {@link Map}{@code <}{@link String}{@code ,} {@link Content}{@code >} 中的数据。
     *
     * @param args 表示参数集合的 {@link Map}{@code <}{@link String}{@code ,} {@link Content}{@code >}。
     * @return 表示当前的 {@link Tip}。
     * @throws IllegalStateException 当 {@code args} 为 {@code null}时。
     */
    public Tip addAll(Map<String, Content> args) {
        Validation.notNull(args, () -> new IllegalStateException("The input map cannot be null."));
        args.forEach(this::add);
        return this;
    }

    /**
     * 合并另一个 {@link Tip}。
     *
     * @param other 表示另一个 {@link Tip}。
     * @return 表示当前的 {@link Tip}。
     */
    public Tip merge(Tip other) {
        return this.addAll(other.values);
    }

    /**
     * 获取参数数据。
     *
     * @return 返回表示参数数据的 {@link Map}{@code <}{@link String}{@code ,} {@link Content}{@code >}。
     */
    public Map<String, Content> freeze() {
        return Collections.unmodifiableMap(this.values);
    }
}