/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.exceptions;

import modelengine.fitframework.exception.ErrorCode;
import modelengine.fitframework.exception.FitException;

/**
 * 服务器内部异常，用于服务器内部报错。
 *
 * @author 陈镕希
 * @since 2023-07-06
 */
@ErrorCode(500)
public class ServerInternalException extends FitException {
    /**
     * 使用异常信息初始化 {@link ServerInternalException} 类的新实例。
     *
     * @param message 表示异常信息的 {@link String}。
     */
    public ServerInternalException(String message) {
        super(message);
    }

    /**
     * 使用异常信息和引发异常的原因初始化 {@link ServerInternalException} 类的新实例。
     *
     * @param message 表示异常信息的 {@link String}。
     * @param cause 表示引发异常的原因的 {@link Throwable}。
     */
    public ServerInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
