/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

let sticker = (context, x = 0, y = 0) => {
    // x = x === undefined ? 0 : x;
    // y = y === undefined ? 0 : y;
    width = width === undefined ? 0 : width;
    height = height === undefined ? 0 : height;

    let self = {};
    self.viewBox = [0, 0, 36, 36];
    let stick = (draw, counter) => {
        draw(context, borderColor, backColor, text, counter);
    };
    self.stick = () => stick(self.draw);
    self.stickAnimation = counter => stick(self.drawAnimation, counter);
    self.draw = () => {
    };
    self.drawAnimation = () => {
    };
    return self;
};