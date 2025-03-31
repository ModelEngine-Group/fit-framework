/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {DEFAULT_FLOW_META, SECTION_TYPE} from '@/common/Consts.js';
import {questionClassificationNodeDrawer} from '@/components/questionClassification/questionClassificationNodeDrawer.jsx';
import {conditionNodeCondition} from '@/components/condition/conditionNodeCondition.jsx';
import {convertToBranchesFormat} from '@/components/util/JadeConfigUtils.js';

/**
 * 问题改写节点.
 *
 * @override
 */
export const questionClassificationNodeCondition = (id, x, y, width, height, parent, drawer) => {
  const self = conditionNodeCondition(id, x, y, width, height, parent, drawer ? drawer : questionClassificationNodeDrawer);
  self.type = 'questionClassificationNodeCondition';
  self.text = '问题分类';
  self.width = 360;
  self.componentName = 'questionClassificationComponent';
  self.flowMeta = JSON.parse(DEFAULT_FLOW_META);

  /**
   * 序列化问题分类节点，序列化为工具节点类型
   *
   * @override
   */
  self.serializerJadeConfig = (jadeConfig) => {
    self.flowMeta.jober.converter.entity = jadeConfig;
    self.flowMeta.enableStageDesc = self.flowMeta.jober.converter.entity.enableStageDesc;
    self.flowMeta.stageDesc = self.flowMeta.jober.converter.entity.stageDesc;
    self.flowMeta.jober.entity.params = self.flowMeta.jober.converter.entity.inputParams.map(property => {
      return {name: property.name};
    });
    self.flowMeta.jober.entity.return.type = 'string';
    self.flowMeta.conditionParams = {branches: convertToBranchesFormat(jadeConfig, self.id)};
  };

  /**
   * 获取用户自定义组件.
   *
   * @return {*}
   */
  self.getComponent = () => {
    return self.graph.plugins[self.componentName](self.flowMeta.jober.converter.entity, self);
  };

  /**
   * 处理传递的元数据
   *
   * @param metaData 元数据信息
   */
  self.processMetaData = (metaData) => {
    if (metaData && metaData.name) {
      self.text = metaData.name;
    }
    self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
  };

  /**
   * 判断引用是否可用.
   *
   * @param preNodeInfos 前序节点信息.
   * @param observerProxy 观察者.
   * @return {*} true/false.
   */
  self.isReferenceAvailable = (preNodeInfos, observerProxy) => {
    return preNodeInfos.some(pre => pre.id === observerProxy.nodeId && pre.runnable === self.runnable);
  };

  /**
   * @override
   */
  self.getBranches = () => {
    return self.drawer.getLatestJadeConfig().inputParams.find(param => param.name === 'classifyQuestionParam').value.find(questionParam => questionParam.name === 'questionTypeList').value;
  };

  /**
   * 问题分类节点默认的测试报告章节
   */
  self.getRunReportSections = () => {
    // 过滤掉else分支
    const conditionSections = [];
    // 添加新的 object
    conditionSections.push({
      no: '1',
      name: 'output',
      type: SECTION_TYPE.DEFAULT,
      data: self.getOutputData(self.output),
    });
    return conditionSections;
  };

  /**
   * 构造输出，重写条件节点的方法
   *
   * @param runFromConnectorName 锚点名
   * @param i18n 国际化组件
   */
  self.buildOutput = (runFromConnectorName, i18n) => {
    if (runFromConnectorName.includes('|')) {
      const match = runFromConnectorName.match(/dynamic-(\d+)/);
      const questionList = self.flowMeta.jober.converter.entity.inputParams.find(item => item.name === 'classifyQuestionParam')
        .value.find(param => param.name === 'questionTypeList');
      if (match) {
        const index = parseInt(match[1], 10);
        const questionDesc = questionList.value[index].value.find(item => item.name === 'questionTypeDesc').value;
        self.output = {result: `${i18n.t('questionClassificationReportPrefix')}-${questionDesc}`};
      }
    } else {
      // 如果没有 '|'，则说明走的是Else分支
      self.output = {result: `${i18n.t('questionClassificationReportPrefix')}-${i18n.t('otherQuestionClassification')}`};
    }
  };

  return self;
};