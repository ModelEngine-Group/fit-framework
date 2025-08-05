/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from '@/components/base/jadeNode.jsx';
import './style.css';
import {codeComponent} from '@/components/code/codeComponent.jsx';
import {SECTION_TYPE} from '@/common/Consts.js';
import {codeNodeDrawer} from '@/components/code/CodeNodeDrawer.jsx';

/**
 * 代码节点shape
 *
 * @override
 */
export const codeNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : codeNodeDrawer);
    self.type = "codeNodeState";
    self.componentName = "codeComponent";
    self.text = "代码节点"
    self.width = 368;
    self.flowMeta.jober.type = 'STORE_JOBER';
    const toolEntity = {
        uniqueName: "",
        params: [],
        return: {
            type: ""
        }
    };
    const template = {
        inputParams: [],
        outputParams: []
    };

    /**
     * @override
     */
    const serializerJadeConfig = self.serializerJadeConfig;
    self.serializerJadeConfig = (jadeConfig) => {
        serializerJadeConfig.apply(self, [jadeConfig]);
        const newConfig = {...template};
        newConfig.outputParams = self.flowMeta.jober.converter.entity.outputParams;
        newConfig.inputParams = self.flowMeta.jober.converter.entity.inputParams;
        self.flowMeta.jober.converter.entity = newConfig;
        self.flowMeta.jober.entity.return.type = "object";
        self.flowMeta.jober.entity.params = self.flowMeta.jober.converter.entity.inputParams.map(property => {
            return {name: property.name}
        });
    };

    /**
     * @override
     */
    const processMetaData = self.processMetaData;
    self.processMetaData = (metaData) => {
        if (!metaData) {
            return;
        }
        processMetaData.apply(self, [metaData]);
        self.flowMeta.jober.entity = toolEntity;
        self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
    };

    /**
     * 获取code节点测试报告章节
     */
    self.getRunReportSections = () => {
        const _getInputData = () => {
            if (self.input && self.input.args) {
                return self.input.args;
            } else {
                return {};
            }
        };

        // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
        return [{
            no: "1",
            name: "input",
            type: SECTION_TYPE.DEFAULT,
            data: _getInputData(),
        }, {
            no: "2",
            name: "output",
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(self.output)
        }];
    };

    /**
     * @override
     */
    self.maxNumToLink = () => {
        return 10;
    };

    return self;
}