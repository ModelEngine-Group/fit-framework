/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import React from 'react';
import PropTypes from 'prop-types';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {TextEditor} from '@/components/note/TextEditor.jsx';

/**
 * 注释节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} 大模型表单Wrapper的DOM
 */
const _NoteWrapper = ({data, shapeStatus}) => {
  const text = data.inputParams.find(item => item.name === 'text');
  const style = data.inputParams.find(item => item.name === 'style');
  const dispatch = useDispatch();

  return (<div
    className={'note-wrapper'}
    style={{
      pointerEvents: shapeStatus.isInDragging ? 'none' : 'auto',
      zIndex: '99999',
      position: 'relative',
    }}>
    <TextEditor text={text.value} style={style} dispatch={dispatch} isFocused={shapeStatus.isFocused}
                isInDragging={shapeStatus.isInDragging}/>
  </div>);
};

_NoteWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.data === nextProps.data &&
    prevProps.shapeStatus === nextProps.shapeStatus;
};

export const NoteWrapper = React.memo(_NoteWrapper, areEqual);