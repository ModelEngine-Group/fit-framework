/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {editorInterface} from "../editorInterface.js";
import {Asserts} from "../Asserts.js";
import ElsaEditor from "./elsa-editor.js";
import {sleep} from "../../common/util.js";

const COMMANDS_MAP = {
    "bold": "bold",
    "italic": "italic",
    "strikeThrough": "strikethrough",
    "underline": "underline",
    "backColor": "fontBackgroundColor",
    "foreColor": "fontColor",
    "fontName": "fontFamily",
    "fontSize": "fontSize",
    "insertOrderedList": "numberedList",
    "insertUnorderedList": "bulletedList"
};

/**
 * 默认的编辑器.
 * 暂时使用ckeditor5实现.
 *
 * @param initial 初始化参数.
 * @return {{}} 默认编辑器对象.
 */
export const defaultEditor = (initial) => {
    Asserts.notNull(initial, "initial is null.");
    Asserts.notNull(initial.editableDom, "editable dom is null.");

    const self = editorInterface();
    self.status = "init";
    self.toolbarDom = initial.toolbarDom;
    self.editableDom = initial.editableDom;
    self.editableDom.style.overflowWrap = "break-word";
    self.editableDom.style.whiteSpace = "pre-wrap";
    let prevData = null;

    // 创建编辑器.
    ElsaEditor.create([], {generateId: initial.generateId}).then(editor => {
        self.editor = editor;

        // 渲染编辑区.
        self.editor.renderEditable(self.editableDom);

        Asserts.notNull(initial.onTextChange, "onTextChange is required.");
        Asserts.notNull(initial.onSelectionChange, "onSelectionChange is required.");
        self.editor.onChange((isDirty, data) => {
            if (isDirty) {
                if (prevData === null || JSON.stringify(prevData) !== JSON.stringify(data)) {
                    initial.onTextChange(data);
                    prevData = data;
                }
            } else {
                initial.onSelectionChange();
            }
        });

        self.status = "ready";
    });

    const isReady = () => {
        if (self.status !== "ready") {
            return false;
        }
        return true;
    };

    /**
     * 对数据进行规范化处理.
     *
     * @param data 数据.
     * @return {*[]|[{children: [{data: string}], name: string}]|*} 符合编辑器格式的数据.
     */
    const normalize = (data) => {
        if (data === null || data === undefined) {
            return {main: []};
        }

        if (typeof data !== "string" && !Array.isArray(data)) {
            throw new Error("invalid data.");
        }

        if (typeof data === "string") {
            return {
                main: [{
                    "name": "paragraph", "children": [{"data": data}]
                }]
            };
        }

        return {main: data};
    };

    /**
     * 渲染.
     * 每次渲染都会重新创建一个编辑器对象(出于手动对shape进行text设置的考虑，shape.text = "xxx").
     *
     * @param data 待渲染数据.
     * @param readOnly 是否只读.
     * @param autoFocus 是否自动聚焦.
     * @param styles 样式.
     */
    self.render = (data, readOnly, autoFocus, styles) => {
        awaitEditorReady().then(editor => {
            // 设置readonly.
            const lockId = self.editableDom.id;
            readOnly ? editor.enableReadOnlyMode(lockId) : editor.disableReadOnlyMode(lockId);

            // 设置编辑区数据.
            // 注意: 设置编辑区数据要放在readOnly设置之后，否则，在ck内部流程中无法正确获取到readOnly状态.
            editor.setData(normalize(data));

            // 自动focus.
            if (autoFocus) {
                editor.editing.view.focus();
            } else {
                self.blur();
            }
        })
    }

    self.format = (key, value = null) => {
        awaitEditorReady().then(editor => {
            if (forbiddenCommands.has(key)) {
                return;
            }

            // 这里需要将elsa的command和编辑器自身的command做一个映射.
            const commandKey = COMMANDS_MAP[key];
            if (value) {
                editor.execute(commandKey, {value});
            } else {
                editor.execute(commandKey);
            }
        });
    }

    /**
     * 渲染toolbar组件.
     *
     * @param div 待渲染的div.
     */
    self.renderToolbar = (div) => {
        awaitEditorReady().then(editor => {
            editor.renderToolbar(div);
        });
    }

    self.isFormatted = (key) => {
        if (!isReady()) {
          return false;
        }
        const model = self.editor.model;
        const document = model.document;
        return model.schema.checkAttributeInSelection(document.selection, COMMANDS_MAP[key]);
    }

    self.getFormatValue = (key) => {
        if (!isReady()) {
          return false;
        }
        return self.editor.model.document.selection.getAttribute(COMMANDS_MAP[key]);
    }

    const forbiddenCommands = new Set();
    self.addForbiddenCommand = (commandName) => {
        if (!commandName || commandName === "") {
            return;
        }
        forbiddenCommands.add(commandName);
    }

    self.isFocused = () => {
        if (!isReady()) {
          return false;
        }
        return self.editor.editing.view.document.isFocused;
    }

    /**
     * ck未提供blur接口给用户，只能自己手动调用activeElement的blur方法.
     */
    self.blur = () => {
        const activeElement = document.activeElement;
        if (activeElement === document.body) {
            return;
        }
        if (self.editableDom.contains(activeElement)) {
            activeElement.blur();
        }
    }

    /**
     * 清空编辑区.
     */
    self.unmount = () => {
        awaitEditorReady().then(editor => editor.setData(normalize([])));
    };

    const awaitEditorReady = async () => {
        while (self.editor === undefined || self.editor === null) {
            await sleep(5);
        }
        return self.editor;
    }

    return self;
}