/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Typography, Form} from 'antd';
import PropTypes from 'prop-types';

const {Text} = Typography;

/**
 * 表单文本展示.
 *
 * @param id 唯一标识.
 * @param title 标题.
 * @param required 需要必须.
 * @param maxWidth 最大宽度.
 * @param toolTip 提示.
 * @return {JSX.Element}
 * @constructor
 */
export const JadeFormText = ({id, title, required = false, maxWidth = null, toolTip = null}) => {
    const _maxWidth = maxWidth ? maxWidth : '100%';

    const getClassName = () =>{
        return required ? 'huggingface-light-font jade-red-asterisk-tex' : 'huggingface-light-font';
    };

    return (<>
        <Form.Item name={`property-${id}`}>
            <div className='jade-input-tree-title-child' style={{display: 'flex', alignItems: 'center'}}>
                <Text ellipsis={{tooltip: toolTip}}
                      className={getClassName()}
                      style={{maxWidth: _maxWidth}}>{title}</Text>
            </div>
        </Form.Item>
    </>);
};

JadeFormText.propTypes = {
    id: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    required: PropTypes.bool,
    maxWidth: PropTypes.number,
    toolTip: PropTypes.object,
};
