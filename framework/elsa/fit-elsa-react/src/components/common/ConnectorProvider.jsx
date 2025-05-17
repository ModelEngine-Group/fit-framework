/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useShapeContext} from "@/components/DefaultRoot.jsx";
import {useEffect, useRef} from "react";
import {connector, DIRECTION} from "@fit-elsa/elsa-core";
import {CONNECTOR} from "@/common/Consts.js";
import PropTypes from "prop-types";

/**
 * 连接点提供器.
 *
 * @param name 名称，用于区分不同的connector.
 * @param children 子组件.
 * @return {JSX.Element}
 * @constructor
 */
export const ConnectorProvider = ({name, children}) => {
    const shape = useShapeContext();
    const divRef = useRef();
    const prevNameRef = useRef(name);
    const directionRef = useRef({
        cursor: "crosshair",
        key: name,
        color: "white",
        ax: "x",
        vector: 1,
        value: DIRECTION.E,
    });

    // 初始化之后，创建connector.
    useEffect(() => {
        const getX = () => {
            const dom = divRef.current;
            const shapeDiv = shape.drawer.parent;
            const rect = dom.getBoundingClientRect();
            const shapeRect = shapeDiv.getBoundingClientRect();
            return (rect.right - shapeRect.left + CONNECTOR.CONDITION_RADIUS) / shape.page.scaleX;
        };

        const getY = () => {
            const dom = divRef.current;
            const shapeDiv = shape.drawer.parent;
            const rect = dom.getBoundingClientRect();
            const shapeRect = shapeDiv.getBoundingClientRect();
            return (rect.top - shapeRect.top + (rect.bottom - rect.top) / 2) / shape.page.scaleY;
        };

        const c = connector(shape, getX, getY, () => directionRef.current,
          s => s.visible,
          () => true,
          () => true,
          () => {},
          () => false,
        );
        c.type = 'dynamic';
        shape.activeConnector(c);
        c.isSolid = true;
        c.allowToLink = false;
        c.allowFromLink = true;
        c.radius = CONNECTOR.CONDITION_RADIUS;
        const moving = c.moving;
        c.moving = (deltaX, deltaY, x, y) => {
            if (!shape.page.canDragOut(shape, c)) {
                return;
            }
            moving.apply(connector, [deltaX, deltaY, x, y]);
        };
        refreshLine();

        return () => {
            // 组件卸载时，删除connector.
            shape.connectors = shape.connectors.filter(c => c.getDirection().key !== name);

            // 组件卸载时，删除connector的同时，删除线.
            deleteLine();
        };
    }, []);

    // 当name变化时更新相关线条的definedFromConnector
    useEffect(() => {
        if (prevNameRef.current !== name) {
            directionRef.current.key = name;

            const lines = shape.page.sm.getShapes(s => s.fromShape === shape.id || s.toShape === shape.id)
              .filter(l => l.definedFromConnector === prevNameRef.current);
            lines.forEach(l => {
                l.definedFromConnector = name;
                l.follow();
            });

            prevNameRef.current = name; // 更新前一个name值
        }
    }, [name]);

    const deleteLine = () => {
        const lines = shape.page.sm.getShapes(s => s.fromShape === shape.id || s.toShape === shape.id)
            .filter(l => l.definedFromConnector === name);
        lines.forEach(l => l.remove());
    };

    const refreshLine = () => {
        const lines = shape.page.sm.getShapes(s => s.fromShape === shape.id || s.toShape === shape.id)
          .filter(l => l.definedFromConnector === name);
        lines.forEach(l => l.follow());
    };

    return (<>
        <div id={"connector-provider-" + name} ref={divRef}>
            {children}
        </div>
    </>);
};

ConnectorProvider.propTypes = {
    name: PropTypes.string.isRequired,
    children: PropTypes.array.isRequired,
};
