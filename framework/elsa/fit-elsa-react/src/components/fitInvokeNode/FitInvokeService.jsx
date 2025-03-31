/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse} from "antd";
import React from "react";
import FitSelectGenericable from "@/components/fitInvokeNode/FitSelectGenericable.jsx";
import FitSelectTool from "@/components/fitInvokeNode/FitSelectTool.jsx";

const {Panel} = Collapse;
/**
 * fit调用服务和实现选择
 *
 * @returns {JSX.Element}
 */
export default function FitInvokeService() {

    return (
        <Collapse bordered={false} className="jade-custom-collapse"
                  defaultActiveKey={['FitInvokeService']}>
            <Panel
                className="jade-panel"
                header={<div style={{display: 'flex', alignItems: 'center'}}>
                    <span className={'title'}>FIT服务</span>
                </div>}
                key='FitInvokeService'
            >
                <div className={"jade-custom-panel-content"}>
                    <div style={{marginTop: "8px"}}><span className='select-genericable'>选择服务</span></div>
                    <FitSelectGenericable/>

                    <div style={{marginTop: "8px"}}><span className='select-fitable'>选择实现</span></div>
                    <FitSelectTool/>
                </div>
            </Panel>
        </Collapse>
    );
};