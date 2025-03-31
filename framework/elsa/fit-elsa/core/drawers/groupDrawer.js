/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {containerDrawer} from "./containerDrawer.js";

/**
 * 组合绘制器.
 *
 * @param group 组合对象.
 * @param div 待渲染的dom.
 * @param x 横坐标.
 * @param y 纵坐标.
 */
export const groupDrawer = (group, div, x, y) => {
    const drawer = containerDrawer(group, div, x, y);
    drawer.type = "group drawer";

    /**
     * 重写背景刷新方法.不做任何处理.
     */
    drawer.backgroundRefresh = () => {
    }

    /**
     * 重写画border的方法.
     */
    drawer.drawBorder = () => {
        const shapes = group.getShapes();
        let border = shapes.some(s => s.isFocused) ? "1px " : "0px ";
        border += group.getDashWidth() > 4 ? "dashed" : "dotted";
        drawer.parent.style.border = border;
        drawer.parent.style.borderColor = group.getBorderColor();
    }

    /**
     * 重写drawStatic方法.
     */
    drawer.drawStatic = () => {
        drawer.parent.style.opacity = "1";
    };

    return drawer;
}