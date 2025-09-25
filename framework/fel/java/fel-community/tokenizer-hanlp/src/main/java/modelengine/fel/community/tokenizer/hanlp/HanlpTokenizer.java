/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.tokenizer.hanlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;

import modelengine.fel.core.tokenizer.Tokenizer;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.util.StringUtils;

import java.util.List;

/**
 * 表示 {@link Tokenizer} 的 hanlp 实现。
 *
 * @author 易文渊
 * @since 2024-09-24
 */
@Component
public class HanlpTokenizer implements Tokenizer {
    private final Segment segment = HanLP.newSegment().enablePartOfSpeechTagging(false).enableOffset(false);

    @Override
    public List<Integer> encode(String text) {
        throw new UnsupportedOperationException("The operator encode is not support.");
    }

    @Override
    public String decode(List<Integer> tokens) {
        throw new UnsupportedOperationException("The operator decode is not support.");
    }

    @Override
    public int countToken(String text) {
        return StringUtils.isBlank(text) ? 0 : segment.seg(text).size();
    }
}