/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format.json;

import modelengine.fel.core.format.MarkdownCompatibleParser;
import modelengine.fel.core.format.OutputParser;
import modelengine.fitframework.serialization.ObjectSerializer;

import java.lang.reflect.Type;

/**
 * 表示 json 解析器的接口。
 *
 * @param <O> 表示输出对象类型。
 * @author 易文渊
 * @since 2024-04-28
 */
public interface JsonOutputParser<O> extends OutputParser<O> {
    /**
     * 创建默认 json 解析器实例。
     *
     * @param serializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @param type 表示输出类型 {@link E} 的 {@link Type}。
     * @param <E> 表示输出对象类型。
     * @return 表示默认 json 解析器的 {@link OutputParser}。
     */
    static <E> OutputParser<E> create(ObjectSerializer serializer, Type type) {
        OutputParser<E> outputParser = new BeanJsonOutputParser<>(serializer, type, null);
        return new MarkdownCompatibleParser<>(outputParser, "json");
    }

    /**
     * 创建默认 json 片段解析器实例。
     *
     * @param serializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @param type 表示输出类型 {@link E} 的 {@link Type}。
     * @param <E> 表示输出对象类型。
     * @return 表示默认 json 片段解析器的 {@link OutputParser}。
     */
    static <E> OutputParser<E> createPartial(ObjectSerializer serializer, Type type) {
        OutputParser<E> outputParser =
                new PartialJsonOutputParser<>(new BeanJsonOutputParser<>(serializer, type, null));
        return new MarkdownCompatibleParser<>(outputParser, "json");
    }
}