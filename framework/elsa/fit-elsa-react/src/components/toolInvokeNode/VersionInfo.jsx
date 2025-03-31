/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import "./style.css";
import {Col, Row} from "antd";

/**
 * 展示版本信息.
 *
 * @return {JSX.Element}
 * @constructor
 */
export const VersionInfo = ({versionInfo}) => {
    return (<>
        <div className={"tool-invoke-version-info"}>
            <div className={"tool-invoke-version-info-header"}>
                <span>当前版本详情</span>
            </div>
            <div className={"tool-invoke-version-info-detail tool-invoke-version-info-text"}>
                <div className={"tool-invoke-version-info-detail-title"}>
                    <Row>
                        <Col span={8}>版本名称</Col>
                        <Col span={8}>创建人</Col>
                        <Col span={8}>创建时间</Col>
                    </Row>
                </div>
                <div className={"tool-invoke-version-info-detail-value"}>
                    <Row>
                        <Col span={8}>{versionInfo.appVersion}</Col>
                        <Col span={8}>{versionInfo.publishedBy}</Col>
                        <Col span={8}>{versionInfo.publishedAt}</Col>
                    </Row>
                </div>
            </div>
            <div className={"tool-invoke-version-info-description tool-invoke-version-info-text"}>
                <div className={"tool-invoke-version-info-detail-title"}>
                    <Row>
                        <Col span={24}>描述</Col>
                    </Row>
                </div>
                <div className={"tool-invoke-version-info-detail-value"}>
                    <Row>
                        <Col span={24}>{versionInfo.publishedDescription}</Col>
                    </Row>
                </div>
            </div>
        </div>
    </>);
};