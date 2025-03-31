/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {PAGE_MODE} from '../common/const.js';
/*
各种键盘事件，包括：
copy/paste, +:zoomOut, -:zoomIn, ctrl+z:undo, ctrl+alt+z:redo, ctrl+a: select all, ctrl+s: save to image,
left: move left, right: move right, up: move up, down: move down, esc: unselect, del: delete shape, ctrl+d: duplicate selected shapes,
ctrl+g: group selected shapes, ctrl+b: break grouped shapes,
ctrl+l: change line mode from sequence: straight->autoCurve->broken->curve
辉子 2020
*/
const keyActions = page => {
    let action = {};

    /**
     * 将elsa定义的事件体系挂载到对应的document，这样才能正确响应鼠标，键盘事件
     * 辉子
     */
    action.attachCopyPaste = () => {
        if (document.page === page) {
            return;
        }
        document.page = page;
        document.oncut = event => {
            const copyResult = document.oncopy(event);
            if (!copyResult) {
                return;
            }
            copyResult.cut();
        };
        document.oncopy = event => {
            if (document.activeElement !== document.body) {
                return true;
            }
            const copyResult = page.onCopy(page.getFocusedShapes());
            if (event.clipboardData && copyResult) {
                event.preventDefault();
                event.clipboardData.setData(`elsa/${copyResult.type}`, JSON.stringify(copyResult.data));
            }
            return copyResult;
        };
        document.onpaste = event => {
            if (document.activeElement !== document.body) {
                return;
            }
            page.onPaste(event);
        };
        document.addEventListener('keydown', page.onkeydown);
        document.addEventListener('keyup', page.onkeyup);
    }

    /**
     * 将elsa定义的事件体系从相应的document卸载，用于一个页面可以有多个page，事件只会触发聚焦的page
     * 辉子
     */
    action.detachCopyPaste = function () {
        if (page.mode !== PAGE_MODE.CONFIGURATION) {
            return;
        }
        document.oncut = document.oncopy = document.onpaste = null;
        document.removeEventListener('keydown', page.onkeydown);
        document.removeEventListener('keyup', page.onkeyup);
        document.page = null;
    };
    return action;
};

export {keyActions};