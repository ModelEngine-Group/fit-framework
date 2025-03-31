/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 树形组件的折叠展开按钮
 *
 * @param expanded
 * @return {JSX.Element}
 * @constructor
 */
export default function TreeSwitcherIcon({expanded}) {
    const DownTriangle = () => (
            // 下面fill中修改按钮颜色
            <svg width="20" height="20" viewBox="0 0 24 24">
                <path d="M7 10l5 5 5-5z" fill="gray"/>
            </svg>
    );

    const RightTriangle = () => (
            <svg width="20" height="20" viewBox="0 0 24 24">
                <path d="M10 7l5 5-5 5z" fill="gray"/>
            </svg>
    );

    return (expanded ? <DownTriangle/> : <RightTriangle/>);
}