/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {layoutCommand} from "../commands.js";

/**
 * 字段配置信息.
 *
 * @param target 字段所属的对象.
 * @param field 字段名称.
 * @constructor
 */
export const fieldConfiguration = (target, field) => {
    const self = {};
    self.field = field;
    self.type = "";
    self.title = "";
    self.group = [{type: "configPanel", name: "格式选项"}];
    self.readOnly = false;
    self.index = 0;

    /**
     * 获取value.
     * 1、如果当前对象的value有值，则直接返回
     * 2、否则去page中的默认设置的值，若page中有值，也直接返回
     * 3、否则，取graph中的默认设置的值.
     *
     * @return {*} 字段的值.
     */
    self.getValue = () => {
        return target.get(field);
    }

    /**
     * 配置的回调.
     *
     * @param value 回调设置的值.
     * @return promise
     */
    self.action = (value) => {
        target.graph.change(() => {
            self.doCommand(self.getChangedData(value));
        });
    };

    self.doCommand = (changedData) => {
        layoutCommand(target.page, changedData).execute();
    };

    self.getChangedData = (value) => {
        const data = {shape: target};
        data[self.field] = value;
        return [data];
    };

    return self;
}