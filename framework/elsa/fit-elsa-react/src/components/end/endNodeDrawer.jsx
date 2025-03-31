/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from "@/components/base/jadeNodeDrawer.jsx";
import {EndNodeHeader} from "@/components/end/EndNodeHeader.jsx";
import EndIcon from "../asserts/icon-end.svg?react"; // 导入背景图片

/**
 * end节点绘制器
 *
 * @override
 */
export const endNodeDrawer = (shape, div, x, y) => {
    const self = jadeNodeDrawer(shape, div, x, y);
    self.type = "endNodeDrawer";

    /**
     * @override
     */
    self.getHeaderComponent = (data, shapeStatus) => {
        return (<EndNodeHeader shape={shape} data={data} shapeStatus={shapeStatus}/>);
    };

    /**
     * @override
     */
    self.getHeaderIcon = () => {
        return (<>
            <EndIcon className="jade-node-custom-header-icon"/>
        </>);
    };

    /**
     * @override
     */
    self.getHeaderTypeIcon = () => {
    };

    /**
     * @override
     */
    self.getToolMenus = () => {
        let toolMenus = [{
            key: 'copy', label: 'copy', action: () => {
                shape.duplicate();
            },
        }, {
            key: 'rename', label: 'rename', action: (setEdit) => {
                setEdit(true);
            },
        }];
        if (shape.page.sm.getShapes(s => s.type === shape.type).length > 1) {
            toolMenus.push({
                key: 'delete', label: 'delete', action: () => {
                    shape.remove();
                },
            });
        }
        return toolMenus;
    };

    return self;
};