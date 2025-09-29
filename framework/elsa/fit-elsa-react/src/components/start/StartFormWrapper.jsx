/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useState} from 'react';
import {Button, Collapse, Popover, Form} from 'antd';
import {DeleteOutlined, EditOutlined, PlusOutlined, QuestionCircleOutlined} from '@ant-design/icons';
import {StartInputForm} from './StartInputForm.jsx';
import './style.css';
import {useConfigContext, useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {v4 as uuidv4} from 'uuid';
import MultiConversation from '@/components/start/MultiConversation.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import {AppConfiguration} from '@/components/start/AppConfiguration.jsx';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

StartFormWrapper.propTypes = {
  data: PropTypes.array.isRequired,
  shapeStatus: PropTypes.object,
};

/**
 * 开始表单Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态.
 * @returns {JSX.Element} 开始表单Wrapper的DOM
 */
export default function StartFormWrapper({data, shapeStatus}) {
  const {t} = useTranslation();
  const dispatch = useDispatch();
  const shape = useShapeContext();
  const isConfig = useConfigContext();
  const config = shape.graph.configs.find(node => node.node === 'startNodeStart');
  const items = data.find(item => item.name === 'input').value; // 找出 name 为 "input" 的项，获取value值
  const memory = data.find(item => item.name === 'memory');
  const memoryId = memory.id;
  const multiConversationSwitch = memory.value.find(item => item.name === 'memorySwitch');
  const multiConversationSwitchValue = multiConversationSwitch?.value ?? true;
  const multiConversationTypeValue = memory.value.find(item => item.name === 'type').value;
  const multiConversationValueValue = memory.value.find(item => item.name === 'value')?.value ?? null;
  const appConfig = data.find(item => item.name === 'appConfig');

  const [openItems, setOpenItems] = useState(() => {
    return isConfig ? items.map(item => item.id) : [];
  });

  useEffect(() => {
    items.map(item => {
      shape.page.registerObservable({
        nodeId: shape.id,
        observableId: item.id,
        value: item.name,
        type: item.type,
        parentId: undefined,
      });
    })

    // 组件unmount时，删除observable.
    return () => {
      items.map(item => {
        shape.page.removeObservable(shape.id, item.id);
      })
    };
  }, []);

  // 添加新元素到 items 数组中，并将其 key 添加到当前展开的面板数组中
  const addItem = () => {
    // 开始节点入参最大数量为20
    if (items.length < 20) {
      const newItemId = 'input_' + uuidv4();
      if (isConfig) {
        setOpenItems([...openItems, newItemId]); // 将新元素 key 添加到 openItems 数组中
      }
      dispatch({actionType: 'addInputParam', id: newItemId});
    }
  };

  const renderAddInputIcon = () => {
    const configObject = data.find(item => item.name === 'input')
      ?.config
      ?.find(configItem => configItem.hasOwnProperty('allowAdd')); // 查找具有 "allowAdd" 属性的对象
    if (configObject ? configObject.allowAdd : false) {
      const inputParams =  data.find(item => item.name === 'input')?.value;
      return (<>
        <Button disabled={shapeStatus.disabled}
                type="text"
                className="icon-button jade-start-add-icon"
                onClick={(event) => {
                  triggerAddInput(event, inputParams);
                }}>
          <PlusOutlined/>
        </Button>
      </>);
    }
    return null;
  };

  const renderIcons = (item) => {
    if (!item.disableModifiable) {
      return (<>
        <Button
            disabled={shape.status.disabled}
            type="text"
            className="icon-button start-node-edit-icon-button"
            onClick={(event) =>triggerEditInput(event, item.id, item)}>
          <EditOutlined/>
        </Button>
        <Button
          disabled={shapeStatus.disabled}
          type="text"
          className="icon-button start-node-delete-icon-button"
          onClick={() => handleDelete(item.id)}>
          <DeleteOutlined/>
        </Button>
      </>);
    }
    return null;
  };

  const handleDelete = (itemId) => {
    const updatedOpenItems = openItems.filter((key) => key !== itemId);
    setOpenItems(updatedOpenItems);
    dispatch({actionType: 'deleteInputParam', id: itemId});
    shape.page.removeObservable(shape.id, itemId)
  };

  const content = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey="startNodeInputPopover" components={{p: <p/>}}/>
  </div>);

  // 处理内部组件值变化的回调函数
  const handleMultiConversationValueChange = (valueType, newValue) => {
    dispatch({
      actionType: 'changeMemory',
      memoryType: multiConversationTypeValue,
      memoryValueType: valueType,
      memoryValue: newValue,
    });
  };

  const handleMultiConversationTypeChange = (e, memoryValueType, memoryValue) => {
    dispatch({
      actionType: 'changeMemory',
      memoryType: e,
      memoryValueType: memoryValueType,
      memoryValue: memoryValue,
    });
  };

  const handleMultiConversationSwitchChange = (e) => {
    dispatch({actionType: 'changeMemorySwitch', value: e});
  };

  /**
   * 添加增加开始节点自定义参数的回调函数
   *
   * @param event 事件
   */
  const triggerAddInput = (event, existParam) => {
    event.preventDefault();
    shape.page.triggerEvent({
      type: 'ADD_START_INPUT',
      value: {
        existParam: items,
        onAdd: (param) => {
          dispatch({
            actionType: 'addParam',
            value: param,
          });
          shape.page.registerObservable({
            nodeId: shape.id,
            observableId: param.id,
            value: param.name,
            type: param.type,
            parentId: undefined
          });

          if (isConfig) {
            setOpenItems(prev => [...prev, param.id]);
          }
        },
      },
    });
    event.stopPropagation();
  };

  /**
   * 添加修改开始节点自定义参数的回调函数
   *
   * @param event 事件
   */
  const triggerEditInput = (event, id, selectedParam) => {
    event.preventDefault();
    shape.page.triggerEvent({
      type: 'EDIT_START_INPUT',
      value: {
        id: id,
        selectedParam: selectedParam,
        onEdit: (param) => {
          dispatch({
            actionType: 'editParam',
            value: param,
            id: id,
          });
          shape.emit(id, {type: param.type, value: param.name});
        },
      },
    });
    event.stopPropagation();
  };


  return (<>
    <div>
      <JadeCollapse
          defaultActiveKey={["startInput"]}
      >
        <Panel
            key={"startInput"}
            header={
          <div style={{
            display: 'flex',
            alignItems: 'center',
            paddingRight: '4px',
            height: '32px',
          }}>
            <div className="jade-panel-header-font">{t('input')}</div>
            <Popover
                content={content}
                align={{offset: [0, 3]}}
                overlayClassName={'jade-custom-popover'}
            >
              <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
            </Popover>
            {renderAddInputIcon()}
          </div>
        }
               className='jade-panel'
        >
          {items.map((item) => (
              <Form.Item
                  className='jade-input-item'
                  name={`input-${item.id}`}
                  key={item.id}
              >
                <div className="param-item">
                  <div className="param-row">
                    <div className="param-left">
                      <div className="param-name">{item.name}</div>
                      <div className="param-display-name">{item.displayName}</div>
                    </div>
                    <div className="param-right">
                      <div className="param-type-wrapper">
                        <div className="param-type">{item.type}</div>
                      </div>
                      <div className="param-icons">
                        {renderIcons(item)}
                      </div>
                    </div>
                  </div>
                </div>
              </Form.Item>
          ))}
        </Panel>
      </JadeCollapse>

      <MultiConversation className="jade-multi-conversation"
                         itemId={memoryId}
                         disabled={shapeStatus.disabled}
                         config={config}
                         props={{
                           switch: {
                             value: multiConversationSwitchValue,
                             onChange: handleMultiConversationSwitchChange,
                           },
                           type: {
                             value: multiConversationTypeValue,
                             onChange: handleMultiConversationTypeChange,
                           },
                           value: {
                             value: multiConversationValueValue,
                             onChange: handleMultiConversationValueChange,
                           },
                         }}/>
      {appConfig && <AppConfiguration item={appConfig} disabled={shapeStatus.disabled} configs={config.appConfig}/>}
    </div>
  </>);
}