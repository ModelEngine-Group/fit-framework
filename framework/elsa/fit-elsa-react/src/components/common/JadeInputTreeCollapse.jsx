/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Collapse, Popover} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

JadeInputTreeCollapse.propTypes = {
    data: PropTypes.array.isRequired,
    children: PropTypes.array.isRequired
};

/**
 * 带input的树形组件.主要用于api调用节点.
 *
 * @param data 数据.
 * @param children 子组件列表.
 * @param props 参数。
 * @return {JSX.Element}
 * @constructor
 */
export default function JadeInputTreeCollapse({data, children, ...props}) {
    const { t } = useTranslation();

    const getContent = () => {
        const contentItems = data
            .filter(item => item.description) // 过滤出有描述的项目
            .map((item) => (
                <p key={item.id}>{item.name}: {item.description}</p>
            ));

        if (contentItems.length === 0) {
            return null; // 如果没有内容，返回null
        }

        return (
            <div className={'jade-font-size'} style={{ lineHeight: '1.2' }}>
                <p>{t('parameterDescription')}</p>
                {contentItems}
            </div>
        );
    };

    const content = getContent();

    return (<>
        <JadeCollapse defaultActiveKey={['jadeInputTreePanel']} {...props}>
            <Panel
                key={'jadeInputTreePanel'}
                header={
                    <div>
                        <span className='jade-panel-header-font'>{t('input')}</span>
                        {content ? (
                          <Popover
                            content={content}
                            align={{offset: [0, 3]}}
                            overlayClassName={'jade-custom-popover'}
                          >
                                <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
                            </Popover>
                        ) : null}
                    </div>
                }
                className='jade-panel'
            >
                <div className={'jade-custom-panel-content'}>
                    {children}
                </div>
            </Panel>
        </JadeCollapse>
    </>);
}