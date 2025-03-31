/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 本地调试只需要协同的url即可.
 *
 * @type {{collaborationUrl: string}}
 */
const LOCAL_CONFIG = {
    collaborationUrl: "http://localhost:8080/collaboration",
    levitationDefault: "",
    levitationActive: ""
}

/**
 * 用于管理elsa相关的所有外部配置.
 *
 * @type {{}} 配置对象.
 */
const ENV_CONFIG = (() => {
    const self = {};

    let configs = null;
    let isLocal = false;
    try {
        configs = __APP_CONFIG__;
    } catch (e) {
        isLocal = true;
        configs = LOCAL_CONFIG;
    }

    /**
     * 判断是否是本地调用(本地调用指的是不通过webpack打包).
     *
     * @return {boolean} true/false.
     */
    self.isLocal = () => {
        return isLocal;
    }

    // 将configs的所有属性全部赋值给envConfig对象.
    Object.keys(configs).forEach(k => self[k] = configs[k]);

    return self;
})();

export {ENV_CONFIG}