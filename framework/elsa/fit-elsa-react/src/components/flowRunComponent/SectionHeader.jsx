/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, message} from "antd";
import {CopyOutlined} from "@ant-design/icons";
import {useRef} from "react";
import {useTranslation} from "react-i18next";

/**
 * 章节头部
 *
 * @param section 章节对象
 * @param shape 图形
 * @return {JSX.Element}
 * @constructor
 */
export default function SectionHeader({section, shape}) {
    const {t} = useTranslation();
    const textAreaRef = useRef(null);

    /**
     * 复制按钮的回调
     *
     * @param text 文本信息
     */
    const handleCopy = (text) => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text)
                    .then(() => {
                        message.success(t('copySucceeded'));
                    })
                    .catch(err => {
                        fallbackCopyTextToClipboard(text);
                    });
        } else {
            fallbackCopyTextToClipboard(text);
        }
    };

    /**
     * 回退的复制方法
     *
     * @param text 文本信息
     */
    const fallbackCopyTextToClipboard = (text) => {
        if (!textAreaRef.current) {
            return;
        }
        textAreaRef.current.value = text;
        textAreaRef.current.select();
        try {
            document.execCommand('copy');
            message.success(t('copySucceeded'));
        } catch (err) {
            message.error(t('copyFailed'));
        }
    };

    return (
            <div className="section-header">
                <div className="section-title">{section.name}</div>
                <Button
                        className="copy-button"
                        type="text"
                        icon={<CopyOutlined className="copy-button-icon"/>}
                        onClick={() => handleCopy(JSON.stringify(section.data))}
                />
                <textarea
                        ref={textAreaRef}
                        style={{position: 'absolute', top: '-9999px', left: '-9999px'}}
                        readOnly
                />
            </div>
    );
}
