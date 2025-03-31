/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {pageCompatibilityProcessor} from '@/flow/compatibility/compatibilityProcessors.js';
import {evaluationPageInitializationProcessor} from '@/flow/initialization/evaluationInitializationProcessor.js';

/**
 * 兼容管理器.
 *
 * @return {{}} 管理器对象.
 */
export const compatibilityManager = () => {
    const self = {};
    self.type = 'flow';

    /**
     * 兼容逻辑.
     *
     * @param pageData 页面对象.
     * @param graph 画布对象.
     */
    self.compatibleWith = (pageData, graph) => {
        if (!pageData) {
            return;
        }
        pageCompatibilityProcessor(pageData, graph).process();
    };

    /**
     * 数据初始化逻辑.
     *
     * @param pageData 页面对象.
     * @param graph 画布对象.
     */
    self.initializeData = (pageData, graph) => {
        if (!pageData) {
            return;
        }
        evaluationPageInitializationProcessor(pageData, graph).process();
    };

    return self;
};