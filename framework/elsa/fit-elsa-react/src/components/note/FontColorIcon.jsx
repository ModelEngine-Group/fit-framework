/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';

/**
 * 字体颜色组件
 *
 * @param color svg填充的颜色
 * @returns {React.JSX.Element}
 * @constructor
 */
const FontColorIcon = ({color}) => (
  <svg
    width='16.000000'
    height='16.000000'
    viewBox='0 0 16 16'
    fill='none'
    xmlns='http://www.w3.org/2000/svg'
    xmlns:xlink='http://www.w3.org/1999/xlink'>
    <desc>
      Created with Pixso.
    </desc>
    <defs/>
    <rect id='A' width='16.000000' height='16.000000' fill='#FFFFFF' fill-opacity='0'/>
    <path id='A'
          d='M8.43 2.87L12.55 13.12L11.16 13.12L10.03 10.18L5.78 10.18L4.7 13.12L3.45 13.12L7.38 2.87L8.43 2.87ZM9.59 9.06L7.85 4.54L6.2 9.06L9.59 9.06Z'
          fill={color} fill-opacity='1.000000' fill-rule='nonzero'/>
  </svg>);

export default FontColorIcon;