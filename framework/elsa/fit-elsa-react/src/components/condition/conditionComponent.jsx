/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ConditionFormWrapper from "@/components/condition/ConditionFormWrapper.jsx";
import {v4 as uuidv4} from "uuid";
import {defaultComponent} from "@/components/defaultComponent.js";

export const conditionComponent = (jadeConfig) => {
    const self = defaultComponent(jadeConfig);

    /**
     * 必须.
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : {
            branches: [
                {
                    id: uuidv4(),
                    conditionRelation: "and",
                    type: "if",
                    runnable: true,
                    conditions: [
                        {
                            id: uuidv4(),
                            condition: undefined,
                            value: [
                                {
                                    id: uuidv4(),
                                    name: "left",
                                    type: "",
                                    from: "Reference",
                                    value: "",
                                    referenceNode: "",
                                    referenceId: "",
                                    referenceKey: ""
                                },
                                {
                                    id: uuidv4(),
                                    name: "right",
                                    type: "",
                                    from: "Reference",
                                    value: "",
                                    referenceNode: "",
                                    referenceId: "",
                                    referenceKey: ""
                                }
                            ]
                        },
                    ]
                },
                {
                    id: uuidv4(),
                    conditionRelation: "and",
                    type: "else",
                    runnable: true,
                    conditions: [
                        {
                            id: uuidv4(),
                            condition: "true",
                            value: []
                        },
                    ]
                }
            ]
        };
    };

    /**
     * 必须.
     */
    self.getReactComponents = (shapeStatus, data) => {
        return (<><ConditionFormWrapper data={data} shapeStatus={shapeStatus}/></>);
    };

    /**
     * 必须.
     */
    const reducers = self.reducers;
    self.reducers = (data, action) => {
        // Functions to be used for updating the data
        const changeConditionConfig = () => {
            return new Data(data).updateBranch(action, branchUpdater => branchUpdater.updateCondition(action));
        }

        const deleteBranch = () => {
            return new Data(data).deleteBranch(action.branchId, action.jadeNodeConfigChangeIgnored);
        }

        const changeConditionRelation = () => {
            return new Data(data).updateBranch(action, branchUpdater => branchUpdater.changeConditionRelation(action.conditionRelation));
        }

        const addCondition = () => {
            const newCondition = {
                id: uuidv4(),
                condition: undefined,
                value: [
                    {id: uuidv4(), name: "left", type: "", from: "Reference", value: [], referenceNode: ""},
                    {id: uuidv4(), name: "right", type: "", from: "Reference", value: [], referenceNode: ""}
                ]
            };
            return new Data(data).updateBranch(action, branchUpdater => branchUpdater.addCondition(newCondition));
        }

        const deleteCondition = () => {
            return new Data(data).updateBranch(action, branchUpdater => branchUpdater.deleteCondition(action.conditionId));
        }

        const addBranch = () => {
            return new Data(data).addBranch(action.jadeNodeConfigChangeIgnored);
        }

        const changeBranchesStatus = () => {
            return new Data(data).changeBranchesStatus(action);
        }

        switch (action.actionType) {
            case 'addBranch': {
                return addBranch();
            }
            case 'deleteBranch': {
                return deleteBranch();
            }
            case 'changeConditionRelation': {
                return changeConditionRelation();
            }
            case 'addCondition': {
                return addCondition();
            }
            case 'deleteCondition': {
                return deleteCondition();
            }
            case 'changeConditionConfig': {
                return changeConditionConfig();
            }
            case 'changeBranchesStatus': {
                return changeBranchesStatus();
            }
            case 'changeFlowMeta': {
                return {
                    ...data,
                    enableStageDesc: action.data.enableStageDesc,
                    stageDesc: action.data.stageDesc,
                };
            }
            default: {
                return reducers.apply(self, [data, action]);
            }
        }
    };

    return self;
};

class Condition {
    constructor(condition) {
        this.condition = {...condition};
    }

    updateValue(item) {
        this.condition.value = this.condition.value.map(conditionValue => {
            if (item.key === conditionValue.id) {
                return {
                    ...conditionValue,
                    ...Object.fromEntries(item.value.map(itemValue => [itemValue.key, itemValue.value]))
                };
            }
            return conditionValue;
        });
    }

    update(updateParams) {
        updateParams.forEach(item => {
            if (item.key === "condition") {
                this.condition[item.key] = item.value;
            } else {
                this.updateValue(item);
            }
        });
        return this.condition;
    }
}

class Branch {
    constructor(branch) {
        this.branch = {...branch};
    }

    static createNewBranch() {
        return {
            id: uuidv4(),
            conditionRelation: "and",
            type: "if",
            runnable: true,
            conditions: [
                {
                    id: uuidv4(),
                    condition: undefined,
                    value: [
                        {
                            id: uuidv4(),
                            name: "left",
                            type: "",
                            from: "Reference",
                            value: [],
                            referenceNode: ""
                        },
                        {
                            id: uuidv4(),
                            name: "right",
                            type: "",
                            from: "Reference",
                            value: [],
                            referenceNode: ""
                        }
                    ]
                }
            ]
        };
    }

    updateCondition(action) {
        this.branch.conditions = this.branch.conditions.map(condition => {
            if (condition.id === action.conditionId) {
                return new Condition(condition).update(action.updateParams);
            }
            return condition;
        });
        return this.branch;
    }

    deleteCondition(conditionId) {
        this.branch.conditions = this.branch.conditions.filter(condition => condition.id !== conditionId);
        return this.branch;
    }

    addCondition(newCondition) {
        this.branch.conditions.push(newCondition);
        return this.branch;
    }

    changeConditionRelation(conditionRelation) {
        this.branch.conditionRelation = conditionRelation;
        return this.branch;
    }
}

class Data {
    constructor(data) {
        this.data = {...data};
    }

    updateJadeNodeConfigChangeIgnored(jadeNodeConfigChangeIgnored) {
        this.data.jadeNodeConfigChangeIgnored = jadeNodeConfigChangeIgnored;
    }

    updateBranch(action, updateBranchFn) {
        this.updateJadeNodeConfigChangeIgnored(action.jadeNodeConfigChangeIgnored ?? false);
        this.data.branches = this.data.branches.map(branch => {
            if (branch.id === action.branchId) {
                return updateBranchFn(new Branch(branch));
            }
            return branch;
        });
        return this.data;
    }

    deleteBranch(branchId, jadeNodeConfigChangeIgnored = false) {
        this.updateJadeNodeConfigChangeIgnored(jadeNodeConfigChangeIgnored);
        this.data.branches = this.data.branches.filter(branch => branch.id !== branchId);
        return this.data;
    }

    addBranch(jadeNodeConfigChangeIgnored = false) {
        this.updateJadeNodeConfigChangeIgnored(jadeNodeConfigChangeIgnored);
        const newBranch = Branch.createNewBranch();
        const elseBranchIndex = this.data.branches.findIndex(branch => branch.type === 'else');

        if (elseBranchIndex !== -1) {
            this.data.branches.splice(elseBranchIndex, 0, newBranch);
        } else {
            this.data.branches.push(newBranch);
        }
        return this.data;
    }

    changeBranchesStatus(action) {
        this.updateJadeNodeConfigChangeIgnored(action.changes.find(change => change.key === 'jadeNodeConfigChangeIgnored').value ?? false);
        this.data.branches.forEach(branch => {
            const disabled = action.changes.find(change => change.key === 'disabled').value;
            if (action.changes.find(change => change.key === 'ids').value.includes(branch.id)) {
                branch.disabled = disabled;
            }
        });
        return this.data;
    }
}