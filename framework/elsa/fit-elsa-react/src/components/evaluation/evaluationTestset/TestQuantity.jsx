/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useShapeContext} from "@/components/DefaultRoot.jsx";
import {Form} from "antd";
import React from "react";

/**
 * 算法及格分数输入框
 *
 * @param quantity 测试集数量
 * @param disabled 是否禁用
 * @return {JSX.Element} 测试集数量展示组件
 * @constructor
 */
function _TestQuantity({quantity}) {
    const shape = useShapeContext();

    return (<>
        <div className={"jade-common-content"}>
            <Form.Item
                id={`score-input-${shape.id}`}
                className="jade-form-item"
                label="用例数量"
                initialValue={quantity}
            >
                <span style={{paddingLeft: "4px"}}>{quantity}</span>
            </Form.Item>
        </div>
    </>);
}

const areEqual = (prevProps, nextProps) => {
    prevProps.quantity === nextProps.quantity;
};

export const TestQuantity = React.memo(_TestQuantity, areEqual);