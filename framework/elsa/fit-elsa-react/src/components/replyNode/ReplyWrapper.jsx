import React from 'react';
import PropTypes from 'prop-types';
import {JadeInputForm} from '../common/JadeInputForm.jsx';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {TemplatePanel} from "@/components/common/TemplatePanel.jsx";

ReplyWrapper.propTypes = {
    data: PropTypes.object.isRequired,
    shapeStatus: PropTypes.object,
};

export default function ReplyWrapper({data, shapeStatus}) {
    const dispatch = useDispatch();
    const shape = useShapeContext();
    const template =  data.inputParams.find(item => item.name === 'template');

    /**
     * 初始化数据
     *
     * @return {*}
     */
    const initItems = () => {
        return data.inputParams.find(item => item.name === "variables").value
    };

    /**
     * 添加输入的变量
     *
     * @param id id 数据id
     */
    const addItem = (id) => {
        // 代码节点入参最大数量为20
        if (data.inputParams.find(item => item.name === 'variables').value.length < 20) {
            dispatch({type: 'addInputParam', id: id});
        }
    };

    /**
     * 更新入参变量属性名或者类型
     *
     * @param id 数据id
     * @param value 新值
     */
    const updateItem = (id, value) => {
        dispatch({type: 'editInputParam', id: id, newValue: value});
    };

    /**
     * 删除input
     *
     * @param id 需要删除的数据id
     */
    const deleteItem = (id) => {
        dispatch({type: 'deleteInputParam', id: id});
    };

    return (
        <div>
            <JadeInputForm
                shapeStatus={shapeStatus}
                items={initItems()}
                addItem={addItem}
                updateItem={updateItem}
                deleteItem={deleteItem}
                content={<div>请输入变量</div>}
                maxInputLength={1000}
            />
            <TemplatePanel
                disabled={shapeStatus.disabled}
                template={template}
                title={"直接回复"}
                placeHolder={"aaa"}
                name={"reply"}
                onChange={(templateText) => {
                    dispatch({type: 'changeTemplate', id: template.id, value: templateText});
                }}
                labelName={"回复内容"}
            />
        </div>
    );
}
