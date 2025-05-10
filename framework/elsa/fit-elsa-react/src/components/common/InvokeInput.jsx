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

_InvokeInput.propTypes = {
    inputData: PropTypes.array,
    shapeStatus: PropTypes.object,
    parentId: PropTypes.string,
}

/**
 * fit接口入参展示和入参赋值
 *
 * @param inputData 需要渲染的输入数据.
 * @param shapeStatus 节点状态.
 * @param showRadio 是否需要展示Radio.
 * @param radioValue Radio对应的值.
 * @param radioTitle Radio对应的展示信息.
 * @param radioRuleMessage Radio没填时的报错信息.
 * @param parentId 输入入参上层所属id，非必填.
 * @returns {JSX.Element}
 */
function _InvokeInput({inputData, shapeStatus, showRadio = false, radioValue, radioTitle, radioRuleMessage, parentId}) {
    const dispatch = useDispatch();

    /**
     * 更新input.
     *
     * @param id 需要更新的值的id.
     * @param changes 需要改变的属性.
     */
    const updateItem = (id, changes) => {
        dispatch({type: 'update', id, changes, parentId});
    };

    /**
     * 更新Radio信息.
     *
     * @param paths Radio选中的值的列表.
     */
    const updateRadioInfo = (paths) => {
        dispatch({type: 'updateRadioInfo', paths});
    };

    return (<>
        <JadeInputTreeCollapse data={inputData} disabled={shapeStatus.disabled}>
            <JadeInputTree shapeStatus={shapeStatus} data={inputData} updateItem={updateItem} showRadio={showRadio} radioValue={radioValue}
                           radioTitle={radioTitle} updateRadioInfo={updateRadioInfo} radioRuleMessage={radioRuleMessage}/>
        </JadeInputTreeCollapse>
    </>);
}

const areEqual = (prevProps, nextProps) => {
    return prevProps.shapeStatus === nextProps.shapeStatus
        && prevProps.showRadio === nextProps.showRadio
        && ArrayUtil.isEqual(prevProps.inputData, nextProps.inputData);
};

export const InvokeInput = React.memo(_InvokeInput, areEqual);