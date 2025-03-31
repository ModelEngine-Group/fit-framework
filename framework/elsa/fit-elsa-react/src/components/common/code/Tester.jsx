/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Card, message} from 'antd';
import {CaretRightOutlined} from '@ant-design/icons';
import {CodeEditor} from '@/components/common/code/CodeEditor.jsx';
import {JsonViewer} from '@/components/common/JsonViewer.jsx';
import {useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';

/**
 * 测试组件.
 *
 * @param codeRef code编辑区代码引用
 * @param executeFunc 执行方法.
 * @param language 语言.
 * @param suggestions 建议，用于在输入中进行联想.
 * @return {JSX.Element}
 * @constructor
 */
export const Tester = ({codeRef, executeFunc, language, suggestions = []}) => {
    const [messageApi, contextHolder] = message.useMessage();
    const { t } = useTranslation();
    const [output, setOutput] = useState({});
    const [isRunning, setIsRunning] = useState(false);
    const inputRef = useRef("{}");

    /**
     * 使用老版本的API进行代码复制操作
     */
    const fallbackCopy = () => {
        const textToCopy = JSON.stringify(output);

        // 创建一个临时的textarea元素
        const textArea = document.createElement("textarea");
        textArea.value = textToCopy;
        document.body.appendChild(textArea);
        textArea.select();
        textArea.setSelectionRange(0, 99999); // 对移动设备进行处理

        try {
            document.execCommand("copy");
            messageApi.open({type: 'success', content: t('copySucceeded')});
        } catch (err) {
            messageApi.open({type: 'error', content: t('copyFailed')});
        }

        // 移除临时的textarea元素
        document.body.removeChild(textArea);
    };

    const onCopyClick = () => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(JSON.stringify(output)).then(() => {
            });
            messageApi.open({type: 'success', content: t('copySucceeded')}).then(() => {
            }).catch(err => {
                fallbackCopy();
            });
        } else {
            fallbackCopy();
        }
    };

    const onTestInputChange = (v) => {
        inputRef.current = v;
    };

    const runTest = () => {
        setIsRunning(true);
        executeFunc(codeRef.current, inputRef.current, language, (output) => {
            setOutput(output);
            setIsRunning(false);
        }, (error) => {
            setOutput(error);
            setIsRunning(false);
        });
    };

    const formatOutput = (output) => {
        if (output === undefined) {
            return 'undefined';
        }
        if (output === null) {
            return 'null';
        }
        if (typeof output === 'boolean') {
            return output.toString();
        }
        return output;
    };
    const isNull = (judgeVar) =>{
        return judgeVar === null || judgeVar === undefined;
    };

    const codeContent = `{\n${suggestions.map(item => `    "${item.label}": ""`).join(',\n')}\n}`;

    return (<>
        {contextHolder}
        <div className={"jade-code-test-input jade-code-parent"}>
            <div className={"jade-code-test-input-title"}>
                <span>{t('input')}</span>
            </div>
            <div className={"jade-code-test-input-content"}>
                <CodeEditor language={"json"}
                            code={codeContent}
                            options={{readOnly: false, lineNumbers: "on"}}
                            onChange={onTestInputChange}/>
            </div>
            <div className={"jade-code-test-input-footer"}>
                <Button type="primary"
                        disabled={isRunning}
                        className={"jade-code-button"}
                        style={{marginLeft: 0, fontWeight: 700}}
                        icon={<CaretRightOutlined/>}
                        onClick={runTest}>
                    {t('run')}
                </Button>
            </div>
        </div>
        <div className={"jade-code-test-output jade-code-parent"}>
            <div className={"jade-code-test-output-title"}>
                <span>{t('output')}</span>
            </div>
            <div className={"jade-code-test-output-content"}>
                {(!isNull(output) && typeof output === 'object') ?
                        <div className={"jade-code-test-output-content-wrapper"}>
                            <JsonViewer jsonData={output}/>
                        </div>
                        :
                        <Card className={"code-run-style"}>
                            {formatOutput(output)}
                        </Card>}
            </div>
            <div className={"jade-code-test-output-footer"}>
                <Button className={"jade-code-button"}
                        style={{marginLeft: 0}}
                        onClick={onCopyClick}>
                    {t('copy')}
                </Button>
            </div>
        </div>
    </>);
};