/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.chat;

import modelengine.fitframework.flowable.Choir;

/**
 * 表示聊天模型推理服务。
 *
 * @author 易文渊
 * @since 2024-04-13
 */
public interface ChatModel {
    /**
     * 调用聊天模型生成结果。
     *
     * @param prompt 表示提示词的 {@link Prompt}。
     * @param chatOption 表示聊天模型参数的 {@link ChatOption}。
     * @return 表示聊天模型生成结果的 {@link Choir}{@code <}{@link ChatMessage}{@code >}。
     */
    Choir<ChatMessage> generate(Prompt prompt, ChatOption chatOption);
}