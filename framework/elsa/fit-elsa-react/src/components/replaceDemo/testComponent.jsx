/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useRef} from "react";
import {useDataContext, useDispatch} from "@/components/DefaultRoot.jsx";
import {JadeInput} from "@/components/common/JadeInput.jsx";

export const testComponent = (jadeConfig) => {
    const self = {};

    /**
     * 必须.
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : [{name: "description", type: "String", value: "这是一个测试"}];
    };

    /**
     * 必须.
     */
    self.getReactComponents = () => {
        return (<><TestComponent/></>);
    };

    /**
     * 必须.
     */
    self.reducers = (config, action) => {
        if (action.type === "update") {
            return [{...config[0], value: action.value}];
        }
      return undefined;
    };

    return self;
};

const TestComponent = (props) => {
    const inputRef = useRef(null);
    const data = useDataContext(null);
    const dispatch = useDispatch();

    const onInputChange = () => {
        dispatch({type: "update", value: inputRef.current.input.value});
    };

    return (<>
        <div>
            <JadeInput ref={inputRef} value={data[0].value} placeholder="Basic usage" onChange={() => onInputChange()}/>
        </div>
        <TestChildComponent {...props} />
    </>);
};

const TestChildComponent = (props) => {
    return <><span>{props.a}</span><span>{props.b}</span><span>{props.c}</span></>
};