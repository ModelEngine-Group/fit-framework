/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Form, message} from "antd";
import {useDispatch, useShapeContext} from "@/components/DefaultRoot.jsx";
import React from "react";
import {PlusCircleOutlined} from "@ant-design/icons";
import httpUtil from "@/components/util/httpUtil.jsx";
import PropTypes from "prop-types";
import JadeScrollSelect from "@/components/common/JadeScrollSelect.jsx";

_EvaluationTestSetSelect.propTypes = {
    shapeStatus: PropTypes.object,
    selectedTestSet: PropTypes.array,
    config: PropTypes.object
};

/**
 * 测试集选择组件
 *
 * @param testSets 接口获取的数据集列表
 * @param shapeStatus 图形状态集合
 * @param selectedTestSet 选择的测试集
 * @param config 组件配置信息，外部设置
 * @return {JSX.Element} 测试集选择组件
 * @constructor
 */
function _EvaluationTestSetSelect({shapeStatus, selectedTestSet, config}) {
    const dispatch = useDispatch();
    const shape = useShapeContext();

    /**
     * 将返回的测试集元数据信息转换为select组件可以展示的选项
     *
     * @param dataList 测试集描述信息
     */
    const getTestSetOptions = (dataList) => {
        return dataList.map(data => {
            return {
                value: data.id,
                label: data.name
            }
        });
    };

    /**
     * 切换测试集
     *
     * @param value 算法唯一名称
     */
    const handleAlgorithmChange = (value) => {
        if (!value) {
            return;
        }
        const url = `${config.urls.datasetUrlPrefix}/dataset/${value}`;
        httpUtil.get(url, new Map(), (response) => {
            delete response.data.id;
            const data = {...response.data, id: value};
            // 先删除已经注册的output
            shape.page.removeObservable(shape.id);
            dispatch({type: "changeTestSet", value: data});
        }, () => {
            message.error("数据集详细信息获取失败，请联系系统管理员");
        });
    };

    const filterOption = (input, option) =>
        (option?.label ?? '').toLowerCase().includes(input.toLowerCase());

    /**
     * 清空选项
     */
    const handleKnowledgeClear = () => {
        dispatch({type: "clearTestSet"});
    };

    /**
     * 触发弹出新建测试集弹窗页面
     *
     * @param e 点击事件
     */
    const clickCreateButton = (e) => {
        e.preventDefault();
        shape.page.triggerEvent({
            type: "CREATE_TEST_SET",
            value: {}
        });
    };

    /**
     * 构造当前组件的url
     *
     * @param page 页码
     * @return {string} url
     */
    const buildUrl = (page) => {
        return `${config.urls.datasetUrlPrefix}/dataset?appId=${shape.page.graph.appId}&pageIndex=${page}&pageSize=10`;
    };

    return (<>
        <div className={"jade-common-content"}>
            <Form.Item
                id={`jade-form-item-evaluation-${shape.id}`}
                className="jade-form-item"
                label="测试集名称"
                name={`jade-form-item-evaluation-${shape.id}`}
                initialValue={selectedTestSet}
                rules={[{required: true, message: "测试集不能为空"}]}
                validateTrigger="onBlur"
            >
                <JadeScrollSelect
                    disabled={shapeStatus.disabled}
                    onClear={() => handleKnowledgeClear()}
                    showSearch
                    allowClear
                    placeholder="请选择测试集"
                    className="jade-select"
                    filterOption={filterOption}
                    optionFilterProp="label"
                    onChange={(value) => handleAlgorithmChange(value)}
                    buildUrl={buildUrl}
                    getOptions={getTestSetOptions}
                    dealResponse={(response) => response.data.items}
                />
            </Form.Item>
            <Button
                type="link"
                className={"button-create"}
                disabled={shapeStatus.disabled}
                onClick={e => clickCreateButton(e)}>
                <div className={"button-create-wrapper"}><PlusCircleOutlined/>
                    <div className={"create-button-text"}>新建测试集</div>
                </div>
            </Button>
        </div>
    </>);
}

const areEqual = (prevProps, nextProps) => {
    return prevProps.shapeStatus === nextProps.shapeStatus
        && prevProps.selectedTestSet === nextProps.selectedTestSet
        && prevProps.config === nextProps.config;
};

export const EvaluationTestSetSelect = React.memo(_EvaluationTestSetSelect, areEqual);
