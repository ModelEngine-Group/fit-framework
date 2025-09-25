/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document;

import modelengine.fel.core.pattern.Synthesizer;

import java.util.List;

/**
 * 表示文档检索结果的合成器。
 *
 * @author 易文渊
 * @since 2024-08-12
 */
public interface DocumentSynthesizer extends Synthesizer<List<? extends Document>> {}