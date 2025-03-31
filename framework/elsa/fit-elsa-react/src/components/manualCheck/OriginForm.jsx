/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import FormDefaultPreview from '../asserts/form-default-preview.svg?react';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';
import React from 'react';

/**
 * 人工检查节点原始表单
 *
 * @param text 需要显示的文字
 * @returns {JSX.Element} 人工检查节点原始表单的Dom
 */
const _OriginForm = ({text}) => {
    const {t} = useTranslation();

    return (<>
        <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
            <FormDefaultPreview style={{width: '64px', height: '64px'}}/>
            <div>{t(text)}</div>
        </div>
    </>);
};

_OriginForm.propTypes = {
    text: PropTypes.string,
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.text === nextProps.text;
};

export const OriginForm = React.memo(_OriginForm, areEqual);