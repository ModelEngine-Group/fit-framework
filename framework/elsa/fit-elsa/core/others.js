/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, DIVISION, DOCK_MODE, PAGE_MODE} from '../common/const.js';
import {vector} from './vector.js';
import {container} from './container.js';
import {cachePool} from './shape.js';

import {canvasContainerDrawer} from './drawers/containerDrawer.js';
import {compareAndSet, getDistance, isPointInRect, sleep} from '../common/util.js';
import {simpleCanvasDrawer} from './drawers/canvasDrawer.js';

const RESOURCE_PATH = "../../resources/";

/**
 * 五角星
 * 辉子 2020
 */
const star = (id, x, y, width, height, parent, drawer) => {
    const WIDTH = 50;
    let self = vector(id, x, y, WIDTH, WIDTH, parent);
    self.borderWidth = 1;
    self.backAlpha = 1;
    self.globalAlpha = 1;
    self.borderColor = self.focusBorderColor = "silver";
    self.type = "star";
    self.backColor = "red";
    self.text = "";
    self.starNumber = 5;
    self.borderWidth = 0;

    let drawStar = function (ctx, x, y, points, radius1, radius2, alpha0) {
        let pointsVal = points;
        let i;
        let angle;
        let radius;
        if (radius2 !== radius1) {
            pointsVal = 2 * pointsVal;
        }
        ctx.beginPath();
        for (i = 0; i <= pointsVal; i++) {
            angle = (i * 2 * Math.PI / pointsVal) - (Math.PI / 2) + alpha0;
            radius = i % 2 === 0 ? radius1 : radius2;
            ctx.lineTo(x + (radius * Math.cos(angle)), y + (radius * Math.sin(angle)));
        }
        angle = (2 * Math.PI / pointsVal) - (Math.PI / 2) + alpha0;
        radius = radius2;
        ctx.closePath();
        ctx.globalAlpha = self.get('backAlpha');
        ctx.fill();
        ctx.globalAlpha = self.get('globalAlpha');
        ctx.stroke();
    };
    self.drawStatic = (context, x, y, width, height) => {
        let r1 = height / 2;
        let r2 = r1 / 2.8;
        context.strokeStyle = self.getBorderColor();
        context.fillStyle = self.backColor;
        context.lineWidth = self.get('borderWidth');
        drawStar(context, x + (width / 2), y + (height / 2), self.starNumber, r1, r2, 0);
    };
    self.drawDynamic = (context, width, height) => {
    };
    return self;
};

/**
 * 倒计时器
 * 辉子 2020
 */
const countdown = (id, x, y, width, height, parent) => {
    const STATUS = {RUNNING: "running", PAUSING: "pausing"};
    const WIDTH = 200;
    const HEIGHT = 65;
    let self = vector(id, x, y, WIDTH, HEIGHT, parent);
    self.type = "countdown";
    self.cornerRadius = 7;
    self.text = "";
    self.fontSize = 35;
    self.initValue = 10;
    self.nowValue = 0;
    self.targetTime = undefined;
    self.enableAnimation = true;
    self.getNowSeconds = () => Math.round(new Date().getTime() / 1000);
    self.now = self.getNowSeconds();
    self.status = STATUS.PAUSING;

    let formatNumber = number => number < 10 ? ("0" + number) : number;

    self.start = () => self.status = STATUS.RUNNING;
    self.pause = () => self.status = STATUS.PAUSING;
    self.stop = () => {
        self.status = STATUS.PAUSING;
        self.nowValue = -1;
    }

    let beginEdit = self.beginEdit;
    self.beginEdit = () => {
        let editor = beginEdit.call(self);
        editor.style.fontSize = self.get("fontSize") * self.width / self.originWidth + "px";
        editor.style.background = "white";
        editor.innerText = Math.round(self.initValue / 60);
    };

    self.edited = editor => {
        try {
            self.initValue = Math.round(parseInt(editor.innerText) * 60);
            self.text = "";
        } catch (e) {
            // 没关系，继续，不影响其他错误信息的处理.
        }
    };

    self.timeUping = () => {
        // 没关系，继续.
    };
    self.timing = async () => {
        while (1 === 1) {
            await sleep(1000);
            if (self.status === STATUS.PAUSING) {
                continue;
            }
            if (self.nowValue === 0) {
                self.nowValue = self.initValue;
            }

            if (self.targetTime) {
                self.nowValue = Math.round(new Date(self.targetTime).getTime() / 1000) - Math.round(new Date().getTime() / 1000);
            } else {
                self.nowValue -= 1;
            }
            if (self.nowValue < 10) {
                self.timeUping();
            }
            if (self.nowValue === 0) {
                self.status = STATUS.PAUSING;
                self.timeUp();
                // return;
            }
            self.ticking && self.ticking();
        }
    }
    self.drawStatic = (context, x, y, width, height) => {
      let yVal = y;
      yVal -= 2;
        let hours = Math.floor(self.nowValue / (60 * 60));
        let rest = self.nowValue % (60 * 60);
        let mins = Math.floor(rest / 60);
        let seconds = rest % 60;

        // draw time
        context.fillStyle = self.get("fontColor");
        context.strokeStyle = "whitesmoke";
        context.font = "normal " + self.get("fontWeight") + " " + (hours > 99 ? "30" : "35") + "px " + self.get("fontFace");
        context.fillText(`${formatNumber(hours)} : ${formatNumber(mins)} : ${formatNumber(seconds)}`, x + 8, yVal + 40);

        // draw caption
        context.font = "normal bold 10px Arial";
      context.fillText('hour', x + 18, yVal + 60);
      context.fillText('minute', x + 82, yVal + 60);
      context.fillText('second', x + 148, yVal + 60);

        // draw underline
        context.strokeStyle = self.get("borderColor");
        context.lineWidth = 1;
        context.beginPath();
      context.moveTo(x + 10, yVal + 48);
      context.lineTo(x + 45, yVal + 48);
        context.stroke();
        context.beginPath();
      context.moveTo(x + 80, yVal + 48);
      context.lineTo(x + 115, yVal + 48);
        context.stroke();
        context.beginPath();
      context.moveTo(x + 148, yVal + 48);
      context.lineTo(x + 183, yVal + 48);
        context.stroke();

        // draw percentage
        if (self.initValue === 0) {
            return;
        }
        let totalWidth = WIDTH - 20;
        let percent = self.nowValue / self.initValue;

        context.fillStyle = "whitesmoke";
        context.beginPath();
      context.rect(x + 7, yVal + 3, totalWidth, 2);
        context.fill();

        context.fillStyle = "green";
        if (percent < 0.8) {
            context.fillStyle = "gold";
        }
        if (percent < 0.6) {
            context.fillStyle = "orange"
        }
        if (percent < 0.4) {
            context.fillStyle = "darkorange";
        }
        if (percent < 0.2) {
            context.fillStyle = "red";
        }

        context.beginPath();
      context.rect(x + 8, yVal + 3, Math.round(totalWidth * percent), 2);
        context.fill();
    };
    self.drawDynamic = context => {
    };
    self.timeUp = () => {
        // 没关系，继续.
    };

    self.serializedFields.batchAdd("initValue", "targetTime");
    self.addDetection(["nowValue"], (property, value, preValue) => {
        self.invalidate();
    });
    self.addDetection(["initValue"], (property, value, preValue) => {
        if (self.nowvalue === preValue) {
            self.nowValue = value;
            self.invalidate();
        }
    });

    self.timing();
    return self;
};

/**
 * 计时器
 * 辉子 2020
 */
const timer = (id, x, y, width, height, parent) => {
    let self = countdown(id, x, y, width, height, parent);
    self.type = "timer";
    self.initValue = 0;
    self.enableAnimation = true;

    self.drawDynamic = (context, width, height) => {
        let nowValue = self.getNowSeconds() - self.now;
        if (nowValue !== self.nowValue) {
            self.nowValue = nowValue;
            self.invalidate();
        }
    };

    self.addDetection(["status"], (property, value, preValue) => {
        self.initValue = 0;
    });
    return self;
};

/**
 * 饼图
 * 辉子 2020
 */
const pieChart = (id, x, y, width, height, parent) => {
    const R1 = 25;
    const R2 = 150;
    const R3 = 30;
    const BORDER_WIDTH = 3;
    let self = container(id, x, y, width, height, parent, canvasContainerDrawer);
    self.type = "pieChart";
    self.text = "Elsa Chart";
    self.hAlign = ALIGN.LEFT;
    self.width = self.height = 2 * R2;
    self.borderWidth = 0;
    self.values = [10, 20, 15, 5, 25, 15];
    self.titles = ["A", "B", "C", "D", "E", "Fit piechart"];
    self.colors = ["steelblue", "darkorange", "OLIVE", "teal", "darkred", "green", "#EAC117", "red", "gray",
        "lightblue", "#CD7F32", "steelblue"];
    self.enableAnimation = true;

    let drawCenter = (context, x, y) => {
        let r1 = R1 * self.width / (2 * R2);
        let r3 = R3 * self.width / (2 * R2);
        context.beginPath();
        let g1 = context.createRadialGradient(self.width / 2 + x, self.height / 2 + y, r1, self.width / 2 + x, self.height / 2 + y, r3);
        g1.addColorStop(0, "gray");
        g1.addColorStop(1, "rgba(255,255,255,0");
        context.arc(self.width / 2 + x, self.height / 2 + y, r3, 0, 2 * Math.PI);
        context.fillStyle = g1;
        context.fill();

        context.beginPath();
        context.arc(self.width / 2 + x, self.height / 2 + y, r1, 0, 2 * Math.PI);
        let g2 = context.createLinearGradient(self.width / 2 - r1, self.height / 2, self.width / 2 + r1, self.height / 2);
        g2.addColorStop(0, "lightgray");
        g2.addColorStop(1, "white");
        context.fillStyle = g2;
        context.strokeStyle = "white";
        context.fill();
        context.stroke();
    };

    let txtInvalidated = true;
    let drawPie = (context, x, y) => {
        const r = self.width / 2;
        const o = 10;
        let total = self.values.sum(v => v);
        let soFar = 0;
        self.values.forEach((v, i) => {
            let offset = i % 2;
            context.beginPath();
            context.arc(x + r, y + r, r - (1 + offset) * o, soFar * 2 * Math.PI, (soFar + v / total) * 2 * Math.PI);
            context.lineTo(x + r, y + r);
            context.closePath();
            context.fillStyle = context.strokeStyle = self.colors[i % self.colors.length];
            context.fill();
            context.stroke();
            context.beginPath();
            context.arc(x + r, y + r, r - (1.5 + offset) * o, soFar * 2 * Math.PI, (soFar + v / total) * 2 * Math.PI);
            context.lineTo(x + r, y + r);
            context.closePath();
            context.fillStyle = offset === 0 ? "RGB(240,240,240)" : "RGB(245,245,245)";
            context.fill();
            soFar += v / total;

            while (self.getShapes().filter(s => s.id === (self.id + i)).length > 1) {
                self.getShapes().find(s => s.id === (self.id + i)).remove();
            }

            let txt = self.getShapes().find(s => s.id === (self.id + i));
            if (txt === undefined) {
                txt = self.page.createShape("text", x + self.x + self.width / 2, y + self.y + self.height / 2);
                txt.pad = txt.margin = 0;
                txt.width = 60;
                txt.height = 28;
                txt.fontFace = "arial black";
                txt.backColor = "transparent";
                txt.fontColor = self.colors[i % self.colors.length];
                txt.container = self.id;
                txt.id = self.id + i;
                txt.deletable = false;
            }
            txt.fontSize = 20 * self.width / (2 * R2);
            txt.text = Math.round(v * 100 / total) + "%";
            if (txtInvalidated) {
                const r2 = self.width / 4;
                let tDeg = (soFar - v / (2 * total)) * 2 * Math.PI;
                let rate = 4 / 7;
                let x1 = r2 * Math.cos(tDeg) - txt.fontSize * 2;
                let y1 = r2 * Math.sin(tDeg) - txt.fontSize / 2;
                txt.moveTo(self.x + self.width / 2 + x1 + x, self.y + self.height / 2 + y1 + y);
            }
            ;txt.unSelect();
        });
        txtInvalidated = false;
    };

    let drawTitle = (context, x, y) => {
        self.titles.forEach((title, i) => {
            let offset = i * 12 + 2;
            context.fillStyle = context.strokeStyle = self.colors[i % self.colors.length];
            context.fontSize = 10;
            context.beginPath();
            context.fillRect(x + self.width - self.width / 5, y + offset - 7, 5, 5);
            context.fillText(title, x + self.width - self.width / 5 + 10, y + offset);
        });
    };

    self.resized = () => {
        txtInvalidated = true;
    }

    self.drawer.drawStatic = (context, x, y) => {
        self.height = self.width;
        let x1 = x;
        let y1 = y;
        if (self.titles.length > 0) {
            x1 -= 10;
            y1 += 20
        }
        drawPie(context, x1, y1);

        drawCenter(context, x1, y1);

        drawTitle(context, x, y);
    };
    let degree = 0;
    let drawDynamic = self.drawer.drawDynamic;
    self.drawer.drawDynamic = (context, x, y) => {
        drawDynamic.call(self.drawer, context, x, y)

        context.save();
        context.translate(1, 1);
        if (self.titles.length > 0) {
            context.translate(-10, 20);
        }
        context.rotate(degree);
        context.beginPath();
        let r1 = R1 * self.width / (2 * R2) - 1;
        context.arc(0, 0, r1, 0, 2 * Math.PI);
        let g2 = context.createLinearGradient(-r1, 0, r1, 0);
        g2.addColorStop(0, "silver");
        g2.addColorStop(1, "white");
        context.fillStyle = g2;
        context.fill();
        context.restore();

        degree += 0.01;
        if (degree >= 360) {
            degree = 0;
        }
    };

    self.serializedFields.batchAdd("values");
    return self;
};

/**
 * 质子
 * 辉子 2021
 */
const calculator = (id, x, y, width, height, parent) => {
    let self = container(id, x, y, width, height, parent, canvasContainerDrawer);
    self.type = "calculator";
    self.hideText = false;
    self.hAlign = ALIGN.RIGHT;
    self.itemPad = [6, 6, 100, 6];
    self.padTop = 20;
    self.pad = 10;
    self.width = 300;
    self.height = 420;
    self.headColor = self.backColor = "whitesmoke";
    self.borderColor = "gray";
    self.borderWidth = 1;
    self.backcolor = "white";
    self.fontSize = 48;
    self.shadow = true;
    self.text = "";
    self.formular = "";
    self.division = DIVISION.FOUR;
    self.dockMode = DOCK_MODE.HORIZONTAL;
    self.itemSpace = 0;
    self.dynamicAddItem = false;

    self.serializedFields.batchDelete("text");

    let values = [];
    values.push(["MR", "←", "－", "＋", "＝", "?"]);// i==0
    values.push(["M-", "×", "9", "6", "3", "·"]);// i==1
    values.push(["M+", "÷", "8", "5", "2", "0"]);// i==2
    values.push(["Mc", "c", "7", "4", "1", "%"]);// i==3

    let mapping = {};
    mapping["－"] = "-";
    mapping["＋"] = "+";
    mapping["×"] = "*";
    mapping["÷"] = "/";
    mapping["·"] = ".";

    let compute = {};
    compute["＝"] = () => self.text = Math.round((eval("(" + self.formular + ")")) * 100000000) / 100000000;
    compute["c"] = () => self.text = "";
    let calculate = input => {
        if (compute[input] !== undefined) {
            compute[input]();
        } else {
            self.text += input;
            self.formular += mapping[input] === undefined ? input : mapping[input];
        }
    };

    self.deSerialized = () => self.initialize();
    self.initialize = () => {
        self.drawer.parent.style.overflow = "hidden";
        for (let i = 0; i < 4; i++) {
            let col = self.page.createNew("container", self.x, self.y);
            col.serializable = false;
            col.container = self.id;
            col.borderWidth = 0;
            col.itemPad = [10, 10, 3, 3];
            col.itemSpace = 10;
            col.backColor = "white";
            col.text = "column" + i;
            col.vAlign = ALIGN.TOP;
            col.division = DIVISION.SIX;
            col.dockMode = DOCK_MODE.VERTICAL;
            col.editable = false;
            col.dynamicAddItem = false;
            for (let j = 0; j < 6; j++) {
                let cell = self.page.createNew("rectangle", col.x, col.y);
                cell.serializable = false;
                cell.padTop = 10;
                cell.cornerRadius = 12;
                cell.fontSize = 15;
                cell.backColor = "white";
                cell.editable = false;
                cell.container = col.id;
                cell.text = values[i][j];
                cell.click = () => calculate(cell.text);
            }
        }
    };
    return self;
};

const score = (id, x, y, width, height, parent) => {
    let self = vector(id, x, y, width, height, parent);
    self.type = 'score';
    self.serializable = false;
    self.borderWidth = 0;
    self.originWidth = self.width = 100;
    self.originHeight = self.height = 50;
    self.fontColor = "gray";
    self.fontSize = 40;
    self.enableAnimation = true;
    self.globalAlpha = 1;

    self.now = 0;
    self.drawDynamic = (context, x, y, width, height) => {
        self.fontColor = self.text[0] === "+" ? "orange" : "red";
        const TIMES = 30;
        if (self.now > TIMES) {
            self.remove();
            return;
        }
        self.globalAlpha -= 1 / TIMES;
        self.y -= 1;
        self.now++;
        self.invalidate();
    };
    return self;
};

const wantAndRefuseKiss = (id, x, y, width, height, parent) => {
    const COLORS = ["darkgreen", "steelblue", "darkorange", "teal"];
    let self = vector(id, x, y, width, height, parent);
    self.serializable = false;
    self.type = "wantAndRefuseKiss";
    self.enableAnimation = true;
    self.originWidth = self.width = 120;
    self.originHeight = self.height = 45;
    self.globalAlpha = 1;
    self.fontSize = 30;
    self.fontColor = "whitesmoke";
    self.borderColor = "red";
    self.backColor = COLORS[Math.floor(Math.random() * 4)];
    self.borderWidth = 0;
    self.pad = 1;
    self.text = "";
    self.host;
    self.now = 0;
    self.drawStatic = (context, x, y, width, height) => {
        if (self.text === "") {
            return;
        }
        context.font = self.get("fontStyle") + " " + self.get("fontWeight") + " " + self.get("fontSize") + "px " + self.get("fontFace");
        const w = context.measureText(self.text).width;
        self.originWidth = self.width = w + 30;

        context.roundRect(x, y, self.width - 5, self.height - 5, 8, self.backColor);
        context.fill();
        context.beginPath();
        if (self.offset < 0) {
            context.moveTo(0, self.height + 10);
            context.lineTo(10, self.height - 15);
            context.lineTo(10, self.height + 5);
        } else {
            context.moveTo(0, 0);
            context.lineTo(10, 25);
            context.lineTo(10, 5);
        }
        context.closePath();
        context.fill();
    };
    self.drawDynamic = (context, x, y, width, height) => {
        const TIMES = 160;
        if (self.now > TIMES) {
            (self.host) && (self.host.say === self) && (self.host.say = undefined);
            self.remove();
            return;
        }
        self.globalAlpha -= 1 / TIMES;
        self.moveTo(self.host.x + self.host.width, self.host.y + self.offset);
        self.now++;
        self.drawer.move();
        self.drawer.transform();
    };

    self.addDetection(["text"], async (property, value, preValue) => {
        self.invalidate();
    });
    return self;
};

const proton = (id, x, y, width, height, parent) => {
    const R = 25;
    let self = vector(id, x, y, width, height, parent, simpleCanvasDrawer);
    self.type = "proton";
    self.selectable = false;
    self.originWidth = 2 * R;
    self.width = 3 * R;
    self.originHeight = 2 * R;
    self.height = 3 * R;
    self.borderWidth = 0;
    self.role = "predator";// prey
    self.score = 0;
    self.text = "";
    self.hideText = true;
    self.tag = {x: x, y: y, direction: 2 * Math.PI * Math.random()};
    self.enableAnimation = true;
    self.grade = 2;
    self.margin = 15;
    self.kissed;
    self.scoreable = true;
    self.coEditingInited = false;
    self.ignoreCoEditFields = ['visible'];

    let addFixedDirtyProperties = self.addFixedDirtyProperties;
    self.addFixedDirtyProperties = (id) => {
        if (!self.coEditingInited) {
            addFixedDirtyProperties(id);
            self.coEditingInited = true;
        } else {
            self.page.dirties[id]["type"] = self.type;
            self.page.dirties[id]["pid"] = self.page.id;
        }
    }

    self.drawer.drawAnimation = () => {
        self.drawDynamic();
    };

    self.findYou = () => {
        const center = self.center();
        let shapes = self.page.filterPositionShapes(center.x, center.y);
        if (!shapes) {
            return [];
        }
        return shapes.filter(s => s.scoreable && s.role !== 'predator' && s.visible && s.id !== self.id && (Math.abs(s.center().x - center.x) < 4 * R) && (Math.abs(s.center().y - center.y) < 4 * R));
    }

    self.drawStatic = (context, x, y, width, height) => {
        const offset = 11;
        const C1 = R + self.get("pad") + self.get("margin") - offset;
        let color = self.get("borderColor");
        const g = context.createRadialGradient(C1, C1, 0, C1, C1, 30);
        g.addColorStop(0, self.role === "prey" ? "white" : color);
        g.addColorStop(1, "rgba(255,255,255,0)");
        context.fillStyle = g;

        context.arc(C1, C1, 30, 0, 2 * Math.PI);
        context.fill();

        if (self.role === "prey") {
            self.width = 2.5 * self.originWidth;
            self.height = 2.5 * self.originHeight;
            if (self.image === undefined) {
                self.image = new Image();
                self.image.src = RESOURCE_PATH + self.id + ".webp";
                self.image.onload = () => self.invalidate();
            }
            if (self.image) {
                const r = 18;
                context.save();
                context.beginPath();
                context.arc(C1, C1, r, 0, 2 * Math.PI);
                context.clip();
                context.drawImage(self.image, C1 - r, C1 - r, 2 * r, 2 * r);
                context.restore();
            }
            color = self.grade > 3 ? "darkred" : "darkgreen";
        } else {
            context.fillStyle = color;
            context.beginPath();
            context.arc(C1, C1, 3, 0, 2 * Math.PI);
            context.fill();
            context.fillStyle = color;
            context.strokeStyle = "white";

            context.save();
            context.translate(C1, C1);
            context.beginPath();
            context.moveTo(-7, -7);
            context.lineTo(12, 0);
            context.lineTo(-7, 7);
            context.lineTo(-3, 0);

            context.closePath();
            context.fill();
            context.stroke();

            context.restore();
        }

        context.strokeStyle = color;
        context.beginPath();
        context.arc(C1, C1, 18, 0, 2 * Math.PI);
        context.stroke();

    };
    self.reactCoEdit = topic => {
        self.reacted = true;
        self.drawer.move();
    }

    self.showScore = () => {
        const score = self.score - preScore;
        preScore = self.score;
        if (score === 0 || !self.visible) {
            return;
        }
        if (!self.page.scores) {
            const page = self.page;
            self.page.scores = cachePool(20, (score, x, y) => {
                const myScore = page.createNew("score", x, y);
                myScore.text = score;
                return myScore;
            }, (self, score, x, y) => {
                self.moveTo(x, y);
                self.text = score;
                self.now = 0;
                self.globalAlpha = 1;
            });
        }
        self.page.scores.show((score > 0 ? "+" : "") + score, self.x - 20, self.y - 90);
    }

    let preScore = 0;
    let time;
    let times = 0;
    self.drawDynamic = async () => {
        let color = self.get("borderColor");
        (time === undefined) && (time = new Date().getTime());
        if (self.role === "prey") {
            color = self.grade > 3 ? "darkred" : "darkgreen";
            if (self.page.graph.mode === PAGE_MODE.PRESENTATION && self.page.status === "running" && (new Date().getTime() - time) > 40) {// 随机的移动自己
                times++;
                if (times > (80 + Math.round(Math.random() * 140))) {
                    self.tag = {direction: 2 * Math.PI * Math.random()};
                    self.cachedTag = self.tag;
                    times = 0;
                } else {
                    if (self.cachedTag === undefined) {
                        self.cachedTag = {direction: self.tag.direction};
                    }
                    self.tag = self.cachedTag;
                }
                time = new Date().getTime();
            }
        } else {
            self.showScore();
        }

        if (self.reacted && self.role === "predator") {
            self.rotateDegree = self.tag.direction * 180 / Math.PI;
            self.reacted = false;
        }

        if ((self.role === "prey" || self.isRanked()) && self.visible) {
            let newFlag = false;
            if (!self.name) {
                self.name = self.page.createNew("rectangle", self.x - 10, self.y - 20);
                newFlag = true;
            }
            self.name.delayInvalidate(() => {
                if (newFlag) {
                    self.name.serializable = false;
                    self.name.tag = self.id;
                    self.name.container = self.container;
                    self.name.text = (self.text === null || self.text === undefined || self.text.trim() === "") ? self.id : self.text;
                    self.name.cornerRadius = 11;
                    self.name.pad = 0;
                    self.name.resize((self.name.text.length > 3 ? 160 : 130), 38);
                    self.name.fontSize = 28;
                    self.name.borderColor = "white";
                    self.name.fontColor = "white";
                    self.name.disableInvalidate = false;
                }
                self.name.visible = true;
                self.name.backColor = color;
                self.name.moveTo(self.x - 70, self.y - 30);
            })
        } else {
            if (self.name) {
                if (self.page.graph.mode === PAGE_MODE.VIEW || self.role === "prey") {
                    self.name.visible = false;
                } else {
                    self.name.remove();
                    self.name = undefined;
                }
            }
        }
    };

    const showDialog = (host, text, offset = -80) => {
        const page = host.page;
        if (!host.visible) {
            return;
        }

        function dialogPropertiesSet(dialog, offset, host, text) {
            dialog.offset = offset;
            dialog.host = host;
            dialog.text = text;
            (offset < 0) && (host.say = self);
            page.warmupAudio("bubble", "bubble.mp3");
            page.playAudio("bubble");
        }

        if (!page.wantAndRefuseKisses) {
            page.wantAndRefuseKisses = cachePool(10, (host, x, y, text, offset) => {
                const dialog = page.createNew("wantAndRefuseKiss", x, y);
                dialog.container = page;
                dialogPropertiesSet(dialog, offset, host, text);
                return dialog;
            }, (self, host, x, y, text, offset) => {
                self.resize(x, y);
                dialogPropertiesSet(self, offset, host, text);
                self.now = 0;
                self.globalAlpha = 1;
            })
        }
        page.wantAndRefuseKisses.show(host, host.x + host.width, host.y + offset, text, offset);
    }

    self.serializedFields.batchAdd("role", "grade", "kissed");

    self.isRanked = () => {
        let index = self.page.scoredShapes.indexOf(self);
        if (index < 3 && index >= 0) {
            (self.borderColor !== "red") && (self.borderColor = rankingColors[index]);
            return true;
        } else {
            if (!self.page.me || self.page.me.id !== self.id) {
                if (self.borderColor !== "red" && self.score > 0) {
                    self.borderColor = "green";
                } else {
                    self.borderColor = "lightGray";
                }
            }
            return false;
        }
    }
    const rankingColors = ["gold", "silver", "RGB(184,115,51)"];
    self.addDetection(["borderColor"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        self.invalidate();
    });

    let direction;
    let lastTime = new Date().getTime();
    const move = value => {
        const FRAME = self.page.getFrame();
        let STEP = 6;
        let R = 20;

        if (value.direction !== direction && self.role === "prey" && (new Date().getTime() - lastTime) > 3000) {
            const inReacting = self.inReacting;
            self.inReacting = false;
            direction !== undefined && (self.visible = !self.visible);
            self.inReacting = inReacting;
            direction = value.direction;
        }
        if (self.role === "prey") {
            STEP = self.grade;
        }

        let touchBorder = false;
        let x = self.x + STEP * Math.cos(value.direction);
        if (x > FRAME.x + FRAME.width - R - self.width) {
            x = FRAME.x + FRAME.width - R - self.width;
            touchBorder = true;
        }
        if (x < FRAME.x + R) {
            x = FRAME.x + R;
            touchBorder = true;
        }

        let y = self.y + STEP * Math.sin(value.direction);
        if (y > FRAME.y + FRAME.height - R - self.height) {
            y = FRAME.y + FRAME.height - R - self.height;
            touchBorder = true;
        }
        if (y < FRAME.y + R) {
            y = FRAME.y + R;
            touchBorder = true;
        }
        self.moveTo(x, y);

        if (self.page.graph.mode === PAGE_MODE.PRESENTATION && touchBorder && self.role === "prey") {
            self.tag.direction = 2 * Math.PI * Math.random();
        }
    };

    const kiss = async value => {
        const whoes = self.findYou();
        if (whoes.length !== 0) {
            self.inReacting = false;// 需要score被相应
            const treasure = whoes.find(w => w.isType('money'));
            if (treasure) {
                const score = treasure.grade * treasure.step - treasure.passed;
                if (score <= 0) {
                    return;
                }
                self.score += score;
                if (self.page.graph.mode === PAGE_MODE.PRESENTATION) {// this part has been expired
                    self.page.graph.collaboration.invoke("publish_page_data", [{
                        page: self.page.id, shape: treasure.id, skipSelf: false,
                        value: {type: "money", passed: (treasure.grade * treasure.step)}
                    }], self.page.id);
                }
                showDialog(self, "欧力给...");
                return;
            }
            const fit = whoes.find(w => w.isType('fitCoin'));
            if (fit) {
                self.score += 20;
                showDialog(self, "FIT Lab钱最多！");
                return;
            }
            const prey = whoes.find(w => w.role === "prey");
            if (prey) {// 可以直接得分了
                self.score += prey.grade;

                const say = self.page.love(prey.text);
                showDialog(self, say[0]);
                await sleep(1000);
                const feedback = self.page.feedback(say[1]);
                self.visible && showDialog(prey, feedback[0], 30);
            }
        }
    }
    self.addDetection(["kissed"], async (property, value, preValue) => {
        if (!self.isCoEditing || self.page.status !== "running") {
            return;
        }
        kiss(value);
    });
    self.addDetection(["tag"], async (property, value, preValue) => {
        if (!self.isCoEditing || self.page.status !== "running") {
            return;
        }
        move(value);
    });

    return self;

};

const wheel = (id, x, y, width, height, parent) => {
    const R = 130;
    let self = vector(id, x, y, width, height, parent);
    self.serializable = false;
    self.originWidth = self.originHeight = 2 * R;
    self.width = self.height = 2 * R;
    self.type = "wheel";
    self.borderColor = "white";
    self.backColor = "rgba(255,255,255,0.01)";
    self.borderWidth = 0;
    self.enableAnimation = true;
    self.ignorePageMode = true;
    self.focusBorderColor = "white";
    self.editable = false;

    self.holdingMovable = true;

    self.click = () => {
    };

    let commandFunction;
    self.onCommand = (dx, dy, direction) => {
        if (self.clickCode && commandFunction === undefined) {
            commandFunction = eval("(" + self.clickCode + ")");
        }
        commandFunction && commandFunction(dx, dy, direction, self);
    }
    const command = (x, y) => {
        self.dx = x - self.x - self.width / 2;
        self.dy = y - self.y - self.height / 2;
        self.direction = Math.atan(self.dy / self.dx) + (self.dx < 0 ? Math.PI : 0);
        self.onCommand(self.dx, self.dy, self.direction);
    }
    const dragTo = self.dragTo;
    self.dragTo = (position) => {
        dragTo.call(self, position);
        command(position.x, position.y);
    };

    const onMouseDown = self.onMouseDown;
    self.onMouseDown = position => {
        onMouseDown.call(self, position);
    };

    function isCurrentMousedownShape() {
        const isMousedownShapes = self.page.mousedownShapes.length > 0 && self.page.mousedownShapes.filter(s => s.shape.id === self.id).length > 0;
        const isCurrentMousedownShape = self.page.mousedownShape !== null && self.page.mousedownShape !== undefined && self.page.mousedownShape === self;
        return isCurrentMousedownShape || isMousedownShapes;
    }

    self.onMouseHold = position => {
        if (!isCurrentMousedownShape()) {
            return;
        }
        command(position.x, position.y);
    }

    self.drawStatic = (context, x, y, width, height) => {
        if (!self.visible) {
            return;
        }
        const r = R - 30;
        const cx = x + R;
        const cy = y + R;
        context.beginPath();
        context.fillStyle = "rgba(0,150,190,0.2)";
        context.strokeStyle = self.borderColor;
        context.lineWidth = 4;
        context.arc(cx, cy, r, r, 0, 2 * Math.PI);
        context.fill();
        context.stroke();
        context.beginPath();
        context.lineWidth = 1;
        context.arc(cx, cy, r - 40, r - 50, 0, 2 * Math.PI);
        context.fill();
        context.stroke();

        const drawDirection = degree => {
            context.lineWidth = 4;
            context.fillStyle = self.borderColor;
            context.save();
            context.beginPath();
            context.translate(cx, cy);
            context.rotate(Math.PI * degree / 180);
            context.moveTo(r - 20, 0);
            context.lineTo(r - 50, 15);
            context.lineTo(r - 50, -15);
            context.closePath();
            context.fill();
            context.restore();
        }
        drawDirection(0);
        drawDirection(90);
        drawDirection(180);
        drawDirection(270);
    };

    self.drawer.drawFocusFrame = context => {
    };

    self.drawDynamic = (context, x, y, width, height) => {

        if (!isCurrentMousedownShape()) {
            return;
        }
        if (!self.direction) {
            return;
        }
        const degree = self.direction;

        context.save();
        context.fillStyle = self.focusBorderColor;
        context.rotate(degree);
        context.beginPath();
        context.moveTo(120, 8);
        context.lineTo(136, 0);
        context.lineTo(120, -8);
        context.closePath();
        context.fill();

        const g = context.createRadialGradient(0, 0, 90, 0, 0, 115);
        g.addColorStop(0, "rgba(255,255,255,0)");
        g.addColorStop(0.7, "rgba(255,255,255,0.1)");
        g.addColorStop(1, self.focusBorderColor);
        context.fillStyle = g;
        context.lineWidth = 10;
        context.beginPath();
        context.moveTo(0, 0);
        context.arc(0, 0, 115, -0.2 * Math.PI, 0.2 * Math.PI);
        context.closePath();
        context.fill();

        context.restore();
    };

    return self;
}

const fire = (id, x, y, width, height, parent) => {
    const R = 60;
    let self = vector(id, x, y, width, height, parent);
    self.serializable = false;
    self.originWidth = self.originHeight = 2 * R;
    self.borderColor = "white";
    self.type = "fire";
    self.ignorePageMode = true;
    self.moveable = false;
    self.borderWidth = 0;
    self.drawStatic = (context, x, y, width, height) => {
        if (!self.visible) {
            return;
        }
        const C = R + self.get("pad") + self.get("margin") - 6;
        context.fillStyle = "rgba(255,170,0,0.2)";
        context.strokeStyle = self.get("borderColor");
        context.lineWidth = 3;
        context.arc(C, C, R - 5, 0, 2 * Math.PI);
        context.fill();
        context.stroke();

        context.beginPath
        context.lineWidth = 1;
        context.strokeStyle = context.fillStyle;
        context.arc(C, C, R - 2, 0, 2 * Math.PI);
        context.stroke();

    };

    return self;
};
const fullScreen = (id, x, y, width, height, parent) => {
    const R = 50;
    let self = vector(id, x, y, width, height, parent);
    self.originWidth = self.originHeight = 2 * R;
    self.borderColor = "white";
    self.type = "fullScreen";
    self.borderWidth = 0;
    self.drawStatic = (context, x, y, width, height) => {
        self.visible = !(self.page.graph.mode === PAGE_MODE.PRESENTATION);
        if (!self.visible) {
            return;
        }

        const r = R - 2;
        const cx = x + R;
        const cy = y + R;
        context.beginPath();
        context.fillStyle = "rgba(0,150,190,0.2)";
        context.strokeStyle = self.borderColor;
        context.lineWidth = 4;
        context.arc(cx, cy, r, r, 0, 2 * Math.PI);
        context.fill();
        context.stroke();

        const drawDirection = degree => {
            context.lineWidth = 4;
            context.fillStyle = self.borderColor;
            context.save();
            context.beginPath();
            context.translate(cx, cy);
            context.rotate(Math.PI * degree / 180);
            context.moveTo(r - 10, 0);
            context.lineTo(r - 25, 10);
            context.lineTo(r - 25, -10);
            context.closePath();
            context.fill();
            context.restore();
        }
        drawDirection(0);
        drawDirection(90);
        drawDirection(180);
        drawDirection(270);

    };

    self.click = () => {
        self.page.fullScreen().then(() => {
            self.page.fillScreen(true);
            self.page.reset();
            self.visible = false;
            self.invalidate();
        })
    }
    return self;
};

const fitCoinImage = new Image();

const fitCoin = (id, x, y, width, height, parent) => {
    let self = vector(id, x, y, width, height, parent);
    self.originWidth = self.width = 200;
    self.originHeight = self.height = 220;
    self.type = "fitCoin";
    self.scoreable = true;
    self.borderWidth = 0;
    self.selectable = false;
    self.text = "";

    self.drawStatic = (context, x, y, width, height) => {
        context.drawImage(fitCoinImage, 0, 70, self.originWidth, self.originHeight - 70);
    };

    return self;
};

const money = (id, x, y, width, height, parent) => {
    let self = vector(id, x, y, width, height, parent);
    self.originWidth = self.originHeight = self.width = self.height = 100;
    self.type = "money";
    self.borderWidth = 0;
    self.selectable = false;
    self.grade = 0;
    self.step = 3;
    self.passed = 0;
    self.emphasized = false;
    self.scoreable = true;
    const image = new Image();
    image.src = RESOURCE_PATH + "money1.png";
    image.onload = () => self.invalidate();
    self.ignoreCoEditFields = ['visible'];

    self.drawStatic = (context, x, y, width, height) => {
        context.drawImage(image, 0, 0, self.originWidth, self.originHeight);
    };

    self.drawDynamic = (context, x, y, width, height) => {
        context.font = "normal bold 50px impact";
        context.fillStyle = "gold";
        context.strokeStyle = "white";
        const g = self.grade * self.step - self.passed;
        const w = context.measureText(g).width;
        context.fillText(g, -w / 2, 10);
        context.strokeText(g, -w / 2, 10);
    };

    self.serializedFields.batchAdd("grade", "passed");
    self.addDetection(["grade"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        compareAndSet(image, 'src', RESOURCE_PATH + "money" + (self.grade - 1) + ".png");
        image.onload = () => self.invalidate();
        self.enableAnimation = true;
    });
    self.addDetection(["passed"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        const g = self.grade * self.step - self.passed;
        if (g > 0) {
            return;
        }
        if (self.page.graph.mode === PAGE_MODE.PRESENTATION && self.container !== "" && self.enableAnimation) {
            self.enableAnimation = false;
            // expired.....huizi
            self.page.communication.invoke("publish_page_data", [{
                page: self.page.id, shape: self.id, skipSelf: false, value: {container: "", type: "money"}
            }], self.page.id);
        }
    });
    return self;
};

const magic = (id, x, y, width, height, parent) => {
    let self = vector(id, x, y, width, height, parent);
    self.originWidth = self.originHeight = self.width = self.height = 250;
    self.type = "magic";
    self.borderColor = "white";
    self.borderWidth = 0;
    self.passed = 0;
    self.radius = self.width / 2;
    self.selectable = false;

    const image = new Image();
    image.src = RESOURCE_PATH + "blackhole.png";
    image.onload = () => self.enableAnimation = true;

    self.drawStatic = (context, x, y, width, height) => {
        context.strokeStyle = context.fillStyle = self.get("borderColor");
        context.beginPath();
        context.moveTo(0, 0);
        context.lineTo(width, height);
        context.stroke();
        context.beginPath();
        context.moveTo(width, 0);
        context.lineTo(0, height);
        context.stroke();
        context.beginPath();
        context.arc(width / 2, height / 2, 3 * width / 8, 0, 2 * Math.PI);
        context.lineWidth = width / 4;
        context.globalAlpha = 0.2;
        context.stroke();
        context.lineWidth = 3;
        context.globalAlpha = 0.7;
        context.beginPath();
        context.arc(width / 2, height / 2, width / 2 - 2, 0, 2 * Math.PI);
        context.stroke();
        context.beginPath();
        context.arc(width / 2, height / 2, width / 4, 0, 2 * Math.PI);
        context.stroke();
    };

    let degree = 0;
    self.drawDynamic = (context, x, y, width, height) => {
        const w = 20 + self.width * 4 / 10;
        const s = 4;
        const w1 = w - 20;
        const w3 = self.width / 2 + 30;
        const h3 = self.height / 2 + 30;
        context.save();
        context.rotate(degree);
        context.globalAlpha = 0.3;
        context.drawImage(image, -w3, -h3, 2 * w3, 2 * h3);
        context.fillStyle = self.get("borderColor");
        context.globalAlpha = 0.5;
        context.lineWidth = 2;
        context.beginPath();
        context.moveTo(0, -w);
        context.lineTo(-s, -s);
        context.lineTo(-w, 0);
        context.lineTo(-s, s);
        context.lineTo(0, w);
        context.lineTo(s, s);
        context.lineTo(w, 0);
        context.lineTo(s, -s);
        context.lineTo(0, -w);
        context.closePath();
        context.fill();
        context.dynamicEllipse(-w1, -w1, 2 * w1, 2 * w1, 1, self.get("borderColor"), "", 0, 7, 0.6);
        context.restore();
        degree += 0.005;
        if (degree > 360) {
            degree = 0;
        }

        context.font = "normal bold 70px impact";
        context.fillStyle = "lightgray";
        context.strokeStyle = "white";
        const g = 5 - self.passed;
        const w2 = context.measureText(g).width;

        context.fillText(g, -w2 / 2, 30);
        context.strokeText(g, -w2 / 2, 30);
        if (g <= 0) {
            self.remove();
            self.enableAnimation = false;
            self.page.sm.updateShapes(writer => writer.deleteShape(self));
        }
    };

    self.serializedFields.batchAdd("passed");

    self.addDetection(["passed"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        const pc = self.center();
        self.page.sm.getShapes(s => {
            const pc1 = s.center();
            return s.isType('proton') && s.score > 0 && s.id === self.page.graph.session.id && getDistance(pc1.x, pc1.y, pc.x, pc.y) < self.radius;
        }).forEach(s => s.score -= 1);
    });

    return self;
};

const nameCard = (() => {
    const icons = [];
    const icon1 = new Image();
    icon1.src = RESOURCE_PATH + "NICO1.png";
    icons.push(icon1);
    const icon2 = new Image();
    icon2.src = RESOURCE_PATH + "NICO2.png";
    icons.push(icon2);
    const icon3 = new Image();
    icon3.src = RESOURCE_PATH + "NICO3.png";
    icons.push(icon3);
    const icon4 = new Image();
    icon4.src = RESOURCE_PATH + "NICO4.png";
    icons.push(icon4);
    return (id, x, y, width, height, parent) => {
        let self = vector(id, x, y, width, height, parent);
        self.type = "nameCard";
        self.enableAnimation = true;
        self.originWidth = self.width = 150;
        self.originHeight = self.height = 80;
        self.shadow = true;
        self.value = "-aaa";
        self.hideText = true;
        self.backColor = "white";
        self.globalAlpha = 1;
        self.rotateDegree = 0;
        self.drawer.displayBackground = true;
        self.image2 = new Image();
        self.image2.src = RESOURCE_PATH + "x.png";
        self.image2.onload = () => {
            self.invalidate();
        }

        self.image3 = new Image();
        self.image3.src = RESOURCE_PATH + "bingo.png";

        self.randomIcon = () => {
            self.image1 = icons[Math.floor(Math.random() * 4)];
            self.image1.onload = () => {
                self.invalidate();
            }
        };
        self.randomIcon();
        self.confirm = () => {
            self.invalidate();
        };

        self.offsetX = 0;
        self.transformX = (offset) => {
            self.offsetX += offset;
            self.drawer.parent.style.transform = 'translate(' + self.offsetX + 'px, 0px)';
        }

        self.drawStatic = (context, x, y, width, height) => {
            if (self.width / self.originWidth > 0.3) {
                context.drawImage(self.image2, 125, 10, 20, 20);
                if (self.bingo) {
                    const offx = 65;
                    const step = 18;
                    const offy = 11;
                    const w = 16;
                    context.drawImage(self.image3, offx, offy, w, w);
                    context.drawImage(self.image3, offx + step, offy, w, w);
                    context.drawImage(self.image3, offx + 2 * step, offy, w, w);
                }
            }
        };

        const steps = 50;
        let direction = 2 * Math.PI * Math.random();
        let prison;
        self.drawDynamic = (context, x, y, width, height) => {
            context.drawImage(self.image1, -70, -30, 60, 60);
            if (self.width / self.originWidth > 0.3) {
                context.font = "normal bold 13px arial";
                context.strokeStyle = context.fillStyle = self.bingo ? "darkred" : self.get("fontColor");
                context.lineWidth = 2;
                context.fillText(self.eid, 0, 5);
                context.fillText(self.value, 15, 25);
            }
            self.drawer.parent.style.borderColor = self.bingo ? "darkred" : "dimgray";
            self.drawer.parent.style.boxShadow = self.drawer.parent.style.borderColor + " " + self.get("shadowData");

            if (self.ending && !self.bingo) {
                self.remove();
                return;
            }

            if (self.targetX !== undefined) {

                const inReacting = self.inReacting;
                self.inReacting = true;
                const dx = self.distanceX / steps;
                const dy = self.distanceY / steps;
                self.x += dx;
                self.y += dy;

                const da = self.distanceAlpha / steps;
                self.globalAlpha += da;

                self.drawer.move();
                self.drawer.transform();

                if (Math.abs(self.x - self.targetX) < 1) {
                    self.targetX = undefined;
                    const off = 20 + Math.round(20 * Math.random());
                    prison = {x: self.x - off, y: self.y - off, width: 2 * off, height: 2 * off};
                }
                self.inReacting = inReacting;
                return;
            }
            if (self.targetY !== undefined) {
                const velocity = 0.2;
                const inReacting = self.inReacting;
                self.inReacting = true;
                self.x += velocity * Math.cos(direction);
                self.y += velocity * Math.sin(direction);
                self.drawer.move();
                self.inReacting = inReacting;
                if (!isPointInRect(self, prison)) {
                    direction += Math.PI / 2;
                    if (direction > 2 * Math.PI) {
                        direction -= 2 * Math.PI;
                    }
                }
            }
        };

        self.serializedFields.batchAdd("value");

        return self;
    };
});
const secondDown = (id, x, y, width, height, parent) => {
    const STATUS = {RUNNING: "running", PAUSING: "pausing"};
    let self = vector(id, x, y, width, height, parent);

    self.width = self.originWidth = self.height = self.originHeight = 200;
    self.type = "secondDown";
    self.status = STATUS.RUNNING;
    self.value = 3;
    self.enableAnimation = true;
    self.borderWidth = 0;

    self.drawStatic = (context, x, y, width, height) => {

        const x1 = x + width / 2;
        const y1 = y + height / 2;
        const w = width / 2;
        const g = context.createRadialGradient(x1, y1, w - 5, x1, y1, w + 5);
        g.addColorStop(0, "rgba(255,255,255,0");
        g.addColorStop(0.5, "teal");
        g.addColorStop(1, "rgba(255,255,255,0");
        context.strokeStyle = g;
        context.lineWidth = 10;
        context.fillStyle = "rgba(255,255,255,0.2";
        context.arc(x1, y1, w, 0, 2 * Math.PI);
        context.fill();
        context.stroke();
    };
    let degree = 0;
    self.drawDynamic = (context, x, y, width, height) => {
        context.save();
        context.rotate(degree);
        const g = context.createRadialGradient(0, 0, self.originWidth / 2, x, y, 10);
        g.addColorStop(0, "rgba(255,255,255,0.2)");
        g.addColorStop(1, "teal");// "rgba(255,255,255,1");
        context.beginPath();
        context.moveTo(0, 0);
        context.arc(0, 0, self.originWidth / 2, 0, 0.2 * Math.PI);
        context.closePath();
        context.fillStyle = g;
        context.fill();
        context.restore();

        context.fillStyle = "white";
        context.strokeStyle = "teal";
        context.font = "normal bold 100px impact";
        context.fillText(self.value, -25, 40);
        context.strokeText(self.value, -25, 40);

        degree += 0.2;
        if (degree >= 360) {
            degree = 0;
        }

    };

    self.timing = async () => {
        while (self.value > 0) {
            if (self.status === STATUS.PAUSING) {
                return;
            }
            self.ticking && self.ticking();
            await sleep(1000);
            self.value--;
        }
        self.remove();
        self.timeup && self.timeup();
    };

    self.timing();

    self.serializedFields.batchAdd("value");
    self.addDetection(["status"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        (value === STATUS.RUNNING) && self.timing();
    });
    return self;

}

const rotator = (id, x, y, width, height, parent) => {
    const drawArc = (angle, context, x, y, radius, width = 10, step = 0.015) => {
        context.beginPath();
        context.lineWidth = width;
        context.arc(x, y, radius, angle, angle + step * Math.PI);
        context.stroke();
    };
    let self = vector(id, x, y, width, height, parent);
    self.type = "rotator";
    self.allowCoEdit = false;
    self.width = self.originWidth = self.height = self.originHeight = 500;
    self.borderWidth = 0;
    self.enableAnimation = true;
    let degree = 0;
    self.drawDynamic = (context, width, height) => {
        context.fill();
        context.strokeStyle = "white";
        for (let i = 0; i < 360; i += 10) {
            drawArc((i + degree) / 180 * Math.PI, context, 0, 0, 170, 1);
            drawArc((i - degree) / 180 * Math.PI, context, 0, 0, 250, 1);
        }
        degree += 0.1;
        if (degree > 360) {
            degree = 0;
        }

    };

    return self;
};

const backLines = (id, x, y, width, height, parent) => {
    let self = vector(id, x, y, width, height, parent);
    self.type = "backLines";
    self.borderWidth = 0;
    self.allowCoEdit = false;
    self.width = self.originWidth = 1550;
    self.height = self.originHeight = 1000;
    self.drawStatic = (context, x, y, width, height) => {
      let xVal = x;
      let yVal = y;
        context.strokeStyle = "whitesmoke";
        context.fillStyle = "whitesmoke";

      xVal += 860;
      yVal += 445;
        const drawArc = (angle, context, radius, width = 10, step = 0.01) => {
            context.beginPath();
            context.lineWidth = width;
            context.arc(xVal, yVal, radius, angle, angle + (step * Math.PI));
            context.stroke();
        };
        const drawDot = (context, x, y, radius = 7) => {
            context.beginPath();
            context.arc(x, y, radius, 0, 2 * Math.PI);
            context.fill();
        }

        let degree = 30;
        for (let i = 0; i < 360; i += 10) {
            drawArc((i - degree) / 180 * Math.PI, context, 230, 15, 0.005);
        }

        context.lineWidth = 2;
        const drawCurve = (context, fx, fy, tx, ty) => {
            drawDot(context, fx, fy);
            const R = 15;
            context.beginPath();
            context.moveTo(fx, fy);
            const d1 = (tx - fx) / Math.abs(tx - fx);
            const d2 = (ty - fy) / Math.abs(ty - fy);
            context.lineTo(tx - R * d1, fy);
            context.quadraticCurveTo(tx, fy, tx, fy + R * d2);
            context.lineTo(tx, ty);
            context.stroke();
            drawDot(context, tx, ty);
        }
      drawCurve(context, xVal - 650, yVal - 300, xVal - 80, yVal - 70);
      drawCurve(context, xVal + 380, yVal - 410, xVal + 70, yVal - 70);
      drawCurve(context, xVal + 50, yVal - 140, xVal, yVal - 70);
      drawCurve(context, xVal + 50, yVal - 140, xVal + 660, yVal - 50);
      drawCurve(context, xVal + 680, yVal + 190, xVal + 50, yVal + 60);
      drawCurve(context, xVal + 150, yVal - 10, xVal + 260, yVal + 250);
      drawCurve(context, xVal + 380, yVal + 450, xVal - 30, yVal + 60);
      drawCurve(context, xVal - 850, yVal - 70, xVal - 400, yVal - 10);
      drawCurve(context, xVal - 150, yVal + 10, xVal - 400, yVal - 10);

      context.roundRect(xVal - 150, yVal - 70, 300, 130, 15, 'transparent', 'white', 2);
    };
    return self;
};

export {
    star,
    countdown,
    timer,
    pieChart,
    calculator,
    proton,
    wantAndRefuseKiss,
    score,
    wheel,
    fire,
    fullScreen,
    money,
    magic,
    nameCard,
    secondDown,
    fitCoin,
    rotator,
    backLines
};
