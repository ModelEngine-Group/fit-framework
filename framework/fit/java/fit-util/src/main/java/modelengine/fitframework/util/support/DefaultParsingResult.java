/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util.support;

import modelengine.fitframework.util.ParsingResult;
import modelengine.fitframework.util.StringUtils;

/**
 * 为 {@link ParsingResult} 提供默认实现。
 *
 * @param <T> 表示结果的类型。
 * @author 梁济时
 * @since 2020-07-24
 */
public class DefaultParsingResult<T> implements ParsingResult<T> {
    private boolean parsed;
    private T result;

    /**
     * 使用表示是否解析成功的值及解析的结果初始化 {@link DefaultParsingResult} 类的新实例。
     *
     * @param parsed 若解析成功，则为 {@code true}；否则为 {@code false}。
     * @param result 表示解析的结果。
     */
    public DefaultParsingResult(boolean parsed, T result) {
        this.parsed = parsed;
        this.result = result;
    }

    @Override
    public boolean isParsed() {
        return this.parsed;
    }

    @Override
    public T getResult() {
        return this.result;
    }

    @Override
    public String toString() {
        return StringUtils.format("[parsed={0}, result={1}]", this.isParsed(), this.getResult());
    }
}
