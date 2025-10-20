/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {JadeTagRender} from '@/components/common/JadeTagRender.jsx';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import {Form, TreeSelect} from 'antd';
import {buildTreeData, buildValue, handleDropdownRender} from '@/components/util/TreeSelectUtils.jsx';
import {useTranslation} from 'react-i18next';

/**
 * jade的带引用功能的级联选择框.
 *
 * @param props 参数
 * @return {JSX.Element}
 * @constructor
 */
const _JadeReferenceMultiTreeSelect = (props) => {
  const {reference, onReferencedValueChange, onReferencedKeyChange, rules, className, width, moveOutReference, treeFilter, typeFilter, ...rest} = props;
  const shape = useShapeContext();
  const {t} = useTranslation();
  const name = `reference-${reference.id}`;
  const combinedClassName = `jade-tree-select ${className || ''}`.trim();
  const [treeData, setTreeData] = useState([]);
  const defaultWidth = 100;

  const getChangedData = (selected) => {
    return selected.map(id => {
      const treeMap = new Map(treeData.map(d => [d.id, d]));
      const treeNode = treeMap.get(id);
      if (!treeNode) {
        return null;
      }
      const value = buildValue(treeMap, treeNode);
      return {
        referenceNode: treeNode.nId,
        referenceId: treeNode.value,
        value,
        type: treeNode.type,
        referenceKey: treeNode.title,
      };
    }).filter(data => data);
  };

  /**
   * 当选择发生变化时，触发.监听不同的组件.
   *
   * @param selected 是个数组，包含从根节点到被选中节点的所有节点数据.
   */
  const onChange = (selected) => {
    if (!selected) {
      return;
    }
    onReferencedKeyChange(getChangedData(selected));
  };

  const getReferenceNodeName = (referenceInfo) => {
    return referenceInfo ? shape.page.getShapeById(referenceInfo.referenceNode)?.text ?? undefined : undefined;
  };

  const getReferenceKey = (referenceInfo) => {
    return referenceInfo?.referenceKey ?? undefined;
  };

  const getDisplayTitle = (referenceInfo) => {
    return `${(getReferenceNodeName(referenceInfo))}/${(getReferenceKey(referenceInfo))}`;
  };

  const getTagRender = () => {
    return (properties) => {
      const referenceInfo = reference.value.find(item => item.referenceId === properties.value) ??
        moveOutReference.find(item => item.referenceId === properties.value);
      if (!referenceInfo) {
        return <></>;
      }
      return (
        <JadeTagRender
          {...properties} showRemoveButton={true} reference={referenceInfo}
          _onReferenceValueChange={onReferencedValueChange} disabled={rest.disabled}
          displayTitle={getDisplayTitle(referenceInfo)} {...rest}/>
      );
    };
  };

  return (<>
    <Form.Item
      id={`reference-id-${reference.id}`}
      name={[name]}
      initialValue={[...reference.value.map(v => v.referenceId), ...moveOutReference.map(item => item.referenceId)]}
      rules={rules}
      validateTrigger="onBlur"
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
        multiple={true}
        showArrow
        treeDefaultExpandAll={false}
        style={{width: width || defaultWidth}}
        dropdownRender={handleDropdownRender}
        tagRender={getTagRender()}
      />
    </Form.Item>
  </>);
};

_JadeReferenceMultiTreeSelect.propTypes = {
  reference: PropTypes.object,
  onReferencedValueChange: PropTypes.func,
  onReferencedKeyChange: PropTypes.func,
  rules: PropTypes.array,
  className: PropTypes.string,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.reference.value === nextProps.reference.value &&
    prevProps.moveOutReference === nextProps.moveOutReference;
};

export const JadeReferenceMultiTreeSelect = React.memo(_JadeReferenceMultiTreeSelect, areEqual);