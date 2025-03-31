/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Input} from "antd";

/**
 * 该Input的mousedown事件不会冒泡.
 *
 * @param props 参数.
 * @return {JSX.Element}
 * @constructor
 */
export const JadeInput = (props) => {
    const {onMouseDown, ...rest} = props;

    const _onMouseDown = (e) => {
        onMouseDown && onMouseDown(e);
    };

    return (<>
        <Input {...rest} onMouseDown={_onMouseDown} />
    </>);
};