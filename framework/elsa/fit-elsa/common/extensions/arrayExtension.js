/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

Array.prototype.distinct = function (f) {
    let result = [];
    this.forEach(i => {
        let value = f(i);
        if (!result.contains(r => f(r) === value)) {
            result.push(i);
        }
    });
    return result;
};

/**
 * 交换两个元素的位置.
 *
 * @param index1 位置1.
 * @param index2 位置2.
 */
Array.prototype.swap = function (index1, index2) {
    const temp = this[index1];
    this[index1] = this[index2];
    this[index2] = temp;
};

Array.prototype.clone = function () {
    return this.slice(0);
};

Array.prototype.max = function (f) {
    let self = this;
    let maxValue = f(self[0]);
    for (let i = 1; i < self.length; i++) {
        let value = f(self[i]);
        if (maxValue < value) {
            maxValue = value;
        }
    }
    return maxValue;
};
Array.prototype.maxBy = function (f) {
    if (this.length === 0) {
        return null;
    }
    let self = this;
    let maxItem = self[0];
    let maxValue = f(self[0]);
    for (let i = 1; i < self.length; i++) {
        let value = f(self[i]);
        if (maxValue < value) {
            maxValue = value;
            maxItem = self[i];
        }
    }
    return maxItem;
};
Array.prototype.min = function (f) {
    let self = this;
    let minValue = f(self[0]);
    for (let i = 1; i < self.length; i++) {
        let value = f(self[i]);
        if (minValue > value) {
            minValue = value;
        }
    }
    return minValue;
};
Array.prototype.minBy = function (f) {
    if (this.length === 0) {
        return null;
    }
    let self = this;
    let minItem = self[0];
    let minValue = f(self[0]);
    for (let i = 1; i < self.length; i++) {
        let value = f(self[i]);
        if (minValue > value) {
            minValue = value;
            minItem = self[i];
        }
    }
    return minItem;
};

Array.prototype.count = function (f) {
    let self = this;
    let count = 0;
    for (let i = 0; i < self.length; i++) {
        count += f(self[i]) ? 1 : 0;
    }
    return count;
};

Array.prototype.exist = function (f) {
    let self = this;
    for (let i = 0; i < self.length; i++) {
        if (f(self[i])) {
            return true;
        }
    }
    return false;
};

Array.prototype.sum = function (f) {
    let self = this;
    let sum = 0;
    for (let i = 0; i < self.length; i++) {
        sum += f(self[i]);
    }
    return sum;
};
Array.prototype.average = function (f) {
    let self = this;
    let sum = 0;
    for (let i = 0; i < self.length; i++) {
        sum += f(self[i]);
    }
    return sum / self.length;
};

Array.prototype.insert = function (item, index) {
    this.splice(index, 0, item);
};

Array.prototype.contains = function (f) {
    let self = this;
    for (let i = 0; i < self.length; i++) {
        if (f(self[i])) {
            return true;
        }
    }
    return false;
};

Array.prototype.remove = function (f) {
    let self = this;
    const removed = [];
    for (let i = 0; i < self.length; i++) {
        if (f(self[i])) {
            removed.push.apply(removed, (self.splice(i, 1)));
            i--;
        }
    }
    return removed;
};
Array.prototype.removeAt = function (i) {
    let self = this;
    self.splice(i, 1);
};

const getFirstParam = (field, a) => {
    if (field) {
        if (typeof field === 'string') {
            return a[field];
        } else {
            return field(a);
        }
    } else {
        return a;
    }
};

const getSecondParam = (field, b) => {
    if (field) {
        if (typeof field === 'string') {
            return b[field];
        } else {
            return field(b);
        }
    } else {
        return b;
    }
};

Array.prototype.orderBy = function (field) {
    let self = this.slice();

    function compare(a, b) {
        let valueA = getFirstParam(field, a);
        let valueB = getSecondParam(field, b);
        if (valueA < valueB) {
            return -1;
        } else if (valueA > valueB) {
            return 1;
        } else {
            return 0;
        }
    }

    self.sort(compare);
    return self;
};
Array.prototype.orderByDesc = function (field) {
    let self = this.slice();

    function compare(a, b) {
        let valueA = getFirstParam(field, a);
        let valueB = getSecondParam(field, b);
        if (valueA < valueB) {
            return 1;
        } else if (valueA > valueB) {
            return -1;
        } else {
            return 0;
        }
    }

    self.sort(compare);
    return self;
};
Array.prototype.equals = function (arr2) {
    let self = this;

    if (!Array.isArray(arr2)) {
        return false;
    }
    return self.length === arr2.length && self.every((i, v) => v === arr2[i]);
};