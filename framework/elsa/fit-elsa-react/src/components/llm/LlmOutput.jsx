/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Checkbox, Collapse, Form, Popover} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import '../common/style.css';
import {JadeObservableTree} from '@/components/common/JadeObservableTree.jsx';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

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
    const {t} = useTranslation();
 
    const content = (
        <div className={"jade-font-size"}>
            <p>{t('llmOutputPopover')}</p>
        </div>
    );

    return (
        <JadeCollapse defaultActiveKey={["outputPanel"]}>
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
                        </div>
                    }
                    className="jade-panel"
                    forceRender
                >
                    <div className={"jade-custom-panel-content"}>
                        <Form.Item className='jade-form-item' name={`enableLog-${enableLogData.id}`}>
                            <Checkbox checked={enableLogData.value} disabled={disabled}
                                      onChange={e => dispatch({ type: 'updateLogStatus', value: e.target.checked})}><span
                              className={'jade-font-size'}>{t('pushResultToChat')}</span></Checkbox>
                        </Form.Item>
                        <JadeObservableTree data={outputItems}/>
                    </div>
                </Panel>
            }
        </JadeCollapse>
    );
}

// 对象不变，不刷新组件.
const areEqual = (prevProps, nextProps) => {
    return prevProps.outputItems === nextProps.outputItems && prevProps.enableLogData === nextProps.enableLogData && prevProps.disabled === nextProps.disabled;
};

export const LlmOutput = React.memo(_LlmOutput, areEqual);