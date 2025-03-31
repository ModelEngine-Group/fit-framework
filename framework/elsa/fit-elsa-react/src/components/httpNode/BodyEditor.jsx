/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Form} from 'antd';
import '../common/style.css';
import React from 'react';
import TextArea from 'antd/es/input/TextArea.js';
import {ParamList} from '@/components/httpNode/ParamList.jsx';
import PropTypes from 'prop-types';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {useTranslation} from 'react-i18next';
import {HTTP_BODY_TYPE} from '@/common/Consts.js';

/**
 * 请求体编辑组件
 *
 * @param type 类型
 * @param json json对象
 * @param text 文本对象
 * @param urlencoded form对象
 * @param disabled 是否禁用
 * @returns {React.JSX.Element}
 * @private
 */
const _BodyEditor = ({type, json, text, urlencoded, disabled}) => {
  const dispatch = useDispatch();
  const {t} = useTranslation();
  /**
   * 失焦时才设置值，对于必填项.若为空，则不设置
   *
   * @param e event
   * @param actionType 事件类型
   * @param id 需要修改的数据id
   */
  const changeOnBlur = (e, actionType, id) => {
    dispatch({actionType: actionType, value: e.target.value, id});
  };

  /**
   * 请求体参数属性修改
   *
   * @param id 条目id
   * @param property 属性名
   * @param value 输入框的值值
   */
  const onBodyParamChange = (id, property, value) => {
    dispatch({actionType: 'bodyParamChange', id: id, value, property});
  };

  /**
   * 删除一条数据
   *
   * @param key 条目id
   */
  const handleDeleteBodyParam = key => {
    dispatch({actionType: 'deleteBodyParam', id: key});
  };

  /**
   * 添加请求体的param
   *
   * @param event 事件
   */
  const addBodyParam = event => {
    dispatch({actionType: 'addBodyParam'});
  };

  /**
   * 根据请求体类型渲染不同的组件
   *
   * @param bodyType 请求体类型
   * @returns {Element}
   */
  const renderByType = (bodyType) => {
    switch (bodyType) {
      case HTTP_BODY_TYPE.X_WWW_FORM_URLENCODED: {
        return <ParamList
          params={urlencoded}
          disabled={disabled}
          onParamChange={onBodyParamChange}
          handleDelete={handleDeleteBodyParam}
          addParam={addBodyParam}
          updateKey='x-www-form-urlencoded'
        />;
      }
      case HTTP_BODY_TYPE.JSON: {
        return <Form.Item
          name={`jsonEditor-${json.id}`}
          id={`jsonEditor-${json.id}`}
          initialValue={json.value}
          rules={[{required: true, message: t('jsonRule')}]}
        >
          <TextArea
            disabled={disabled}
            className={`raw-text-textarea-input`}
            maxLength={2000}
            onBlur={(e) => changeOnBlur(e, 'changeData', json.id)}
          />
        </Form.Item>;
      }
      case HTTP_BODY_TYPE.TEXT : {
        return <Form.Item
          className='jade-form-item'
          rules={[{required: true, message: t('textRule')}]}
          name={`raw-text-${text.id}`}
          id={`raw-text-${text.id}`}
          initialValue={text.value}
          validateTrigger='onBlur'
        >
          <TextArea
            disabled={disabled}
            className={`raw-text-textarea-input`}
            maxLength={2000}
            onBlur={(e) => changeOnBlur(e, 'changeData', text.id)}
          />
        </Form.Item>;
      }
      default: {
        return <></>;
      }
    }
  };

  return (<>
      {renderByType(type)}
    </>
  );
};

_BodyEditor.propTypes = {
  type: PropTypes.string.isRequired,
  json: PropTypes.object.isRequired,
  text: PropTypes.object.isRequired,
  urlencoded: PropTypes.object.isRequired,
  disabled: PropTypes.bool.isRequired,
  onParamChange: PropTypes.func.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.type === nextProps.type &&
    prevProps.json === nextProps.json &&
    prevProps.text === nextProps.text &&
    prevProps.urlencoded === nextProps.urlencoded &&
    prevProps.onParamChange === nextProps.onParamChange &&
    prevProps.disabled === nextProps.disabled;
};

export const BodyEditor = React.memo(_BodyEditor, areEqual);