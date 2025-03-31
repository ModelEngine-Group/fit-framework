/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const chart = (id, x, y, width, height, parent, drawer) => {
    const self = rectangle(id, x, y, width, height, parent, drawer);
    self.grid = undefined;// grid id
    self.range = undefined;// {1,2,3,4}第一行，第二列，3行，4列
    self.cells = [];

    self.invalidate = ()=>{};

    return self;
};

const columnChart = (id, x, y, width, height, parent) => {
    const self = chart(id, x, y, width, height, parent, columnChartDrawer);
    return self;
};

export {columnChart};
