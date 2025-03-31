/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useState} from 'react';
import {useDataContext, useDispatch} from "@/components/DefaultRoot.jsx";
import {JadeInput} from "@/components/common/JadeInput.jsx";

export default function TaskList() {
    const tasks = useDataContext();
    return (<ul>
                {tasks.map(task => (<li key={task.id}>
                            <Task task={task}/>
                        </li>))}
            </ul>);
}

function Task({task}) {
    const [isEditing, setIsEditing] = useState(false);
    const dispatch = useDispatch();
    let taskContent;
    if (isEditing) {
        taskContent = (<>
                    <JadeInput
                            value={task.text}
                            onChange={e => {
                                dispatch({
                                    type: 'changed', task: {
                                        ...task, text: e.target.value
                                    }
                                });
                            }}/>
                    <button onClick={() => setIsEditing(false)}>
                        Save
                    </button>
                </>);
    } else {
        taskContent = (<>
                    {task.text}
                    <button onClick={() => setIsEditing(true)}>
                        Edit
                    </button>
                </>);
    }
    return (<label>
                <JadeInput
                        type="checkbox"
                        checked={task.done}
                        onChange={e => {
                            dispatch({
                                type: 'changed', task: {
                                    ...task, done: e.target.checked
                                }
                            });
                        }}
                />
                {taskContent}
                <button onClick={() => {
                    dispatch({
                        type: 'deleted', id: task.id
                    });
                }}>
                    Delete
                </button>
            </label>);
}
