/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JadeObservableTree} from '@/components/common/JadeObservableTree.jsx';
import {Collapse} from 'antd';
import React from 'react';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';

const {Panel} = Collapse;

/**
 * 人工检查节点输出表单。
 *
 * @param outputItems 出参.
 * @returns {JSX.Element} 大模型节点输出表单的DOM。
 */
const _ManualCheckFormOutput = ({outputItems}) => {
  const {t} = useTranslation();

  return (<Collapse bordered={false} className='jade-custom-collapse'
                    defaultActiveKey={['InvokeOutput']}>
    <Panel
      className='jade-panel'
      header={<div style={{display: 'flex', alignItems: 'center'}}>
        <span className='jade-panel-header-font'>{t('output')}</span>
      </div>}
      key='InvokeOutput'>
      <div className={'jade-custom-panel-content'}>
        <JadeObservableTree data={outputItems}/>
      </div>
    </Panel>
  </Collapse>);
};

_ManualCheckFormOutput.propTypes = {
  outputItems: PropTypes.array.isRequired,
};

// 对象不变，不刷新组件.
const areEqual = (prevProps, nextProps) => {
  return prevProps.outputItems === nextProps.outputItems;
};

export const ManualCheckFormOutput = React.memo(_ManualCheckFormOutput, areEqual);