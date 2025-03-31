/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import EvaluationStartIcon from "@/components/asserts/icon-evaluation-start.svg?react";
import {startNodeDrawer} from "@/components/start/startNodeDrawer.jsx";

/**
 * 评估开始节点绘制器
 *
 * @override
 */
export const evaluationStartNodeDrawer = (shape, div, x, y) => {
    const self = startNodeDrawer(shape, div, x, y);
    self.type = "evaluationStartNodeDrawer";

    /**
     * @override
     */
    self.getHeaderIcon = () => {
        return (<>
            <EvaluationStartIcon className="jade-node-custom-header-icon"/>
        </>);
    };

    return self;
};