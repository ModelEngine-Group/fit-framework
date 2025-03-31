/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';

/**
 * addInputParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddInputParamReducer = () => {
  const self = {};
  self.type = 'addInputParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'prompt') {
            return {
              ...item, value: item.value.map(promptItem => {
                if (promptItem.name === 'variables') {
                  return {
                    ...promptItem, value: [...promptItem.value, {
                      id: action.id,
                      name: undefined,
                      type: 'String',
                      from: 'Reference',
                      value: '',
                    }],
                  };
                } else {
                  return promptItem;
                }
              }),
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * addSkill 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddSkillReducer = () => {
  const self = {};
  self.type = 'addSkill';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const tools = config.inputParams.find(item => item.name === 'tools');
    const workflows = config.inputParams.find(item => item.name === 'workflows');
    const toolUniqueNames = tools?.value?.map(item => item.value) ?? [];
    const workflowUniqueNames = workflows?.value?.map(item => item.value) ?? [];
    // 防止重复添加
    if (toolUniqueNames.includes(action.value) || workflowUniqueNames.includes(action.value)) {
      return newConfig;
    }
    const newSkill = {id: uuidv4(), type: 'String', from: 'Input', value: action.value};
    const newTools = {...tools, value: [...tools.value, newSkill]};
    replaceTool(newConfig, newTools, 'tools');
    return newConfig;
  };

  return self;
};

/**
 * addOutputParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddOutputParamReducer = () => {
  const self = {};
  self.type = 'addOutputParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'outputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'output') {
            return {
              ...item,
              value: [...item.value, {
                id: action.id,
                name: '',
                type: 'string',
                from: 'Input',
                description: '',
                value: '',
              }],
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * changePrompt 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangePromptReducer = () => {
  const self = {};
  self.type = 'changePrompt';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'prompt') {
            return {
              ...item, value: item.value.map(promptItem => {
                if (action.id === promptItem.id && promptItem.name === 'template') {
                  return {
                    ...promptItem, value: action.value,
                  };
                } else {
                  return promptItem;
                }
              }),
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * changeInputParams 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeInputParamsReducer = () => {
  const self = {};
  self.type = 'changeInputParams';

  const newInputParams = (inputParams, action) => {
    return inputParams.map(item => {
      if (item.name === 'prompt') {
        return newPrompt(item, action);
      } else {
        return item;
      }
    });
  };

  const newVariables = (promptItem, action) => {
    return {
      ...promptItem, value: promptItem.value.map(inputItem => {
        if (inputItem.id === action.id) {
          let updatedInputItem = {...inputItem};
          // 遍历 updateParams 中的每个对象，更新 updatedInputItem 中对应的属性
          action.updateParams.map((item) => {
            updatedInputItem[item.key] = item.value;
          });
          return updatedInputItem;
        } else {
          return inputItem;
        }
      }),
    };
  };

  const newPrompt = (item, action) => {
    return {
      ...item, value: item.value.map(promptItem => {
        if (promptItem.name === 'variables') {
          return newVariables(promptItem, action);
        } else {
          return promptItem;
        }
      }),
    };
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = newInputParams(value, action);
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * changeOutputParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeOutputParamReducer = () => {
  const self = {};
  self.type = 'changeOutputParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'outputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'output') {
            return {
              ...item, value: item.value.map(outputItem => {
                if (outputItem.id === action.id) {
                  return {...outputItem, [action.type]: action.value};
                } else {
                  return outputItem;
                }
              }),
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * changeConfig 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeConfigReducer = () => {
  const self = {};
  self.type = 'changeConfig';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.id === action.id) {
            return {
              ...item, value: action.value,
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * changeAccessInfoConfig 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeAccessInfoConfigReducer = () => {
  const self = {};
  self.type = 'changeAccessInfoConfig';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'accessInfo') {
            // 将 action.value 按 "&&" 分割为 serviceName 和 tag
            const [serviceName, tag] = action.value.split('&&');
            // 更新 accessInfo 的值
            return {
              ...item,
              value: item.value.map(subItem => {
                if (subItem.name === 'serviceName') {
                  return {...subItem, value: serviceName};
                }
                if (subItem.name === 'tag') {
                  return {...subItem, value: tag};
                }
                return subItem;
              }),
            };
          } else if (item.name === 'model') {
            return {...item, value : action.value.split('&&')[0]};
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * changeSkillConfig 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeSkillConfigReducer = () => {
  const self = {};
  self.type = 'changeSkillConfig';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.id === action.id) {
            return {
              ...item, value: action.value.map(valueItem => {
                return {id: uuidv4(), type: 'String', from: 'Input', value: valueItem};
              }),
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * changeKnowledge 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeKnowledgeReducer = () => {
  const self = {};
  self.type = 'changeKnowledge';

  const newInputParams = (inputParams, action) => {
    return inputParams.map(item => {
      if (item.name === 'knowledgeBases') {
        return newKnowledgeBases(item, action);
      } else {
        return item;
      }
    });
  };

  const newKnowledgeBases = (knowledgeBasesItem, action) => {
    return {
      ...knowledgeBasesItem,
      value: action.value.map(actionItem => {
        const existingKnowledgeBase = knowledgeBasesItem.value.find(knowledgeBase => knowledgeBase.referenceId === actionItem.referenceId);

        if (existingKnowledgeBase) {
          return existingKnowledgeBase;
        } else {
          actionItem.id = uuidv4();
          return actionItem;
        }
      }).filter(updatedKnowledgeBase => {
        // 删除原来有的但新的 action 中没有的 referenceId
        return action.value.some(item => item.referenceId === updatedKnowledgeBase.referenceId);
      }),
    };
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = newInputParams(value, action);
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * updateTools 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateToolsReducer = () => {
  const self = {};
  self.type = 'updateTools';

  const newInputParams = (inputParams, action) => {
    return inputParams.map(item => {
      if (item.name === 'tools') {
        return newTools(item, action);
      } else {
        return item;
      }
    });
  };

  const newTools = (tools, action) => {
    // 复制tools对象，避免直接修改原始数据
    return {
      ...tools,
      value: tools.value.map(tool => {
        const updatedTool = { ...tool };
        const actionItem = action.value.find(item => updatedTool.value === item.value);
        if (actionItem) {
          updatedTool.name = actionItem.name || updatedTool.name;
          updatedTool.tags = actionItem.tags || updatedTool.tags;
          updatedTool.version = actionItem.version || updatedTool.version;
        }
        return updatedTool;
      }),
    };
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = newInputParams(value, action);
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * deleteInputParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteInputParamReducer = () => {
  const self = {};
  self.type = 'deleteInputParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'prompt') {
            return {
              ...item, value: item.value.map(promptItem => {
                if (promptItem.name === 'variables') {
                  return {
                    ...promptItem,
                    value: promptItem.value.filter(inputItem => (inputItem.id !== action.id)),
                  };
                } else {
                  return promptItem;
                }
              }),
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * deleteOutputParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteOutputParamReducer = () => {
  const self = {};
  self.type = 'deleteOutputParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return config.filter(item => item.id !== action.id);
  };

  return self;
};

const replaceTool = (newData, newTools, keyName) => {
  newData.inputParams = newData.inputParams.map(item => {
    if (item.name === keyName) {
      return newTools;
    } else {
      return item;
    }
  });
};

/**
 * deleteTool 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteToolReducer = () => {
  const self = {};
  self.type = 'deleteTool';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const tools = newConfig.inputParams.find(item => item.name === 'tools');
    const workflows = newConfig.inputParams.find(item => item.name === 'workflows');
    const uniqueNames = tools.value.map(item => item.value);

    // 先从tools里面找，找到就删除，找不到就删除workflows里面的数据
    if (uniqueNames.includes(action.value)) {
      const newTools = {...tools, value: tools?.value?.filter(toolItem => toolItem.value !== action.value)};
      replaceTool(newConfig, newTools, 'tools');
    } else if (workflows && workflows.value.length > 0) {
      const newWorkflows = {
        ...workflows,
        value: workflows?.value?.filter(uniqueName => uniqueName.value !== action.value),
      };
      replaceTool(newConfig, newWorkflows, 'workflows');
    }
    return newConfig;
  };

  return self;
};

/**
 * moveKnowledgeItem 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const MoveKnowledgeItemReducer = () => {
  const self = {};
  self.type = 'moveKnowledgeItem';

  const newInputParams = (config, inputParams, action) => {
    return inputParams.map(item => {
      if (item.name === 'knowledgeBases') {
        if (action.updateParams.find(updateParam => updateParam.key === 'value').value.length > 0) {
          return newMoveInKnowledgeBases(config, item, action);
        }
        return newMoveOutKnowledgeBases(config, item, action);
      } else {
        return item;
      }
    });
  };

  const newMoveOutKnowledgeBases = (config, knowledgeBasesItem, action) => {
    const knowledgeBasesMoveOutItems = config.tempReference[knowledgeBasesItem.id] ?? [];
    const targetItem = knowledgeBasesItem.value.find(v => v.id === action.id);
    if (targetItem) {
      let updatedTargetItem = {...targetItem};
      action.updateParams.map((item) => {
        updatedTargetItem[item.key] = item.value;
      });
      knowledgeBasesMoveOutItems.push(updatedTargetItem);
    }
    config.tempReference[knowledgeBasesItem.id] = knowledgeBasesMoveOutItems;
    return {
      ...knowledgeBasesItem, value: knowledgeBasesItem.value.filter(knowledgeBase => knowledgeBase.id !== action.id),
    };
  };

  const newMoveInKnowledgeBases = (config, knowledgeBasesItem, action) => {
    const knowledgeBasesMoveOutItems = config.tempReference[knowledgeBasesItem.id] ?? [];
    const specifyMoveOutItem = knowledgeBasesMoveOutItems.find(item => item.id === action.id);
    config.tempReference[knowledgeBasesItem.id] = knowledgeBasesMoveOutItems.filter(item => item.id !== action.id);
    if (specifyMoveOutItem && !knowledgeBasesItem.value.find(item => item.id === action.id)) {
      knowledgeBasesItem.value.push(specifyMoveOutItem);
    }
    return {
      ...knowledgeBasesItem, value: knowledgeBasesItem.value.map(knowledgeBase => {
        if (knowledgeBase.id === action.id) {
          let updatedKnowledgeBaseItem = {...knowledgeBase};
          // 遍历 updateParams 中的每个对象，更新 updatedKnowledgeBaseItem 中对应的属性
          action.updateParams.map((item) => {
            updatedKnowledgeBaseItem[item.key] = item.value;
          });
          return updatedKnowledgeBaseItem;
        } else {
          return knowledgeBase;
        }
      }),
    };
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = newInputParams(config, value, action);
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * updateLogStatus 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateLogStatusReducer = () => {
  const self = {};
  self.type = 'updateLogStatus';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const enableLogIndex = config.inputParams.findIndex(p => p.name === 'enableLog');

    if (enableLogIndex === -1) {
      newConfig.inputParams.push({
        id: uuidv4(),
        from: 'input',
        name: 'enableLog',
        type: 'Boolean',
        value: action.value,
      });
    } else {
      newConfig.inputParams = config.inputParams.map((item, index) => {
        if (index === enableLogIndex) {
          return {
            ...item,
            value: action.value,
          };
        }
        return item;
      });
    }
    return newConfig;
  };

  return self;
};