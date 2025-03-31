/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Name} from './Name.jsx';
import Type from '../common/Type.jsx';
import Description from './Description.jsx';
import PropTypes from 'prop-types'; // 导入 PropTypes
import {useDispatch} from '@/components/DefaultRoot.jsx';
import Required from '@/components/start/Required.jsx';
import React from 'react';
import Visible from '@/components/start/Visible.jsx';
import {DisplayName} from '@/components/start/DisplayName.jsx';
import {useTranslation} from 'react-i18next';

/**
 * 开始节点入参表单
 *
 * @param item 表单对应的对象
 * @param items 所有入参表单对象
 * @returns {JSX.Element} 开始节点入参表单的DOM
 */
const _StartInputForm = ({item, items}) => {
    const dispatch = useDispatch();
    const { t } = useTranslation();

    /**
     * 传递至子组件，用于使用dispatch更新
     *
     * @param type 子组件对应的type
     * @param value 更新后的value
     */
    const handleFormValueChange = (type, value) => {
        dispatch({actionType: 'changeInputParam', id: item.id, type: type, value: value});
        if (type === 'isRequired' && value) {
            dispatch({actionType: 'changeInputParam', id: item.id, type: 'isVisible', value: value});
        }
    };

    const handleChange = (value) => {
        handleFormValueChange("type", value); // 当选择框的值发生变化时调用父组件传递的回调函数
        document.activeElement.blur();// 在选择后取消焦点
    };

    return (<>
        <Name
          itemId={item.id}
          propValue={item.name}
          type={item.type}
          disableModifiable={item.disableModifiable}
          onChange={handleFormValueChange}
          items={items}/>
        <DisplayName itemId={item.id} propValue={item.displayName} disableModifiable={item.disableModifiable} onChange={handleFormValueChange} items={items}/>
        <Type itemId={item.id} propValue={item.type} disableModifiable={item.disableModifiable} onChange={handleChange} labelName={t('fieldType')}/>
        <Description itemId={item.id} propValue={item.description} disableModifiable={item.disableModifiable} onChange={handleFormValueChange}/>
        <div style={{display: 'flex', flexDirection: 'row'}}>
            <Required itemId={item.id} propValue={item.isRequired} disableModifiable={item.disableModifiable} onChange={handleFormValueChange}/>
            <Visible
              itemId={item.id} propValue={item.isVisible} disableModifiable={item.isRequired || item.disableModifiable}
              onChange={handleFormValueChange}/>
        </div>
    </>);
};

_StartInputForm.propTypes = {
    item: PropTypes.shape({
        id: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
        type: PropTypes.string.isRequired,
        description: PropTypes.string.isRequired,
        from: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired,
        disableModifiable: PropTypes.bool,
        isRequired: PropTypes.bool,
        isVisible: PropTypes.bool,
    }).isRequired,
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.disabled === nextProps.disabled &&
      prevProps.item.id === nextProps.item.id &&
      prevProps.item.name === nextProps.item.name &&
      prevProps.item.type === nextProps.item.type &&
      prevProps.item.description === nextProps.item.description &&
      prevProps.item.from === nextProps.item.from &&
      prevProps.item.value === nextProps.item.value &&
      prevProps.item.disableModifiable === nextProps.item.disableModifiable &&
      prevProps.item.isRequired === nextProps.item.isRequired &&
      prevProps.item.isVisible === nextProps.item.isVisible &&
      prevProps.items === nextProps.items;
};

export const StartInputForm = React.memo(_StartInputForm, areEqual);