/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JadeObservableTree} from '@/components/common/JadeObservableTree.jsx';
import React from 'react';
import {Collapse, Popover} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import ArrayUtil from '@/components/util/ArrayUtil.js';
import {useTranslation} from 'react-i18next';
import {JadeTree} from '@/components/common/JadeTree.jsx';

const {Panel} = Collapse;

_InvokeOutput.propTypes = {
  outputData: PropTypes.array,
  getDescription: PropTypes.func,
  isObservableTree: PropTypes.bool,
};

/**
 * 获取输出数据中的描述信息
 *
 * @param outputData 输出数据
 * @param t 国际化
 * @return {JSX.Element|null} 描述信息div
 */
const getContent = (outputData, t) => {
  const contentItems = outputData
    .filter(item => item.description) // 过滤出有描述的项目
    .map((item) => (
      <p key={item.id}>{item.name}: {item.description}</p>
    ));

  if (contentItems.length === 0) {
    return null; // 如果没有内容，返回null
  }

  return (<>
    <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <p>{t('parameterDescription')}</p>
      {contentItems}
    </div>
  </>);
};

/**
 * fit接口出参展示
 *
 * @param outputData 输出数据
 * @param getDescription 获取输出描述的方法
 * @param isObservableTree 是否可被观察的树结构
 * @returns {JSX.Element}
 */
function _InvokeOutput({outputData, getDescription = getContent, isObservableTree = true}) {
  const {t} = useTranslation();
  const content = getDescription(outputData, t);

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={['InvokeOutput']}>
      <Panel
        className='jade-panel'
        header={<div style={{display: 'flex', alignItems: 'center'}}>
          <span className='jade-panel-header-font'>{t('output')}</span>
          {content ? (
            <Popover
              content={content}
              align={{offset: [0, 3]}}
              overlayClassName={'jade-custom-popover'}
            >
              <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
            </Popover>
          ) : null}
        </div>}
        key='InvokeOutput'>
        <div className={'jade-custom-panel-content'}>
          {isObservableTree ? <JadeObservableTree data={outputData}/> : <JadeTree data={outputData}/>}
        </div>
      </Panel>
    </Collapse>
  </>);
}

const areEqual = (prevProps, nextProps) => {
  return ArrayUtil.isEqual(prevProps.outputData, nextProps.outputData) && prevProps.isObservableTree === nextProps.isObservableTree;
};

export const InvokeOutput = React.memo(_InvokeOutput, areEqual);