/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangleDrawer} from './rectangleDrawer.js';

/**
 * svg画笔
 * 主要是装在svg格式，给到浏览器自行渲染，不做格式转换
 * 辉子 2020
 */
const svgDrawer = (shape, div, x, y) => {
    let self = rectangleDrawer(shape, div, x, y);
    self.type = "vector drawer";
    self.init = () => {
        self.svg = document.getElementById(svgId());
        if (self.svg === undefined) {
            return;
        }
        // shape.width = self.svg.getBBox().width;
        // shape.height = self.svg.getBBox().height;
    };

    self.drawBorder = () => {
    };

    self.backgroundRefresh = () => {
    };

    let resize = self.resize;

    function svgId() {
        return shape.type + "-svg:" + shape.id;
    }

    self.resize = () => {
        shape.pad = 0;
        resize.call(self);
        if (self.originWidth === undefined) {
            let elementId = svgId();
            self.svg = document.getElementById(elementId);
            if (!self.svg || self.svg.clientWidth === 0) {
                return;
            }
            // self.originWidth = shape.width = self.svg.clientWidth;// self.svg.getBBox().width;
            // self.originWidth = shape.height = self.svg.clientHeight;// self.svg.getBBox().height;
            shape.rate = shape.height / shape.width;
        }
    };

    let drawStatic = self.drawStatic;
    self.drawStatic = () => {
        drawStatic.call(self);
        if (self.text.childNodes.length === 0 || !self.text.childNodes[0]) {
            return;
        }
        const svgDom = self.text.childNodes[0];
        if (!svgDom.style) {
            return;
        }
        svgDom.style.width = shape.width;
        svgDom.style.height = shape.height;
        svgDom.setAttribute("preserveAspectRatio", "none");
    }
    return self;
};

export {svgDrawer};