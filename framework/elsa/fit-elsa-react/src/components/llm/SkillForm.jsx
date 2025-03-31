/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Collapse, Form, Row} from 'antd';
import {PlusOutlined} from '@ant-design/icons';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import '../common/style.css';
import PropTypes from 'prop-types';
import React from 'react';
import DeleteItem from '../asserts/icon-tool-delete.svg?react';
import ToolIcon from '../asserts/icon-tool.svg?react';
import WorkflowIcon from '../asserts/icon-workflow.svg?react';
import {useTranslation} from 'react-i18next';
import {TOOL_TYPE} from '@/common/Consts.js';

const {Panel} = Collapse;

/**
 * 大模型节点技能表单。
 *
 * @param toolOptions 工具选项。
 * @param disabled 是否禁用.
 * @returns {JSX.Element} 大模型节点技能表单的DOM。
 */
const _SkillForm = ({toolOptions, disabled}) => {
  const dispatch = useDispatch();
  const shape = useShapeContext();
  const {t} = useTranslation();

  /**
   * 添加添加技能的回调函数
   *
   * @param event 事件
   */
  const onAddSkillClick = event => {
    event.preventDefault();
    shape.page.triggerEvent({
      type: 'SELECT_SKILL',
      value: {
        onSelect: onSelect,
      },
    });
    event.stopPropagation();
  };

  /**
   * 选择工具后的回调
   *
   * @param data 目前是uniqueName
   */
  const onSelect = (data) => {
    dispatch({type: 'addSkill', value: data});
  };

  return (
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={['skillPanel']}>
      {
        <Panel
          key={'skillPanel'}
          header={
            <div className='panel-header' style={{display: 'flex', alignItems: 'center', justifyContent: 'flex-start'}}>
              <span className='jade-panel-header-font'>{t('skill')}</span>
              <Button disabled={disabled}
                      type='text' className='icon-button jade-panel-header-icon-position'
                      onClick={(event) => {
                        onAddSkillClick(event);
                      }}>
                <PlusOutlined/>
              </Button>
            </div>
          }
          className='jade-panel'
        >
          <SkillContent toolOptions={toolOptions} disabled={disabled} dispatch={dispatch} t={t}/>
        </Panel>
      }
    </Collapse>
  );
};

_SkillForm.propTypes = {
  toolOptions: PropTypes.array.isRequired, // 确保 toolOptions 是一个必需的array类型
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.toolOptions === nextProps.toolOptions &&
    prevProps.disabled === nextProps.disabled;
};

/**
 * 技能内容区域组件
 *
 * @param toolOptions 添加的工具详情
 * @param disabled 是否禁用
 * @param dispatch 修改jadeConfig的方法
 * @param t 国际化方法
 * @returns {React.JSX.Element} 技能内容区域组件
 * @constructor
 */
export const SkillContent = ({toolOptions, disabled, dispatch, t}) => {
  const shape = useShapeContext();

  /**
   * 根据标签类型返回不同的Icon
   *
   * @param tags 工具标签
   */
  const getIcon = (tags) => {
    if (tags.includes(TOOL_TYPE.WATER_FLOW)) {
      return WorkflowIcon;
    } else {
      return ToolIcon;
    }
  };

  /**
   * 获取工具类型
   *
   * @param tags 工具标签
   * @returns {{}} 类型
   */
  const getType = (tags) => {
    if (tags.includes(TOOL_TYPE.WATER_FLOW)) {
      return 'workflow';
    } else {
      return 'tool';
    }
  };

  /**
   * 删除工具
   *
   * @param itemId 知识库id
   */
  const handleDelete = (itemId) => {
    dispatch({type: 'deleteTool', value: itemId});
  };

  /**
   * 渲染删除按钮
   *
   * @param item 条目，具体是一个工具的对象
   * @returns {React.JSX.Element} 删除按钮
   */
  const renderDeleteIcon = (item) => {
    return (<>
      <Button disabled={disabled}
              type='text'
              icon={<DeleteItem/>}
              className='tool-delete-btn'
              onClick={() => handleDelete(item.value)}/>
    </>);
  };

  /**
   * 获取技能项的校验结果
   *
   * @param item
   * @returns {(function(): (Promise<never>))|*}
   */
  const getValidatorOfSkill = (item) => {
    return () => {
      const validateInfo = shape.graph.validateInfo?.find(node => node?.nodeId === shape.id);
      if (!(validateInfo?.isValid ?? true)) {
        const isPluginInvalid = validateInfo.configChecks?.find(configCheck => configCheck.configName === 'plugin' && configCheck.uniqueName === item.value);

        if (isPluginInvalid) {
          return Promise.reject(new Error(`${item.name} ${t('selectedValueNotExist')}`));
        }
      }
      return Promise.resolve();
    };
  };

  return (<>
    <div className={'tool-custom-collapse-content'}>
      {toolOptions
        // 历史数据/模板中知识库自带一个空数组的object结构体，这里不需要渲染这个所以加上此条件
        .filter(item => item.name).map((item) => {
          // 动态获取图标
          const IconComponent = getIcon(item.tags);
          return (<>
            <Form.Item
              className='jade-form-item'
              name={`skill-${item.id}`}
              rules={[{
                validator: getValidatorOfSkill(item),
              }]}
              validateTrigger='onBlur'
            >
              <Row key={`tools-${item.id}`}>
                <div className={'tool-item item-hover'}>
                  <div className={'tool-item-content-wrapper'}>
                    <IconComponent class={'tool-icon-style'}/>
                    <div className={'tool-name'} title={`${item.name}(${t(getType(item.tags))})`}>
                      {`${item.name}(${t(getType(item.tags))})`}
                    </div>
                    <div className={'tool-version-wrapper'}>
                      <span className={'tool-version-font'}>{item.version}</span>
                    </div>
                  </div>
                  {renderDeleteIcon(item)}
                </div>
              </Row>
            </Form.Item>
          </>);
        })}
    </div>
  </>);
};

SkillContent.propTypes = {
  toolOptions: PropTypes.array.isRequired, // 确保 toolOptions 是一个必需的array类型
  disabled: PropTypes.bool.isRequired,
  dispatch: PropTypes.func.isRequired,
  t: PropTypes.func.isRequired,
};

export const SkillForm = React.memo(_SkillForm, areEqual);