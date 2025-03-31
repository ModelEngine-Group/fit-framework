/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ENV_CONFIG} from "../config/envConfig.js";
import {localRepository} from "./localRepository.js";
import {onlineRepository} from "./onlineRepository.js";

/**
 * 组合repo.用于统一repo层，提供统一接口.
 *
 * @return {{}}
 */
export const compositeRepository = () => {
    const self = {};

    const repo = ENV_CONFIG.isLocal() ? localRepository() : onlineRepository(ENV_CONFIG);

    /**
     * 保存graph.
     *
     * @param graphData graph数据.
     * @return {Promise<*>}
     */
    self.saveGraph = async graphData => {
        return repo.saveGraph(graphData);
    };

    /**
     * 获取graph.
     *
     * @param graphId graph的唯一标识.
     * @return {Promise<*>}
     */
    self.getGraph = async graphId => {
        return repo.getGraph(graphId);
    }

    /**
     * 获取session.
     *
     * @return {Promise<*>}
     */
    self.getSession = async () => {
        return repo.getSession();
    }

    return self;
}