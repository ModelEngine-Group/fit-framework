/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import React from 'react';
import {Collapse, Slider} from 'antd';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {useTranslation} from 'react-i18next';

const {Panel} = Collapse;
const DEFAULT_TEXT_TO_IMAGE_COUNT_CONFIG_RECALLS = {
  1: '1', 5: '5',
};

/**
 * 文生图节点生成图片配置组件
 *
 * @param disabled 是否禁用
 * @param imageCount 生成图片数量
 * @returns {Element}
 * @private
 */
const _TextToImageParamConfig = ({disabled, imageCount}) => {
  const dispatch = useDispatch();
  const shapeId = useShapeContext().id;
  const {t} = useTranslation();

  const handleSliderChange = (value) => {
    dispatch({actionType: 'changeImageCount', value: value});
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={[`textToImageParamPanel-${shapeId}`]}>
      {<Panel
        key={`textToImageParamPanel-${shapeId}`}
        header={<div className='panel-header'>
          <span className='jade-panel-header-font'
                style={{whiteSpace: 'nowrap', textOverflow: 'ellipsis'}}>{t('textToImageParamConfig')}</span>
        </div>}
        className='jade-panel'
      >
        <div className={'textToImageConfig-wrapper'}>
          <div className={'image-count-title-wrapper'}>
            <span>{t('generateCount')}</span>&nbsp;&nbsp;
            <span style={{fontWeight: 'bold'}}>{imageCount.value} {t('imageUnit')}</span>
          </div>
          <Slider disabled={disabled}
                  className='image-count-slider'
                  min={1}
                  max={5}
                  defaultValue={2}
                  value={imageCount.value}
                  marks={DEFAULT_TEXT_TO_IMAGE_COUNT_CONFIG_RECALLS}
                  onChange={handleSliderChange}
          />
        </div>
      </Panel>}
    </Collapse>
  </>);
};

_TextToImageParamConfig.propTypes = {
  disabled: PropTypes.bool,
  imageCount: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.imageCount === nextProps.imageCount;
};

export const TextToImageParamConfig = React.memo(_TextToImageParamConfig, areEqual);