/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from "react";
import {useDataContext, useDispatch, useShapeContext} from "@/components/DefaultRoot.jsx";
import JadeScrollSelect from "@/components/common/JadeScrollSelect.jsx";
import {Form} from "antd";

/**
 * 工具选择
 *
 * @returns {JSX.Element}
 */
export default function FitSelectTool() {
    const dispatch = useDispatch();
    const data = useDataContext();
    const shape = useShapeContext();

    // 当前被选中的genericable，调接口需要用到这个数据
    const selectGenericable = data && data.genericable.value.find(item => item.name === "id").value;
    const baseUrl = shape.graph.configs.find(config => config.node === "fitInvokeState").urls.fitableMetaInfoUrl;
    const selectDisabled = selectGenericable === '';

    /**
     * 构造当前组件的url
     *
     * @param page 页码
     * @return {string}
     */
    const buildUrl = (page) => {
        return baseUrl + selectGenericable + '?pageNum=' + page + '&' + 'pageSize=10';
    }

    /**
     * 选中工具时触发的函数
     *
     * @param value 选中的工具名称
     * @param options 接口返回的数据
     */
    const handleToolChange = (value, options) => {
        dispatch({type: "selectFitable", value: options.find(option => option.name === value)});
    };

    /**
     * 获取工具组件的options
     *
     * @param options 选项
     * @return {*}
     */
    const getOptions = options => options.map(option => ({
        value: option.name,
        label: option.name,
    }));

    return (
        <Form.Item
            name={`select-fitable-${shape.id}`}
            label={<span style={{color: 'red'}}/>}
            rules={[{required: true, message: '请选择一个服务，再选择实现'}]}
            colon={false}
        >
            <JadeScrollSelect
                className="jade-select-tool"
                placeholder="请选择一个服务，再选择实现"
                onChange={handleToolChange}
                buildUrl={buildUrl}
                disabled={selectDisabled}
                getOptions={getOptions}
                dealResponse={(response) => response.data}
            />
        </Form.Item>
    );
};