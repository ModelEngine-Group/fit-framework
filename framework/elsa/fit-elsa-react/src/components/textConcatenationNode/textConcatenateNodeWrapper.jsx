import React from 'react';
import PropTypes from 'prop-types';
import {JadeInputForm} from '../common/JadeInputForm.jsx';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {TemplatePanel} from "@/components/common/TemplatePanel.jsx";
import {InvokeOutput} from "@/components/common/InvokeOutput.jsx";
import {Trans, useTranslation} from "react-i18next";

TextConcatenateNodeWrapper.propTypes = {
    data: PropTypes.object.isRequired,
    shapeStatus: PropTypes.object,
};

export default function TextConcatenateNodeWrapper({data, shapeStatus}) {
    const dispatch = useDispatch();
    const {t} = useTranslation();
    const template =  data.inputParams.find(item => item.name === 'template');

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
        // 入参最大数量为20
        if (data.inputParams.find(item => item.name === 'args').value.length < 20) {
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

    const getOutputDescription = () => {
        return (<>
            <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
                <Trans i18nKey='textConcatenationOutputPopover' components={{p: <p/>}}/>
            </div>
        </>);
    };

    const content =
        (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
            <Trans i18nKey='templateInputPopover' components={{p: <p/>}}/>
        </div>)

    return (
        <div>
            <JadeInputForm
                shapeStatus={shapeStatus}
                items={initItems()}
                addItem={addItem}
                updateItem={updateItem}
                deleteItem={deleteItem}
                content={content}
                maxInputLength={1000}
            />
            <TemplatePanel
                disabled={shapeStatus.disabled}
                template={template}
                title={t("concatenatedTextLabel")}
                placeHolder={t("promptPlaceHolder")}
                name={"template"}
                onChange={(templateText) => {
                    dispatch({type: 'changeTemplate', id: template.id, value: templateText});
                }}
                labelName={t("concatenatedTextLabel")}
                maxLength={1000}
            />
            <InvokeOutput outputData={data.outputParams} getDescription={getOutputDescription}/>
        </div>
    );
}
