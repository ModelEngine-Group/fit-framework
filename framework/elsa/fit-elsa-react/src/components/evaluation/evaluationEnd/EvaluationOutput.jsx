/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Col, Collapse, Form, Popover, Row, Tree} from "antd";
import {QuestionCircleOutlined} from "@ant-design/icons";
import React, {useEffect, useState} from "react";
import {JadeStopPropagationSelect} from "@/components/common/JadeStopPropagationSelect.jsx";
import {useDispatch, useFormContext, useShapeContext} from "@/components/DefaultRoot.jsx";
import DeleteItem from '../../asserts/icon-delete.svg?react';
import AddSubItem from '../../asserts/icon-add-subitem.svg?react';
import TreeSwitcherIcon from "@/components/common/TreeSwitcherIcon.jsx";
import {JadeInput} from "@/components/common/JadeInput.jsx";
import PropTypes from "prop-types";
import {JadeReferenceTreeSelect} from "@/components/common/JadeReferenceTreeSelect.jsx";
import { useTranslation } from "react-i18next";

const {Panel} = Collapse;

_EvaluationOutput.propTypes = {
    shapeStatus: PropTypes.object,
    output: PropTypes.object
};

/**
 * 评估结束节点输出组件
 *
 * @param shapeStatus 节点状态
 * @param output 输出数据
 * @return {JSX.Element}
 * @constructor
 */
function _EvaluationOutput({shapeStatus, output}) {
    const shape = useShapeContext();
    const {t} = useTranslation();
    const dispatch = useDispatch();
    const [outputTreeData, setOutputTreeData] = useState(() => [convertToTreeData(output, 1, null)]);

    useEffect(() => {
        setOutputTreeData([convertToTreeData(output, 1, null)]);
    }, [output]);

    /**
     * 将jadeConfig转换成TreeData
     *
     * @param data output数据
     * @param level 层级
     * @return {{}}
     */
    function convertToTreeData(data, level) {
        if (!data) {
            return {};
        }
        const {id, name, type, value} = data;
        const children = Array.isArray(value) ?
            value.filter(item => typeof item === "object").map(item => convertToTreeData(item, level + 1)) : [];
        return {
            key: id,
            title: name,
            type: type,
            children: children,
            level: level,
            expanded: true,
            originalData: data
        };
    }

    const content = (<div className={"jade-font-size"} style={{lineHeight: "1.2"}}>
        <p>代码运行完成后输出的变量，必须保证此处定义的变量名、</p>
        <p>变量类型与代码的return对象中完全一致</p></div>);

    /**
     * 渲染tree数据
     *
     * @param node 节点数据
     * @return {JSX.Element}
     */
    const renderTreeNode = (node) => {
        return <EvaluationEndOutputTreeNode node={node} shapeStatus={shapeStatus} shape={shape} dispatch={dispatch}/>;
    };

    return (<>
        <Collapse bordered={false} className="jade-custom-collapse" defaultActiveKey={["evaluationOutputPanel"]}>
            {<Panel key={"evaluationOutputPanel"}
                    header={<div className="panel-header">
                        <span className="jade-panel-header-font">{t('output')}</span>
                        <Popover
                          content={content}
                          align={{offset: [0, 3]}}
                          overlayClassName={'jade-custom-popover'}
                        >
                            <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
                        </Popover>
                    </div>}
                    className="jade-panel"
            >
                <Tree blockNode={true}
                      switcherIcon={({expanded}) => <TreeSwitcherIcon expanded={expanded}/>}
                      showLine
                      defaultExpandAll
                      treeData={outputTreeData}
                      titleRender={renderTreeNode}
                />
            </Panel>}
        </Collapse>
    </>);
}

/**
 * 输出树状展示组件
 *
 * @param node 节点数据
 * @param shapeStatus 节点状态
 * @param shape 图形
 * @param dispatch 发送数据的动作
 * @return {JSX.Element} 组件
 * @constructor
 */
const EvaluationEndOutputTreeNode = ({node, shapeStatus, shape, dispatch}) => {
    const {key, title, level} = node;
    const item = node.originalData;
    const form = useFormContext();
    const {t} = useTranslation();
    const options = [{
        value: 'Reference',
        label: '引用',
        disabled: shapeStatus.disabled
    }];

    /**
     * 处理输入发生变化的动作
     *
     * @param id id
     * @param type 事件类型
     * @param changes 需要修改的属性
     */
    const handleItemChange = (id, type, changes) => {
        dispatch({type: type, id: id, changes: changes});
    };

    /**
     * 更新变量名
     *
     * @param id id
     * @param type 事件类型
     * @param e event事件
     */
    const editOutputName = (id, type, e) => {
        handleItemChange(id, type, [{key: 'name', value: e.target.value}]);
        form.setFieldsValue({[`property-${shape.id}-${id}`]: e.target.value});
    };

    /**
     * 添加子项
     *
     * @param key 需要添加子项的父项的id
     */
    const handleAddSubItem = (key) => {
        dispatch({type: "addSubItem", id: key});
    };

    /**
     * 删除一项
     *
     * @param key 需要删除的数据id
     */
    const handleDelete = key => {
        dispatch({type: "deleteRow", id: key});
    };

    /**
     * 获取最后一列的组件
     *
     * @param level 数据层级
     * @return {JSX.Element} 按钮或者空
     */
    const getButton = (level) => {
        return level > 1
            ? <Button type="text"
                      icon={<DeleteItem/>}
                      disabled={shapeStatus.disabled}
                      onClick={() => handleDelete(key)}/>
            : <Button type="text"
                      style={{marginLeft: '4px'}}
                      icon={<AddSubItem/>}
                      disabled={shapeStatus.disabled}
                      onClick={() => handleAddSubItem(key)}/>;
    };

    const _onReferencedValueChange = (referenceKey, value, type) => {
        handleItemChange(item.id, 'editOutputProperty', [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {key: 'type', value: type}]);
    };

    const _onReferencedKeyChange = (e) => {
        handleItemChange(item.id, "editOutputProperty", [{key: 'referenceNode', value: e.referenceNode},
            {key: 'referenceId', value: e.referenceId},
            {key: "referenceKey", value: e.referenceKey},
            {key: 'value', value: e.value},
            {key: "type", value: e.type}]);
    };

    /**
     * level大于1时获取切换模式的下拉框，目前只有“引用”
     *
     * @return {false|JSX.Element}
     */
    const getReferenceSelect = () => level > 1 &&
        <Form.Item style={{marginBottom: '8px'}}
                   rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
                   name={`value-select-${shape.id}-${key}`}
                   id={`type-${node.id}-${key}`}
                   initialValue={t('reference')}
        >
            <JadeStopPropagationSelect
                className={"value-source-custom jade-select"}
                disabled={shapeStatus.disabled}
                options={options}
            />
        </Form.Item>;

    /**
     * level大于1时获取可选择前序节点引用的下拉框
     *
     * @return {false|JSX.Element} 可选择前序节点引用的下拉框
     */
    const getObserveSelect = () => level > 1 &&
        <Form.Item style={{marginBottom: '8px', marginTop: "0px"}}
                   name={`value-select-${shape.id}-${key}`}
                   id={`type-${node.id}-${key}`}
        > <JadeReferenceTreeSelect
            disabled={shapeStatus.referenceDisabled}
            reference={item}
            onReferencedValueChange={(referenceKey, value, type) => _onReferencedValueChange(referenceKey, value, type)}
            onReferencedKeyChange={_onReferencedKeyChange}
            style={{fontSize: "12px"}}
            showSearch
            className="value-custom jade-select"
            dropdownStyle={{
                maxHeight: 400, overflow: 'auto'
            }}
            value={item.value}
            rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
        /> </Form.Item>;

    return (<>
        <Row key={`evaluation-end-output-${item.id}`}>
            <Col span={7} style={{marginRight: '8px'}}>
                <Form.Item name={`property-${shape.id}-${key}`}
                           id={`property-${node.id}-${key}`}
                           rules={[{required: true, message: t('fieldValueCannotBeEmpty')}, {
                               pattern: /^[^\s]*$/,
                               message: t('spacesAreNotAllowed')
                           }]}
                           initialValue={title}
                >
                    <JadeInput className="jade-input"
                               disabled={level === 1 || shapeStatus.disabled}
                               onChange={(e) => editOutputName(key, 'editOutputProperty', e)}
                    />
                </Form.Item>
            </Col>
            <Col span={6} style={{paddingRight: 0}}>
                {getReferenceSelect()}
            </Col>
            <Col span={8} style={{paddingLeft: 0}}>
                {getObserveSelect()}
            </Col>
            <Col span={2}>
                {getButton(level)}
            </Col>
        </Row>
    </>);
};

EvaluationEndOutputTreeNode.propTypes = {
    node: PropTypes.object,
    shapeStatus: PropTypes.object,
    shape: PropTypes.object,
    dispatch: PropTypes.func,
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.shapeStatus === nextProps.shapeStatus && prevProps.output === nextProps.output;
};

export const EvaluationOutput = React.memo(_EvaluationOutput, areEqual);