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