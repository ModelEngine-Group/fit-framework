/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template;

import java.util.List;
import java.util.Map;

/**
 * 批量字符串模板接口定义。
 *
 * @author 何嘉斌
 * @since 2024-05-13
 */
public interface BulkStringTemplate extends GenericTemplate<List<Map<String, String>>, String> {}