/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

export const uuid = function (isLong) {
    if (isLong) {
        const id = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            let r = Math.random() * 16 | 0;
            let v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
        return id.replaceAll("-", "");
    } else {
        let firstPart = (Math.random() * 46656) | 0;
        let secondPart = (Math.random() * 46656) | 0;
        firstPart = ("000" + firstPart.toString(36)).slice(-3);
        secondPart = ("000" + secondPart.toString(36)).slice(-3);
        return firstPart + secondPart;
    }
};

/**
 * 求点(x, y)绕圆心(cx, cy)旋转degree度之后的点的坐标(x1, y1).
 * 公式为:
 * x1 = (x - cx)cos(degree) - (y - cy)sin(degree) + cx.
 * y1 = (y - cy)cos(degree) + (x - cx)sin(degree) + cy.
 */
export const getRotatedCoordinate = (x, y, cx, cy, degree) => {
    let dx = x - cx;
    let dy = y - cy;
    return {
        x: dx * Math.cos(degree) - dy * Math.sin(degree) + cx, y: dx * Math.sin(degree) + dy * Math.cos(degree) + cy
    };
};

export const convertPositionWithParents = (parent, x, y) => {
    let parents = [];
    let rotateDegree = 0;
  let parentVal = parent;
  while (parentVal !== parentVal.page) {
    rotateDegree += parentVal.rotateDegree;
    parents.push(parentVal);
    parentVal = parentVal.getContainer();
    }
    if (rotateDegree === 0) {
        return {x, y};
    }

    let convertPositionWithParent = (parents, x, y) => {
      let xVal = x;
      let yVal = y;
        if (parents.length === 0) {
          return {x: xVal, y: yVal};
        }
        let parent = parents[parents.length - 1];
        if (parent.scaleX !== undefined) {
            xVal = parent.x + ((xVal - parent.x) / parent.scaleX);
        }
        if (parent.scaleY !== undefined) {
            yVal = parent.y + ((yVal - parent.y) / parent.scaleY);
        }
        let degree = parent.rotateDegree * Math.PI / 180;
        let cx = parent.x + parent.width / 2;
        let cy = parent.y + parent.height / 2;
      let p = getRotatedCoordinate(xVal, yVal, cx, cy, -degree);
        parents.pop();
        return convertPositionWithParent(parents, p.x, p.y);
    };
    return convertPositionWithParent(parents, x, y);
};

export const getInteractRect = (rect1, rect2) => {
    let leftX = Math.max(rect1.x, rect2.x);
    let rightX = Math.min(rect1.x + rect1.width, rect2.x + rect2.width);
    let topY = Math.max(rect1.y, rect2.y);
    let bottomY = Math.min(rect1.y + rect1.height, rect2.y + rect2.height);

    if (leftX < rightX && topY < bottomY) {
        return {x: leftX, y: topY, width: rightX - leftX, height: bottomY - topY};
    } else {
        return null;
    }
}

export const getPixelRatio = (context) => {
  let contextVal = context;
  if (!contextVal) {
    contextVal = document.createElement('canvas').getContext('2d');
    }
    let devicePixelRatio = window.devicePixelRatio || 1;
  let backingStoreRatio = contextVal.webkitBackingStorePixelRatio ||
    contextVal.mozBackingStorePixelRatio ||
    contextVal.msBackingStorePixelRatio ||
    contextVal.oBackingStorePixelRatio ||
    contextVal.backingStorePixelRatio ||
        1;
    return devicePixelRatio / backingStoreRatio;
}

export const pixelRateAdapter = function (context, pageScaleX, pageScaleY, ignorePageScale = false, ignoreHighQuality = false) {
    let canvas = context.canvas;
    const pixelRatio = getPixelRatio(context);

    let ratioX = ignoreHighQuality ? 1 : pixelRatio;
    let ratioY = ignoreHighQuality ? 1 : pixelRatio;
    if (!ignorePageScale) {
        ratioX *= (pageScaleX !== undefined ? pageScaleX : 1);
        ratioY *= (pageScaleY !== undefined ? pageScaleY : 1);
    }

    let oldWidth = canvas.width;
    let oldHeight = canvas.height;

    canvas.width = oldWidth * ratioX;
    canvas.height = oldHeight * ratioY;

    canvas.style.width = oldWidth + "px";
    canvas.style.height = oldHeight + "px";
    context.scale(ratioX, ratioY);
    return {ratioX, ratioY}
};

export const isPointInRect = function (point, rect) {
    try {
        return (point.x >= rect.x && point.x <= (rect.x + rect.width) && point.y >= rect.y && point.y <= (rect.y + rect.height));
    } catch (e) {
        return false;
    }
};

export const isRectInRect = function (rect1, rect2) {
    let point1 = {x: rect1.x, y: rect1.y};
    let point2 = {x: rect1.x, y: rect1.y + rect1.height};
    let point3 = {x: rect1.x + rect1.width, y: rect1.y};
    let point4 = {x: rect1.x + rect1.width, y: rect1.y + rect1.height};
    return isPointInRect(point1, rect2) && isPointInRect(point2, rect2) && isPointInRect(point3, rect2) && isPointInRect(point4, rect2);
};

export const getDistance = function (x1, y1, x2, y2) {
    return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
};

export const closestPoint = function (points1, points2) {
    if (points1.length === 0 || points2.length === 0) {
      return undefined;
    }

    let closest = function (points, point) {
        let i;
        let closest = points[0];
        let distance = getDistance(point.x, point.y, points[0].x, points[0].y);
        for (i = 1; i < points.length; i++) {
            let nextDistance = getDistance(point.x, point.y, points[i].x, points[i].y);
            if (distance > nextDistance) {
                closest = points[i];
                distance = nextDistance;
            }
        }
        return {
            from: closest, to: point, distance: distance
        };
    };
    let cpoint = function (ps) {
        let i;
        let closest = ps[0];
        for (i = 1; i < ps.length; i++) {
            if (closest.distance > ps[i].distance) {
                closest = ps[i];
            }
        }
        return closest;
    };
    let i;
    let closestPoints = [];
    for (i = 0; i < points2.length; i++) {
        closestPoints.push(closest(points1, points2[i]));
    }
    return cpoint(closestPoints);
};

// 画布相对浏览器的原始偏移（与缩放比无关）
export const offsetPosition = function (page) {
    // 全屏状态
    // 全屏时，其父div铺满整个屏幕，因此坐标可直接返回(0, 0).
    if (page.isFullScreen) {
        return {
            x: 0, y: 0
        }
    }

    let top = page.getOffsetTop();
    let left = page.getOffsetLeft();
    let node = page.div.offsetParent;
    while (node !== null) {
        top += node.offsetTop || 0;
        left += node.offsetLeft || 0;
        node = node.offsetParent;
    }

    let parent = page.div.parentNode;
    let scrollTop = 0;
    let scrollLeft = 0;
    while (parent) {
        if (parent.pageYOffset || parent.scrollTop) {
            scrollTop += parent.pageYOffset || parent.scrollTop;
        }
        if (parent.pageXOffset || parent.scrollLeft) {
            scrollLeft += parent.pageXOffset || parent.scrollLeft;
        }
        parent = parent.parentNode;
    }

    top -= scrollTop;
    left -= scrollLeft;
    return {
        x: left, y: top
    }
};

export const position = function (canvas) {
    let offset = function () {
        let top = 0;
        let left = 0;
        let element = canvas;
        do {
            top += element.offsetTop || 0;
            left += element.offsetLeft || 0;
            element = element.offsetParent;
        } while (element);

        let node = canvas.parentNode;
        let scrollTop = 0;
        let scrollLeft = 0;
        do {
            scrollTop += node.scrollTop;
            scrollLeft += node.scrollLeft;
            node = node.parentNode;
        } while (node.tagName.toLowerCase() !== "html");
        left -= scrollLeft;
        top -= scrollTop;
        return {
            x: left, y: top
        }
    };
    return {
        offset: offset, x: 0, y: 0, zoom: 1
    }
};

export const isRectInteractRect = function (rect1, rect2) {
    let r1 = {left: rect1.x, right: rect1.x + rect1.width, top: rect1.y, bottom: rect1.y + rect1.height};
    let r2 = {left: rect2.x, right: rect2.x + rect2.width, top: rect2.y, bottom: rect2.y + rect2.height};
    return !(r2.left > r1.right || r2.right < r1.left || r2.top > r1.bottom || r2.bottom < r1.top);
};

export const getGridGrabPosition = function (point, graphPage) {
    if (!graphPage.enableGrid) {
        return point;
    }
    let p = {x: 0, y: 0};
    let rate = graphPage.gridSpace / graphPage.scale;
    p.x = Math.round(point.x / rate) * rate;
    p.y = Math.round(point.y / rate) * rate;
    return p;
};

export const sleep = async sleepDuration => {
    await new Promise(r => setTimeout(r, sleepDuration));
};

export const requestAnimationFrame = window.requestAnimationFrame ||
    window.mozRequestAnimationFrame ||
    window.webkitRequestAnimationFrame ||
    window.msRequestAnimationFrame;

export const isNumeric = (str) => {
    return !isNaN(str) && !isNaN(parseFloat(str))
}

export const getUrlParam = key => {
    return new URL(window.location.href).searchParams.get(key);
}

export const compareAndSet = (obj, key, value, callback) => {
    if (obj[key] === value) {
        return;
    }
    if (callback) {
        callback();
    } else {
        obj[key] = value;
    }
};

export const showDialog = () => {
};

/**
 * 深拷贝一个对象.
 *
 * @param obj 对象.
 * @return {null|any} 返回null或拷贝后的对象.
 */
export const deepClone = (obj) => {
    if (obj === null || obj === undefined || typeof obj !== "object") {
        return null;
    }
    const jsonString = JSON.stringify(obj);
    return JSON.parse(jsonString);
};

// 根据指定条件，求二维数组对象的交集
export const getIntersection = (arrays, condition) => {
    if (arrays.length === 0) {
        return [];
    }
    return arrays.reduce((accumulator, currentArray) => {
        if (currentArray.length === 0) {
            return accumulator;
        }
        return accumulator.filter((item) =>
            currentArray.some(
                (otherItem) => condition && condition(otherItem, item)
            )
        );
    });
};

// 将数组对象按照指定属性分组，并返回二维数组
export const groupBy = (arr, property) => {
    return arr.reduce((accumulator, currentValue) => {
        const key = currentValue[property];
        if (!accumulator[key]) {
            accumulator[key] = [];
        }
        accumulator[key].push(currentValue);
        return accumulator;
    }, {});
};

/**
 * 事件去抖，防止在一次事件循环中，某些方法被执行了多次..
 *
 * @return {{}} 防抖对象.
 */
export const eventDebounce = () => {
    const self = {};
    self.flush = false;

    /**
     * 减少func执行次数.
     *
     * @param event 事件.
     */
    self.debounce = (event) => {
        if (self.flush) {
            return;
        }

        self.flush = true;
        Promise.resolve().then(() => {
            try {
                event();
            } finally {
                self.flush = false;
            }
        });
    };

    return self;
};

/**
 * 获取div元素的可编辑状态
 *
 * @param element div元素
 * @return {string} true/false的字符串
 */
export const getEditStatus = element => {
    // 辅助函数：递归查找 contentEditable 状态
    const _findContentEditableState = el => {
        if (!el || el.nodeType !== Node.ELEMENT_NODE) {
            // 如果到达文档根节点或非元素节点，返回 false
            return 'false';
        }
        const editableStatus = el.contentEditable;
        if (editableStatus === 'true' || editableStatus === 'false') {
            return editableStatus;
        } else { // editable为inherit的场景
            // 继续向上查找父元素
            return _findContentEditableState(el.parentElement);
        }
    };

    // 调用辅助函数，从当前元素开始查找
    return _findContentEditableState(element);
};