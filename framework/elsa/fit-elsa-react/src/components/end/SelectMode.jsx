/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';

/**
 * 选择模式的组件
 *
 * @param mode 模式
 * @param onSelectChange 更改模式的回调
 * @returns {JSX.Element}
 * @constructor
 */
export const SelectMode = ({mode, disabled}) => {
    const dispatch = useDispatch();
    const {t} = useTranslation();

    /**
     * 模式切换后的回调
     *
     * @param value 模式的值
     */
    const onChange = (value) => {
        if (value === mode) {
            return;
        }
        dispatch({type: 'changeMode', value});
    };

    return (<>
        <div style={{display: 'flex', alignItems: 'center'}}>
            <div className='mode-select-title jade-panel-header-font'>{t('modeSelect')}</div>
            <JadeStopPropagationSelect
                disabled={disabled}
                className={'jade-select'}
                showSearch
                optionFilterProp='children'
                onChange={onChange}
                defaultValue='mode-variables'
                value={mode}
                options={[
                    {value: 'variables', label: t('directlyOutputTheResult')},
                    {value: 'manualCheck', label: t('intelligentFormShowResult')},
                ]}
                style={{width: '70%', marginLeft: 'auto'}}
            >
            </JadeStopPropagationSelect>
        </div>
    </>);
};

SelectMode.propTypes = {
    mode: PropTypes.string,
    disabled: PropTypes.bool,
};