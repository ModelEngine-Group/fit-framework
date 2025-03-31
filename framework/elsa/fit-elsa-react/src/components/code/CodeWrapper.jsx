/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDispatch} from "@/components/DefaultRoot.jsx";
import React from "react";
import {CodePanel} from "@/components/code/CodePanel.jsx";
import {JadeObservableOutput} from "@/components/common/JadeObservableOutput.jsx";
import {JadeInputForm} from "@/components/common/JadeInputForm.jsx";
import PropTypes from "prop-types";
import {useTranslation} from 'react-i18next';

CodeWrapper.propTypes = {
    data: PropTypes.object.isRequired,
    shapeStatus: PropTypes.object
};

/**
 * code节点组件
 *
 * @return {*}
 * @constructor
 */
export default function CodeWrapper({data, shapeStatus}) {
    const dispatch = useDispatch();
    const {t} = useTranslation();
    const output = data.outputParams.find(item => item.name === "output");
    const input = data.inputParams;
    /**
     * 初始化数据
     *
     * @return {*}
     */
    const initItems = () => {
        return data.inputParams.find(item => item.name === "args").value
    };

    /**
     * 添加输入的变量
     *
     * @param id id 数据id
     */
    const addItem = (id) => {
        // 代码节点入参最大数量为20
        if (data.inputParams.find(item => item.name === 'args').value.length < 20) {
            dispatch({actionType: 'addInput', id: id});
        }
    };

    /**
     * 更新入参变量属性名或者类型
     *
     * @param id 数据id
     * @param value 新值
     */
    const updateItem = (id, value) => {
        dispatch({actionType: 'editInput', id: id, changes: value});
    };

    /**
     * 删除input
     *
     * @param id 需要删除的数据id
     */
    const deleteItem = (id) => {
        dispatch({actionType: 'deleteInput', id: id});
    };

    const content = (<>
        <div>
            <p>{t('codeInputPopover')}</p>
        </div>
    </>);

    return (<>
        <JadeInputForm
          shapeStatus={shapeStatus}
          items={initItems()}
          addItem={addItem}
          updateItem={updateItem}
          deleteItem={deleteItem}
          content={content}
          maxInputLength={1000}/>
        <CodePanel disabled={shapeStatus.disabled} input={input} dispatch={dispatch}/>
        <JadeObservableOutput disabled={shapeStatus.disabled} output={output}/>
    </>);
}