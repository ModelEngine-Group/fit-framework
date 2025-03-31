/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DIRECTION} from "@fit-elsa/elsa-core";
import {CONNECTOR} from "@/common/Consts.js";

/**
 * 重写方法放在这里.
 *
 * @param node 节点.
 */
export const overrideMethods = (node) => {
    /**
     * 设置方向为W和N的connector不支持拖出连接线
     *
     * @override
     */
    const initConnectors = node.initConnectors;
    node.initConnectors = () => {
        initConnectors.apply(node);
        node.connectors.remove(c => c.direction.key === DIRECTION.S.key || c.direction.key === DIRECTION.N.key
            || c.direction.key === "ROTATE");
        node.connectors.forEach(connector => {
            connector.radius = CONNECTOR.RADIUS;
            connector.isSolid = true;
            if (connector.direction.key === DIRECTION.W.key) {
                connector.allowFromLink = false;
            }
            if (connector.direction.key === DIRECTION.E.key) {
                connector.allowToLink = false;
            }
            const moving = connector.moving;
            connector.moving = (deltaX, deltaY, x, y) => {
                // 找到从当前锚点拖出去的线
                if (!node.page.canDragOut(node, connector)) {
                    return;
                }
                moving.apply(connector, [deltaX, deltaY, x, y]);
            }
        })
    };

    /**
     * @override
     */
    const load = node.load;
    node.load = (ignoreFilter) => {
        load.apply(node, [ignoreFilter]);
        /**
         * jadeNode高度变化不触发dirties.
         *
         */
        const propertyChanged = node.propertyChanged;
        node.propertyChanged = (property, value, preValue) => {
            if (property === "height") {
                return;
            }
            propertyChanged.apply(node, [property, value, preValue]);
        };
    };

    /**
     * 需要加上report的范围.
     *
     * @override
     */
    const getBound = node.getBound;
    node.getBound = () => {
        const bound = getBound.apply(node);
        const reportFrame = node.drawer.getReportFrame();
        if (!reportFrame) {
            return bound;
        }
        bound.x = Math.min(reportFrame.x, bound.x);
        bound.y = Math.min(reportFrame.y, bound.y);
        bound.width = Math.max(reportFrame.x + reportFrame.width, bound.x + bound.width) - Math.min(reportFrame.x, bound.x);
        bound.height = Math.max(reportFrame.y + reportFrame.height, node.y + node.height)
            - Math.min(node.y, reportFrame.y);
        return bound;
    };

    /**
     * 反序列化.
     *
     * @override
     */
    const deSerialized = node.deSerialized;
    node.deSerialized = () => {
        deSerialized.apply(node);
        const flowMeta = node.flowMeta;
        if (flowMeta.jober) {
            flowMeta.jober.converter.entity.enableStageDesc = flowMeta.enableStageDesc;
            flowMeta.jober.converter.entity.stageDesc = flowMeta.stageDesc;
        }
    };

    /**
     * 需要加上report的范围.
     *
     * @override
     */
    const getShapeFrame = node.getShapeFrame;
    node.getShapeFrame = (withMargin) => {
        const frame = getShapeFrame.apply(node, [withMargin]);
        const reportFrame = node.drawer.getReportFrame();
        if (!reportFrame) {
            return frame;
        }

        frame.x1 = Math.min(frame.x1, reportFrame.x);
        frame.x2 = Math.max(frame.x2, reportFrame.x + reportFrame.width);
        frame.y1 = Math.min(frame.y1, reportFrame.y);
        frame.y2 = Math.max(frame.y2, reportFrame.y + reportFrame.height);
        return frame;
    };

    /**
     * 当节点被取消选中时，校验表单中的数据.
     */
    const unSelect = node.unSelect;
    node.unSelect = () => {
        unSelect.apply(node);
        if (node.validateForm) {
            node.validateForm().then(node.offError).catch(node.onError);
        }
    };

    /**
     * 更新jadeConfig.
     *
     * @override
     */
    const serialize = node.serialize;
    node.serialize = () => {
        const jadeConfig = node.drawer.getLatestJadeConfig();
        jadeConfig && node.serializerJadeConfig(jadeConfig);
        return serialize.apply(node);
    };

    /**
     * @override
     */
    const remove = node.remove;
    node.remove = (source) => {
        // 如果有连线，需要同时删除连线.
        const events = node.page.getEvents()
            .filter(s => s.fromShape === node.id || s.toShape === node.id);
        const lineRemoved = events.flatMap(e => e.remove());

        // 删除图形本身.
        const removed = remove.apply(node, [source]);

        // 清理observables.
        node.cleanObservables();

        node.page.triggerEvent({
            type: 'DELETE_SHAPE',
            value: {
                shapeId: node.id,
            },
        });

        return [...removed, ...lineRemoved];
    };
};