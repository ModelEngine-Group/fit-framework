/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from "prop-types";
import {useState} from "react";

JsonNode.propTypes = {
    keyName: PropTypes.string.isRequired,
    value: PropTypes.any.isRequired,
    isLast: PropTypes.bool,
    level: PropTypes.number
};

/**
 * 展示Json节点的组件.
 *
 * @param keyName key的名称.
 * @param value 值.
 * @param isLast 是否是最后一个.
 * @param level 层级.
 * @return {JSX.Element}
 * @constructor
 */
export default function JsonNode({keyName, value, isLast = false, level = 0}) {
    const [expanded, setExpanded] = useState(false);
    const isArray = Array.isArray(value);
    const keys = Object.keys(value);

    const toggleExpand = () => {
        setExpanded(!expanded);
    };

    /**
     * 构建属性.
     *
     * @param keys 属性key的集合.
     * @return {JSX.Element}
     */
    const buildAttributes = (keys) => {
        return (<>
            <div className={"jade-json-node-content"}>
                {keys.map((key, i) => {
                    const v = value[key];
                    const isLast = i === keys.length - 1;
                    if (typeof v === "object" && v !== null) {
                        return (<>
                            <JsonNode keyName={key}
                                      value={v}
                                      level={level + 1}
                                      isLast={isLast}/>
                        </>);
                    }
                    return (<><JsonAttribute keyName={key} value={v} isLast={isLast}/></>);
                })}
            </div>
        </>);
    };

    const getIconClass = () => {
        return expanded ? "jade-json-node-expand-icon" : "jade-json-node-collapse-icon";
    };

    return (<>
        <div key={keyName}>
            <span className={getIconClass()} onClick={toggleExpand}/>
            {level !== 0 && (<><span className={"jade-json-node-key"}>{keyName} : </span></>)}
            <span className={"jade-json-node-use-select-none jade-json-node-left-symbol"}>
                    {isArray ? '[' : '{'}
                </span>
            {expanded ? buildAttributes(keys) : (
                    <span className={"jade-json-node-use-select-none jade-json-node-ellipsis"}>...</span>)}
            <span className={"jade-json-node-use-select-none"}>{isArray ? ']' : '}'}</span>
            {!isLast && (<><span>，</span></>)}
        </div>
    </>);
}

JsonAttribute.propTypes = {
    keyName: PropTypes.string.isRequired, value: PropTypes.any.isRequired, isLast: PropTypes.bool
};

/**
 * 展示Json属性的组件.
 *
 * @param keyName key的名称.
 * @param value 值.
 * @param isLast 是否是最后一个.
 * @return {JSX.Element}
 * @constructor
 */
function JsonAttribute({keyName, value, isLast}) {
    const isNumber = typeof value === "number";
    const valueClass = isNumber ? "jade-json-node-value-number" : "jade-json-node-value-string";
    const formatValue = isNumber ? value : "\"" + String(value) + "\"";

    return (<>
        <div key={keyName}>
            <span className={"jade-json-node-key"}>{keyName} : </span>
            <span className={valueClass}>{formatValue}</span>
            {!isLast && (<><span>，</span></>)}
        </div>
    </>);
}