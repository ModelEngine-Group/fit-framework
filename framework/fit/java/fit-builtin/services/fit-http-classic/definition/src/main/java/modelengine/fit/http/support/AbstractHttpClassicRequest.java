/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.support;

import static modelengine.fit.http.protocol.MessageHeaderNames.HOST;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.HttpClassicRequest;
import modelengine.fit.http.HttpResource;
import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fit.http.protocol.MessageHeaderNames;
import modelengine.fit.http.protocol.MessageHeaders;
import modelengine.fit.http.protocol.QueryCollection;
import modelengine.fit.http.protocol.RequestLine;

/**
 * 表示 {@link HttpClassicRequest} 的抽象实现类。
 *
 * @author 季聿阶
 * @since 2022-11-23
 */
public abstract class AbstractHttpClassicRequest extends AbstractHttpMessage implements HttpClassicRequest {
    private final RequestLine startLine;
    private final MessageHeaders headers;

    /**
     * 创建经典的 Http 请求对象。
     *
     * @param httpResource 表示 Http 的资源的 {@link HttpResource}。
     * @param startLine 表示 Http 请求的起始行的 {@link RequestLine}。
     * @param headers 表示只读的 Http 消息头集合的 {@link MessageHeaders}。
     */
    public AbstractHttpClassicRequest(HttpResource httpResource, RequestLine startLine, MessageHeaders headers) {
        super(httpResource, startLine, headers);
        this.startLine = notNull(startLine, "The request line cannot be null.");
        this.headers = notNull(headers, "The message headers cannot be null.");
    }

    @Override
    public HttpRequestMethod method() {
        return this.headers.first(MessageHeaderNames.X_HTTP_METHOD_OVERRIDE)
                .map(HttpRequestMethod::from)
                .orElse(this.startLine.method());
    }

    @Override
    public String requestUri() {
        return this.startLine.requestUri();
    }

    @Override
    public String host() {
        return this.headers.first(HOST).orElse(null);
    }

    @Override
    public String path() {
        return this.startLine.requestUri();
    }

    @Override
    public QueryCollection queries() {
        return this.startLine.queries();
    }
}
