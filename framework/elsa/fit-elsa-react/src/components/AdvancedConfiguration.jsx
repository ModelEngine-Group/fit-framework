/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useState} from 'react';
import {Button, Input, Modal, Popover, Switch} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';

const {TextArea} = Input;

/**
 * 高级配置.
 *
 * @param data 数据.
 * @param disabled 是否禁用.
 * @param onConfirm 当点击确认时的回调.
 * @param isAdvancedConfigurationOpen 组件是否需要打开.
 * @param setAdvancedConfigurationOpen 组件打开设置修改方法.
 * @return {JSX.Element}
 * @constructor
 */
export const AdvancedConfiguration = ({data,
                                        disabled,
                                        onConfirm,
                                        isAdvancedConfigurationOpen,
                                        setAdvancedConfigurationOpen}) => {
  const [stageData, setStageData] =
    useState({enableStageDesc: data.enableStageDesc, stageDesc: data.stageDesc});

  const onModalOk = () => {
    onConfirm(stageData);
    setAdvancedConfigurationOpen(false);
  };

  const onModalCancel = () => {
    setAdvancedConfigurationOpen(false);
  };

  const advancedConfigurationContent = (
    <>
      <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <p>说明：是否在节点运行前，打印过</p>
        <p>程信息，告知用户当前在做什么操</p>
        <p>作。如：数据库查询中...</p>
      </div>
    </>);

  const handleProcessInfoSwitchChange = (e) => {
    if (e === false) {
      setStageData({...stageData, enableStageDesc: e, stageDesc: ''});
    } else {
      setStageData({...stageData, enableStageDesc: e});
    }
  };

  const handleProcessInfoChange = (e) => {
    setStageData({...stageData, stageDesc: e.target.value});
  };

  return (<>
    <Modal
      className='jade-custom-modal' title='高级配置' open={isAdvancedConfigurationOpen} onCancel={onModalCancel}
      footer={[
        <div key='footer' style={{marginTop: '24px'}}>
          <Button onClick={onModalCancel}>取消</Button>
          <Button type='primary' style={{marginLeft: '8px'}} onClick={onModalOk}>确定</Button>
        </div>,
      ]}>
      <div style={{display: 'flex', alignItems: 'center', paddingBottom: '12px'}}>
        <span style={{fontSize: '12px'}}>是否打印过程信息</span>
        <Popover
          content={advancedConfigurationContent}
          align={{offset: [0, 3]}}
          overlayClassName={'jade-custom-popover'}
        >
          <QuestionCircleOutlined style={{color: 'rgb(77, 77, 77)'}} className='jade-panel-header-popover-content'/>
        </Popover>
        <Switch style={{marginLeft: '4px'}}
                onChange={(e) => handleProcessInfoSwitchChange(e)}
                checked={stageData?.enableStageDesc ?? false}></Switch>
      </div>
      {stageData.enableStageDesc && (
        <>
          <span style={{fontSize: '12px'}}>请输入过程信息</span>
          <TextArea disabled={disabled}
                    style={{borderColor: '#edeeef'}}
                    className='jade-font-size'
                    onChange={(e) => handleProcessInfoChange(e)}
                    value={stageData?.stageDesc ?? ''}
                    rows={4}
                    maxLength={100}
                    showCount
                    placeholder='如：数据库查询中...'/>
        </>
      )}
    </Modal>
  </>);
};

AdvancedConfiguration.propTypes = {
  data: PropTypes.object,
  disabled: PropTypes.bool,
  onConfirm: PropTypes.func,
  isAdvancedConfigurationOpen: PropTypes.bool,
  setAdvancedConfigurationOpen: PropTypes.func,
};
