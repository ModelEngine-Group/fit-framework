/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import CodeWrapper from '@/components/code/CodeWrapper.jsx';
import {defaultComponent} from '@/components/defaultComponent.js';
import {
  addInput,
  addSubItem,
  deleteInput,
  deleteProperty,
  editInput,
  editOutputFieldProperty,
  editOutputType, toCodeOutputJsonSchema,
  toJsonSchema,
} from '@/components/util/JadeConfigUtils.js';

/**
 * code节点组件
 *
 * @param jadeConfig
 */
export const codeComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [
        {
          id: uuidv4(),
          name: 'args',
          type: 'Object',
          from: 'Expand',
          value: [
            {
              id: uuidv4(),
              name: 'input',
              type: 'String',
              from: 'Reference',
              value: '',
              referenceNode: '',
              referenceId: '',
              referenceKey: '',
            },
          ],
        }, {
          id: uuidv4(),
          name: 'code',
          type: 'String',
          from: 'Input',
          language: 'python',
          value: 'async def main(args: Args) -> Output:\n' +
            '    ret: Output = {\n' +
            '        "key0": args[\'input\'] + args[\'input\'],\n' +
            '        "key1": ["hello", "world"],\n' +
            '        "key2": {\n' +
            '            "key21": "hi"\n' +
            '        },\n' +
            '    }\n' +
            '    return ret',
        }, {
          id: uuidv4(),
          name: 'language',
          type: 'String',
          from: 'Input',
          value: 'python',
        },
      ],
      outputParams: [
        {
          id: uuidv4(),
          name: 'output',
          type: 'Object',
          from: 'Expand',
          value: [
            {id: uuidv4(), name: 'key0', type: 'String', from: 'Input', description: '', value: ''},
            {
              id: uuidv4(),
              name: 'key1',
              type: 'Array',
              from: 'Input',
              description: '',
              value: '',
            },
            {
              id: uuidv4(),
              name: 'key2',
              type: 'Object',
              from: 'Expand',
              description: '',
              value: [{
                id: uuidv4(),
                name: 'key21',
                type: 'String',
                from: 'Input',
                description: '',
                value: '',
              }],
            },
          ],
        },
      ],
    };
  };

  /**
   * @override
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<>
      <CodeWrapper shapeStatus={shapeStatus} data={data}/>
    </>);
  };

  /**
   * @override
   */
  const reducers = self.reducers;
  self.reducers = (config, action) => {
    /**
     * 修改code代码
     *
     * @private
     */
    const _editCode = () => {
      newConfig.inputParams = newConfig.inputParams.map(inputParam => {
        if (inputParam.name === 'code') {
          return {
            ...inputParam, value: action.value,
          };
        } else {
          return inputParam;
        }
      });
    };

    /**
     * 根据output生成jsonSchema，随后插入InputParams中
     */
    const _generateOutputSchemaToInputParams = () => {
      const outputSchema = toCodeOutputJsonSchema(newConfig.outputParams);
      const properties = {};
      properties.properties = outputSchema;
      let isOutputKeyExist = false;
      newConfig.inputParams = newConfig.inputParams.map(inputParam => {
        if (inputParam.name === 'output') {
          isOutputKeyExist = true;
          inputParam.value = properties;
        }
        return inputParam;
      });
      if (!isOutputKeyExist) {
        const newItem = {
          id: uuidv4(),
          name: 'output',
          type: 'Object',
          from: 'Input',
          value: properties,
        };
        newConfig.inputParams.push(newItem);
      }
    };

    let newConfig = {...config};
    switch (action.actionType) {
      case 'addInput': {
        addInput(newConfig, action);
        return newConfig;
      }
      case 'editInput': {
        editInput(newConfig, action);
        return newConfig;
      }
      case 'deleteInput': {
        deleteInput(newConfig, action);
        return newConfig;
      }
      case 'editCode': {
        _editCode();
        _generateOutputSchemaToInputParams();
        return newConfig;
      }
      case 'editOutputFieldProperty': {
        editOutputFieldProperty(newConfig, action);
        _generateOutputSchemaToInputParams();
        return newConfig;
      }
      case 'editOutputType': {
        editOutputType(newConfig, action);
        _generateOutputSchemaToInputParams();
        return newConfig;
      }
      case 'addSubItem': {
        addSubItem(newConfig, action);
        _generateOutputSchemaToInputParams();
        return newConfig;
      }
      case 'deleteRow': {
        deleteProperty(newConfig, action);
        _generateOutputSchemaToInputParams();
        return newConfig;
      }
      case 'changeFlowMeta': {
        newConfig.enableStageDesc = action.data.enableStageDesc;
        newConfig.stageDesc = action.data.stageDesc;
        return newConfig;
      }
      default: {
        return reducers.apply(self, [config, action]);
      }
    }
  };

  return self;
};
