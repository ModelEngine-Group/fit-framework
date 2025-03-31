/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {InfoCircleOutlined} from '@ant-design/icons';
import {Popover} from 'antd';
import {VIRTUAL_CONTEXT_NODE} from '@/common/Consts.js';
import {Trans} from 'react-i18next';

const content = (<>
  <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='systemContextPopover' components={{p: <p/>}}/>
  </div>
</>);

/**
 * 当dropdown显示时触发，实时获取tree数据.
 */
export const buildTreeData = (shape, treeFilter = () => true, typeFilter = () => true) => {
  const nodeInfos = shape.getPreReferencableNodeInfos()
    .filter(n => n.observableList.length > 0)
    .filter(n => treeFilter(n));

  const getTitle = (nodeInfo) => {
    return nodeInfo.id === VIRTUAL_CONTEXT_NODE.id ? <>
      <div className={'jade-font-size'}>
        <span>{nodeInfo.name}</span>
        <Popover
          className={'jade-drop-down-popover'} overlayClassName='jade-drop-down-popover-overlay'
          content={content}>
          <InfoCircleOutlined/>
        </Popover>
      </div>
    </> : nodeInfo.name;
  };

  return nodeInfos.map(n => {
    const ans = [];
    ans.push({
      id: n.id,
      pId: 0,
      value: n.id,
      title: getTitle(n),
      selectable: false,
    });
    n.observableList.filter(o => o.visible !== false).forEach(o => {
      if (!o.parentId) {
        o.parentId = n.id;
      }
      const treeNode = {};
      treeNode.nId = n.id;
      treeNode.id = o.observableId;
      treeNode.pId = o.parentId;
      treeNode.value = o.observableId;
      treeNode.title = o.value;
      treeNode.selectable = (o.selectable ?? true) && typeFilter(o);
      treeNode.disabled = !treeNode.selectable;
      treeNode.label = `${shape.page.getShapeById(n.id).text}>${o.value}`;
      treeNode.type = o.type;
      ans.push(treeNode);
    });

    return ans;
  }).flatMap(n => n);
};

/**
 * 树形选择使用的下拉渲染Dom。
 *
 * @param menu 下拉菜单。
 * @returns {JSX.Element}
 */
export const handleDropdownRender = (menu) => (
  <div onMouseDown={(e) => e.stopPropagation()}>
    {menu}
  </div>
);

/**
 * 通过树节点结构和观察的Map组装出对应的value数组。
 *
 * @param observableMap 观察Map。
 * @param treeNode 树节点。
 * @returns {*[]}
 */
export const buildValue = (observableMap, treeNode) => {
  const ans = [];
  ans.unshift(treeNode.title);

  let parentId = treeNode.pId;
  while (parentId) {
    const parent = observableMap.get(parentId);
    if (!parent || parent.pId === 0) {
      break;
    }

    ans.unshift(parent.title);
    parentId = parent.pId;
  }

  return ans;
};