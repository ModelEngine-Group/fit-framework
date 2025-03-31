/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {EVENT_TYPE} from "../common/const.js";
import {uuid} from "../common/util.js";
import {reGenerateId} from "../common/elsaEntryUtil.js";
import {defaultGraph} from "./defaultGraph.js";
import {elsatoimage} from "../common/elsa2image.js";
import {defaultRepository} from "../repository/defaultRepository.js";
import {elsaWriter} from "./writer.js";
import {ENV_CONFIG} from "../config/envConfig.js";

/**
 * 第三方开发调用API返回简易graph对象
 * 辉子 2022
 * 辉子 2022
 */
const graphAgent = (graph, enableDebug, repo) => {
    const agent = {};
    agent.elsaToImage = elsatoimage();

    /**
     * 设置想要的插入的图形.当再次点击画布时，创建图形.
     *
     * @param shapeType 图形类型.
     * @param properties 图形属性.
     */
    agent.want = (shapeType, properties) => {
        graph.activePage.want(shapeType, properties);
    };

    /**
     * 获取画布中心点位置---暂未考虑画布的横纵偏移量
     * @returns {{x: number, y: number}}
     */
    agent.getCenterPoint = () => {
        const frame = graph.activePage.getFrame();
        return {
            x: frame ? frame.width / 2 : 0, y: frame ? frame.height / 2 : 0
        }
    };

    /**
     * 按传入的rate比例缩放画布.
     *
     * @param rate 传入的比例.
     */
    agent.zoom = (rate) => {
        graph.activePage.zoom(rate);
    };

    /**
     * 根据模板初始化画布
     * @param data
     * @returns {Promise<void>}
     */
    agent.initGraphWithTemplate = async (data) => {
        if (!data) {
            return;
        }
        // 重新生成所有id，并更新container、reference信息
        let cloneData = reGenerateId(graph, data);
        ELSA.convertGraphData(cloneData);
        graph.deSerialize(cloneData);
        await graph.collaboration.invoke({method: "register_graph", value: cloneData});
    }

    /**
     * 获取graph的序列化的数据.
     *
     * @return {*} graph序列化后的数据.
     */
    agent.getGraph = () => {
        return graph.serialize();
    }

    /**
     * 获取graph 模式
     *
     * @return * graph模式
     */
    agent.getMode = () => {
        return graph.getMode();
    }

    /**
     * 获取shapes的配置信息
     *
     * @param shapeIds shapeId
     */
    agent.getConfigurations = (shapeIds) => {
        const configs = shapeIds.map(shapeId => {
            if (shapeId === graph.activePage.id) {
                return graph.activePage.getConfigurations();
            }
            const shape = graph.activePage.sm.getShapeById(shapeId);
            return shape ? shape.getConfigurations() : [];
        })

        return configs.reduce((preArr, curArr) => {
            const curArrMap = new Map();
            curArr.forEach(item => curArrMap.set(item.field, item));
            for (const prevItem of preArr) {
                const nextItem = curArrMap.get(prevItem.field);
                if (!nextItem) {
                    continue;
                }

                // 聚合所有的getChangedData方法
                if (prevItem.getChangedData) {
                    const getChangedData = prevItem.getChangedData;
                    prevItem.getChangedData = (value) => {
                        const changedData = getChangedData.apply(prevItem, [value]);
                        nextItem.getChangedData(value).forEach(p => changedData.push(p));
                        return changedData;
                    }
                }

                // 聚合所有的getValue方法.若存在不相等的值，则返回空字符串，否则返回值.
                if (prevItem.getValue) {
                    const getValue = prevItem.getValue;
                    prevItem.getValue = () => {
                        const preValue = getValue.apply(prevItem);
                        const value = nextItem.getValue();
                        return value === preValue ? value : "";
                    }
                }
            }
            return preArr;
        });
    }

    /**
     * 获取field配置信息
     */
    agent.getConfigurationsByField = (shapes, field) => {
        const shapeIds = shapes.map(s => s.id);
        return agent.getConfigurations(shapeIds).find(config => config.field === field);
    }

    /**
     * 编辑page.
     * 1、若当前page为null，调用graph的edit方法创建一个page.
     * 2、若当前page不为null，直接调用currentPage的editPage方法.
     *
     * @param id page的唯一标识.
     * @param div dom对象.
     */
    agent.editPage = async (id, div) => {
        const activePage = graph.activePage;
        if (activePage === null || activePage === undefined || activePage.expired) {
            const page = await graph.edit(graph.getPageIndex(id), div, id);
            page.startAnimation();
            return;
        }

        // 如果id不存在，或当前page和传入的id一样，直接返回，不editPage.
        if (id === null || id === undefined || activePage.id === id) {
            return;
        }

        // 在翻页之前，先取消所有的选中图形.
        activePage.getFocusedShapes().forEach(s => s.unSelect());
        await activePage.take(graph.getPageDataById(id));
    };

    /**
     * 展示page.
     * 1、若page不存在，则创建一个.
     * 2、若page存在，直接调用page的displayPage方法.
     *
     * @param id page的唯一标识.
     * @param div dom对象.
     */
    agent.displayPage = async (id, div) => {
        graph.display(id, div);
    };

    /**
     * 演示graph.
     *
     * @param index page得index.
     */
    agent.present = async (index) => {
        let viewer = document.getElementById("present");
        if (viewer === null) {
            viewer = document.createElement("div");
            viewer.id = "present";
            viewer.style.background = "red";
            graph.activePage.div.parentNode.appendChild(viewer);
        } else {
            viewer.innerHTML = "";
        }
        const presentGraph = await ELSA.presentGraph(graph.id, graph.type, viewer, index, graph.serialize());
        const dirtied = presentGraph.dirtied;
        presentGraph.dirtied = (data, event) => {
            dirtied.call(presentGraph, data, event);
            if (event.action === "exit_present") {
                agent.dirtied(data, event);
            }
        }
    }
    agent.dirtied = () => {
    };

    const dirtied = graph.dirtied;
    graph.dirtied = (data, dirtyAction) => {
        dirtied.call(graph, data, dirtyAction);
        agent.dirtied(data, dirtyAction);
    }

    agent.openCollaboration = () => graph.openCollaboration();
    agent.closeCollaboration = () => graph.closeCollaboration();

    /**
     * 获取所有分页数据.
     *
     * @return {[]} page数据列表.
     */
    agent.getPages = () => {
        return graph.pages;
    }

    /**
     * 保存数据.
     *
     * @return {*} 保存graph的返回值.
     * @return {*} 保存graph的返回值.
     */
    agent.save = async () => {
        return await repo.saveGraph(graph.serialize());
    }

    agent.addFocusedShapeChangeListener = (shapesHandler) => {
        graph.addEventListener(EVENT_TYPE.FOCUSED_SHAPES_CHANGE, shapesHandler);
    }

    agent.addFocusedPageChangeListener = (pageHandler) => {
        graph.addEventListener(EVENT_TYPE.FOCUSED_PAGE_CHANGE, pageHandler);
    }

    /**
     * 当编辑器选区发生变化时触发该监听器.
     *
     * @param shapesHandler 发生变化时用户定义的处理器.
     */
    agent.addEditorSelectionChangeListener = (shapesHandler) => {
        graph.addEventListener(EVENT_TYPE.EDITOR_SELECTION_CHANGE, shapesHandler);
    }

    agent.addRegionListener = (shapeHandler) => {
        graph.addEventListener(EVENT_TYPE.REGION_CLICK, shapeHandler);
    }

    /**
     * 获取被选中的shape的数据.
     * 1、如果存在focusedShape，则返回focusedShape的数据
     * 2、否则，返回page的数据(page也是shape的一种).
     *
     * @return {*[]} 被选中shape的数据列表.
     */
    agent.getFocusedShapes = () => {
        return graph.activePage.getFocusedShapes();
    }

    /**
     * 获取frame.
     *
     * @return {null|*}
     */
    agent.getFrame = () => {
        if (graph.activePage && graph.activePage.getFrame().page) {
            return graph.activePage.getFrame();
        }
        return null;
    }

    /**
     * 获取graph属性的方法.
     *
     * @param property 属性名.
     * @returns {*} graph属性的值.
     */
    agent.getGraphProperty = (property) => {
        return graph[property] || graph.setting[property];
    }

    agent.getShapeAnimation = (shapeId) => {
        return graph.activePage.animations.find(item => item.shape === shapeId);
    }

    /**
     * 获取文本格式化的值.
     *
     * @param shape 图形对象.
     * @param key 格式化key.
     * @returns {*} 格式化的值.
     */
    agent.getFormatValue = (shape, key) => {
        if (!shape) {
            return false;
        }
        return shape.getFormatValue(key);
    }

    /**
     * 主动触发当前页面重绘
     * @param width，非必填项
     * @param height，非必填项
     */
    agent.reset = (width, height) => {
        const page = graph.activePage;
        if (width) {
            page.div.style.width = width + "px";
            page.width = width;
        }
        if (height) {
            page.div.style.height = height + "px";
            page.height = height;
        }
        page.fillScreen(true);
        page.reset();
    };

    /**
     * 所有对elsa的修改，都应该通过change接口来执行.
     *
     * @param operation 操作，应该是一个function.
     */
    let writer = null;
    agent.change = (operation) => {
        if (typeof operation !== "function") {
            throw new Error("operation must be a function.");
        }
        graph.change(() => {
            !writer && (writer = elsaWriter(graph));
            operation(writer);
        });
    }

    /**
     * 获取当前页面对象.
     *
     * @returns {null|*} 当前页面对象.
     */
    agent.getActivePage = () => {
        return graph.activePage;
    };

    agent.setGraphData = (data) => {
        const p = graph.activePage;
        graph.shapeCache.clear();
        graph.deSerialize(data);
        p.take(data.pages.find(s => s.id === p.id));
    }

    agent.id = graph.id;
    agent.title = graph.title;
    agent.type = graph.type;
    agent.session = graph.session;
    agent.tenant = graph.tenant;
    agent.updateTime = graph.updateTime;
    agent.createTime = graph.createTime;
    enableDebug && (agent._graph = graph);//for debug

    return agent;
};

/**
 * elsa应用起始点
 * 从ELSA开始载入graph插件，新建graph，开始图形绘制
 * 辉子 2021
 */
const ELSA = (() => {
    const self = {};
    const graphTypes = {};
    graphTypes.graph = (div, title) => defaultGraph(div, title);
    let repo = defaultRepository();
    let emptyGraph = async (graphType, div, session) => {
      let graphTypeVal = graphType;
      (graphTypeVal === undefined) && (graphTypeVal = 'graph');
      const g = graphTypes[graphTypeVal](div, '');
        const s = session ? session : await repo.getSession();
        g.login(s);
        g.dirtied = (data, pageid) => {
        };
        return g;
    };
    self.session = {name: "huizi", id: uuid()};
    self.enableDebug = true;

    /**
     * 载入用户定制的graph插件
     */
    self.import = async (address, definedGraphs) => {
        await import(/* webpackIgnore: true */ address).then(gs => {
            //没有指定载入的graph，默认载入所有export的变量
            if (definedGraphs === undefined) {
                for (let g in gs) {
                    graphTypes[g] = gs[g];
                }
            } else {
                definedGraphs.forEach(g => graphTypes[g] = gs[g]);
            }
        });
    };

    /**
     * 新建graph，存盘，并返回graph id.
     * 辉子 2022
     */
    self.newGraph = async (id, graphType, div) => {
        const g = await emptyGraph(graphType, div);
        g.collaboration.mute = true;
        (id !== undefined) && (g.id = id);
        await g.initialize();
        const data = g.serialize();
        g.collaborationSession = g.id;
        await g.collaboration.invoke({method: "register_graph", value: data});
        await repo.saveGraph(data);
        return graphAgent(g, self.enableDebug, repo);
    };

    self._mockEmptyGraph = mock => {
        emptyGraph = mock;
    };
    self._mockRepo = repository => {
        repo = repository;
    }

    /**
     * 开启编辑graph
     * 先到协同服务上查找数据
     * 没有数据再到repo查找数据，并将数据发送到协同服务
     * 辉子 2022
     */
    self.editGraph = async (graphId, graphType, div, initializer) => {
        const g = await emptyGraph(graphType, div);
        // g.collaboration.mute = true;
        await g.initialize();
        g.collaborationSession = g.id = graphId;//编辑状态下collaborationSession==graphid
        const gAgent = graphAgent(g, self.enableDebug, repo);

        let data;
        const sessionData = await g.collaboration.invoke({method: "load_graph", value: graphId});
        if (sessionData) {
            self.convertGraphData(sessionData);
            data = sessionData;
        } else {
            data = await repo.getGraph(graphId);
            if (data === undefined || data === null) {
                initializer && initializer(gAgent);
                data = g.serialize();
                await repo.saveGraph(data);
            }
            await g.collaboration.invoke({method: "register_graph", value: data});
        }

        if (data) {
            g.deSerialize(data);
        }
        g.openCollaboration();
        return gAgent;
    };

    self.data2Graph = async (graphType, div, graphId, version, tenantId) => {
        const g = await emptyGraph(graphType, div);
        await g.initialize();
        g.collaboration.mute = true;
        g.collaborationSession = g.id = graphId;
        const gAgent = graphAgent(g, self.enableDebug, repo);
        let data;
        data = await repo.getGraph(graphId, version, tenantId);
        if (data === undefined || data === null) {
            data = g.serialize();
            await repo.saveGraph(data);
        }
        if (data) {
            g.deSerialize(data);
        }
        return gAgent;
    };

    self.editOffice = async (fileName, div) => {
        const g = await emptyGraph("graph", div);
        await g.initialize();
        g.collaborationSession = g.id = fileName;
        const sessionData = await g.collaboration.invoke({method: "register_office", value: fileName});
        self.convertGraphData(sessionData);
        g.deSerialize(sessionData);
        const gAgent = graphAgent(g, self.enableDebug, repo);
        return gAgent;
    };

    self.convertGraphData = sessionData => {
        const setProperties = source => {
            for (let f in source.properties) {
                (!source[f]) && (source[f] = source.properties[f]);
            }
            delete source.properties;
        };

        setProperties(sessionData);
        sessionData.pages.forEach(page => {
            setProperties(page);
            page.shapes.forEach(shape => {
                setProperties(shape);
            })
            page.shapes = page.shapes.orderBy(s => s.index);
        })
        sessionData.pages = sessionData.pages.orderBy(p => p.index);
    }

    /**
     * 预览模式直接从数据库里拿到graph数据
     * 辉子 2022
     */
    self.displayGraph = async (graphId, graphType, div) => {
        const g = await emptyGraph(graphType, div);
        await g.initialize();
        const data = repo.getGraph(graphId);
        g.deSerialize(data)
        return g;
    };

    /**
     * 直接演示某graph，这种演示模式不能进入config模式
     * 辉子 2022
     */
    self.presentGraph = async (graphId, graphType, div, index = 0, data) => {
      let dataVal = data;
        const g = await emptyGraph(graphType, div);
        await g.initialize();
      (dataVal === undefined) && (dataVal = await repo.getGraph(graphId));
      g.deSerialize(dataVal);
        g.collaborationSession = g.id + "_present";
        await g.present(index);
        await g.collaboration.invoke({method: "register_graph", value: g.serialize()});
        g.openCollaboration();
        return g;
    };

    self.viewGraph = async (session, graphType, div, errorCallback) => {
        const collaborationSession = session;
        //协同服务器取得协同数据
        const g = await emptyGraph(graphType, div);
        await g.initialize();
        const data = await g.collaboration.invoke({method: "load_graph", value: collaborationSession});
        if (data === undefined || data === null) {
            if (errorCallback) {
                errorCallback({code: "-1", message: "data is null"})
            }
          return undefined;
        }
        self.convertGraphData(data);
        g.deSerialize(data);
        g.collaborationSession = collaborationSession;
        g.viewPresent();
        g.openCollaboration();
        return g;
    };

    // 默认是将当前画布中选中的图形存为模板
    self.saveAsTemplate = ({name, type, description, icon}) => {
        // 先做数据转换等等序列化
        // 这一层支持第三方复写
        // tunnel.save();
    }

    // 获取所有模板
    // return: [{id, name, type, description, icon}]
    self.getTemplates = (filter) => {
        // 先做数据转换等等序列化
        // 这一层支持第三方复写
        //tunnel.query(filter);
    }

    // 使用模板，用户选用模板后，系统将模板内容创建到画布中
    self.loadTemplate = (id, x, y) => {

    }

    self.createEmptyGraph = async (graphType, div, session) => {
        const g = await emptyGraph(graphType, div, session);
        await g.initialize();
        g.collaboration.mute = true;
        return graphAgent(g, self.enableDebug, repo);
    };

    self.setRepo = (repository) => {
        repo = repository;
        // 把repo所有的配置复制给ENV_CONFIG，包括了方法
        Object.keys(repo).forEach(k => ENV_CONFIG[k] = repo[k]);
    }

    self.getRepo = () => {
        return repo;
    }

    return self;
})();

export {ELSA}
