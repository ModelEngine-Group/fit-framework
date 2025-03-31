/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInputParam} from '@/components/util/JadeConfigUtils.js';
import {v4 as uuidv4} from 'uuid';
import {HTTP_BODY_TYPE} from '@/common/Consts.js';

/**
 * changeRequestUrl 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeRequestUrlReducer = () => {
  const self = {};
  self.type = 'changeRequestUrl';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    let {rawUrl, paramsPair} = _processUrl(action.value);
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'httpRequest', item =>
        _updateItem(item, action, rawUrl, paramsPair))),
    };
  };

  const _processUrl = (url) => {
    let rawUrl = url;
    let paramsPair = [];
    if (url.includes('?')) {
      const urlPart = url.split('?');
      rawUrl = urlPart[0];
      if (urlPart.length > 1 && urlPart[1].includes('&')) {
        paramsPair = urlPart[1].split('&');
      }
    }
    return {rawUrl, paramsPair};
  };

  const _updateItem = (item, action, rawUrl, paramsPair) => {
    if (item.id === action.id) {
      return {...item, value: rawUrl};
    } else if (paramsPair.length >= 1 && item.name === 'params') {
      const newParams = _extractNewParams(item.value, paramsPair);
      return {...item, value: [...item.value, ...newParams]};
    }
    return item;
  };

  const _extractNewParams = (existingParams, paramsPair) => {
    const existsArgNames = existingParams.map(arg => arg.name);
    return paramsPair
      .filter(param => !existsArgNames.includes(param.split('=')[0]))
      .map(param => {
        const [key, value] = param.split('=');
        return {
          id: `${uuidv4()}`,
          name: key,
          type: 'String',
          from: 'Input',
          value: value,
        };
      });
  };
  return self;
};

/**
 * changeRequestConfig 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeRequestConfigReducer = () => {
  const self = {};
  self.type = 'changeRequestConfig';

  const _updateRequestItem = (item, action) => {
    if (item.id === action.id) {
      return {...item, value: action.value};
    } else {
      return item;
    }
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'httpRequest', (item) => {
        return _updateRequestItem(item, action);
      })),
    };
  };

  return self;
};

/**
 * confirm 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ConfirmReducer = () => {
  const self = {};
  self.type = 'confirm';

  const _updateAuthentication = (item, action) => {
    if (item.name === 'authentication') {
      const value = Object.keys(action.changes).map(property => {
        return {...item.value.find(p => p.name === property), value: action.changes[property]};
      });
      return {...item, value: value};
    } else {
      return item;
    }
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'httpRequest', (item) => {
        return _updateAuthentication(item, action);
      })),
    };
  };

  return self;
};

/**
 * ConfigChangeReducer 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ConfigChangeReducer = () => {
  const self = {};
  self.type = 'configChange';

  const _updateConfig = (item, action) => {
    if (item.name !== action.updateKey) {
      return item;
    }
    return {
      ...item,
      value: _updateValue(item.value, action),
    };
  };

  const _updateValue = (values, action) => values.map(v => updateValueItem(v, action));

  const updateValueItem = (v, action) => {
    if (v.id === action.id) {
      return {...v, [action.property]: action.value};
    }
    return v;
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'httpRequest', (item) => {
        return _updateConfig(item, action);
      })),
    };
  };

  return self;
};


/**
 * AddConfigReducer 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddConfigReducer = () => {
  const self = {};
  self.type = 'addConfig';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateItem = (item) => {
      if (item.name === action.updateKey) {
        const newParam = {
          id: `${uuidv4()}`,
          name: '',
          type: 'String',
          from: 'Input',
          value: '',
        };
        return {
          ...item,
          value: [...item.value, newParam],
        };
      } else {
        return item;
      }
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'httpRequest', _updateItem)),
    };
  };

  return self;
};

/**
 * DeleteConfigReducer 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteConfigReducer = () => {
  const self = {};
  self.type = 'deleteConfig';

  const _updateItem = (item, action) => {
    if (item.name === action.updateKey) {
      return {
        ...item,
        value: item.value.filter(paramItem => paramItem.id !== action.id),
      };
    } else {
      return item;
    }
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'httpRequest', (item) => _updateItem(item, action))),
    };
  };

  return self;
};

/**
 * BodyTypeChangeReducer 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const BodyTypeChangeReducer = () => {
  const self = {};
  self.type = 'bodyTypeChange';

  const _updateBodyType = (item, action) => {
    if (item.name === 'activeBodyType') {
      return {
        ...item, value: action.value,
      };
    } else {
      return item;
    }
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'allBodyData', (item) => {
        return _updateBodyType(item, action);
      })),
    };
  };

  return self;
};

/**
 * DataChangeReducer 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DataChangeReducer = () => {
  const self = {};
  self.type = 'changeData';

  const _changeData = (item, action) => {
    if (item.id !== action.id) {
      return item;
    }
    return {
      ...item, value: action.value,
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
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'allBodyData', (item) => {
        return _changeData(item, action);
      })),
    };
  };

  return self;
};

/**
 * bodyParamChange 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const BodyParamChangeReducer = () => {
  const self = {};
  self.type = 'bodyParamChange';

  const _changeBodyParam = (item, action) => {
    if (item.name !== HTTP_BODY_TYPE.X_WWW_FORM_URLENCODED) {
      return item;
    }
    return {
      ...item, value: item.value.map(param => {
        if (param.id !== action.id) {
          return param;
        }
        return {...param, [action.property]: action.value};
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
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'allBodyData', (item) => {
        return _changeBodyParam(item, action);
      })),
    };
  };

  return self;
};

/**
 * addBodyParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddBodyParamReducer = () => {
  const self = {};
  self.type = 'addBodyParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateItem = (item) => {
      if (item.name === HTTP_BODY_TYPE.X_WWW_FORM_URLENCODED) {
        const newParam = {
          id: `${uuidv4()}`,
          name: '',
          type: 'String',
          from: 'Input',
          value: '',
        };
        return {...item, value: [...item.value, newParam]};
      } else {
        return item;
      }
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'allBodyData', _updateItem)),
    };
  };

  return self;
};

/**
 * deleteBodyParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteBodyParamReducer = () => {
  const self = {};
  self.type = 'deleteBodyParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateItem = (item) => {
      if (item.name !== HTTP_BODY_TYPE.X_WWW_FORM_URLENCODED) {
        return item;
      }
      return {...item, value: item.value.filter(v => v.id !== action.id)};
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'allBodyData', _updateItem)),
    };
  };

  return self;
};

/**
 * TabChangeReducer 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const TabChangeReducer = () => {
  const self = {};
  self.type = 'tabChange';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    newConfig.inputParams.find(item => item.name === 'activeKey').value = action.value;
    return newConfig;
  };

  return self;
};