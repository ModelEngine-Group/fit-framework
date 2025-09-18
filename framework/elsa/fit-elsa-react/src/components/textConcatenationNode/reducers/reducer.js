/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * changeTemplate 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeTemplateReducer = () => {
    const self = {};
    self.type = 'changeTemplate';

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
                    if (item.name === 'args') {
                        return {
                            ...item,
                            value: [
                                ...item.value,
                                {
                                    id: action.id,
                                    name: undefined,
                                    type: 'String',
                                    from: 'Reference',
                                    value: '',
                                }
                            ]
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
                    if (item.name === 'args') {
                        return {
                            ...item,
                            value: item.value.filter((item) => item.id !== action.id),
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
 * deleteInputParam 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const EditInputParamReducer = () => {
    const self = {};
    self.type = 'editInputParam';

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
                    if (item.name === 'args') {
                        return {
                            ...item,
                            value: item.value.map(inputItem => {
                                if (inputItem.id === action.id) {
                                    let updatedInputItem = {...inputItem};
                                    action.newValue.map((param) => {
                                        updatedInputItem[param.key] = param.value;
                                    });
                                    return updatedInputItem;
                                } else {
                                    return inputItem;
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
}