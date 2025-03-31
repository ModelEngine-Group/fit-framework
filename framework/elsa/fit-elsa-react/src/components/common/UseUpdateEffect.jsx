/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useEffect, useRef} from "react";

/**
 * 第一次变化时不执行逻辑的effect.
 * 
 * @param effect 回调.
 * @param deps 依赖.
 * @constructor
 */
export const useUpdateEffect = (effect, deps) => {
    const firstUpdate = useFirstMountState();

    useEffect(() => {
        if (!firstUpdate) {
            return effect();
        }
      return undefined;
    }, deps);
};

const useFirstMountState = () => {
    const isFirst = useRef(true);

    if (isFirst.current) {
        isFirst.current = false;
        return true;
    }

    return isFirst.current;
};