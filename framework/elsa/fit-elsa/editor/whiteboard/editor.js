/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {elsaSelection} from "./elsaSelection.js";

export const editor = (root) => {
    const self = {forbiddenCommands: new Set()};

    /**
     * 格式化选中区域.
     *
     * @param key 键值.
     * @param value 格式化的值.
     */
    self.format = (key, value = null) => {
        withSelect(() => {
            // execCommand针对fontSize只识别1-7，所以这里需要做特殊处理.
            if (key === "fontSize") {
                document.execCommand("styleWithCSS", false, "false");
                fontSizeCommand(value);
            } else {
                document.execCommand("styleWithCSS", false, "true");
                document.execCommand(key, false, value)
            }
        });
    };

    const fontSizeCommand = (value) => {
        const selection = elsaSelection(root);
        if (selection.isCollapsed()) {
            return;
        }
        document.execCommand("fontSize", false, "7");

        // 只保留<font>标签.
        const filter = (node) => node.nodeName.toLowerCase() === "font";
        const nodes = selection.getSelectNodes(NodeFilter.SHOW_ELEMENT, filter);
        nodes.forEach(n => {
            if (n.size) {
                n.removeAttribute("size");
                n.style.fontSize = value;
            }
        });
    };

    /**
     * 查询key的状态.
     *
     * @param key 键值.
     * @returns {*} true/false.
     */
    self.isFormatted = (key) => {
        return withSelect(() => document.queryCommandState(key));
    };

    /**
     * 获取格式化的值.
     *
     * @param key 键值.
     * @returns {*} 格式化的值.
     */
    self.getFormatValue = (key) => {
        return withSelect(() => {
            if (key === "fontSize") {
                return getFontSizeValue();
            }
            document.execCommand("styleWithCSS", false, "true");
            return document.queryCommandValue(key)
        });
    }

    const getFontSizeValue = () => {
        const selection = elsaSelection(root);
        const fontSizes = selection.getSelectNodes(NodeFilter.SHOW_TEXT, () => true)
            .map(node => {
                const parentElement = node.parentElement;
                const fontSize = parentElement.style.fontSize
                if (fontSize === "" || fontSize === null || fontSize === undefined) {
                    return getComputedStyle(parentElement).fontSize;
                } else {
                    return fontSize;
                }
            });

        if (fontSizes.length === 0) {
            return null;
        }

        // 返回最小的fontSize值，不带px.
        return fontSizes.map(fontSize => parseInt(fontSize.replace("px", "")))
            .reduce((prev, cur) => prev < cur ? prev : cur);
    };

    /**
     * 添加禁止指令.
     *
     * @param commandName 指令名称.
     */
    self.addForbiddenCommand = (commandName) => {
        self.forbiddenCommands.add(commandName);
    };

    const withSelect = (func) => {
        const selection = elsaSelection(root);
        if (selection.isFocused()) {
            return func();
        }
        selection.focus();
        const contentEditable = root.contentEditable;

        // 这里只有设置成contentEditable，才能focus使document的activeElement变为root.
        // 当是group时，就算调用了group中共元素的focus，也无法使activeElement变为root.因此获取fontSize及fontFace会出现问题.
        if (root.contentEditable !== "true") {
            root.contentEditable = "true";
            root.focus();
        }
        try {
            return func();
        } finally {
            selection.unFocus();
            root.contentEditable = contentEditable;
        }
    }

    return self;
}