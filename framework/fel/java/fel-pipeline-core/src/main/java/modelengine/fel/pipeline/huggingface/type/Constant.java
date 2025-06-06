/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface.type;

import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 表示 huggingface pipeline 的常量集合。
 *
 * @author 易文渊
 * @since 2024-06-06
 */
public interface Constant {
    /**
     * 表示 {@link List}{@code <}{@link Media}{@code >} 的 {@link Type}。
     */
    Type LIST_MEDIA_TYPE = TypeUtils.parameterized(List.class, new Type[] {Media.class});
}