/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.tokenizer;

import java.util.List;

/**
 * 表示分词器的接口。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public interface Tokenizer {
    /**
     * 对给定的字符串进行分词。
     *
     * @param text 表示需要进行分词字符串的 {@link String}。
     * @return 表示分词结果的 {@link List}{@code <}{@link Integer}{@code >}。
     */
    List<Integer> encode(String text);

    /**
     * 对给定的分词结果进行解码。
     *
     * @param tokens 表示需要进行解码的 {@link List}{@code <}{@link Integer}{@code >}。
     * @return 表示解码后的字符串的 {@link String}。
     */
    String decode(List<Integer> tokens);

    /**
     * 计算分词数。
     *
     * @param text 表示需要进行分词字符串的 {@link String}。
     * @return 表示分词数的 {@code int}。
     */
    int countToken(String text);
}