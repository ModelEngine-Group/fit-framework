/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.elicitation;

import java.util.Map;

/**
 * Represents the result of handling an elicitation request.
 * This is a simplified version that doesn't depend on MCP SDK types.
 *
 * @param action The {@link ElicitResult.Action} to take in elicitation result.
 * @param content The elicitation result {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >} data
 * matching the requested schema.
 * @author 黄可欣
 * @see <a href=https://modelcontextprotocol.io/specification/2025-06-18/client/elicitation#protocol-messages>MCP
 * Protocol</a>
 * @since 2025-11-25
 */
public record ElicitResult(Action action, Map<String, Object> content) {
    /**
     * Action types for elicitation results.
     */
    public enum Action {
        /**
         * User explicitly approved and submitted with data.
         */
        ACCEPT,

        /**
         * User explicitly declined the request.
         */
        DECLINE,

        /**
         * User dismissed without making an explicit choice.
         */
        CANCEL
    }
}
