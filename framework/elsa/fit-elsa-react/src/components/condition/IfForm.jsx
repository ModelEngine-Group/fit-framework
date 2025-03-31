/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Col, Collapse, Form, InputNumber, Row, Switch} from 'antd';
import {MinusCircleOutlined, PlusOutlined} from '@ant-design/icons';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import {JadeReferenceTreeSelect} from '@/components/common/JadeReferenceTreeSelect.jsx';
import './style.css';
import {useFormContext} from '@/components/DefaultRoot.jsx';
import {BINARY_OPERATOR, UNARY_OPERATOR} from '@/common/Consts.js';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import React from 'react';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';

const {Panel} = Collapse;

/**
 * 判断条件表单。
 *
 * @returns {JSX.Element} 判断条件表单的DOM。
 */
export const IfForm = (
  {
    branch,
    name,
    index,
    totalItemNum,
    disabled = false,
    deleteBranch,
    changeConditionRelation,
    addCondition,
    deleteCondition,
    changeConditionConfig,
  }) => {
  const {t} = useTranslation();
  const form = useFormContext();
  const unaryOperators = Object.values(UNARY_OPERATOR);

  const conditionWrapper = (condition) => {
    condition.getLeft = () => {
      return condition.value.find(item => item.name === 'left');
    };

    condition.getRight = () => {
      return condition.value.find(item => item.name === 'right');
    };

    return condition;
  };

  const handleDeleteBranch = () => {
    deleteBranch(branch.id);
  };

  const handleDeleteCondition = (conditionId) => {
    deleteCondition(branch.id, conditionId);
  };

  const handleConditionRelationChange = (updatedConditionRelation) => {
    changeConditionRelation(branch.id, updatedConditionRelation);
  };

  const handleConditionChange = (condition, newConditionValue, newLeftType) => {
    const conditionId = condition.id;
    form.setFieldsValue({[`condition-${conditionId}`]: newConditionValue});
    changeConditionConfig(branch.id, conditionId, [{key: 'condition', value: newConditionValue}]);
    setDefaultValueOfRightInput(condition, newLeftType, newConditionValue);
  };

  const handleReferenceValueChange = (conditionId, itemId, referenceKey, value, type) => {
    let valueArray = [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}];
    if (type !== null) {
      // 这里需要判断type是否为空，避免选择从Reference切换至Input时会重置输入的type为null
      valueArray.push({key: 'type', value: type});
    }
    changeConditionConfig(branch.id, conditionId, [{
      key: itemId,
      value: valueArray,
    }]);
  };

  const handleReferenceKeyChange = (conditionId, itemId, e) => {
    changeConditionConfig(branch.id, conditionId, [{
      key: itemId,
      value: [
        {key: 'referenceNode', value: e.referenceNode},
        {key: 'referenceId', value: e.referenceId},
        {key: 'referenceKey', value: e.referenceKey},
        {key: 'value', value: e.value},
        {key: 'type', value: e.type},
      ],
    }]);
  };

  const handleItemChange = (conditionId, itemId, changeParams) => {
    changeConditionConfig(branch.id, conditionId, [{key: itemId, value: changeParams}]);
  };

  const getConditionOptionsByReferenceType = (referenceType) => {
    if (!referenceType) {
      return [];
    }
    switch (referenceType.toLowerCase()) {
      case 'string':
        return [
          {value: BINARY_OPERATOR.EQUAL, label: t('equal')},
          {value: BINARY_OPERATOR.NOT_EQUAL, label: t('notEqual')},
          {value: BINARY_OPERATOR.CONTAINS, label: t('contains')},
          {value: BINARY_OPERATOR.DOES_NOT_CONTAIN, label: t('doesNotContain')},
          {value: BINARY_OPERATOR.LONGER_THAN, label: t('longerThan')},
          {value: BINARY_OPERATOR.LONGER_THAN_OR_EQUAL, label: t('longerThanOrEqual')},
          {value: BINARY_OPERATOR.SHORTER_THAN, label: t('shorterThan')},
          {value: BINARY_OPERATOR.SHORTER_THAN_OR_EQUAL, label: t('shorterThanOrEqual')},
          {value: BINARY_OPERATOR.STARTS_WITH, label: t('startsWith')},
          {value: BINARY_OPERATOR.ENDS_WITH, label: t('endsWith')},
          {value: UNARY_OPERATOR.IS_EMPTY_STRING, label: t('isEmpty')},
          {value: UNARY_OPERATOR.IS_NOT_EMPTY_STRING, label: t('isNotEmpty')},
          {value: UNARY_OPERATOR.IS_NULL, label: t('isNull')},
          {value: UNARY_OPERATOR.IS_NOT_NULL, label: t('isNotNull')},
        ];
      case 'boolean':
        return [
          {value: BINARY_OPERATOR.EQUAL, label: t('equal')},
          {value: BINARY_OPERATOR.NOT_EQUAL, label: t('notEqual')},
          {value: UNARY_OPERATOR.IS_NULL, label: t('isNull')},
          {value: UNARY_OPERATOR.IS_NOT_NULL, label: t('isNotNull')},
          {value: UNARY_OPERATOR.IS_TRUE, label: t('isTrue')},
          {value: UNARY_OPERATOR.IS_FALSE, label: t('isFalse')},
        ];
      case 'integer':
      case 'number':
        return [
          {value: BINARY_OPERATOR.EQUAL, label: t('equal')},
          {value: BINARY_OPERATOR.NOT_EQUAL, label: t('notEqual')},
          {value: UNARY_OPERATOR.IS_NULL, label: t('isNull')},
          {value: UNARY_OPERATOR.IS_NOT_NULL, label: t('isNotNull')},
          {value: BINARY_OPERATOR.GREATER_THAN, label: t('greaterThan')},
          {value: BINARY_OPERATOR.GREATER_THAN_OR_EQUAL, label: t('greaterThanOrEqual')},
          {value: BINARY_OPERATOR.LESS_THAN, label: t('lessThan')},
          {value: BINARY_OPERATOR.LESS_THAN_OR_EQUAL, label: t('lessThanOrEqual')},
        ];
      case 'array<string>':
      case 'array<integer>':
      case 'array<boolean>':
      case 'array<number>':
      case 'array':
        return [
          // {value: 'longer than', label: 'longer than'},
          // {value: 'longer than or equal', label: 'longer than or equal'},
          // {value: 'shorter than', label: 'shorter than'},
          // {value: 'shorter than or equal', label: 'shorter than or equal'},
          // {value: 'contain', label: 'contain'},
          // {value: 'not contain', label: 'not contain'},
          {value: UNARY_OPERATOR.IS_EMPTY, label: t('isEmpty')},
          {value: UNARY_OPERATOR.IS_NOT_EMPTY, label: t('isNotEmpty')},
        ];
      default:
        return [];
    }
  };

  /**
   * 根据总的组件数目渲染优先级样式
   *
   * @return {JSX.Element|null}
   */
  const renderPriority = () => {
    if (totalItemNum > 2) {
      return <div className={'priority-tag'}>
        <div className={'priority-inner-text jade-font-size'}>Priority {index + 1}</div>
      </div>;
    } else {
      return null;
    }
  };

  /**
   * 根据总的组件数目渲染删除按钮
   *
   * @return {JSX.Element|null}
   */
  const renderDeleteIcon = () => {
    if (totalItemNum > 2) {
      return (<>
        <Button type='text' className='jade-panel-header-icon-position icon-button'
                disabled={disabled}
                onClick={() => handleDeleteBranch()}>
          <MinusCircleOutlined/>
        </Button>
      </>);
    } else {
      return null;
    }
  };

  /**
   * 若条件分支中条件数大于1，则需要渲染左侧图示，用于配置条件之间的关系 e.g. and/or
   *
   * @return {JSX.Element|null}
   */
  const renderLeftDiagram = () => {
    return <div className={'condition-left-diagram'}>
      <JadeStopPropagationSelect
        id={`condition-left-select-${branch.id}`}
        className='jade-select operation'
        placement='bottomLeft'
        popupClassName={'condition-left-drop'}
        onChange={(value) => handleConditionRelationChange(value)}
        options={[
          {value: 'and', label: 'And'},
          {value: 'or', label: 'Or'},
        ]}
        value={branch.conditionRelation}
      />
    </div>;
  };

  const renderDeleteConditionIcon = (index, conditionId) => {
    if (index > 0) {
      return <Form.Item>
        <Button type='text' className='icon-button'
                style={{height: '100%'}}
                disabled={disabled}
                onClick={() => handleDeleteCondition(conditionId)}>
          <MinusCircleOutlined/>
        </Button>
      </Form.Item>;
    }
    return null;
  };

  // 根据 condition 设置 rules
  const selectedRightSideValueRules = (condition, rule) => {
    return unaryOperators.includes(condition) ? [{}] : (rule || [{required: true, message: t('fieldValueCannotBeEmpty')}]);
  };

  // 根据不同的值渲染不同的组件
  const renderRightSideValueComponent = (conditionId, item, referenceType, condition) => {
    switch (item.from) {
      case 'Reference':
        return <>
          <JadeReferenceTreeSelect
            className='value-custom jade-select'
            disabled={disabled || unaryOperators.includes(condition)}
            rules={selectedRightSideValueRules(condition, [{
              validator: (_, value) => {
                if (value && item.type.toLowerCase() !== referenceType.toLowerCase()) {
                  return Promise.reject(new Error(t('fieldTypeMismatch')));
                }
                return Promise.resolve();
              },
            }, {required: true, message: t('fieldValueCannotBeEmpty')}])}
            reference={item}
            onReferencedValueChange={(referenceKey, value, type) => handleReferenceValueChange(conditionId, item.id, referenceKey, value, type)}
            onReferencedKeyChange={(e) => handleReferenceKeyChange(conditionId, item.id, e)}
          />
        </>;
      case 'Input':
        form.setFieldsValue({[`value-${item.id}`]: item.value});
        switch (referenceType) {
          case 'String':
          case 'Array<String>':
            return <Form.Item
              id={`value-${item.id}`}
              name={`value-${item.id}`}
              rules={selectedRightSideValueRules(condition)}
              initialValue={item.value}
            >
              <JadeInput
                className='value-custom jade-input'
                disabled={disabled || unaryOperators.includes(condition)}
                value={item.value}
                onChange={(e) => handleItemChange(conditionId, item.id, [{
                  key: 'value',
                  value: e.target.value,
                }])}
              />
            </Form.Item>;
          case 'Boolean':
          case 'Array<Boolean>':
            return <div style={{height: '22.4px', display: 'flex', alignItems: 'center'}}>
              <Switch disabled={disabled || unaryOperators.includes(condition)}
                      style={{marginLeft: '8px'}}
                      onChange={(e) => handleItemChange(conditionId, item.id, [{
                        key: 'value',
                        value: e,
                      }])}
                      checked={item.value}/>
            </div>;
          case 'Integer':
          case 'Array<Integer>':
            return <Form.Item
              id={`value-${item.id}`}
              name={`value-${item.id}`}
              rules={selectedRightSideValueRules(condition)}
              initialValue={item.value}
            >
              <InputNumber
                className='value-custom jade-input'
                disabled={disabled || unaryOperators.includes(condition)}
                step={1}
                precision={0}
                parser={(value) => value.replace(/[^\d-]/g, '')} // 只允许输入整数部分
                onChange={(e) => handleItemChange(conditionId, item.id, [{key: 'value', value: e}])}
                stringMode
              />
            </Form.Item>;
          case 'Number':
          case 'Array<Number>':
            return <Form.Item
              id={`value-${item.id}`}
              name={`value-${item.id}`}
              rules={selectedRightSideValueRules(condition)}
              initialValue={item.value}
            >
              <InputNumber
                className='value-custom jade-input'
                disabled={disabled || unaryOperators.includes(condition)}
                step={1}
                onChange={(e) => handleItemChange(conditionId, item.id, [{key: 'value', value: e}])}
                stringMode
              />
            </Form.Item>;
          default:
            return <Form.Item
              id={`value-${item.id}`}
              name={`value-${item.id}`}
              rules={selectedRightSideValueRules(condition, [{
                required: true,
                message: t('fieldValueCannotBeEmpty'),
              }, {
                pattern: /^[^\s]*$/,
                message: t('spacesAreNotAllowed'),
              }])}
              initialValue={item.value}
            >
              <JadeInput
                className='value-custom jade-input'
                disabled={disabled || unaryOperators.includes(condition)}
                value={item.value}
                onChange={(e) => handleItemChange(conditionId, item.id, [{
                  key: 'value',
                  value: e.target.value,
                }])}
              />
            </Form.Item>;
        }
      default:
        return <></>;
    }
  };

  const setDefaultValueOfRightInput = (condition, newLeftType, newConditionValue) => {
    const newRightType = inferRightType(newLeftType, newConditionValue);
    if (newRightType === condition.getRight().type) {
      return;
    }
    const rightValue = newRightType === 'Boolean' ? false : '';
    const right = condition.getRight();
    if (right.from === 'Input') {
      const rightItemId = right.id;
      form.setFieldsValue({[`value-${rightItemId}`]: rightValue});
      handleItemChange(condition.id, rightItemId, [
        {key: 'value', value: rightValue},
        {key: 'type', value: newRightType},
      ]);
    }
  };

  const onLeftReferenceTreeSelectReferencedValueChange = (condition, referenceKey, value, type) => {
    const left = condition.getLeft();
    if (!left.type) {
      handleReferenceValueChange(condition.id, left.id, referenceKey, value, type);
      return;
    }
    if (!type) {
      handleConditionChange(condition, null, type);
      handleReferenceValueChange(condition.id, left.id, referenceKey, value, type);
      return;
    }
    if (left.type.toLowerCase() !== type.toLowerCase()) {
      handleConditionChange(condition, null, type);
      handleReferenceValueChange(condition.id, left.id, referenceKey, value, type);
      return;
    }
    if (left.referenceKey !== referenceKey) {
      handleReferenceValueChange(condition.id, left.id, referenceKey, value, type);
    }
  };

  const inferRightType = (leftType, condition) => {
    if (leftType && leftType.toLowerCase() === 'string' && [BINARY_OPERATOR.LONGER_THAN, BINARY_OPERATOR.LONGER_THAN_OR_EQUAL, BINARY_OPERATOR.SHORTER_THAN, BINARY_OPERATOR.SHORTER_THAN_OR_EQUAL].includes(condition)) {
      return 'Integer';
    }
    return leftType;
  };

  const onRightFromSelectChange = (condition, value) => {
    const left = condition.getLeft();
    const rightValue = left.type === 'Boolean' ? false : '';
    const rightId = condition.getRight().id;
    form.setFieldsValue({[`value-${rightId}`]: rightValue});
    handleItemChange(condition.id, rightId,
      [
        {key: 'from', value: value},
        {
          key: 'type',
          value: inferRightType(left.type, condition.condition),
        },
        {key: 'value', value: rightValue},
        {key: 'referenceNode', value: ''}, {key: 'referenceId', value: ''}, {key: 'referenceKey', value: ''},
      ]);
  };

  const renderCondition = (condition, index) => {
    // 初始化时，如果condition的值，存在，需要通过form进行设置才会生效.
    if (condition.condition) {
      form.setFieldsValue({[`condition-${condition.id}`]: condition.condition});
    }
    return <>
      <Row gutter={16} key={`row-${index}`} style={{marginBottom: '6px', marginRight: 0}}>
        <Col span={6}>
          <JadeReferenceTreeSelect
            disabled={disabled}
            className='jade-select'
            rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
            reference={condition.getLeft()}
            onReferencedValueChange={(referenceKey, value, type) => {
              onLeftReferenceTreeSelectReferencedValueChange(condition, referenceKey, value, type);
            }}
            onReferencedKeyChange={(e) => {
              const left = condition.getLeft();
              handleReferenceKeyChange(condition.id, left.id, e);
              const isTypeExist = left.type && e.type;
              if (isTypeExist && e.type.toLowerCase() !== left.type.toLowerCase()) {
                handleConditionChange(condition, null, e.type);
              }
            }}
          />
        </Col>
        <Col span={6}>
          <Form.Item
            name={`condition-${condition.id}`}
            rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
            initialValue={condition.condition}
          >
            <JadeStopPropagationSelect
              disabled={disabled}
              className='jade-select'
              style={{width: '100%'}}
              dropdownStyle={{width: '150px'}} // 设置下拉框宽度
              placeholder={t('pleaseSelectCondition')}
              options={getConditionOptionsByReferenceType(condition.getLeft().type)}
              value={condition.condition}
              onChange={(e) => handleConditionChange(condition, e, condition.getLeft().type)}
              dropdownMatchSelectWidth={false}
            />
          </Form.Item>
        </Col>
        <Col span={4} style={{paddingRight: 0}}>
          <Form.Item>
            <JadeStopPropagationSelect
              id={`from-select-${condition.id}`}
              disabled={disabled || unaryOperators.includes(condition.condition)}
              className='value-source-custom jade-select'
              style={{width: '100%'}}
              onChange={(value) => onRightFromSelectChange(condition, value)}
              options={[
                {value: 'Reference', label: t('reference')},
                {value: 'Input', label: t('input')},
              ]}
              value={condition.getRight().from}
            />
          </Form.Item>
        </Col>
        <Col span={7} style={{paddingLeft: 0}}>
          {renderRightSideValueComponent(condition.id, condition.getRight(),
            inferRightType(condition.getLeft().type, condition.condition), condition.condition)} {/* 渲染对应的组件 */}
        </Col>
        <Col span={1} style={{paddingLeft: 0}}>
          {renderDeleteConditionIcon(index, condition.id)}
        </Col>
      </Row>
    </>;
  };

  /**
   * 渲染每个条件分支中的表格
   *
   * @return {JSX.Element|null}
   */
  const renderForm = () => {
    return <div style={{display: 'flex'}}>
      {branch.conditions.length > 1 && renderLeftDiagram()}
      <div
        className={'jade-form condition-right-component'}
      >
        <Row gutter={16}>
          <Col span={6}>
            <Form.Item>
              <span className='jade-font-size jade-font-color'>{t('variable')}</span>
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item>
              <span className='jade-font-size jade-font-color'>{t('condition')}</span>
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item>
              <span className='jade-font-size jade-font-color'>{t('compareObject')}</span>
            </Form.Item>
          </Col>
        </Row>
        {branch.conditions.map((condition, index) => renderCondition(conditionWrapper(condition), index))}
        <Row gutter={16} style={{marginBottom: '6px', marginRight: 0}}>
          <Button type='link'
                  className='icon-button'
                  onClick={() => addCondition(branch.id)}
                  disabled={disabled}
                  style={{height: '32px', paddingLeft: '8px'}}>
            <PlusOutlined/>
            <span>{t('addCondition')}</span>
          </Button>
        </Row>
      </div>
    </div>;
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse' defaultActiveKey={['ifPanel']}>
      {<Panel
        key={'ifPanel'}
        header={
          <div className='panel-header'>
            <span className='jade-panel-header-font'>{name}</span>
            {renderPriority()}
            {renderDeleteIcon()}
          </div>
        }
        className='jade-panel'
      >
        <div className={'jade-custom-panel-content'}>
          {renderForm()}
        </div>
      </Panel>}
    </Collapse>
  </>);
};

IfForm.propTypes = {
  branch: PropTypes.object,
  name: PropTypes.string,
  index: PropTypes.number,
  totalItemNum: PropTypes.number,
  disabled: PropTypes.bool,
  deleteBranch: PropTypes.func,
  changeConditionRelation: PropTypes.func,
  addCondition: PropTypes.func,
  deleteCondition: PropTypes.func,
  changeConditionConfig: PropTypes.func,
};