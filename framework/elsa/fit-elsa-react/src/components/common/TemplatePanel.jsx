/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Col, Collapse, Form, Popover, Row} from 'antd';
import {useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import React, {useState} from 'react';
import FullScreenIcon from '../asserts/icon-full-screen.svg?react';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';
import {PromptDrawer} from "@/components/common/prompt/PromptDrawer.jsx";
import TextArea from "antd/es/input/TextArea.js";
import {QuestionCircleOutlined} from "@ant-design/icons";
import * as Proptypes from "prop-types";

const {Panel} = Collapse;

/**
 * 文本模板组件。
 *
 * @param name 表单名称
 * @param title 标题文本
 * @param placeHolder 输入框占位符文本
 * @param template 文字模板对象
 * @param labelName 标签名称
 * @param onChange 内容变化时的回调函数
 * @param disabled 是否禁用输入框
 * @param maxLength 输入内容的最大长度限制
 * @returns {Element} 返回文本模板组件的JSX元素
 * @constructor
 */
const _TemplatePanel = ({name, title, placeHolder, template, labelName, onChange, disabled, maxLength}) => {
    const shape = useShapeContext();
    const {t} = useTranslation();
    const form = useFormContext();

    const [promptOpen, setPromptOpen] = useState(false);

    const promptContent = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='promptPopover' components={{p: <p/>}}/>
    </div>);

    const _onChange = (templateText) => {
        onChange(templateText);
        form.setFieldsValue({[name]: templateText});
    };

    /**
     * 失焦时才设置值，对于必填项.若为空，则不设置
     *
     * @param e event事件
     */
    const changeOnBlur = (e) => {
        if (template.value !== e.target.value) {
            _onChange(e.target.value);
        }
    };

    return (<JadeCollapse defaultActiveKey={['templatePanel']}>
        <Panel
            key="templatePanel"
            header={<>
                <div className={'required-after'} style={{display: 'flex', alignItems: 'center', width: '100%'}}>
                    <span className='jade-second-title'>{title}</span>
                    {promptContent && <Popover
                        content={[promptContent]}
                        align={{offset: [0, 3]}}
                        overlayClassName={'jade-custom-popover'}
                    >
                        <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
                    </Popover>}
                    <div className={'prompt-title-buttons'}>
                        <Button
                            disabled={disabled}
                            type='text'
                            className='icon-button'
                            style={{height: '100%'}}
                            onClick={() => setPromptOpen(true)}>
                            <FullScreenIcon/>
                        </Button>
                    </div>
                </div>
            </>}
            className="jade-panel"
        >
            <div className="jade-custom-panel-content">
                <Row gutter={16}>
                    <Col span={24}>
                        <div className={'prompt-container'}>
                            <Form.Item
                                className='jade-form-item'
                                name={name}
                                rules={[{required: true, message: t('paramCannotBeEmpty')}]}
                                initialValue={template.value}
                                validateTrigger='onBlur'
                            >
                                <TextArea
                                    disabled={disabled}
                                    maxLength={2000}
                                    className='jade-textarea-input jade-font-size'
                                    onBlur={(e) => changeOnBlur(e)}
                                    placeholder={placeHolder}
                                />
                            </Form.Item>
                            <PromptDrawer
                                value={template.value}
                                name={name}
                                title={title}
                                rules={[{required: true, message: t('paramCannotBeEmpty')}]}
                                placeHolder={placeHolder}
                                container={shape.page.graph.div.parentElement}
                                open={promptOpen}
                                onClose={() => setPromptOpen(false)}
                                onConfirm={(v) => _onChange(v)}
                                labelName={labelName}
                                maxLength={maxLength}
                            />
                        </div>
                    </Col>
                </Row>
            </div>
        </Panel>
    </JadeCollapse>);
};

_TemplatePanel.propTypes = {
    name: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    placeHolder: PropTypes.string.isRequired,
    template: PropTypes.object.isRequired,
    labelName: PropTypes.string.isRequired,
    onChange: Proptypes.func.isRequired,
    disabled: PropTypes.bool,
    maxLength: PropTypes.number,
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.template === nextProps.template;
};

export const TemplatePanel = React.memo(_TemplatePanel, areEqual);