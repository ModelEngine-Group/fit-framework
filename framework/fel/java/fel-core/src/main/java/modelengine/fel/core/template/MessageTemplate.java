/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.document.Content;

import java.util.Map;

/**
 * 消息模板接口定义。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public interface MessageTemplate extends GenericTemplate<Map<String, Content>, ChatMessage> {}