/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

CanvasRenderingContext2D.prototype.dynamicEllipse = function (x, y, width, height, lineWidth, stroke, fill, fillAlpha = 0, dash = 0, strokeAlpha = 1) {
    let self = this;
    let alpha = self.globalAlpha;

    self.beginPath();
    self.setLineDash([dash, dash / 1.3]);
    if (typeof (self.ellipse) === "undefined") {
        let rate = width / height;
        self.save();
        self.translate(x + width / 2, y + height / 2);
        self.scale(rate, 1)
        self.arc(0, 0, height / 2, 0, 2 * Math.PI, false);
        self.restore();
    } else {
        self.ellipse(x + width / 2, y + height / 2, width / 2, height / 2, 0, 0, 2 * Math.PI);
    }

    if (fill !== "") {
        self.globalAlpha = fillAlpha;
        self.fillStyle = fill;
        self.fill();
        self.globalAlpha = 1;
    }

    if (lineWidth > 0) {
        self.strokeStyle = stroke;
        self.lineWidth = lineWidth;
        self.globalAlpha = strokeAlpha;
        self.stroke();
        self.globalAlpha = 1;
    }
    self.closePath();
    self.setLineDash([]);
    self.globalAlpha = alpha;
};
CanvasRenderingContext2D.prototype.dynamicRect = function (x, y, width, height, lineWidth, stroke, fill, radius = 0, fillAlpha = 1, dash = 0, strokeAlpha = 1) {
    let self = this;
    let alpha = self.globalAlpha;
    if (radius === 0) {// 一般矩形
        self.setLineDash([dash]);
        self.beginPath();
        self.rect(x, y, width, height);
        if (typeof fill !== "undefined") {
            self.globalAlpha = fillAlpha;
            self.fillStyle = fill;
            self.fill();
            self.globalAlpha = 1;
        }
        if (lineWidth > 0) {
            self.strokeStyle = stroke;
            self.lineWidth = lineWidth;

            self.globalAlpha = strokeAlpha;
            self.stroke();
            self.globalAlpha = 1;
        }
        self.closePath();
        self.setLineDash([]);
        self.globalAlpha = alpha;
        return;
    }
    if (radius > 0) {// 圆角实线矩形
        self.roundRect(x, y, width, height, radius, fill, stroke, lineWidth, fillAlpha, dash, strokeAlpha);
        self.globalAlpha = alpha;
    }
};
CanvasRenderingContext2D.prototype.roundRect = function (x, y, width, height, radius = 5, fill, stroke, lineWidth, fillAlpha = 1, dash = 0, strokeAlpha) {
    let alpha = this.globalAlpha;
    this.setLineDash([dash]);
    this.beginPath();
    this.moveTo(x + radius, y);
    this.lineTo(x + width - radius + this.lineWidth / 2, y);
    this.quadraticCurveTo(x + width, y, x + width, y + radius);
    this.lineTo(x + width, y + height - radius + this.lineWidth / 2);
    this.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    this.lineTo(x + radius - this.lineWidth / 2, y + height);
    this.quadraticCurveTo(x, y + height, x, y + height - radius);
    this.lineTo(x, y + radius - this.lineWidth / 2);
    this.quadraticCurveTo(x, y, x + radius, y);
    if (fill !== "") {
        this.globalAlpha = fillAlpha;
        this.fillStyle = fill;
        this.fill();
        this.globalAlpha = 1;
    }
    if (stroke !== "" && lineWidth > 0) {
        this.strokeStyle = stroke;
        this.lineWidth = lineWidth;
        this.globalAlpha = strokeAlpha;
        this.stroke();
        this.globalAlpha = 1;
    }
    this.closePath();
    this.setLineDash([]);
    this.globalAlpha = alpha;
};
CanvasRenderingContext2D.prototype.dashedRect = function (x, y, width, height, pattern, lineWidth, stroke, fill, fillAlpha = 1) {
    let self = this;
    self.beginPath();
    if (fill !== undefined) {
        self.globalAlpha = fillAlpha;
        self.rect(x, y, width, height);
        self.fillStyle = fill;
        self.fill();
        self.globalAlpha = 1;
    }
    self.dashedLineTo(x, y, x + width, y, pattern, lineWidth, stroke);
    self.dashedLineTo(x + width, y, x + width, y + height, pattern, lineWidth, stroke);
    self.dashedLineTo(x, y, x, y + height, pattern, lineWidth, stroke);
    self.dashedLineTo(x, y + height, x + width, y + height, pattern, lineWidth, stroke);
    self.closePath();
};
CanvasRenderingContext2D.prototype.dashedLineTo = function (fromX, fromY, toX, toY, pattern = 5, lineWidth, stroke, offset = 0) {
    // default interval distance -> 5px
    // calculate the delta x and delta y
    let dx = (toX - fromX);
    let dy = (toY - fromY);
    let distance = Math.floor(Math.sqrt(dx * dx + dy * dy));
    let dashlineInteveral = (pattern <= 0) ? distance : (distance / pattern);
    let deltay = (dy / distance) * pattern;
    let deltax = (dx / distance) * pattern;

    // draw dash line
    this.beginPath();
    this.lineWidth = lineWidth;
    if (stroke !== "") {
        this.strokeStyle = stroke;
    }
    for (let dl = 0; dl < dashlineInteveral; dl++) {
        if (dl % 2) {
            this.lineTo(offset + fromX + dl * deltax, fromY + dl * deltay);
        } else {
            this.moveTo(offset + fromX + dl * deltax, fromY + dl * deltay);
        }
    }
    this.stroke();
};
CanvasRenderingContext2D.prototype.triangle = function ({
                                                            firstPointX,
                                                        firstPointY,
                                                        secondPointX,
                                                        secondPointY,
                                                        thirdPointX,
                                                        thirdPointY,
                                                        fill,
                                                        stroke,
                                                            lineWidth,
                                                        }) {
  if (typeof (lineWidth) !== 'undefined') {
        this.lineWidth = lineWidth;
    }
    // 开始一个新的绘制路径
    this.beginPath();
    // 设置路径起点坐标(firstPointX, firstPointY)
    this.moveTo(firstPointX, firstPointY);
    // 绘制直线线段到坐标点(secondPointX, secondPointY)
    this.lineTo(secondPointX, secondPointY);
    // 绘制直线线段到坐标点(thirdPointX,thirdPointY)
    this.lineTo(thirdPointX, thirdPointY);
    // 先关闭绘制路径。注意，此时将会使用直线连接当前端点和起始端点
    this.closePath();
    // 实心三角形
  if (typeof fill !== 'undefined' && fill !== '') {
        this.fillStyle = fill;
        this.fill();
    }
  if (typeof stroke !== 'undefined' && stroke !== '') {
        this.strokeStyle = stroke;
        this.stroke();
    }
};

CanvasRenderingContext2D.prototype.drawLine = function (fromX, fromY, toX, toY, lineWidth, stroke) {
    let self = this;
    self.beginPath();
    self.strokeStyle = stroke;
    self.lineWidth = lineWidth;
    self.moveTo(fromX, fromY);
    self.lineTo(toX, toY);
    self.stroke();
    self.closePath();

};