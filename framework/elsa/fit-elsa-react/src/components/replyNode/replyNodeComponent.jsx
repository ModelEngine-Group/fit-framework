/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ReplyWrapper from './ReplyWrapper.jsx';
import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {
    AddInputParamReducer,
    ChangeTemplateReducer,
    DeleteInputParamReducer,
    EditInputParamReducer
} from "@/components/replyNode/reducers/reducer.js";

/**
 * 直接回复节点组件
 *
 * @param jadeConfig
 * @param shape 图形对象.
 */
export const replyNodeComponent = (jadeConfig, shape) => {
    const self = defaultComponent(jadeConfig);
    const addReducer = (map, reducer) => map.set(reducer.type, reducer);
    const builtInReducers = new Map();
    addReducer(builtInReducers, AddInputParamReducer());
    addReducer(builtInReducers, EditInputParamReducer());
    addReducer(builtInReducers, ChangeTemplateReducer());
    addReducer(builtInReducers, DeleteInputParamReducer());

    /**
     * 必须.
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : {
            inputParams: [
                {
                    id: uuidv4(), name: 'variables', type: 'Object', from: 'Expand', value: [
                        {
                            id: uuidv4(),
                            name: undefined,
                            type: 'String',
                            from: 'Reference',
                            value: '',
                            referenceNode: '',
                            referenceId: '',
                            referenceKey: '',
                        }
                    ],
                },
                {
                    id: uuidv4(),
                    name: 'template',
                    type: 'String',
                    from: 'Input',
                    value: ''
                },
            ],
            outputParams: [],
            tempReference: {},
        };
    };

    /**
     * 必须.
     *
     * @param shapeStatus 图形状态集合.
     * @param data 数据.
     */
    self.getReactComponents = (shapeStatus, data) => {
        return (<><ReplyWrapper shapeStatus={shapeStatus} data={data}/></>);
    };

    /**
     * 必须.
     */
    const reducers = self.reducers;
    self.reducers = (data, action) => {
        const reducer = builtInReducers.get(action.type);
        return reducer ? reducer.reduce(data, action) : reducers.apply(self, [data, action]);
    };

    return self;
};