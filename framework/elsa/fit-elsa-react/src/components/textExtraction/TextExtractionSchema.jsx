/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDispatch, useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import {Trans, useTranslation} from 'react-i18next';
import {convertToTreeData} from '@/components/util/JadeConfigUtils.js';
import React, {useEffect, useState} from 'react';
import {TreeNode} from '@/components/common/JadeObservableOutput.jsx';
import {Button, Col, Collapse, Form, Popover, Row, Tree} from 'antd';
import {PlusOutlined, QuestionCircleOutlined} from '@ant-design/icons';
import TreeSwitcherIcon from '@/components/common/TreeSwitcherIcon.jsx';
import PropTypes from 'prop-types';

const {Panel} = Collapse;

/**
 * 变量提取模块header组件
 *
 * @param t 国际化组件
 * @param disabled 是否禁用
 * @param handleAddSubItem 添加子项回调
 * @param output 输出对象
 * @returns {React.JSX.Element} 变量提取模块header组件
 * @constructor
 */
const TextExtractionHeader = ({t, disabled, handleAddSubItem, output, outputTreeData}) => {
  const shape = useShapeContext();
  const dispatch = useDispatch();
  const content = <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='TextExtractionSchema' components={{p: <p/>}}/>
  </div>;

  /**
   * 点击从工具导入按钮
   */
  const handleImportClick = (e) => {
    e.stopPropagation();
    shape.page.triggerEvent({
      type: 'SELECT_TOOL',
      value: {
        onSelect: onSelect,
      },
    });
  };

  // 选择了model之后的回调.
  const onSelect = (data) => {
    // 先删除当前shape之前所有注册的引用
    shape.page.removeObservable(shape.id);
    dispatch({actionType: 'changeTool', value: data});
  };

  /**
   * 添加子项
   *
   * @param event 事件
   */
  const onAddSubItemClick = (event) => {
    handleAddSubItem(output.id, outputTreeData[0].children);
    event.stopPropagation();
  };

  return (<>
    <div className='schema-panel-header'>
      <div className={'schema-button-wrapper'}>
        <span className='jade-panel-header-font'>{t('extractVariables')}</span>
        <Popover
          content={content}
          align={{offset: [0, 3]}}
          overlayClassName={'jade-custom-popover'}
        >
          <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
        </Popover>
      </div>

      <div className={'schema-button-wrapper'}>
        <Button type='link'
                className={'button-import'}
                onClick={handleImportClick}
                disabled={disabled}
        >
          <div className={'import-button-text'}>{t('importTools')}</div>
        </Button>
        <div style={{
          width: '1px',
          height: '10px',
          flexGrow: 0,
          flex: 'none',
          background: 'rgb(217, 217, 217)',
          margin: '0 10px 0 8px',
        }}></div>
        <Button disabled={disabled}
                type='link' className='icon-button text-schema-header-icon-position'
                onClick={(event) => {
                  onAddSubItemClick(event);
                }}>
          <PlusOutlined/>
        </Button>
      </div>
    </div>
  </>);
};

TextExtractionHeader.propTypes = {
  t: PropTypes.func.isRequired,
  handleAddSubItem: PropTypes.func.isRequired,
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
const _TextExtractionSchema = ({disabled, output}) => {
  const shape = useShapeContext();
  const dispatch = useDispatch();
  const {t} = useTranslation();
  const [outputTreeData, setOutputTreeData] = useState(() => [convertToTreeData(output, 1, null, null)]);
  const form = useFormContext();

  useEffect(() => {
    // 组件unmount时，删除observable.
    return () => {
      shape.page.removeObservable(shape.id);
    };
  }, []);

  useEffect(() => {
    setOutputTreeData([convertToTreeData(output, 1, null, null)]);
  }, [output]);

  /**
   * 渲染tree数据
   *
   * @param node 节点数据
   * @return {JSX.Element}
   */
  const renderTreeNode = (node) => {
    const className = node.level === 2 && node.children.length === 0 ? 'first-level' : 'not-first';
    return <span className={className}>
      <TreeNode
        node={node}
        disabled={disabled}
        shape={shape}
        showDescription={true}
        treeData={outputTreeData}
        output={output}
        dispatch={dispatch}
        form={form}
      /></span>;
  };

  /**
   * 添加子项
   *
   * @param key 需要添加子项的父项的id
   * @param children 当前数据项的子项
   */
  const handleAddSubItem = (key, children) => {
    // 代码节点出参每层最大宽度为20
    if (children.length < 20) {
      dispatch({actionType: 'addSubItem', id: key});
    }
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse' defaultActiveKey={['textExtractionInputPanel']}>
      {<Panel key={'textExtractionInputPanel'}
              header={<TextExtractionHeader output={output} t={t} disabled={disabled}
                                            handleAddSubItem={handleAddSubItem} outputTreeData={outputTreeData}/>}
              className='jade-panel'
      >
        <VariableInfo t={t}/>
        <Tree blockNode={true}
              switcherIcon={({expanded}) => <TreeSwitcherIcon expanded={expanded}/>}
              showLine
              defaultExpandAll
              treeData={outputTreeData[0].children}
              titleRender={renderTreeNode}
        />
      </Panel>}
    </Collapse>
  </>);
};

_TextExtractionSchema.propTypes = {
  disabled: PropTypes.bool,
  output: PropTypes.object,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled && prevProps.output === nextProps.output;
};

/**
 * 变量信息组件，包含变量名、变量类型和变量描述三个字段
 *
 * @param t 国际化组件
 * @returns {React.JSX.Element} 变量信息组件
 * @constructor
 */
const VariableInfo = ({t}) => {
  return (<>
    <Row gutter={16}>
      <Col span={8}>
        <Form.Item>
          <span style={{paddingLeft: '30px'}}
                className='jade-red-asterisk-tex jade-font-size jade-font-color'>
            {t('variableName')}
          </span>
        </Form.Item>
      </Col>
      <Col span={8}>
        <Form.Item>
          <span className='jade-red-asterisk-tex jade-font-size jade-font-color'>
            {t('variableType')}
          </span>
        </Form.Item>
      </Col>
      <Col span={8}>
        <Form.Item>
              <span style={{marginLeft: '-30px'}}
                    className='jade-red-asterisk-tex jade-font-size jade-font-color'>
                {t('variableDescription')}
              </span>
        </Form.Item>
      </Col>
    </Row>
  </>);
};

VariableInfo.propTypes = {
  t: PropTypes.func,
};

export const TextExtractionSchema = React.memo(_TextExtractionSchema, areEqual);
