/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useEffect} from "react";
import {useShapeContext} from "@/components/DefaultRoot.jsx";
import {JadeInput} from "@/components/common/JadeInput.jsx";

/**
 * 可被监听的Input组件.
 *
 * @param props 参数.
 * @return {JSX.Element}
 * @constructor
 */
export const JadeObservableInput = (props) => {
    const {onChange, onBlur, type, ...rest} = props;
    if (!rest.id) {
        throw new Error("JadeObservableInput requires an id property.");
    }

    const shape = useShapeContext();
    if (!shape) {
        throw new Error("JadeObservableInput must be wrapped by ShapeContext.");
    }

    /**
     * 输入被修改时调用.
     *
     * @param e 事件对象.
     * @private
     */
    const _onChange = (e) => {
        onChange && onChange(e);

        // 触发节点的emit事件.
        shape.emit(rest.id, {value: e.target.value});
    };

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
            value: rest.value,
            type: type,
            parentId: rest.parent
        });

        // 组件unmount时，删除observable.
        return () => {
            shape.page.removeObservable(shape.id, rest.id);
        };
    }, []);

    // 如果类型发生了变化，重新注册，修改observable中的type值.
    useEffect(() => {
        shape.emit(rest.id, {type: type});
    }, [type]);

    return <><JadeInput {...rest} onChange={(e) => _onChange(e)} onBlur={_onBlur} /></>
};