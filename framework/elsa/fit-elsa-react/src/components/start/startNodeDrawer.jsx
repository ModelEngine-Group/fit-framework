/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from "@/components/base/jadeNodeDrawer.jsx";
import StartIcon from '../asserts/icon-start.svg?react';
import {StartNodeHeader} from '@/components/start/startNodeHeader.jsx'; // 导入背景图片

/**
 * 开始节点绘制器
 *
 * @override
 */
export const startNodeDrawer = (shape, div, x, y) => {
    const self = jadeNodeDrawer(shape, div, x, y);
    self.type = "startNodeDrawer";

    /**
     * @override
     */
    self.getHeaderComponent = (data, shapeStatus) => {
        return (<StartNodeHeader shape={shape} data={data} shapeStatus={shapeStatus}/>);
    };

    /**
     * @override
     */
    self.getHeaderIcon = () => {
        return (<>
            <StartIcon className="jade-node-custom-header-icon"/>
        </>);
    };

    /**
     * @override
     */
    self.getHeaderTypeIcon = () => {
    };

    /**
     * 开始节点header只显示重命名选项
     *
     * @override
     */
    self.getToolMenus = () => {
        let toolMenus = [{
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