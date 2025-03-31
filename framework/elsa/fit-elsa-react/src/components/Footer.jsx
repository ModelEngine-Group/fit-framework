/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from "react";
import OfficialIcon from "./asserts/icon-official.svg?react";
import HuggingFaceIcon from "./asserts/icon-huggingface.svg?react";
import LlamaIndexIcon from "./asserts/icon-llamaindex.svg?react";
import LangChainIcon from "./asserts/icon-langchain.svg?react";
import {Button} from "antd";
import {SOURCE_PLATFORM} from "@/common/Consts.js";
import {useTranslation} from "react-i18next";

// 使用对象字面量来创建图标映射
const footerMap = {
    [SOURCE_PLATFORM.OFFICIAL]: {
        icon: <OfficialIcon className="jade-node-footer-icon" style={{height: "20px", width: "20px"}}/>,
        text: 'official'
    },
    [SOURCE_PLATFORM.HUGGING_FACE]: {
        icon: <HuggingFaceIcon className="jade-node-footer-icon" style={{height: "20px", width: "20px"}}/>,
        text: 'huggingFace'
    },
    [SOURCE_PLATFORM.LLAMA_INDEX]: {
        icon: <LlamaIndexIcon className="jade-node-footer-icon" style={{height: "16.4px", width: "16.4px"}}/>,
        text: 'llamaIndex'
    },
    [SOURCE_PLATFORM.LANG_CHAIN]: {
        icon: <LangChainIcon className="jade-node-footer-icon" style={{height: "20px", width: "36px"}}/>,
        text: 'langChain'
    },
};

/**
 * 脚部.
 *
 * @param shape 图形.
 * @return {JSX.Element}
 */
export const Footer = ({shape}) => {
    const {t} = useTranslation();

    const getSourceIcon = (sourcePlatform) => {
        return footerMap[sourcePlatform]?.icon ?? '';
    };

    /**
     * 获取footer文本
     *
     * @param sourcePlatform 平台
     * @return {*} 文本
     */
    const getFooterText = (sourcePlatform) => {
        return t(footerMap[sourcePlatform]?.text ?? '');
    };

    return (<>
        <div className='react-node-footer'>
            <div style={{display: 'flex', alignItems: 'center'}}>
                <Button disabled={true} className='jade-node-footer-button'>
                    {getSourceIcon(shape.sourcePlatform)}
                </Button>
                <div className={'jade-footer-text'}>{getFooterText(shape.sourcePlatform)}</div>
            </div>
        </div>
    </>);
};