/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Col, Collapse, Form, Popover, Row, Tree} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import React, {useEffect, useState} from 'react';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import {useDispatch, useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import AddSubItem from '../asserts/icon-add-subitem.svg?react';
import DeleteItem from '../asserts/icon-delete.svg?react';
import TreeSwitcherIcon from '@/components/common/TreeSwitcherIcon.jsx';
import {DATA_TYPES} from '@/common/Consts.js';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import {convertToTreeData, findChildIds} from '@/components/util/JadeConfigUtils.js';
import {removeFirstLevelLine} from '@/components/util/CssUtil.js';

const {Panel} = Collapse;

_JadeObservableOutput.propTypes = {
  disabled: PropTypes.bool,
  output: PropTypes.object,
};

/**
 * code节点输出组件
 *
 * @param disabled 是否禁用
 * @param output 输出数据
 * @return {JSX.Element}
 * @constructor
 */
function _JadeObservableOutput({disabled, output}) {
  const shape = useShapeContext();
  const dispatch = useDispatch();
  const {t} = useTranslation();
  const [outputTreeData, setOutputTreeData] = useState(() => convertOutputToTreeData());
  const form = useFormContext();
  // 组件初始化时注册observable.
  useEffect(() => {
    // 组件unmount时，删除observable.
    return () => {
      shape.page.removeObservable(shape.id);
    };
  }, []);

  useEffect(() => {
    setOutputTreeData(convertOutputToTreeData());
  }, [output]);

  const content = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='codeOutputPopover' components={{p: <p/>}}/>
  </div>);

  /**
   * 把输出的jadeConfig数据转换为treeData
   *
   * @returns {{}[]} treeData
   */
  function convertOutputToTreeData() {
    return [convertToTreeData(output, 1, null, (data, parent) => {
      shape.page.registerObservable({
        nodeId: shape.id,
        observableId: data.id,
        value: data.name,
        type: data.type,
        parentId: parent ? parent.id : null,
      });
    })];
  }

  /**
   * 渲染tree数据
   *
   * @param node 节点数据
   * @return {JSX.Element}
   */
  const renderTreeNode = (node) => {
    return <TreeNode
      node={node}
      disabled={disabled}
      shape={shape}
      dispatch={dispatch}
      form={form}
      output={output}
      showDescription={false}
      treeData={outputTreeData}
    />;
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse' defaultActiveKey={['codeOutputPanel']}>
      {<Panel key={'codeOutputPanel'}
              header={<div className='panel-header'>
                <span className='jade-panel-header-font'>{t('output')}</span>
                <Popover
                  content={content}
                  align={{offset: [0, 3]}}
                  overlayClassName={'jade-custom-popover'}
                >
                  <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
                </Popover>
              </div>}
              className='jade-panel'
      >
        <Tree blockNode={true}
              switcherIcon={({expanded}) => <TreeSwitcherIcon expanded={expanded}/>}
              showLine
              defaultExpandAll
              treeData={outputTreeData}
              titleRender={renderTreeNode}
        />
      </Panel>}
    </Collapse>
  </>);
}

/**
 * 输出树状展示组件
 *
 * @param node 节点数据
 * @param disabled 是否禁用
 * @param shape 图形
 * @param showDescription 是否展示字段描述信息
 * @param editOutputFieldProperty 更新参数
 * @param treeData 节点所在树数据
 * @return {JSX.Element} 组件
 * @constructor
 */
export const TreeNode = ({
  node,
  disabled,
  dispatch,
  form,
  shape,
  showDescription,
  treeData,
  output,
  }) => {
  const dataTypes = Object.values(DATA_TYPES);
  const keyWidth = showDescription ? 100 : 140;
  const typeWidth = showDescription ? 80 : 100;
  const inputWidth = keyWidth - ((node.level - 1) * 24);
  const {key, title, type, description, level, children} = node;
  const {t} = useTranslation();
  // 输出的最大层数是4
  const maxLevel = 4;

  // 去掉第一层级属性组件左边的虚线
  useEffect(() => {
    removeFirstLevelLine();
  }, [treeData]);

  /**
   * 处理输入发生变化的动作
   *
   * @param id id
   * @param eventType 事件类型
   * @param changes 需要修改的属性
   */
  const handleItemChange = (id, eventType, changes) => {
    dispatch({actionType: eventType, id: id, changes: changes});
  };

  /**
   * 更新input的属性
   *
   * @param id id
   * @param eventType 事件类型
   * @param property 属性名
   * @param e event事件
   */
  const editOutputFieldProperty = (id, eventType, property, e) => {
    handleItemChange(id, eventType, [{key: property, value: e.target.value}]);
    form.setFieldsValue({[`${property}-${shape.id}-${id}`]: e.target.value});
  };

  /**
   * 切换属性类型
   *
   * @param id id
   * @param eventType 事件类型
   * @param value 目标值
   */
  const editOutputType = (id, eventType, value) => {
    const _buildChanges = () => {
      const changes = [{key: 'type', value: value}];
      if (value === 'Object') {
        changes.push({key: 'value', value: []});
      } else {
        findChildIds([output], id).filter(childId => childId !== id).forEach(childId => {
          shape.page.removeObservable(shape.id, childId);
        });
        changes.push({key: 'value', value: ''});
      }
      return changes;
    };

    const changes = _buildChanges();
    handleItemChange(id, eventType, changes);
    form.setFieldsValue({[`value-select-${shape.id}-${id}`]: value});
  };

  /**
   * 添加子项
   *
   * @param itemKey 需要添加子项的父项的id
   */
  const handleAddSubItem = (itemKey) => {
    // 代码节点出参每层最大宽度为20
    if (children.length < 20) {
      dispatch({actionType: 'addSubItem', id: itemKey});
    }
  };

  /**
   * 删除一项
   *
   * @param itemKey 需要删除的数据id
   */
  const handleDelete = itemKey => {
    findChildIds(output.value, itemKey).forEach(id => {
      shape.page.removeObservable(shape.id, id);
    });
    dispatch({actionType: 'deleteRow', id: itemKey});
  };

  /**
   * 是否不可被选择
   *
   * @param dataType 数据类型
   * @return {boolean} 是否不可别选择
   */
  const isTypeDisable = (dataType) => {
    return level === maxLevel && dataType === 'Object';
  };

  const isObjectType = dataType => dataType === 'Object';
  const options = dataTypes.map(dataType => ({
    value: dataType, label: dataType, disabled: disabled || isTypeDisable(dataType),
  }));

  /**
   * 获取最后一列的组件
   *
   * @param dataLevel 数据层级
   * @return {JSX.Element} 按钮或者空
   */
  const getDeleteButton = (dataLevel) => {
    return dataLevel > 1 ? <Button
      type='text'
      icon={<DeleteItem/>}
      disabled={disabled}
      onClick={() => handleDelete(key)}/> : <></>;
  };

  /**
   * 根据是否展示字符按描述标志位，返回不同的校验规则
   *
   * @param isShowDescription
   * @returns 校验规则
   */
  const getDescriptionRules = (isShowDescription) => {
    return isShowDescription ? [{required: true, message: t('fieldDescriptionCannotBeEmpty')}] : [];
  };

  /**
   * 是否展示字段描述组件
   *
   * @param dataLevel 数据层级（第一次不展示字段描述input输入框）
   * @return {JSX.Element} 字段描述组件或者空白区域
   */
  const getDescription = (dataLevel) => {
    return (dataLevel > 1 && showDescription) ? <JadeInput
      value={description}
      id={`description-input-${node.id}-${key}`}
      className='jade-input'
      disabled={disabled}
      onBlur={(e) => editOutputFieldProperty(key, 'editOutputFieldProperty', 'description', e)}
    /> : <div style={{width: '50px'}}></div>;
  };

  /**
   * 寻找兄弟姐妹节点
   *
   * @param nodeKey 被找节点的唯一标识
   * @param nodeLevel 数据层级
   * @param treeDataInfo 需要寻找兄弟姐妹节点的树数据
   * @return {JSX.Element} 按钮或者空
   */
  const findSiblings = (nodeKey, nodeLevel, treeDataInfo) => {
    for (const treeNode of treeDataInfo) {
      // 若当前节点的层级比需要找兄弟节点的父节点层级更深，则剪枝
      if (treeNode.level >= nodeLevel) {
        return [];
      }

      // 检查当前节点是否是目标节点的父节点
      if (treeNode.children && treeNode.level === nodeLevel - 1) {
        // 查找目标节点是否在当前节点的子节点中
        if (treeNode.children.some(child => child.key === nodeKey)) {
          // 返回所有子节点（即兄弟节点），排除目标节点本身
          return treeNode.children.filter(child => child.key !== nodeKey);
        }
      }

      // 递归查找
      if (treeNode.children && treeNode.children.length > 0) {
        const result = findSiblings(nodeKey, nodeLevel, treeNode.children);
        if (result.length > 0) {
          return result;
        }
      }
    }
    return [];
  };

  return (<>
    <Row align='middle' wrap={false}>
      <Col flex={`0 0 ${inputWidth}px`} style={{marginRight: '8px', alignSelf: 'normal'}}>
        <Form.Item
          name={`property-${shape.id}-${key}`}
          id={`property-${node.id}-${key}`}
          rules={[
            {required: true, message: t('fieldValueCannotBeEmpty')}, {
              pattern: /^[^\s]*$/,
              message: t('spacesAreNotAllowed'),
            }, // 自定义校验函数
            () => ({
              validator(_, value) {
                const siblings = findSiblings(key, level, treeData);
                if (siblings.some(sibling => sibling.title === value)) {
                  return Promise.reject(new Error(t('attributeNameMustBeUnique')));
                }
                return Promise.resolve();
              },
            })]}
          initialValue={title}
        >
          <JadeInput
            className='jade-input'
            id={`property-input-${node.id}-${key}`}
            value={title}
            disabled={level === 1 || disabled}
            onChange={(e) => editOutputFieldProperty(key, 'editOutputFieldProperty', 'name', e)}
          />
        </Form.Item>
      </Col>
      <Col flex={`0 0 ${typeWidth}px`} style={{alignSelf: 'normal'}}>
        <Form.Item
          name={`value-select-${shape.id}-${key}`}
          id={`type-${node.id}-${key}`}
          initialValue={type}
        >
          <JadeStopPropagationSelect
            style={{borderRadius: 4}}
            disabled={disabled}
            onChange={(value) => editOutputType(key, 'editOutputType', value)}
            options={options}
          />
        </Form.Item>
      </Col>
      <Col flex={'0 0 100 px'} style={{alignSelf: 'normal'}}>
        <Form.Item
          name={`description-${shape.id}-${key}`}
          id={`description-${node.id}-${key}`}
          rules={getDescriptionRules(showDescription)}
          initialValue={description}
        >
          {getDescription(level)}
        </Form.Item>
      </Col>
      <Col flex='0 0 30px' style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
        <Button disabled={disabled || !(isObjectType(type))}
                type='text'
                style={{
                  margin: 0, padding: 0, visibility: (isObjectType(type)) ? 'visible' : 'hidden',
                }}
                icon={<AddSubItem/>}
                onClick={() => handleAddSubItem(key)}
        />
      </Col>
      <Col flex='0 0 15px' style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
        {getDeleteButton(level)}
      </Col>
    </Row>
  </>);
};

TreeNode.propTypes = {
  disabled: PropTypes.bool,
  node: PropTypes.object,
  dispatch: PropTypes.func,
  form: PropTypes.object,
  shape: PropTypes.object,
  showDescription: PropTypes.bool,
  treeData: PropTypes.object,
  output: PropTypes.object,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled && prevProps.output === nextProps.output;
};

export const JadeObservableOutput = React.memo(_JadeObservableOutput, areEqual);