/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Radio} from 'antd';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import SimpleFormWrapper from '@/components/intelligentForm/SimpleFormWrapper.jsx';
import ManualCheckFormWrapper from '@/components/manualCheck/ManualCheckFormWrapper.jsx';
import {useTranslation} from 'react-i18next';
import {FORM_TYPE} from '@/components/intelligentForm/Consts.js';

IntelligentFormWrapper.propTypes = {
  data: PropTypes.object.isRequired,
  shapeStatus: PropTypes.object,
};

/**
 * 智能表单Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态.
 * @returns {JSX.Element} 智能表单Wrapper的DOM
 */
export default function IntelligentFormWrapper({data, shapeStatus}) {
  const {t} = useTranslation();
  const dispatch = useDispatch();

  const changeFormType = (e) => {
    dispatch({type: 'changeFormType', value: e.target.value});
  };

  const getFormTypeOptions = () => {
    return [{label: t('orchestration'), value: FORM_TYPE.ORCHESTRATION}, {label: t('manual'), value: FORM_TYPE.MANUAL}];
  };

  return (<>
    <div>
      <div style={{fontSize: '16px', fontFamily: 'Huawei Sans', fontWeight: 400, display: 'flex', paddingBottom: '8px'}}>
        <span>{t('type')}</span>
        <Radio.Group
          value={data.formType}
          onChange={changeFormType}
          options={getFormTypeOptions()}
          buttonStyle="solid"
          style={{fontFamily: 'Huawei Sans', paddingLeft: '8px'}}
        />
      </div>
      {
        data.formType === FORM_TYPE.ORCHESTRATION ? (
          <SimpleFormWrapper data={data} shapeStatus={shapeStatus}/>
        ) : (
          <ManualCheckFormWrapper data={data} shapeStatus={shapeStatus}/>
        )
      }
    </div>
  </>);
}