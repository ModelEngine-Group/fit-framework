/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDispatch} from '@/components/DefaultRoot.jsx';
import ArrayUtil from '@/components/util/ArrayUtil.js';
import React from 'react';
import PropTypes from 'prop-types';
import JadeInputTreeCollapse from '@/components/common/JadeInputTreeCollapse.jsx';
import {JadeInputTree} from '@/components/common/JadeInputTree.jsx';

_EvaluationInvokeInput.propTypes = {
    inputData: PropTypes.array,
    shapeStatus: PropTypes.object
};

/**
 * 评估节点入参展示和入参赋值
 *
 * @param inputData 输入数据
 * @param shapeStatus 图形状态
 * @returns {JSX.Element}
 */
function _EvaluationInvokeInput({inputData, shapeStatus}) {
    const dispatch = useDispatch();

    /**
     * 更新input
     *
     * @param id 需要更新的值的id
     * @param changes 需要改变的属性
     */
    const updateItem = (id, changes) => {
        dispatch({type: 'update', id, changes});
    };

    /**
     * 返回评估节点input选项，所有类型不支持输入
     *
     * @return {[{label: string, value: string}]} 评估节点input选项
     */
    const getOptions = (node) => {
        switch (node.type) {
            case 'Object':
                if (Object.prototype.hasOwnProperty.call(node, 'generic') || node.props === undefined) {
                    return [{value: 'Reference', label: '引用'}];
                } else {
                    return [{value: 'Reference', label: '引用'},
                        {value: 'Expand', label: '展开'},
                    ];
                }
            case 'Array':
            default:
                return [{value: 'Reference', label: '引用'}];
        }
    };

    return (<>
        <JadeInputTreeCollapse data={inputData}>
            <JadeInputTree shapeStatus={shapeStatus} data={inputData} updateItem={updateItem} getOptions={getOptions}/>
        </JadeInputTreeCollapse>
    </>);
}

const areEqual = (prevProps, nextProps) => {
    return prevProps.disabled === nextProps.disabled && ArrayUtil.isEqual(prevProps.inputData, nextProps.inputData);
};

export const EvaluationInput = React.memo(_EvaluationInvokeInput, areEqual);