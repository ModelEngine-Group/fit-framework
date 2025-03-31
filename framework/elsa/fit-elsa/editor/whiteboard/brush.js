/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {emitter} from "./emitter.js";
import {editor} from "./editor.js";
import {elsaSelection} from "./elsaSelection.js";
import {editorInterface} from "../editorInterface.js";
import {EVENT_TYPE} from "../../common/const";

export const brush = (shape) => {
    const self = editorInterface();
    self.parent = shape.drawer.text;
    self.root = document.createElement("div");
    self.root.spellcheck = false;
    self.root.style.outline = "none";
    self.root.style.whiteSpace = "pre-wrap";
    self.root.style.overflowWrap = "break-word";
    self.parent.appendChild(self.root);

    self.editor = editor(self.root);
    self.emitter = emitter(self.root, self.editor);
    self.emitter.onTextChange((data) => {
        shape.page.ignoreReact(() => shape.text = data);
    });
    self.emitter.onSelectionChange(() => {
        shape.graph.activePage.triggerEvent({
            type: EVENT_TYPE.EDITOR_SELECTION_CHANGE, value: window.getSelection()
        });
    });

    /**
     * 渲染数据.
     *
     * @param data 数据.
     * @param styles 样式.
     */
    self.render = (data, styles) => {
        self.root.contentEditable = String(!readOnly);
        self.root.innerHTML = data;
        styles && Object.keys(styles).forEach(key => self.root.style[key] = styles[key]);
        self.emitter.emit();
    }

    self.focus = () => {
        self.root.focus();
    };

    /**
     * 格式化文本.
     *
     * @param key
     * @param value
     */
    self.format = (key, value = null) => {
        self.editor.format(key, value);
    }

    /**
     * 查询key的状态.
     *
     * @param key 键值.
     * @returns {boolean} true/false.
     */
    self.isFormatted = (key) => {
        return self.editor.isFormatted(key);
    }

    /**
     * 获取格式化的值.
     *
     * @param key 键值.
     * @returns {*} 格式化的值.
     */
    self.getFormatValue = (key) => {
        return self.editor.getFormatValue(key);
    }

    /**
     * 添加禁止指令.
     *
     * @param commandName 指令名称.
     */
    self.addForbiddenCommand = (commandName) => {
        self.editor.addForbiddenCommand(commandName);
    }

    /**
     * 判断编辑器是否处于focus状态.
     *
     * @returns {*} true/false.
     */
    self.isFocused = () => {
        const selection = elsaSelection(self.root);
        return selection.isFocused();
    }

    /**
     * 使文本失焦.
     */
    self.blur = () => {
        const selection = elsaSelection(self.root);
        if (selection.isFocused()) {
            selection.unFocus();
        }

        if (document.activeElement === self.root) {
            self.root.blur();
        }
    }

    /**
     * 卸载.
     */
    self.unmount = () => {
        if (self.root.contentEditable === "true") {
            self.emitter.shutdown();
        }
        self.blur();
    };

    /**
     * @inheritDoc
     * @override
     */
    self.getTextString = () => {
        const div = document.createElement("div");
        div.innerHTML = shape.text;
        let textString = div.innerText;
        (textString.length > 20) && (textString = textString.substr(0, 19) + "...");
        return textString;
    }

    return self;
}