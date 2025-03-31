/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {forwardRef, useImperativeHandle, useRef, useState} from "react";
import {Button, Row, Col} from "antd";
import RunReport from "@/components/flowRunComponent/RunReport.jsx";
import Icon from "@ant-design/icons";
import IconRunningLoading from '../asserts/icon-running-loading.svg?react';
import IconRunningResult from '../asserts/icon-running-result.svg?react';
import IconRunningUnRunning from '../asserts/icon-running-unRunning.svg?react';
import IconRunningSuccess from '../asserts/icon-running-success.svg?react';
import IconRunningFailed from '../asserts/icon-running-failed.svg?react';
import IconRunningTerminated from '../asserts/icon-running-terminated.svg?react';
import './style.css';
import {NODE_STATUS} from '@';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';

// 左右上下的边距.
const MARGIN = {
    left: 20,
    right: 20,
    top: 64,
    bottom: 20
};

/**
 * 运行状态面板
 *
 * @type {*} 面板组件.
 */
const RunningStatusPanel = forwardRef(function ({shape, shapeStatus, onReportShow}, ref) {
    const {t} = useTranslation();
    const [showResultPanel, setShowResultPanel] = useState(false);
    const [, setHeight] = useState(shape.height);
    const reportRef = useRef();
    const status = getStatus(shapeStatus.runStatus);

    // 对外暴露方法.
    useImperativeHandle(ref, () => {
        return {
            getRunReportRect: () => {
                return reportRef.current && reportRef.current.getRunReportRect();
            },
            setHeight: (height) => {
                setHeight(height);
            }
        }
    });

    const getWidth = () => {
        return shape.width + MARGIN.left + MARGIN.right;
    };

    const getHeight = () => {
        return shape.height + MARGIN.top + MARGIN.bottom;
    };

    const getStyles = () => {
        return {
            left: `-${MARGIN.left}px`,
            top: `-${MARGIN.top}px`,
            width: getWidth(),
            height: getHeight(),
            zIndex: -100,
        };
    };

    /**
     * 展开关闭测试报告的回调
     */
    const handleExpandResult = () => {
        setShowResultPanel(!showResultPanel);
        setTimeout(() => {
            if (!showResultPanel) {
                onReportShow();
            }
        });
    };

    return (<>
        <div className={'running-status-panel'} style={getStyles()}>
            <Row style={{width: '95%', height: 20, marginLeft: MARGIN.left, marginTop: (MARGIN.top - 20) / 2}} justify="space-between" align="middle">
                <Col>
                    <Row align="middle">
                        <div className="icon-container">{status.getIcon()}</div>
                        <div className="run-text"><span>{status.title}</span></div>
                        {status.getTime(shape)}
                    </Row>
                </Col>
                <Col>
                    <Button
                        type="text"
                        className="button-text button-text-no-hover-effect"
                        disabled={!status.enableReport}
                        onMouseDown={(e) => {
                            // 防止每次点击触发elsa的鼠标事件，导致图形取消选中.
                            e.stopPropagation();
                            shape.page.getFocusedShapes().forEach(s => s.unSelect());
                            shape.select();
                        }}
                        onClick={handleExpandResult}
                        icon={<Icon component={IconRunningResult} />}
                    >
                        {showResultPanel ? t('closeRunResult') : t('runResult')}
                    </Button>
                </Col>
            </Row>
        </div>
        {showResultPanel && (
                <RunReport shape={shape}
                           ref={reportRef}
                           showResultPanel={showResultPanel}
                           handleExpandResult={handleExpandResult}/>
        )}
        <div className="jade-panel-background" style={{
            left: 0,
            top: 0,
            width: shape.width,
            height: shape.height,
            borderRadius: shape.borderRadius,
            zIndex: -99,
        }}/>
    </>);
});

RunningStatusPanel.propTypes = {
    shape: PropTypes.object,
    shapeStatus: PropTypes.object,
    onReportShow: PropTypes.func
};

const SvgComponent = (props) => {
    const {SvgCom, ...rest} = props;
    return <SvgCom width={20} height={20} {...rest}/>;
};

SvgComponent.propTypes = {
    SvgCom: PropTypes.object
};

const TimeComponent = ({shape, runStatus}) => {
    return (<>
        <div className={`time-text-container time-text-container-${runStatus}`}>
            <div className={`time-text time-text-${runStatus}`}>
                {shape.cost ? (shape.cost / 1000) + "s" : "0.000s"}
            </div>
        </div>
    </>);
};

TimeComponent.propTypes = {
    shape: PropTypes.object,
    runStatus: PropTypes.string,
};

const getStatus = (nodeStatus) => {
  const {t} = useTranslation();
  switch (nodeStatus) {
    case NODE_STATUS.SUCCESS:
      return {
        title: t('runSuccessfully'),
        enableReport: true,
        getTime: (shape) => {
          return <TimeComponent shape={shape} runStatus={nodeStatus}/>;
        },
        getIcon: () => {
          return <Icon component={(props) => <SvgComponent {...props} SvgCom={IconRunningSuccess}/>}/>;
        },
      };
    case NODE_STATUS.ERROR:
      return {
        title: t('runFailed'),
        enableReport: true,
        getTime: (shape) => {
          return <TimeComponent shape={shape}/>;
        },
        getIcon: () => {
          return <Icon component={(props) => <SvgComponent {...props} SvgCom={IconRunningFailed}/>}/>;
        },
      };
    case NODE_STATUS.RUNNING:
      return {
        title: t('running'),
        enableReport: false,
        getTime: () => {
          return null;
        },
        getIcon: () => {
          return <Icon
            spin={true}
            component={(props) => <SvgComponent {...props} SvgCom={IconRunningLoading}/>}/>;
        },
      };
    case NODE_STATUS.UN_RUNNING:
      return {
        title: t('notRunning'),
        enableReport: false,
        getTime: () => {
          return null;
        },
        getIcon: () => {
          return <Icon component={(props) => <SvgComponent {...props} SvgCom={IconRunningUnRunning}/>}/>;
        },
      };
    case NODE_STATUS.TERMINATED:
      return {
        title: t('terminated'),
        enableReport: false,
        getTime: (shape) => {
          return <TimeComponent shape={shape}/>;
        },
        getIcon: () => {
          return <Icon component={(props) => <SvgComponent {...props} SvgCom={IconRunningTerminated}/>}/>;
        },
      };
    default:
      throw new Error(`Unsupported node status[${nodeStatus}]`);
  }
};

export default RunningStatusPanel;