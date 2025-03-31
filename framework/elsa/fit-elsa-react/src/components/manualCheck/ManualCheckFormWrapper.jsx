/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {useDataContext, useDispatch} from '@/components/DefaultRoot.jsx';
import {ManualCheckForm} from '@/components/manualCheck/ManualCheckForm.jsx';
import JadeInputTreeCollapse from '@/components/common/JadeInputTreeCollapse.jsx';
import {JadeInputTree} from '@/components/common/JadeInputTree.jsx';
import React from 'react';
import {useTranslation} from 'react-i18next';
import {ManualCheckFormOutput} from '@/components/manualCheck/ManualCheckFormOutput.jsx';

/**
 * 人工检查表单Wrapper
 *
 * @param shapeStatus 图形状态
 * @returns {JSX.Element} 人工检查表单Wrapper的DOM
 */
const ManualCheckFormWrapper = ({shapeStatus}) => {
    const data = useDataContext();
    const dispatch = useDispatch();
    const {t} = useTranslation();

    const handleFormChange = (changeFormName, changeFormId, imgUrl, entity) => {
        dispatch({
            type: 'changeFormByMetaData',
            formName: changeFormName,
            formId: changeFormId,
            imgUrl: imgUrl,
            entity: entity,
        });
    };

    const handleFormDelete = (deleteFormId) => {
        dispatch({
            type: 'deleteForm', formId: deleteFormId,
        });
    };

    const updateFormInput = (id, changes) => {
        dispatch({type: 'update', id, changes});
    };

    /**
     * 组装表单对象。
     */
    const form = {
        name: data?.formName ?? undefined, id: data?.taskId ?? undefined, imgUrl: data?.imgUrl ?? undefined,
    };

    return (<>
        <div>
            <JadeInputTreeCollapse data={data.converter.entity.inputParams} disabled={shapeStatus.disabled}>
                <JadeInputTree shapeStatus={shapeStatus} data={data.converter.entity.inputParams} updateItem={updateFormInput}/>
            </JadeInputTreeCollapse>
            <ManualCheckForm
              form={form} data={data.converter.entity.outputParams} handleFormChange={handleFormChange} handleFormDelete={handleFormDelete}
              disabled={shapeStatus.disabled}/>
            <ManualCheckFormOutput outputItems={data.converter.entity.outputParams}/>
        </div>
    </>);
};

export default ManualCheckFormWrapper;