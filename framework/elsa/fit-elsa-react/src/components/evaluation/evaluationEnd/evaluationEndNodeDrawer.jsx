/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import EvaluationStartIcon from "@/components/asserts/icon-evaluation-end.svg?react";
import {endNodeDrawer} from "@/components/end/endNodeDrawer.jsx";

/**
 * 评估结束节点绘制器
 *
 * @override
 */
export const evaluationEndNodeDrawer = (shape, div, x, y) => {
    const self = endNodeDrawer(shape, div, x, y);
    self.type = "evaluationEndNodeDrawer";

    /**
     * @override
     */
    self.getHeaderIcon = () => {
        return (<>
            <EvaluationStartIcon className="jade-node-custom-header-icon"/>
        </>);
    };

    /**
     * 评估结束节点header只显示重命名选项
     *
     * @override
     */
    self.getToolMenus = () => {
        return [{
            key: '1', label: "重命名", action: (setEdit) => {
                setEdit(true);
            }
        }];
    };

    return self;
};