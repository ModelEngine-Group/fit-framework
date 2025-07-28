/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

import java.util.Map;

/**
 * Represents a JSON RPC request entity, encapsulating information related to JSON RPC requests.
 * This class follows the JSON RPC specification, supporting the construction and parsing of JSON RPC request objects.
 *
 * @since 2025-05-15
 */
public class JsonRpc {
    /**
     * Creates a JSON RPC request with the specified ID and method.
     *
     * @param id The unique identifier for the request.
     * @param method The method name to be invoked.
     * @param <T> The type of the request ID.
     * @return A JSON RPC request object.
     */
    public static <T> Request<T> createRequest(T id, String method) {
        return new Request<>("2.0", id, method, null);
    }

    /**
     * Creates a JSON RPC request with the specified ID, method, and parameters.
     *
     * @param id The unique identifier for the request.
     * @param method The method name to be invoked.
     * @param params The parameters associated with the method.
     * @param <T> The type of the request ID.
     * @return A JSON RPC request object.
     */
    public static <T> Request<T> createRequest(T id, String method, Object params) {
        return new Request<>("2.0", id, method, params);
    }

    /**
     * Creates a JSON RPC response with the specified ID and result.
     *
     * @param id The unique identifier for the response.
     * @param result The result of the request.
     * @param <T> The type of the response ID.
     * @return A JSON RPC response object.
     */
    public static <T> Response<T> createResponse(T id, Object result) {
        return new Response<>("2.0", id, result, null);
    }

    /**
     * Creates a JSON RPC response with the specified ID and error.
     *
     * @param id The unique identifier for the response.
     * @param error The error associated with the request.
     * @param <T> The type of the response ID.
     * @return A JSON RPC response object.
     */
    public static <T> Response<T> createResponseWithError(T id, Object error) {
        return new Response<>("2.0", id, null, error);
    }

    /**
     * Creates a JSON RPC notification with the specified method.
     *
     * @param method The method name to be invoked.
     * @return A JSON RPC notification object.
     */
    public static Notification createNotification(String method) {
        return new Notification("2.0", method, null);
    }

    /**
     * Creates a JSON RPC notification with the specified method and parameters.
     *
     * @param method The method name to be invoked.
     * @param params The parameters associated with the method.
     * @return A JSON RPC notification object.
     */
    public static Notification createNotification(String method, Map<String, Object> params) {
        return new Notification("2.0", method, params);
    }

    /**
     * Represents a JSON RPC request.
     *
     * @param <T> The type of the request ID.
     */
    public record Request<T>(String jsonrpc, T id, String method, Object params) {}

    /**
     * Represents a JSON RPC response.
     *
     * @param <T> The type of the response ID.
     */
    public record Response<T>(String jsonrpc, T id, Object result, Object error) {}

    /**
     * Represents a JSON RPC notification.
     */
    public record Notification(String jsonrpc, String method, Map<String, Object> params) {}
}
