/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Collapse, Form, Popover, Typography} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import {CodeEditor} from '@/components/common/code/CodeEditor.jsx';
import React, {useState} from 'react';
import EditCode from '../asserts/icon-edit.svg?react';
import {useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import httpUtil from '@/components/util/httpUtil.jsx';
import ArrayUtil from '@/components/util/ArrayUtil.js';
import PropTypes from 'prop-types';
import {CodeDrawer} from '@/components/common/code/CodeDrawer.jsx';
import {Trans, useTranslation} from 'react-i18next';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;
const {Text} = Typography;
const defaultEditorHeight = 272;
const defaultDrawerWidth = 1232;
const successCode = 0;

_CodePanel.propTypes = {
    input: PropTypes.array.isRequired,
    disabled: PropTypes.bool,
    dispatch: PropTypes.func
};

/**
 * code编辑器面板
 *
 * @param shapeStatus 图形状态
 * @param input 数据
 * @param dispatch 回调
 * @return {JSX.Element}
 * @constructor
 */
function _CodePanel({disabled, input, dispatch}) {
    const [open, setOpen] = useState(false);
    const {t} = useTranslation();
    const shape = useShapeContext();
    const url = shape.graph.configs.find(config => config.node === "codeNodeState") && shape.graph.configs.find(config => config.node === "codeNodeState").urls.testCodeUrl;
    const form = useFormContext();
    const suggestions = input.find(item => item.name === "args").value.map(arg => {
        return {label: arg.name, insertText: arg.name}
    });
    const selectedLanguage = input.find(item => item.name === "language").value;
    const code = input.find(item => item.name === "code").value;

    /**
     * 更新code代码
     *
     * @param value 新的code代码
     */
    const editCode = (value) => {
        dispatch({actionType: 'editCode', value: value});
        form.setFieldsValue({[`codeEditor-${shape.id}`]: value});
    };

    /**
     * 点击弹出code编辑页面
     *
     * @param event 事件
     */
    const handleEditClick = (event) => {
        event.stopPropagation();
        setOpen(true);
    };

    /**
     * 代码执行结果展示的回调方法
     *
     * @param currentCode 调试区代码
     * @param args 入参
     * @param language 编程语言
     * @param callback 回调方法
     * @param errorCallback 错误发生时的回调
     */
    const executeFunc = (currentCode, args, language, callback, errorCallback) => {
        const input = {};
        try {
            input.args = JSON.parse(args);
        } catch (e) {
            errorCallback(`${t('codeInputErrorMsg')}${e.message}`);
            return;
        }
        input.code = currentCode;
        input.language = language;
        httpUtil.post(url, input, new Map(), (response) => {
            if (response.code === successCode) {
                callback(response.data);
            } else {
                errorCallback(response.msg);
            }
        }, (err) => {
            errorCallback(t('codeExecuteErrorMsg'));
        });
    };

    return (<>
        <JadeCollapse defaultActiveKey={["codeOutputPanel"]}>
            {<Panel key={"codeOutputPanel"}
                    header={<Header handleEditClick={handleEditClick} disabled={disabled}/>}
                    className="jade-panel"
            >
                <Form.Item
                  name={`codeEditor-${shape.id}`}
                  initialValue={code}
                  rules={[
                      {
                          required: true,
                          message: t('codeCannotEmpty'),
                      },
                  ]}>
                    <div style={{height: `${defaultEditorHeight}px`}}>
                        <CodeEditor code={code} language={selectedLanguage} onChange={editCode}/>
                    </div>
                </Form.Item>
                <CodeDrawer container={shape.page.graph.div.parentElement}
                            width={defaultDrawerWidth}
                            open={open}
                            languages={["python"]}
                            editorConfig={{
                                language: selectedLanguage, code: code, suggestions: suggestions
                            }}
                            onClose={() => setOpen(false)}
                            onConfirm={(v) => editCode(v)}
                            executeFunc={executeFunc}/>
            </Panel>}
        </JadeCollapse>
    </>);
}

/**
 * 代码折叠区域头部组件
 *
 * @param handleEditClick 弹出编辑弹框按钮的回调
 * @param disabled 是否禁用
 * @return {JSX.Element} Header组件
 * @constructor
 */
const Header = ({handleEditClick, disabled}) => {
    const {t} = useTranslation();
    const content = (<div className={"jade-font-size"} style={{lineHeight: "1.2"}}>
        <Trans i18nKey="codePopover" components={{p: <p/>}}/>
    </div>);

    return (<>
        <div className="panel-header">
            <span>
                <span className="jade-panel-header-font">{t('code')}</span>
                <Text type="danger">*</Text>
            </span>
            <Popover
              content={content}
              align={{offset: [0, 3]}}
              overlayClassName={'jade-custom-popover'}
            >
                <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
            </Popover>
            <Button type="link"
                    className={"button-edit"}
                    onClick={handleEditClick}
                    disabled={disabled}
            >
                <div className={"button-wrapper"}><EditCode/>
                    <div className={'edit-button-text'}>{t('editInIde')}</div>
                </div>
            </Button>
        </div>
    </>);
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.disabled === nextProps.disabled && ArrayUtil.isEqual(prevProps.input, nextProps.input);
};

export const CodePanel = React.memo(_CodePanel, areEqual);