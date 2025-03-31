/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * description配置.
 *
 * @param target 图形对象.
 * @param field 字段.
 * @returns {{}} 配置对象.
 */
export const promptConfiguration = (target, field) => {
    const self = {};
    self.type = "string";
    self.title = "prompt";

    /**
     * 触发行为.这里只需要简单设置 prompt 即可.
     *
     * @param value 设置的值.
     */
    self.action = (value) => {
        if (!target.get('jober')) {
            target.jober = {};
        }
        target.get('jober')[field] = value;
    };

    /**
     * 获取值.
     *
     * @returns {*} 属性值.
     */
    self.getValue = () => {
        if (!target.get('jober')) {
            return "";
        }
        return target.get('jober')[field];
    };

    return self;
};