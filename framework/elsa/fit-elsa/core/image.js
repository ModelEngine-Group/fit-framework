/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangle} from './rectangle.js';
import {rectangleDrawer} from "./drawers/rectangleDrawer.js";

/**
 * 图片绘制器.
 *
 * @override
 */
const imageDrawer = (shape, div, x, y) => {
    const self = rectangleDrawer(shape, div, x, y);
    self.img = self.createElement("img", shape.id + ":image");
    self.img.crossOrigin = "Anonymous";
    self.parent.insertBefore(self.img, self.text);

    self.containsText = () => false;
    /**
     * resize时，需要对image元素也进行resize todo 此方法不能删除，否则导致图片缩放异常
     *
     * @override
     */
    const resize = self.resize;
    self.resize = () => {
        const size = resize.apply(self);
        self.img.style.width = size.width + "px";
        self.img.style.height = size.height + "px";
        return size;
    }

    // 修复图片绘制位置问题，具体原因待查
    let drawStatic = self.drawStatic;
    self.drawStatic = () => {
        drawStatic.call(self);
        self.parent.style.alignItems = "normal";
    };

    return self;
};

/**
 * 图片
 * 将图片转换成elsa可操作格式
 * 辉子 2020
 * 重写 2021
 */
const image = (id, x, y, width, height, parent) => {
    let self = rectangle(id, x, y, width, height, parent, imageDrawer);
    self.type = "image";
    self.editable = false;
    self.borderWidth = 0;
    self.text = "";
    self.width = self.height = -1;//适应图像
    self.backColor = "rgba(255,255,255,0)";
    self.emphasizeType = 1;

    self.hideText = true;

    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.call(self);
        const westNorthConnector = self.connectors[4];
        const westSouthConnector = self.connectors[5];
        const eastNorthConnector = self.connectors[6];
        const eastSouthConnector = self.connectors[7];
        // 保持和ppt一样的效果，图片的四个顶点默认具备等比缩放能力
        westNorthConnector.equiproportional = s => true;
        westSouthConnector.equiproportional = s => true;
        eastNorthConnector.equiproportional = s => true;
        eastSouthConnector.equiproportional = s => true;
    }

    self.addDetection(["src"], (property, value, preValue) => {
        if (value === "" || value === undefined) {
            return;
        }
        self.drawer.img.src = value;
        self.drawer.img.onload = () => {
            self.onImageLoaded(self.drawer.img);
        };
    });

    const load = self.load;
    self.load = (ignoreFilter) => {
        load.apply(self, [ignoreFilter]);
        if (self.src) {
            self.drawer.img.src = self.src;
            self.drawer.img.onload = () => {
                self.onImageLoaded(self.drawer.img);
            };
        }
    };

    /**
     * 图片加载完成后触发.
     *
     * @param img 图形对象.
     */
    self.onImageLoaded = (img) => {};

    return self;
};

export {image};