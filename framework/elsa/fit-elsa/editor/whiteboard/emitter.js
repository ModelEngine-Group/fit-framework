/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {elsaSelection} from "./elsaSelection.js";

export const emitter = (dom, editor) => {
    const self = {editor: editor};

    /**
     * 注册内容改变时的回调.
     *
     * @param textChange 回调函数.
     */
    self.onTextChange = (textChange) => {
        self.textChange = textChange;
    }

    /**
     * 注册光标改变时的回调.
     *
     * @param onSelectionChange 回调函数.
     */
    self.onSelectionChange = (onSelectionChange) => {
        self.selectionChange = onSelectionChange;
    }

    /**
     * 启动监听.
     */
    let observer = null;
    self.emit = () => {
        // 启动文本监听.
        observer = new MutationObserver(() => self.textChange && self.textChange(dom.innerHTML));
        observer.observe(dom, {
            attributes: true, childList: true, subtree: true, characterData: true
        });

        // 启动selection监听.
        window.document.addEventListener("selectionchange", selectionChange);

        // 启动键盘事件监听
        dom.addEventListener("keydown", event => {
            const isCtrl = event.ctrlKey || event.metaKey;
            if (isCtrl && event.shiftKey && event.code === "KeyX") {
                if (self.editor.isFormatted("strikeThrough")) {
                    self.editor.format("strikeThrough", false);
                } else {
                    self.editor.format("strikeThrough", true);
                }
            }
        });
    }

    const selectionChange = (event) => {
        const selection = elsaSelection(dom);
        if (selection.isFocused()) {
            self.selectionChange && self.selectionChange(event);
        }
    }

    /**
     * 关闭.
     */
    self.shutdown = () => {
        observer.disconnect();
        window.document.removeEventListener("selectionchange", selectionChange);
    }

    return self;
}