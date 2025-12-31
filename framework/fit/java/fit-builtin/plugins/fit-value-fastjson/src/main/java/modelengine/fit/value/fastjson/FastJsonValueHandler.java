/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.value.fastjson;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.value.ValueFetcher;
import modelengine.fitframework.value.ValueSetter;

/**
 * {@link ValueFetcher} 的 fastjson 的实现。
 *
 * @author 季聿阶
 * @since 2022-08-04
 */
@Component
public class FastJsonValueHandler implements ValueFetcher, ValueSetter {
    private static final String $ = "$";

    @Override
    public Object fetch(Object object, String propertyPath) {
        if (object == null) {
            return null;
        }
        if (isObjectSelf(propertyPath)) {
            return object;
        }
        if (object instanceof String) {
            return null;
        }
        try {
            return JSONPath.eval(object, this.getParsedPath(propertyPath));
        } catch (JSONException e) {
            throw new IllegalArgumentException(StringUtils.format(
                    "Failed to fetch value by JSONPath. [propertyPath={0}]",
                    propertyPath), e);
        }
    }

    @Override
    public Object set(Object object, String propertyPath, Object value) {
        if (object == null) {
            return null;
        }
        if (isObjectSelf(propertyPath)) {
            return value;
        }
        if (object instanceof String) {
            return object;
        }
        try {
            JSONPath.set(object, this.getParsedPath(propertyPath), value);
            return object;
        } catch (JSONException e) {
            throw new IllegalArgumentException(StringUtils.format("Failed to set value by JSONPath. [propertyPath={0}]",
                    propertyPath), e);
        }
    }

    private static boolean isObjectSelf(String propertyPath) {
        return StringUtils.isBlank(propertyPath) || $.equals(propertyPath);
    }

    private String getParsedPath(String propertyPath) {
        if (propertyPath.startsWith($)) {
            return propertyPath;
        } else if (propertyPath.startsWith("[")) {
            return $ + propertyPath;
        } else {
            return $ + "." + propertyPath;
        }
    }
}
