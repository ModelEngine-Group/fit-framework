/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 编辑器接口类.
 * 提供统一的接口方法给elsa使用.
 *
 * @returns {{}} 编辑器对象.
 */
export const editorInterface = () => {
    const self = {};

    /**
     * 渲染数据.
     *
     * @param data 数据.
     * @param styles 样式.
     */
    self.render = (data, styles) => {
    }

    self.focus = () => {
    };

    /**
     * 格式化文本.
     *
     * @param key
     * @param value
     */
    self.format = (key, value = null) => {
    }

    /**
     * 查询key的状态.
     *
     * @param key 键值.
     * @returns {boolean} true/false.
     */
    self.isFormatted = (key) => {
    }

    /**
     * 获取格式化的值.
     *
     * @param key 键值.
     * @returns {*} 格式化的值.
     */
    self.getFormatValue = (key) => {
    }

    /**
     * 添加禁止指令.
     *
     * @param commandName 指令名称.
     */
    self.addForbiddenCommand = (commandName) => {
    }

    /**
     * 判断编辑器是否处于focus状态.
     *
     * @returns {*} true/false.
     */
    self.isFocused = () => {
    }

    /**
     * 使文本失焦.
     */
    self.blur = () => {
    }

    /**
     * 卸载.
     */
    self.unmount = () => {
    };

    /**
     * 销毁.
     */
    self.destroy = () => {
    };

    /**
     * 获取渲染的数据的字符串.
     *
     * @abstract
     * @public
     */
    self.getTextString = () => {
    };

    return self;
}