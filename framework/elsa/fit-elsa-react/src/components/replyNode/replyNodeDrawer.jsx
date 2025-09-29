/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from "@/components/base/jadeNodeDrawer.jsx";
import NodeIcon from "../asserts/icon-end.svg?react"; // 导入背景图片

/**
 * 直接回复节点绘制器
 *
 * @override
 */
export const replyNodeDrawer = (shape, div, x, y) => {
    const self = jadeNodeDrawer(shape, div, x, y);
    self.type = "replyNodeDrawer";

    /**
     * @override
     */
    self.getHeaderIcon = () => {
        return (<>
            <NodeIcon className="jade-node-custom-header-icon"/>
        </>);
    };

    return self;
};