/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from "@/components/base/jadeNodeDrawer.jsx";
import NodeIcon from "../asserts/icon-text-concatenation.svg?react"; // 导入背景图片

/**
 * 文本拼接节点绘制器
 *
 * @override
 */
export const textConcatenateNodeDrawer = (shape, div, x, y) => {
    const self = jadeNodeDrawer(shape, div, x, y);
    self.type = "textConcatenateNodeDrawer";

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