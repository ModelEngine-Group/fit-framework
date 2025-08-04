/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

/**
 * 表示调用 MCP 的结果。
 *
 * @author 季聿阶
 * @since 2025-08-04
 */
public class Result {
    private final boolean success;
    private final Object content;
    private final String error;

    private Result(boolean success, Object content, String error) {
        this.success = success;
        this.content = content;
        this.error = error;
    }

    /**
     * 创建一个成功的结果。
     *
     * @param content 表示成功结果的内容的 {@link Object}。
     * @return 表示成功结果的对象的 {@link Result}。
     */
    public static Result success(Object content) {
        return new Result(true, content, null);
    }

    /**
     * 创建一个失败的结果。
     *
     * @param error 表示错误结果的信息的 {@link String}。
     * @return 表示错误结果的对象的 {@link Result}。
     */
    public static Result error(String error) {
        return new Result(false, null, error);
    }

    /**
     * 获取结果是否成功。
     *
     * @return 如果结果成功，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * 获取结果内容。
     *
     * @return 表示结果内容的 {@link Object}。
     */
    public Object getContent() {
        return this.content;
    }

    /**
     * 获取结果错误信息。
     *
     * @return 表示错误信息的 {@link String}。
     */
    public String getError() {
        return this.error;
    }
}
