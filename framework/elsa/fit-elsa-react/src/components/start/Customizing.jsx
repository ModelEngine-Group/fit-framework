/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import "./style.css";
import PropTypes from "prop-types";
import {JadeStopPropagationSelect} from "../common/JadeStopPropagationSelect.jsx";
import {useEffect, useState} from "react";
import httpUtil from "@/components/util/httpUtil.jsx";

Customizing.propTypes = {
    propValue: PropTypes.oneOfType([PropTypes.string, PropTypes.oneOf([null])]), // 确保 propValue 是一个必需的string类型或者null
    onValueChange: PropTypes.func.isRequired, // 确保 onValueChange 是一个必需的函数类型
    config: PropTypes.object.isRequired, // 确保 config 是一个必需的对象类型
};

/**
 * Memory按自定义选取
 *
 * @param propValue 前端渲染的值
 * @param onValueChange 参数变化所需调用方法
 * @param config 配置
 * @param disabled 禁用.
 * @returns {JSX.Element} 按自定义选取的Dom
 */
export default function Customizing({propValue, onValueChange, config, disabled}) {
    const value = propValue;
    const [fitableOptions, setFitableOptions] = useState([]);

    const handleSelectClick = (event) => {
        event.stopPropagation(); // 阻止事件冒泡
    };

    // Filter `option.label` match the user type `input`
    const filterOption = (input, option) =>
        (option?.label ?? '').toLowerCase().includes(input.toLowerCase());

    const handleChange = (value) => {
        onValueChange("String", value); // 当选择框的值发生变化时调用父组件传递的回调函数
        document.activeElement.blur();// 在选择后取消焦点
    };

    useEffect(() => {
        // 发起网络请求获取 options 数据
        httpUtil.get(config?.urls?.customHistoryUrl ?? '', new Map(), (jsonData) => setFitableOptions(jsonData.data.map(item => {
            return {
                value: item.fitableId,
                label: item.name
            };
        })))
    }, []); // useEffect 依赖数组为空，表示只在组件挂载时执行一次

    return (<>
            <JadeStopPropagationSelect
                allowClear
                showSearch
                disabled={disabled}
                className="jade-select"
                style={{ width: "100%" }}
                onClick={handleSelectClick} // 点击下拉框时阻止事件冒泡
                onChange={handleChange}
                filterOption={filterOption}
                placeholder="选择合适的获取历史记录服务"
                options={fitableOptions}
                value={value}
            />
        </>
    );
}