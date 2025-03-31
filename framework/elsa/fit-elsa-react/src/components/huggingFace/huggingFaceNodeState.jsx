/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {toolInvokeNodeState} from '@/components/toolInvokeNode/toolInvokeNodeState.jsx';
import {huggingFaceNodeDrawer} from '@/components/huggingFace/huggingFaceNodeDrawer.jsx';
import {SOURCE_PLATFORM} from '@/common/Consts.js'; // 导入背景图片

/**
 * huggingFace节点.
 *
 * @override
 */
export const huggingFaceNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = toolInvokeNodeState(id, x, y, width, height, parent, drawer ? drawer : huggingFaceNodeDrawer);
    self.type = "huggingFaceNodeState";
    self.text = "huggingFace调用";
    self.componentName = "huggingFaceComponent";
    self.width = 368;

    /**
     * @override
     */
    const processMetaData = self.processMetaData;
    self.processMetaData = (metaData) => {
        if (!metaData) {
            return;
        }
        processMetaData.apply(self, [metaData]);
        self.text = metaData.schema.name;
        const INPUT_FROM_TYPE_VALUE = "Input";
        self.flowMeta.jober.converter.entity.inputParams[0].from = INPUT_FROM_TYPE_VALUE;
        self.flowMeta.jober.converter.entity.inputParams[1].from = INPUT_FROM_TYPE_VALUE;
        self.flowMeta.jober.converter.entity.inputParams[0].value = metaData.schema.name;
        self.flowMeta.jober.converter.entity.inputParams[1].value = metaData.context.default_model;
        self.sourcePlatform = SOURCE_PLATFORM.HUGGING_FACE;
        self.drawer.unmountReact();
        self.invalidateAlone();
    };

    return self;
}