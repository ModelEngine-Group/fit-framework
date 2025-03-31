/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const BORDER_COLOR = "#3388FF";
const DASH_WIDTH = 5;
const LINE_WIDTH = 3;

/**
 * 线的装饰器.
 *
 * @return {{}} 装饰器对象.
 */
export const eventDecorator = () => {
    const self = {};

    /**
     * 是否匹配.
     *
     * @param shape 图形.
     * @return {false|*} true/false.
     */
    self.isMatch = (shape) => {
        return shape && shape.isTypeof("jadeEvent");
    };

    /**
     * 对线进行装饰.
     *
     * @param event 线.
     */
    self.decorate = (event) => {
        if (!event) {
            return;
        }

        // 先保存原始的边框颜色.
        let originBorderColor = null;
        let originMouseInBorderColor = null;

        // 新创建的线，会设置下列属性；如果是反序列化的图形，这些属性会被覆盖.
        event.dashWidth = DASH_WIDTH;
        event.lineWidth = LINE_WIDTH;
        event.borderColor = BORDER_COLOR;
        event.mouseInBorderColor = BORDER_COLOR;

        /* runnable发生变化时，需要刷新线 */
        event.addDetection(["runnable"], (property, value, preValue) => {
            if (value === preValue || event.deletable) {
                return;
            }

            // runnable = true，需要修改borderColor.
            // runnable = false，需要将颜色还原.
            if (value) {
                !originBorderColor && (originBorderColor = event.borderColor);
                !originMouseInBorderColor && (originMouseInBorderColor = event.mouseInBorderColor);
                event.borderColor = BORDER_COLOR;
                event.mouseInBorderColor = BORDER_COLOR;
            } else {
                event.borderColor = originBorderColor;
                event.mouseInBorderColor = originMouseInBorderColor;
            }
            event.invalidateAlone();
        });
    };

    return self;
};
