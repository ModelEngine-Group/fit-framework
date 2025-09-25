/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.models;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.pattern.Model;
import modelengine.fit.waterflow.bridge.fitflow.FitBoundedEmitter;

/**
 * 流式模型。
 *
 * @param <O> 表示对话模型的输出类型。
 * @author 刘信宏
 * @since 2024-04-16
 */
public interface FlowModel<I, O> extends Model<I, FitBoundedEmitter<O, ChatMessage>> {}
