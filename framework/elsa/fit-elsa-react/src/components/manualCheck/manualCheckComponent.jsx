/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ManualCheckFormWrapper from "@/components/manualCheck/ManualCheckFormWrapper.jsx";
import {defaultComponent} from "@/components/defaultComponent.js";
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';
import {ChangeFormByMetaDataReducer, DeleteFormReducer, UpdateInputReducer} from '@/components/manualCheck/reducers/reducers.js';

export const manualCheckComponent = (jadeConfig) => {
    const self = defaultComponent(jadeConfig);
    const addReducer = (map, reducer) => map.set(reducer.type, reducer);
    const builtInReducers = new Map();
    addReducer(builtInReducers, ChangeFormByMetaDataReducer());
    addReducer(builtInReducers, DeleteFormReducer());
    addReducer(builtInReducers, UpdateInputReducer());
    addReducer(builtInReducers, ChangeFlowMetaReducer());

    /**
     * 必须.
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : {
            converter: {
                type: 'mapping_converter',
                entity: {
                    inputParams: [],
                    outputParams: [],
                },
            },
            taskId: '',
            type: 'AIPP_SMART_FORM',
            formName: '',
            imgUrl: undefined,
        };
    };

    /**
     * 必须.
     */
    self.getReactComponents = (shapeStatus) => {
        return (<>
            <ManualCheckFormWrapper shapeStatus={shapeStatus}/>
        </>);
    };

    /**
     * @override
     */
    const reducers = self.reducers;
    self.reducers = (config, action) => {
        // 等其他节点改造完成，可以将reducers相关逻辑提取到基类中，子类中只需要向builtInReducers中添加reducer即可.
        const reducer = builtInReducers.get(action.type);
        return reducer ? reducer.reduce(config, action) : reducers.apply(self, [config, action]);
    };

    return self;
};