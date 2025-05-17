/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse, Popover} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import React from 'react';
import {JadeObservableTree} from '@/components/common/JadeObservableTree.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

/**
 * 内容输出组件
 *
 * @returns {JSX.Element}
 * @constructor
 */
const _OutputForm = ({outputParams, outputPopover}) => {
  const {t} = useTranslation();

  const tips = <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey={outputPopover} components={{p: <p/>}}/>
  </div>;

  return (
    <JadeCollapse
      style={{marginTop: '10px', marginBottom: 8, borderRadius: '8px', width: '100%'}}
      defaultActiveKey={['Output']}>
      <Panel
        header={
          <div
            style={{display: 'flex', alignItems: 'center', paddingLeft: '-16px'}}>
            <span className='jade-panel-header-font'>{t('output')}</span>
            <Popover content={tips}>
              <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
            </Popover>
          </div>
        }
        className='jade-panel'
        key='Output'
        forceRender
      >
        <div className={'jade-custom-panel-content'}>
          <JadeObservableTree data={outputParams}/>
        </div>
      </Panel>
    </JadeCollapse>
  );
};

_OutputForm.propTypes = {
    outputParams: PropTypes.array.isRequired,
    outputPopover: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.outputParams === nextProps.outputParams && prevProps.outputPopover === nextProps.outputPopover;
};

export const OutputForm = React.memo(_OutputForm, areEqual);