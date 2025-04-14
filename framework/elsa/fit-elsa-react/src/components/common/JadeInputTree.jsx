/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Col, Form, Popover, Radio, Row, Tree} from 'antd';
import './jadeInputTree.css';
import PropTypes from 'prop-types';
import {JadeStopPropagationSelect} from './JadeStopPropagationSelect.jsx';
import {JadeReferenceTreeSelect} from './JadeReferenceTreeSelect.jsx';
import {useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import {useTranslation} from 'react-i18next';
import React, {useEffect, useState} from 'react';
import {MinusCircleOutlined} from '@ant-design/icons';
import {JadeFieldName} from '@/components/common/JadeFieldName.jsx';
import {JadeFormText} from '@/components/common/JadeFormText.jsx';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

/*
 * 获取options数据.
 */
const defaultGetOptions = (node) => {
  const {t} = useTranslation();
  switch (node.type) {
    case 'Object':
      if (Object.prototype.hasOwnProperty.call(node, 'generic') || node.props === undefined) {
        return [{value: FROM_TYPE.REFERENCE, label: t('reference')}, {value: FROM_TYPE.INPUT, label: t('input')}];
      } else {
        return [{value: FROM_TYPE.REFERENCE, label: t('reference')},
          {value: FROM_TYPE.INPUT, label: t('input')},
          {value: FROM_TYPE.EXPAND, label: t('expand')},
        ];
      }
    case 'Array':
    default:
      return [{value: FROM_TYPE.REFERENCE, label: t('reference')}, {value: FROM_TYPE.INPUT, label: t('input')}];
  }
};

/**
 * 构建树.
 *
 * @param data 数据.
 * @param level 层级.
 * @param parentPath 父级路径（用于拼接）
 * @return {Omit<*, 'name'>} 树状结构.
 */
const convert = (data, level, parentPath) => {
  const {name, ...ans} = data;
  ans.level = level;
  ans.title = name;
  ans.key = data.id;
  ans.isRequired = data.isRequired ?? false;
  ans.isHidden = data.isHidden ?? false;
  ans.isLeaf = data.from !== FROM_TYPE.EXPAND;

  // 计算当前节点的路径
  ans.path = parentPath ? `${parentPath}.${name}` : name;

  if (!ans.isLeaf) {
    ans.children = data.value.map(v => convert(v, level + 1, ans.path));
  }
  return ans;
};

const INPUT_WIDTH = 100;
const LEVEL_DISTANCE = 24;

/**
 * 带input的树形组件.主要用于api调用节点.
 *
 * @param data 数据.
 * @param updateItem 修改方法.
 * @param shapeStatus 节点状态
 * @param getOptions 获取options的方法.
 * @param onDelete 当触发删除时的回调.
 * @param defaultExpandAll 是否默认展开所有.
 * @param width 宽度.
 * @param showRadio 是否需要展示Radio.
 * @param radioValue Radio对应的值.
 * @param radioTitle Radio对应的展示信息.
 * @param updateRadioInfo 修改Radio信息的方法.
 * @param radioRuleMessage Radio没填时的报错信息.
 * @return {JSX.Element}
 * @constructor
 */
export const JadeInputTree = (
  {
    data,
    updateItem,
    shapeStatus,
    getOptions = defaultGetOptions,
    onDelete = () => {
    },
    defaultExpandAll = false,
    width = null,
    showRadio = false,
    radioValue,
    radioTitle,
    updateRadioInfo,
    radioRuleMessage,
  }) => {
  const {t} = useTranslation();
  const shape = useShapeContext();
  const form = useFormContext();
  const treeData = data.map(d => convert(d, 0, undefined));
  const [openRadioId, setOpenRadioId] = useState(radioValue);

  useEffect(() => {
    form.setFieldsValue({ [`inputTree-RadioGroup-${shape.id}`]: openRadioId });
  }, [openRadioId, form]);

  const handleRadioChange = (e) => {
    const nodePath = e.target.value;
    setOpenRadioId(nodePath);
    updateRadioInfo(nodePath);
  };

  // 自定义标题展示.
  const displayTitle = (node, sameLevelNodes) => {
    return (<>
      <TreeTitle
        node={node}
        sameLevelNodes={sameLevelNodes}
        updateItem={updateItem}
        shapeStatus={shapeStatus}
        getOptions={getOptions}
        onDelete={onDelete}
        showRadio={showRadio}
        radioTitle={radioTitle}  // 传递状态
      />
    </>);
  };

  // 渲染树节点.
  const renderTreeNodes = (items) => {
    return items.map((item) => {
      if (item.isHidden) {
        return <></>;
      }
      const isRootNode = item.level === 0;
      const className = isRootNode ? 'jade-hide-tree-left-line jade-tree-node' : 'jade-tree-node';
      if (item.children) {
        return (
          <Tree.TreeNode title={displayTitle(item, items)} key={item.key} className={className}>
            {renderTreeNodes(item.children)}
          </Tree.TreeNode>
        );
      }
      return <Tree.TreeNode title={displayTitle(item, items)} key={item.key} className={className}/>;
    });
  };

  return (<>
    {treeData.filter(node => !node.isHidden).length > 0 && <div style={{width: width || 'fit-content'}}>
      <div style={{paddingLeft: '15px'}}>
        <Row>
          {showRadio && <Col flex={`0 0 25px`}/>}
          <Col flex={`0 0 ${INPUT_WIDTH + 15}px`}>
            <span className={'jade-second-title-text'}>{t('fieldName')}</span>
          </Col>
          <Col>
            <span className={'jade-second-title-text'}>{t('fieldValue')}</span>
          </Col>
        </Row>
      </div>
      {showRadio ? (
        <Form.Item
          name={`inputTree-RadioGroup-${shape.id}`}
          rules={[{required: true, message: t(radioRuleMessage)}]} // 必填验证
          initialValue={openRadioId}
        >
          <Radio.Group value={openRadioId ?? undefined} onChange={handleRadioChange}>
            <Tree selectable={false} defaultExpandAll={defaultExpandAll} blockNode className='jade-ant-tree' showLine>
              {renderTreeNodes(treeData)}
            </Tree>
          </Radio.Group>
        </Form.Item>
      ) : (
        <Tree selectable={false} defaultExpandAll={defaultExpandAll} blockNode className='jade-ant-tree' showLine>
          {renderTreeNodes(treeData)}
        </Tree>
      )}
    </div>}
  </>);
};

JadeInputTree.propTypes = {
  data: PropTypes.array.isRequired,
  updateItem: PropTypes.func.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  getOptions: PropTypes.func,
  onDelete: PropTypes.func,
  defaultExpandAll: PropTypes.bool,
  width: PropTypes.number,
};

/**
 * 树title组件.
 *
 * @param node 当前数据.
 * @param sameLevelNodes 同一层级的节点列表.
 * @param updateItem 修改方法.
 * @param shapeStatus 图形状态.
 * @param getOptions 获取options.
 * @param onDelete 删除时触发回调.
 * @param showRadio 是否需要展示单选.
 * @param radioTitle hover时radio的显示信息.
 * @return {JSX.Element}
 * @constructor
 */
const TreeTitle = ({node, sameLevelNodes, updateItem, shapeStatus, getOptions, onDelete, showRadio = false, radioTitle}) => {
  const inputWidth = INPUT_WIDTH - (node.level * LEVEL_DISTANCE);
  const form = useFormContext();
  const {t} = useTranslation();

  /* input变化时的回调. */
  const onInputChange = (key, e) => {
    const value = e.target.value.trim() === '' ? null : e.target.value;
    if (value === null) {
      form.setFieldsValue({[`value-${node.id}`]: null});
    }
    updateItem(node.id, [{key, value}]);
  };

  /* input失去焦点时的回调. */
  const onInputBlur = () => {
    let value = node.value;
    const type = node.type;
    if (type === 'Object' || type === 'Array') {
      try {
        value = JSON.parse(value);
      } catch (error) {
        // 不影响，报错可以继续执行.
      }
    }
    const key = 'value';
    updateItem(node.id, [{key, value}]);
  };

  /**
   * 当reference的value变化时的处理方法.
   *
   * @param referenceKey 键值.
   * @param value 值.
   * @param type 类型.
   */
  const onReferenceValueChange = (referenceKey, value, type) => {
    updateItem(node.id, [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {key: 'type', value: type}]);
  };

  /* 当reference的key变化时的处理方法. */
  const onReferenceKeyChange = (e) => {
    updateItem(node.id, [{key: 'referenceNode', value: e.referenceNode},
      {key: 'referenceId', value: e.referenceId},
      {key: 'referenceKey', value: e.referenceKey},
      {key: 'value', value: e.value},
      {key: 'type', value: e.type}]);
  };

  /* 名称变化时触发. */
  const onNameChange = (value) => {
    updateItem(node.id, [{key: 'name', value}]);
  };

  /* 提示. */
  const getToolTip = () => {
    return {
      title: node.title,
      color: 'white',
      overlayInnerStyle: {fontSize: '12px', color: 'rgb(128, 128, 128)', background: 'white'},
    };
  };

  /* 校验. */
  const validate = (v) => {
    const otherValues = sameLevelNodes.filter(i => i.id !== node.id).map(i => i.name);
    if (otherValues.includes(v)) {
      return Promise.reject(new Error(t('attributeNameMustBeUnique')));
    }
    return Promise.resolve();
  };

  /* 获取名称field. */
  const getNameField = (fieldWidth) => {
    if (node.editable) {
      return (<>
        <JadeFieldName id={node.id}
                       name={node.title}
                       onNameChange={onNameChange}
                       shapeStatus={shapeStatus}
                       style={{paddingRight: '12px', maxWidth: fieldWidth - 15}}
                       validator={validate}/>
      </>);
    }
    return (<>
      <JadeFormText id={node.id}
                    title={node.title}
                    toolTip={getToolTip()}
                    maxWidth={fieldWidth - 15}/>
    </>);
  };

  /* 获取值field. */
  const getValueField = (tNode) => {
    if (tNode.from === FROM_TYPE.INPUT) {
      return <Form.Item
        id={`value-${tNode.id}`}
        name={`value-${tNode.id}`}
        rules={tNode.isRequired ? [{required: true, message: t('fieldValueCannotBeEmpty')}] : []}
        initialValue={tNode.value}
        validateTrigger='onBlur'
      >
        <JadeInput
          disabled={shapeStatus.disabled}
          className='jade-input'
          style={{borderRadius: '0px 8px 8px 0px'}}
          placeholder={t('plsEnter')}
          value={tNode.value}
          onChange={(e) => onInputChange('value', e)}
          onBlur={() => onInputBlur()}
        />
      </Form.Item>;
    } else if (tNode.from === FROM_TYPE.REFERENCE) {
      return <JadeReferenceTreeSelect
        className='jade-input-tree-title-tree-select jade-select'
        disabled={shapeStatus.referenceDisabled}
        rules={tNode.isRequired ? [{
          required: true,
          message: t('fieldValueCannotBeEmpty'),
        }] : []}
        reference={tNode}
        onReferencedKeyChange={(e) => onReferenceKeyChange(e)}
        onReferencedValueChange={(referenceKey, value, type) => onReferenceValueChange(referenceKey, value, type)}
        width={100}
      />;
    } else {
      return null;
    }
  };

  /* 获取删除按钮. */
  const getDeleteIcon = (tNode) => {
    if (!tNode.editable) {
      return null;
    }
    return (<>
      <Col style={{paddingLeft: 5}}>
        <Form.Item id={`delete-${tNode.id}`} name={`delete-${tNode.id}`}>
          <Button disabled={shapeStatus.disabled}
                  type='text'
                  className='icon-button'
                  style={{height: '100%'}}
                  onClick={() => onDelete(tNode.id)}
          >
            <MinusCircleOutlined/>
          </Button>
        </Form.Item>
      </Col>
    </>);
  };

  return (<>
    <div className='jade-input-tree-title'>
      <Row wrap={false}>
        {/* 只有 showSwitch 为 true 时才渲染 Switch */}
        {showRadio && (
          <Col flex="0 0 20px">
            <Popover content={t(radioTitle)}>
              <Radio value={node.path} />
            </Popover>
          </Col>
        )}
        <Col flex={`0 0 ${inputWidth}px`} style={{width: inputWidth}}>
          <div className={'jade-input-tree-title-child'}>
            {getNameField(node, inputWidth)}
          </div>
        </Col>
        <Col flex='0 0 70px' style={{paddingRight: 0}}>
          <div className='jade-input-tree-title-child'>
            <Form.Item name={`value-select-${node.id}`}>
              <JadeInputTreeSelect node={node}
                                   options={getOptions(node)}
                                   updateItem={updateItem}
                                   disabled={shapeStatus.disabled || node.editable === false}/>
            </Form.Item>
          </div>
        </Col>
        <Col style={{width: 100}}>
          <div className='jade-input-tree-title-child'>
            {getValueField(node)}
          </div>
        </Col>
        {getDeleteIcon(node)}
      </Row>
    </div>
  </>);
};

TreeTitle.propTypes = {
  node: PropTypes.object.isRequired,
  sameLevelNodes: PropTypes.array.isRequired,
  updateItem: PropTypes.func.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  getOptions: PropTypes.func,
  onDelete: PropTypes.func,
};

/**
 * 适配JadeInputTree的select框.
 *
 * @param node 节点.
 * @param options 可选项.
 * @param updateItem 修改item的方法.
 * @param disabled 禁用.
 * @return {JSX.Element}
 * @constructor
 */
export const JadeInputTreeSelect = ({node, options, updateItem, disabled}) => {
  const form = useFormContext();
  const {t} = useTranslation();

  /**
   * 处理选择变化事件.
   *
   * @param v 变化后的值.
   */
  const handleItemChange = (v) => {
    if (v === FROM_TYPE.EXPAND) {
      updateItem(node.id, [{key: 'from', value: v},
        {key: 'referenceNode', value: null},
        {key: 'referenceId', value: null},
        {key: 'referenceKey', value: null},
        {key: 'value', value: node.props},
      ]);
    } else if (v === FROM_TYPE.INPUT) {
      form.setFieldsValue({[`value-${node.id}`]: null});
      updateItem(node.id, [{key: 'from', value: v},
        {key: 'referenceNode', value: null},
        {key: 'referenceId', value: null},
        {key: 'referenceKey', value: null},
        {key: 'value', value: null},
        {key: 'type', value: DATA_TYPES.STRING},
      ]);
    } else {
      updateItem(node.id, [{key: 'from', value: v}]);
    }
  };

  return (<>
    <JadeStopPropagationSelect
      style={{background: '#f7f7f7', width: '100%'}}
      disabled={disabled}
      placeholder={t('pleaseSelect')}
      defaultValue={node.from}
      className={'jade-input-tree-title-select jade-select'}
      onChange={handleItemChange}
      options={options}
    />
  </>);
};

JadeInputTreeSelect.propTypes = {
  node: PropTypes.object.isRequired,
  options: PropTypes.array.isRequired,
  updateItem: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
};
