/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Col, Collapse, Form, Popover, Row} from "antd";
import React from "react";
import {QuestionCircleOutlined} from '@ant-design/icons';
import "./style.css";
import {OutputVariableRow} from "@/components/end/OutputVariableRow.jsx";
import {useDispatch} from "@/components/DefaultRoot.jsx";
import PropTypes from "prop-types";
import ArrayUtil from "@/components/util/ArrayUtil.js";
import {Trans, useTranslation} from "react-i18next";

const {Panel} = Collapse;

/**
 * 输出变量的组件，包含多条输出变量的条目
 *
 * @param inputParams 入参.
 * @param shapeStatus 图形状态.
 * @returns {JSX.Element}
 * @constructor
 */
const _OutputVariable = ({inputParams, shapeStatus}) => {
    const dispatch = useDispatch();
    const {t} = useTranslation();

    /**
     * 处理输入发生变化的动作
     *
     * @param id id
     * @param changes 变更的字段
     */
    const handleItemChange = (id, changes) => {
        dispatch({type: "editOutputVariable", id: id, changes: changes});
    };


    const tips =
        <div className={"jade-font-size"} style={{lineHeight: "1.2"}}>
            <Trans i18nKey="endOutputPopover" components={{p: <p/>}}/>
        </div>;

    return (
        <div>
            <Collapse bordered={false} className="jade-custom-collapse"
                      style={{marginTop: "10px", marginBottom: 8, borderRadius: "8px", width: "100%"}}
                      defaultActiveKey={['Output variable']}>
                <Panel
                    style={{marginBottom: 8, borderRadius: "8px", width: "100%"}}
                    header={
                        <div
                            style={{display: 'flex', alignItems: 'center', justifyContent: "flex-start"}}>
                            <span className="jade-panel-header-font">{t('output')}</span>
                            <Popover
                              content={tips}
                              align={{offset: [0, 3]}}
                              overlayClassName={'jade-custom-popover'}
                            >
                                <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
                            </Popover>
                        </div>
                    }
                    className="jade-panel"
                    key='Output variable'
                >
                    <div className={"jade-custom-panel-content"}>
                        <Row gutter={16}>
                            <Col span={8}>
                                <Form.Item style={{marginBottom: "8px"}}>
                                    <span className="jade-font-size jade-font-color">{t('fieldName')}</span>
                                </Form.Item>
                            </Col>
                            <Col span={16}>
                                <Form.Item style={{marginBottom: "8px"}}>
                                    <span className="jade-font-size jade-font-color">{t('fieldValue')}</span>
                                </Form.Item>
                            </Col>
                        </Row>
                        <OutputVariableRow shapeStatus={shapeStatus}
                                           item={inputParams.find(item => item.name === 'finalOutput')}
                                           handleItemChange={handleItemChange}/>
                    </div>
                </Panel>
            </Collapse>
        </div>
    );
};

_OutputVariable.propTypes = {
    inputParams: PropTypes.array.isRequired,
    shapeStatus: PropTypes.object
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.shapeStatus === nextProps.shapeStatus
        && ArrayUtil.isEqual(prevProps.inputParams, nextProps.inputParams);
};

export const OutputVariable = React.memo(_OutputVariable, areEqual);