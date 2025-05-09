/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {shapeFields} from "./shapeFields.js";
import {MODE_MANAGER} from "../common/mode/modeManager.js";

/**
 * 确保一个类型一个fields常量，避免每个shape都有一个serializedFields列表
 * huizi 2022
 */
const getSerializedFields = (() => {
    const types = {};
    return (shape, type) => {
        let fields = types[type];
        if (fields === undefined) {
            if (shape.typeChain.parent) {
                fields = new Set(types[shape.typeChain.parent.type]);
            } else {
                fields = new Set();
            }
            const myFields = shapeFields.get(type)
            if (myFields.delete) {
                if (myFields.delete.length > 0) {
                    fields.batchDelete.apply(fields, myFields.delete);
                } else {
                    fields.clear();
                }
            }
            fields.batchAdd.apply(fields, myFields);
            types[type] = fields;
        }
        return fields;
    }
})();

/*
elsa中所有shape类的基类
主要添加了属性侦听功能
所有子类可以通过addDetection的方式侦听属性变化，这在shape里对页面响应有很大的帮助
辉子 2020-01-15
*/
class Atom {
    constructor(detections = []) {
        let defaultDetections = [];
        defaultDetections.push({
            props: new Set(["type"]), react: function (property, value, preValue, target) {
                let parentChain = target.typeChain;
                target.typeChain = {
                    parent: parentChain, type: value
                };
                (!target._data) && (target._data = {});
                target.serializedFields = getSerializedFields(target, (target.displayType ? target.displayType : value));

                // 兜底方法，如果在设置field为serializedFields时，target中已经存在该属性的值，那么将该值赋值给序列化的属性
                // 同时，删除target中的值;若target中本身就不存在该属性，那么不需要做任何处理.
                const batchAdd = target.serializedFields.batchAdd;
                target.serializedFields.batchAdd = (...fields) => {
                    batchAdd.apply(target.serializedFields, fields);
                    fields.forEach(field => {
                        const value = target[field];
                        if (typeof value === "function") {
                            throw new Error("Function field[" + field + "] can't serialize.");
                        }
                        if (value) {
                            target._data[field] = value;
                            delete target[field];
                        }
                    });
                };
            }
        });
        let reactDetection = (detections, propKey, value, preValue, target) => {
            detections.forEach(detection => {
                if (detection.props.has(propKey)) {
                    detection.react(propKey, value, preValue, target);
                }
            });
        };

        /**
        * 获取属性
        * 先取得自身该属性，如果没有集成page该属性，如果再没有，集成graph该属性
        * 辉子 2021
        */
        const proxy = new Proxy({}, {
            set: function (target, propKey, value, receiver) {
                /* ---------------------------- ModeManager override methods begin ---------------------------- */
                if (value instanceof Function) {
                    return handleFunctionSet(target, propKey, receiver, proxy, value);
                }

                if (propKey === "type") {
                    handleTypeSet(target, value, proxy);
                }

                /* ---------------------------- ModeManager override methods end ---------------------------- */

                let preValue = target[propKey];
                let set = target.set ? target.set(propKey, value) : undefined;
                if (preValue === undefined) {
                    preValue = (target._data ? target._data[propKey] : undefined);
                }
                reactDetection(defaultDetections, propKey, value, preValue, target);
                if (target.serializedFields && target.serializedFields.has(propKey)) {
                    target._data[propKey] = set ? set : value;
                    delete target[propKey];
                } else {
                    target[propKey] = set ? set : value;
                }
                reactDetection(detections, propKey, value, preValue, target);
                const isValidKey = propKey !== "inReacting" &&
                    propKey !== "propertyChanged" &&
                    propKey !== "changeIgnored";
                if (!target.changeIgnored && target.propertyChanged && isValidKey) {
                    target.propertyChanged(propKey, value, preValue);
                }
                return true;
            },
            get: function (target, propKey, receiver) {
                if (target.serializedFields && target.serializedFields.has(propKey)) {
                    return target.get ? target.get(propKey) : target._data[propKey];
                }
                const value = target[propKey];
              if (value === undefined) {
                return undefined;
              }
              if (typeof value !== 'function') {
                return target.propertyGet(target, propKey);
              }
              let currentMode = target.page.mode;
              let chain = target.typeChain;
              while (chain != null) {
                let forbiddenMethods = MODE_MANAGER[currentMode].forbiddenMethods[chain.type];
                if (forbiddenMethods && forbiddenMethods.indexOf(propKey) !== -1) {
                  return () => {
                  };
                }
                chain = chain.parent;
              }
              return value;
            }
        });

        proxy.propertyGet = (target, property) => {
            return target[property];
        }

        proxy.ignoreChange = (ops) => {
            proxy.changeIgnored = true;
            ops();
            delete proxy.changeIgnored;
        };

        proxy.addDetection = (props, react) => {
            detections.push({
                props: new Set(props), react: function (property, value, preValue) {
                    if (proxy.page === undefined || proxy.page.disableReact) {
                        return;
                    }
                    if (proxy.inReacting) {
                        return;
                    }
                    proxy.inReacting = true;
                    try {
                        react(property, value, preValue);
                    } catch (e) {
                        // 没关系，继续，不影响其他错误信息的处理.
                    } finally {
                        proxy.inReacting = false;
                    }
                }
            });
        };

        proxy.propertyChanged = (property, value, preValue) => {
        };

        return proxy;
    };
}

/**
 * 处理set方法中的function处理逻辑.
 *
 * @param target 目标对象.
 * @param propKey 设置的属性的key.
 * @param receiver 接收对象.
 * @param proxy 当前对象.
 * @param value 设置的值.
 * @return {boolean} true/false.
 */
const handleFunctionSet = (target, propKey, receiver, proxy, value) => {
    if (target.typeChain && target.typeChain.type) {
        let overrideMethods = MODE_MANAGER[target.page.mode].overrideMethods[target.typeChain.type];
        if (overrideMethods && overrideMethods[propKey]) {
            // 如果是shape，由于是基类，不会触发step2的override行为，因此这里需要对shape做特殊处理，进行方法的重写.
            // 这里target无法获取到type，receiver可以获取到type.
            if (receiver.type === "shape") {
                target[propKey] = (...args) => {
                    return overrideMethods[propKey](proxy, ...args);
                }
            }
            return true;
        }
    }
    target[propKey] = value;
    return true;
};

const handleTypeSet = (target, value, proxy) => {
    /**
     * step2 当设置type时，一次性将modeManager中的所有overrideMethods全部写入到target对象中.
     * 如果是shape基类，则无法获取到page，具体查看{@see shape}。page的设置在type的设置之后.
     */
    if (target.page && target.page.mode) {
        const overrideMethods = MODE_MANAGER[target.page.mode].overrideMethods[value];
        overrideMethods && Object.keys(overrideMethods).forEach(k => {
            target[k] = (...args) => {
                return overrideMethods[k](proxy, ...args);
            };
        });
    }
};

export { Atom };
