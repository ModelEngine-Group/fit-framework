/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Checkbox, Form, Image, Popover} from 'antd';
import './style.css';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import {EyeOutlined, QuestionCircleOutlined} from '@ant-design/icons';
import chatScreenshot from './chat-screenshot.jpg';

/**
 * 开始节点关于入参是否在聊天页面可见。
 *
 * @param itemId 所属Item的唯一标识
 * @param propValue 初始值
 * @param disableModifiable 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @returns {JSX.Element} 开始节点关于入参是否在聊天页面可见的Dom。
 */
const Visible = ({itemId, propValue, disableModifiable, onChange}) => {
  const {t} = useTranslation();

  const content = (
    <div style={{display: 'flex', flexDirection: 'column', textAlign: 'left'}}>
      <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='visiblePopover' components={{p: <p/>}}/>
      </div>
      <span className={'jade-font-size'} style={{fontWeight: 700}}>{t('forExample')}</span>
      <div className={'jade-custom-image-container'}>
        <Image
          src={chatScreenshot} width={222} height={124.56} style={{borderRadius: '4px'}}
          preview={{
            mask: (
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  height: '100%',
                  backgroundColor: 'transparent',
                }}
              >
                <EyeOutlined style={{fontSize: '20px', color: '#000'}}/> {/* 自定义图标 */}
              </div>
            ),
          }}/>
      </div>
    </div>
  );

  return (<>
    <Form.Item className='jade-form-item' name={`required-${itemId}`}>
      <Checkbox checked={propValue} disabled={disableModifiable} onChange={e => onChange('isVisible', e.target.checked)}><span
        className={'jade-font-size'}>{t('displayInDialogConfiguration')}</span></Checkbox>
      <Popover
        content={content}
        align={{offset: [0, 3]}}
        overlayClassName={'jade-custom-popover'}
      >
        <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
      </Popover>
    </Form.Item>
  </>);
};

Visible.propTypes = {
  itemId: PropTypes.string.isRequired, // 确保 itemId 是一个必需的字符串
  propValue: PropTypes.bool, // 确保 propValue 是一个bool值
  disableModifiable: PropTypes.bool.isRequired, // 确保 disableModifiable 是一个必需的bool值
  onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
};

export default Visible;