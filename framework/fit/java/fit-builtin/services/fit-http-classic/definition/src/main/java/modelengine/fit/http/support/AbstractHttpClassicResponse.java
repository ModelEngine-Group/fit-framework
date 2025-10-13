/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.support;

import static modelengine.fit.http.protocol.MessageHeaderNames.SET_COOKIE;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.HttpClassicResponse;
import modelengine.fit.http.HttpResource;
import modelengine.fit.http.protocol.MessageHeaders;
import modelengine.fit.http.protocol.RequestLine;
import modelengine.fit.http.protocol.StatusLine;
import modelengine.fit.http.util.HttpUtils;

import java.util.List;

/**
 * {@link HttpClassicResponse} 的默认实现。
 *
 * @author 季聿阶
 * @since 2022-07-21
 */
public abstract class AbstractHttpClassicResponse extends AbstractHttpMessage implements HttpClassicResponse {
    private final StatusLine startLine;

    /**
     * 创建经典的 Http 响应对象。
     *
     * @param httpResource 表示 Http 的资源的 {@link HttpResource}。
     * @param startLine 表示 Http 请求的起始行的 {@link RequestLine}。
     * @param headers 表示只读的 Http 消息头集合的 {@link MessageHeaders}。
     */
    public AbstractHttpClassicResponse(HttpResource httpResource, StatusLine startLine, MessageHeaders headers) {
        super(httpResource, startLine, headers);
        this.startLine = notNull(startLine, "The status line cannot be null.");
        notNull(headers, "The headers cannot be null.");
        List<String> actualCookies = headers.all(SET_COOKIE);
        actualCookies.stream().map(HttpUtils::parseSetCookie).forEach(this.cookies()::add);
    }

    @Override
    public int statusCode() {
        return this.startLine.statusCode();
    }

    @Override
    public String reasonPhrase() {
        return this.startLine.reasonPhrase();
    }
}
