/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.source.support;

import modelengine.fel.core.source.JsonMetadataExtractor;
import modelengine.fitframework.inspection.Validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示 {@link JsonMetadataExtractor} 的默认实现。
 *
 * @author 易文渊
 * @since 2024-08-10
 */
public class DefaultMetadataExtractor implements JsonMetadataExtractor {
    private final List<String> includeKey;

    /**
     * 根据需要包含的键，构造默认元数据提取器。
     *
     * @param includeKey 表示需要提取的键列表的 {@link List}{@code <}{@link String}{@code >}。
     */
    public DefaultMetadataExtractor(List<String> includeKey) {
        this.includeKey = includeKey;
    }

    @Override
    public Map<String, Object> apply(Map<String, Object> json) {
        Map<String, Object> output = new HashMap<>();
        for (String key : includeKey) {
            Object value = Validation.notNull(json.get(key), "The key: {0} is missing.", key);
            output.put(key, value);
        }
        return output;
    }
}