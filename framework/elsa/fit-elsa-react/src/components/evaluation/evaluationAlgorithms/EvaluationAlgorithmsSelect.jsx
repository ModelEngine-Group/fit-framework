/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Form} from "antd";
import {JadeStopPropagationSelect} from "@/components/common/JadeStopPropagationSelect.jsx";
import {useDispatch, useShapeContext} from "@/components/DefaultRoot.jsx";
import ArrayUtil from "@/components/util/ArrayUtil.js";
import React from "react";
import PropTypes from "prop-types";

_EvaluationAlgorithmsSelect.prototypes = {
    algorithms: PropTypes.array,
    disabled: PropTypes.bool,
    selectedAlgorithm: PropTypes.object
};

/**
 * 算法选择组件
 *
 * @param algorithms 接口获取的算法列表
 * @param disabled 是否禁用
 * @param selectedAlgorithm 选择的算法
 * @return {JSX.Element} 算法选择组件
 * @constructor
 */
function _EvaluationAlgorithmsSelect({algorithms, disabled, selectedAlgorithm}) {
    const dispatch = useDispatch();
    const shape = useShapeContext();

    /**
     * 将返回的算法元数据信息转换为select组件可以展示的选项
     *
     * @param dataList 算法描述信息
     */
    const getAlgorithmOptions = (dataList) => {
        return dataList.map(data => {
            return {
                value: data.uniqueName,
                label: data.name
            }
        });
    };

    /**
     * 切换算法
     *
     * @param value 算法唯一名称
     */
    const handleAlgorithmChange = (value) => {
        const schema = algorithms.find(item => item.uniqueName === value);
        dispatch({type: "changeAlgorithm", value: schema});
    };

    const filterOption = (input, option) =>
        (option?.label ?? '').toLowerCase().includes(input.toLowerCase());

    /**
     * 清空选项
     */
    const handleKnowledgeClear = () => {
        dispatch({type: "clearAlgorithm"});
    };

    return (<>
        <div className={"jade-custom-panel-content"}>
            <Form.Item
                id={`jade-form-item-evaluation-${shape.id}`}
                className="jade-form-item"
                label="算法名称"
                name={`jade-form-item-evaluation-${shape.id}`}
                initialValue={selectedAlgorithm}
                rules={[{required: true, message: "算法不能为空"}]}
                validateTrigger="onBlur"
            >
                <JadeStopPropagationSelect
                    disabled={disabled}
                    onClear={() => handleKnowledgeClear()}
                    showSearch
                    allowClear
                    placeholder="请选择算法"
                    className="jade-select"
                    filterOption={filterOption}
                    optionFilterProp="label"
                    onChange={(value) => handleAlgorithmChange(value)}
                    options={getAlgorithmOptions(algorithms)}
                />
            </Form.Item>
        </div>
    </>);
}

const areEqual = (prevProps, nextProps) => {
    return prevProps.disabled === nextProps.disabled
        && ArrayUtil.isEqual(prevProps.algorithms, nextProps.algorithms)
        && prevProps.selectedAlgorithm === nextProps.selectedAlgorithm;
};

export const EvaluationAlgorithmsSelect = React.memo(_EvaluationAlgorithmsSelect, areEqual);
