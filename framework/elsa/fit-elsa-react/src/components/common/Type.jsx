/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import {Form} from 'antd';
import {JadeStopPropagationSelect} from './JadeStopPropagationSelect.jsx';
import {useEffect} from 'react';
import {useFormContext} from '@/components/DefaultRoot.jsx';

Type.propTypes = {
    itemId: PropTypes.string.isRequired, // 确保 itemId 是一个必需的字符串
    propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的字符串
    disableModifiable: PropTypes.bool.isRequired, // 确保 disableModifiable 是一个必需的bool值
    onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
    labelName: PropTypes.string.isRequired, // 确保 labelName 是一个必需的字符串
    className: PropTypes.string, // 确保 className 是一个字符串
};

/**
 * 入参的类型
 *
 * @param itemId 类型所属Item的唯一标识
 * @param propValue 类型的初始值
 * @param disableModifiable 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @param labelName 类型对应显示的标签名
 * @param style 特殊的前端渲染参数
 * @returns {JSX.Element} 入参类型的Dom
 */
export default function Type({itemId, propValue, disableModifiable, onChange, labelName, className}) {
    const form = useFormContext();

    useEffect(() => {
        form.setFieldsValue({ [`type-${itemId}`]: propValue });
    }, [propValue, form]);

    const handleSelectClick = (event) => {
        event.stopPropagation(); // 阻止事件冒泡
    };

    return (<Form.Item
        className="jade-form-item"
        label={labelName}
        name={`type-${itemId}`}
        initialValue={propValue}
    >
        <JadeStopPropagationSelect
            className={`jade-select ${className}`}
            value={propValue}
            disabled={disableModifiable}
            style={{width: "100%"}}
            onClick={handleSelectClick} // 点击下拉框时阻止事件冒泡
            onChange={onChange}
            options={[{value: 'String', label: 'String'}, {value: 'Integer', label: 'Integer'},
                {value: 'Boolean', label: 'Boolean'}, {value: 'Number', label: 'Number'},]}
        />
    </Form.Item>);
}