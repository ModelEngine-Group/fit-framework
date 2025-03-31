/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 格式化存在占位符('{}')的字符串.
 *
 * @param str 字符串.
 * @param data 需要替换的数据.
 * @return {*} 格式化之后的字符串.
 */
export const formatString = (str, data) => {
    let formatStr = str;
    for (let key in data) {
        formatStr = formatStr.replace(new RegExp("\\{" + key + "\\}", "g"), data[key]);
    }
    return formatStr;
};