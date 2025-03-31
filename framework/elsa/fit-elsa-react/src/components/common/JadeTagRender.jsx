/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useRef} from 'react';
import {CloseOutlined} from '@ant-design/icons';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import {Form} from 'antd';
import {buildValue} from '@/components/util/TreeSelectUtils.jsx';

export const JadeTagRender = ({label, value, onClose, showRemoveButton, reference, _onReferenceValueChange, disabled, displayTitle, ...rest}) => {
  const shape = useShapeContext();
  const stopObserve = useRef(null);

  const handleMouseDown = (e) => {
    e.stopPropagation(); // 阻止鼠标按下事件冒泡，防止触发下拉框打开
  };

  const handleClick = (e) => {
    e.stopPropagation(); // 阻止点击事件冒泡，避免触发TreeSelect的onChange事件和onDropdownVisibleChange
    onClose(); // 执行关闭（删除）选项
  };

  /**
   * 当dropdown显示时触发，实时获取tree数据.
   */
  const buildTreeData = () => {
    const nodeInfos = shape.getPreReferencableNodeInfos()
      .filter(n => n.observableList.length > 0)
      .filter(n => rest.treeFilter ? rest.treeFilter(n) : true); // 根据rest.treeFilter添加的条件;

    return nodeInfos.map(n => {
      const ans = [];
      ans.push({
        id: n.id,
        pId: 0,
        value: n.id,
        title: n.name,
        selectable: false,
      });
      n.observableList.filter(o => o.visible !== false).forEach(o => {
        if (!o.parentId) {
          o.parentId = n.id;
        }
        const treeNode = {
          nId: n.id,
          id: o.observableId,
          pId: o.parentId,
          value: o.observableId,
          title: o.value,
          selectable: o.selectable ?? true,
          type: o.type,
        };
        ans.push(treeNode);
      });

      return ans;
    }).flatMap(n => n);
  };

  // 组件初始化后，若存在引用关系，则进行监听.
  useEffect(() => {
    // 当组件unmount时，停止监听.
    return () => {
      if (stopObserve.current) {
        stopObserve.current();
      }
    };
  }, []);

  // reference变化需要重新监听.
  useEffect(() => {
    if (stopObserve.current) {
      stopObserve.current();
    }
    // disabled为true的状态下，不需要注册监听，只展示即可.
    if (reference.referenceNode && reference.referenceId) {
      stopObserve.current = shape.observeTo(reference.id,
        reference.referenceNode,
        reference.referenceId,
        (args) => {
          if (disabled) {
            return;
          }
          const newTreeData = buildTreeData();
          const treeMap = new Map(newTreeData.map(d => [d.id, d]));
          const treeNode = treeMap.get(reference.referenceId);

          if (treeNode) {
            treeNode.title = args.value;
            const newValue = buildValue(treeMap, treeNode);
            _onReferenceValueChange(reference.id, args.value, newValue, args.type);
          } else {
            _onReferenceValueChange(reference.id, args.value, [], args.type);
          }
        });
    }
  }, [reference.referenceNode, reference.referenceId]);

  return (
    <>
      <Form.Item
        id={`tag-render-id-${reference.id}`}
        initialValue={reference.referenceKey}
        validateTrigger='onBlur'
      >
        {reference.value.length > 0 && (<div className='ant-select-selection-item'>
          <div className='jade-custom-selected-item'>
            <span className='ant-select-selection-item-content'>{displayTitle}</span>
            {showRemoveButton && (
              <button
                className='ant-select-selection-item-remove'
                onMouseDown={handleMouseDown}
                onClick={handleClick}
              >
                <CloseOutlined/>
              </button>
            )}
          </div>
        </div>)}
      </Form.Item>
    </>
  );
};