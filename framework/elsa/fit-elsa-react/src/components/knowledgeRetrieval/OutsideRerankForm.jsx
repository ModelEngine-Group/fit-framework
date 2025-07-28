/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useState} from 'react';
import {Collapse, Form, Slider, Spin, Switch} from 'antd';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';
import './knowledge.css';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import httpUtil from '@/components/util/httpUtil.jsx';

const {Panel} = Collapse;

/**
 * 重排参数设置表单.
 *
 * @param option 搜索数据.
 * @param shapeStatus 图形状态.
 * @return {JSX.Element} 组件.
 * @constructor
 */
export const OutsideRerankForm = ({option, shapeStatus}) => {
  const {t} = useTranslation();
  const text = 'rerankConfig';
  const dispatch = useDispatch();
  const shape = useShapeContext();
  let config;
  if (!shape || !shape.graph || !shape.graph.configs) {
    // 没关系，继续.
  } else {
    config = shape.graph.configs.find(node => node.node === 'knowledgeRetrievalNodeState');
  }
  const topK = getConfigValue(option, ['referenceLimit', 'value'], 'value');
  const enableRerank = getConfigValue(option, ['rerankParam', 'enableRerank'], 'value');
  const model = getConfigValue(option, ['rerankParam', 'model'], 'value');
  const topN = getConfigValue(option, ['rerankParam', 'topN'], 'value');
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(false);

  const handleAccessInfoChange = (serviceName, tag) => {
    dispatch({type: 'changeAccessInfo', serviceName: serviceName, tag: tag});
  };

  const handleRerankParamChange = (name, e) => {
    dispatch({type: 'changeRerankParam', name: name, value: e});
  };

  const loadOptions = async () => {
    setLoading(true);
    try {
      httpUtil.get(`${config.urls.llmModelEndpoint}/fetch/model-list?type=rerank`, new Map(), (jsonData) => setOptions(jsonData.models.map(item => {
        return {
          value: item.serviceName,
          label: item.serviceName,
          title: t(item.tag),
          tag: item.tag,
        };
      })));
      setHasLoaded(true);
    } catch (error) {
      console.error('加载选项失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDropdownVisibleChange = (open) => {
    if (open && !hasLoaded && !loading) {
      loadOptions();
    }
  };

  return (<>
    <JadeCollapse defaultActiveKey={['inputPanel']}>
      {
        <Panel key={'inputPanel'}
               header={<>
                 <div style={{display: 'flex', alignItems: 'center'}}>
                   <span className="jade-panel-header-font">{t(text)}</span>
                 </div>
               </>}
               className="jade-panel"
        >
          <div style={{display: 'flex', flexDirection: 'column', gap: '4px'}}>
            <Form.Item
              className="jade-form-item"
              label={t('whetherRerank')}
              name={`whetherRerank-${option.id}`}
              rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
              validateTrigger="onBlur"
              initialValue={enableRerank}
            >
              <Switch style={{marginLeft: '4px', width: '40px'}}
                      disabled={shapeStatus.disabled}
                      onChange={(e) => handleRerankParamChange('enableRerank', e)}
                      checked={enableRerank}></Switch>
            </Form.Item>
          </div>
          {enableRerank && <div style={{display: 'flex', flexDirection: 'column', gap: '4px'}}>
            <Form.Item
              className="jade-form-item"
              label={t('rerankModel')}
              name={`rerankModel-${option.id}`}
              rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
              validateTrigger="onBlur"
              initialValue={model}
            >
              <JadeStopPropagationSelect
                disabled={shapeStatus.disabled}
                placeholder={t('pleaseSelect')}
                loading={loading}
                onDropdownVisibleChange={handleDropdownVisibleChange}
                onChange={(value, option) => {
                  handleAccessInfoChange(value, option.tag);
                }}
                notFoundContent={loading ? <Spin size="small"/> : t('noContent')}
                options={options}
              />
            </Form.Item>
          </div>}
          {enableRerank && <div style={{display: 'flex', flexDirection: 'column', gap: '4px'}}>
            <Form.Item
              className="jade-form-item"
              label={t('topN')}
              name={`topN-${option.id}`}
              rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
              validateTrigger="onBlur"
              initialValue={topN}
            >
              <Slider style={{width: '95%'}} // 设置固定宽度
                      min={1}
                      max={topK}
                      disabled={shapeStatus.disabled}
                      defaultValue={3}
                      marks={{[1]: 1, [topK]: topK}}
                      step={1} // 设置步长为1
                      onChange={(value) => handleRerankParamChange('topN', value)}
                      value={topN}/>
            </Form.Item>
          </div>}
        </Panel>
      }
    </JadeCollapse>
  </>);
};

OutsideRerankForm.propTypes = {
  option: PropTypes.object,
  shapeStatus: PropTypes.object,
};
