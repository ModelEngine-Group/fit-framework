/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.splitter.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示 {@link Tokenizer} 的简单实现。
 *
 * @author 易文渊
 * @since 2024-08-09
 */
public class SimpleTokenizer implements Tokenizer {
    @Override
    public List<Integer> encode(String text) {
        char[] charArray = text.toCharArray();
        List<Integer> tokens = new ArrayList<>();
        for (char ch : charArray) {
            tokens.add((int) ch);
        }
        return tokens;
    }

    @Override
    public String decode(List<Integer> tokens) {
        char[] charArray = new char[tokens.size()];
        for (int i = 0; i < tokens.size(); ++i) {
            charArray[i] = (char) tokens.get(i).intValue();
        }
        return new String(charArray);
    }

    @Override
    public int countToken(String text) {
        notNull(text, "Text cannot be null.");
        return text.length();
    }
}