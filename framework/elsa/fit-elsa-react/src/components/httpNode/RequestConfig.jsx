/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Col, Collapse, Form, Row} from 'antd';
import {useDispatch, useFormContext} from '@/components/DefaultRoot.jsx';
import '../common/style.css';
import PropTypes from 'prop-types';
import React, {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import {HTTP_METHOD_TYPE} from '@/common/Consts.js';
import {AuthenticationButton} from '@/components/httpNode/AuthenticationButton.jsx';
import {TimeoutInput} from '@/components/httpNode/TimeOutInput.jsx';
import {AuthenticationModal} from '@/components/httpNode/AuthenticationModal.jsx';

const {Panel} = Collapse;

/**
 * http节点请求配置模块
 *
 * @param timeout 超时时间
 * @param authentication 认证信息
 * @param httpMethod http方法
 * @param url url
 * @param disabled 是否禁用
 * @returns {Element}
 * @private
 */
const _RequestConfig = ({timeout, authentication, httpMethod, url, disabled}) => {
  const dispatch = useDispatch();
  const form = useFormContext();
  const {t} = useTranslation();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleCancel = () => setIsModalOpen(false);
  const handleConfirm = (action) => {
    setIsModalOpen(false);
    dispatch(action);
  };

  /**
   * 输入失焦后的回调
   *
   * @param event 事件
   * @param actionType 事件类型
   * @param id 条目id
   */
  const onBlur = (event, actionType, id) => {
    let originValue = parseFloat(event.target.value); // 将输入值转换为浮点数
    // 如果转换后的值不是数字（NaN），则将其设为 1
    if (isNaN(originValue) || originValue <= 1.0) {
      originValue = 1.0;
    }
    if (originValue >= 600.0) {
      originValue = 600.0;
    }
    form.setFieldValue(`timeout-${timeout}`, originValue);
    dispatch({actionType: actionType, id: id, value: originValue * 1000});
  };

  /**
   * 点击鉴权按钮后的回到
   *
   * @param event 事件
   */
  const onAuthenticationClick = (event) => {
    setIsModalOpen(true);
    event.stopPropagation();
  };

  return (
    <Collapse bordered={false} className='jade-custom-collapse' defaultActiveKey={['httpConfigPanel']}>
      <Panel key={'httpConfigPanel'} header={<RequestConfigHeader
        onAuthenticationClick={onAuthenticationClick} t={t} disabled={disabled} onBlur={onBlur}
        timeout={timeout}/>}>
        <RequestUrlConfig disabled={disabled} dispatch={dispatch} t={t} httpMethod={httpMethod} url={url}/>
      </Panel>

      {/* 用Drawer替换Modal */}
      {isModalOpen && (<AuthenticationModal
          open={isModalOpen} // 控制Drawer的显示
          onCancel={handleCancel}
          onConfirm={handleConfirm}
          authentication={authentication}
        />
      )}
    </Collapse>
  );
};

_RequestConfig.propTypes = {
  timeout: PropTypes.object.isRequired, // 确保 toolOptions 是一个必需的array类型
  authentication: PropTypes.object.isRequired, // 确保 toolOptions 是一个必需的array类型
  httpMethod: PropTypes.object.isRequired, // 确保 toolOptions 是一个必需的array类型
  url: PropTypes.object.isRequired, // 确保 toolOptions 是一个必需的array类型
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.timeout === nextProps.timeout &&
    prevProps.authentication === nextProps.authentication &&
    prevProps.httpMethod === nextProps.httpMethod &&
    prevProps.url === nextProps.url &&
    prevProps.disabled === nextProps.disabled;
};

/**
 * 请求配置标题组件
 *
 * @param onAuthenticationClick 点击认证按钮的回调
 * @param t 国际化方法
 * @param disabled 是否禁用
 * @param timeout 超时时长
 * @param onBlur 输入框失焦后的回调
 * @returns {React.JSX.Element}
 * @constructor
 */
const RequestConfigHeader = ({onAuthenticationClick, t, disabled, timeout, onBlur}) => (
  <div className='panel-header' style={{display: 'flex', alignItems: 'center'}}>
    <span className='jade-panel-header-font'>{t('requestConfig')}</span>
    <div style={{flexGrow: 1}}></div>
    <AuthenticationButton disabled={disabled} onClick={onAuthenticationClick}/>
    <div
      style={{width: '2px', height: '12px', flex: 'none', marginRight: '16px', background: 'rgb(217, 217, 217)'}}></div>
    <TimeoutInput timeout={timeout} disabled={disabled} onBlur={onBlur}/>
  </div>
);

RequestConfigHeader.propTypes = {
  timeout: PropTypes.object.isRequired, // 确保 timeout 是一个必需的object类型
  onBlur: PropTypes.func.isRequired, // 确保 onBlur 是一个必需的func类型
  onAuthenticationClick: PropTypes.func.isRequired, // 确保 onAuthenticationClick 是一个必需的func类型
  t: PropTypes.func.isRequired, // 确保 t 是一个必需的func类型
  disabled: PropTypes.bool.isRequired,
};


/**
 * http请求url设置组件
 *
 * @param httpMethod http方法
 * @param url url
 * @param disabled 是否禁用
 * @param dispatch dispatch方法
 * @param t 国际化组件
 * @returns {React.JSX.Element}
 * @constructor
 */
export const RequestUrlConfig = ({httpMethod, url, disabled, dispatch, t}) => {
  const form = useFormContext();
  const options = Object.values(HTTP_METHOD_TYPE).map(dataType => ({
    value: dataType, label: dataType,
  }));

  /**
   * 请求方式改变时的处理函数
   *
   * @param value 函数类型
   * @param id 条目对应id
   */
  const handleMethodChange = (value, id) => {
    dispatch({actionType: 'changeRequestConfig', id: id, value: value});
  };

  /**
   * url改变的处理函数
   *
   * @param e 事件
   * @param id 条目id
   */
  const handleUrlChange = (e, id) => {
    dispatch({actionType: 'changeRequestUrl', id: id, value: e.target.value});
    form.setFieldValue(`http-method-url-${url.id}`, e.target.value.split('?')[0]);
  };

  return (<>
    <div className={'request-url-config-wrapper'}>
      <Row>
        <Col>
          <span className='http-config-request-mode-title jade-font-color'>{t('requestMode')}</span>
        </Col>
      </Row>
      <Row gutter={16} style={{width: '424px'}}>
        <Col style={{paddingRight: 0}}>
          <Form.Item
            id={`http-form-method-type-${httpMethod.id}`}
            name={`http-form-method-type-${httpMethod.id}`}
            rules={[{required: true}]} // 设置必选项
            initialValue={httpMethod.value}
          >
            <JadeStopPropagationSelect
              disabled={disabled}
              className={'http-method-select jade-select'}
              id={`http-form-method-type-${httpMethod.id}`}
              onChange={(e) => handleMethodChange(e, httpMethod.id)}
              options={options}
            />
          </Form.Item>
        </Col>

        <Col style={{paddingLeft: 0}}>
          <Form.Item
            id={`http-method-url-${url.id}`}
            name={`http-method-url-${url.id}`}
            rules={[{required: true, message: t('pleaseInputRequestUrl')}]} // 设置必填项
            initialValue={url.value}
            validateTrigger='onBlur'
          >
            <JadeInput
              disabled={disabled}
              className={'http-method-url-input jade-input'}
              onChange={(e) => handleUrlChange(e, url.id)}
              placeholder={t('pleaseInputRequestUrl')}
            />
          </Form.Item>
        </Col>
      </Row>
    </div>
  </>);
};

RequestUrlConfig.propTypes = {
  httpMethod: PropTypes.object.isRequired, // 确保 toolOptions 是一个必需的array类型
  url: PropTypes.object.isRequired, // 确保 toolOptions 是一个必需的array类型
  disabled: PropTypes.bool.isRequired,
  dispatch: PropTypes.func.isRequired,
  t: PropTypes.func.isRequired,
};

export const RequestConfig = React.memo(_RequestConfig, areEqual);