/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.exceptions;

import modelengine.fit.waterflow.ErrorCodes;

/**
 * 参数错误抛出异常类。
 *
 * @author 陈镕希
 * @since 1.0
 */
public class WaterflowParamException extends WaterflowException {
    /**
     * 抛出Jobber参数错误异常。
     *
     * @param error 异常枚举的{@link ErrorCodes}。
     */
    public WaterflowParamException(ErrorCodes error) {
        super(error);
    }

    /**
     * 抛出Jobber参数错误异常。
     *
     * @param error 异常枚举的{@link ErrorCodes}。
     * @param args 额外参数。
     */
    public WaterflowParamException(ErrorCodes error, Object... args) {
        super(error, args);
    }
}
