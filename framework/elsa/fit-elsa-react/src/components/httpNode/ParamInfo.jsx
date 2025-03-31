/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Col, Form, Row} from 'antd';
import '../common/style.css';
import React from 'react';
import PropTypes from 'prop-types';

/**
 * 参数信息组件，包含参数名称和参数值
 *
 * @param t 国际化组件
 * @returns {React.JSX.Element} 变量信息组件
 * @constructor
 */
const _ParamInfo = ({t}) => {
  return (<>
    <Row gutter={16}>
      <Col span={11}>
          <span
            className='jade-red-asterisk-tex jade-font-size jade-font-color'>
            {t('requestParamName')}
          </span>
      </Col>
      <Col span={11} style={{paddingLeft: 0}}>
          <span className='jade-red-asterisk-tex jade-font-size jade-font-color'>
            {t('requestParamValue')}
          </span>
      </Col>
    </Row>
  </>);
};

_ParamInfo.propTypes = {
  t: PropTypes.func.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.t === nextProps.t;
};

export const ParamInfo = React.memo(_ParamInfo, areEqual);