/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { pixelRateAdapter } from "../../common/util.js";

/**
 * 循环调度每个shape的动画，驱动由requestAnimation
 * 每个shape.drawAnimation里绘制动画
 * 辉子 2021
 */
const animationDrawer = (graph, page, div) => {
    let self = {};
    self.type = "animation drawer";

    self.reset = () => {
    };

    self.pageIdChange = () => {
    }

    self.draw = () => {
        page.sm.getShapes(s => s.enableAnimation).forEach(s => {
            s.drawer.drawAnimation();
        })
        page.drawer.drawDynamic && page.drawer.drawDynamic();
        if (!page.animationCode) {
            return;
        }
        if (!self.animation) {
            self.animation = graph.createDom(div, "canvas", "animationLayer:" + page.id, page.id);
            // self.animation.id = "animationLayer:" + page.id;
            self.animation.style.position = "absolute";
            self.animation.style.zIndex = 1;
            div.appendChild(self.animation);
        }
        self.animation.width = div.clientWidth;
        self.animation.height = div.clientHeight;
        pixelRateAdapter(self.animation.getContext("2d"), page.scaleX, page.scaleY, page.graph.ignoreHighQuality);

        const context = self.animation.getContext("2d");
        context.clearRect(0, 0, self.animation.width, self.animation.height);
        eval("(" + page.animationCode + ")(context, page);");
    };
    return self;
};

export { animationDrawer };