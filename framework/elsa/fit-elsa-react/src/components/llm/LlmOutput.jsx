/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from "react";
import {Checkbox, Collapse, Form, Popover} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import "../common/style.css";
import {JadeObservableTree} from "@/components/common/JadeObservableTree.jsx";
import PropTypes from "prop-types";
import {useTranslation} from "react-i18next";
import {useDispatch} from '@/components/DefaultRoot.jsx';

const {Panel} = Collapse;

_LlmOutput.propTypes = {
    outputItems: PropTypes.array.isRequired,
    enableLogData: PropTypes.object.isRequired,
    disabled: PropTypes.bool.isRequired,
};

/**
 * 大模型节点输出表单。
 *
 * @param outputItems 出参.
 * @param enableLogData 是否输出至聊天窗.
 * @param disabled 是否禁止操作.
 * @returns {JSX.Element} 大模型节点输出表单的DOM。
 */
function _LlmOutput({outputItems, enableLogData, disabled}) {
    const dispatch = useDispatch();
    // 430演示大模型输出不需要新增和删除，暂时屏蔽
    // 添加新元素到 items 数组中，并将其 key 添加到当前展开的面板数组中
    // const addItem = () => {
    //     dispatch({actionType: "addOutputParam", id: "uuidv4()})
    // };
 
    // 430演示大模型输出不允许用户修改，暂时屏蔽
    // const handleItemChange = (name, value, itemId) => {
    //     dispatch({actionType: "changeOutputParam", id: itemId, type: name, value: value});
    //     if (name === "type") {
    //         document.activeElement.blur();// 在选择后取消焦点
    //     }
    // };
 
    // 430演示大模型输出不需要新增和删除，暂时屏蔽
    // const handleDelete = (itemId) => {
    //     dispatch({actionType: "deleteOutputParam", id: itemId});
    // };
 
    // 430演示大模型输出不需要新增和删除，暂时屏蔽
    // const handleSelectClick = (event) => {
    //     event.stopPropagation(); // 阻止事件冒泡
    // };
    const {t} = useTranslation();
 
    const content = (
        <div className={"jade-font-size"}>
            <p>{t('llmOutputPopover')}</p>
        </div>
    );

    return (
        <Collapse bordered={false} className="jade-custom-collapse" defaultActiveKey={["outputPanel"]}>
            {
                <Panel
                    key={"outputPanel"}
                    header={
                        <div className="panel-header"
                             style={{display: 'flex', alignItems: 'center', justifyContent: "flex-start"}}>
                            <span className="jade-panel-header-font">{t('output')}</span>
                            <Popover
                              content={content}
                              align={{offset: [0, 3]}}
                              overlayClassName={'jade-custom-popover'}
                            >
                                <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
                            </Popover>
                            {/* 430演示大模型输出不需要新增和删除，暂时屏蔽*/}
                            {/* <Button type="text" className="icon-button"*/}
                            {/*        style={{"height": "22px", "marginLeft": "auto"}}*/}
                            {/*        onClick={(event) => {*/}
                            {/*            addItem();*/}
                            {/*            handleSelectClick(event);*/}
                            {/*        }}>*/}
                            {/*    <PlusOutlined/>*/}
                            {/* </Button>*/}
                        </div>
                    }
                    className="jade-panel"
                >
                    <div className={"jade-custom-panel-content"}>
                        <Form.Item className='jade-form-item' name={`enableLog-${enableLogData.id}`}>
                            <Checkbox checked={enableLogData.value} disabled={disabled}
                                      onChange={e => dispatch({ type: 'updateLogStatus', value: e.target.checked})}><span
                              className={'jade-font-size'}>{t('pushResultToChat')}</span></Checkbox>
                        </Form.Item>
                        <JadeObservableTree data={outputItems}/>
                        {/* 430演示大模型输出不允许用户操作，写死*/}
                        {/* <Row gutter={16}>*/}
                        {/*    <Col span={7}>*/}
                        {/*        <Form.Item>*/}
                        {/*            <span style={{color: "rgba(28,31,35,.35)"}}>Name</span>*/}
                        {/*        </Form.Item>*/}
                        {/*    </Col>*/}
                        {/*    <Col span={5}>*/}
                        {/*        <Form.Item>*/}
                        {/*            <span style={{color: "rgba(28,31,35,.35)"}}>Type</span>*/}
                        {/*        </Form.Item>*/}
                        {/*    </Col>*/}
                        {/*    <Col span={12}>*/}
                        {/*        <Form.Item>*/}
                        {/*            <span style={{color: "rgba(28,31,35,.35)"}}>Description</span>*/}
                        {/*        </Form.Item>*/}
                        {/*    </Col>*/}
                        {/* </Row>*/}
                        {/* 430演示大模型输出不允许用户操作，写死*/}
                        {/* {outputItems.map((item) => (*/}
                        {/*    <Row*/}
                        {/*        key={item.id}*/}
                        {/*        gutter={16}*/}
                        {/*    >*/}
                        {/* <Col span={7}>*/}
                        {/*    <Form.Item*/}
                        {/*        id={`name-${item.id}`}*/}
                        {/*        name={`name-${item.id}`}*/}
                        {/*        rules={[{required: true, message: '输出参数名称不能为空!'}]}*/}
                        {/*        initialValue={item.name}*/}
                        {/*    >*/}
                        {/*        <JInput*/}
                        {/*            style={{paddingRight: "12px"}}*/}
                        {/*            value={item.name}*/}
                        {/*            onChange={(e) => handleItemChange('name', e.target.value, item.id)}*/}
                        {/*        />*/}
                        {/*    </Form.Item>*/}
                        {/* </Col>*/}
                        {/* <Col span={5}>*/}
                        {/*    <Form.Item*/}
                        {/*        id={`type-${item.id}`}*/}
                        {/*        initialValue='String'*/}
                        {/*    >*/}
                        {/*        <JadeStopPropagationSelect*/}
                        {/*            id={`type-select-${item.id}`}*/}
                        {/*            style={{width: "100%"}}*/}
                        {/*            onChange={(value) => handleItemChange('type', value, item.id)}*/}
                        {/*            options={[*/}
                        {/*                {value: 'String', label: 'String'},*/}
                        {/*                {value: 'Integer', label: 'Integer'},*/}
                        {/*                {value: 'Boolean', label: 'Boolean'},*/}
                        {/*                {value: 'Number', label: 'Number'},*/}
                        {/*                {value: 'Object', label: 'Object', disabled: true},*/}
                        {/*                {value: 'Array<String>', label: 'Array<String>'},*/}
                        {/*                {value: 'Array<Integer>', label: 'Array<Integer>'},*/}
                        {/*                {value: 'Array<Boolean>', label: 'Array<Boolean>'},*/}
                        {/*                {value: 'Array<Number>', label: 'Array<Number>'},*/}
                        {/*                {value: 'Array<Object>', label: 'Array<Object>', disabled: true},*/}
                        {/*            ]}*/}
                        {/*            value={item.type}*/}
                        {/*        />*/}
                        {/*    </Form.Item>*/}
                        {/* </Col>*/}
                        {/* <Col span={11}>*/}
                        {/*    <Form.Item*/}
                        {/*        id={`description-${item.id}`}*/}
                        {/*    >*/}
                        {/*        <JInput*/}
                        {/*            style={{paddingRight: "12px"}}*/}
                        {/*            value={item.description}*/}
                        {/*            onChange={(e) => handleItemChange('description', e.target.value, item.id)}*/}
                        {/*        />*/}
                        {/*    </Form.Item>*/}
                        {/* </Col>*/}

                        {/* 430演示大模型输出不需要新增和删除，暂时屏蔽*/}
                        {/* <Col span={1} style={{paddingLeft: "2px"}}>*/}
                        {/*    <Form.Item>*/}
                        {/*        <Button type="text" className="icon-button"*/}
                        {/*                style={{"height": "100%", "marginLeft": "auto"}}*/}
                        {/*                onClick={() => handleDelete(item.id)}>*/}
                        {/*            <MinusCircleOutlined/>*/}
                        {/*        </Button>*/}
                        {/*    </Form.Item>*/}
                        {/* </Col>*/}
                        {/* </Row>*/}
                        {/* ))}*/}
                    </div>
                </Panel>
            }
        </Collapse>
    );
}

// 对象不变，不刷新组件.
const areEqual = (prevProps, nextProps) => {
    return prevProps.outputItems === nextProps.outputItems && prevProps.enableLogData === nextProps.enableLogData && prevProps.disabled === nextProps.disabled;
};

export const LlmOutput = React.memo(_LlmOutput, areEqual);