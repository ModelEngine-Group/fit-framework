/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.parameter;

import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestCookie;
import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fit.http.annotation.RequestParam;

import org.junit.jupiter.api.DisplayName;

/**
 * 参数测试模型类。
 *
 * @author 白鹏坤
 * @since 2023-02-24
 */
@DisplayName("参数测试模型类")
abstract class HttpParamTest {
    /**
     * 表示测试用的查询参数或表单参数（指定 name 属性）。
     *
     * @param str 表示查询参数或表单参数的 {@link String}。
     */
    protected abstract void requestParam(@RequestParam(name = "p1") String str);

    /**
     * 表示测试用的查询参数或表单参数（使用参数名作为 fallback）。
     *
     * @param username 表示查询参数或表单参数的 {@link String}。
     */
    protected abstract void requestParamWithParameterName(@RequestParam String username);

    /**
     * 表示测试用的查询参数或表单参数（使用 value 属性）。
     *
     * @param str 表示查询参数或表单参数的 {@link String}。
     */
    protected abstract void requestParamWithValue(@RequestParam("user_id") String str);

    /**
     * 表示测试用的消息头参数。
     *
     * @param str 表示消息头参数的 {@link String}。
     */
    protected abstract void requestHeader(@RequestHeader(name = "h1") String str);

    /**
     * 表示测试用的消息体参数。
     *
     * @param str 表示消息体参数的 {@link String}。
     */
    protected abstract void requestBody(@RequestBody(required = false) String str);

    /**
     * 表示测试用的路径参数。
     *
     * @param str 表示路径参数的 {@link String}。
     */
    protected abstract void pathVariable(@PathVariable(name = "v1") String str);

    /**
     * 表示测试用的 Cookie 参数。
     *
     * @param str 表示路径参数的 {@link String}。
     */
    protected abstract void cookieValue(@RequestCookie(name = "c1") String str);
}
