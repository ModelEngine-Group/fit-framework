/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useLayoutEffect, useState} from 'react';
import {Button, Collapse} from 'antd';
import {DeleteOutlined, PlusOutlined} from '@ant-design/icons';
import {useConfigContext, useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {v4 as uuidv4} from 'uuid';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {IntelligentInputFormItem} from '@/components/intelligentForm/IntelligentInputFormItem.jsx';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

SimpleFormWrapper.propTypes = {
  data: PropTypes.object.isRequired,
  shapeStatus: PropTypes.object,
};

/**
 * 智能表单Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态.
 * @returns {JSX.Element} 智能表单Wrapper的DOM
 */
export default function SimpleFormWrapper({data, shapeStatus}) {
  const {t} = useTranslation();
  const dispatch = useDispatch();
  const shape = useShapeContext();
  const isConfig = useConfigContext();
  const items = data.converter.entity.inputParams.find(
    (item) => item.name === 'schema'
  )?.value.parameters;
  const output = data.converter.entity.outputParams.find((item) => item.name === 'output');

  // items中所有初始都为打开状态
  const [openItems, setOpenItems] = useState(() => {
    return isConfig ? items.map(item => item.id) : [];
  });

  // 智能表单节点使用JadeInput，但输出的地方外层需要套一个output，在此处把output注册
  useLayoutEffect(() => {
    shape.page.registerObservable({
      nodeId: shape.id,
      observableId: output.id,
      value: output.name,
      type: output.type,
      parentId: undefined,
    });
  }, []);

  const outputDataConvert = (items) => {
    const outputData = JSON.parse(JSON.stringify(data.converter.entity.outputParams)); // 手动深度拷贝
    const outputItem = outputData.find((item) => item.name === 'output');
    if (outputItem) {
      outputItem.value = [...items]; // 创建新数组
    }
    return outputData;
  };

  // 添加新元素到 items 数组中，并将其 key 添加到当前展开的面板数组中
  const addItem = () => {
    // 智能表单节点入参最大数量为30
    if (items.length < 30) {
      const newItemId = 'input_' + uuidv4();
      if (isConfig) {
        setOpenItems([...openItems, newItemId]); // 将新元素 key 添加到 openItems 数组中
      }
      dispatch({type: 'addParam', id: newItemId});
    }
  };

  const renderAddInputIcon = () => {
    return (<>
      <Button disabled={shapeStatus.disabled}
              type='text'
              className='icon-button jade-start-add-icon'
              onClick={addItem}>
        <PlusOutlined/>
      </Button>
    </>);
  };

  const renderDeleteIcon = (item) => {
    return (<>
      <Button
        disabled={shapeStatus.disabled}
        type='text'
        className='icon-button start-node-delete-icon-button'
        onClick={() => handleDelete(item.id)}>
        <DeleteOutlined/>
      </Button>
    </>);
  };

  const handleDelete = (itemId) => {
    const updatedOpenItems = openItems.filter((key) => key !== itemId);
    setOpenItems(updatedOpenItems);
    dispatch({type: 'deleteParam', id: itemId});
  };

  return (<>
    <div>
      <div style={{display: 'flex', alignItems: 'center', marginBottom: '8px', paddingLeft: '8px', paddingRight: '4px', height: '32px'}}>
        <div className='jade-panel-header-font'>{t('formItem')}</div>
        {renderAddInputIcon()}
      </div>
      <JadeCollapse
        activeKey={openItems}
        onChange={(keys) => setOpenItems(keys)}
        style={{backgroundColor: 'transparent'}}>
        {
          items.map((item) => (
            <Panel
              key={item.id}
              header={
                <div className="panel-header">
                  <span className="jade-panel-header-font">{item.name}</span> {/* 显示Name值的元素 */}
                  {renderDeleteIcon(item)}
                </div>
              }
              className="jade-panel"
              style={{marginBottom: 8, borderRadius: '8px', width: '100%'}}
              forceRender
            >
              <div className={'jade-custom-panel-content'}>
                <IntelligentInputFormItem item={item} items={items} shapeStatus={shapeStatus} output={output}/>
              </div>
            </Panel>
          ))
        }
      </JadeCollapse>
      <InvokeOutput outputData={outputDataConvert(items)} isObservableTree={false}/>
    </div>
  </>);
}