/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse, Form, Popover, Select, Typography} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import {Trans, useTranslation} from 'react-i18next';
import React from 'react';
import {useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import {JadeReferenceTreeSelect} from '@/components/common/JadeReferenceTreeSelect.jsx';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import PropTypes from 'prop-types';

const {Text} = Typography;
const {Panel} = Collapse;

const _TextExtractionInput = ({shapeStatus, extractParam, dispatch}) => {
  const {t} = useTranslation();
  const text = getConfigValue(extractParam, ['text'], '');
  const shape = useShapeContext();

  /**
   * 处理输入发生变化的动作
   *
   * @param id id
   * @param changes 变更的字段
   */
  const handleItemChange = (id, changes) => {
    dispatch({actionType: 'editInput', id: id, changes: changes});
  };

  /**
   * 根据值渲染组件
   *
   * @param item 值
   * @return {JSX.Element|null}
   */
  const renderComponent = (item) => {
    switch (item.from) {
      case 'Reference':
        return (<>
          <TextExtractionInputSelect text={text} handleItemChange={handleItemChange} shapeStatus={shapeStatus} t={t}/>
        </>);
      case 'Input':
        return (<>
          <TextExtractionInputInput handleItemChange={handleItemChange} shapeStatus={shapeStatus} t={t} item={item}/>
        </>);
      default:
        return null;
    }
  };

  return (
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={[`textExtractionInputPanel${shape.id}`]}>
      {<Panel
        key={`textExtractionInputPanel${shape.id}`}
        header={<div className='panel-header'>
          <span className='jade-panel-header-font'>{t('input')}</span>
        </div>}
        className='jade-panel'
      >
        <div className={'jade-custom-panel-content'}>
          <TextExtractionInputTitle text={text} handleItemChange={handleItemChange} shapeStatus={shapeStatus} t={t}/>
          {renderComponent(text)}
        </div>
      </Panel>}
    </Collapse>
  );
};

_TextExtractionInput.propTypes = {
  shapeStatus: PropTypes.object.isRequired,
  extractParam: PropTypes.object.isRequired,
  dispatch: PropTypes.func.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.shapeStatus === nextProps.shapeStatus &&
    prevProps.extractParam === nextProps.extractParam &&
    prevProps.dispatch === nextProps.dispatch;
};

/**
 * 文本提取输入的标题组件
 *
 * @param t 国际化组件
 * @param shapeStatus 组件状态
 * @param text 文本对象
 * @param handleItemChange 引用切换的回调函数
 * @returns {React.JSX.Element} 文本提取输入的标题组件
 * @constructor
 */
const TextExtractionInputTitle = ({t, shapeStatus, text, handleItemChange}) => {
  const from = text.from;
  const form = useFormContext();
  const tips = <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='textExtractionInputPopover' components={{p: <p/>}}/>
  </div>;

  return (<>
    <div className='panel-header custom-header'>
            <span style={{display: 'flex', alignItems: 'center'}}>
              <span className='jade-red-asterisk-tex jade-panel-header-font'>{t('textToBeExtracted')}</span>
            </span>
      <Popover
        content={tips}
        align={{offset: [0, 3]}}
        overlayClassName={'jade-custom-popover'}
      >
        <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
      </Popover>
      <Select
        disabled={shapeStatus.disabled}
        id={`valueSource-select-${text.id}`}
        className='select-value-custom jade-select'
        onChange={(value) => {
          let changes = [{key: 'from', value: value}, {key: 'value', value: ''}];
          if (value === 'Input') {
            changes = [{key: 'from', value: value},
              {key: 'value', value: ''},
              {key: 'referenceNode', value: ''},
              {key: 'referenceId', value: ''},
              {key: 'referenceKey', value: ''}];
          }
          form.resetFields([`reference-${text.id}`, `input-${text.id}`]);
          handleItemChange(text.id, changes);
        }}
        options={[{value: 'Reference', label: t('reference')}, {
          value: 'Input', label: t('input'),
        }]}
        value={from}
      />
    </div>
  </>);
};

TextExtractionInputTitle.propTypes = {
  t: PropTypes.func.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  text: PropTypes.object.isRequired,
  handleItemChange: PropTypes.func.isRequired,
};

/**
 * 文本提取输入区域的输入框组件
 *
 * @param t 国际化组件
 * @param item 文本对象
 * @param shapeStatus 组件状态
 * @param handleItemChange 数据修改后的的回调函数
 * @returns {React.JSX.Element} 文本提取输入区域的输入框组件
 * @constructor
 */
const TextExtractionInputInput = ({t, item, shapeStatus, handleItemChange}) => {
  /**
   * 更新input的属性
   *
   * @param itemObj 子组件
   * @return {(function(*): void)|*}
   */
  const editInput = itemObj => (e) => {
    if (!e.target.value) {
      return;
    }
    handleItemChange(itemObj.id, [{key: 'value', value: e.target.value}]);
  };

  return (<>
    <Form.Item
      id={`input-${item.id}`}
      name={`input-${item.id}`}
      rules={[{required: true, message: t('fieldValueCannotBeEmpty')}, {
        pattern: /^[^\s]*$/,
        message: t('spacesAreNotAllowed'),
      }]}
      initialValue={item.value}
      validateTrigger='onBlur'
    >
      <JadeInput
        disabled={shapeStatus.disabled}
        className='value-custom jade-input'
        placeholder={t('plsEnter')}
        value={item.value}
        onBlur={editInput(item)}
      />
    </Form.Item>
  </>);
};

TextExtractionInputInput.propTypes = {
  t: PropTypes.func.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  item: PropTypes.object.isRequired,
  handleItemChange: PropTypes.func.isRequired,
};

/**
 * 文本提取输入区域的选择框组件
 *
 * @param t 国际化组件
 * @param text 文本对象
 * @param shapeStatus 组件状态
 * @param handleItemChange 数据修改后的的回调函数
 * @returns {React.JSX.Element} 文本提取输入区域的选择框组件
 * @constructor
 */
const TextExtractionInputSelect = ({t, text, shapeStatus, handleItemChange}) => {
  /**
   * 引用的值发生改变
   *
   * @param item 子组件
   * @param referenceKey 关联的字段名
   * @param value 值
   * @param type 数据类型
   * @private
   */
  const _onReferencedValueChange = (item, referenceKey, value, type) => {
    handleItemChange(item.id, [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {
      key: 'type',
      value: type,
    }]);
  };

  /**
   * 引用的对象发生变化
   *
   * @param e 动作
   * @private
   */
  const _onReferencedKeyChange = (e) => {
    handleItemChange(text.id, [{key: 'referenceNode', value: e.referenceNode},
      {key: 'referenceId', value: e.referenceId},
      {key: 'referenceKey', value: e.referenceKey},
      {key: 'value', value: e.value},
      {key: 'type', value: e.type}]);
  };

  return (<>
    <JadeReferenceTreeSelect
      width={'100%'}
      className='textExtraction-input-select jade-select'
      disabled={shapeStatus.disabled}
      reference={text}
      onMouseDown={(e) => e.stopPropagation()}
      onReferencedKeyChange={_onReferencedKeyChange}
      onReferencedValueChange={(referenceKey, value, type) => _onReferencedValueChange(text, referenceKey, value, type)}
      rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}/>
  </>);
};

TextExtractionInputSelect.propTypes = {
  t: PropTypes.func.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  text: PropTypes.object.isRequired,
  handleItemChange: PropTypes.func.isRequired,
};

export const TextExtractionInput = React.memo(_TextExtractionInput, areEqual);