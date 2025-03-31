/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Select} from 'antd';

/**
 * 不会受Elsa冒泡机制影响的Select组件.
 *
 * @param props 参数.
 * @return {JSX.Element}
 * @constructor
 */
export const JadeStopPropagationSelect = (props) => {
  const {onMouseDown, className, ...rest} = props;

  /**
   * 选择框被鼠标点击时调用.
   *
   * @param e 事件对象.
   * @private
   */
  const _onMouseDown = (e) => {
    onMouseDown && onMouseDown(e);
    e.stopPropagation(); // 阻止事件冒泡
  };

  // 默认的 className
  const defaultClassName = 'jade-select';

  // 使用默认 className 和外部传递的 className
  const combinedClassName = className ? `${defaultClassName} ${className}` : defaultClassName;

  return (<><Select getPopupContainer={trigger => trigger.parentNode}
                    onMouseDown={(e) => _onMouseDown(e)}
                    className={combinedClassName}
                    {...rest}
  /></>);
};