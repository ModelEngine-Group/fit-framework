/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 断言工具类.
 */
export class Asserts {
    /**
     * 断言一个对象是function.
     *
     * @param obj 待判断的对象.
     * @param errMsg 错误信息.
     */
    static isFunc(obj, errMsg) {
        if (typeof obj !== "function") {
            throw new Error(errMsg);
        }
    }

    /**
     * 断言一个对象不为null或undefine.
     *
     * @param obj 对象.
     * @param msg 错误信息.
     * @returns {*} 抛出异常或返回obj对象.
     */
    static notNull(obj, msg = undefined) {
        if (obj === null || obj === undefined) {
            const errorMsg = msg ? msg : "obj[" + obj + "] is null";
            throw new Error(errorMsg);
        }
        return obj;
    }
}