/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import {Form, Popover} from 'antd';
import {JadeObservableInput} from "../common/JadeObservableInput.jsx";
import {Trans, useTranslation} from 'react-i18next';
import {QuestionCircleOutlined} from '@ant-design/icons';
import React from 'react';

/**
 * 开始节点关于入参的名称
 *
 * @param itemId 名称所属Item的唯一标识
 * @param propValue 名称的初始值
 * @param type 名称对应的字段类型
 * @param disableModifiable 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @param items 所有表单对象
 * @returns {JSX.Element} 开始节点关于入参名称的Dom
 */
export const Name = ({itemId, propValue, type, disableModifiable, onChange, items}) => {
    const { t } = useTranslation();

    const promptContent = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='startNodeNamePopover' components={{p: <p/>}}/>
    </div>);

    return (<Form.Item
        className="jade-form-item"
        label={<div className={'required-after'} style={{display: 'flex', alignItems: 'center'}}>
            <span>{t('fieldName')}</span>
            <Popover
              content={[promptContent]}
              align={{offset: [0, 3]}}
              overlayClassName={'jade-custom-popover'}
            >
                <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
            </Popover>
        </div>}
        name={`name-${itemId}`}
        rules={[{required: true, message: t('paramNameCannotBeEmpty')},
            {pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: t('fieldNameRule')},
            // 自定义校验函数
            () => ({
                validator(_, value) {
                    const otherValues = items.filter(i => i.id !== itemId).map(i => i.name);
                    if (otherValues.includes(value)) {
                        return Promise.reject(new Error(t('attributeNameMustBeUnique')));
                    }
                    return Promise.resolve();
                },
            })]}
        validateTrigger="onBlur"
        initialValue={propValue}
    >
        <JadeObservableInput
          className='jade-start-node-input'
            id={itemId}
            value={propValue}
            type={type}
            disabled={disableModifiable}
            placeholder={t('pleaseInsertFieldName')}
            showCount
            maxLength={20}
            onChange={e => onChange && onChange("name", e.target.value)} // 当输入框的值发生变化时调用父组件传递的回调函数
        />
    </Form.Item>);
};

Name.propTypes = {
    itemId: PropTypes.string.isRequired, // 确保 itemId 是一个必需的字符串
    propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的字符串
    type: PropTypes.string.isRequired, // 确保 type 是一个必需的字符串
    disableModifiable: PropTypes.bool.isRequired, // 确保 disableModifiable 是一个必需的bool值
    onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
};