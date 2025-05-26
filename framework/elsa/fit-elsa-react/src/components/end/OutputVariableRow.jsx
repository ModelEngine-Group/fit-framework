/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Col, Form, Row} from 'antd';
import React from 'react';
import {JadeReferenceTreeSelect} from '@/components/common/JadeReferenceTreeSelect.jsx';
import {useFormContext} from '@/components/DefaultRoot.jsx';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import {DATA_TYPES} from '@/common/Consts.js';

/**
 * 输出变量的每个条目
 *
 * @param item 当前条目数据
 * @param handleItemChange 更改值的回调
 * @param shapeStatus 图形状态.
 * @returns {JSX.Element}
 * @constructor
 */
const _OutputVariableRow = ({item, handleItemChange, shapeStatus}) => {
    const inputName = `value-${item.id}`;
    const form = useFormContext();
    const {t} = useTranslation();

    const _onReferencedValueChange = (referenceKey, value, type) => {
        handleItemChange(item.id, [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {key: 'type', value: type}]);
    };

    const _onReferencedKeyChange = (e) => {
        handleItemChange(item.id, [{key: 'referenceNode', value: e.referenceNode},
            {key: 'referenceId', value: e.referenceId},
            {key: 'referenceKey', value: e.referenceKey},
            {key: 'value', value: e.value},
            {key: 'type', value: e.type}]);
    };

    /**
     * 更改输出变量的属性
     *
     * @return {(function(*): void)|*}
     */
    const editOutputVariable = (e) => {
        if (!e.target.value) {
            return;
        }
        handleItemChange(item.id, [{key: 'value', value: e.target.value}]);
    };

    /**
     * 通过字段类型选择渲染组件
     *
     * @param from 类型
     * @return {JSX.Element|null}
     */
    const renderByType = (from) => {
        switch (from) {
            case 'Reference':
                return (<>
                    <JadeReferenceTreeSelect
                        disabled={shapeStatus.referenceDisabled}
                        reference={item}
                        onReferencedValueChange={(referenceKey, value, type) => _onReferencedValueChange(referenceKey, value, type)}
                        onReferencedKeyChange={_onReferencedKeyChange}
                        style={{fontSize: '12px'}}
                        placeholder={t('pleaseSelect')}
                        showSearch
                        className='value-custom jade-select'
                        dropdownStyle={{
                            maxHeight: 400, overflow: 'auto'
                        }}
                        rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
                    />
                </>);
            case 'Input':
                return (<>
                    <Form.Item
                        style={{marginBottom: '8px'}}
                        id={`value-${item.id}`}
                        name={`value-${item.id}`}
                        rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
                        validateTrigger='onBlur'
                        initialValue={item.value}
                    >
                        <JadeInput disabled={shapeStatus.disabled}
                                   className='value-custom jade-input'
                                   style={{fontSize: '12px'}}
                                   placeholder={t('plsEnter')}
                                   onBlur={editOutputVariable}
                        />
                    </Form.Item>
                </>);
            default:
                return null;
        }
    };

    return (<>
        <Row key={`output-variable-${item.id}`} gutter={16}>
            <Col span={8} style={{display: 'flex', paddingTop: '5px'}}>
                <span className='end-starred-text'>finalOutput</span>
            </Col>
            <Col span={6} style={{paddingRight: 0}}>
                <Form.Item style={{marginBottom: '8px'}} id={`valueSource-${item.id}`} initialValue={item.from}>
                    <JadeStopPropagationSelect disabled={shapeStatus.disabled}
                            id={`valueSource-select-${item.id}`}
                            className={'value-source-custom jade-select'}
                            style={{width: '100%'}}
                            onChange={(value) => {
                                form.resetFields([`reference-${item.id}`, inputName]);
                                let changes = [
                                    {key: 'from', value: value},
                                    {key: 'value', value: ''},
                                ];
                                if (value === 'Input') {
                                    changes = [
                                        {key: 'from', value: value},
                                        {key: 'type', value: DATA_TYPES.STRING},
                                        {key: 'value', value: ''},
                                        {key: 'referenceNode', value: ''},
                                        {key: 'referenceId', value: ''},
                                        {key: 'referenceKey', value: ''},
                                    ];
                                }
                                handleItemChange(item.id, changes);
                            }}
                            options={[{value: 'Reference', label: t('reference')}, {value: 'Input', label: t('input')}]}
                            value={item.from}
                    />
                </Form.Item>
            </Col>
            <Col span={10} style={{paddingLeft: 0}}>
                {renderByType(item.from)}
            </Col>
        </Row>
    </>);
};

_OutputVariableRow.propTypes = {
    item: PropTypes.object.isRequired,
    handleItemChange: PropTypes.func.isRequired,
    shapeStatus: PropTypes.object
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.item === nextProps.item && prevProps.shapeStatus === nextProps.shapeStatus;
};

export const OutputVariableRow = React.memo(_OutputVariableRow, areEqual);