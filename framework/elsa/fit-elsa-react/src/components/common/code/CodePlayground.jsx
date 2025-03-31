/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useRef, useState} from 'react';
import './codePlaygroundStyle.css';
import {Tester} from '@/components/common/code/Tester.jsx';
import {CodeEditor} from '@/components/common/code/CodeEditor.jsx';
import {Button, Dropdown, Space, Popover} from 'antd';
import {CloseOutlined, DownOutlined, QuestionCircleOutlined, RightOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';

const TEST_WIDTH = 600;

/**
 * 代码调试器.
 *
 * @param width 宽度.
 * @param languages 语言.
 * @param editorConfig 编辑器配置.
 * @param onClose 关闭时的回调.
 * @param onConfirm 确认时的回调.
 * @param executeFunc 执行函数，该函数第一个参数为参数args，第二参数为回调.
 * @return {JSX.Element}
 * @constructor
 */
const CodePlayground = ({width, languages, editorConfig, onClose, onConfirm, executeFunc}) => {
    const {t} = useTranslation();
    const [ctl, setCtl] = useState({testStatus: "none", codeWidth: width});
    const codeRef = useRef(editorConfig.code);
    const [language, setLanguage] = useState(editorConfig.language);
    const defaultKey = languages.indexOf(editorConfig.language);
    const items = languages.map((l, i) => {
        return {key: String(i), label: l};
    });

    const content = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='codePopover' components={{p: <p/>}}/>
    </div>);

    const onLanguageClick = (e) => {
        setLanguage(items.find(i => i.key === e.key)?.label);
    };

    const handleCancel = () => {
        onClose();
    };

    const handleConfirm = () => {
        onConfirm(codeRef.current);
    };

    /**
     * 语言下拉组件.
     *
     * @return {JSX.Element} 组件对象.
     * @constructor
     */
    const LanguageDropDown = () => {
        return (<>
            <div className={"code-title-language"} style={{display: "flex"}}>
                <span style={{color: 'rgb(128, 128, 128)'}}>{t('language')}</span>
                <div style={{marginLeft: 8}}>
                    <Dropdown getPopupContainer={trigger => trigger.parentNode}
                              trigger={'click'}
                              menu={{
                                  items, selectable: true, defaultSelectedKeys: [defaultKey], onClick: onLanguageClick
                              }}
                    >
                        <a onClick={(e) => e.preventDefault()}>
                            <Space style={{color: "rgb(26, 26, 26)"}}>
                                {language}
                                <DownOutlined/>
                            </Space>
                        </a>
                    </Dropdown>
                </div>
            </div>
        </>);
    };

    /**
     * 代码标题组件.
     *
     * @return {JSX.Element} 组件对象.
     * @constructor
     */
    const CodeTitle = () => {
        return (<>
            <div style={{width: '8%', minWidth: 100, display: 'flex', alignItems: 'center'}}>
                <div className="code-title-text"><span>{t('editCode')}</span></div>
                <Popover
                    content={content}
                    align={{offset: [0, 3]}}
                    overlayClassName={'jade-custom-popover'}
                >
                    <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
                </Popover>
            </div>
            <div style={{width: "77%"}}>
                <LanguageDropDown/>
            </div>
            <div style={{width: "15%", display: "flex", justifyContent: "end"}}>
                <div style={{display: "flex", alignItems: "center"}}>
                    <Button onClick={onTestButtonClick} className={'code-title-test-text'} type='text'>{t('testCode')}</Button>
                    <Button onClick={handleCancel}
                            style={{width: 16, height: 16, marginRight: 0}}
                            type="text"
                            icon={<CloseOutlined/>}/>
                </div>
            </div>
        </>);
    };

    const onTestClose = () => {
        setCtl({testStatus: "none", codeWidth: width});
    };

    const onTestButtonClick = () => {
        const isFlex = ctl.testStatus === "flex";
        setCtl({
            testStatus: isFlex ? "none" : "flex", codeWidth: isFlex ? width : width - TEST_WIDTH
        });
    };

    return (<>
        <div className={"jade-code-container"} style={{width: width, height: "100%"}}>
            <div className={"jade-code-code jade-code-parent"} style={{width: ctl.codeWidth}}>
                <div className={"jade-code-code-title"}>
                    <CodeTitle/>
                </div>
                <div className={"jade-code-code-content"}>
                    <CodeEditor language={language}
                                code={editorConfig.code}
                                options={{readOnly: false}}
                                suggestions={editorConfig.suggestions}
                                onChange={(v) => {
                                    codeRef.current = v;
                                }}/>
                </div>
                <div className={"jade-code-code-footer"}>
                    <Button className={"jade-code-button"} style={{marginRight: 16}}
                            onClick={handleCancel}>{t('cancel')}</Button>
                    <Button type="primary"
                            className={"jade-code-button"}
                            style={{marginRight: 0, fontWeight: 700}}
                            onClick={() => handleConfirm()}>{t('ok')}</Button>
                </div>
            </div>
            <div className={"jade-code-test jade-code-parent"} style={{display: ctl.testStatus}}>
                <div className={"jade-code-test-header"}>
                    <div className={"jade-code-test-header-text"}>
                        <span>{t('testCode')}</span>
                    </div>
                    <div className={"jade-code-test-header-close"}>
                        <Button onClick={onTestClose}
                                style={{width: 16, height: 16, marginLeft: 16}}
                                type="text"
                                icon={<RightOutlined/>}/>
                    </div>
                </div>
                <div className={"jade-code-test-content jade-code-parent"}>
                    <Tester codeRef={codeRef}
                            executeFunc={executeFunc}
                            suggestions={editorConfig.suggestions}
                            language={language}/>
                </div>
            </div>
        </div>
    </>);
};

CodePlayground.propTypes = {
    width: PropTypes.number.isRequired,
    languages: PropTypes.array,
    editorConfig: PropTypes.object,
    onClose: PropTypes.func,
    onConfirm: PropTypes.func,
    executeFunc: PropTypes.func,
};

export {CodePlayground};