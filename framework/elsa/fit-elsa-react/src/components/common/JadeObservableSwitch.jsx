/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Switch} from "antd";
import {useEffect} from "react";
import {useShapeContext} from "@/components/DefaultRoot.jsx";

/**
 * 可被监听的Switch组件.
 *
 * @param props 参数.
 * @return {JSX.Element}
 * @constructor
 */
export const JadeObservableSwitch = (props) => {
    const {onBlur, type, ...rest} = props;
    if (!rest.id) {
        throw new Error("JadeObservableSwitch requires an id property.");
    }

    const shape = useShapeContext();
    if (!shape) {
        throw new Error("JadeObservableSwitch must be wrapped by ShapeContext.");
    }

    /**
     * 有些场景下value是在blur时触发修改的，所以这里透出blur事件.
     *
     * @param e 事件对象.
     * @private
     */
    const _onBlur = (e) => {
        onBlur && onBlur(e);
    };

    // 组件初始化时注册observable.
    useEffect(() => {
        shape.page.registerObservable({
            nodeId: shape.id,
            observableId: rest.id,
            value: rest.name,
            type: type,
            parentId: rest.parent
        });

        // 组件unmount时，删除observable.
        return () => {
            shape.page.removeObservable(shape.id, rest.id);
        };
    }, []);

    return <><Switch {...rest} onBlur={_onBlur}/></>
};