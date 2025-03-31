/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Tooltip} from "antd";
import PropTypes from "prop-types";

TextDisplay.propTypes = {
    text: PropTypes.string,
    fontSize: PropTypes.number,
    fontWeight: PropTypes.number,
    lineHeight: PropTypes.number,
    width: PropTypes.number,
};

/**
 * 文本展示组件，文本过长会显示省略号
 *
 * @param text 文本
 * @param fontSize 字体大小
 * @param fontWeight 字体重量
 * @param lineHeight 行高
 * @param width 文本显示宽度
 * @return {JSX.Element} 文本展示组件
 * @constructor
 */
export default function TextDisplay({text, fontSize, fontWeight, lineHeight, width}) {

    return (<>
        <Tooltip overlayClassName="text-title-tooltip" title={text}>
            <div className="header-title"
                 style={{
                     fontSize: fontSize,
                     fontWeight: fontWeight,
                     lineHeight: `${lineHeight}px`,
                     maxWidth: width
                 }}>{text}</div>
        </Tooltip>
    </>);
}