/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Slider} from "antd";
import "./style.css";
import PropTypes from "prop-types";
import {useEffect} from "react";
import {useShapeContext} from "@/components/DefaultRoot.jsx";

ByConversationTurn.propTypes = {
    propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的number类型
    onValueChange: PropTypes.func.isRequired, // 确保 onNameChange 是一个必需的函数类型
    disabled: PropTypes.bool
};

/**
 * Memory按对话轮次选取
 *
 * @param propValue 前端渲染的值
 * @param onValueChange 参数变化所需调用方法
 * @param disabled 禁用.
 * @returns {JSX.Element} Memory按对话轮次的Dom
 */
export default function ByConversationTurn({propValue, onValueChange, disabled, i18n}) {
    const intValue = parseInt(propValue);
    const shape = useShapeContext && useShapeContext();

    const defaultRecalls = {
        1: '1', [3]: i18n('default'), 10: '10'
    };

    // 注册开始节点轮次数.
    useEffect(() => {
        shape && shape.page.registerObservable({
            nodeId: shape.id,
            observableId: "start_node_conversation_turn_count",
            value: !isNaN(intValue) ? intValue : 3,
            type: "number",
            parentId: null,
            visible: false
        });
    }, []);

    // 修改时，emit数据给监听器.
    const onChange = (e) => {
        onValueChange("Integer", e.toString());
        shape && shape.emit("start_node_conversation_turn_count", {value: e});
    };

    return (<>
        <div style={{display: 'flex', alignItems: 'center'}}>
            <Slider style={{width: '95%'}} // 设置固定宽度
                    min={1}
                    max={10}
                    disabled={disabled}
                    defaultValue={3}
                    marks={defaultRecalls}
                    step={1} // 设置步长为1
                    onChange={onChange}
                    value={!isNaN(intValue) ? intValue : 3}
            />
        </div>
    </>);
}