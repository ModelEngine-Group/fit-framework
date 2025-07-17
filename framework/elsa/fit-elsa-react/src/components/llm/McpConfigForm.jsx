/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse} from 'antd';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import React, {useState} from 'react';
import {Prompt} from '@/components/common/prompt/Prompt.jsx';
import FullScreenIcon from '../asserts/icon-full-screen.svg?react';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

/**
 * 大模型节点MCP配置表单。
 *
 * @param shapeId 所属图形唯一标识。
 * @param modelData 数据.
 * @param disabled 是否禁用.
 * @returns {JSX.Element} 大模型节点模型表单的DOM。
 */
const _McpConfigForm = ({shapeId, modelData, disabled}) => {
  const dispatch = useDispatch();
  const {t} = useTranslation();
  const [configOpen, setConfigOpen] = useState(false);

  const JsonValidator = () => ({
    validator(_, value) {
      try {
        if (value) JSON.parse(value);
        return Promise.resolve();
      } catch (err) {
        return Promise.reject(t('pleaseEnterValidJson'));
      }
    },
  });

  const mcpServerConfigContent = (<div className={'jade-font-size'} style={{lineHeight: '1.2', whiteSpace: 'pre'}}>
    <Trans i18nKey='mcpServerConfigPopover' components={{p: <p/>}}/>
  </div>);

  return (<>
    <JadeCollapse defaultActiveKey={['mcpServerConfigPanel']}>
      {<Panel
        key={'mcpServerConfigPanel'}
        header={<div className='panel-header'>
          <span className='jade-panel-header-font'>{t('mcpServerConfig')}</span>
        </div>}
        className='jade-panel'
      >
        <div className={'jade-custom-panel-content'}>
          <Prompt
            prompt={{
              ...modelData.mcpServers,
              value: JSON.stringify(modelData.mcpServers.value, null, 2),
            }}
            rules={[{required: true, message: t('paramCannotBeEmpty')}, JsonValidator]}
            name={`mcpServers-${shapeId}`}
            tips={mcpServerConfigContent}
            onChange={(text) => {
              try {
                const newObj = JSON.parse(text); // 解析字符串
                dispatch({type: 'changeMcpServers', id: modelData.mcpServers.id, value: newObj});
              } catch (err) {
                // 不影响
              }
            }}
            buttonConfigs={[{
              icon: <FullScreenIcon/>, onClick: () => {
                setConfigOpen(true);
              },
            }]}
            disabled={disabled}
            open={configOpen}
            setOpen={setConfigOpen}/>
        </div>
      </Panel>}
    </JadeCollapse>
  </>);
};

_McpConfigForm.propTypes = {
  shapeId: PropTypes.string.isRequired, // 确保 shapeId 是一个必需的string类型
  modelData: PropTypes.object.isRequired, // 确保 modelData 是一个必需的object类型
  disabled: PropTypes.bool, // 确保 modelOptions 是一个必需的array类型
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.modelData.mcpServers === nextProps.modelData.mcpServers &&
    prevProps.disabled === nextProps.disabled;
};

export const McpConfigForm = React.memo(_McpConfigForm, areEqual);