/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import LlmFormWrapper from './LlmFormWrapper.jsx';
import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {DEFAULT_MAX_MEMORY_ROUNDS, DEFAULT_MCP_SERVERS} from '@/common/Consts.js';
import {
    AddInputParamReducer,
    AddOutputParamReducer,
    AddSkillReducer,
    ChangeAccessInfoConfigReducer,
    ChangeConfigReducer,
    ChangeInputParamsReducer,
    ChangeKnowledgeReducer, ChangeMcpServersReducer,
    ChangeOutputParamReducer,
    ChangePromptReducer,
    ChangeSkillConfigReducer,
    DeleteInputParamReducer,
    DeleteOutputParamReducer,
    DeleteToolReducer,
    MoveKnowledgeItemReducer,
    UpdateLogStatusReducer,
    UpdateToolsReducer,
} from '@/components/llm/reducers/reducers.js';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';

/**
 * 大模型节点组件
 *
 * @param jadeConfig
 * @param shape 图形对象.
 */
export const llmComponent = (jadeConfig, shape) => {
    const self = defaultComponent(jadeConfig);
    const addReducer = (map, reducer) => map.set(reducer.type, reducer);
    const builtInReducers = new Map();
    addReducer(builtInReducers, AddInputParamReducer(shape, self));
    addReducer(builtInReducers, AddSkillReducer(shape, self));
    addReducer(builtInReducers, AddOutputParamReducer(shape, self));
    addReducer(builtInReducers, ChangeAccessInfoConfigReducer(shape, self));
    addReducer(builtInReducers, ChangeConfigReducer(shape, self));
    addReducer(builtInReducers, ChangeFlowMetaReducer());
    addReducer(builtInReducers, ChangeInputParamsReducer(shape, self));
    addReducer(builtInReducers, ChangeKnowledgeReducer(shape, self));
    addReducer(builtInReducers, ChangeOutputParamReducer(shape, self));
    addReducer(builtInReducers, ChangePromptReducer(shape, self));
    addReducer(builtInReducers, ChangeSkillConfigReducer(shape, self));
    addReducer(builtInReducers, UpdateToolsReducer(shape, self));
    addReducer(builtInReducers, DeleteInputParamReducer(shape, self));
    addReducer(builtInReducers, DeleteOutputParamReducer(shape, self));
    addReducer(builtInReducers, DeleteToolReducer(shape, self));
    addReducer(builtInReducers, MoveKnowledgeItemReducer(shape, self));
    addReducer(builtInReducers, UpdateLogStatusReducer(shape, self));
    addReducer(builtInReducers, ChangeMcpServersReducer(shape, self));

    /**
     * 必须.
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : {
            inputParams: [
                {
                    id: uuidv4(),
                    name: 'model',
                    type: 'String',
                    from: 'Input',
                    value: '',
                },
                {
                    id: uuidv4(),
                    name: 'accessInfo',
                    type: 'Object',
                    from: 'Expand',
                    value: [
                        {id: uuidv4(), name: 'serviceName', type: 'String', from: 'Input', value: ''},
                        {id: uuidv4(), name: 'tag', type: 'String', from: 'Input', value: ''},
                    ]
                },
                {
                    id: uuidv4(),
                    name: 'temperature',
                    type: 'Number',
                    from: 'Input',
                    value: '0.3',
                },
                {
                    id: uuidv4(),
                    name: 'prompt',
                    type: 'Object',
                    from: 'Expand',
                    value: [
                        {id: uuidv4(), name: 'template', type: 'String', from: 'Input', value: ''},
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
                        }
                    ],
                },
                JSON.parse(JSON.stringify(DEFAULT_MAX_MEMORY_ROUNDS)),
                {id: uuidv4(), name: 'tools', type: 'Array', from: 'Expand', value: []},
                {id: uuidv4(), name: 'systemPrompt', type: 'String', from: 'Input', value: ''},
                {
                    id: uuidv4(),
                    from: 'Input',
                    name: 'enableLog',
                    type: 'Boolean',
                    value: true,
                },
                {
                    id: uuidv4(),
                    from: 'Expand',
                    name: 'knowledgeBases',
                    type: 'Array',
                    value: [],
                },
                JSON.parse(JSON.stringify(DEFAULT_MCP_SERVERS)),
            ],
            outputParams: [
                {
                    id: uuidv4(),
                    name: 'output',
                    type: 'Object',
                    from: 'Expand',
                    value: [
                        {id: uuidv4(), name: 'llmOutput', type: 'String', from: 'Input', description: '', value: ''},
                        {id: uuidv4(), name: 'reference', type: 'Array', from: 'Input', description: '', value: []},
                    ],
                }
            ],
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
        return (<><LlmFormWrapper shapeStatus={shapeStatus} data={data}/></>);
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