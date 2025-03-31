/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {evaluationNode} from "@/components/evaluation/evaluationNode.jsx";
import {evaluationEndComponent} from "@/components/evaluation/evaluationEnd/evaluationEndComponent.jsx";
import {evaluationEndNodeDrawer} from "@/components/evaluation/evaluationEnd/evaluationEndNodeDrawer.jsx";
import {DIRECTION} from "@fit-elsa/elsa-core";
import {SECTION_TYPE} from "@/common/Consts.js";
import {EndNodeConnectorValidator, FormValidator} from '@/components/base/validator.js';

/**
 * 评估结束节点shape
 *
 * @override
 */
export const evaluationEndNodeEnd = (id, x, y, width, height, parent, drawer) => {
    const self = evaluationNode(id, x, y, width, height, parent, drawer ? drawer : evaluationEndNodeDrawer);
    self.type = "evaluationEndNodeEnd";
    self.componentName = "evaluationEndComponent";
    self.text = "评估结束";
    self.width = 461;
    self.deletable = false;
    self.flowMeta = {
        "triggerMode": "auto",
        "callback": {
            "type": "general_callback",
            "name": "通知回调",
            "fitables": ["modelengine.jade.app.engine.task.fitable.EvalTaskFlowEndCallback"],
            "converter": {
                "type": "mapping_converter"
            },
        }
    };

    /**
     * 序列化组件信息
     *
     * @override
     */
    self.serializerJadeConfig = (jadeConfig) => {
        self.flowMeta.callback.converter.entity = jadeConfig;
    };

    /**
     * 评估结束节点设置E方向没有连接点
     *
     * @override
     */
    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.apply(self);
        self.connectors.remove(c => c.direction.key === DIRECTION.E.key);
    };

    /**
     * 获取用户自定义组件.
     *
     * @override
     */
    self.getComponent = () => {
        return self.graph.plugins[self.componentName](self.flowMeta.callback.converter.entity);
    };

    /**
     * 评估结束节点的测试报告章节
     */
    self.getRunReportSections = () => {
        return [{
            no: "1",
            name: "输出",
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(self.input)
        }];
    };

    /**
     * @override
     */
    self.validate = () => {
        const validators = [new FormValidator(self), new EndNodeConnectorValidator(self)];
        return self.runValidators(validators);
    };

    return self;
}
