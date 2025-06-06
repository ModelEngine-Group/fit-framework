/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.exceptions;

import modelengine.fit.waterflow.ErrorCodes;
import modelengine.fitframework.exception.FitException;

import java.text.MessageFormat;

/**
 * 插件 Jobber 的异常基类。
 *
 * @author 陈镕希
 * @since 1.0
 */
public class WaterflowException extends FitException {
    private Object[] args;

    /**
     * 抛出Jobber异常。
     *
     * @param error 异常枚举的{@link ErrorCodes}。
     */
    public WaterflowException(ErrorCodes error) {
        super(error.getErrorCode(), error.getMessage());
    }

    /**
     * 抛出Jobber异常。
     *
     * @param error 异常枚举的{@link ErrorCodes}。
     * @param args 额外参数。
     */
    public WaterflowException(ErrorCodes error, Object... args) {
        super(error.getErrorCode(), MessageFormat.format(error.getMessage(), args));
        this.args = args;
    }

    /**
     * 抛出Jobber异常。
     *
     * @param cause 表示异常原因的 Throwable。
     * @param error 异常枚举的{@link ErrorCodes}。
     * @param args 额外参数。
     */
    public WaterflowException(Throwable cause, ErrorCodes error, Object... args) {
        super(error.getErrorCode(), MessageFormat.format(error.getMessage(), args), cause);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
