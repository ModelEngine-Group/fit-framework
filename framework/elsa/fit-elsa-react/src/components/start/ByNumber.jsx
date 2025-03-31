/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Slider} from "antd";
import "./style.css";
import PropTypes from "prop-types";

ByNumber.propTypes = {
    propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的number类型
    onValueChange: PropTypes.func.isRequired, // 确保 onNameChange 是一个必需的函数类型
};

/**
 * Memory按条数选取
 *
 * @param propValue 前端渲染的值
 * @param onValueChange 参数变化所需调用方法
 * @param disabled 禁用.
 * @returns {JSX.Element} Memory按条数的Dom
 */
export default function ByNumber({propValue, onValueChange, disabled}) {
    const floatValue = parseFloat(propValue);

    return (<div style={{display: 'flex', alignItems: 'center'}}>
            <Slider
                style={{width: "90%"}} // 设置固定宽度
                min={1}
                disabled={disabled}
                max={100}
                defaultValue={20}
                step={1}
                onChange={e => onValueChange("Integer", e.toString())}
                value={!isNaN(floatValue) ? floatValue : 20}
            />
            <span style={{marginLeft: '8px'}}>{floatValue}</span>
        </div>);
}