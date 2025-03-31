/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {addCommand, layoutCommand, positionCommand} from "./commands.js";

/**
 * 用于对elsa进行修改.
 *
 * @param graph 画布对象.
 * @return {{}}
 */
export const elsaWriter = (graph) => {
    const self = {};

    /**
     * 添加页面.
     *
     * @param name 页面名称.
     * @param id 页面id.
     * @param targetDiv 目标dom元素.
     * @param index 页面的下标.
     * @param data 页面的数据.
     * @return {*}
     */
    self.addPage = ({name, id, targetDiv, index, data}) => {
        const page = graph.addPage(name, id, targetDiv, index, data);
        page.startAnimation();
        page.fillScreen();
        page.type === "presentationPage" && page.zoom(-0.02);
    };

    /**
     * 调整页面顺序.
     *
     * @param id 页面的id.
     * @param index 目标位置.
     */
    self.movePage = (id, index) => {
        graph.changePageIndex(graph.getPageIndex(id), index);
    };

    /**
     * 批量创建图形.
     *
     * @param shapes 图形数据集合.
     * @return {*[]|*} 创建出的图形集合.
     */
    self.newShapes = (shapes) => {
        if (!shapes || shapes.length <= 0) {
            return [];
        }
        const newShapes = shapes.map(shape => {
            if (!shape.type || !shape.properties) {
              return undefined;
            }
            const x = shape.properties.x || 0;
            const y = shape.properties.y || 0;
            const newShape = graph.activePage.createNew(shape.type, x, y, undefined, shape.properties);
            newShape.invalidateAlone();
            newShape.initConnectors();
            return newShape;
        });

        addCommand(graph.activePage, newShapes.map(s => {
            return {shape: s}
        }));

        return newShapes;
    };

    /**
     * 批量设置图形属性.
     *
     * @param shape 图形对象.
     * @param attributes 属性集合.
     */
    self.setShapeAttributes = (shape, attributes) => {
        if (!shape) {
            return;
        }
        const layoutCommandParam = {shape, ...attributes};
        layoutCommand(graph.activePage, [layoutCommandParam]).execute(graph.activePage);
    };

    /**
     * 设置图形属性.
     *
     * @param shape 图形对象.
     * @param key 属性键.
     * @param value 属性值.
     */
    self.setShapeAttribute = (shape, key, value) => {
        if (!shape) {
            return;
        }
        const layoutCommandParam = {shape};
        layoutCommandParam[key] = value;
        layoutCommand(graph.activePage, [layoutCommandParam]).execute(graph.activePage);
    };

    /**
     * 设置图形位置属性.
     *
     * @param shape 图形对象.
     * @param position 位置.
     */
    self.setShapePosition = (shape, position) => {
        if (!shape) {
            return;
        }

        if (position.x === shape.x && position.y === shape.y) {
            return;
        }

        const positionCommandParam = {shape, x:{}, y:{}, container:{}};
        positionCommandParam['x'].preValue = shape.x;
        positionCommandParam['x'].value = position.x;
        positionCommandParam['y'].preValue = shape.y;
        positionCommandParam['y'].value = position.y;
        positionCommand(graph.activePage, [positionCommandParam]).execute();
    };

    /**
     * 设置画布属性.
     *
     * @param key 画布属性键.
     * @param value 画布属性值.
     * @param isCoEditing 是否忽略协同.
     */
    self.setGraph = (key, value, isCoEditing = false) => {
        graph.setProperty(key, value, isCoEditing);
    };

    /**
     * 删除页面.
     *
     * @param pageIds 页面id集合.
     */
    self.deletePages = (pageIds) => {
        if (!pageIds || pageIds.length === 0) {
            return;
        }
        graph.deletePages(pageIds);
    };

    /**
     * 批量上移图形层级.
     *
     * @param shapes 图形集合.
     */
    self.moveUp = (shapes = []) => {
        graph.activePage.sm.updateShapes(writer => {
            writer.moveUp(shapes);
        }, true, true);
    };

    /**
     * 图形层级下移一层.
     *
     * @param shapes 图形列表.
     */
    self.moveDown = (shapes = []) => {
        graph.activePage.sm.updateShapes(writer => {
            writer.moveDown(shapes);
        }, true, true);
    };

    /**
     * 图形层级移动到最顶层.
     *
     * @param shapes 图形列表.
     */
    self.moveTop = (shapes = []) => {
        graph.activePage.sm.updateShapes(writer => {
            writer.moveTop(shapes);
        }, true, true);
    };

    /**
     * 图形层级移动到最底层.
     *
     * @param shapes 图形列表.
     */
    self.moveBottom = (shapes = []) => {
        graph.activePage.sm.updateShapes(writer => {
            writer.moveBottom(shapes);
        }, true, true);
    };

    /**
     * 创建动画.
     *
     * @param action in/out
     * @param method 执行的动画类型
     * @param trigger 触发方式
     * @param follow 跟随元素
     * @param shapes 需要创建动画的shape数组
     */
    self.createAnimations = (shapes, action, method, trigger, follow) => {
        graph.activePage.createAnimations(shapes, action, method, trigger, follow);
    };

    /**
     * 清除动画.
     *
     * @param shapes 需要清除动画的shape数组
     */
    self.clearAnimations = (shapes) => {
        graph.activePage.clearAnimations(shapes);
    };

    /**
     * 格式化图形文本.
     *
     * @param shape 图形对象.
     * @param key 格式化key.
     * @param value 格式化的值.
     */
    self.formatText = (shape, key, value) => {
        shape && shape.format(key, {value});
    };

    /**
     * 设置节点中实例数目统计。
     *
     * @param shape 图形对象。
     * @param num 实例数目。
     * @param status 实例状态。
     */
    self.setCurrentTask = (shape, num, status) => {
        if (shape) {
            switch (status) {
                case 'running':
                    shape.runningTask = num;
                    break;

                case 'warning':
                    shape.warningTask = num;
                    break;
                case 'completed':
                    shape.completedTask = num;
                    break;
                default:
                    break;
            }
        }
    }

    return self;
};