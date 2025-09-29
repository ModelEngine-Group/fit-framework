/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useEffect, useRef, useState} from 'react';
import {JadeFlow} from './flow/jadeFlowEntry.jsx';
import {graphData} from './testFlowData.js';
import {Button} from 'antd';
import {CodeDrawer} from '@/components/common/code/CodeDrawer.jsx';
import 'antd/dist/antd.css';
import {createGraphOperator} from '@/data/GraphOperator.js';
import {v4 as uuidv4} from 'uuid';

function App({i18n}) {
  const [open, setOpen] = useState(false);
  const ctlRef = useRef();
  const agentRef = useRef();

  useEffect(() => {
    const stage = document.getElementById('stage');
    const configs = [];
    configs.push({
      node: 'startNodeStart',
      urls: {customHistoryUrl: ''},
    });
    configs.push({
      node: 'llmNodeState', urls: {
        llmModelEndpoint: '',
        toolListEndpoint: '',
        workflowListEndpoint: '',
      }, params: {
        tenantId: '', appId: '',
      },
    });
    configs.push({
      node: 'knowledgeState',
      urls: {knowledgeUrl: ''},
    });
    configs.push({
      node: 'fitInvokeState', urls: {
        serviceListEndpoint: '',
        fitableMetaInfoUrl: '',
      },
    });
    configs.push({
      node: 'manualCheckNodeState',
      urls: {runtimeFormUrl: ''},
    });
    configs.push({
      node: 'codeNodeState', urls: {
        testCodeUrl: '',
      },
    });
    configs.push({
      node: 'evaluationAlgorithmsNodeState', urls: {
        evaluationAlgorithmsUrl: '',
      },
    });
    configs.push({
      node: 'evaluationTestSetNodeState', urls: {
        datasetUrlPrefix: '',
      },
    });
    configs.push({
      node: 'queryOptimizationNodeState',
      urls: {
        llmModelEndpoint: '',
      },
    });
    configs.push({
      node: 'textConcatenateNodeState'
    });

    JadeFlow.edit({
      div: stage,
      tenant: '1111',
      appId: 'xxx',
      flowConfigData: graphData,
      configs: configs,
      i18n: i18n,
    }).then(agent => {
      window.agent = agent;
      window.operator = createGraphOperator(JSON.stringify(graphData));
      agentRef.current = agent;
      agent.onModelSelect((onModelSelectedCallback) => {
        onModelSelectedCallback.onSelect({name: 'zy-model'});
      });
      agent.onCreateButtonClick(() => {
      });
      agent.onChange(() => {
      });
      agent.onKnowledgeSearchArgsSelect(({callback}) => {
        callback({
          indexType: {
            type: 'enum-string',
            name: '语义检索',
            description: 'sdfsdfsdfsdfsdf',
          },
          similarityThreshold: 0.5,
          referenceLimit: {
            type: 'TOPK',
            value: 4,
          },
          rerankParam: {
            enableRerank: true,
          },
        });
      });
      agent.onKnowledgeBaseSelect((context) => {
        context.onSelect([
          {
            id: '1',
            name: 'ssdfsdf速度快放假水电费',
            description: '',
            type: 'VECTOR',
            createdAt: '2024-10-22 09:47:31',
            checked: true,
          },
        ]);
      });
      agent.onAddInputParam(({ onAdd, existParam }) => {
        const mockParam = {
          id: 'input_' + uuidv4(),
          name: 'userAge',
          type: 'Boolean',
          value: '',
          displayName: 'test',
          isRequired: true,
          isVisible: true,
          disableModifiable: false,
          appearance: {},
        };

        // 直接调用回调，模拟弹窗“确认”后的行为
        onAdd(mockParam);
        console.log(existParam);
      });

      agent.onEditInputParam(({id,  onEdit, selectedParam }) => {
        const mockParam = {
          id: id,
          name: 'userAge',
          type: 'String',
          value: '',
          displayName: 'test',
          isRequired: true,
          isVisible: true,
          disableModifiable: false,
          appearance: {},
        };

        // 直接调用回调，模拟弹窗“确认”后的行为
        onEdit(mockParam);

        console.log(selectedParam);
      });
      agent.listen('GENERATE_AI_PROMPT', (event) => {
        event.applyPrompt('123');
      });
      agent.listen('SELECT_KNOWLEDGE_BASE_GROUP', (event) => {
        event.onSelect('groupIdTest');
      });
    });
  }, []);

  const runTest = () => {
    ctlRef.current = agentRef.current.run();
    setTimeout(() => {
      ctlRef.current.refresh([
        {
          parameters: [
            {
              input: '{\"useMemory\":false,\"chatId\":\"9f1c069ec8224b7d93e72fa402217ebd\",\"Question\":\"11111\"}',
              output: '{\"useMemory\":false,\"chatId\":\"9f1c069ec8224b7d93e72fa402217ebd\",\"Question\":\"11111\"}',
            },
          ],
          nodeId: 'jade6qm5eg',
          nodeType: 'START',
          startTime: 1724772633987,
          runCost: 0,
          status: 'ARCHIVED',
          errorMsg: '',
        },
        {
          parameters: [
            {
              input: '{\"query\":\"11111\",\"maximum\":3,\"knowledge\":[{}]}',
              output: '{\"output\":{\"retrievalOutput\":\"\"}}',
            },
          ],
          nodeId: 'jade0pg2ag',
          nodeType: 'STATE',
          startTime: 1724772634000,
          runCost: 47,
          status: 'ARCHIVED',
          errorMsg: '',
        },
      ]);
      setTimeout(() => {
        ctlRef.current.stop([
          {
            parameters: [
              {
                input: '{\"chatId\":\"2ad84f7cbc2445798dc62d219f0fca42\",\"Question\":\"11111\"}',
                output: '{\"chatId\":\"2ad84f7cbc2445798dc62d219f0fca42\",\"Question\":\"11111\"}',
              },
            ],
            nodeId: 'jade6qm5eg',
            nodeType: 'START',
            startTime: 1725290745940,
            runCost: 0,
            status: 'ARCHIVED',
            errorMsg: '',
          },
          {
            parameters: [
              {
                input: '{\"query\":\"11111\",\"maximum\":3,\"knowledge\":[{}]}',
                output: '{\"output\":{\"retrievalOutput\":\"\"}}',
              },
            ],
            nodeId: 'jade0pg2ag',
            nodeType: 'STATE',
            startTime: 1725290745958,
            runCost: 27,
            status: 'ARCHIVED',
            errorMsg: '',
          },
          {
            parameters: [
              {
                input: '{\"branches\":[{\"conditionRelation\":\"and\",\"conditions\":[{\"condition\":\"equal\",\"left\":{\"type\":\"String\",\"value\":\"\",\"key\":\"jade0pg2ag.output.retrievalOutput\"},\"right\":{\"type\":\"String\",\"value\":\"123123\"}},{\"condition\":\"equal\",\"left\":{\"type\":\"String\",\"value\":\"11111\",\"key\":\"jade6qm5eg.Question\"},\"right\":{\"type\":\"String\",\"value\":\"2222\"}}]},{\"conditionRelation\":\"and\",\"conditions\":[{\"condition\":\"true\"}]}]}',
                output: null,
              },
            ],
            errorMsg: 'Condition rule parse error. Condition Rule: (businessData.get(\"_internal\").get(\"outputScope\").get(\"jadeih9c4h\").get(\"output\").isEmpty())',
            nodeId: 'jade62q33k',
            nodeType: 'CONDITION',
            startTime: 1725290746003,
            runCost: 39,
            status: 'ERROR',
          },
          {
            parameters: [
              {
                input: '{\"finalOutput\":\"008a734011be4473ac87c268342b4630\"}',
                output: null,
              },
            ],
            nodeId: 'jadesoux5i',
            nodeType: 'END',
            startTime: 1725290746284,
            runCost: 63,
            status: 'ARCHIVED',
            errorMsg: '',
          },
        ]);
      }, 2000);
    }, 500);
  };

  const runReset = () => {
    ctlRef.current && ctlRef.current.reset();
  };

  return (<>
    <div>
      <div>
        <Button onClick={() => {
          window.agent.validate().then(() => {
          }).catch((error) => {
          });
        }}>validate</Button>
        <Button onClick={() => {
          setOpen(true);
        }}>打开drawer</Button>
        <Button onClick={() => runTest()}>调试</Button>
        <Button onClick={() => runReset()}>reset</Button>
        <Button onClick={() => window.agent.createNodeByPosition('textExtractionNodeState', {x:100, y:100}, {uniqueName : ''})}>创建文本提取节点</Button>
        <Button onClick={() => window.agent.createNodeByPosition('queryOptimizationNodeState', {x:100, y:100}, {uniqueName : ''})}>创建问题优化节点</Button>
        <Button onClick={() => window.agent.createNodeByPosition('codeNodeState', {x:100, y:100}, {uniqueName : ''})}>创建code节点</Button>
        <Button onClick={() => window.agent.createNodeByPosition('textConcatenateNodeState', {x:100, y:100}, {uniqueName : ''})}>创建文本拼接节点</Button>
        <Button onClick={() => {
          const nodeTypes = ['endNodeEnd',
            'retrievalNodeState',
            'llmNodeState',
            'manualCheckNodeState',
            'conditionNodeCondition',
            'codeNodeState',
            'knowledgeRetrievalNodeState',
            'queryOptimizationNodeState',
            'textExtractionNodeState',
            'questionClassificationNodeCondition',
            'variableAggregationNodeState',
            'httpNodeState',
            'fileExtractionNodeState',
            'textToImageNodeState',
            'noteNode',
            'loopNodeState',
            'intelligentFormNodeState'];
          nodeTypes.forEach((type, index) => {
            window.agent.createNodeByPosition(
              type,
              {x: 100 + index * 200, y: 100},
              {uniqueName: ''},
            );
          });
        }}>创建每一种节点</Button>
      </div>
      <div id='stage' style={{position: 'relative', width: 1600, height: 800}}></div>
      <CodeDrawer container={document.getElementById('stage')}
                  width={1232}
                  open={open}
                  languages={['python']}
                  editorConfig={{
                    language: 'python', code: 'async def main(args: Args) -> Output:\n return ret',
                    suggestions: [{label: 'zyyyyyyyyyyyy', insertText: 'zyyyyyyyyyyyy'}],
                  }}
                  onClose={() => setOpen(false)}
                  onConfirm={(v) => {
                    // 这里对编辑后的代码进行处理
                  }}
                  executeFunc={(args, language, callback) => {
                    // 这里调用执行代码的接口

                    // 接口返回的output通过callback传递给组件，展示output
                  }}/>
    </div>
  </>);
}

export default App;
