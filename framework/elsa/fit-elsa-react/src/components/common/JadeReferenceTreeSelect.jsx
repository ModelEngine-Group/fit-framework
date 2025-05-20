/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Form, TreeSelect} from 'antd';
import {useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import {useEffect, useRef, useState} from 'react';
import {buildTreeData, buildValue, handleDropdownRender} from '@/components/util/TreeSelectUtils.jsx';
import {useTranslation} from 'react-i18next';
import {EVENT_NAME} from '@/common/Consts.js';
import PropTypes from 'prop-types';

/**
 * jade的带引用功能的级联选择框.
 *
 * @param props 参数
 * @return {JSX.Element}
 * @constructor
 */
export const JadeReferenceTreeSelect = (props) => {
  const {
    reference,
    onReferencedValueChange,
    onReferencedKeyChange,
    rules,
    className,
    treeFilter,
    typeFilter,
    ...rest
  } = props;
  const shape = useShapeContext();
  const {t} = useTranslation();
  const form = useFormContext();
  const stopObserve = useRef(null);
  const [treeData, setTreeData] = useState([]);
  const referenceNodeIdRef = useRef(reference.referenceNode);
  const referenceIdRef = useRef(reference.referenceId);
  const name = `reference-${reference.id}`;
  // 合并外部传入的 className 和内部定义的 className
  const combinedClassName = `jade-tree-select ${className || ''}`.trim();
  // 后续可能需要根据新的剩余的宽度，动态设置: 100 - rest.level * 20

  /**
   * 当选择发生变化时，触发.监听不同的组件.
   *
   * @param selected 是个数组，包含从根节点到被选中节点的所有节点数据.
   */
  const onChange = (selected) => {
    if (!selected) {
      return;
    }

    const treeMap = new Map(treeData.map(d => [d.id, d]));
    const treeNode = treeMap.get(selected);
    const value = buildValue(treeMap, treeNode);
    onReferencedKeyChange({
      referenceNode: treeNode.nId,
      referenceId: treeNode.value,
      value,
      type: treeNode.type,
      referenceKey: treeNode.title,
    });
  };

  /*
   * 组件挂载后，需要监听节点名称变化事件.
   * 组件写在后，需要删除节点名称变化的监听器；同时，停止reference的监听.
   */
  useEffect(() => {
    // 必须写在里面，每个组件的监听函数应该是不同的对象.
    const onNodeNameChange = (e) => {
      if (referenceNodeIdRef.current === e.id) {
        // 这里获取不到最新的treeData值，只能全量刷新.
        setTreeData(buildTreeData(shape, treeFilter, typeFilter));
      }
    };
    shape.page.addEventListener(EVENT_NAME.NODE_NAME_CHANGED, onNodeNameChange);

    // 设置初始化treeData.
    setTreeData(buildTreeData(shape, treeFilter, typeFilter));

    // 当组件unmount时，停止监听.
    return () => {
      stopObserve.current && stopObserve.current();
      shape.page.removeEventListener(EVENT_NAME.NODE_NAME_CHANGED, onNodeNameChange);
    };
  }, []);

  /*
   * reference变化需要重新监听.
   * 只有referenceNode或referenceId变化时，才需要改变监听的observable.
   */
  useEffect(() => {
    stopObserve.current && stopObserve.current();
    // disabled为true的状态下，不需要注册监听，只展示即可.
    if (reference.referenceNode && reference.referenceId && !rest.disabled) {
      stopObserve.current = shape.observeTo(reference.id,
        reference.referenceNode,
        reference.referenceId,
        (args) => {
          const newTreeData = buildTreeData(shape, treeFilter, typeFilter);

          // *** 重要 *** 这一步不能省略，当observable比观察者晚注册时，需要在此时刷新树状下拉列表数据.
          setTreeData(newTreeData);

          const treeMap = new Map(newTreeData.map(d => [d.id, d]));
          const treeNode = treeMap.get(reference.referenceId);

          if (treeNode) {
            treeNode.title = args.value;
            const value = buildValue(treeMap, treeNode);
            onReferencedValueChange(args.value, value, args.type);
          } else {
            onReferencedValueChange(args.value, [], args.type);
          }
        });
    }

    referenceNodeIdRef.current = reference.referenceNode;
    referenceIdRef.current = reference.referenceId;
  }, [reference.referenceNode, reference.referenceId, rest.disabled]);

  /*
   * 当referenceKey变化时，需要修改显示的值.
   */
  useEffect(() => {
    if (!reference.referenceKey) {
      // referenceKey为null，需要将显示的值设置为空.
      form.setFieldsValue({[name]: null});
    } else {
      // 这里不能直接使用treeData，当复制一个图形，并且连线时，treeData还是原来的数据，会导致异常.
      setTreeData(buildTreeData(shape, treeFilter, typeFilter));
      form.setFieldsValue({[name]: reference.referenceId});
    }
  }, [reference.referenceKey]);

  return (<>
    <Form.Item
      id={`reference-id-${reference.id}`}
      name={[name]}
      initialValue={reference.referenceId}
      rules={rules}
      validateTrigger='onBlur'
    >
      <TreeSelect
        {...rest}
        getPopupContainer={trigger => trigger.parentNode}
        className={combinedClassName}
        treeDataSimpleMode
        dropdownStyle={{maxHeight: 400, overflow: 'auto', minWidth: 250}}
        placeholder={t('pleaseSelect')}
        onChange={(e) => rest.onChange ? rest.onChange(e, treeData) : onChange(e, treeData)}
        treeData={treeData}
        onDropdownVisibleChange={() => {
          setTreeData(buildTreeData(shape, treeFilter, typeFilter));
        }}
        treeDefaultExpandedKeys={[reference.referenceId]}
        treeNodeLabelProp={'label'}
        dropdownRender={handleDropdownRender}
      />
    </Form.Item>
  </>);
};

JadeReferenceTreeSelect.propTypes = {
  reference: PropTypes.object,
  onReferencedValueChange: PropTypes.func,
  onReferencedKeyChange: PropTypes.func,
  rules: PropTypes.array,
  className: PropTypes.string,
  treeFilter: PropTypes.func,
  typeFilter: PropTypes.func,
};