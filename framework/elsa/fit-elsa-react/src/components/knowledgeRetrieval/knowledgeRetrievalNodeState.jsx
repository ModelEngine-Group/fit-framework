/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {retrievalNodeState} from '@/components/retrieval/retrievalNodeState.jsx';
import {knowledgeRetrievalNodeDrawer} from '@/components/knowledgeRetrieval/knowledgeRetrievalNodeDrawer.jsx';
import {FormValidator, KnowledgeRetrievalValidator, NormalNodeConnectorValidator} from '@/components/base/validator.js';

/**
 * 知识检索shape
 *
 * @override
 */
export const knowledgeRetrievalNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = retrievalNodeState(id, x, y, width, height, parent, drawer ? drawer : knowledgeRetrievalNodeDrawer);
  self.type = 'knowledgeRetrievalNodeState';
  self.text = '知识检索';
  self.componentName = 'knowledgeRetrievalComponent';
  self.flowMeta = {
    triggerMode: 'auto',
    jober: {
      type: 'STORE_JOBER',
      name: '',
      fitables: [],
      converter: {
        type: 'mapping_converter',
      },
      entity: {
        uniqueName: '',
        params: [
          {
            name: 'query',
          },
          {
            name: 'knowledgeRepos',
          },
          {
            name: 'option',
          },
        ],
        return: {
          type: 'object',
        },
      },
    },
    joberFilter: {
      type: 'MINIMUM_SIZE_FILTER',
      threshold: 1,
    },
  };

  /**
   * @override
   */
  const processMetaData = self.processMetaData;
  self.processMetaData = (metaData) => {
    if (!metaData) {
      return;
    }
    processMetaData.apply(self, [metaData]);
    self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
  };

  /**
   * 校验节点状态是否正常.
   *
   * @param linkNodeSet 链路中的节点列表的Set
   * @return Promise 校验结果
   */
  self.validate = (linkNodeSet) => {
    const validators = [new FormValidator(self)];
    if (linkNodeSet.has(self.id)) {
      validators.push(new NormalNodeConnectorValidator(self));
      validators.push(new KnowledgeRetrievalValidator(self));
    }
    return self.runValidators(validators);
  };

  return self;
};