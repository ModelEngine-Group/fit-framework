/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Button, Collapse} from 'antd';
import {useTranslation} from 'react-i18next';
import SearchConfigIcon from '../asserts/icon-search-args-config.svg?react';
import PropTypes from 'prop-types';
import './knowledge.css';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {configToStruct, getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {DEFAULT_KNOWLEDGE_REPO_GROUP} from '@/common/Consts.js';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

/**
 * 搜索参数设置表单.
 *
 * @param option 搜索数据.
 * @param groupId 知识库组id.
 * @param shapeStatus 图形状态.
 * @return {JSX.Element} 组件.
 * @constructor
 */
export const _SearchForm = ({option, groupId, shapeStatus}) => {
  const {t} = useTranslation();
  const text = 'searchArgsConfig';
  const dispatch = useDispatch();
  const shape = useShapeContext();

  const onChange = (checked) => {
    dispatch({
      actionType: 'updateOption', option: {
        rerankParam: {
          enableRerank: checked,
        },
      },
    });
  };

  const onClick = (e) => {
    e.preventDefault();
    shape.page.triggerEvent({
      type: 'KNOWLEDGE_SEARCH_ARGS_EVENT',
      value: {
        options: configToStruct(option),
        groupId: groupId ?? DEFAULT_KNOWLEDGE_REPO_GROUP,
        callback: (callbackData) => {
          dispatch({
            actionType: 'updateOption',
            option: callbackData,
          });
        },
      },
    });
    e.stopPropagation(); // 阻止事件冒泡
  };

  const getContent = () => {
    const indexTypeName = getConfigValue(option, ['indexType', 'name']);
    const referenceLimitType = getConfigValue(option, ['referenceLimit', 'type']);
    const referenceLimitValue = getConfigValue(option, ['referenceLimit', 'value']);
    const similarityThreshold = getConfigValue(option, ['similarityThreshold']);
    return (<>
      <div className={'search-args-config-form-content'}>
        <div className={'search-args-config-form-column'}>
          <span className="jade-font-size jade-font-color">{indexTypeName}</span>
        </div>
        <div className={'search-args-config-form-column'}>
                    <span className="jade-font-size jade-font-color">
                        {referenceLimitType && referenceLimitValue ?
                          `${referenceLimitType}:${referenceLimitValue}` : ''}
                    </span>
        </div>
        <div className={'search-args-config-form-column'}>
          <span className="jade-font-size jade-font-color">{similarityThreshold}</span>
        </div>
      </div>
    </>);
  };

  return (<>
    <JadeCollapse defaultActiveKey={['inputPanel']}>
      {
        <Panel key={'inputPanel'}
               header={<>
                 <div style={{display: 'flex', alignItems: 'center'}}>
                   <span className="jade-panel-header-font">{t(text)}</span>
                   <Button disabled={shapeStatus.disabled}
                           style={{padding: 0, height: 22}}
                           type="text"
                           icon={<SearchConfigIcon/>}
                           onClick={onClick}/>
                 </div>
               </>}
               className="jade-panel"
        >
          <div className={'search-args-config-form'}>
            <div className={'search-args-config-form-row'}>
              <div className={'search-args-config-form-header'}>
                <div className={'search-args-config-form-column'}>
                  <span className="jade-font-size jade-font-color">{t('retrievalIndexType')}</span>
                </div>
                <div className={'search-args-config-form-fence'}/>
                <div className={'search-args-config-form-column'}>
                  <span className="jade-font-size jade-font-color">{t('referenceLimit')}</span>
                </div>
                <div className={'search-args-config-form-fence'}/>
                <div className={'search-args-config-form-column'}>
                  <span className="jade-font-size jade-font-color">{t('similarityThreshold')}</span>
                </div>
                <div className={'search-args-config-form-fence'}/>
              </div>
              {option.value.length > 0 && getContent()}
            </div>
          </div>
        </Panel>
      }
    </JadeCollapse>
  </>);
};

_SearchForm.propTypes = {
  option: PropTypes.object,
  groupId: PropTypes.string,
  shapeStatus: PropTypes.object,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.option === nextProps.option &&
    prevProps.groupId === nextProps.groupId &&
    prevProps.shapeStatus === nextProps.shapeStatus;
};

export const SearchForm = React.memo(_SearchForm, areEqual);