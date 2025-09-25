/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.tokenizer.hanlp;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fitframework.util.StringUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 表示 {@link HanlpTokenizer} 的测试集。
 *
 * @author 易文渊
 * @since 2024-09-24
 */
@DisplayName("测试 hanlpTokenizer")
public class HanlpTokenizerTest {
    @Test
    @DisplayName("测试分词")
    void testCountToken() {
        HanlpTokenizer tokenizer = new HanlpTokenizer();
        assertThat(tokenizer.countToken(StringUtils.EMPTY)).isEqualTo(0);
        assertThat(tokenizer.countToken("你好")).isEqualTo(1);
    }
}