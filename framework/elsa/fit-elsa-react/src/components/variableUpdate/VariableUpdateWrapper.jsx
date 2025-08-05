/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useState} from 'react';
import {Button} from 'antd';
import {PlusOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {VariableUpdateInputForm} from './VariableUpdateInputForm.jsx';
import {useTranslation} from 'react-i18next';
import './variableUpdate.css';
import {useConfigContext, useDispatch} from '@/components/DefaultRoot.jsx';
import {v4 as uuidv4} from 'uuid';

/**
 * 变量更新节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} DOM对象.
 */
const _VariableUpdateWrapper = ({data, shapeStatus}) => {
  const {t} = useTranslation();
  const dispatch = useDispatch();
  const isConfig = useConfigContext();
  const variables = data.inputParams.find(ip => ip.name === 'updateVariables')?.value ?? [];
  const [openItems, setOpenItems] = useState(() => {
    return isConfig ? variables.map(item => item.id) : [];
  });

  // 添加新元素到 variables 数组中，并将其 key 添加到当前展开的面板数组中
  const addItem = () => {
    // 变量更新节点入参最大数量为20
    if (variables.length < 20) {
      const newItemId = 'updateVariable_' + uuidv4();
      if (isConfig) {
        setOpenItems([...openItems, newItemId]); // 将新元素 key 添加到 openItems 数组中
      }
      dispatch({actionType: 'addVariable', id: newItemId});
    }
  };

  return (<>
    <div>
      <div style={{
        display: 'flex',
        alignItems: 'center',
        marginBottom: '8px',
        paddingLeft: '8px',
        paddingRight: '4px',
        height: '24px',
      }}>
          <Button disabled={shapeStatus.disabled}
                  type="text"
                  className="icon-button jade-variable-update-button"
                  onClick={addItem}>
            <PlusOutlined/>
            <div className="jade-panel-header-font" style={{paddingLeft: '4px'}}>{t('add')}</div>
          </Button>
      </div>
      <VariableUpdateInputForm variables={variables} shapeStatus={shapeStatus}/>
    </div>
  </>);
};

_VariableUpdateWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

export const VariableUpdateWrapper = React.memo(_VariableUpdateWrapper);