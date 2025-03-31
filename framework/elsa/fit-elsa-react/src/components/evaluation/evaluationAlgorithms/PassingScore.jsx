/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDispatch, useShapeContext} from "@/components/DefaultRoot.jsx";
import {Form, Input} from "antd";
import React from "react";

/**
 * 算法及格分数输入框
 *
 * @param score 分数
 * @param disabled 是否禁用
 * @return {JSX.Element} 算法及格分数输入框
 * @constructor
 */
function _PassingScore({score, disabled}) {
    const dispatch = useDispatch();
    const shape = useShapeContext();

    /**
     * 切换算法
     *
     * @param value 算法
     */
    const handleScoreChange = (value) => {
        dispatch({type: "editScore", value: value});
    };

    return (<>
        <div className={"jade-custom-panel-content"}>
            <Form.Item
                id={`score-input-${shape.id}`}
                className="jade-form-item"
                label="算法及格分数"
                name={`score-input-form-${shape.id}`}
                rules={[{required: true, message: "算法及格分数不能为空"},
                    {
                        pattern: /^[0-9]*$/,
                        message: '只能包含数字'
                    }]}
                validateTrigger="onBlur"
                initialValue={score}
            >
                <Input
                    className="jade-input"
                    id={`score-input-${shape.id}`}
                    value={score}
                    disabled={disabled}
                    placeholder="请输入算法及格分数"
                    showCount
                    onChange={e => handleScoreChange(e.target.value)}
                />
            </Form.Item>
        </div>
    </>);
}

const areEqual = (prevProps, nextProps) => {
    return prevProps.disabled === nextProps.disabled && prevProps.score === nextProps.score;
};

export const PassingScore = React.memo(_PassingScore, areEqual);