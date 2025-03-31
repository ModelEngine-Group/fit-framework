/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ajax} from "../common/ajax.js";

/**
 * 用于访问线上存储的数据.
 *
 * @param envConfig 环境配置.
 * @return {{}} repository对象.
 */
export const onlineRepository = (envConfig) => {
    const self = {};

    /**
     * 获取 {@code graph}v的数据.
     *
     * @param graphId graph的唯一标识.
     * @return {undefined|{}} graph的数据.
     */
    self.getGraph = async (graphId) => {
        const result = await ajax.get(envConfig.getGraphUrl + "?graphId=" + graphId);
        if (result.code < 0) {
            throw new Error("Get graph failed:[" + result.msg + "].");
        }
        if (result.code === 404) {
            return null;
        }
        return JSON.parse(result.data);
    };

    /**
     * 保存graph数据.
     *
     * @param graphData graph的数据.
     * @return {Promise<void>}
     */
    self.saveGraph = async (graphData) => {
        const data = JSON.stringify(graphData);
        const result = await ajax.post(envConfig.saveGraphUrl, data);
        if (result.code < 0 || !result.data) {
            throw new Error("Save graph failed:[" + result.msg + "]");
        }
        return result;
    };

    /**
     * 获取当前登陆用户.
     *
     * @return {Promise<null|any>} 登陆用户.
     */
    self.getSession = async () => {
        const result = await ajax.get(envConfig.getUserUrl);
        if (result.code < 0) {
            throw new Error("Get user failed:[" + result.msg + "].");
        }
        if (result.code === 404) {
            return null;
        }
        return {
            name: result.data.userName, id: result.data.userId
        }
    }

    return self;
}