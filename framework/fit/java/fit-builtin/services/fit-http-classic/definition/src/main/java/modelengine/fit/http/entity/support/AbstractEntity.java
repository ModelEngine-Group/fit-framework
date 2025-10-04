/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.entity.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.HttpMessage;
import modelengine.fit.http.entity.Entity;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * 表示 {@link Entity} 的抽象实现。
 * <p>每一个 {@link Entity} 都必然属于一个 {@link HttpMessage}。</p>
 *
 * @author 季聿阶
 * @since 2022-08-03
 */
public abstract class AbstractEntity implements Entity {
    private final HttpMessage httpMessage;

    /**
     * 通过 Http 消息来实例化 {@link AbstractEntity}。
     *
     * @param httpMessage 表示 Http 消息的 {@link HttpMessage}。
     * @throws IllegalArgumentException 当 {@code httpMessage} 为 {@code null} 时。
     */
    protected AbstractEntity(HttpMessage httpMessage) {
        this.httpMessage = notNull(httpMessage, "The http message cannot be null.");
    }

    @Override
    public HttpMessage belongTo() {
        return this.httpMessage;
    }

    @Override
    public Map<String, String> resolvedParameters() {
        return Collections.emptyMap();
    }

    @Override
    public void close() throws IOException {}
}
