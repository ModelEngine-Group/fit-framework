/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Tree} from "antd";
import {useShapeContext} from "../DefaultRoot.jsx";
import React, {useEffect} from "react";
import "./jadeObservableTree.css";
import TreeSwitcherIcon from "@/components/common/TreeSwitcherIcon.jsx";

/**
 * 构建节点，只有Object节点有子孙节点.
 * 构建过程中，逐级注册observable，使后续节点可以进行reference操作.
 *
 * @param nodeData 节点数据.
 * @param parent 父节点数据.
 * @param level 层级.
 * @param shape 图形.
 * @return {{title, isLeaf: boolean, key}|{children: *, title, key}} 树节点.
 */
const buildNode = (nodeData, parent, level, shape) => {
    shape.page.registerObservable({
        nodeId: shape.id,
        observableId: nodeData.id,
        value: nodeData.name,
        type: nodeData.type,
        parentId: parent ? parent.id : null
    });
    if (nodeData.type === "Object") {
        return {
            title: nodeData.name,
            type: nodeData.type,
            key: nodeData.id,
            level: level,
            children: nodeData?.value?.map(v => buildNode(v, nodeData, level + 1, shape)) ?? [],
        };
    } else {
        return {
            title: nodeData.name, type: nodeData.type, key: nodeData.id, level: level, isLeaf: true
        };
    }
};

/**
 * 可被观察的树装结构.
 *
 * @param data 数据.
 * @return {JSX.Element}
 * @constructor
 */
const _JadeObservableTree = ({data}) => {
    if (!Array.isArray(data)) {
        throw new Error("data must be array.");
    }

    const shape = useShapeContext();
    const treeData = data.map(d => buildNode(d, null, 0, shape));

    useEffect(() => {
        // unmount时取消监听.
        return () => {
            if (treeData) {
                traverseTree(treeData, (n) => {
                    shape.page.removeObservable(shape.id, n.key)
                });
            }
        };
    }, []);

    const traverseTree = (nodes, action) => {
        nodes.forEach(n => {
            n.children && traverseTree(n.children, action);
            action(n);
        });
    };

    /**
     * 自定义标题展示.
     *
     * @param nodeData 节点数据.
     * @return {JSX.Element} react 组件对象.
     */
    const displayTitle = (nodeData) => {
        return (<>
            <div className="jade-observable-tree-node-div">
                <div style={{display: "flex", alignItems: "center"}}>
                    <span className="jade-observable-tree-node-title">{nodeData.title}</span>
                    <div className="jade-observable-tree-node-type-div">
                        <span className="jade-observable-tree-node-type-name">{nodeData.type}</span>
                    </div>
                </div>
            </div>
        </>);
    };

    const renderTreeNodes = (treeData) =>
        treeData.map((item) => {
            const isRootNode = item.level === 0;
            const className = isRootNode && (!item.children || item.children.length === 0) ? "jade-hide-tree-left-line" : '';

            if (item.children) {
                return (
                    <Tree.TreeNode title={displayTitle(item)} key={item.key} className={className}>
                        {renderTreeNodes(item.children)}
                    </Tree.TreeNode>
                );
            }
            return <Tree.TreeNode title={displayTitle(item)} key={item.key} className={className}/>;
        });

    return <>
        <Tree
            className={"jade-ant-tree"}
            switcherIcon={({expanded}) => <TreeSwitcherIcon expanded={expanded}/>}
            showLine={true}
            selectable={false}>
            {renderTreeNodes(treeData)}
        </Tree>
    </>;
};

const areEqual = (prevProps, nextProps) => {
    return prevProps.data === nextProps.data;
};

export const JadeObservableTree = React.memo(_JadeObservableTree, areEqual);