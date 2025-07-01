/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Tree} from 'antd';
import {v4 as uuidv4} from 'uuid';
import TreeSwitcherIcon from '@/components/common/TreeSwitcherIcon.jsx';
import {useEffect} from 'react';
import {removeFirstLevelLine} from '@/components/util/CssUtil.js';

/**
 *  构造数组元素的树形结构
 *
 * @param value 数组元素
 * @returns {*}
 */
const _getArrayItem = value => {
  return value.map((item, index) => {
    // 处理数组中参数为null的情况
    if (item === null || item === undefined) {
      return ({
        title: `${index} null`,
        key: uuidv4(),
        children: [],
        isFirstLevel: false,
      });
    }
    return ({
      title: typeof item === 'object' ? `${index} {${Object.keys(item).length}}` : `${index} ${item}`,
      key: uuidv4(),
      children: typeof item === 'object' ? generateTreeData(item, false) : [],
      isFirstLevel: false,
    });
  });
};

const generateTreeData = (data, isFirstLevel = true) => {
  if (data === null) {
    return [{
      title: <div style={{wordBreak: 'break-all'}}>null</div>,
      key: uuidv4(),
      children: [],
      isFirstLevel,
    }];
  }
  // 如果数据是字符串或数字，直接返回一个树节点
  if (typeof data === 'string' || typeof data === 'number' || typeof data === 'boolean') {
    return [{
      title: <div style={{wordBreak: 'break-all'}}>{data.toString()}</div>,
      key: uuidv4(),
      children: [],
      isFirstLevel,
    }];
  }

  // 如果数据是对象（包括数组），进行递归解析
  return Object.keys(data).map(key => {
    const value = data[key];
    const isLeaf = typeof value !== 'object' || value === null;
    let title;

    if (isLeaf) {
      title = <div style={{wordBreak: 'break-all'}}>{`${key}: ${value}`}</div>;
    } else if (Array.isArray(value)) {
      title = `${key} [${value.length}]`;
      return {
        title,
        key: uuidv4(),
        children: _getArrayItem(value),
        isFirstLevel,
      };
    } else {
      title = `${key} {${Object.keys(value).length}}`;
    }

    return {
      title,
      key: uuidv4(),
      children: isLeaf ? [] : generateTreeData(value, false),
      isFirstLevel,
    };
  });
};

/**
 * 内容展示区域
 *
 * @param data 数据
 * @return {JSX.Element}
 * @constructor
 */
const SectionContent = ({data}) => {
  const treeData = generateTreeData(data);

  useEffect(() => {
    const handleClickOutside = (e) => {
      // 如果点击的不是树节点内容区域
      if (!e.target.closest('.ant-tree-node-content-wrapper')) {
        window.getSelection().removeAllRanges();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    removeFirstLevelLine();

    // 监听全局 keydown 事件
    const handleKeyDown = (e) => {
      if (e.ctrlKey && e.key === 'c') {
        const selectedText = window.getSelection().toString();
        if (selectedText) {
          e.preventDefault(); // 阻止默认行为（可选）
          navigator.clipboard.writeText(selectedText)
            .catch(err => console.error('Copy failed:', err));
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [treeData]);

  return (<>
    <Tree showLine={{showLeafIcon: false}}
          switcherIcon={({expanded}) => <TreeSwitcherIcon expanded={expanded}/>}
          defaultExpandAll={false}
          treeData={treeData}
          titleRender={(nodeData) => (
            <span
              style={{
                userSelect: 'text',       // 允许选择文本
                WebkitUserSelect: 'text', // Safari 兼容
                MozUserSelect: 'text',    // Firefox 兼容
              }}
              onMouseDown={(e) => e.stopPropagation()} // 阻止拖拽
            >
              {nodeData.title}
            </span>
          )}
    />
  </>);
};

export default SectionContent;
