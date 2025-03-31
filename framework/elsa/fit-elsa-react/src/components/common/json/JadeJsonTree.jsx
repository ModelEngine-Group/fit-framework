/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from "react";
import PropTypes from "prop-types";
import "./jadeJsonTree.css";
import JsonNode from "@/components/common/json/JsonNode.jsx";

_JadeJsonTree.propTypes = {
    jsonObj: PropTypes.object.isRequired
};

/**
 * 展示Json树状结构的组件.
 *
 * @param jsonObj json对象.
 * @return {JSX.Element}
 * @private
 */
function _JadeJsonTree({jsonObj}) {
    return (<>
        <div className={"json-tree-viewer"}>
            <JsonNode keyName="root" value={jsonObj} isLast={true}/>
        </div>
    </>);
}

const areEqual = (prevProps, nextProps) => {
    return prevProps.jsonObj === nextProps.jsonObj;
};

export const JadeJsonTree = React.memo(_JadeJsonTree, areEqual);

