/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Form, Checkbox} from 'antd';
import "./style.css";
import PropTypes from "prop-types";
import {useTranslation} from "react-i18next";

Required.propTypes = {
    itemId: PropTypes.string.isRequired, // 确保 itemId 是一个必需的字符串
    propValue: PropTypes.bool, // 确保 propValue 是一个bool值
    disableModifiable: PropTypes.bool.isRequired, // 确保 disableModifiable 是一个必需的bool值
    onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
};

/**
 * 开始节点关于入参是否必填。
 *
 * @param itemId 名称所属Item的唯一标识
 * @param propValue 描述的初始值
 * @param disableModifiable 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @returns {JSX.Element} 开始节点关于入参是否必填的Dom。
 */
export default function Required({itemId, propValue, disableModifiable, onChange}) {
    const { t } = useTranslation();
    return (<>
        <Form.Item className='jade-form-item' name={`required-${itemId}`}>
            <Checkbox checked={propValue} disabled={disableModifiable} onChange={e => onChange('isRequired', e.target.checked)}><span
              className={'jade-font-size'}>{t('requiredOrNot')}</span></Checkbox>
        </Form.Item>
    </>);
}