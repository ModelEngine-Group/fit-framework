/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeEvaluationPage} from '@/flow/evaluation/jadeEvaluationPage.js';
import {
  evaluationAlgorithmsComponent,
} from '@/components/evaluation/evaluationAlgorithms/evaluationAlgorithmsComponent.jsx';
import {
  evaluationAlgorithmsNodeState,
} from '@/components/evaluation/evaluationAlgorithms/evaluationAlgorithmsNodeState.jsx';
import {evaluationStartComponent} from '@/components/evaluation/evaluationStart/evaluationStartComponent.jsx';
import {evaluationStartNodeStart} from '@/components/evaluation/evaluationStart/evaluationStartNodeStart.jsx';
import {evaluationTestSetNodeState} from '@/components/evaluation/evaluationTestset/evaluationTestSetNodeState.jsx';
import {evaluationTestSetComponent} from '@/components/evaluation/evaluationTestset/evaluationTestSetComponent.jsx';
import {evaluationEndNodeEnd} from '@/components/evaluation/evaluationEnd/evaluationEndNodeEnd.jsx';
import {evaluationEndComponent} from '@/components/evaluation/evaluationEnd/evaluationEndComponent.jsx';
import {jadeFlowGraph} from '@/flow/jadeFlowGraph.js';

/**
 * jadeFlow的专用画布.
 *
 * @param div dom元素.
 * @param title 名称.
 */
export const jadeEvaluationGraph = (div, title) => {
  const self = jadeFlowGraph(div, title);
  self.type = 'jadeEvaluationGraph';
  self.pageType = 'jadeEvaluationPage';
  self.compatibilityManager.type = 'evaluation';

  /**
   * 导入flow相关依赖.
   *
   * @override
   */
  const initialize = self.initialize;
  self.initialize = async () => {
    self.registerPlugin('jadeEvaluationPage', jadeEvaluationPage);
    self.registerPlugin('evaluationStartComponent', evaluationStartComponent);
    self.registerPlugin('evaluationStartNodeStart', evaluationStartNodeStart);
    self.registerPlugin('evaluationAlgorithmsComponent', evaluationAlgorithmsComponent);
    self.registerPlugin('evaluationAlgorithmsNodeState', evaluationAlgorithmsNodeState);
    self.registerPlugin('evaluationTestSetComponent', evaluationTestSetComponent);
    self.registerPlugin('evaluationTestSetNodeState', evaluationTestSetNodeState);
    self.registerPlugin('evaluationEndComponent', evaluationEndComponent);
    self.registerPlugin('evaluationEndNodeEnd', evaluationEndNodeEnd);
    return initialize.apply(self);
  };

  /**
   * @override
   */
  const deSerialize = self.deSerialize;
  self.deSerialize = data => {
    data.type = 'jadeEvaluationGraph';
    data.pageType = 'jadeEvaluationPage';
    deSerialize.apply(self, [data]);
  };

  /**
   * 评估.
   *
   * @param graphData 画布数据.
   * @param isPublished 是否已发布.
   * @return {Promise<void>} Promise.
   */
  self.evaluate = async (graphData, isPublished) => {
    const pageData = self.getPageData(0);
    normalizeData(pageData, isPublished);
    const page = await self.edit(0, self.div, pageData.id);
    page.normalize(graphData, isPublished);
  };

  const normalizeData = (pageData, isPublished) => {
    pageData.type = 'jadeEvaluationPage';
    pageData.namespace = 'jadeEvaluationFlow';
    if (isPublished) {
      pageData.shapes.forEach(shapeData => {
        shapeData.moveable = false;
        shapeData.selectable = false;
        shapeData.deletable = false;
        shapeData.published = true;
      });
    } else {
      // 一个应用第一次被打开对应的评估页面
      const start = pageData.shapes.find(s => s.type === 'evaluationStartNodeStart');
      if (!start) {
        self.compatibilityManager.initializeData(pageData, self);
      }
    }
  };

  /**
   * 序列化flowMeta中新增onCompletedFitables字段.
   *
   * @override
   */
  const serialize = self.serialize;
  self.serialize = () => {
    const serialized = serialize.apply(self);
    serialized.flowMeta.onCompletedFitables = ['modelengine.jade.app.engine.task.fitable.EvalTaskUpdater'];
    return serialized;
  };

  return self;
};
