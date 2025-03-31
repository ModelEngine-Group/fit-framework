/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JadeStopPropagationSelect} from "@/components/common/JadeStopPropagationSelect.jsx";
import {useState} from "react";
import httpUtil from "@/components/util/httpUtil.jsx";

/**
 * 通用分页加载下拉框组件
 *
 * @return {JSX.Element}
 * @constructor
 * @param props
 */
export default function JadeScrollSelect(props) {
    const [options, setOptions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(1);
    const {
        buildUrl,
        onChange,
        getOptions,
        disabled,
        dealResponse,
        ...rest
    } = props;
    const url = buildUrl(page);

    /**
     * 下拉框展开时进行接口调用
     *
     * @param open 是否打开
     * @return {Promise<void>}
     */
    const handleDropdown = async (open) => {
        if (!open) {
            return;
        }
        setLoading(true); // 设置加载状态为 true
        httpUtil.get(url, new Map(), (response) => {
            const data = dealResponse(response);
            // 更新下拉框选项
            data && setOptions(data);
            setLoading(false);
        }, (error) => {
            setLoading(false);
        })
    };

    /**
     * 下拉框滑动条到底部后触发接口调用
     *
     * @param e 动作事件
     * @return {Promise<void>}
     */
    const handlePopupScroll = async (e) => {
        const {target} = e;
        // 未滚动到底部，直接返回
        if (target.scrollTop + target.clientHeight !== target.scrollHeight) {
            return;
        }
        // 加载新页面的操作
        const curPage = page;
        setPage(page + 1);
        setLoading(true); // 设置加载状态为 true
        httpUtil.get(url, new Map(), (response) => {
            const data = response.data;
            // 更新下拉框选项，合并之前的数据
            data && setOptions([...options, ...data]);
            setLoading(false);
        }, (error) => {
            setLoading(false);
            // 数据加载失败，重新加载当前页
            setPage(curPage);
        })
    };

    /**
     * 选择option时发生的回调
     *
     * @param value 数据
     */
    const handleOnChange = (value) => {
        onChange(value, options);
    }

    return (
        <JadeStopPropagationSelect
            className={"jade-select"}
            style={{width: '100%'}}
            onPopupScroll={handlePopupScroll}
            onDropdownVisibleChange={handleDropdown}
            onChange={handleOnChange}
            disabled={disabled ? disabled : false}
            options={getOptions(options)}
            loading={loading}
            mode="single"
            {...rest}
        />
    );
}