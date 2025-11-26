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
 * @param action The action to take
 * @param content The user-provided data matching the requested schema
 * @author 黄可欣
 * @since 2025-11-25
 */
public record ElicitResult(Action action, Map<String, Object> content) {
    /**
     * Action types for elicitation results.
     */
    public enum Action {
        ACCEPT,
        DECLINE,
        CANCEL
    }
}
