/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

class Validator {
  constructor(node) {
    this.node = node;
  }

  validate() {
    throw new Error('Method \'validate()\' must be implemented.');
  }
}

export class FormValidator extends Validator {
  validate() {
    return new Promise((resolve, reject) => {
      try {
        this.node.validateForm().then(resolve).catch(reject);
      } catch (error) {
        reject({
          errorFields: [{
            errors: [error.message],
            name: 'node-error',
          }],
        });
      }
    });
  }
}

export class NormalNodeConnectorValidator extends Validator {
  validate() {
    const nextEvents = this.node.getNextRunnableEvents();
    const i18n = this.node.graph.i18n;
    if (nextEvents.length !== 1) {
      return Promise.reject({
        errorFields: [{
          errors: [`${i18n?.t('node') ?? 'node'} ${this.node.text} ${i18n?.t('problemWithConnection') ?? 'problemWithConnection'}`],
          name: 'link-error',
        }],
      });
    }
    return Promise.resolve();
  }
}

export class ConditionNodeConnectorValidator extends Validator {
  validate() {
    const nextEvents = this.node.getNextRunnableEvents();
    const i18n = this.node.graph.i18n;
    if (nextEvents.length !== this.node.getBranches().filter(b => b.runnable).length) {
      return Promise.reject({
        errorFields: [{
          errors: [`${i18n?.t('node') ?? 'node'} ${this.node.text} ${i18n?.t('problemWithConnection') ?? 'problemWithConnection'}`],
          name: 'link-error',
        }],
      });
    }
    return Promise.resolve();
  }
}

export class EndNodeConnectorValidator extends Validator {
  validate() {
    const nextEvents = this.node.getNextRunnableEvents();
    const i18n = this.node.graph.i18n;
    if (nextEvents.length !== 0) {
      return Promise.reject({
        errorFields: [{
          errors: [`${i18n?.t('node') ?? 'node'} ${this.node.text} ${i18n?.t('problemWithConnection') ?? 'problemWithConnection'}`],
          name: 'link-error',
        }],
      });
    }
    return Promise.resolve();
  }
}

export class KnowledgeRetrievalValidator extends Validator {
  validate() {
    const jadeConfig = this.node.drawer.getLatestJadeConfig();
    const i18n = this.node.graph.i18n;

    // 校验搜索参数.
    const option = jadeConfig.inputParams.find(ip => ip.name === 'option');
    if (option.value.length === 0) {
      return Promise.reject({
        errorFields: [{
          errors: [`${this.node.text} ${i18n?.t('noSearchOption') ?? 'noSearchOption'}`],
          name: 'search-args-error',
        }],
      });
    }

    return Promise.resolve();
  }
}
