/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {defaultGraph} from "../../core/defaultGraph.js";
import {sleep} from "../../common/util.js";

/**
 * aippFlow的专用画布.
 *
 * @param div dom元素.
 * @param title 名称.
 */
export const aippFlowGraph = (div, title) => {
    const self = defaultGraph(div, title);
    self.type = "aippFlowGraph";
    self.pageType = "aippFlowPage";

    /**
     * 导入flow相关依赖.
     *
     * @override
     */
    const initialize = self.initialize;
    self.initialize = async () => {
        let finished = false;
        self.staticImport(() => import(/* webpackMode: "eager" */ "./aippFlowPage.js"))
            .staticImport(() => import(/* webpackMode: "eager" */ "./aippNodes/aippEvent.js"))
            .staticImport(() => import(/* webpackMode: "eager" */ "./aippNodes/aippState.js"))
            .staticImport(() => import(/* webpackMode: "eager" */ "./aippNodes/aippStartEnd.js"))
            .then(() => {
                finished = true;
            });

        while (!finished) {
            await sleep(5);
        }
        return initialize.apply(self);
    };
    return self;
};