/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from "react";
import {useDispatch, useShapeContext} from "@/components/DefaultRoot.jsx";
import JadeScrollSelect from "@/components/common/JadeScrollSelect.jsx";
import {Form} from "antd";

/**
 * 服务选择
 *
 * @returns {JSX.Element}
 */
export default function FitSelectGenericable() {
    const dispatch = useDispatch();
    const shape = useShapeContext();
    const baseUrl = shape.graph.configs && shape.graph.configs.find(config => config.node === "fitInvokeState").urls.serviceListEndpoint;

    /**
     * 构造当前组件的url
     *
     * @param page 页码
     * @return {string}
     */
    const buildUrl = (page) => {
        return baseUrl + '?pageNum=' + page + '&pageSize=10';
    }

    /**
     * 选中服务时触发的函数
     *
     * @param value 选中的服务的id
     * @param options 接口返回的数据
     */
    const handleServiceChange = (value, options) => {
        dispatch({type: "selectGenericable", value: value});
    };

    /**
     * 获取服务组件的options
     *
     * @param options 选项
     * @return {*}
     */
    const getOptions = options => options.map(option => ({
        value: option,
        label: option,
    }));

    return (
        <Form.Item
            name={`select-genericable-${shape.id}`}
            label={<span style={{color: 'red'}}/>}
            rules={[{required: true, message: '请选择一个服务'}]}
            colon={false}
        >
            <JadeScrollSelect
                className="jade-select-genericable"
                placeholder="请选择一个服务"
                onChange={handleServiceChange}
                buildUrl={buildUrl}
                disabled={false}
                getOptions={getOptions}
                dealResponse={(response) => response.data}
            />
        </Form.Item>
    );
};