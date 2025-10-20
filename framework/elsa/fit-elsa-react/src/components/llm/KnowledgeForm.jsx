/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse, Popover} from 'antd';
import {useDataContext, useDispatch} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import React, {useMemo} from 'react';
import {Trans, useTranslation} from 'react-i18next';
import {JadeReferenceMultiTreeSelect} from '@/components/common/JadeReferenceMultiTreeSelect.jsx';
import {QuestionCircleOutlined} from '@ant-design/icons';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';
import {DATA_TYPES, VIRTUAL_CONTEXT_NODE} from "@/common/Consts.js";

const {Panel} = Collapse;

/**
 * 大模型节点知识表单。
 *
 * @param knowledgeData 数据.
 * @param disabled 是否禁用.
 * @returns {JSX.Element} 大模型节点模型表单的DOM。
 */
const _KnowledgeForm = ({knowledgeData, disabled}) => {
  const dispatch = useDispatch();
  const data = useDataContext();
  const {t} = useTranslation();
  const arrayType = useMemo(() => (DATA_TYPES?.ARRAY ?? 'ARRAY').toUpperCase(), [DATA_TYPES?.ARRAY]);

  const content = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='llmKnowledgePopover' components={{p: <p/>}}/>
  </div>);

  const handleReferenceKeyChange = (e) => {
    e.forEach(value => {
      value.from = 'Reference';
    });
    dispatch({
      type: 'changeKnowledge',
      value: e,
    });
  };

  const handleReferenceValueChange = (id, referenceKey, value, type) => {
      dispatch({type: 'moveKnowledgeItem', id: id, updateParams: [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {key: 'type', value: type}]});
  };

  return (<JadeCollapse defaultActiveKey={['knowledgePanel']}>
    {<Panel
      key={'knowledgePanel'}
      header={
        <div
          className='panel-header'>
        <span
          className='jade-panel-header-font'>
          {t('knowledge')}
        </span>
          <Popover
            content={content}
            align={{offset: [0, 3]}}
            overlayClassName={'jade-custom-popover'}>
            <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
          </Popover>
        </div>
      }
      className='jade-panel'
    >
      <div className={'jade-custom-panel-content'}>
        <JadeReferenceMultiTreeSelect
          disabled={disabled}
          className='jade-select'
          width={'100%'}
          reference={knowledgeData}
          onReferencedValueChange={handleReferenceValueChange}
          onReferencedKeyChange={handleReferenceKeyChange}
          treeFilter={(node) => node.node.type !== VIRTUAL_CONTEXT_NODE.name && node.observableList.some(item => item.type === 'Array')}
          typeFilter={(o) => (o?.type ?? '').toUpperCase() === arrayType}
          moveOutReference={!data.tempReference || !data.tempReference[knowledgeData.id] ? [] : data.tempReference[knowledgeData.id]}
        />
      </div>
    </Panel>}
  </JadeCollapse>);
};

_KnowledgeForm.propTypes = {
  knowledgeData: PropTypes.object.isRequired, // 确保 knowledgeData 是一个必需的object类型
  disabled: PropTypes.bool, // 确保 modelOptions 是一个必需的array类型
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.knowledgeData === nextProps.knowledgeData &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.moveOutReference === nextProps.moveOutReference;
};

export const KnowledgeForm = React.memo(_KnowledgeForm, areEqual);