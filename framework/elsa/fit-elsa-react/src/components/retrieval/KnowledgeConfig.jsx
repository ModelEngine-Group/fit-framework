/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Col, Form, Popover, Row, Slider} from "antd";
import {QuestionCircleOutlined} from "@ant-design/icons";
import React from "react";
import {useDispatch} from "@/components/DefaultRoot.jsx";
import PropTypes from "prop-types";
import {Trans, useTranslation} from "react-i18next";

_KnowledgeConfig.propTypes = {
    maximum: PropTypes.number.isRequired,
    disabled: PropTypes.bool
};

/**
 * 知识配置组件
 *
 * @param maximum 最大值.
 * @param disabled 禁用状态.
 * @returns {JSX.Element}
 * @constructor
 */
function _KnowledgeConfig({maximum, disabled}) {
    const dispatch = useDispatch();
    const {t} = useTranslation();

    const maxRecallsTip = <div className={"jade-font-size"} style={{lineHeight: "1.2"}}>
        <Trans i18nKey="returnsTheMaximumValuePopover" components={{p: <p/>}}/>
    </div>;

    const defaultRecalls = {
        1: '1',
        [3]: t('default'),
        10: '10',
    };

    return (
        <Row className="jade-row">
            <Col span={12} className="jade-column">
                <Form.Item>
                    <span style={{
                        fontSize: '12px',
                        fontFamily: 'SF Pro Display',
                        letterSpacing: '0.12px',
                        lineHeight: '16px',
                        alignItems: "center",
                        userSelect: 'none',
                        marginRight: '4px',
                        color: 'rgb(37, 43, 58)'
                    }}>{t('returnsTheMaximumValue')}</span>
                    <Popover
                      content={maxRecallsTip}
                      align={{offset: [0, 3]}}
                      overlayClassName={'jade-custom-popover'}
                    >
                        <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
                    </Popover>
                </Form.Item>
            </Col>
            <Col span={12}>
                <Form.Item
                    style={{marginBottom: '0'}}
                    id={`valueSource`}
                    initialValue='Reference'
                >
                    <Slider disabled={disabled}
                            className="jade-slider"
                            style={{width: "90%"}}
                            min={1}
                            max={10}
                            step={1}
                            marks={defaultRecalls}
                            defaultValue={3}
                            value={maximum}
                            onChange={(value) => dispatch({type: "changeMaximum", key: "maximum", value: value})}
                    />
                </Form.Item>
            </Col>
        </Row>
    );
}

const areEqual = (prevProps, nextProps) => {
    return prevProps.maximum === nextProps.maximum && prevProps.disabled === nextProps.disabled;
};

export const KnowledgeConfig = React.memo(_KnowledgeConfig, areEqual);