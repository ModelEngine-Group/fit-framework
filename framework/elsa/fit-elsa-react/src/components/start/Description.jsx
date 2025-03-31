/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Input, Form} from "antd";
import PropTypes from "prop-types";
import {useTranslation} from "react-i18next";

const {TextArea} = Input;

Description.propTypes = {
    propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的字符串
    disableModifiable: PropTypes.bool.isRequired, // 确保 disableModifiable 是一个必需的bool值
    onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
};

/**
 * 开始节点关于入参的描述
 *
 * @param itemId 名称所属Item的唯一标识
 * @param propValue 描述的初始值
 * @param disableModifiable 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @returns {JSX.Element} 开始节点关于入参描述的Dom
 */
export default function Description({itemId, propValue, disableModifiable, onChange}) {
    const { t } = useTranslation();
    return (<Form.Item
        className="jade-form-item"
        label={t('fieldDescription')}
        name={`description-${itemId}`}
        rules={[{required: true, message: t('paramDescriptionCannotBeEmpty')}]}
        initialValue={propValue}
    >
        <TextArea
            className="jade-textarea-input jade-font-size"
            maxLength={10000}
            onMouseDown={(e) => e.stopPropagation()}
            value={propValue}
            disabled={disableModifiable}
            onChange={e => onChange("description", e.target.value)} // 当文本输入框的值发生变化时调用父组件传递的回调函数
            placeholder={t('pleaseInsertFieldDescription')}
        />
    </Form.Item>);
}