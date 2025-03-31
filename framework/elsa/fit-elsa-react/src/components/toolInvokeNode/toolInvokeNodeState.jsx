/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import httpUtil from '@/components/util/httpUtil.jsx';
import {formatString} from '@/components/util/StringUtil.js';
import {toolInvokeNodeDrawer} from '@/components/toolInvokeNode/toolInvokeNodeDrawer.jsx';
import {baseToolNode} from '@/components/base/baseToolNode.jsx';

/**
 * 工具调用节点shape
 *
 * @override
 */
export const toolInvokeNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = baseToolNode(id, x, y, width, height, parent, drawer ? drawer : toolInvokeNodeDrawer);
    self.type = "toolInvokeNodeState";
    self.width = 360;
    self.componentName = "toolInvokeComponent";
    self.flowMeta.jober.type = 'STORE_JOBER';

    /**
     * 拉取versionInfo数据.
     *
     * @param callback 回调.
     */
    self.fetchVersionInfo = (callback) => {
        const url = self.graph.getConfig(self)?.urls?.versionInfo;
        if (!url) {
            return;
        }
        const uniqueName = self.flowMeta.jober.entity.uniqueName;
        const replacedUrl = formatString(url, {tenant: self.graph.tenant, uniqueName});
        httpUtil.get(replacedUrl, new Map(), (result) => {
            callback(result.data);
        });
    };

    /**
     * override
     */
    self.validate = () => {
        return new Promise((resolve, reject) => {
            try {
                // 自定义校验逻辑
                const customValidationPromise = new Promise((resolveCustom, rejectCustom) => {
                    if (self.graph.validateInfo?.find(info => info?.nodeId === self.id)) {
                        // 自定义错误对象
                        rejectCustom({
                            errorFields: [{
                                errors: [`${self.text} ${self.graph.i18n?.t('selectedValueNotExist') ?? 'selectedValueNotExist'}`],
                                name: 'node-error',
                            }],
                        });
                    } else {
                        resolveCustom();
                    }
                });
                // 调用 form 的原始校验逻辑
                const formValidationPromise = self.validateForm();
                // 同时执行自定义校验和原始校验逻辑
                Promise.all([customValidationPromise, formValidationPromise])
                  .then(resolve) // 如果都通过，调用 resolve
                  .catch(reject); // 任何一个校验失败，都会触发 reject
            } catch (error) {
                reject({errorFields: [{errors: [error.message], name: 'node-error'}]});
            }
        });
    };

    return self;
};