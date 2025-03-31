/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JadeObservableTree} from "@/components/common/JadeObservableTree.jsx";
import {useDataContext} from "@/components/DefaultRoot.jsx";
import React from "react";
import {Collapse} from "antd";
import {useTranslation} from "react-i18next";

const {Panel} = Collapse;
/**
 * fit接口出参展示
 *
 * @returns {JSX.Element}
 */
export default function FitInvokeOutput() {
    const data = useDataContext();
    const {t} = useTranslation();
    const outputData = data && data.outputParams;

    return (<Collapse bordered={false} className="jade-custom-collapse"
                      defaultActiveKey={['FitInvokeOutput']}>
        <Panel
            className="jade-panel"
            header={<div style={{display: 'flex', alignItems: 'center'}}>
                <span className={'title'}>{t('output')}</span>
            </div>}
            key='FitInvokeOutput'>
            <div className={"jade-custom-panel-content"}>
                <JadeObservableTree data={outputData}/>
            </div>
        </Panel>
    </Collapse>);
}