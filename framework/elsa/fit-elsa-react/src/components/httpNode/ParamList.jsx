/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ParamInfo} from '@/components/httpNode/ParamInfo.jsx';
import {ParamRow} from '@/components/httpNode/ParamRow.jsx';
import {useTranslation} from 'react-i18next';
import {Button} from 'antd';
import {PlusOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import React from 'react';

/**
 * 变量区域组件
 *
 * @param params 变量数据
 * @param disabled 是否禁用
 * @param onParamChange 变量修改的回调
 * @param handleDelete 删除
 * @param addParam 新增
 * @returns {React.JSX.Element} 变量区域组件
 * @constructor
 */
const _ParamList = ({
                      params,
                      disabled,
                      onParamChange,
                      handleDelete,
                      addParam,
                    }) => {
  const {t} = useTranslation();

  return (<>
    <ParamInfo t={t}/>
    {params.value.map((item) => (
      <ParamRow
        key={item.id}
        item={item}
        t={t}
        disabled={disabled}
        onParamChange={onParamChange}
        handleDelete={handleDelete}
      />
    ))}
    {/* 添加参数按钮 */}
    <div className='add-param-wrapper'>
      <Button
        disabled={disabled}
        type='text'
        className='icon-button add-param-button-style'
        onClick={addParam}
      >
        <div className='add-param-btn'>
          <PlusOutlined/>
          <div className='add-param-text'>{t('addParam')}</div>
        </div>
      </Button>
    </div>
  </>);
};

_ParamList.propTypes = {
  params: PropTypes.object.isRequired,
  disabled: PropTypes.bool.isRequired,
  handleDelete: PropTypes.func.isRequired,
  onParamChange: PropTypes.func.isRequired,
  addParam: PropTypes.func.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.params === nextProps.params &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.handleDelete === nextProps.handleDelete &&
    prevProps.onParamChange === nextProps.onParamChange &&
    prevProps.addParam === nextProps.addParam;
};

export const ParamList = React.memo(_ParamList, areEqual);