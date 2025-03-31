/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {colorPickerMenu, customMenu, iconMenu, selectMenu, submenu} from "./menu.js";
import {groupBy} from "../../common/util.js";
import {rectangle} from "../rectangle.js";
import {contextMenuDrawer, rectangleDrawer} from "../drawers/rectangleDrawer.js";

/**
 * 上下文工具栏工厂类.
 *
 * @return {{}} 配置工厂类对象.
 */
export const contextToolbar = () => {
    const menuMap = new Map();
    menuMap.set("icon", iconMenu);
    menuMap.set("select", selectMenu);
    menuMap.set("colorPicker", colorPickerMenu);
    menuMap.set("customComponent", customMenu);
    menuMap.set("submenu", submenu);

    const self = {};

    self.create = (page, shapes, config) => {
        self.page = page;
        // 因为跟随工具栏上的按钮不是shape，而是用原生html绘制，导致事件会被shape抢夺，故hack方式 将包围跟随栏的图形宽高调大，就可以以contextMenu图形来响应事件了，这不是一个好方案
        const w = config.frame.width;
        const h = config.frame.height;
        const dx = config.frame.x - (w / 2 - config.frame.width / 2);

        // ignoreReact防止在创建图形时，触发各种react行为，比如设置text = ""
        const contextContainer = page.ignoreReact(() => {
            return rectangle("context-menu", dx, config.frame.y, w, h, page, contextMenuDrawer(shapes.length, rectangleDrawer));
        });
        contextContainer.type = "contextMenu";
        contextContainer.initConnectors = () => {
            contextContainer.connectors = [];
        }
        contextContainer.serializable = false;
        contextContainer.resizeable = false;
        contextContainer.moveable = false;
        contextContainer.selectable = false;
        contextContainer.editable = false;
        contextContainer.deletable = false;
        contextContainer.allowLink = false;
        contextContainer.hideText = true;

        self._createToolBar(contextContainer.drawer.parent, config, shapes);
        self.page.sm.pushShape(contextContainer);
        contextContainer.invalidate();

        return contextContainer;
    }

    self.destroy = () => {
        if (self.page) {
            self.page.sm.getShapes(s => s.isType('contextMenu')).forEach(s => s.remove());
        }
    };

    self._createToolBar = (node, config, shapes) => {
        const toolbar = document.createElement("div");
        toolbar.id = "elsaToolBar";
        toolbar.classList.add("elsa-toolbar");
        toolbar.style[config.getLocation()] = config.getOffset() + "px";
        node.appendChild(toolbar);

        const groups = groupBy(config.menus, "group");

        Object.keys(groups).forEach((g, i) => {
            const group = document.createElement("div");
            group.classList.add("menu-group");
            toolbar.appendChild(group);

            const groupMenus = groups[g];
            groupMenus.forEach((m, i) => {
                const menu = self._createMenu(m.type, m, shapes);
                group.appendChild(menu);
            });
        });

        return toolbar;
    };

    self._createMenu = (type, config, shapes) => {
        const menu = menuMap.get(type)(type, config, shapes);
        return menu.createDom(config);
    }

    return self;
};

(() => {
    const style = document.createElement('style');
    style.innerHTML = `
        .contextMenu {
            background: #fff0 !important;
            z-index: 999;
        }
        .context-menu-rect {
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            top: 0;
            background: #fff0 !important;
            border-radius: 5px !important;
        }
        .elsa-toolbar {
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            display: inline-flex;
            align-items: center;
            flex-wrap: nowrap;
            border: 1px solid #d3d3d3;
            box-shadow: 0 2px 12px 0 rgba(56,56,56,.2);
            padding: 5px;
            background: #fff;
            border-radius: 2px;
            z-index: 999;
            pointer-events: auto;
        }
        .elsa-toolbar .menu-group {
            display: flex;
        }
        .elsa-toolbar .menu-group::after {
            content: "|";
            display: inline-block;
            width: 1px;
            height: 16px;
            color: #ccc;
            line-height: 32px;
            vertical-align: text-top;
            font-weight: normal;
            font-family: serif;
            text-align: center;
            margin-right: 5px;
        }
        .elsa-toolbar .menu-group:last-child::after {
            content: "";
        }
        .elsa-toolbar .menu {
            position: relative;
            padding: 4px;
            margin: 0 4px;
            cursor: pointer;
            color: rgb(89, 89, 89);
            border-radius: 2px;
        }
        .elsa-toolbar .menu:hover {
            background: #F1F1F2;
        }
        .elsa-toolbar .menu .icon-menu {
            display:inline-block;
            width: 22px;
            height: 22px;
        }
        .elsa-toolbar .menu .input-menu {
            width: 28px;
            height: 28px;
            border: none;
            background: none;
            outline: none;
        }
        .elsa-toolbar .menu .select-menu {
            height: 26px;
            line-height: 26px;
            border: none;
            outline: none;
            vertical-align: text-bottom;
            background: none;
        }
        .elsa-toolbar .sub-menu-container {
            position: absolute;
            display: flex;
            background: #fff;
            box-shadow: 0 2px 12px 0 rgba(56,56,56,.2);
            border: 1px solid #e2e6ed;
            border-radius: 2px;
         }
        .elsa-toolbar .sub-menu-container .customer-submenu {
            min-width: 120px;
            min-height: 200px;
        }
        .elsa-toolbar .sub-menu-container .vertical {
            flex-direction: column;
            width: 200px;
        }
        .elsa-toolbar .sub-menu-container .vertical .menu {
            padding: 8px;
        }
        .elsa-toolbar .sub-menu-container .vertical .menu .text {
            vertical-align: top;
        }
        .elsa-toolbar .text {
            white-space: nowrap;
        }
    `;
    document.head.appendChild(style);
})();