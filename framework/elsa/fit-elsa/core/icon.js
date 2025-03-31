/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {vector} from './vector.js';

/**
 * 一些好玩的图标
 * 辉子 2021
 */
const icon = (id, x, y, width, height, parent) => {
  let size = 64;
  let widthVal = size;
  let heightVal = size;
  let self = vector(id, x, y, widthVal, heightVal, parent);
    self.type = "icon";
    self.iconName = "plane";
    self.text = "";
    self.borderWidth = 0;
    self.enableAnimation = false;
    self.editable = false;

    self.drawStatic = (context, x, y, width, height) => {
        eval("__" + self.iconName + "(context, x, y, width, height,self)");
    };
    self.drawDynamic = (context, width, height) => {
        eval("__" + self.iconName + "Dynamic(context, x, y, width, height,self)");
    };
    return self;
};

const __plane = (context, x, y, width, height, icon) => {
    let x1 = x + width / 2 - 2;
    let y1 = y;
    let w1 = 5;
    let h1 = 23;
    let w2 = 28;
    let h2 = 35;
    let h3 = 40;
    let h4 = 35;
    let h5 = 50;
    let h6 = 55;
    let w3 = 15;
    let h7 = 60;
    let h8 = 57;
    context.beginPath();
    context.fillStyle = icon.headColor;
    context.strokeStyle = icon.backColor;
    context.moveTo(x1, y1);
    context.lineTo(x1 + w1, y1 + w1);
    context.lineTo(x1 + w1, y1 + h1);
    context.lineTo(x1 + w2, y1 + h2);
    context.lineTo(x1 + w2, y1 + h3);
    context.lineTo(x1 + w1, y1 + h4);
    context.lineTo(x1 + w1, y1 + h5);
    context.lineTo(x1 + w3, y1 + h6);
    context.lineTo(x1 + w3, y1 + h7);
    context.lineTo(x1, y1 + h8);
    context.lineTo(x1 - w3, y1 + h7);
    context.lineTo(x1 - w3, y1 + h6);
    context.lineTo(x1 - w1, y1 + h5);
    context.lineTo(x1 - w1, y1 + h4);
    context.lineTo(x1 - w2, y1 + h3);
    context.lineTo(x1 - w2, y1 + h2);
    context.lineTo(x1 - w1, y1 + h1);
    context.lineTo(x1 - w1, y1 + w1);
    context.closePath();
    context.fill();
    context.stroke();
};

const __train = (context, x, y, width, height, icon) => {
    let x1 = x + 15;
    let y1 = y + 3;
    let w1 = 30;
    let h1 = 40;
    let x2 = x1 + 5;
    let y2 = y1 + 5;
    let w2 = 20;
    let h2 = 15;
    let x3 = x2 + 1;
    let y3 = y2 + w2 + 5;
    let x4 = x3 + 15;
    let fill1 = icon.headColor;
    let stroke1 = icon.backColor;
    context.roundRect(x1, y1, w1, h1, 5, fill1, stroke1, 1);
    context.roundRect(x2, y2, w2, h2, 3, "white", stroke1, 1);
    context.dynamicEllipse(x3, y3, 4, 4, 1, stroke1, "white", 1);
    context.dynamicEllipse(x4, y3, 4, 4, 1, stroke1, "white", 1);

    let x5 = x3 + 3;
    let y5 = y3 + 10;
    let x6 = x1 - 2;
    let y6 = y5 + 17;
    context.beginPath();
    context.lineWidth = 2;
    context.strokeStyle = fill1;
    context.moveTo(x5, y5);
    context.lineTo(x6, y6);

    context.moveTo(x5 + 13, y5);
    context.lineTo(x5 + 23, y6);

    context.moveTo(x5 - 2, y5 + 4);
    context.lineTo(x5 + 15, y5 + 4);

    context.moveTo(x5 - 5, y5 + 8);
    context.lineTo(x5 + 18, y5 + 8);

    context.moveTo(x5 - 8, y5 + 12);
    context.lineTo(x5 + 21, y5 + 12);

    context.stroke();
};

const __car = (context, x, y, width, height, icon) => {
    let fill1 = icon.headColor;
    let stroke1 = icon.backColor;

    let x1 = x + 2;
    let y1 = y + 28;
    let x2 = x1 + 9;
    let y2 = y1 - 11;
    let x3 = x2 + 28;
    let x4 = x3 + 8;
    let x5 = x4 + 11;
    let x6 = x5 + 3;
    let y3 = y1 + 3;
    let y4 = y3 + 11;
    context.fillStyle = fill1;
    context.strokeStyle = stroke1;

    context.beginPath();
    context.moveTo(x1, y1);
    context.lineTo(x2, y2);
    context.lineTo(x3, y2);
    context.lineTo(x4, y1);
    context.lineTo(x5, y1);
    context.lineTo(x6, y3);
    context.lineTo(x6, y4);
    context.lineTo(x1, y4);
    context.lineTo(x1 + 3, y4);
    context.lineTo(x1, y4 - 3);
    context.closePath();
    context.fill();
    context.stroke();

    context.lineWidth = 2;
    context.beginPath();
    context.arc(x1 + 13, y1 + 12, 7, 0, Math.PI * 2);
    context.fill();
    context.stroke();

    context.beginPath();
    context.arc(x1 + 45, y1 + 12, 7, 0, Math.PI * 2);
    context.fill();
    context.stroke();

    let x7 = x1 + 4;
    let x8 = x2 + 2;
    let y5 = y2 + 3;
    let x9 = x8 + 11;
    context.fillStyle = "whitesmoke";
    context.beginPath();
    context.moveTo(x7, y1);
    context.lineTo(x8, y5);
    context.lineTo(x9, y5);
    context.lineTo(x9, y1);
    context.closePath();
    context.fill();

    let x10 = x9 + 3;
    let x11 = x10 + 10;
    let x12 = x11 + 4;
    context.beginPath();
    context.moveTo(x10, y5);
    context.lineTo(x11, y5);
    context.lineTo(x12, y1);
    context.lineTo(x10, y1);
    context.closePath();
    context.fill();
};

const __bike = (context, x, y, width, height, icon) => {
    let fill1 = icon.backColor;
    let stroke1 = icon.headColor;

    context.strokeStyle = stroke1;
    context.fillStyle = fill1;
    context.lineWidth = 3;
    let r = 10;
    let x1 = x + 12;
    let x2 = x1 + 37;
    let y1 = y + 40;
    context.beginPath();
    context.arc(x1, y1, r, 0, 2 * Math.PI);
    context.fill();
    context.stroke();

    context.beginPath();
    context.arc(x2, y1, r, 0, 2 * Math.PI);
    context.fill();
    context.stroke();

    let x3 = x1 + 10;
    let y2 = y1 - 15;
    let x4 = x3 + 17;
    let x5 = x4 - 10;
    context.beginPath();
    context.moveTo(x1, y1);
    context.lineTo(x3, y2);
    context.lineTo(x4, y2);
    context.lineTo(x5, y1);
    context.lineTo(x3, y1);
    context.closePath();
    context.stroke();

    let x6 = x2 - 12;
    let y3 = y1 - 26;
    context.beginPath();
    context.moveTo(x2, y1);
    context.lineTo(x6, y3);
    context.lineTo(x6 + 4, y3);
    context.lineTo(x6 - 4, y3);
    context.stroke();

    let x7 = x3 - 2;
    let y4 = y3 + 6;
    context.beginPath();
    context.moveTo(x5, y1);
    context.lineTo(x7, y4);
    context.lineTo(x7 + 4, y4);
    context.lineTo(x7 - 4, y4);
    context.stroke();
};

const __man = (context, x, y, width, height, icon) => {
    let fill1 = icon.headColor;
    let stroke1 = icon.backColor;

    context.strokeStyle = stroke1;
    context.fillStyle = fill1;
    let x1 = x + width / 2;
    let y1 = y + 10;
    let r1 = 5;
    context.beginPath();
    context.arc(x1, y1, r1, 0, 2 * Math.PI);
    context.stroke();
    context.fill();

    let x2 = x1 - 6;
    let y2 = y1 + 8;
    let r2 = 4;
    let x3 = x2 + r2 + 1;
    let y3 = y2 - r2;
    let x4 = x3 + r2 - 2;
    let y4 = y2 + 2;
    let x5 = x4 - 1;
    let y5 = y4 + 16;
    let x6 = x5 + 12;
    let y6 = y5 + 17;
    let x7 = x6;
    let y7 = y6 + 4;
    let r3 = 2;
    let x8 = x6 - 4;
    let y8 = y6 + 3;
    let x9 = x8 - 16;
    let y9 = y8 - 23;
    let x10 = x9 + 1;
    let y10 = y9 - 9;
    let x11 = x10 - 3;
    let y11 = y10 + 2;
    let x12 = x11 - 3;
    let y12 = y11 + 8;
    let x13 = x12 - 4;
    let y13 = y12 - 2;
    let x14 = x13 + 4;
    let y14 = y13 - 10;
    context.beginPath();
    context.moveTo(x2, y2);
    context.arcTo(x3, y3, x4, y4, r2);
    context.lineTo(x4, y4);
    context.lineTo(x5, y5);
    context.lineTo(x6, y6);
    context.arcTo(x7, y7, x8, y8, r3);
    context.lineTo(x8, y8);
    context.lineTo(x9, y9);
    context.lineTo(x10, y10);
    context.lineTo(x11, y11);
    context.lineTo(x12, y12);
    context.arcTo(x13 + 1, y12 + 1, x13, y13, r3);
    context.lineTo(x13, y13);
    context.lineTo(x14, y14);
    context.closePath();
    context.stroke();
    context.fill();

    x2 = x1 - 8, y2 = y1 + 28;
    x3 = x2 + 3, y3 = y2 + 4;
    x4 = x3 - 8, y4 = y3 + 12;
    x5 = x4 - 4, y5 = y4 - 3;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.arcTo(x5, y4 + 1, x5, y5, r3);
    context.lineTo(x5, y5);
    context.closePath();
    context.stroke();
    context.fill();

    x2 = x1 + 3, y2 = y1 + 14;
    x3 = x2 + 9, y3 = y2 + 5;
    x4 = x3 - 2, y4 = y3 + 3;
    x5 = x2, y5 = y2 + 4;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.arcTo(x3 + 1, y4, x4, y4, r3);
    context.lineTo(x4, y4);
    context.lineTo(x5, y5);
    context.closePath();
    context.stroke();
    context.fill();

};

const __turtle = (context, x, y, width, height, icon) => {
    let fill1 = icon.headColor;
    let stroke1 = icon.backColor;

    context.strokeStyle = stroke1;
    context.fillStyle = fill1;

    let x1 = x + width / 2;
    let y1 = y + 7;
    let x2 = x1 + 4;
    let y2 = y1 + 2;
    let x3 = x2 + 2;
    let y3 = y2 + 7;
    let x4 = x2;
    let y4 = y3 + 3;
    let x5 = x4 - 8;
    let y5 = y4;
    let x6 = x5 - 2;
    let y6 = y3;
    let x7 = x6 + 2;
    let y7 = y2;
    context.beginPath();
    context.moveTo(x1, y1);
    context.lineTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.lineTo(x5, y5);
    context.lineTo(x6, y6);
    context.lineTo(x7, y7);
    context.closePath();
    context.fill();

    x2 = x1;
    y2 = y1 + 16;
    x3 = x2 + 14, y3 = y2 - 7;
    x4 = x3 + 2, y4 = y3 + 11;
    x5 = x2 - 16, x6 = x2 - 14;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.lineTo(x5, y4);
    context.lineTo(x6, y3);
    context.closePath();
    context.fill();
    context.stroke();

    context.fillStyle = fill1;
    x2 = x1 + 14, y2 = y1 + 9;
    x3 = x2 + 2, y3 = y2 + 11;
    x4 = x3 + 12, y4 = y3 + 3;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.closePath();
    context.fill();
    context.stroke();

    x2 = x1 - 14, x3 = x2 - 2, x4 = x3 - 12;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.closePath();
    context.fill();
    context.stroke();

    x2 = x1 + 4, y2 = y1 + 44;
    x3 = x2 + 5, y3 = y2 - 5;
    x4 = x3 + 2, y4 = y3 + 10;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.closePath();
    context.fill();
    context.stroke();

    x2 = x1 - 4, x3 = x2 - 5, x4 = x3 - 2;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.closePath();
    context.fill();
    context.stroke();

    x2 = x1;
    y2 = y1 + 12;
    x3 = x2 + 2, y3 = y2;
    x4 = x3 + 10, y4 = y3 + 2;
    x5 = x4 + 4, y5 = y4 + 16;
    x6 = x1 - 16;
    x7 = x1 - 12;
    y6 = y5 + 17;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x3, y3);
    context.lineTo(x3, y3);
    context.lineTo(x4, y4);
    context.lineTo(x5, y5);
    context.lineTo(x1, y6);
    context.lineTo(x1, y6);
    context.lineTo(x6, y5);
    context.lineTo(x7, y4);
    context.closePath();
    context.fill();
    context.stroke();

};

const __maple = (context, x, y, width, height, icon) => {
    if (icon.maples === undefined) {
        let random = () => {
            let color = "darkred";
            let random = Math.random();
            if (random < 0.25) {
                color = "Firebrick";
            }
            if (random > 0.9) {
                color = "darkorange";
            }
            if (random > 0.7 && random < 0.9) {
                color = "IndianRed";
            }
            return {
                x: Math.round(10 + 50 * Math.random()),
                y: Math.round(10 + 50 * Math.random()),
                w: 2 + Math.round(2 * Math.random()),
                r: Math.round(360 * Math.random()),
                c: color
            };
        };
        icon.maples = [];
        for (let i = 0; i < 10; i++) {
            icon.maples.push(random());
        }
    }
    ;let drawMaple = (x, y, w, r, c) => {
        let color = "darkred";
        let random = Math.random();
        if (random < 0.25) {
            color = "Firebrick";
        }
        if (random > 0.9) {
            color = "darkorange";
        }
        if (random > 0.7 && random < 0.9) {
            color = "IndianRed"
        }

        const W = 11;
        context.save();
        context.scale(w / W, w / W);
        context.rotate(r * 2 * Math.PI / 360);
        context.fillStyle = context.strokeStyle = color;
        context.lineWidth = 1;
        context.beginPath();
        context.moveTo(x + 5, y);
        context.lineTo(x + 5, y + 3);
        context.lineTo(x + 3.5, y + 3);
        context.lineTo(x + 1, y + 8);
        context.lineTo(x + 3.5, y + 6);
        context.lineTo(x + 5, y + 10);
        context.lineTo(x + 6.5, y + 6);
        context.lineTo(x + 9, y + 8);
        context.lineTo(x + 6.5, y + 3);
        context.lineTo(x + 5, y + 3);
        context.closePath();
        context.fill();
        context.stroke();
        context.restore();
    };
    icon.maples.forEach(i => drawMaple(i.x, i.y, i.w, i.r, i.c));
};

const drawStar = (context, x, y, width, height, icon) => {
    context.beginPath()
    context.moveTo(0, -30);
    context.lineTo(-5, -5);
    context.lineTo(-30, 0);
    context.lineTo(-5, 5);
    context.lineTo(0, 30);
    context.lineTo(5, 5);
    context.lineTo(30, 0);
    context.lineTo(5, -5);
    context.closePath();
    context.fillStyle = icon.color;
    context.fill();
};

const __star = (context, x, y, width, height, icon) => {
    icon.opacity = Math.random();
    icon.direction = 1;
    icon.rotateDegree = Math.round(Math.random() * 360);
    let g = 200 + Math.round(Math.random() * 55);
    let b = 100 + Math.round(Math.random() * 200);
    icon.color = "rgba(255," + g + "," + b + ",1)";
    if (icon.enableAnimation) {
        return;
    }
    context.save();
    context.translate(x + 32, y + 32);
    drawStar(context, x, y, width, height, icon);
    context.restore();
};

const __starDynamic = (context, x, y, width, height, icon) => {
    if (icon.opacity > 0.5) {
        drawStar(context, x, y, width, height, icon);
    }

    let r = 20;
    context.beginPath();
    let g = context.createRadialGradient(0, 0, 3, 0, 0, r);
    g.addColorStop(0, "rgba(255,255,255," + icon.opacity + ")");
    g.addColorStop(1, 'RGBA(255,255,255,0)');
    context.fillStyle = g;
    context.arc(0, 0, r, 0, 2 * Math.PI);
    context.fill();
    if (icon.opacity >= 1) {
        icon.direction = -1;
    }
    if (icon.opacity <= 0) {
        icon.direction = 1;
    }
    icon.opacity += 0.005 * icon.direction;
};

export {icon};