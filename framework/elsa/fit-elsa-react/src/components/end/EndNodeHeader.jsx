/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useState} from "react";
import {Header} from "@/components/Header.jsx";
import PropTypes from "prop-types";

/**
 * 头部.
 *
 * @param shape 图形.
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @return {JSX.Element}
 * @constructor
 */
export const EndNodeHeader = ({shape, data, shapeStatus}) => {
    const [, setEndNodeCount] = useState(0);

    useEffect(() => {
        // 节点数量变化
        shape.page.addEventListener('END_NODE_MENU_CHANGE', (value) => {
            setEndNodeCount(value[0]);
        });
    }, []);

    return (<>
        <Header shape={shape} data={data} shapeStatus={shapeStatus}/>
    </>);
};

EndNodeHeader.propTypes = {
    shape: PropTypes.object,
    shapeStatus: PropTypes.object
};