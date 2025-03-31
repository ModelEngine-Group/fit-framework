/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useState} from 'react';
import {EvaluationAlgorithmsSelect} from '@/components/evaluation/evaluationAlgorithms/EvaluationAlgorithmsSelect.jsx';
import {PassingScore} from '@/components/evaluation/evaluationAlgorithms/PassingScore.jsx';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import {EvaluationInput} from '@/components/evaluation/evaluationAlgorithms/EvaluationInput.jsx';
import PropTypes from 'prop-types';
import {EVALUATION_ALGORITHM_NODE_CONST} from '@/common/Consts.js';
import httpUtil from '@/components/util/httpUtil.jsx';

EvaluationAlgorithmsWrapper.propTypes = {
  data: PropTypes.object,
  disabled: PropTypes.bool,
};

/**
 * 获取评估算法节点配置数据
 *
 * @param shape 评估算法节点shape
 * @return {*} 配置数据
 */
const getEvaluationAlgorithmsConfig = shape => {
  if (!shape || !shape.graph || !shape.graph.configs) {
    throw new Error('Cannot get shape.graph.configs.');
  } else {
    return shape.graph.configs.find(node => node.node === 'evaluationAlgorithmsNodeState');
  }
};

EvaluationAlgorithmsWrapper.propTypes = {
  shapeStatus: PropTypes.object,
  data: PropTypes.object,
};

/**
 * 评估算法节点组件
 *
 * @param data 节点数据
 * @param shapeStatus 节点状态
 * @constructor
 */
export default function EvaluationAlgorithmsWrapper({data, shapeStatus}) {
  const shape = useShapeContext();
  const selectedAlgorithm = data && (data.inputParams.find(item => item.name === EVALUATION_ALGORITHM_NODE_CONST.UNIQUE_NAME)?.value ?? '');
  const algorithmInput = data.inputParams.find(item => item.name === 'algorithmArgs').value;
  const nodeOutput = data.outputParams;
  const passScore = data.inputParams.find(item => item.name === EVALUATION_ALGORITHM_NODE_CONST.PASS_SCORE).value;
  const config = getEvaluationAlgorithmsConfig(shape);
  const [algorithms, setAlgorithms] = useState([]);

  useEffect(() => {
    if (!config.urls.evaluationAlgorithmsUrl) {
      throw new Error('Cannot get config.urls.evaluationAlgorithmsUrl.');
    } else {
      httpUtil.get(config.urls.evaluationAlgorithmsUrl + '?includeTags=ALGORITHM&pageNum=1&pageSize=10',
        new Map(),
        (jsonData) => setAlgorithms(jsonData.data),
        (error) => {
          throw new Error(`get evaluation algorithms failed: ${error}`);
        });
    }
  }, []); // useEffect 依赖数组为空，表示只在组件挂载时执行一次

  /**
   * 获取输出描述信息
   *
   * @return {JSX.Element}
   */
  const getDescription = () => {
    return <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <p>Score:算法评分</p>
      <p>IsPass:是否通过</p>
    </div>;
  };

  return (<>
    <EvaluationInput inputData={algorithmInput} shapeStatus={shapeStatus}/>
    <EvaluationAlgorithmsSelect disabled={shapeStatus.disabled}
                                algorithms={algorithms}
                                selectedAlgorithm={selectedAlgorithm}/>
    <PassingScore disabled={shapeStatus.disabled} score={passScore}/>
    <InvokeOutput outputData={nodeOutput} getDescription={getDescription}/>
  </>);
}