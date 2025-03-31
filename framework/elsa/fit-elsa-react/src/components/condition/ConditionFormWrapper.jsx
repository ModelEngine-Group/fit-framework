/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {IfForm} from '@/components/condition/IfForm.jsx';
import ElseForm from "@/components/condition/ElseForm.jsx";
import {useDispatch} from "@/components/DefaultRoot.jsx";
import {Button} from "antd";
import {PlusOutlined} from "@ant-design/icons";
import {ConnectorProvider} from "@/components/common/ConnectorProvider.jsx";
import PropTypes from "prop-types";
import {useTranslation} from "react-i18next";

ConditionFormWrapper.propTypes = {
    shapeStatus: PropTypes.object,
    data: PropTypes.object
};

/**
 * 条件节点表单Wrapper
 *
 * @param shapeStatus 节点状态
 * @param data jadeConfig
 * @returns {JSX.Element} 条件节点表单Wrapper的DOM
 */
export default function ConditionFormWrapper({shapeStatus, data}) {
    const dispatch = useDispatch();

    const {t} = useTranslation();

    const branches = data.branches;

    const addBranch = () => {
        dispatch({actionType: 'addBranch', jadeNodeConfigChangeIgnored: false});
    };

    const deleteBranch = (branchId) => {
        dispatch({actionType: 'deleteBranch', branchId: branchId, jadeNodeConfigChangeIgnored: false});
    };

    const changeConditionRelation = (branchId, conditionRelation) => {
        dispatch({actionType: 'changeConditionRelation', branchId: branchId, conditionRelation: conditionRelation, jadeNodeConfigChangeIgnored: false});
    };

    const addCondition = (branchId) => {
        dispatch({actionType: 'addCondition', branchId: branchId, jadeNodeConfigChangeIgnored: false});
    };

    const deleteCondition = (branchId, conditionId) => {
        dispatch({actionType: 'deleteCondition', branchId: branchId, conditionId: conditionId, jadeNodeConfigChangeIgnored: false});
    };

    const changeConditionConfig = (branchId, conditionId, updateParams) => {
        dispatch({
            actionType: 'changeConditionConfig',
            branchId: branchId,
            conditionId: conditionId,
            updateParams: updateParams,
            jadeNodeConfigChangeIgnored: false,
        });
    };

    return (<div>
        <div style={{
            display: "flex", alignItems: "center", marginBottom: "8px", paddingLeft: "8px", paddingRight: "4px"
        }}>
            <div className="jade-panel-header-font">{t('conditionBranch')}</div>
            <Button type="link" className="icon-button"
                    onClick={addBranch}
                    disabled={shapeStatus.disabled}
                    style={{"height": "32px", marginLeft: "auto"}}>
                <PlusOutlined/>
                <span>{t('addBranch')}</span>
            </Button>
        </div>
        {branches.filter(branch => branch.type === "if").map((branch, index) => (
            <ConnectorProvider key={"dynamic-" + branch.id} name={"dynamic-" + index + "|" + branch.id}>
                <IfForm key={branch.id}
                        branch={branch}
                        index={index}
                        name={index === 0 ? "If" : "Else if"}
                        totalItemNum={branches.length + 1}
                        disabled={branch.disabled}
                        deleteBranch={deleteBranch}
                        changeConditionRelation={changeConditionRelation}
                        addCondition={addCondition}
                        deleteCondition={deleteCondition}
                        changeConditionConfig={changeConditionConfig}/>
            </ConnectorProvider>
        ))}
        {/* 这里需要保证dynamic-之后数字为最大值，因为后端根据connect名字来判断条件执行优先级 */}
        <ConnectorProvider name={`dynamic-999`}>
            <ElseForm/>
        </ConnectorProvider>
    </div>);
}