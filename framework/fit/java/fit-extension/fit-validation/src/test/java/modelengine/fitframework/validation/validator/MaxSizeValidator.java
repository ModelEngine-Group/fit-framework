/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.validator;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fitframework.validation.ConstraintValidator;
import modelengine.fitframework.validation.annotation.MaxSize;

import java.util.List;

/**
 * 表示单集合元素的测试校验类。
 *
 * @author 李金绪
 * @since 2025-03-17
 */
public class MaxSizeValidator implements ConstraintValidator<MaxSize, Object> {
    private long max;

    @Override
    public void initialize(MaxSize constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Object value) {
        if (value == null) {
            return true;
        }
        if (!(value instanceof List)) {
            return false;
        }
        List<Object> valueList = cast(value);
        return valueList.size() < this.max;
    }
}