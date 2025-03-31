/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse, Switch} from 'antd';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import React, {useEffect, useState} from 'react';
import {MemoryConfig} from '@/components/queryOptimization/MemoryConfig.jsx';

const {Panel} = Collapse;

/**
 * 多轮对话组件
 *
 * @param dispatch 对应的样 式类名.
 * @param memorySwitch 历史记录开关
 * @param memoryConfig 历史记录配置
 * @param disabled 是否禁用.
 * @returns {JSX.Element} 多轮对话组件的Dom
 */
const _HistoryConfig = ({
                          dispatch,
                          memorySwitch,
                          memoryConfig,
                          disabled = false,
                        }) => {
  const {t} = useTranslation();
  const [activeKey, setActiveKey] = useState(memorySwitch.value ? ['historyConfigPanel'] : []);

  /**
   * 历史记录消费方式options
   *
   * @type {[{label: string, value: string},{label: string, value: string}]}
   */
  const historyOption = [{label: t('byConversation'), value: 'full'}];

  useEffect(() => {
    // 监听 memorySwitch.value 的变化，并更新 activeKey
    setActiveKey(memorySwitch.value ? ['historyConfigPanel'] : []);
  }, [memorySwitch.value]);

  const handleCollapseChange = (key) => {
    // 如果用户点击折叠区域，更新 activeKey
    setActiveKey(key);
  };

  /**
   * 更改switch状态
   *
   * @param e event
   */
  const onSwitchChange = e => {
    dispatch({actionType: 'changeMemorySwitch', value: e});
  };

  return (<>
      <div className={'jade-multi-conversation'}>
        <Collapse bordered={false} className='jade-custom-collapse'
                  activeKey={activeKey}
                  onChange={handleCollapseChange}>
          {<Panel
            key={'historyConfigPanel'}
            header={<div className='panel-header'>
              <span className='jade-panel-header-font'>{t('historyRecord')}</span>
              <Switch
                checked={memorySwitch.value}
                disabled={disabled}
                onClick={(value, event) => event.stopPropagation()}
                onChange={e => onSwitchChange(e)}
              />
            </div>}
            style={{width: '100%'}}
          >
            <div className={'jade-custom-panel-content'}>
              <MemoryConfig disabled={disabled || !memorySwitch.value} memoryConfig={memoryConfig}
                            templateType={'builtin'}
                            isShowUseMemoryType={true} historyOption={historyOption}/>
            </div>
          </Panel>}
        </Collapse>
      </div>
    </>
  );
};

_HistoryConfig.propTypes = {
  disabled: PropTypes.bool,
  dispatch: PropTypes.func.isRequired,
  memorySwitch: PropTypes.object.isRequired,
  memoryConfig: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.dispatch === nextProps.dispatch &&
    prevProps.memorySwitch === nextProps.memorySwitch &&
    prevProps.memoryConfig === nextProps.memoryConfig;
};

export const HistoryConfig = React.memo(_HistoryConfig, areEqual);