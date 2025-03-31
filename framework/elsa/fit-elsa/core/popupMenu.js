/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, DEFAULT_FOLLOW_BAR_OFFSET, DOCK_MODE, FONT_WEIGHT} from "../common/const.js";
import {container} from "./container.js";
import {canvasContainerDrawer} from "./drawers/containerDrawer.js";
import {text} from "./rectangle.js";
import {contextToolbar} from "./contextToolbar/contextToolbar.js";
import {getIntersection} from "../common/util.js";

const contextMenu = (page, shapes) => {
    // 暂时关闭文档的上下文菜单
    if (page.type === "docPage") {
      return undefined;
    }

    if (shapes.length === 0) {
      return undefined;
    }

    // 先销毁之前的上下文菜单
    const contextBar = contextToolbar();
    page.contextToolbar = contextBar;
    contextBar.destroy();
    // 清理画布上的右键功能菜单
    page.sm.getShapes(s => s.isType('menu')).forEach(s => s.remove());

    // 获取图形的上下文菜单功能配置
    if (shapes.some(s => s.ignoreDefaultContextMenu)) {
      return undefined;
    }
    let menuScript = getMenuScript(shapes);

    if (!menuScript || !menuScript.menus || menuScript.menus.length === 0) {
      return undefined;
    }

    return contextBar.create(page, shapes, menuScript);
};

const getMenuScript = (shapes) => {
    // followBar的显示位置需要根据图形的frame在page中的位置去计算
    const frame = boundOfShapes(shapes.map(s => s.getFrame()));
    // 获取图形的上下文菜单功能配置
    const script = shapes[0].getContextMenuScript();
    script.frame = frame;
    if (shapes.length > 1) {
        const menus = shapes.map(s => s.getContextMenuScript()).map(script => script.menus).filter(m => !!m);
        script.menus = getIntersection(menus, (a, b) => a.name === b.name);
        script.getLocation = calculateShapesPosition(frame).getLocation;
        script.getOffset = calculateShapesPosition(frame).getOffset;
    } else {
        script.getLocation = shapes[0].getFollowBarLocation;
        script.getOffset = shapes[0].getFollowBarOffset;
    }
    return script;
}

/**
 * 计算圈选所有图形后的一个矩形的区域
 *
 * @param frames 图形的frame
 * @returns {{x: number, width: number, y: number, height: number}}
 */
const boundOfShapes = (frames) => {
    if (frames.length === 0) {
        return {x: 0, y: 0, width: 0.1, height: 0.1};
    }
    const pad = 8;
    const minX = Math.floor(frames.min(l => l.x));
    const minY = Math.floor(frames.min(l => l.y));
    const maxX = Math.ceil(frames.max(l => l.x + l.width));
    const maxY = Math.ceil(frames.max(l => l.y + l.height));

    return {
        x: minX - pad,
        y: minY - pad,
        width: maxX - minX + 2 * pad,
        height: maxY - minY + 2 * pad
    };
};

const calculateShapesPosition = (frame) => {
    return {
        getLocation: () => "bottom",
        getOffset: () => DEFAULT_FOLLOW_BAR_OFFSET,
    };
}

const popupMenu = (() => {
    const popup = {};
    popup.show = (shape, x, y, menuScript) => {
      let menuScriptVal = menuScript;
      shape.page.sm.getShapes(s => s.isType('menu')).forEach(s => s.remove());
      shape.page.contextToolbar && shape.page.contextToolbar.destroy();
      !menuScriptVal && (menuScriptVal = shape.getMenuScript(x, y));
      if (menuScriptVal && menuScriptVal.length > 0) {
        const pop = menu(shape, menuScriptVal, x, y);
            if (pop.firstChild) {
                pop.firstChild.onMouseIn();
                pop.invalidate();
                return pop;
            }
        }
      return undefined;
    };
    popup.dispose = () => {

    };
    return (x, y, shape, menuScript) => {
        shape.page.getFocusedShapes().forEach(s => s.unSelect());
        shape.select();
        popup.dispose();
        return popup.show(shape, x, y, menuScript);
    };
})();

const menu = (() => {
    // let mouseOnItem;
    return (shape, menuScript, x, y, level = 0, drawer) => {
        const STEPS = menuScript.max(m => m.text ? m.text.length : 0);
        let self = container("menu-" + level, x, y, STEPS * 12 + 60, 140, shape.page, drawer ? drawer : canvasContainerDrawer);
        self.type = "menu";
        self.serializable = false;
        self.getIndex();
        self.text = "";
        self.backColor = "rgba(255,255,255,0.8)";
        self.borderColor = menuScript.borderColor ? menuScript.borderColor : "dimgray";
        self.cornerRadius = 6;
        self.shadow = true;
        const padx = 2;
        const pady = 2;
        self.itemPad = [padx, padx, pady, pady];
        self.autoFit = true;
        self.level = level;
        self.resizeable = false;
        self.rotateable = false;
        self.selectable = false;
        self.drawer.drawStatic = (context, x, y) => {
            self.getShapes().forEach(s => {
                context.save();
                context.translate(x + 12, y + s.y - self.y + 10);
                context.beginPath();
                if (s.itemDraw) {
                    s.itemDraw(context);
                } else {
                    if (s.itemText === "无") {
                        context.fillStyle = "red";
                        context.fillRect(-1, -1, 2, 2);
                    }
                }
                if (s.menus) {
                    const x0 = self.width - 30;
                    context.fillStyle = "dimgray";
                    // context.beginPath();
                    context.moveTo(x0, -3);
                    context.lineTo(x0 + 6, 0);
                    context.lineTo(x0, 3);
                    context.closePath();
                    context.fill();
                }
                context.restore();
            })
        }
        shape.page.sm.pushShape(self);
        menuScript.width && (self.width = menuScript.width);

        let previous;
        menuScript.forEach(m => {
            const MENU_HEIGHT = 18;
            const MENU_FONT_SIZE = 10;
            const item = text(undefined, self.x, self.y, MENU_HEIGHT, self.width, self, m.drawer);
            item.serializable = false;
            item.getIndex();
            item.resizeable = false;
            item.type = "menuItem";
            item.fontColor = menuScript.fontColor ? menuScript.fontColor : "black";
            item.hAlign = ALIGN.LEFT;
            item.pad = 3;
            item.padLeft = 20;
            item.fontSize = MENU_FONT_SIZE;// / self.page.scaleY;
            item.fontWeight = FONT_WEIGHT.LIGHTER;
            item.backColor = "rgba(0,0,0,0)";
            item.itemDraw = m.draw;
            item.itemImg = m.img;
            item.itemText = m.text;
            if (m.text === undefined) {
                item.cornerRadius = 0;
                item.backColor = menuScript.borderColor ? menuScript.borderColor : "lightgray";
                item.borderWidth = 0;
                item.text = "";
                item.height = 1;
                item.width = self.width;
            } else {
                item.height = MENU_HEIGHT;
                item.text = "  " + m.text;
                item.action = m.action;
                item.menus = m.menus;
                if (previous) {
                    previous.next = item;
                    item.previous = previous;
                } else {
                    self.firstChild = item;
                }
                previous = item;
                item.onMouseOut = shape => {
                    item.backColor = "rgba(0,0,0,0)";
                    delete self.page.focusMenuItem;
                    // if (!item.subMenu) return;
                    // item.subMenu.remove();
                    // item.subMenu = undefined;

                };

                item.onMouseIn = () => {
                    self.page.sm.getShapes(s => s.level > self.level && s.type === self.type).forEach(s => {
                        s.remove();
                    })
                    self.page.focusMenuItem = item;
                    // if (mouseOnItem !== undefined && mouseOnItem.subMenu && mouseOnItem.subMenu.id !== item.container) {
                    //     mouseOnItem.subMenu.remove();
                    //     mouseOnItem.subMenu = undefined;
                    // }

                    item.backColor = "rgba(0,0,0,0.1)";

                    // if (item.menus && item.subMenu) return;
                    if (!item.menus) {
                        return;
                    }
                    const subMenu = menu(shape, item.menus, self.x + self.width + 2, item.y, self.level + 1);
                    subMenu.parent = item;
                    item.subMenu = subMenu;
                    item.width && subMenu.resize(subMenu.width, subMenu.height);
                    subMenu.moveTo(subMenu.x, item.y + item.height / 2 - subMenu.height / 2);
                };

                item.click = (x, y) => {
                    if (item.menus) {
                        return;
                    }
                    item.action(shape, x, y);
                    shape.select(x, y);
                    shape.page.sm.getShapes(s => s.isType('menu')).forEach(s => s.remove());
                };
                item.toParent = () => {
                    const container = item.getContainer();
                    if (container.parent) {
                        item.onMouseOut();
                        container.parent.onMouseIn();
                    }
                };
                item.toChild = () => {
                    if (item.subMenu && item.subMenu.firstChild) {
                        item.onMouseOut();
                        item.subMenu.firstChild.onMouseIn();
                    }
                };
                item.toPrevious = () => {
                    if (item.previous) {
                        item.onMouseOut();
                        item.previous.onMouseIn();
                    }
                };
                item.toNext = () => {
                    if (item.next) {
                        item.onMouseOut();
                        item.next.onMouseIn();
                    }
                };
            }
            item.container = self.id;
            shape.page.sm.pushShape(item);
        })
        self.container = self.page.id;
        self.dockMode = DOCK_MODE.VERTICAL;
        shape.unSelect();

        const escaped = shape.page.escaped;
        const onMouseUp = shape.page.onMouseUp;
        shape.page.escaped = () => {
            shape.page.sm.getShapes(s => s.isType('menu')).forEach(s => s.remove());
            shape.page.escaped = escaped;
        };
        shape.page.onMouseUp = pos => {
            if (shape.page.cancelClick) {
                delete shape.page.cancelClick;
                return false;
            }
            if (shape.page.mouseInShape.isTypeof('menu')) {
                return false;
            }

            shape.page.contextToolbar && shape.page.contextToolbar.destroy();
            shape.page.sm.getShapes(s => s.isType('menu')).forEach(s => s.remove());
            shape.page.onMouseUp = onMouseUp;
            return true;
        };
        self.childAllowed = (shape) => {
            return shape.isTypeof("menuItem");
        }
        return self;
    }
})();
export {popupMenu, menu, contextMenu, boundOfShapes};