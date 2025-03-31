/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 节点名字配置.
 *
 * @param target 图形对象.
 * @param field 字段.
 * @returns {{}} 配置对象.
 */
export const startFormConfiguration = (target, field) => {
    const self = {};
    self.type = "string";
    self.title = "startFormConfig";

    /**
     * 触发行为.这里只需要简单设置.
     *
     * @param value 设置的值.
     */
    self.action = (value) => {
        target.triggerMode = 'auto';
        if (value && value !== '') {
            target.task = {
                taskId : value,
                type : 'AIPP_SMART_FORM'
            }
        } else {
            target.task = null;
        }
    };

    /**
     * 获取值.
     *
     * @returns {*} 属性值.
     */
    self.getValue = () => {
        if (target.task) {
            return target.task.taskId;
        }
        return null;
    };

    return self;
};