/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import SectionHeader from '@/components/flowRunComponent/SectionHeader.jsx';
import SectionContent from '@/components/flowRunComponent/SectionContent.jsx';
import {v4 as uuidv4} from 'uuid';
import {Card} from 'antd';
import {DATA_TYPES, SECTION_TYPE, UNARY_OPERATOR, VIRTUAL_CONTEXT_NODE} from '@/common/Consts.js';
import {useTranslation} from 'react-i18next';

/**
 * 构造运行报告的章节
 *
 * @param shape 图形
 * @return {*}
 * @constructor
 */
export default function SectionFactory({shape}) {
    const {t} = useTranslation();
    // 一元表达式
    const unaryOperators = Object.values(UNARY_OPERATOR);

    /**
     * 构造默认章节
     *
     * @param section 章节信息
     * @return {JSX.Element}
     */
    const buildDefaultSection = section => {
        section.name = t(section.name)
        return (<>
            <div key={`section-${section.no}-${uuidv4()}`} className="section">
                <SectionHeader section={section} shape={shape}/>
                <div className="json-tree-section default-json-tree-section-width">
                    <SectionContent data={Object.keys(section.data).length === 0 ? `""` : section.data}/>
                </div>
            </div>
        </>);
    }

    const convertValue = (type, value) => {
        if (type === DATA_TYPES.OBJECT) {
            return value ? value : '{}';
        } else if (type === DATA_TYPES.ARRAY) {
            if (Array.isArray(value)) {
                return value.length !== 0 ? value : '[]';
            }
            return value;
        } else {
            return value;
        }
    };

    const _convertLeftValue = (condition, value) => {
        return convertValue(condition.left.type, value);
    };

    const _convertRightValue = (condition) => {
        // 如果是一元运算符，则不处理右值
        if (unaryOperators.includes(condition.condition)) {
            return {};
        }
        // 后端不会返回{}这种类型的空对象数据
        let rightValue = condition.right.value;
        return convertValue(condition.right.type, rightValue);
    };

    /**
     * 构造条件章节
     *
     * @param section 章节信息
     * @return {JSX.Element}
     */
    const buildConditionSection = section => {
        section.name = t(section.name) + " " + section.no;
        return (<>
            <div key={`section-${section.no}-${uuidv4()}`} className="section">
                <SectionHeader section={section} shape={shape}/>
                {section.data.conditions
                        .filter(c => c.condition !== "true" || c.left)
                        .map((condition, index) => {
                            let {key, value} = condition.left;
                            value = _convertLeftValue(condition, value);
                            const rightValue = _convertRightValue(condition);
                            const separatorIndex = key.indexOf(".");
                            const referenceNodeId = key.substring(0, separatorIndex);
                            const text = referenceNodeId === VIRTUAL_CONTEXT_NODE.id ? t('systemEnv') : shape.page.getShapeById(referenceNodeId)?.text ?? '';
                            const newKey = text + key.substring(separatorIndex);
                            let isShowLogic = section.data.conditions.length > 1 && index < section.data.conditions.length - 1;
                            return (<div key={`condition-container-${section.no}-${uuidv4()}`}
                                         className={"condition-container"}>
                                <div key={`condition-card-${index}-${uuidv4()}`} className="condition-card">
                                    <div className="json-tree-section condition-json-tree-section-width">
                                        <SectionContent data={{[newKey]: value}}/>
                                    </div>
                                    <svg className="line-svg">
                                        <line x1="50%" y1="0" x2="50%" y2="100%" stroke="rgba(29,28,35,.16)"/>
                                    </svg>
                                    <div className="center-card">
                                        <div className="center-text">{condition.condition}</div>
                                    </div>
                                    <div className="json-tree-section condition-json-tree-section-width">
                                        <SectionContent data={rightValue}/>
                                    </div>
                                </div>
                                {isShowLogic && <div className={"condition-logic"}>
                                    {section.data.conditionRelation}
                                </div>}
                            </div>);
                        })}
            </div>
        </>)
    };


    /**
     * 根据图形类型获取测试报告
     *
     * @return {*}
     */
    const getReportSections = () => {
        return shape.getRunReportSections().map(section => {
            // 这里改为根据type来决定，如何绘制
            if (section.type === SECTION_TYPE.DEFAULT) {
                return buildDefaultSection(section);
            } else if (section.type === SECTION_TYPE.CONDITION) {
                return buildConditionSection(section);
            }
          return undefined;
        });
    };

    return (getReportSections());
}