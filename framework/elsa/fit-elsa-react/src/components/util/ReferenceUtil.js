/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 获取默认的引用数据.
 *
 * @param id 唯一标识.
 * @return {{}} 引用.
 */
export const getDefaultReference = (id) => {
    return {
        id: id,
        name: '',
        type: 'String',
        description: '',
        from: 'Reference',
        referenceNode: '',
        referenceId: '',
        referenceKey: '',
        value: [],
        editable: true,
    };
};

/**
 * 对Reference中Object结构进行递归调用。
 *
 * @param params 传入的数组。
 * @param parent 父元素。
 * @param action 具体递归操作方法。
 */
export const recursive = (params, parent, action) => {
    params.forEach(p => {
        if (p.type === 'Object') {
            recursive(p.value, p, action);
            action(p, parent);
        } else {
            action(p, parent);
        }
    });
};