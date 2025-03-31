/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {EVENT_TYPE, PAGE_MODE} from "../../common/const.js";
import {BASE_EVENT_LENGTH} from "./common/const.js";
import {aippFlowGraph} from "./aippFlowGraph.js";

/**
 * Aipp流程代码，对外暴露接口，以便对流程进行操作以及获取数据.
 */
const aippFlowAgent = (graph) => {
    const self = {};
    self.graph = graph;
    self.graph.eventAcceptPreHandler = (event) => {
        if (event.type !== EVENT_TYPE.FOCUSED_SHAPE_CHANGE) {
            return true;
        }
        let shapes = event.value;
        shapes = shapes.filter(s => !s.isTypeof("aippEvent"));
        if (shapes.length === 0) {
            return false;
        }
        event.value = shapes;
        return true;
    }

    self.graph.eventFirePreHandler = (event) => {
        if (event.type !== EVENT_TYPE.FOCUSED_SHAPES_CHANGE) {
            return true;
        }
        event.value = event.value.filter(s => !s.isTypeof("aippEvent"));
        return true;
    }

    /**
     * 序列化数据.
     *
     * @returns {*} 流程的全量序列化数据.
     */
    self.serialize = () => {
        return graph.serialize();
    };

    /**
     * 加载数据.
     *
     * @param flowConfigData 流程数据.
     * @returns {Promise<void>} 异步任务.
     */
    self.loadFlowFromGraph = async (flowConfigData) => {
        graph.activePage.clear();
        graph.deSerialize(flowConfigData);
        graph.activePage.deSerialize(flowConfigData.pages[0]);
        graph.activePage.shapes.filter(s => s.type === 'aippEvent').map(s => s.enableNodeInsertion());
        utils.centerFlow(graph.div.clientWidth / 2, graph.activePage.shapes);
        utils.initAgentStatus(graph.activePage.shapes);
        graph.activePage.reset();
    };

    /**
     * 添加 focusedShapes 的监听器.
     *
     * @param handler 事件处理器.
     */
    self.addFocusedShapeChangeListener = (handler) => {
        graph.addEventListener(EVENT_TYPE.FOCUSED_SHAPES_CHANGE, handler);
    };

    /**
     * 移除 focusedShapes 的监听器.
     *
     * @param handler 监听处理器.
     */
    self.removeFocusedShapeChangeListener = (handler) => {
        graph.removeEventListener(EVENT_TYPE.FOCUSED_SHAPES_CHANGE, handler);
    };

    /**
     * 添加"+"插入按钮的监听器.
     *
     * @param handler 事件处理器. handler会获得一个回调函数insertNodeCallback，用户可以调用这个函数，并传入将想插入的节点的JSON数据
     * node的内容格式如下：
     * {
     *    type: state,
     *    name: "<节点名称>",
     *    icon: "<节点图标URL>",
     *    description: "<节点描述文字>",
     *    jober: {
     *        ...
     *    },
     * }
     *
     */
    self.addInsertButtonListener = (handler) => {
        graph.addEventListener(EVENT_TYPE.INSERT_NODE_REGION_CLICKED, (event) => {
            handler((nodes) => event.region.insertNodes(Array.isArray(nodes) ? nodes : [nodes]));
        });
    };

    /**
     * 添加类型改变的监听器
     *
     * @param handler 事件处理器. handler会获得一个回调函数updateNodeCallback，用户可以调用这个函数，并传入将想改变属性的节点的JSON数据
     * node的内容格式如下：
     * {
     *    type: state,
     *    data: {
     *        name: "<节点名称>",
     *        icon: "<节点图标URL>",
     *        description: "<节点描述文字>",
     *        jober: {
     *            ...
     *        },
     *    }
     * }
     *
     */
    self.addTypeChangeListener = (handler) => {
        graph.addEventListener(EVENT_TYPE.FLOWABLE_STATE_TYPE_CHANGE, (event) => {
            handler((node) => event[0].setFlowableContext(node).recenter());
        });
    };

    return self;
};

/*
 * 工具类
 */
const utils = (() => {
    return {
        /*
         * 使整个流程以centerX为对称轴
         */
        centerFlow: (centerX, shapes) => {
            shapes.filter(shape => shape.type !== 'aippEvent')
                .forEach(shape => shape.x = centerX - shape.width / 2);
        }, initAgentStatus: (shapes) => {
            shapes.filter(shape => shape.isAgent)
                .forEach(shape => shape.agentRegion.visible = true);
        }
    }
})();

/**
 * Aipp流程对外接口.
 */
export const AippFlow = (() => {
    const self = {};

    /**
     * 新建Aipp流程.
     *
     * @param div 待渲染的dom元素.
     */
    self.new = async (div) => {
        const g = aippFlowGraph(div, "AippFlow");
        g.collaboration.mute = true;
        await g.initialize();
        const page = g.addPage("newFlowPage");

        const start = page.createNew("aippStart", 100, 100, undefined, page.wantedShape.getProperties());
        const end = page.createNew("aippEnd", 100, 100 + BASE_EVENT_LENGTH + start.height, undefined, page.wantedShape.getProperties());

        // const ev = page.createNew("aippEvent", 0, 0);
        // ev.enableNodeInsertion().connectFrom(start.id, "S").connectTo(end.id, "N");
        utils.centerFlow(500, g.activePage.shapes);

        g.exceptionFitables = ["modelengine.fit.jober.aipp.fitable.AippFlowExceptionHandler"];

        page.reset();
        page.fillScreen();
        return aippFlowAgent(g);
    };

    /**
     * 展示流程.
     *
     * @param div 待渲染的dom元素.
     * @param flowConfigData 流程元数据.
     */
    self.run = async (div, flowConfigData) => {
        const g = aippFlowGraph(div, "AippFlow");
        g.collaboration.mute = true;
        await g.initialize();
        g.addPage("newFlowPage", undefined, undefined, null, null, PAGE_MODE.RUNTIME);
        g.deSerialize(flowConfigData);
        g.activePage.deSerialize(flowConfigData.pages[0]);
        utils.centerFlow(g.div.clientWidth / 2, g.activePage.shapes);
        utils.initAgentStatus(g.activePage.shapes);
        g.activePage.reset();
        return aippFlowAgent(g);
    };

    return self;
})();