/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Col, Collapse, Form, Row} from 'antd';
import {MinusCircleOutlined} from '@ant-design/icons';
import './style.css';
import PropTypes from 'prop-types';
import {v4 as uuidv4} from 'uuid';
import {JadeStopPropagationSelect} from './JadeStopPropagationSelect.jsx';
import {JadeReferenceTreeSelect} from './JadeReferenceTreeSelect.jsx';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import React from 'react';
import ArrayUtil from '@/components/util/ArrayUtil.js';
import {useTranslation} from 'react-i18next';
import {JadeFieldName} from '@/components/common/JadeFieldName.jsx';
import {JadePanelHeader} from '@/components/common/JadePanelHeader.jsx';
import {JadeFormText} from '@/components/common/JadeFormText.jsx';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {useFormContext} from '@/components/DefaultRoot.jsx';

const {Panel} = Collapse;

/**
 * InputForm组件用于展示一个表单，其中包含一些输入项，这些输入项根据用户选择的值源展示不同的组件。
 *
 * 示例items,其中id，name，from，value必须，涉及reference时，referenceNode, referenceId, referenceKey必须, value会变为列表
 *
 * [{id: uuidv4(), name: '', type: 'String', from: 'Reference', value: '', referenceNode: '', referenceId: '', referenceKey: ''},
 * {id: uuidv4(), name: '', type: 'String', from: 'Reference', value: '', referenceNode: '', referenceId: '', referenceKey: ''},
 * {id: uuidv4(), name: '', type: 'String', from: 'Reference', value: '', referenceNode: '', referenceId: '', referenceKey: ''}
 *
 * @returns {JSX.Element}
 * @constructor
 */
/**
 * Jade标准输入
 *
 * @param items 需要组件展示的item数据结构的数组
 * @param addItem 当添加一个新item时会调用此方法，此方法需要有id入参
 * @param updateItem 当修改一个已有item时会调用此方法，此方法需要有id，修改的key和修改的value组成的对象列表两个入参
 * @param deleteItem 当删除一个已有item时会调用此方法，此方法需要有id入参
 * @param content 输入提示
 * @param shapeStatus 图形状态集合.
 * @param options 自定义的类型选择下拉框
 * @param maxInputLength 输入的最大长度限制.
 * @param editable 是否可编辑.
 * @param fieldNameClassName
 * @param typeSelectClassName
 * @param fieldValueClassName
 * @param deleteBtnClassName
 * @returns {JSX.Element} Jade标准输入表单的DOM
 */
const _JadeInputForm = (
  {
    items,
    addItem,
    updateItem,
    deleteItem,
    content,
    shapeStatus,
    options = [],
    maxInputLength,
    editable = true,
    fieldNameClassName,
    typeSelectClassName,
    fieldValueClassName,
    deleteBtnClassName,
  }) => {
  const form = useFormContext();
  const {t} = useTranslation();

  const handleAdd = () => {
    addItem(uuidv4());
  };

  const getTypeSelectOptions = () => {
    if (ArrayUtil.isEqual(options, [])) {
      return [{value: FROM_TYPE.REFERENCE, label: t('reference')},
        {value: FROM_TYPE.INPUT, label: t('input')}];
    } else {
      return options;
    }
  };

  /**
   * 当reference对应监听对象自身发生变化时调用
   *
   * @param item 变化的item信息
   * @param referenceKey 依赖key对象
   * @param value 变化值对象
   * @param type 变化值类型
   */
  const handleReferenceValueChange = (item, referenceKey, value, type) => {
    updateItem(item.id, [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {
      key: 'type',
      value: type,
    }]);
  };

  /**
   * 当切换reference对应监听对象时调用
   *
   * @param item 变化的item信息
   * @param e 变化值对象
   */
  const handleReferenceKeyChange = (item, e) => {
    updateItem(item.id, [{key: 'referenceNode', value: e.referenceNode}, {
      key: 'referenceId', value: e.referenceId,
    }, {key: 'referenceKey', value: e.referenceKey}, {key: 'value', value: e.value}, {key: 'type', value: e.type}]);
  };

  const handleItemChange = (name, value, itemId) => {
    const changes = [{key: name, value}];
    // 如果字段为 from，则清空 value 字段
    if (name === 'from') {
      changes.push({key: 'value', value: undefined});
      changes.push({key: 'referenceNode', value: ''});
      changes.push({key: 'referenceId', value: ''});
      changes.push({key: 'referenceKey', value: ''});
      if (value === FROM_TYPE.INPUT) {
        changes.push({key: 'type', value: DATA_TYPES.STRING});
      }
      document.activeElement.blur(); // 在选择后取消焦点
      form.setFieldsValue({[`value-${itemId}`]: undefined});
      form.setFieldsValue({[`reference-${itemId}`]: undefined});
    }
    updateItem(itemId, changes);
  };

  const getValueSelectClassName = () => {
    if (fieldValueClassName) {
      return `value-custom jade-select ${fieldValueClassName}`;
    } else {
      return 'value-custom jade-select';
    }
  };

  const getValueInputClassName = () => {
    if (fieldValueClassName) {
      return `value-custom jade-input ${fieldValueClassName}`;
    } else {
      return 'value-custom jade-input';
    }
  };

  // 根据不同的值渲染不同的组件
  const renderComponent = (item) => {
    switch (item.from) {
      case 'Reference':
        return (<>
          <JadeReferenceTreeSelect
            disabled={shapeStatus.referenceDisabled}
            rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
            className={getValueSelectClassName()}
            placeholder={t('pleaseSelect')}
            reference={item}
            onReferencedValueChange={(referenceKey, value, type) => handleReferenceValueChange(item, referenceKey, value, type)}
            onReferencedKeyChange={(e) => handleReferenceKeyChange(item, e)}
          />
        </>);
      case 'Input':
        return <Form.Item
          id={`value-${item.id}`}
          name={`value-${item.id}`}
          rules={[{required: true, message: t('fieldValueCannotBeEmpty')}, {
            pattern: /^[^\s]*$/, message: t('spacesAreNotAllowed'),
          }]}
          initialValue={item.value}
          validateTrigger='onBlur'
        >
          <JadeInput
            disabled={shapeStatus.disabled}
            maxLength={maxInputLength}
            placeholder={t('plsEnter')}
            className={getValueInputClassName()}
            value={item.value}
            onChange={(e) => handleItemChange('value', e.target.value, item.id)}
          />
        </Form.Item>;
      default:
        return <></>;
    }
  };

  /**
   * 条目是否可以删除
   *
   * @param item 条目对象
   * @returns {*|boolean} true：可删除， false：不可删除
   */
  const getDeletable = (item) => (item.editable === undefined || item.editable === null) ? true : item.editable;

  const handleSelectClick = (event) => {
    event.stopPropagation(); // 阻止事件冒泡
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse' defaultActiveKey={['inputPanel']}>
      {<Panel key={'inputPanel'}
              header={<>
                <JadePanelHeader
                  text={'input'}
                  tips={content}
                  shapeStatus={shapeStatus}
                  onClick={(event) => {
                    handleAdd();
                    handleSelectClick(event);
                  }}
                  editable={editable}/>
              </>}
              className='jade-panel'
      >
        <div className={'jade-custom-panel-content'}>
          <JadeInputTitle t={t}/>
          {items.map((item) => (<>
              <Row key={item.id} gutter={16} align={'top'}>
                <Col span={8}>
                  <JadeInputFieldName item={item}
                                      t={t}
                                      className={fieldNameClassName}
                                      shapeStatus={shapeStatus}
                                      handleItemChange={handleItemChange}
                                      items={items}/>
                </Col>
                <Col span={6} style={{paddingRight: 0}}>
                  <JadeInputTypeSelect
                    t={t}
                    className={typeSelectClassName}
                    shapeStatus={shapeStatus}
                    updateItem={updateItem}
                    item={item}
                    options={getTypeSelectOptions()}
                    handleItemChange={handleItemChange}/>
                </Col>
                <Col span={8} style={{paddingLeft: 0}}>
                  {renderComponent(item)} {/* 渲染对应的组件 */}
                </Col>
                <Col span={2} style={{paddingLeft: 0}}>
                  {getDeletable(item) && <JadeInputRowDeleteButton
                    shapeStatus={shapeStatus} item={item} deleteItem={deleteItem} className={deleteBtnClassName}/>}
                </Col>
              </Row>
          </>))}
        </div>
      </Panel>}
    </Collapse>
  </>);
};

_JadeInputForm.propTypes = {
  items: PropTypes.array.isRequired, // 确保 items 是一个必需的数组类型
  addItem: PropTypes.func,
  updateItem: PropTypes.func,
  deleteItem: PropTypes.func,
  shapeStatus: PropTypes.object,
  content: PropTypes.element,
  editable: PropTypes.bool,
  options: PropTypes.array,
  maxInputLength: PropTypes.number,
  fieldNameClassName: PropTypes.string,
  typeSelectClassName: PropTypes.string,
  fieldValueClassName: PropTypes.string,
  deleteBtnClassName: PropTypes.string,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.shapeStatus === nextProps.shapeStatus &&
    ArrayUtil.isEqual(prevProps.items, nextProps.items) &&
    prevProps.editable === nextProps.editable;
};

/**
 * JadeInput标题组件，展示字段名、字段值等
 *
 * @param t 国际化组件
 * @returns {React.JSX.Element} JadeInput标题组件
 * @constructor
 */
const JadeInputTitle = ({t}) => {
  return (<>
    <Row gutter={16}>
      <Col span={8}>
        <span className='jade-font-size jade-font-color'>{t('fieldName')}</span>
      </Col>
      <Col span={16}>
        <span className='jade-font-size jade-font-color'>{t('fieldValue')}</span>
      </Col>
    </Row>
  </>);
};

JadeInputTitle.propTypes = {
  t: PropTypes.func.isRequired,
};

/**
 * 输入条目的属性名组件
 *
 * @param t 国际化组件
 * @param item 条目
 * @param items 所有条目
 * @param shapeStatus shape状态
 * @param handleItemChange 条目改变后的回调
 * @param className 输入框样式
 * @returns {React.JSX.Element} 输入条目的属性名组件
 * @constructor
 */
const JadeInputFieldName = ({t, item, items, shapeStatus, handleItemChange, className}) => {
  // 判断是否可编辑，决定使用不同的组件渲染
  return item.editable !== false ? (<>
    <JadeFieldName
      id={item.id}
      name={item.name}
      className={className}
      onNameChange={(v) => handleItemChange('name', v, item.id)}
      shapeStatus={shapeStatus}
      style={{paddingRight: '12px'}}
      validator={(v) => {
        const otherValues = items.filter(i => i.id !== item.id)
          .map(i => i.name);
        if (otherValues.includes(v)) {
          return Promise.reject(new Error(t('attributeNameMustBeUnique')));
        }
        return Promise.resolve();
      }}
    />
  </>) : (<>
    <JadeFormText id={item.id} title={item.name} required={true}/>
  </>);
};

JadeInputFieldName.propTypes = {
  t: PropTypes.func.isRequired,
  item: PropTypes.object.isRequired,
  items: PropTypes.array.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  handleItemChange: PropTypes.func.isRequired,
};

/**
 * 展示JadeInput输入字段名组件
 *
 * @param t 国际化组件
 * @param item 条目
 * @param shapeStatus shape状态
 * @param handleItemChange 更新条目回调
 * @param className 自定义的className
 * @param options 自定义的options
 * @returns {React.JSX.Element} 展示JadeInput输入字段名组件
 * @constructor
 */
const JadeInputTypeSelect = ({t, item, shapeStatus, handleItemChange, className, options}) => {
  const getClassName = () => {
    if (className) {
      return `value-source-custom jade-select ${className}`;
    } else {
      return 'value-source-custom jade-select';
    }
  };

  return (<>
    <Form.Item id={`from-${item.id}`} name={`from-${item.id}`} initialValue={item.from}>
      <JadeStopPropagationSelect
        disabled={shapeStatus.disabled}
        id={`from-select-${item.id}`}
        className={getClassName()}
        style={{width: '100%'}}
        onChange={(value) => handleItemChange('from', value, item.id)}
        options={options}
        value={item.from}
      />
    </Form.Item>
  </>);
};

JadeInputTypeSelect.propTypes = {
  t: PropTypes.func.isRequired,
  item: PropTypes.object.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  handleItemChange: PropTypes.func.isRequired,
  options: PropTypes.array.isRequired,
};

/**
 * 条目删除按钮
 *
 * @param item 输入条目
 * @param shapeStatus shape状态
 * @param deleteItem 删除条目更新数据的回调方法
 * @returns {React.JSX.Element} 条目删除按钮
 * @constructor
 */
const JadeInputRowDeleteButton = ({item, shapeStatus, deleteItem, className}) => {
  /**
   * 删除回调
   *
   * @param itemId 条目id
   */
  const handleDelete = (itemId) => {
    deleteItem(itemId);
  };

  const getDeleteBtnClassName = () => {
    if (className) {
      return `icon-button ${className}`;
    } else {
      return 'icon-button';
    }
  };

  return (<>
    <Form.Item id={`delete-${item.id}`} name={`delete-${item.id}`}>
      <Button disabled={shapeStatus.disabled}
              type='text'
              className={getDeleteBtnClassName()}
              style={{height: '100%'}}
              onClick={() => handleDelete(item.id)}>
        <MinusCircleOutlined/>
      </Button>
    </Form.Item>
  </>);
};

JadeInputRowDeleteButton.propTypes = {
  item: PropTypes.object.isRequired, shapeStatus: PropTypes.object.isRequired, deleteItem: PropTypes.func.isRequired,
};

export const JadeInputForm = React.memo(_JadeInputForm, areEqual);