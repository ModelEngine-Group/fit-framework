/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 智能参考线
 * maliya 2022
 */
const guideLineUtil = (page) => {
    const limitX = 5 * page.scaleX;
    const limitY = 5 * page.scaleY;
    const stepX = 14 * page.scaleX;
    const stepY = 14 * page.scaleY;
    const color = "#BD6C56";
    let lines = [];
    let targetShapes = [];
    let compareShapes = [];
    let target = {};
    let compares = [];
    let resultsX = {};
    let resultsY = {};
    let self = {};

    self.init = () => {
        targetShapes = page.getFocusedShapes();
        compareShapes = page.sm.getShapes(s => {
            return !targetShapes.find(item => item.id === s.id);
        });
        compareShapes.push(page);
    };
    self.showGuideLines = (position) => {
        self._buildLines(position);
        self._renderLines();
    };
    self.clear = () => {
        lines.map(l => {
            const dom = document.getElementById(l.id);
            dom && dom.remove();
        });
        lines = [];
        resultsX = {};
        resultsY = {};
    };
    self._buildLines = (position) => {
        target = self._calcFocusedShapesFrame(targetShapes);
        compares = self._calcShapesPosition(compareShapes);
        if (!target) {
            return;
        }
        compares.map(item => {
            self._compare(target, item, position);
        });
        Object.keys(resultsX).length > 0 && self._adsorb(target, position, "x", resultsX);
        Object.keys(resultsY).length > 0 && self._adsorb(target, position, "y", resultsY);
    };
    self._renderLines = () => {
        lines.map(l => {
            let width = l.type === "vLine" ? 1 : l.length + stepX * 2;
            let height = l.type === "hLine" ? 1 : l.length + stepY * 2;
            let left = l.type === "vLine" ? l.value - 1 : l.origin - stepX;
            let top = l.type === "hLine" ? l.value - 1 : l.origin - stepY;
            let spanDom = document.createElement("span");
            spanDom.setAttribute("id", l.id);
            spanDom.setAttribute("style", `position: absolute; z-index: 9999 ; left: ${left}px; top: ${top}px; width: ${width}px; height: ${height}px;`);
            if (l.type === "hLine") {
                spanDom.style.backgroundImage = `linear-gradient(to right, ${color} 0%, ${color} 50%, transparent 50%)`;
                spanDom.style.backgroundRepeat = "repeat-x";
                spanDom.style.backgroundSize = "8px 2px";
            }
            if (l.type === "vLine") {
                spanDom.style.backgroundImage = `linear-gradient(to bottom, ${color} 0%, ${color} 50%, transparent 50%)`;
                spanDom.style.backgroundRepeat = "repeat-y";
                spanDom.style.backgroundSize = "2px 8px";
            }
            page.drawer.container.appendChild(spanDom);
        });
    };
    self._calcFocusedShapesFrame = (shapes) => {
        const targetPositions = self._calcShapesPosition(shapes);
        // 当只有一个选中图形时，直接返回
        if (shapes && shapes.length < 2) {
            return targetPositions[0];
        }
        // 当有多个选中图形是，取多个图形整体的边界
        const length = targetPositions.length;
        const minX = targetPositions.orderBy("l")[0]["l"];
        const minY = targetPositions.orderBy("t")[0]["t"];
        const maxX = targetPositions.orderBy("r")[length - 1]["r"];
        const maxY = targetPositions.orderBy("b")[length - 1]["b"];
        return {
            shape: null,
            x: minX,
            y: minY,
            w: maxX - minX,
            h: maxY - minY,
            l: minX,
            r: maxX,
            t: minY,
            b: maxY,
            lr: minX + (maxX - minX) / 2,
            tb: minY + (maxY - minY) / 2
        }
    };
    self._calcShapesPosition = (shapes) => {
        return shapes.map((s) => {
            const pos = s.getShapeFrame(false);
            const x = pos.x1;
            const y = pos.y1;
            const w = pos.x2 - pos.x1;
            const h = pos.y2 - pos.y1;

            return {
                shape: s, x, y, w, h, l: x, r: x + w, t: y, b: y + h, lr: x + w / 2, tb: y + h / 2
            };
        });
    };
    self._compare = (current, compare, position) => {
        const directions = {
            x: ['ll', 'rr', 'lr'], y: ['tt', 'bb', 'tb']
        };

        directions.x.map(dire => {
            // 当参照图形是page时，不对比page的外边框
            if (compare.shape.type === 'page' && ["ll", "rr"].includes(dire)) {
                return;
            }
            // 当选中多个图形时，不对比中心线
            if (targetShapes.length > 1 && dire === "lr") {
                return;
            }
            const {near, isNearDirection, dist, value, origin, length} = self._calcDistantResult(current, compare, dire, "x", position);

            // 当前操作图形接近参照图形，且已进入吸附范围
            // 当前操作图形离开参照图形，但仍在吸附范围，且离开步长小于1.1(1.1是试出来的 没有科学依据)
            const isNotFar = near && !isNearDirection && Math.abs(position.deltaX) < 0.8;
            if ((near && isNearDirection) || isNotFar) {
                self._checkArrayWithPush(resultsX, dist, {
                    "id": `vLine_${Math.random()}`, "type": "vLine", value, origin, length
                });
            }
        });

        directions.y.map(dire => {
            // 当参照图形是page时，不对比page的外边框
            if (compare.shape.type === 'page' && ["tt", "bb"].includes(dire)) {
                return;
            }
            // 当选中多个图形时，不对比中心线
            if (targetShapes.length > 1 && dire === "tb") {
                return;
            }
            const {near, isNearDirection, dist, value, origin, length} = self._calcDistantResult(current, compare, dire, "y", position);
            const isNotFar = near && !isNearDirection && Math.abs(position.deltaY) < 0.8;
            if ((near && isNearDirection) || isNotFar) {
                self._checkArrayWithPush(resultsY, dist, {
                    "id": `hLine_${Math.random()}`, "type": "hLine", value, origin, length
                });
            }
        });

    };
    self._adsorb = (current, position, key, results) => {
        const resultsArray = Object.entries(results);
        if (resultsArray.length) {
            const [minDistance, activeLines] = resultsArray.sort(([dist1], [dist2]) => Math.abs(dist1) - Math.abs(dist2))[0];
            const dist = parseInt(minDistance)
            if (targetShapes && targetShapes.length > 1) {
                // 当选中多个图形时，选中的图形都做dist距离的偏移变化
                targetShapes.map(item => {
                    item[key] -= dist;
                    item.invalidateAlone();
                })
            } else {
                current.shape[key] -= dist;
                current.shape.invalidateAlone();
            }
            target = self._calcFocusedShapesFrame(targetShapes);

            // 吸附后增加对齐参考线
            lines = lines.concat(activeLines);
        }
    }
    self._calcDistantResult = (current, compare, dire, key, position) => {
        const {x, y} = current;
        const W = current.w;
        const H = current.h;
        const {l, r, t, b, lr, tb} = compare;
        const {origin, length} = self._calcLineValues({x, y}, current, compare, key);

        const result = {
            // 是否进入吸附范围
            near: false, // 标识是接近、还是远离参照图形
            isNearDirection: false, // 举例差
            dist: Number.MAX_SAFE_INTEGER, // 辅助线坐标，吸附方向坐标，如x方向，则为横坐标
            value: 0, // 辅助线坐标，吸附方向的垂直方向坐标，如x方向，则为纵坐标
            origin, // 辅助线长度
            length
        }

        switch (dire) {
            case 'lr':
                result.dist = x + W / 2 - lr;
                result.value = lr;
                result.adsorptivePos = lr - W / 2;
                result.isNearDirection = (position.deltaX + x + W / 2 - result.value) * position.deltaX < 0;
                break;
            case 'll':
                if (Math.abs(x - l) > Math.abs(x - r)) {
                    result.dist = x - r;
                    result.value = r;
                    result.adsorptivePos = r;
                } else {
                    result.dist = x - l;
                    result.value = l;
                    result.adsorptivePos = l;
                }
                result.isNearDirection = (position.deltaX + x - result.value) * position.deltaX < 0;
                break;
            case 'rr':
                if (Math.abs(x + W - l) > Math.abs(x + W - r)) {
                    result.dist = x + W - r;
                    result.value = r;
                    result.adsorptivePos = r - W;
                } else {
                    result.dist = x + W - l;
                    result.value = l;
                    result.adsorptivePos = l - W;
                }
                result.isNearDirection = (position.deltaX + x + W - result.value) * position.deltaX < 0;
                break;
            case 'tt':
                if (Math.abs(y - t) > Math.abs(y - b)) {
                    result.dist = y - b;
                    result.value = b;
                    result.adsorptivePos = b;
                } else {
                    result.dist = y - t;
                    result.value = t;
                    result.adsorptivePos = t;
                }
                result.isNearDirection = (position.deltaY + y - result.value) * position.deltaY < 0;
                break;
            case 'bb':
                if (Math.abs(y + H - t) > Math.abs(y + H - b)) {
                    result.dist = y + H - b;
                    result.value = b;
                    result.adsorptivePos = b - H;
                } else {
                    result.dist = y + H - t;
                    result.value = t;
                    result.adsorptivePos = t - H;
                }
                result.isNearDirection = (position.deltaY + y + H - result.value) * position.deltaY < 0;
                break;
            case 'tb':
                result.dist = y + H / 2 - tb;
                result.value = tb;
                result.adsorptivePos = tb - H / 2;
                result.isNearDirection = (position.deltaY + y + H / 2 - result.value) * position.deltaY < 0;
                break;
            default:
                break;
        }

        if (key === "x" && Math.abs(result.dist) < limitX + 1) {
            result.near = true;
        }
        if (key === "y" && Math.abs(result.dist) < limitY + 1) {
            result.near = true;
        }

        return result;
    };
    self._calcLineValues = (values, current, compare, key) => {
        const {x, y} = values;
        const {h: H, w: W} = current;
        const {l, r, t, b} = compare;
        const T = y;
        const B = y + H;
        const L = x;
        const R = x + W;

        const direValues = {
            x: [t, b, T, B], y: [l, r, L, R]
        };

        const length = self._getMax(direValues[key]);
        const origin = Math.min(...direValues[key]);
        return {length, origin};
    };
    self._checkArrayWithPush = (arr, key, value) => {
        if (!arr) {
            return;
        }
        if (Array.isArray(arr[key])) {
            arr[key].push(value)
        } else {
            arr[key] = [value]
        }
    };
    self._getMax = (arr) => {
        const num = arr.sort((a, b) => a - b);
        return num[num.length - 1] - num[0];
    };

    return self;
};

export {guideLineUtil};