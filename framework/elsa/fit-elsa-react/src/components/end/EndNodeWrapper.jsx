/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {OutputVariable} from '@/components/end/OutputVariable.jsx';
import {ManualCheckForm} from '@/components/manualCheck/ManualCheckForm.jsx';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {EndInputForm} from '@/components/end/EndInputForm.jsx';
import {SelectMode} from '@/components/end/SelectMode.jsx';
import {END_NODE_TYPE, FLOW_TYPE} from '@/common/Consts.js';
import JadeInputTreeCollapse from '@/components/common/JadeInputTreeCollapse.jsx';
import {JadeInputTree} from '@/components/common/JadeInputTree.jsx';
import React from 'react';
import {getEndNodeType} from '@/components/end/endNodeUtils.js';

/**
 * 用来封装结束节点子组件的最顶层组件
 *
 * @param data 数据
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element}
 * @constructor
 */
export const EndNodeWrapper = ({data, shapeStatus}) => {
    const dispatch = useDispatch();
    const mode = getEndNodeType(data.inputParams);
    const inputParams = data && data.inputParams;
    const shape = useShapeContext();

    /**
     * 表单更改后的回调
     *
     * @param changeFormName 表单名
     * @param changeFormId 表单id
     * @param imgUrl 图片地址
     * @param entity 表单实体
     */
    const handleFormChange = (changeFormName, changeFormId, imgUrl, entity) => {
        dispatch({
            type: 'changeFormByMetaData',
            formName: changeFormName,
            formId: changeFormId,
            formImgUrl: imgUrl,
            entity: entity,
        });
    };

    /**
     * 表单删除后的回调
     *
     * @param deleteFormId 需要删除的表单id
     */
    const handleFormDelete = (deleteFormId) => {
        dispatch({
            type: 'deleteForm',
            formId: deleteFormId,
        });
    };

    const updateFormInput = (id, changes) => {
        dispatch({type: 'update', id, changes});
    };

    /**
     * 组装表单对象。
     */
    const form = {
        name: inputParams.find(item => item.name === 'endFormName')?.value ?? undefined,
        id: inputParams.find(item => item.name === 'endFormId')?.value ?? undefined,
        imgUrl: inputParams.find(item => item.name === 'endFormImgUrl')?.value ?? undefined,
    };

    /**
     * 通过模式渲染结束节点
     *
     * @param mode
     * @return {JSX.Element}
     */
    const renderByMode = (mode) => {
        if (mode === END_NODE_TYPE.VARIABLES) {
            if (shape.graph.flowType === FLOW_TYPE.APP) {
                return <EndInputForm inputParams={inputParams} shapeStatus={shapeStatus}/>;
            }
            return (<OutputVariable inputParams={inputParams} shapeStatus={shapeStatus}/>);
        } else {
            return (<>
                  <JadeInputTreeCollapse data={inputParams} disabled={shapeStatus.disabled}>
                      <JadeInputTree shapeStatus={shapeStatus} data={inputParams} updateItem={updateFormInput}/>
                  </JadeInputTreeCollapse>
                  <ManualCheckForm
                    form={form}
                    handleFormChange={handleFormChange}
                    handleFormDelete={handleFormDelete}
                    disabled={shapeStatus.disabled}
                  />
              </>
            );
        }
    };

    return (<>
        <div style={{backgroundColor: 'white'}}>
            <SelectMode mode={mode} disabled={shapeStatus.disabled}/>
            {renderByMode(mode)}
        </div>
    </>);
};

EndNodeWrapper.propTypes = {
    data: PropTypes.object.isRequired,
    shapeStatus: PropTypes.object
};
