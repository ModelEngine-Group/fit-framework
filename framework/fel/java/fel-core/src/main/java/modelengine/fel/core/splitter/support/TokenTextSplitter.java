/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.splitter.support;

import modelengine.fel.core.pattern.Splitter;
import modelengine.fel.core.splitter.AbstractTextSplitter;
import modelengine.fel.core.tokenizer.Tokenizer;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 表示根据 token 数量进行分割的 {@link Splitter}。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public class TokenTextSplitter extends AbstractTextSplitter {
    private static final int DEFAULT_CHUNK_SIZE = 400;
    private static final int DEFAULT_CHUNK_OVERLAP = 200;

    /**
     * 分块大小。
     */
    protected final int chunkSize;

    /**
     * 重叠大小。
     */
    protected final int chunkOverlap;

    private final Tokenizer tokenizer;

    /**
     * 使用默认的分块大小和重叠大小创建 {@link TokenTextSplitter} 的实例。
     *
     * @param tokenizer 表示分词器的 {@link Tokenizer}。
     */
    public TokenTextSplitter(Tokenizer tokenizer) {
        this(tokenizer, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }

    /**
     * 使用指定的分块大小和重叠大小创建 {@link TokenTextSplitter} 的实例。
     *
     * @param tokenizer 表示分词器的 {@link Tokenizer}。
     * @param chunkSize 表示分块大小的整数。
     * @param chunkOverlap 表示分块重叠大小的整数。
     */
    public TokenTextSplitter(Tokenizer tokenizer, int chunkSize, int chunkOverlap) {
        Validation.greaterThan(chunkSize,
                chunkOverlap,
                "The chunk size `{0}` must greater than chunk overlap `{1}`.",
                chunkSize,
                chunkOverlap);
        this.tokenizer = tokenizer;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    @Override
    protected List<String> splitText(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        List<String> splits = new ArrayList<>();
        List<Integer> inputTokens = tokenizer.encode(text);
        int step = chunkSize - chunkOverlap;
        int startIndex = 0;
        int curIndex = Math.min(startIndex + this.chunkSize, inputTokens.size());
        List<Integer> chunkTokens = inputTokens.subList(startIndex, curIndex);
        while (startIndex < inputTokens.size()) {
            splits.add(tokenizer.decode(chunkTokens));
            if (curIndex == inputTokens.size()) {
                break;
            }
            startIndex += step;
            curIndex = Math.min(startIndex + this.chunkSize, inputTokens.size());
            chunkTokens = inputTokens.subList(startIndex, curIndex);
        }
        return splits;
    }
}