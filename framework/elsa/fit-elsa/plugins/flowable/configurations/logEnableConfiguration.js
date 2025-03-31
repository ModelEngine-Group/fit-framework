/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Log开关配置.
 *
 * @param target 图形对象.
 * @param field 字段.
 * @returns {{}} 配置对象.
 */
export const logEnableConfiguration = (target, field) => {
    const self = {};
    self.type = "boolean";
    self.title = "logEnabled";

    /**
     * 触发行为.这里只需要简单设置布尔值
     *
     * @param {boolean} value 设置的值.
     */
    self.action = (value) => {
        if (!target.get('jober')) {
            target.jober = {};
        }
        // 将输入值强制类型转化为boolean
        target.get('jober')[field] = !!value;
    };

    /**
     * 获取值.
     *
     * @returns {boolean} 属性值.
     */
    self.getValue = () => {
        return !!(target.get('jober') && target.get('jober')[field]);
    };

    // 如果jober不存在，或者field不存在于jober里，那么初始化field的值为true
    if (!(target.get('jober')) || !(field in target.get('jober'))) {
        self.action(true);
    }

    return self;
};