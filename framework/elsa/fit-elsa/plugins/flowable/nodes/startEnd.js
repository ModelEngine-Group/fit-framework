/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {node} from './node.js';
import {endDrawer, startDrawer} from '../drawers/drawer.js';

/**
 * flowable开始节点
 * flowable的设计其实可以没有开始节点，每个节点都可以是数据输入节点
 * 辉子 2020
 */
const start = (id, x, y, width, height, parent, drawer) => {
    const WIDTH = 30;
    let self = node(id, x, y, WIDTH, WIDTH, parent, false, drawer === undefined ? startDrawer : drawer);
    self.type = "start";
    self.autoWidth = false;
    self.width = self.height = WIDTH;
    self.margin = 10;
    // 这边设置了region显示
    self.regions.remove(r => r.type !== "lock");
    self.text = "开始";
    self.hideText = true;
    self.editable = false;
    self.allowFromLink = true;
    self.allowToLink = false;
    self.borderWidth = 0;

    return self;
};
/**
 * 结束节点
 * 数据到此不再处理
 * huiz 2020
 */
const end = (id, x, y, width, height, parent) => {
    let self = start(id, x, y, 0, 0, parent, endDrawer);
    self.type = "end";
    self.text = "结束";
    self.allowFromLink = false;
    self.allowToLink = true;

    return self;
};

export {start, end};