/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * ohscript内置脚本配置.
 *
 * @param target 图形对象.
 * @param field 字段.
 * @returns {{}} 配置对象.
 */
export const codeConfiguration = (target, field) => {
    const self = {};
    self.type = "string";
    self.title = "code";

    /**
     * 触发行为.这里只需要简单设置 code 即可.
     *
     * @param value 设置的值.
     */
    self.action = (value) => {
        if (!target.get('jober')) {
            target.jober = {
                "type": "ohscript_jober"
            };
        }
        target.get('jober')['entity'] = {};
        target.get('jober')['entity'][field] = value;
    };

    /**
     * 获取值.
     *
     * @returns {*} 属性值.
     */
    self.getValue = () => {
        if (!target.get('jober') || !target.get('jober')['entity']) {
            return "";
        }
        return target.get('jober')['entity'][field];
    };

    return self;
};