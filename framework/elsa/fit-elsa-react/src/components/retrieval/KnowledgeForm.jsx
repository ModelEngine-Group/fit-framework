/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useState} from 'react';
import {Button, Collapse, Form, Row} from 'antd';
import {MinusCircleOutlined, PlusOutlined} from '@ant-design/icons';
import {KnowledgeConfig} from '@/components/retrieval/KnowledgeConfig.jsx';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import SearchConfigIcon from '../asserts/icon-search-args-config.svg?react';
import {DEFAULT_KNOWLEDGE_REPO_GROUP} from '@/common/Consts.js';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

/**
 * 知识节点组件
 *
 * @param knowledge 知识库利列表.
 * @param groupId 知识库group.
 * @param knowledgeConfigId 知识库配置的唯一标识.
 * @param disabled 禁用状态.
 * @param maximum 最大值.
 * @param enableGroup 是否启动groupId.
 * @returns {JSX.Element}
 */
const _KnowledgeForm = ({knowledge, groupId, knowledgeConfigId, disabled, maximum = null, enableGroup = true}) => {
  const dispatch = useDispatch();
  const shape = useShapeContext();
  const {t} = useTranslation();
  const [errorItems, setErrorItems] = useState([]);

  /**
   * 删除知识库
   *
   * @param itemId 知识库id
   */
  const handleDelete = (itemId) => {
    dispatch({type: 'deleteKnowledge', id: itemId});
  };

  const renderDeleteIcon = (item) => {
    return (<>
      <Button disabled={disabled}
              type='text'
              className='icon-button'
              style={{height: '100%', marginLeft: 'auto', padding: '0 4px'}}
              onClick={() => handleDelete(item.id)}>
        <MinusCircleOutlined/>
      </Button>
    </>);
  };

  const onSelect = (data) => {
    dispatch({type: 'updateKnowledge', value: data});
  };

  const getSelectedKnowledgeBases = () => {
    return knowledge
      .map(obj => {
        const innerValue = obj.value;
        return innerValue.reduce((acc, curr) => {
          acc[curr.name] = curr.value;
          return acc;
        }, {});
      })
      .filter(result => Object.keys(result).length > 0); // 过滤掉空对象
  };

  const triggerSelect = (e) => {
    e.preventDefault();
    shape.page.triggerEvent({
      type: 'SELECT_KNOWLEDGE_BASE',
      value: {
        shapeId: shape.id,
        selectedKnowledgeBases: getSelectedKnowledgeBases(),
        groupId: groupId ?? DEFAULT_KNOWLEDGE_REPO_GROUP,
        selectedKnowledgeConfigId: knowledgeConfigId,
        onSelect: onSelect,
      },
    });
    e.stopPropagation(); // 阻止事件冒泡
  };

  const triggerRepoGroupSelect = (e) => {
    e.preventDefault();
    shape.page.triggerEvent({
      type: 'SELECT_KNOWLEDGE_BASE_GROUP',
      value: {
        selectedGroupId: groupId ?? DEFAULT_KNOWLEDGE_REPO_GROUP,
        selectedKnowledgeConfigId: knowledgeConfigId,
        onSelect: (repoGroupId, knowledgeConfigId) => {
          dispatch({
            type: 'updateGroupIdAndConfigId',
            value: repoGroupId,
            knowledgeConfigId: knowledgeConfigId,
          });
        },
      },
    });
    e.stopPropagation(); // 阻止事件冒泡
  };

  const getValidatorOfKnowledgeBase = (item) => {
    return () => {
      const validateInfo = shape.graph.validateInfo?.find(node => node?.nodeId === shape.id);
      if (!(validateInfo?.isValid ?? true)) {
        const notExistKnowledgeBases = validateInfo.configChecks?.filter(configCheck => configCheck.configName === 'knowledge');
        const notExistKnowledgeRepos = validateInfo.configChecks?.filter(configCheck => configCheck.configName === 'knowledgeRepos');
        const isSameKnowledgeBase = notExistKnowledgeBases.find(knowledgeBase => {
          const isSameRepoId = knowledgeBase?.repoId && knowledgeBase?.repoId === String(item.value?.find(subItem => subItem.name === 'repoId')?.value);
          const isSameTableId = knowledgeBase?.tableId && knowledgeBase?.tableId === String(item.value?.find(subItem => subItem.name === 'tableId')?.value);
          const isSameName = knowledgeBase?.name && knowledgeBase?.name === item.value?.find(subItem => subItem.name === 'name')?.value;
          return isSameRepoId && isSameTableId && isSameName;
        });
        const isSameKnowledgeRepo = notExistKnowledgeRepos.find(knowledgeBase => {
          const isSameId = knowledgeBase?.id && knowledgeBase?.id === String(item.value?.find(subItem => subItem.name === 'id')?.value);
          const isSameName = knowledgeBase?.name && knowledgeBase?.name === item.value?.find(subItem => subItem.name === 'name')?.value;
          return isSameId && isSameName;
        });
        if (isSameKnowledgeBase || isSameKnowledgeRepo) {
          setErrorItems((prevErrorItems) => [...prevErrorItems, item.id]); // 添加到错误列表
          return Promise.reject(new Error(`${item.value?.find(subItem => subItem.name === 'name')?.value} ${t('selectedValueNotExist')}`));
        }
      }
      setErrorItems((prevErrorItems) => prevErrorItems.filter(id => id !== item.id)); // 移除错误
      return Promise.resolve();
    };
  };

  const getHeader = () => {
    return (<>
      <div
        style={{display: 'flex', alignItems: 'center'}}>
        <span className='jade-panel-header-font'>{t('knowledgeBases')}</span>
        {
          enableGroup && (<>
            <Button disabled={disabled} className={'icon-button'} type='text' onClick={triggerRepoGroupSelect}>
              <SearchConfigIcon/>
            </Button>
          </>)
        }
        <Button
          disabled={disabled}
          type='text' className='icon-button jade-panel-header-icon-position'
          onClick={(event) => triggerSelect(event)}>
          <PlusOutlined/>
        </Button>
      </div>
    </>);
  };

  return (<>
    <JadeCollapse
      style={{marginTop: '10px', marginBottom: 8, borderRadius: '8px', width: '100%'}}
      defaultActiveKey={['Knowledge']}>
      <Panel
        style={{marginBottom: 8, borderRadius: '8px', width: '100%'}}
        header={getHeader()}
        className='jade-panel'
        key='Knowledge'>
        <div className={'jade-custom-panel-content'}>
          <div className={'jade-custom-multi-item-container'}>
            {knowledge
              // 历史数据/模板中知识库自带一个空数组的object结构体，这里不需要渲染这个所以加上此条件
              .filter(item => item.value && item.value.length > 0)
              .map((item) => {
                return (<>
                  <Form.Item
                    className='jade-form-item'
                    name={`knowledge-${item.id}`}
                    rules={[{
                      validator: getValidatorOfKnowledgeBase(item),
                    }]}
                    validateTrigger='onBlur'
                  >
                    <Row key={`knowledgeRow-${item.id}`}>
                      <div className={`jade-custom-multi-select-with-slider-div item-hover ${errorItems.includes(item.id) ? 'jade-error-border' : ''}`}>
                                            <span className={'jade-custom-multi-select-item'}>
                                                {item.value?.find(subItem => subItem.name === 'name')?.value ?? ''}
                                            </span>
                        {renderDeleteIcon(item)}
                      </div>
                    </Row>
                  </Form.Item>
                </>);
              })}
          </div>
          {maximum !== null && <KnowledgeConfig maximum={maximum} disabled={disabled}/>}
        </div>
      </Panel>
    </JadeCollapse>
  </>);
};

_KnowledgeForm.propTypes = {
  knowledge: PropTypes.array.isRequired,
  groupId: PropTypes.string.isRequired,
  knowledgeConfigId: PropTypes.string,
  maximum: PropTypes.number,
  disabled: PropTypes.bool,
  enableGroup: PropTypes.bool,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.knowledge === nextProps.knowledge &&
    prevProps.groupId === nextProps.groupId &&
    prevProps.knowledgeConfigId === nextProps.knowledgeConfigId &&
    prevProps.maximum === nextProps.maximum &&
    prevProps.enableGroup === nextProps.enableGroup &&
    prevProps.disabled === nextProps.disabled;
};

export const KnowledgeForm = React.memo(_KnowledgeForm, areEqual);