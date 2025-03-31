/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import "./style.css";
import PropTypes from "prop-types";
import {JadeStopPropagationSelect} from "../common/JadeStopPropagationSelect.jsx";

ByTime.propTypes = {
    propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的string类型
    onValueChange: PropTypes.func.isRequired, // 确保 onNameChange 是一个必需的函数类型
};

/**
 * Memory按时间选取
 *
 * @param propValue 前端渲染的值
 * @param onValueChange 参数变化所需调用方法
 * @param disabled 禁用.
 * @returns {JSX.Element} Memory按时间的Dom
 */
export default function ByTime({propValue, onValueChange, disabled}) {
    const value = propValue;

    const handleSelectClick = (event) => {
        event.stopPropagation(); // 阻止事件冒泡
    };

    // Filter `option.label` match the user type `input`
    const filterOption = (input, option) => (option?.label ?? '').toLowerCase().includes(input.toLowerCase());

    const handleChange = (value) => {
        onValueChange("String", value); // 当选择框的值发生变化时调用父组件传递的回调函数
        document.activeElement.blur();// 在选择后取消焦点
    };

    return (<>
            <JadeStopPropagationSelect
                showSearch
                disabled={disabled}
                className="jade-select"
                style={{width: "100%"}}
                onClick={handleSelectClick} // 点击下拉框时阻止事件冒泡
                onChange={handleChange}
                filterOption={filterOption}
                options={[{value: 'oneHour', label: '近1小时'}, {value: 'sixHour', label: '近6小时'},
                    {value: 'twelveHour', label: '近12小时'}, {value: 'oneDay', label: '近1天'},
                    {value: 'twoDays', label: '近2天'}, {value: 'oneWeek', label: '近1周'},]}
                value={value}
            />
        </>);
}