/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ByConversationTurn from '@/components/start/ByConversationTurn.jsx';
import ByNumber from '@/components/start/ByNumber.jsx';
import ByTokenSize from '@/components/start/ByTokenSize.jsx';
import ByTime from '@/components/start/ByTime.jsx';
import Customizing from '@/components/start/Customizing.jsx';
import {useDataContext, useDispatch} from '@/components/DefaultRoot.jsx';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import PropTypes from 'prop-types';
import {FLOW_TYPE} from '@/common/Consts.js';

Memory.propTypes = {
    config: PropTypes.object.isRequired, // 确保 config 是一个必需的对象类型
};

/**
 * 开始节点Memory组件
 *
 * @param config 配置.
 * @param disabled 禁用.
 * @returns {JSX.Element} 开始节点Memory组件的Dom
 */
export default function Memory({config, disabled}) {
    const dispatch = useDispatch();
    const data = useDataContext();
    const memoryTypeValue = data.find(item => item.name === 'memory').value.find(item => item.name === 'type').value;
    const memoryValueValue = data.find(item => item.name === 'memory').value.find(item => item.name === 'value').value;

    const handleSelectClick = (event) => {
        event.stopPropagation(); // 阻止事件冒泡
    };

    // 处理内部组件值变化的回调函数
    const handleComponentValueChange = (valueType, newValue) => {
        dispatch({actionType: 'changeMemory', memoryType: memoryTypeValue, memoryValueType: valueType, memoryValue: newValue});
    };

    // 根据不同的值渲染不同的组件
    const renderComponent = () => {
        switch (memoryTypeValue) {
            case 'ByConversationTurn': // 按对话轮次(一问一答，0-10，默认3)
                return <ByConversationTurn disabled={disabled}
                                           propValue={memoryValueValue}
                                           onValueChange={handleComponentValueChange}/>;
            case 'ByNumber': // 按条数(默认20)
                return <ByNumber disabled={disabled}
                                 propValue={memoryValueValue}
                                 onValueChange={handleComponentValueChange}/>;
            case 'ByTokenSize': // 按token大小
                return <ByTokenSize disabled={disabled}
                                    propValue={memoryValueValue}
                                    onValueChange={handleComponentValueChange}/>;
            case 'ByTime': // 按时间
                return <ByTime disabled={disabled}
                               propValue={memoryValueValue}
                               onValueChange={handleComponentValueChange}/>;
            case 'Customizing': // 自定义(选fitable)
                return <Customizing disabled={disabled}
                                    propValue={memoryValueValue}
                                    onValueChange={handleComponentValueChange}
                                    config={config}/>;
            case 'UserSelect': // 用户自己勾选(true、false)
                return null;
            case 'NotUseMemory': // 不使用历史记录
                return null;
            default:
                return null;
        }
    };

    const onChange = (e) => {
        let memoryValueType = ''; // 初始化 memoryValueType
        let memoryValue = null; // 初始化 memoryValue

        // 由于一级菜单变化时，二级菜单的render还没到，所以二级菜单默认值设置在此，用于dispatch至jadeConfig中，二级菜单读取jadeConfig值进行渲染
        // 根据选择的值设置 memoryType 和 memoryValueType
        switch (e) {
            case 'ByConversationTurn':
                memoryValueType = 'Integer';
                memoryValue = '3';
                break;
            case 'ByNumber':
                memoryValueType = 'Integer';
                memoryValue = '20';
                break;
            case 'ByTokenSize':
                memoryValueType = 'Integer';
                memoryValue = '1000';
                break;
            case 'ByTime':
                memoryValueType = 'String';
                memoryValue = 'oneHour';
                break;
            case 'Customizing':
                memoryValueType = 'String';
                break;
            default:
                break;
        }
        dispatch({
            actionType: 'changeMemory',
            memoryType: e,
            memoryValueType: memoryValueType,
            memoryValue: memoryValue
        });
        document.activeElement.blur();// 在选择后取消焦点
    }

    const getOptions = (config) => {
        const defaultOptions = [
            { value: 'ByConversationTurn', label: '按对话轮次' },
            // 430演示大模型选项不需要按条数、按Token大小、按时间，暂时屏蔽
            // { value: 'ByNumber', label: '按条数' },
            // { value: 'ByTokenSize', label: '按Token大小' },
            // { value: 'ByTime', label: '按时间' },
            // { value: 'Customizing', label: '自定义' },
            { value: 'NotUseMemory', label: '不使用历史记录' }
        ];

        // 如果 config 或 config.params 为空，设置默认 type 为 'app'
        const type = config?.params?.type || 'app';

        // 根据 type 返回不同的 options
        switch (type) {
            case FLOW_TYPE.WORK_FLOW:
                return [
                    ...defaultOptions,
                    { value: 'UserSelect', label: '用户自勾选' }
                ];
            default:
                return defaultOptions;
        }
    }

    return (<>
        <JadeStopPropagationSelect
            className='jade-select'
            disabled={disabled}
            defaultValue={memoryTypeValue}
            style={{width: '100%', marginBottom: '8px', marginTop: '8px'}}
            onClick={handleSelectClick} // 点击下拉框时阻止事件冒泡
            onChange={e => onChange(e)}
            options={getOptions(config)}
        />
        {renderComponent()} {/* 渲染对应的组件 */}
    </>);
}