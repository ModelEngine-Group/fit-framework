/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.source;

import java.util.Map;
import java.util.function.Function;

/**
 * 表示 json 文件元数据萃取器接口。
 *
 * @author 易文渊
 * @since 2024-08-10
 */
public interface JsonMetadataExtractor extends Function<Map<String, Object>, Map<String, Object>> {}