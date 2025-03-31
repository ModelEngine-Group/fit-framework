/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import {Button, Popover} from 'antd';
import {PlusOutlined, QuestionCircleOutlined} from '@ant-design/icons';
import React from 'react';
import {useTranslation} from 'react-i18next';

/**
 * panel的header.
 *
 * @param text 文本.
 * @param tips 提示.
 * @param shapeStatus 图形状态.
 * @param onClick 点击事件.
 * @param editable 是否可编辑.
 * @return {JSX.Element}
 * @constructor
 */
export const JadePanelHeader = ({text, tips, shapeStatus, onClick, editable = true}) => {
    const {t} = useTranslation();

    return (<>
        <div className='panel-header'>
            <span className='jade-panel-header-font'>{t(text)}</span>
            <Popover
              content={tips}
              align={{offset: [0, 3]}}
              overlayClassName={'jade-custom-popover'}
            >
                <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
            </Popover>
            {
                editable ? (<>
                    <Button disabled={shapeStatus.disabled}
                            type='text' className='icon-button jade-panel-header-icon-position'
                            onClick={(event) => {
                                onClick(event);
                                event.stopPropagation();
                            }}>
                        <PlusOutlined/>
                    </Button>
                </>) : null
            }
        </div>
    </>);
};

JadePanelHeader.propTypes = {
    text: PropTypes.string.isRequired,
    tips: PropTypes.object.isRequired,
    shapeStatus: PropTypes.object.isRequired,
    onClick: PropTypes.func,
    editable: PropTypes.bool,
};
