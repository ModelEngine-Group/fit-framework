/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.entity;

import java.util.Map;

/**
 * Represents a JSON RPC request entity, encapsulating information related to JSON RPC requests.
 * This class follows the JSON RPC specification, supporting the construction and parsing of JSON RPC request objects.
 *
 * @author 季聿阶
 * @since 2025-05-15
 */
public class JsonRpcEntity {
    private String jsonrpc = "2.0";
    private String method;
    private Object id;
    private Map<String, Object> params;
    private Object result;
    private Object error;

    /**
     * Gets the JSON RPC version used in the request or response.
     *
     * @return The JSON RPC version as a {@link String}, typically "2.0".
     */
    public String getJsonrpc() {
        return this.jsonrpc;
    }

    /**
     * Sets the JSON RPC version used in the request or response.
     *
     * @param jsonrpc The JSON RPC version as a {@link String}, typically "2.0".
     */
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    /**
     * Gets the method name to be invoked in the JSON RPC request.
     *
     * @return The method name as a {@link String}.
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Sets the method name to be invoked in the JSON RPC request.
     *
     * @param method The method name as a {@link String}.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the identifier used to correlate the request with its corresponding response.
     *
     * @return The identifier as an {@link Object}, which can be of type {@link String}, {@link Number}, or null.
     */
    public Object getId() {
        return this.id;
    }

    /**
     * Sets the identifier used to correlate the request with its corresponding response.
     *
     * @param id The identifier as an {@link Object}, which can be of type {@link String}, {@link Number}, or null.
     */
    public void setId(Object id) {
        this.id = id;
    }

    /**
     * Gets the parameters associated with the JSON RPC method call.
     *
     * @return A map containing the parameters as a {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     */
    public Map<String, Object> getParams() {
        return this.params;
    }

    /**
     * Sets the parameters associated with the JSON RPC method call.
     *
     * @param params A map containing the parameters as a
     * {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Gets the result returned by the JSON RPC method execution.
     *
     * @return The result as an {@link Object}, which can be of any type depending on the method implementation.
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Sets the result returned by the JSON RPC method execution.
     *
     * @param result The result as an {@link Object}, which can be of any type depending on the method implementation.
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * Gets the error information if the JSON RPC method execution resulted in an error.
     *
     * @return The error information as an {@link Object}, or null if no error occurred.
     */
    public Object getError() {
        return this.error;
    }

    /**
     * Sets the error information indicating that the JSON RPC method execution resulted in an error.
     *
     * @param error The error information as an {@link Object}, or null if no error occurred.
     */
    public void setError(Object error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "JsonRpcEntity{" + "jsonrpc='" + this.jsonrpc + '\'' + ", method='" + this.method + '\'' + ", id="
                + this.id + ", params=" + this.params + ", result=" + this.result + ", error=" + this.error + '}';
    }
}
