/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DIRECTION} from "../common/const.js";

const OFFSET = 20;

/**
 * 提供一些方便线的功能方法.
 *
 * @returns {{}}
 */
export const lineHelper = () => {
    const self = {};
    self.brokenLineHelper = BrokenLineHelper();
    return self;
};

/**
 * 折线帮助器.
 *
 * @return {{}} 帮助器对象.
 * @constructor
 */
const BrokenLineHelper = () => {
    const self = {};

    /**
     * 南 -> 北 连接.
     *
     * @param line 线对象.
     */
    self.SN = (line) => {
        line.arrowBeginPoint.direction = DIRECTION.S;
        line.arrowEndPoint.direction = DIRECTION.N;
        if (line.height > 0) {
            line.brokenPoints.push({x: 0, y: line.height / 2});
            line.brokenPoints.push({x: line.width, y: line.height / 2});
        } else {
            if (line.toShape === "" || line.fromShape === "") {
                return;
            }
            const fromShape = line.getFromShape();
            const toShape = line.getToShape();
            let x;
            if (fromShape.x + fromShape.width < toShape.x) {
                x = (toShape.x - fromShape.x) / 2;
            } else if (toShape.x + toShape.width < fromShape.x) {
                x = -fromShape.width / 2 - (fromShape.x - toShape.x - toShape.width) / 2;
            } else {
                const distance = calcLeftRightDistance(line, fromShape, toShape);
                x = (Math.abs(distance) + OFFSET) * (distance / Math.abs(distance));
            }
            line.brokenPoints.push({x: 0, y: OFFSET});
            line.brokenPoints.push({x: x, y: OFFSET});
            line.brokenPoints.push({x: x, y: line.height - OFFSET});
            line.brokenPoints.push({x: line.width, y: line.height - OFFSET});
        }
    };

    /**
     * 南 -> 西 连接.
     *
     * @param line 线对象.
     */
    self.SW = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const fromConnectorAboveToConnector = () => {
            // fromConnector在toConnector的左边.
            if (fromShape.x + fromShapeConnector.x + line.endArrowSize < toShape.x + toShapeConnector.x) {
                line.brokenPoints.push({x: 0, y: line.height});
            } else {
                // fromConnector在toShape的上方
                if (fromShape.y + fromShape.height < toShape.y) {
                    line.brokenPoints.push({x: 0, y: (toShape.y - fromShape.y - fromShape.height) / 2});
                    line.brokenPoints.push({
                        x: line.width - OFFSET, y: (toShape.y - fromShape.y - fromShape.height) / 2
                    });
                    line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
                } else {
                    // 在y方向上，fromConnector在toShape的顶边和toConnector之间.
                    line.brokenPoints.push({
                        x: 0, y: toShape.y + toShape.height - fromShape.y - fromShapeConnector.y + OFFSET
                    });
                    line.brokenPoints.push({
                        x: line.width - OFFSET,
                        y: toShape.y + toShape.height - fromShape.y - fromShapeConnector.y + OFFSET
                    });
                    line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
                }
            }
        };

        const fromConnectorBelowToConnector = () => {
            if (toShape.x + toShapeConnector.x > fromShape.x + fromShape.width) {
                // fromShape整体在toShape的左侧边的左方.
                line.brokenPoints.push({x: 0, y: OFFSET});
                line.brokenPoints.push({x: (toShape.x - fromShape.x) / 2, y: OFFSET});
                line.brokenPoints.push({x: (toShape.x - fromShape.x) / 2, y: line.height});
            } else {
                // fromShape的右侧边越过了toShape的左侧边.
                line.brokenPoints.push({
                    x: 0, y: Math.max(OFFSET, toShape.y + toShape.height - fromShape.y - fromShape.height + OFFSET)
                });
                line.brokenPoints.push({
                    x: Math.min(line.width - OFFSET, -fromShape.width / 2 - OFFSET),
                    y: Math.max(OFFSET, toShape.y + toShape.height - fromShape.y - fromShape.height + OFFSET)
                });
                line.brokenPoints.push({
                    x: Math.min(line.width - OFFSET, -fromShape.width / 2 - OFFSET), y: line.height
                });
            }
        };

        // fromConnector在toConnector的上边.
        if (toShape.y + toShapeConnector.y > fromShape.y + fromShapeConnector.y) {
            fromConnectorAboveToConnector();
        } else {
            fromConnectorBelowToConnector();
        }
        line.arrowBeginPoint.direction = DIRECTION.S;
        line.arrowEndPoint.direction = DIRECTION.W;
    };

    /**
     * 南 -> 南 连接.
     *
     * @param line 线对象.
     */
    self.SS = (line) => {
        if (line.height > 0) {
            line.brokenPoints.push({x: 0, y: line.height + OFFSET});
            line.brokenPoints.push({x: line.width, y: line.height + OFFSET});
        } else {
            line.brokenPoints.push({x: 0, y: OFFSET});
            line.brokenPoints.push({x: line.width, y: OFFSET});
        }
        line.arrowBeginPoint.direction = DIRECTION.S;
        line.arrowEndPoint.direction = DIRECTION.S;
    };

    /**
     * 南 -> 东 连接.
     *
     * @param line 线对象.
     */
    self.SE = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const fromConnectorAboveToConnector = () => {
            if (fromShape.x + fromShapeConnector.x > toShape.x + toShapeConnector.x) {
                // fromConnector在toConnector的右边
                line.brokenPoints.push({x: 0, y: line.height});
            } else {
                // fromConnector在toConnector的左边
                if (fromShape.y + fromShape.height < toShape.y) {
                    // fromShape的底边在toShape的顶边上方
                    line.brokenPoints.push({x: 0, y: -(fromShape.y + fromShape.height - toShape.y) / 2});
                    line.brokenPoints.push({
                        x: line.width + OFFSET, y: -(fromShape.y + fromShape.height - toShape.y) / 2
                    });
                    line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
                } else {
                    line.brokenPoints.push({
                        x: 0, y: toShape.y + toShape.height - fromShape.y - fromShape.height + OFFSET
                    });
                    line.brokenPoints.push({
                        x: line.width + OFFSET, y: toShape.y + toShape.height - fromShape.y - fromShape.height + OFFSET
                    });
                    line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
                }
            }
        };

        const fromConnectorBelowToConnector = () => {
            if (toShape.x + toShape.width + line.endArrowSize < fromShape.x) {
                // fromShape在toShape的右方.
                line.brokenPoints.push({x: 0, y: OFFSET});
                line.brokenPoints.push({
                    x: -((fromShape.x - toShape.x - toShape.width) / 2 + fromShape.width / 2), y: OFFSET
                });
                line.brokenPoints.push({
                    x: -((fromShape.x - toShape.x - toShape.width) / 2 + fromShape.width / 2), y: line.height
                });
            } else {
                // fromShape在toShape的右侧边的左方.
                line.brokenPoints.push({x: 0, y: OFFSET});
                line.brokenPoints.push({x: Math.max(line.width + OFFSET, fromShape.width / 2 + OFFSET), y: OFFSET});
                line.brokenPoints.push({
                    x: Math.max(line.width + OFFSET, fromShape.width / 2 + OFFSET), y: line.height
                });
            }
        };

        if (toShape.y + toShapeConnector.y > fromShape.y + fromShapeConnector.y) {
            fromConnectorAboveToConnector();
        } else {
            fromConnectorBelowToConnector();
        }
        line.arrowBeginPoint.direction = DIRECTION.S;
        line.arrowEndPoint.direction = DIRECTION.E;
    };

    /**
     * 东 -> 西 连接.
     *
     * @param line 线对象.
     */
    self.EW = (line) => {
        line.arrowBeginPoint.direction = DIRECTION.E;
        line.arrowEndPoint.direction = DIRECTION.W;
        if (line.width > 0) {
            // 既兼容连接有图形时的情况，也兼容未连接图形的情况.
            line.brokenPoints.push({x: line.width / 2, y: 0});
            line.brokenPoints.push({x: line.width / 2, y: line.height});
        } else {
            if (line.toShape === "" || line.fromShape === "") {
                return;
            }

            const fromShape = line.getFromShape();
            const toShape = line.getToShape();
            if (fromShape.y + fromShape.height < toShape.y) {
                line.brokenPoints.push({x: OFFSET, y: 0});
                line.brokenPoints.push({x: OFFSET, y: (toShape.y - fromShape.y) / 2});
                line.brokenPoints.push({x: line.width - OFFSET, y: (toShape.y - fromShape.y) / 2});
                line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
            } else if (toShape.y + toShape.height < fromShape.y) {
                line.brokenPoints.push({x: OFFSET, y: 0});
                line.brokenPoints.push({x: OFFSET, y: -((fromShape.y - toShape.y - toShape.height) / 2 + fromShape.height / 2)});
                line.brokenPoints.push({x: line.width - OFFSET, y: -((fromShape.y - toShape.y - toShape.height) / 2 + fromShape.height / 2)});
                line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
            } else {
                const distance = calcUpDownDistance(line, fromShape, toShape);
                const y = (Math.abs(distance) + OFFSET) * (distance / Math.abs(distance));
                line.brokenPoints.push({x: OFFSET, y: 0});
                line.brokenPoints.push({x: OFFSET, y: y});
                line.brokenPoints.push({x: line.width - OFFSET, y: y});
                line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
            }
        }
    };

    /**
     * 东 -> 东 连接.
     *
     * @param line 线对象.
     */
    self.EE = (line) => {
        if (line.width > 0) {
            line.brokenPoints.push({x: line.width + OFFSET, y: 0});
            line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
        } else {
            line.brokenPoints.push({x: OFFSET, y: 0});
            line.brokenPoints.push({x: OFFSET, y: line.height});
        }
        line.arrowBeginPoint.direction = DIRECTION.E;
        line.arrowEndPoint.direction = DIRECTION.E;
    };

    /**
     * 东 -> 南 连接.
     *
     * @param line 线对象.
     */
    self.ES = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const toConnectorAboveFromConnector = () => {
            if (toShape.x + toShapeConnector.x > fromShape.x + fromShapeConnector.x) {
                // toShapeConnector在fromShape右侧边的右边
                line.brokenPoints.push({x: line.width, y: 0});
            } else {
                if (toShape.y + toShape.height < fromShape.y - OFFSET) {
                    // 整个toShape处于fromShape的上方.
                    const y = (toShape.y + toShape.height - fromShape.y) / 2 - fromShape.height / 2;
                    line.brokenPoints.push({x: OFFSET, y: 0});
                    line.brokenPoints.push({x: OFFSET, y: y});
                    line.brokenPoints.push({x: line.width, y: y});
                } else {
                    line.brokenPoints.push({x: OFFSET, y: 0});
                    line.brokenPoints.push({x: OFFSET, y: fromShape.height / 2 + OFFSET});
                    line.brokenPoints.push({x: line.width, y: fromShape.height / 2 + OFFSET});
                }
            }
        };

        const toConnectorBelowFromConnector = () => {
            if (fromShape.x + fromShape.width < toShape.x) {
                // 整个fromShape都在toShape的左边.
                line.brokenPoints.push({x: (toShape.x - fromShape.x - fromShape.width) / 2, y: 0});
                line.brokenPoints.push({x: (toShape.x - fromShape.x - fromShape.width) / 2, y: line.height + OFFSET});
                line.brokenPoints.push({x: line.width, y: line.height + OFFSET});
            } else {
                const x = Math.max(OFFSET, toShape.x + toShape.width - fromShape.x - fromShape.width + OFFSET);
                const y = Math.max(line.height + OFFSET, fromShape.width / 2 + OFFSET);
                line.brokenPoints.push({x: x, y: 0});
                line.brokenPoints.push({x: x, y: y});
                line.brokenPoints.push({x: line.width, y: y});
            }
        };

        if (fromShape.y + fromShapeConnector.y > toShape.y + toShapeConnector.y) {
            toConnectorAboveFromConnector();
        } else {
            toConnectorBelowFromConnector();
        }
        line.arrowBeginPoint.direction = DIRECTION.E;
        line.arrowEndPoint.direction = DIRECTION.S;
    };

    /**
     * 东 -> 北 连接.
     *
     * @param line 线对象.
     * @constructor
     */
    self.EN = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const fromConnectorAboveToShape = () => {
            if (fromShape.x + fromShapeConnector.x < toShape.x + toShapeConnector.x) {
                // fromShapeConnector在toShapeConnector的左侧.
                line.brokenPoints.push({x: line.width, y: 0});
            } else {
                // fromShapeConnector在toShapeConnector的右侧.
                if (fromShape.y + fromShape.height + OFFSET < toShape.y) {
                    // 整个fromShape在toShape的上方.
                    line.brokenPoints.push({x: OFFSET, y: 0});
                    line.brokenPoints.push({x: OFFSET, y: (toShape.y - fromShape.y) / 2});
                    line.brokenPoints.push({x: line.width, y: (toShape.y - fromShape.y) / 2});
                } else {
                    line.brokenPoints.push({x: OFFSET, y: 0});
                    line.brokenPoints.push({x: OFFSET, y: -(fromShape.height / 2 + OFFSET)});
                    line.brokenPoints.push({x: line.width, y: -(fromShape.height / 2 + OFFSET)});
                }
            }
        };

        const fromConnectorBelowToShape = () => {
            if (fromShape.x + fromShape.width < toShape.x) {
                // 整个fromShape在toShape的左侧.
                const x = (toShape.x - fromShape.x - fromShape.width) / 2;
                line.brokenPoints.push({x: x, y: 0});
                line.brokenPoints.push({x: x, y: line.height - OFFSET});
                line.brokenPoints.push({x: line.width, y: line.height - OFFSET});
            } else {
                const x = Math.max(OFFSET, toShape.x + toShape.width - fromShape.x - fromShape.width + OFFSET);
                const y = Math.min(line.height - OFFSET, -fromShape.height / 2 - OFFSET);
                line.brokenPoints.push({x: x, y: 0});
                line.brokenPoints.push({x: x, y: y});
                line.brokenPoints.push({x: line.width, y: y});
            }
        };

        if (fromShape.y + fromShapeConnector.y < toShape.y) {
            fromConnectorAboveToShape();
        } else {
            fromConnectorBelowToShape();
        }
        line.arrowBeginPoint.direction = DIRECTION.E;
        line.arrowEndPoint.direction = DIRECTION.N;
    };

    /**
     * 西 -> 东 连接.
     *
     * @param line 线对象.
     */
    self.WE = (line) => {
        line.arrowBeginPoint.direction = DIRECTION.W;
        line.arrowEndPoint.direction = DIRECTION.E;
        if (line.width < 0) {
            line.brokenPoints.push({x: line.width / 2, y: 0});
            line.brokenPoints.push({x: line.width / 2, y: line.height});
        } else {
            if (line.toShape === "" || line.fromShape === "") {
                return;
            }

            const fromShape = line.getFromShape();
            const toShape = line.getToShape();
            let y;
            if (fromShape.y + fromShape.height + OFFSET < toShape.y) {
                // fromShape整体处于toShape的上方.
                y = (toShape.y - fromShape.y) / 2;
            } else if (toShape.y + toShape.height + OFFSET < fromShape.y) {
                // toShape整体处于fromShape的上方.
                y = -((fromShape.y - toShape.y - toShape.height) / 2 + fromShape.height / 2);
            } else {
                const distance = calcUpDownDistance(line, fromShape, toShape);
                y = (Math.abs(distance) + OFFSET) * (distance / Math.abs(distance));
            }
            line.brokenPoints.push({x: -OFFSET, y: 0});
            line.brokenPoints.push({x: -OFFSET, y: y});
            line.brokenPoints.push({x: line.width + OFFSET, y: y});
            line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
        }
    };

    /**
     * 西 -> 西 连接.
     *
     * @param line 线对象.
     */
    self.WW = (line) => {
        if (line.width > 0) {
            line.brokenPoints.push({x: -OFFSET, y: 0});
            line.brokenPoints.push({x: -OFFSET, y: line.height});
        } else {
            line.brokenPoints.push({x: line.width - OFFSET, y: 0});
            line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
        }
        line.arrowBeginPoint.direction = DIRECTION.W;
        line.arrowEndPoint.direction = DIRECTION.W;
    };

    /**
     * 西 -> 北 连接.
     *
     * @param line 线对象.
     * @constructor
     */
    self.WN = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const fromConnectorAboveToConnector = () => {
            if (fromShape.x + fromShapeConnector.x > toShape.x + toShapeConnector.x) {
                // fromShapeConnector在toShapeConnector的右侧.
                line.brokenPoints.push({x: line.width, y: 0});
            } else {
                // fromShapeConnector在toShapeConnector的左侧.
                if (fromShape.y + fromShape.height < toShape.y) {
                    // fromShape在toShape的上方.
                    line.brokenPoints.push({x: -OFFSET, y: 0});
                    line.brokenPoints.push({x: -OFFSET, y: (toShape.y - fromShape.y) / 2});
                    line.brokenPoints.push({x: line.width, y: (toShape.y - fromShape.y) / 2});
                } else {
                    line.brokenPoints.push({x: -OFFSET, y: 0});
                    line.brokenPoints.push({x: -OFFSET, y: -fromShape.height / 2 - OFFSET});
                    line.brokenPoints.push({x: line.width, y: -fromShape.height / 2 - OFFSET});
                }
            }
        };

        const fromConnectorBelowToConnector = () => {
            if (fromShape.x + fromShapeConnector.x > toShape.x + toShape.width) {
                // fromShapeConnector在toShape的右侧.
                line.brokenPoints.push({x: -(fromShape.x - toShape.x - toShape.width) / 2, y: 0});
                line.brokenPoints.push({x: -(fromShape.x - toShape.x - toShape.width) / 2, y: line.height - OFFSET});
                line.brokenPoints.push({x: line.width, y: line.height - OFFSET});
            } else {
                // fromShapeConnector在toShape右侧边的左边.
                const x = Math.min(-OFFSET, -(fromShape.x - toShape.x + OFFSET));
                const y = Math.min(-(fromShape.y - toShape.y + fromShape.height / 2) - OFFSET, -fromShape.height / 2 - OFFSET);
                line.brokenPoints.push({x: x, y: 0});
                line.brokenPoints.push({x: x, y: y});
                line.brokenPoints.push({x: line.width, y: y});
            }
        };

        if (fromShape.y + fromShapeConnector.y < toShape.y + toShapeConnector.y) {
            fromConnectorAboveToConnector();
        } else {
            fromConnectorBelowToConnector();
        }
        line.arrowBeginPoint.direction = DIRECTION.W;
        line.arrowEndPoint.direction = DIRECTION.N;
    };

    /**
     * 西 -> 南 连接.
     *
     * @param line 线对象.
     * @constructor
     */
    self.WS = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const fromConnectorBelowToConnector = () => {
            if (toShape.x + toShapeConnector.x < fromShape.x + fromShapeConnector.x) {
                // toShapeConnector在fromShape的左侧.
                line.brokenPoints.push({x: line.width, y: 0});
            } else {
                // toShapeConnector在fromShape的右侧.
                if (toShape.y + toShape.height + OFFSET < fromShape.y) {
                    // toShape在fromShape的上方，不重叠.
                    const y = (toShape.y + toShape.height - fromShape.y) / 2 - fromShape.height / 2
                    line.brokenPoints.push({x: -OFFSET, y: 0});
                    line.brokenPoints.push({x: -OFFSET, y: y});
                    line.brokenPoints.push({x: line.width, y: y});
                } else {
                    line.brokenPoints.push({x: -OFFSET, y: 0});
                    line.brokenPoints.push({x: -OFFSET, y: fromShape.height / 2 + OFFSET});
                    line.brokenPoints.push({x: line.width, y: fromShape.height / 2 + OFFSET});
                }
            }
        };

        const fromConnectorAboveToConnector = () => {
            if (fromShape.x + fromShapeConnector.x > toShape.x + toShape.width) {
                // fromShape在toShape整体的右侧.
                line.brokenPoints.push({x: (toShape.x + toShape.width - fromShape.x) / 2, y: 0});
                line.brokenPoints.push({x: (toShape.x + toShape.width - fromShape.x) / 2, y: line.height + OFFSET});
                line.brokenPoints.push({x: line.width, y: line.height + OFFSET});
            } else {
                // fromShapeConnector在toShape右侧边的左侧.
                const x = Math.min(-OFFSET, -(fromShape.x - toShape.x + OFFSET));
                const y = Math.max(toShape.y + toShape.height - fromShape.y - fromShape.height / 2 + OFFSET, fromShape.height / 2 + OFFSET);
                line.brokenPoints.push({x: x, y: 0});
                line.brokenPoints.push({x: x, y: y});
                line.brokenPoints.push({x: line.width, y: y});
            }
        };

        if (fromShape.y + fromShapeConnector.y > toShape.y + toShapeConnector.y) {
            fromConnectorBelowToConnector();
        } else {
            fromConnectorAboveToConnector();
        }
        line.arrowBeginPoint.direction = DIRECTION.W;
        line.arrowEndPoint.direction = DIRECTION.S;
    };

    /**
     * 北 -> 南 连接.
     *
     * @param line 线对象.
     */
    self.NS = (line) => {
        line.arrowBeginPoint.direction = DIRECTION.N;
        line.arrowEndPoint.direction = DIRECTION.S;
        if (line.height < 0) {
            line.brokenPoints.push({x: 0, y: line.height / 2});
            line.brokenPoints.push({x: line.width, y: line.height / 2});
        } else {
            if (line.toShape === "" || line.fromShape === "") {
                return;
            }
            const fromShape = line.getFromShape();
            const toShape = line.getToShape();
            let x;
            if (fromShape.x + fromShape.width < toShape.x) {
                x = (toShape.x - fromShape.x) / 2;
            } else if (toShape.x + toShape.width < fromShape.x) {
                x = -fromShape.width / 2 - (fromShape.x - (toShape.x + toShape.width)) / 2;
            } else {
                const distance = calcLeftRightDistance(line, fromShape, toShape);
                x = (Math.abs(distance) + OFFSET) * (distance / Math.abs(distance));
            }
            line.brokenPoints.push({x: 0, y: -OFFSET});
            line.brokenPoints.push({x: x, y: -OFFSET});
            line.brokenPoints.push({x: x, y: line.height + OFFSET});
            line.brokenPoints.push({x: line.width, y: line.height + OFFSET});
        }
    };

    /**
     * 北 -> 北 连接.
     *
     * @param line 线对象.
     */
    self.NN = (line) => {
        if (line.height > 0) {
            line.brokenPoints.push({x: 0, y: -OFFSET});
            line.brokenPoints.push({x: line.width, y: -OFFSET});
        } else {
            line.brokenPoints.push({x: 0, y: line.height - OFFSET});
            line.brokenPoints.push({x: line.width, y: line.height - OFFSET});
        }
        line.arrowBeginPoint.direction = DIRECTION.N;
        line.arrowEndPoint.direction = DIRECTION.N;
    };

    /**
     * 北 -> 东 连接.
     *
     * @param line 线对象.
     * @constructor
     */
    self.NE = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const fromConnectorBelowToConnector = () => {
            if (toShape.x + toShapeConnector.x < fromShape.x + fromShapeConnector.x) {
                // toShapeConnector在fromShapeConnector的左侧
                line.brokenPoints.push({x: 0, y: line.height});
            } else {
                // toShapeConnector在fromShapeConnector的右侧
                if (toShape.y + toShape.height < fromShape.y) {
                    // toShape整体在fromShape的上方.
                    line.brokenPoints.push({x: 0, y: (toShape.y + toShape.height - fromShape.y) / 2});
                    line.brokenPoints.push({x: line.width + OFFSET, y: (toShape.y + toShape.height - fromShape.y) / 2});
                    line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
                } else {
                    line.brokenPoints.push({x: 0, y: toShape.y - fromShape.y - OFFSET});
                    line.brokenPoints.push({x: line.width + OFFSET, y: toShape.y - fromShape.y - OFFSET});
                    line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
                }
            }
        };

        const fromConnectorAboveToConnector = () => {
            if (toShape.x + toShape.width < fromShape.x) {
                // toShape整体在fromShape的左侧.
                const x = -(fromShape.width / 2 + (fromShape.x - toShape.x - toShape.width) / 2);
                line.brokenPoints.push({x: 0, y: -OFFSET});
                line.brokenPoints.push({x: x, y: -OFFSET});
                line.brokenPoints.push({x: x, y: line.height});
            } else {
                const x = Math.max(toShape.x + toShape.width - fromShape.x - fromShape.width / 2 + OFFSET, fromShape.width / 2 + OFFSET);
                const y = Math.min(-OFFSET, toShape.y - fromShape.y - OFFSET);
                line.brokenPoints.push({x: 0, y: y});
                line.brokenPoints.push({x: x, y: y});
                line.brokenPoints.push({x: x, y: line.height});
            }
        };

        if (toShape.y + toShapeConnector.y < fromShape.y + fromShapeConnector.y) {
            fromConnectorBelowToConnector();
        } else {
            fromConnectorAboveToConnector();
        }
        line.arrowBeginPoint.direction = DIRECTION.N;
        line.arrowEndPoint.direction = DIRECTION.E;
    };

    /**
     * 北 -> 西 连接.
     *
     * @param line 线对象.
     * @constructor
     */
    self.NW = (line) => {
        const fromShape = line.getFromShape();
        const toShape = line.getToShape();
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const fromConnectorLeftOnToShape = () => {
            if (fromShape.y > toShape.y + toShapeConnector.y) {
                // fromShapeConnector在toShapeConnector的下方.
                line.brokenPoints.push({x: 0, y: line.height});
            } else {
                // fromShapeConnector在toShapeConnector的上方.
                if (fromShape.x + fromShape.width < toShape.x) {
                    // fromShape整体在toShape的左侧.
                    line.brokenPoints.push({x: 0, y: -OFFSET});
                    line.brokenPoints.push({x: (toShape.x - fromShape.x) / 2, y: -OFFSET});
                    line.brokenPoints.push({x: (toShape.x - fromShape.x) / 2, y: line.height});
                } else {
                    // fromShape的右侧边超过了toShape的左侧边.
                    line.brokenPoints.push({x: 0, y: -OFFSET});
                    line.brokenPoints.push({x: -(fromShape.width / 2 + OFFSET), y: -OFFSET});
                    line.brokenPoints.push({x: -(fromShape.width / 2 + OFFSET), y: line.height});
                }
            }
        };

        const fromConnectorRightOnToShape = () => {
            if (fromShape.y > toShape.y + toShape.height) {
                // toShape整体在fromShape的上方.
                line.brokenPoints.push({x: 0, y: -(fromShape.y - toShape.y - toShape.height) / 2});
                line.brokenPoints.push({x: line.width - OFFSET, y: -(fromShape.y - toShape.y - toShape.height) / 2});
                line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
            } else {
                // fromShape未整体在toShape的下方.
                const y = Math.min(-OFFSET, toShape.y - fromShape.y - OFFSET);
                const x = Math.min(toShape.x - fromShape.x - fromShape.width / 2 - OFFSET, -fromShape.width / 2 - OFFSET);
                line.brokenPoints.push({x: 0, y: y});
                line.brokenPoints.push({x: x, y: y});
                line.brokenPoints.push({x: x, y: line.height});
            }
        };

        if (fromShape.x + fromShapeConnector.x < toShape.x) {
            fromConnectorLeftOnToShape();
        } else {
            fromConnectorRightOnToShape();
        }
        line.arrowBeginPoint.direction = DIRECTION.N;
        line.arrowEndPoint.direction = DIRECTION.W;
    };

    /**
     * 线中只有一个连接点连接有图形，并且连接点在图形的E方向上的连接点.
     *
     * @param line 线对象.
     */
    self.E = (line) => {
        if (!line.fromShapeConnector && !line.toShapeConnector) {
            return;
        }

        const fromShapeConnectorExists = () => {
            line.arrowBeginPoint.direction = DIRECTION.E;
            const shape = line.getFromShape();
            const shapeConnector = line.fromShapeConnector;
            const lineConnector = line.toConnector;
            if (line.x + lineConnector.x > shape.x + shapeConnector.x) {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: line.width / 2, y: 0});
                    line.brokenPoints.push({x: line.width / 2, y: line.height});
                    line.arrowEndPoint.direction = DIRECTION.W;
                } else {
                    line.brokenPoints.push({x: line.width, y: 0});
                    line.arrowEndPoint.direction = line.height > 0 ? DIRECTION.N : DIRECTION.S;
                }
            } else {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: OFFSET, y: 0});
                    line.brokenPoints.push({x: OFFSET, y: line.height / 2});
                    line.brokenPoints.push({x: line.width, y: line.height / 2});
                    line.arrowEndPoint.direction = line.height > 0 ? DIRECTION.N : DIRECTION.S;
                } else {
                    line.brokenPoints.push({x: OFFSET, y: 0});
                    line.brokenPoints.push({x: OFFSET, y: line.height});
                    line.arrowEndPoint.direction = DIRECTION.E;
                }
            }
        };

        const toShapeConnectorExists = () => {
            line.arrowEndPoint.direction = DIRECTION.E;
            const shape = line.getToShape();
            const shapeConnector = line.toShapeConnector;
            const lineConnector = line.fromConnector;
            if (line.x + lineConnector.x > shape.x + shapeConnector.x) {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: line.width / 2, y: 0});
                    line.brokenPoints.push({x: line.width / 2, y: line.height});
                    line.arrowBeginPoint.direction = DIRECTION.W;
                } else {
                    line.brokenPoints.push({x: 0, y: line.height});
                    line.arrowBeginPoint.direction = line.height > 0 ? DIRECTION.S : DIRECTION.N;
                }
            } else {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: 0, y: line.height / 2});
                    line.brokenPoints.push({x: line.width + OFFSET, y: line.height / 2});
                    line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
                    line.arrowBeginPoint.direction = line.height > 0 ? DIRECTION.S : DIRECTION.N;
                } else {
                    line.brokenPoints.push({x: line.width + OFFSET, y: 0});
                    line.brokenPoints.push({x: line.width + OFFSET, y: line.height});
                    line.arrowBeginPoint.direction = DIRECTION.E;
                }
            }
        };

        if (line.fromShapeConnector) {
            fromShapeConnectorExists();
        } else {
            toShapeConnectorExists();
        }
    };

    /**
     * 线中只有一个连接点连接有图形，并且连接点在图形的N方向上的连接点.
     *
     * @param line 线对象.
     */
    self.N = (line) => {
        if (!line.fromShapeConnector && !line.toShapeConnector) {
            return;
        }

        const fromShapeConnectorExists = () => {
            line.arrowBeginPoint.direction = DIRECTION.N;
            const shape = line.getFromShape();
            const shapeConnector = line.fromShapeConnector;
            const lineConnector = line.toConnector;
            if (line.y + lineConnector.y < shape.y + shapeConnector.y) {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: 0, y: line.height / 2});
                    line.brokenPoints.push({x: line.width, y: line.height / 2});
                    line.arrowEndPoint.direction = DIRECTION.S;
                } else {
                    line.brokenPoints.push({x: 0, y: line.height});
                    line.arrowEndPoint.direction = line.width > 0 ? DIRECTION.W : DIRECTION.E;
                }
            } else {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: 0, y: -OFFSET});
                    line.brokenPoints.push({x: line.width / 2, y: -OFFSET});
                    line.brokenPoints.push({x: line.width / 2, y: line.height});
                    line.arrowEndPoint.direction = line.width > 0 ? DIRECTION.W : DIRECTION.E;
                } else {
                    line.brokenPoints.push({x: 0, y: -OFFSET});
                    line.brokenPoints.push({x: line.width, y: -OFFSET});
                    line.arrowEndPoint.direction = DIRECTION.N;
                }
            }
        };

        const toShapeConnectorExists = () => {
            line.arrowEndPoint.direction = DIRECTION.N;
            const shape = line.getToShape();
            const shapeConnector = line.toShapeConnector;
            const lineConnector = line.fromConnector;
            if (line.y + lineConnector.y < shape.y + shapeConnector.y) {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: 0, y: line.height / 2});
                    line.brokenPoints.push({x: line.width, y: line.height / 2});
                    line.arrowBeginPoint.direction = DIRECTION.S;
                } else {
                    line.brokenPoints.push({x: line.width, y: 0});
                    line.arrowBeginPoint.direction = line.width > 0 ? DIRECTION.E : DIRECTION.W;
                }
            } else {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: line.width / 2, y: 0});
                    line.brokenPoints.push({x: line.width / 2, y: line.height - OFFSET});
                    line.brokenPoints.push({x: line.width, y: line.height - OFFSET});
                    line.arrowBeginPoint.direction = line.width > 0 ? DIRECTION.E : DIRECTION.W;
                } else {
                    line.brokenPoints.push({x: 0, y: line.height - OFFSET});
                    line.brokenPoints.push({x: line.width, y: line.height - OFFSET});
                    line.arrowBeginPoint.direction = DIRECTION.N;
                }
            }
        };

        if (line.fromShapeConnector) {
            fromShapeConnectorExists();
        } else {
            toShapeConnectorExists();
        }
    };

    /**
     * 线中只有一个连接点连接有图形，并且连接点在图形的W方向上的连接点.
     *
     * @param line 线对象.
     */
    self.W = (line) => {
        if (!line.fromShapeConnector && !line.toShapeConnector) {
            return;
        }

        const fromShapeConnectorExists = () => {
            line.arrowBeginPoint.direction = DIRECTION.W;
            const shape = line.getFromShape();
            const shapeConnector = line.fromShapeConnector;
            const lineConnector = line.toConnector;
            if (line.x + lineConnector.x < shape.x + shapeConnector.x) {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: line.width / 2, y: 0});
                    line.brokenPoints.push({x: line.width / 2, y: line.height});
                    line.arrowEndPoint.direction = DIRECTION.E;
                } else {
                    line.brokenPoints.push({x: line.width, y: 0});
                    line.arrowEndPoint.direction = line.height > 0 ? DIRECTION.N : DIRECTION.S;
                }
            } else {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: -OFFSET, y: 0});
                    line.brokenPoints.push({x: -OFFSET, y: line.height / 2});
                    line.brokenPoints.push({x: line.width, y: line.height / 2});
                    line.arrowEndPoint.direction = line.height > 0 ? DIRECTION.N : DIRECTION.S;
                } else {
                    line.brokenPoints.push({x: -OFFSET, y: 0});
                    line.brokenPoints.push({x: -OFFSET, y: line.height});
                    line.arrowEndPoint.direction = DIRECTION.W;
                }
            }
        };

        const toShapeConnectorExists = () => {
            line.arrowEndPoint.direction = DIRECTION.W;
            const shape = line.getToShape();
            const shapeConnector = line.toShapeConnector;
            const lineConnector = line.fromConnector;
            if (line.x + lineConnector.x < shape.x + shapeConnector.x) {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: line.width / 2, y: 0});
                    line.brokenPoints.push({x: line.width / 2, y: line.height});
                    line.arrowBeginPoint.direction = DIRECTION.E;
                } else {
                    line.brokenPoints.push({x: 0, y: line.height});
                    line.arrowBeginPoint.direction = line.height > 0 ? DIRECTION.S : DIRECTION.N;
                }
            } else {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: 0, y: line.height / 2});
                    line.brokenPoints.push({x: line.width - OFFSET, y: line.height / 2});
                    line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
                    line.arrowBeginPoint.direction = line.height > 0 ? DIRECTION.S : DIRECTION.N;
                } else {
                    line.brokenPoints.push({x: line.width - OFFSET, y: 0});
                    line.brokenPoints.push({x: line.width - OFFSET, y: line.height});
                    line.arrowBeginPoint.direction = DIRECTION.W;
                }
            }
        };

        if (line.fromShapeConnector) {
            fromShapeConnectorExists();
        } else {
            toShapeConnectorExists();
        }
    };

    /**
     * 线中只有一个连接点连接有图形，并且连接点在图形的S方向上的连接点.
     *
     * @param line 线对象.
     */
    self.S = (line) => {
        if (!line.fromShapeConnector && !line.toShapeConnector) {
            return;
        }

        const fromShapeConnectorExists = () => {
            line.arrowBeginPoint.direction = DIRECTION.S;
            const shape = line.getFromShape();
            const shapeConnector = line.fromShapeConnector;
            const lineConnector = line.toConnector;
            if (line.y + lineConnector.y > shape.y + shapeConnector.y) {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: 0, y: line.height / 2});
                    line.brokenPoints.push({x: line.width, y: line.height / 2});
                    line.arrowEndPoint.direction = DIRECTION.N;
                } else {
                    line.brokenPoints.push({x: 0, y: line.height});
                    line.arrowEndPoint.direction = line.width > 0 ? DIRECTION.W : DIRECTION.E;
                }
            } else {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: 0, y: OFFSET});
                    line.brokenPoints.push({x: line.width / 2, y: OFFSET});
                    line.brokenPoints.push({x: line.width / 2, y: line.height});
                    line.arrowEndPoint.direction = line.width > 0 ? DIRECTION.W : DIRECTION.E;
                } else {
                    line.brokenPoints.push({x: 0, y: OFFSET});
                    line.brokenPoints.push({x: line.width, y: OFFSET});
                    line.arrowEndPoint.direction = DIRECTION.S;
                }
            }
        };

        const toShapeConnectorExists = () => {
            line.arrowEndPoint.direction = DIRECTION.S;
            const shape = line.getToShape();
            const shapeConnector = line.toShapeConnector;
            const lineConnector = line.fromConnector;
            if (line.y + lineConnector.y > shape.y + shapeConnector.y) {
                if (Math.abs(line.height) > Math.abs(line.width)) {
                    line.brokenPoints.push({x: 0, y: line.height / 2});
                    line.brokenPoints.push({x: line.width, y: line.height / 2});
                    line.arrowBeginPoint.direction = DIRECTION.N;
                } else {
                    line.brokenPoints.push({x: line.width, y: 0});
                    line.arrowBeginPoint.direction = line.width > 0 ? DIRECTION.E : DIRECTION.W;
                }
            } else {
                if (Math.abs(line.width) > Math.abs(line.height)) {
                    line.brokenPoints.push({x: line.width / 2, y: 0});
                    line.brokenPoints.push({x: line.width / 2, y: line.height + OFFSET});
                    line.brokenPoints.push({x: line.width, y: line.height + OFFSET});
                    line.arrowBeginPoint.direction = line.width > 0 ? DIRECTION.E : DIRECTION.W;
                } else {
                    line.brokenPoints.push({x: 0, y: line.height + OFFSET});
                    line.brokenPoints.push({x: line.width, y: line.height + OFFSET});
                    line.arrowBeginPoint.direction = DIRECTION.S;
                }
            }
        };

        if (line.fromShapeConnector) {
            fromShapeConnectorExists();
        } else {
            toShapeConnectorExists();
        }
    };

    /**
     * 生成转折点.
     *
     * @param line 线对象.
     */
    self.generateBrokenPoints = (line) => {
        const keys = getKeys(line);
        const keyString = keys.get();

        // 初始化开始和结束点(也可算转折点.)
        line.arrowBeginPoint = {x: 0, y: 0};
        line.arrowEndPoint = {x: line.width, y: line.height};

        // 每次生成brokenPoints之前先进行清理.
        line.brokenPoints = [];

        // 生成转折点.
        getHandler(keyString)(line);

        // 处理箭头，若存在箭头，则给箭头预留下足够的绘制空间吗，否则会导致箭头方向绘制错乱.
        const fromDirection = line.arrowBeginPoint.direction;
        line.arrowBeginPoint[fromDirection.ax] += line.beginArrow ? line.beginArrowSize * fromDirection.vector : 0;
        const toDirection = line.arrowEndPoint.direction;
        line.arrowEndPoint[toDirection.ax] += line.endArrow ? line.endArrowSize * toDirection.vector : 0;
    };

    const getKeys = (line) => {
        const fromShapeConnector = line.getFromShapeConnector();
        const toShapeConnector = line.getToShapeConnector();

        const keys = {
            fromDirection: null,
            toDirection: null,
            get: () => {
                return (keys.fromDirection ? keys.fromDirection.value : "")
                    + (keys.toDirection ? keys.toDirection.value : "");
            }
        };

        // 两个连接点都存在
        // 只有一个连接点存在
        // 两个连接点都不存在
        if (fromShapeConnector && toShapeConnector) {
            keys.fromDirection = fromShapeConnector.direction;
            keys.toDirection = toShapeConnector.direction;
        } else if (fromShapeConnector || toShapeConnector) {
            if (fromShapeConnector) {
                keys.fromDirection = fromShapeConnector.direction;
            } else {
                keys.toDirection = toShapeConnector.direction;
            }
        } else {
            keys.fromDirection = getFromDirection(line);
            keys.toDirection = getToDirection(line);
        }
        return keys;
    };

    const getFromDirection = (line) => {
        if (Math.abs(line.width) >= Math.abs(line.height)) {
            return line.width > 0 ? DIRECTION.E : DIRECTION.W;
        } else {
            return line.height > 0 ? DIRECTION.S : DIRECTION.N;
        }
    };

    const getToDirection = (line) => {
        if (Math.abs(line.width) >= Math.abs(line.height)) {
            return line.width > 0 ? DIRECTION.W : DIRECTION.E;
        } else {
            return line.height > 0 ? DIRECTION.N : DIRECTION.S;
        }
    };

    const getHandler = (keys) => {
        let handler = self[keys];
        return handler ? handler : self["WE"];
    };

    return self;
};

/**
 * 计算起始点到上边界和下边界的最短距离.
 *
 * @param line 线对象.
 * @param fromShape 起始图形.
 * @param toShape 结束图形.
 * @returns {number}
 */
const calcUpDownDistance = (line, fromShape, toShape) => {
    const fromConnectorY = fromShape.y + line.getFromShapeConnector().y;
    const upDistance = fromConnectorY - Math.min(fromShape.y, toShape.y);
    const downDistance = Math.max(fromShape.y + fromShape.height, toShape.y + toShape.height) - fromConnectorY;
    if (upDistance < downDistance) {
        return -upDistance;
    } else {
        return downDistance;
    }
};

/**
 * 计算起始点到左右边界的最短距离.
 *
 * @param line 线对象.
 * @param fromShape 起始图形.
 * @param toShape 结束图形.
 * @returns {number}
 */
const calcLeftRightDistance = (line, fromShape, toShape) => {
    const fromConnectorX = fromShape.x + line.getFromShapeConnector().x;
    const leftDistance = fromConnectorX - Math.min(fromShape.x, toShape.x);
    const rightDistance = Math.max(fromShape.x + fromShape.width, toShape.x + toShape.width) - fromConnectorX;
    if (leftDistance < rightDistance) {
        return -leftDistance;
    } else {
        return rightDistance;
    }
};