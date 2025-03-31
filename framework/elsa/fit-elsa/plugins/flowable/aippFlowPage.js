/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {page} from "../../core/page.js";

/**
 * aippFlow的page.
 *
 * @param div dom元素.
 * @param graph 画布.
 * @param name 名称.
 * @param id 唯一标识.
 * @returns {(WorkerGlobalScope & Window) | Window}
 */
export const aippFlowPage = (div, graph, name, id) => {
    const self = page(div, graph, name, id);
    self.type = "aippFlowPage";
    self.namespace = "aippFlow";
    self.getMenuScript = () => [];

    return self;
};