/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.fewshot.support;

import static modelengine.fitframework.inspection.Validation.notBlank;

import modelengine.fel.core.fewshot.Example;

/**
 * 表示 {@link Example} 的默认实现。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public class DefaultExample implements Example {
    private final String question;
    private final String answer;

    /**
     * 使用问答对创建 {@link DefaultExample} 的实例。
     *
     * @param question 表示问题的 {@link String}。
     * @param answer 表示答案的 {@link String}。
     * @throws IllegalArgumentException 当 {@code question}、{@code answer} 为 {@code null} 时。
     */
    public DefaultExample(String question, String answer) {
        this.question = notBlank(question, "The question cannot be blank.");
        this.answer = notBlank(answer, "The answer cannot be blank.");
    }

    @Override
    public String question() {
        return this.question;
    }

    @Override
    public String answer() {
        return this.answer;
    }
}