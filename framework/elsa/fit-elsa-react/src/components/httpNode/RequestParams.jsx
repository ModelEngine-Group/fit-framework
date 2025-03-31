/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse, Popover, Tabs} from 'antd';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import '../common/style.css';
import PropTypes from 'prop-types';
import React from 'react';
import {Trans, useTranslation} from 'react-i18next';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {ParamList} from '@/components/httpNode/ParamList.jsx';
import {BodyEditor} from '@/components/httpNode/BodyEditor.jsx';
import {BodyTypeSelector} from '@/components/httpNode/BodyTypeSelector.jsx';
import {QuestionCircleOutlined} from '@ant-design/icons';
import {HTTP_METHOD_TYPE} from '@/common/Consts.js';

const {TabPane} = Tabs;
const {Panel} = Collapse;

/**
 * http节点请求配置模块。
 *
 * @param disabled 是否禁用
 * @param params 参数
 * @param headers 请求头
 * @param requestBody 请求体
 * @param activeKey 当前选用的是哪个body类型
 * @param httpMethod http方法类型
 * @returns {Element}
 * @private
 */
const _RequestParams = ({disabled, params, headers, requestBody, activeKey, httpMethod}) => {
  const dispatch = useDispatch();
  const type = getConfigValue(requestBody, ['activeBodyType'], 'value');
  // data根据type，可能是多种类型
  const json = getConfigValue(requestBody, ['json'], '');
  const text = getConfigValue(requestBody, ['text'], '');
  const urlencoded = getConfigValue(requestBody, ['x-www-form-urlencoded'], '');
  const {t} = useTranslation();

  /**
   * 切换requestBody类型
   *
   * @param e event事件
   */
  const handleBodyTypeChange = (e) => {
    dispatch({actionType: 'bodyTypeChange', value: e.target.value});
  };

  /**
   * 参数属性修改
   *
   * @param id 条目id
   * @param property 属性名
   * @param value 输入框的值值
   */
  const onParamChange = (id, property, value) => {
    dispatch({actionType: 'configChange', id: id, updateKey: 'params', value, property});
  };

  /**
   * 删除一条数据
   *
   * @param key 条目id
   */
  const handleDeleteParam = key => {
    dispatch({actionType: 'deleteConfig', id: key, updateKey: 'params'});
  };

  /**
   * 添加参数
   *
   * @param event 事件
   */
  const addParam = event => {
    dispatch({actionType: 'addConfig', updateKey: 'params'});
  };

  /**
   * 参数属性修改
   *
   * @param id 条目id
   * @param property 属性名
   * @param value 输入框的值值
   */
  const onHeaderChange = (id, property, value) => {
    dispatch({actionType: 'configChange', id: id, updateKey: 'headers', value, property});
  };

  /**
   * 删除一条数据
   *
   * @param key 条目id
   */
  const handleDeleteHeader = key => {
    dispatch({actionType: 'deleteConfig', id: key, updateKey: 'headers'});
  };

  /**
   * 添加header
   *
   * @param event 事件
   */
  const addHeader = event => {
    dispatch({actionType: 'addConfig', updateKey: 'headers'});
  };

  /**
   * 切换tabs的回调
   *
   * @param activeTabKey 当前tabs的key
   */
  const handleTabChange = (activeTabKey) => {
    dispatch({actionType: 'tabChange', value: activeTabKey});
  };

  const content = <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='requestParamsTips' components={{p: <p/>}}/>
  </div>;

  return (
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={['requestParamsPanel']}>
      {
        <Panel
          key={'requestParamsPanel'}
          header={
            <div className='panel-header' style={{display: 'flex', alignItems: 'center'}}>
              <span className='jade-panel-header-font'>{t('requestParams')}</span>
              <Popover content={content}>
                <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
              </Popover>
            </div>
          }
          className='jade-panel'
        >
          <div className={'jade-custom-panel-content'}>
            <Tabs defaultActiveKey={activeKey} onChange={(key) => handleTabChange(key)}>
              <TabPane tab='Params' key='1'>
                <ParamList
                  params={params}
                  disabled={disabled}
                  onParamChange={onParamChange}
                  handleDelete={handleDeleteParam}
                  addParam={addParam}
                />
              </TabPane>

              {httpMethod.value !== HTTP_METHOD_TYPE.GET && (
                <TabPane tab='Body' key='2'>
                  <BodyTypeSelector onBodyTypeChange={handleBodyTypeChange} disabled={disabled} type={type}/>
                  <BodyEditor
                    type={type}
                    json={json}
                    text={text}
                    urlencoded={urlencoded}
                    disabled={disabled}
                  />
                </TabPane>
              )}

              <TabPane tab='Headers' key='3'>
                <ParamList
                  params={headers}
                  disabled={disabled}
                  onParamChange={onHeaderChange}
                  handleDelete={handleDeleteHeader}
                  addParam={addHeader}
                />
              </TabPane>
            </Tabs>
          </div>
        </Panel>
      }
    </Collapse>
  );
};

_RequestParams.propTypes = {
  activeKey: PropTypes.string.isRequired,
  params: PropTypes.object.isRequired,
  disabled: PropTypes.bool.isRequired,
  headers: PropTypes.object.isRequired,
  requestBody: PropTypes.object.isRequired,
  httpMethod: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.params === nextProps.params &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.activeKey === nextProps.activeKey &&
    prevProps.headers === nextProps.headers &&
    prevProps.httpMethod === nextProps.httpMethod &&
    prevProps.requestBody === nextProps.requestBody;
};

export const RequestParams = React.memo(_RequestParams, areEqual);