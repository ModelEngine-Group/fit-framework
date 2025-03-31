/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {node} from './node.js';

/**
 * flowable通用数据处理节点
 * 辉子 2020
 */
const state = (id, x, y, width, height, parent, drawer) => {
    let self = node(id, x, y, width, height, parent, false, drawer);
    self.type = "state";
    self.text = "state";
    // 根据这个属性设置是否自动调整宽度
    self.autoWidth = true;
    self.autoHeight = true;
    self.width = self.minWidth = 40;
    self.height = 22;
    self.maxWidth = 300;

    /**
     * 当容器是parallel时，不进行autoWidth.
     *
     * @override
     */
    const invalidate = self.invalidate;
    self.invalidate = () => {
        self.autoWidth = self.getContainer().type !== "parallel";
        invalidate.apply(self);
    };

    return self;
};

export {state};