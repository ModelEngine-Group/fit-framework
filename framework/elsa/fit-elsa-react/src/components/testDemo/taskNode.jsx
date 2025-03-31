/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from "@/components/base/jadeNode.jsx";
import TaskList from "@/components/testDemo/TaskList.jsx";
import AddTask from "@/components/testDemo/AddTask.jsx";
import {CloudSyncOutlined} from "@ant-design/icons";

/**
 * jadeStream中的流程编排节点.
 *
 * @override
 */
export const taskNode = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer);
    self.type = "taskNode";
    self.text = "LLM";
    self.width = 700;
    self.jadeConfig = [{id: 0, text: 'Philosopher’s Path', done: true}, {id: 1, text: 'Visit the temple', done: false},
        {id: 2, text: 'Drink matcha', done: false}];
    // self.toolMenus = [];

    /**
     * @override
     */
    self.getReactComponents = () => {
        return (<>
            <h1>Day off in Kyoto</h1>
            <AddTask/>
            <TaskList/>
        </>)
    };

    /**
     * @override
     */
    self.getHeaderIcon = () => {
        return <CloudSyncOutlined/>;
    };

    /**
     * @override
     */
    self.reducers = (tasks, action) => {
        switch (action.type) {
            case 'added': {
                return [...tasks, {
                    id: action.id, text: action.text, done: false
                }];
            }
            case 'changed': {
                return tasks.map(t => {
                    if (t.id === action.task.id) {
                        return action.task;
                    } else {
                        return t;
                    }
                });
            }
            case 'deleted': {
                return tasks.filter(t => t.id !== action.id);
            }
            default: {
                throw Error('Unknown action: ' + action.type);
            }
        }
    };

    return self;
};