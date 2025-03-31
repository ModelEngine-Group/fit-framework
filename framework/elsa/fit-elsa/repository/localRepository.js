/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {uuid} from "../common/util.js";

/**
 * 本地repo，本地测试时使用.
 *
 * @return {{}}
 */
export const localRepository = () => {
    const repo = {};
    //mock
    const graphs = [];

    repo.saveGraph = graphData => {
        graphs.push(graphData);
        return graphData;
    };
    repo.getGraph = graphId => {
        return graphs.find(g => g.id === graphId);
    }

    repo.getSession = () => {
        return {name: "huizi", id: uuid()};
    }
    return repo;
};