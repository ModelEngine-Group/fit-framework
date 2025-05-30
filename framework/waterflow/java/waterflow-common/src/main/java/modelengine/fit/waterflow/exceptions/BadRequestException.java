/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.exceptions;

import modelengine.fit.waterflow.ErrorCodes;

/**
 * 错误请求异常类。
 *
 * @author 陈镕希
 * @since 1.0
 */
public class BadRequestException extends WaterflowException {
    /**
     * 抛出错误请求异常。
     *
     * @param error 异常枚举的{@link ErrorCodes}。
     */
    public BadRequestException(ErrorCodes error) {
        super(error);
    }

    /**
     * 抛出错误请求异常。
     *
     * @param error 异常枚举的{@link ErrorCodes}。
     * @param args 额外参数。
     */
    public BadRequestException(ErrorCodes error, Object... args) {
        super(error, args);
    }
}
