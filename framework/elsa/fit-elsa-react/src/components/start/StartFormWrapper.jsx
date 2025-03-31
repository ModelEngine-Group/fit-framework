/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useState} from 'react';
import {Button, Collapse, Popover} from 'antd';
import {DeleteOutlined, PlusOutlined, QuestionCircleOutlined} from '@ant-design/icons';
import {StartInputForm} from './StartInputForm.jsx';
import './style.css';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {v4 as uuidv4} from 'uuid';
import MultiConversation from '@/components/start/MultiConversation.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';

const {Panel} = Collapse;

StartFormWrapper.propTypes = {
    data: PropTypes.array.isRequired,
    shapeStatus: PropTypes.object
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
    const config = shape.graph.configs.find(node => node.node === "startNodeStart");
    const items = data.find(item => item.name === "input").value // 找出 name 为 "input" 的项，获取value值
    const memory = data.find(item => item.name === "memory");
    const memoryId = memory.id;
    const multiConversationSwitch = memory.value.find(item => item.name === "memorySwitch");
    const multiConversationSwitchValue = multiConversationSwitch?.value ?? true;
    const multiConversationTypeValue = memory.value.find(item => item.name === "type").value;
    const multiConversationValueValue = memory.value.find(item => item.name === "value")?.value ?? null;

    // items中所有初始都为打开状态
    const [openItems, setOpenItems] = useState(() => {
        return items.map(item => item.id);
    });

    // 添加新元素到 items 数组中，并将其 key 添加到当前展开的面板数组中
    const addItem = () => {
        // 开始节点入参最大数量为20
        if (items.length < 20) {
            const newItemId = "input_" + uuidv4();
            setOpenItems([...openItems, newItemId]); // 将新元素 key 添加到 openItems 数组中
            dispatch({actionType: "addInputParam", id: newItemId});
        }
    };

    const renderAddInputIcon = () => {
        const configObject = data.find(item => item.name === "input")
            ?.config
            ?.find(configItem => configItem.hasOwnProperty("allowAdd")); // 查找具有 "allowAdd" 属性的对象
        if (configObject ? configObject.allowAdd : false) {
            return (<>
                <Button disabled={shapeStatus.disabled}
                        type="text"
                        className="icon-button jade-start-add-icon"
                        onClick={addItem}>
                    <PlusOutlined/>
                </Button>
            </>);
        }
      return null;
    };

    const renderDeleteIcon = (item) => {
        if (!item.disableModifiable) {
            return (<>
                <Button
                  disabled={shapeStatus.disabled}
                  type='text'
                  className='icon-button start-node-delete-icon-button'
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
        dispatch({actionType: "deleteInputParam", id: itemId});
    };

    const content = (<div className={"jade-font-size"} style={{lineHeight: "1.2"}}>
        <Trans i18nKey="startNodeInputPopover" components={{p: <p/>}}/>
    </div>);

    // 处理内部组件值变化的回调函数
    const handleMultiConversationValueChange = (valueType, newValue) => {
        dispatch({
            actionType: "changeMemory",
            memoryType: multiConversationTypeValue,
            memoryValueType: valueType,
            memoryValue: newValue
        });
    };

    const handleMultiConversationTypeChange = (e, memoryValueType, memoryValue) => {
        dispatch({
            actionType: "changeMemory",
            memoryType: e,
            memoryValueType: memoryValueType,
            memoryValue: memoryValue
        });
    };

    const handleMultiConversationSwitchChange = (e) => {
        dispatch({actionType: "changeMemorySwitch", value: e});
    };

    return (<>
        <div>
            <div style={{
                display: "flex",
                alignItems: "center",
                marginBottom: "8px",
                paddingLeft: "8px",
                paddingRight: "4px",
                height: "32px"
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
            <Collapse bordered={false}
                      activeKey={openItems}
                      onChange={(keys) => setOpenItems(keys)}
                      style={{backgroundColor: 'transparent'}}
                      className="jade-custom-collapse">
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
                            style={{marginBottom: 8, borderRadius: "8px", width: "100%"}}
                        >
                            <div className={"jade-custom-panel-content"}>
                                <StartInputForm item={item} items={items}/>
                            </div>
                        </Panel>
                    ))
                }
            </Collapse>

            <MultiConversation className="jade-multi-conversation"
                               itemId={memoryId}
                               disabled={shapeStatus.disabled}
                               config={config}
                               props={{
                                   switch: {
                                       value: multiConversationSwitchValue,
                                       onChange: handleMultiConversationSwitchChange
                                   },
                                   type: {
                                       value: multiConversationTypeValue,
                                       onChange: handleMultiConversationTypeChange
                                   },
                                   value: {
                                       value: multiConversationValueValue,
                                       onChange: handleMultiConversationValueChange
                                   }
                               }}/>
        </div>
    </>);
}