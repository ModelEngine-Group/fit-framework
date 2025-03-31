/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {PAGE_MODE} from "../const.js";

/**
 * 提供mode的管理
 *
 * @returns {{}}
 */
export const MODE_MANAGER = (() => {
    const self = {};
    self[PAGE_MODE.CONFIGURATION] = initConfiguration();
    self[PAGE_MODE.DISPLAY] = initDisplay();
    self[PAGE_MODE.PRESENTATION] = initPresentation();
    self[PAGE_MODE.VIEW] = initView();
    self[PAGE_MODE.RUNTIME] = initRuntime();
    self[PAGE_MODE.HISTORY] = initHistory();

    self.registerOverrideMethod = (mode, type, methodName, method) => {
        if (!self[mode].overrideMethods[type]) {
            self[mode].overrideMethods[type] = {};
        }
        self[mode].overrideMethods[type][methodName] = method;
    };

    /**
     * 批量注册override方法.
     *
     * @param mode 模式.
     * @param types 类型列表.
     * @param methodName 方法名称.
     * @param method 方法体.
     */
    self.registerOverrideMethodBatch = (mode, types = [], methodName, method) => {
        types.forEach(type => self.registerOverrideMethod(mode, type, methodName, method));
    };

    self.registerForbiddenMethods = (mode, type, methodName) => {
        if (!self[mode].forbiddenMethods[type]) {
            self[mode].forbiddenMethods[type] = [];
        }
        self[mode].forbiddenMethods[type].push(methodName);
    };

    return self;
})();

function initConfiguration() {
    const self = {};
    self.forbiddenMethods = {
        "page": ["ifKeyPressed", "cleanPresentationDiv", "presentationKeyPressed"],
        "presentationFrame": ["managePageComment"],
    };
    self.overrideMethods = {
        "page": {
            "ifHideSelection": () => {
                return false;
            },
            "ifAddCommand": () => {
                return false;
            },
            "enableHistory": () => {
                return true;
            },
            "modeKeyPressed": (proxy, e, keyPressed) => {
                const base = (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey);

                // 目的是阻止浏览器的默认行为，比如ctrl + P会弹出打印框，在配置模式下不允许其弹出.
                if (base && (e.code === "KeyE")) {
                    e.preventDefault();
                    return true;
                }
                if (base && e.code.indexOf("Digit") >= 0) {
                    e.preventDefault();
                    return true;
                }
                if (base && e.code === "KeyI") {
                    e.preventDefault();
                    return true;
                }
                if (base && e.code === "KeyG") {
                    e.preventDefault();
                    return true;
                }
                if (base && e.code === "KeyP") {
                    e.preventDefault();
                    return true;
                }
                if (e.shiftKey && e.code === "Digit2") {
                    e.preventDefault();
                    return true;
                }

                keyPressed.call(proxy, e);
                return true;
            }
        },
        "shape": {
            "ifDrawFocusFrame": ()=> {
                return true;
            },
            /**
             * configuration模式下重写shape的getFontColor方法
             */
            "getFontColor": (proxy) => {
                return proxy.fontColor;
            },
            /**
             * configuration模式下重写shape的ifInConfig方法
             */
            "ifInConfig": () => {
                return true;
            }
        },
        "reference": {
            "getBorderWidth": () => {
                return 1;
            }
        },
        "presentationFrame": {
            "getIfMaskItems": () => {
                return false;
            },
        }
    };
    return self;
}

function initDisplay() {
    const self = {};
    self.forbiddenMethods = {
        "page": ["onkeyup", "onkeydown", "cleanPresentationDiv", "presentationKeyPressed", "configurationKeyPressed",
            "onLoaded", "onPaste", "onCopy"],
        "docPage": ["fillScreenInDocPage"],
    };
    self.overrideMethods = {};
    return self;
}

function initPresentation() {
    const self = {};
    self.forbiddenMethods = {
        "page": ["configurationKeyPressed", "onLoaded"],
        "docPage": ["fillScreenInDocPage"],
    };
    self.overrideMethods = {
        "page": {
            "modeKeyPressed" : async (proxy, e) => {
                if (e.code === "Enter" || e.key.indexOf("Right") >= 0 || e.key.indexOf("Down") >= 0) {
                    await proxy.moveNext();
                    await proxy.graph.collaboration.invoke({
                        method: "move_page_step", page: proxy.id, value: proxy.animationIndex, mode: proxy.mode
                    });
                    e.stopPropagation();
                    return false;
                }
                if (e.key.indexOf("Left") >= 0 || e.key.indexOf("Up") >= 0 || e.key.indexOf("Backspace") >= 0) {
                    await proxy.movePrevious();
                    await proxy.graph.collaboration.invoke({
                        method: "move_page_step", page: proxy.id, value: proxy.animationIndex, mode: proxy.mode
                    });
                    e.stopPropagation();
                    return false;
                }

                return true;
            }
        }
    };
    return self;
}

function initView() {
    const self = {};
    self.forbiddenMethods = {
        "page": ["presentationKeyPressed", "configurationKeyPressed", "onLoaded"],
        "docPage": ["fillScreenInDocPage"],
    };
    self.overrideMethods = {};
    return self;
}

function initRuntime() {
    const self = {};
    self.forbiddenMethods = {
        "page": ["cleanPresentationDiv", "presentationKeyPressed", "configurationKeyPressed", "onLoaded"],
        "docPage": ["fillScreenInDocPage"],
    };
    self.overrideMethods = {
    };
    return self;
}

function initHistory() {
    const self = {};
    self.forbiddenMethods = {
        "page": ["cleanPresentationDiv", "presentationKeyPressed", "configurationKeyPressed", "onLoaded"],
        "docPage": ["fillScreenInDocPage"]
    };
    self.overrideMethods = {
    };
    return self;
}