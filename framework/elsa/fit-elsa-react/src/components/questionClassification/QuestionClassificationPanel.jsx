/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Button, Collapse, Form, Popover} from 'antd';
import TextArea from 'antd/es/input/TextArea.js';
import {PlusOutlined, QuestionCircleOutlined} from '@ant-design/icons';
import {Trans, useTranslation} from 'react-i18next';
import DeleteItem from '../asserts/icon-tool-delete.svg?react';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {ConnectorProvider} from '@/components/common/ConnectorProvider.jsx';

const {Panel} = Collapse;

/**
 * 问题分类面板
 *
 * @param disabled
 * @param shapeId
 * @param questionTypeList
 * @returns {React.JSX.Element}
 * @private
 */
const _QuestionClassificationPanel = ({disabled, shapeId, questionTypeList}) => {
  const dispatch = useDispatch();
  const {t} = useTranslation();

  const content = (<>
    <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <Trans i18nKey='classificationPopover' components={{p: <p/>}}/>
    </div>
  </>);

  /**
   * 渲染问题分类条目
   *
   * @returns {React.JSX.Element}
   */
  const renderQuestionItem = () => <>
    {questionTypeList.map((question, index) => {
      const questionId = question.value.find(item => item.name === 'id');
      const isLastIndex = index === questionTypeList.length - 1;
      if (!isLastIndex) {
        return (<ConnectorProvider key={`dynamic-${isLastIndex ? `999` : questionId.value}`}
                                   name={`dynamic-${isLastIndex ? `999` : `${index}|`}${questionId.value}`}>
          <QuestionClassificationItem question={question} index={index} disabled={disabled} isLastIndex={isLastIndex}/>
        </ConnectorProvider>);
      }
    })}
    {<ConnectorProvider key={`dynamic-999`} name={`dynamic-999`}>
      <QuestionClassificationItem question={questionTypeList[questionTypeList.length - 1]} index={questionTypeList.length - 1} disabled={disabled}
                                  isLastIndex={true}/>
    </ConnectorProvider>}
  </>;

  return (<>
      <div style={{
        display: 'flex',
        alignItems: 'center',
        marginBottom: '8px',
        paddingLeft: '8px',
        paddingRight: '4px',
        height: '32px',
      }}>
        <span className='jade-panel-header-font'>{t('classification')}</span>
        <Popover
          content={content}
          align={{offset: [0, 3]}}
          overlayClassName={'jade-custom-popover'}
        >
          <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
        </Popover>
      </div>
      {renderQuestionItem()}
      <AddQuestionClassification disabled={disabled} dispatch={dispatch} t={t}/>
  </>);
};

_QuestionClassificationPanel.propTypes = {
  questionTypeList: PropTypes.array.isRequired,
  disabled: PropTypes.bool.isRequired,
  shapeId: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.questionTypeList === nextProps.questionTypeList &&
    prevProps.shapeId === nextProps.shapeId;
};

/**
 * 添加问题分类按钮组件
 *
 * @param disabled 是否禁用
 * @param dispatch dispatch方法
 * @param t 国际化组件
 * @returns {React.JSX.Element} 添加问题分类按钮组件
 * @constructor
 */
const AddQuestionClassification = ({disabled, dispatch, t}) => {
  /**
   * 点击添加新的问题分类
   *
   * @param event 事件
   */
  const onAddQuestionClassificationClick = event => {
    dispatch({actionType: 'addQuestion'});
  };

  return (<>
    <div className={'add-question-classification-wrapper'}>
      <Button disabled={disabled}
              type='text' className='icon-button add-question-classification-button-style'
              onClick={(event) => {
                onAddQuestionClassificationClick(event);
              }}>
        <div className={'add-question-classification-btn'}>
          <PlusOutlined/>
          <div className={'add-question-classification-text'}>
            {t('addQuestionClassification')}
          </div>
        </div>
      </Button>
    </div>
  </>);
};

AddQuestionClassification.propTypes = {
  t: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
  dispatch: PropTypes.func.isRequired,
};

/**
 * 问题分类条目
 *
 * @param disabled 是否禁用
 * @param question 问题
 * @param index 索引
 * @param isLastIndex 是否最后一个索引
 * @returns {React.JSX.Element} 问题分类条目
 * @constructor
 */
const QuestionClassificationItem = ({disabled, question, index, isLastIndex}) => {
  const shapeId = useShapeContext().id;
  const dispatch = useDispatch();
  const {t} = useTranslation();
  const type = question.conditionType;
  const questionId = question.value.find(item => item.name === 'id');
  const questionDesc = question.value.find(item => item.name === 'questionTypeDesc');

  /**
   * 删除问题分类
   *
   * @param itemId 知识库id
   */
  const handleDelete = (itemId) => {
    dispatch({actionType: 'deleteQuestion', value: itemId});
  };

  /**
   * 失焦时才设置值，对于必填项.若为空，则不设置
   *
   * @param e event
   * @param actionType 事件类型
   * @param id config的id
   */
  const changeOnBlur = (e, actionType, id) => {
    dispatch({actionType: actionType, id: id, value: e.target.value});
  };

  /**
   * 获取问题分类的标题
   *
   * @returns {TFuncReturn<'translation', string, string, undefined>|string}
   */
  const getQuestionTitle = () => {
    if (isLastIndex) {
      return t('otherQuestion');
    } else {
      return `${t('classification')}${index + 1}`;
    }
  };

  const getClassName = () => {
    if (isLastIndex) {
      return ('last-question-classification-textarea-height');
    }
    return ('question-classification-textarea-normal-height');
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={[`questionClassificationPanel${shapeId}-${questionId.value}`]}>
      {<Panel
        key={`questionClassificationPanel${shapeId}-${questionId.value}`}
        header={
          <div className={'classification-title-row'}>
            <span className='classification-title-text'>{getQuestionTitle()}</span>
            {type === 'if' && <Button
              disabled={disabled}
              type='text'
              icon={<DeleteItem/>}
              className='classification-title-delete-btn'
              onClick={() => handleDelete(question.id)}/>}
          </div>
        }
        className='jade-panel'
      >
        <div className={'classification-component'}>
          <Form.Item
            className='jade-form-item'
            name={`question-${shapeId}-${questionId.value}`}
            id={`question-${shapeId}-${questionId.value}`}
            initialValue={questionDesc.value}
            validateTrigger='onBlur'
          >
            <TextArea
              disabled={disabled || type === 'else'}
              maxLength={100}
              className={`question-classification-textarea-input ${getClassName()}`}
              onBlur={(e) => changeOnBlur(e, 'changeQuestionDesc', question.id)}
            />
          </Form.Item>
        </div>
      </Panel>}
    </Collapse>
  </>);
};

QuestionClassificationItem.propTypes = {
  question: PropTypes.object.isRequired,
  disabled: PropTypes.bool.isRequired,
  isLastIndex: PropTypes.bool.isRequired,
  index: PropTypes.number.isRequired,
};

export const QuestionClassificationPanel = React.memo(_QuestionClassificationPanel, areEqual);