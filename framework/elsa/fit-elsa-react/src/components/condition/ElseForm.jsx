/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse} from 'antd';

const {Panel} = Collapse;

/**
 * 兜底条件表单。
 *
 * @returns {JSX.Element} 兜底条件表单的DOM。
 */
export default function ElseForm() {
    return (
        <Panel
            key={"elsePanel"}
            header={
                <div className="panel-header">
                    <span className="jade-panel-header-font">Else</span>
                </div>
            }
            className="jade-panel"
        >
        </Panel>
    );
}