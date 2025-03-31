/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

// 多边形的射线检测，参考：https://blog.csdn.net/WilliamSun0122/article/details/77994526
const tolerance = 1e-6;
// 三态函数，判断两个double在eps精度下的大小关系
function dcmp(x) {
    if (Math.abs(x) < tolerance) {
        return 0;
    }

    return x < 0 ? -1 : 1;
}

// 判断点Q是否在p1和p2的线段上
function onSegment(p1, p2, q) {
    const isSlopeEqual = (q[0] - p1[0]) * (p2[1] - p1[1]) === (p2[0] - p1[0]) * (q[1] - p1[1]);
    const isQxIn = Math.min(p1[0], p2[0]) <= q[0] && q[0] <= Math.max(p1[0], p2[0]);
    const isQyIn = Math.min(p1[1], p2[1]) <= q[1] && q[1] <= Math.max(p1[1], p2[1]);
    return isSlopeEqual && isQxIn && isQyIn;
}

// 判断点P在多边形内-射线法
export function inPolygon(points, x, y) {
    let isHit = false;
    const n = points.length;
    if (n <= 2) {
        // svg 中点小于 3 个时，不显示，也无法被拾取
        return false;
    }
    for (let i = 0; i < n; i++) {
        const p1 = points[i];
        const p2 = points[(i + 1) % n];
        if (onSegment(p1, p2, [x, y])) {
            // 点在多边形一条边上
            return true;
        }
        // 前一个判断min(p1[1],p2[1])<P.y<=max(p1[1],p2[1])
        // 后一个判断被测点 在 射线与边交点 的左边
        if (
            dcmp(p1[1] - y) > 0 !== dcmp(p2[1] - y) > 0 &&
            dcmp(x - ((y - p1[1]) * (p1[0] - p2[0])) / (p1[1] - p2[1]) - p1[0]) < 0
        ) {
            isHit = !isHit;
        }
    }
    return isHit;
}
export function inPolygons(polygons, x, y) {
    let isHit = false;
    for (let i = 0; i < polygons.length; i++) {
        const points = polygons[i];
        isHit = inPolygon(points, x, y);
        if (isHit) {
            break;
        }
    }
    return isHit;
}