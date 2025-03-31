/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ReactDOM from 'react-dom/client';
import {isPointInRect, rectangleDrawer} from '@fit-elsa/elsa-core';
import React, {useRef, useState} from 'react';
import {NODE_STATUS} from '@';
import RunningStatusPanel from '@/components/flowRunComponent/RunningStatusPanel.jsx';
import {DefaultRoot} from '@/components/DefaultRoot.jsx';
import {HORIZONTAL_LEFT, HORIZONTAL_RIGHT, VERTICAL_DOWN, VERTICAL_UP} from '../asserts/svgIcons.jsx?react';
import ToolIcon from '../asserts/icon-tool.svg?react';
import {Header} from '@/components/Header.jsx';
import {Footer} from '@/components/Footer.jsx';
import {Tooltip} from 'antd';
import {I18nextProvider} from 'react-i18next';
import {CONNECTOR} from '@/common/Consts.js';

const WATER_DROP_DISTANCE = 500;
const BORDER_PADDING = 16;

/**
 * jadeNode绘制器.
 *
 * @override
 */
export const jadeNodeDrawer = (shape, div, x, y) => {
    const self = rectangleDrawer(shape, div, x, y);
    self.type = "jadeNodeDrawer";
    self.reactContainer = null;
    self.panelRef = null;
    self.waterDrops = [];

    /**
     * @override
     */
    const initialize = self.initialize;
    self.initialize = () => {
        initialize.apply(self);
        self.reactContainer = document.createElement("div");
        self.reactContainer.id = "react-container-" + shape.id;
        self.reactContainer.style.width = "100%";

        // 留出connector的位置.
        self.reactContainer.style.margin = CONNECTOR.RADIUS + "px";
        self.parent.appendChild(self.reactContainer);
        self.parent.style.pointerEvents = "auto";
    };

    /**
     * 写在react.
     */
    self.unmountReact = () => {
        if (!self.root) {
            return;
        }
        self.root.unmount();
        self.root = null;
    };

    /**
     * @override
     */
    self.drawStatic = () => {
        if (!shape.componentName || self.root) {
            return;
        }
        self.root = ReactDOM.createRoot(self.reactContainer);
        self.root.render(
            <I18nextProvider i18n={shape.graph.i18n}>
                <JadeWrapper/>
            </I18nextProvider>
        );
    };

    /**
     * 获取组件最新配置数据.
     *
     * @return {*} 配置数据.
     */
    self.getLatestJadeConfig = () => {
        return self.rootRef && self.rootRef.current && self.rootRef.current.getData();
    };

    // 修改入参.
    self.dispatch = (action) => {
        self.rootRef.current && self.rootRef.current.dispatch(action);
    };

    const JadeWrapper = () => {
        self.panelRef = useRef();
        self.rootRef = useRef();
        const [shapeStatus, setShapeStatus] = useState({
            disabled: shape.disabled,
            runnable: shape.runnable,
            runStatus: shape.runStatus,
            enableMask: shape.enableMask,
            referenceDisabled: shape.referenceDisabled,
            isFocused: shape.isFocused,
            isInDragging: shape.inDragging,
        });

        // 设置节点状态.
        self.setShapeStatus = (updated) => {
            const newStatus = {...shapeStatus};
            Object.keys(updated).forEach(k => {
                const v = updated[k];
                if (v === null || v === undefined) {
                    return;
                }
                newStatus[k] = updated[k];
            });
            setShapeStatus(newStatus);
        };

        // 当展示报告时，需要重新计算area.
        const onReportShow = () => {
            shape.indexCoordinate();
        };

        return (<>
            {shapeStatus.runStatus !== NODE_STATUS.DEFAULT &&
                <RunningStatusPanel shape={shape}
                                    shapeStatus={shapeStatus}
                                    ref={self.panelRef}
                                    onReportShow={onReportShow}/>}
            <div style={{
                position: "relative",
                background: shape.backColor,
                margin: BORDER_PADDING - CONNECTOR.RADIUS,
                borderRadius: shape.borderRadius
            }}>
                <DefaultRoot ref={self.rootRef}
                             shape={shape}
                             component={shape.getComponent()}
                             shapeStatus={shapeStatus}
                             borderPadding={BORDER_PADDING}/>
            </div>
        </>);
    };

    /**
     * 计算运行结果panel相对于elsa坐标系中的位置和大小
     *
     * @return {{x: *, width: number, y, height: number}|null} 位置frame信息.
     */
    self.getReportFrame = () => {
        if (!self.panelRef || !self.panelRef.current || !self.panelRef.current.getRunReportRect()) {
            return null;
        }
        const rect = self.panelRef.current.getRunReportRect();
        return {
            x: shape.x + rect.left,
            y: shape.y + rect.top,
            width: rect.width / shape.page.scaleX,
            height: rect.height / shape.page.scaleY
        }
    };

    /**
     * 存在report时，需要判断是否坐标在report dom中.
     *
     * @override
     */
    const containsBack = self.containsBack;
    self.containsBack = (x, y) => {
        const reportFrame = self.getReportFrame();
        if (!reportFrame) {
            return containsBack.apply(self, [x, y]);
        }
        return containsBack.apply(self, [x, y]) || isPointInRect({x, y}, reportFrame);
    };

    /**
     * 不绘制focusFrame.
     *
     * @overview
     */
    self.drawFocusFrame = () => {
    };

    /**
     * @override
     */
    const drawBorder = self.drawBorder;
    self.drawBorder = () => {
        drawBorder.apply(self);
        if (shape.isFocused || shape.emphasized) {
            self.parent.style.outline = shape.outlineWidth + "px" + " solid " + shape.outlineColor;
        } else {
            self.parent.style.outline = "";
        }
    };

    /**
     * 删除之前清理掉react相关的组件.
     *
     * @override
     */
    const beforeRemove = self.beforeRemove;
    self.beforeRemove = () => {
        beforeRemove.apply(self);
        self.unmountReact();
    };

    /**
     * 监听parent的变化，当发生变化时，需要修改图形的高度和宽度.
     */
    let prevHeight = 0;
    self.observe = () => {
        new ResizeObserver(() => {
            if (prevHeight === self.parent.offsetHeight) {
                return;
            }

            // 删除之后不需要修改图形大小.
            if (shape.container === "") {
                return;
            }

            // 重新渲染能选中的范围
            shape.resize(shape.width, self.parent.offsetHeight);
            prevHeight = self.parent.offsetHeight;
            self.panelRef?.current?.setHeight(shape.height);
            generateMarquees();
        }).observe(self.parent);
    };

    const generateMarquees = () => {
        self.marquees = [];
        const width = shape.width;
        const height = shape.height;
        const emphasizedOffset = shape.emphasizedOffset;

        // 上右.
        self.marquees.push(marquee(HORIZONTAL_RIGHT,
            (varX, ctl) => varX + (ctl.percent * width),
            (varY) => varY + emphasizedOffset));

        // 下左.
        self.marquees.push(marquee(HORIZONTAL_LEFT,
            (varX, ctl) => varX + width - (ctl.percent * width),
            (varY) => varY + height - emphasizedOffset));
        const count = Math.floor(height / WATER_DROP_DISTANCE) + 1;
        for (let i = 0; i < count; i++) {
            // 左上
            self.marquees.push(marquee(VERTICAL_UP,
                (varX) => varX + emphasizedOffset,
                (varY, ctl) => varY + (height / count * (count - i)) - (ctl.percent * height / count)));

            // 右下.
            self.marquees.push(marquee(VERTICAL_DOWN, (varX) => varX + width - emphasizedOffset,
                (varY, ctl) => varY + (height / count * i) + (ctl.percent * height / count)));
        }
    };

    /**
     * 绘制跑马灯流转效果.
     *
     * @override
     */
    self.drawDynamic = (context, x, y) => {
        self.marquees.forEach(wd => {
            if (wd.loaded) {
                wd.draw(context, x, y, shape)
            } else {
                wd.load();
            }
        })
    };

    /**
     * 有子类重写.
     */
    self.getHeaderIcon = () => {
    };

    /**
     * 本方法提供默认值-工具，具体可由子类重写.
     */
    self.getHeaderTypeIcon = () => {
        return (<>
            <Tooltip overlayClassName={'jade-node-custom-header-type-tooltip'} title={shape.graph.i18n?.t('tool') ?? 'tool'}>
                <div className={"jade-node-custom-header-type-icon-wrapper"}>
                    <ToolIcon className="jade-node-custom-header-type-icon"/>
                </div>
            </Tooltip>
        </>);
    };

    /**
     * 获取Header组件
     *
     * @param data 数据.
     * @param shapeStatus 图形状态集合.
     * @return {JSX.Element}
     */
    self.getHeaderComponent = (data, shapeStatus) => {
        return (<Header shape={shape} data={data} shapeStatus={shapeStatus}/>);
    }

    /**
     * 获取Footer组件
     *
     * @return {JSX.Element}
     */
    self.getFooterComponent = () => {
        return (<Footer shape={shape}/>);
    };

    /**
     * 默认的工具栏配置.
     *
     * @return {*} 数组.
     */
    self.getToolMenus = () => {
        return [{
            key: 'copy', label: 'copy', action: () => {
                shape.duplicate();
            },
        }, {
            key: 'delete', label: 'delete', action: () => {
                shape.remove();
            },
        }, {
            key: 'rename', label: 'rename', action: (setEdit) => {
                setEdit(true);
            },
        }, {
            key: 'advancedConfiguration', label: 'advancedConfiguration', action: (setAdvancedConfiguration) => {
                setAdvancedConfiguration(true);
            },
        }];
    };

    /**
     * 让animationCanvas始终处在最下层.
     *
     * @override
     */
    const drawFocus = self.drawFocus;
    self.drawFocus = (context) => {
        drawFocus.apply(self, [context]);
        self.animationCanvas.style.zIndex = -1;
    };

    return self;
};

const STEP = 0.009

/**
 * 跑马灯效果绘制器.
 *
 * @param rawSvg 原始svg字符串.
 * @param getX 获取x的值.
 * @param getY 获取y的值.
 * @return {{}} 对象.
 */
const marquee = (rawSvg, getX, getY) => {
    const self = {};
    self.img = null;
    self.control = {percent: 0, times: 0};

    /**
     * 绘制.
     *
     * @param context canvas2dContext对象.
     * @param x 横坐标.
     * @param y 纵坐标.
     * @param shape 图形对象.
     */
    self.draw = (context, x, y, shape) => {
      let xVal = x;
      let yVal = y;
        self.control.percent += STEP;
        if (self.control.percent >= 1) {
            self.control.times = 100;
            self.control.percent = 0;
            return;
        }
      xVal -= shape.width / 2;
      yVal -= shape.height / 2;
      xVal = getX(xVal, self.control);
      yVal = getY(yVal, self.control);
      context.drawImage(self.img, xVal, yVal);
    };

    /**
     * 加载svg为image对象.
     */
    self.load = () => {
        const svg = new Blob([rawSvg], {type: 'image/svg+xml;charset=utf-8'});
        const url = URL.createObjectURL(svg);
        const img = new Image();
        img.src = url;
        img.onload = function () {
            URL.revokeObjectURL(url);
            self.img = img;
            self.loaded = true;
        };
    };

    return self;
};