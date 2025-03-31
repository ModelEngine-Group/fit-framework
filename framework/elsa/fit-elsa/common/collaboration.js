/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {PAGE_MODE} from "./const.js";
import {sleep} from "./util.js";
import {ajax} from "./ajax.js";
import {ELSA} from "../core/elsaEntry.js";

/**
 * 协作服务客户端
 * 辉子 2022
 */
const collaboration = (graph, url) => {
    const COLLABORATION_STATUS = {RUNNING: "running", CLOSE: "close"};
    const self = {graph};
    self.connectMode = 'push';
    self.pullingSequence = 0;
    self.whiteTopics = []; // message的topic白名单.
    self.communicator = ((session, url) => {
        let webSocket = null;

        const com = {};
        com.send = async (args, callback) => {
            const result = await ajax.post(encodeURI(url + "/" + args.method), args, callback);
            const data = result.data;
            if (args.method === 'load_graph') {
                if (data) {
                    self.pullingSequence = data.sequence + 1;
                } else {
                    self.pullingSequence = 0;
                }
            }
            return data;
        }

        /**
         * when received websocket server publish
         */
        com.onMessage = message => {
            if (!self.whiteTopics.contains(t => t === message.topic)
                && (message.session !== self.graph.collaborationSession || message.from === self.graph.cookie)) {
                return;
            }
            self.graph.publish(message);
        };

        const checkConnection = async (com) => {
            while (com.status === COLLABORATION_STATUS.RUNNING) {
                await sleep(1000);
                self.invoke({method: "ping"}).then(users=>{
                    if (!users) {
                        return;
                    }
                    const pages = {};
                    const shapes = {};
                    users.forEach(user=>{
                        !pages[user.page] && (pages[user.page] = []);
                        pages[user.page].push({id: user.id, name: user.name});
                        if (!user.shape) {
                            return;
                        }

                        !shapes[user.shape] && (shapes[user.shape] = []);
                        shapes[user.shape].push({id: user.id, name: user.name});
                    });
                    graph.pages.forEach(p=>{
                        p.editBy = pages[p.id];
                        (graph.activePage) && (p.id === graph.activePage.id) && (graph.activePage.editBy = p.editBy);
                        if (!p.editBy) {
                            return;
                        }
                        p.shapes.forEach(s=>{
                            s.editBy = shapes[s.id];
                        })
                        if (graph.activePage && p.id === graph.activePage.id) {
                            graph.activePage.sm.getShapes().forEach(s => {
                                s.editBy = shapes[s.id];
                            })
                        }
                    })
                })
            }
        };

        function openWebsocket() {
            com.status = COLLABORATION_STATUS.RUNNING;
            let baseDomain = url.split("//")[1];

            // 当使用https协议时，websocket使用wss协议.
            const protocol = window.location.protocol === "https:" ? "wss" : "ws";
            webSocket = new WebSocket(protocol + "://" + baseDomain + "/elsaData?" + "session=" + self.graph.session.id + "&collaborationSession=" + self.graph.collaborationSession + "&cookie=" + self.graph.cookie);
            webSocket.onmessage = msg => {
                let message = JSON.parse(msg.data);
                com.onMessage(message);
            }
            checkConnection(com);
        }

        let reViewGraphCount = 6;

        function reViewGraph() {
            ELSA.viewGraph(graph.collaborationSession, graph.type, graph.div, () => {
                if (reViewGraphCount-- > 0) {
                    setTimeout(reViewGraph, (3 + Math.round(Math.random() * 10)) * 1000);
                }
            });
        }

        function httpPull() {
            if (self.mute) {
                return;
            }
            ajax.get(encodeURI(url + "/" + 'get_topics?' + 'session=' + self.graph.collaborationSession + '&sequence=' + self.pullingSequence), data => {
                const result = JSON.parse(data);
                if (result.code === 3000) {
                    com.close();
                    if (graph.getMode() === PAGE_MODE.VIEW) {
                        setTimeout(reViewGraph, (3 + Math.round(Math.random() * 10)) * 1000);
                    }
                    return;
                }
                const topics = result.data;

                topics && topics.forEach(topic => {
                    self.pullingSequence = topic.sequence + 1;
                    com.onMessage(topic);
                });
            });
        }

        function openHttpPulling() {
            self.pullingInterval = setInterval(httpPull, 1000);
        }

        function closeHttpPulling() {
            clearInterval(self.pullingInterval);
        }

        com.connect = () => {
            if (self.connectMode === 'push') {
                setTimeout(() => {
                    openWebsocket();
                });
            } else {
                openHttpPulling();
            }
        };

        com.close = () => {
            com.status = COLLABORATION_STATUS.CLOSE;
            if (self.connectMode === 'push') {
                webSocket && webSocket.close();
            } else {
                closeHttpPulling();
            }
        };

        return com;
    })(graph.collaborationSession, url);

    const TOPIC_MAP = {
        "new_page": "page_added",
        "new_shape": "shape_added",
        "change_shape_index": "shape_index_changed",
        "change_page_shape_data": "page_shape_data_changed",
        "publish_comment": "comment",
        "add_freeline_point": "add_freeline_point",
        "freeline_done": "freeline_done"
    }

    /**
     * 向协作服务器发送命令
     * @param {*} args：{command,collaborationSession,from,tenant,graphid,pageid,shapeid,value<T>}
     * 辉子 2022
     * "load_edit_graph":下载协同编辑数据；"register_edit_graph":注册协同编辑数据;"new_page":新增一页; “change_page_index“：变更page层级
     * ”new_shape“:新增shape; "change_page_shape_data":page里shape数据变更，不包括新增和删除;"insert_shape":删除后的undo;
     * "get_present_page_index":演示模式得到当前的page信息
     */
    self.invoke = async (args, callback) => {
        if (self.communicator.inMessaging) {
          return undefined;
        }
        if (args.mode === PAGE_MODE.DISPLAY) {
          return undefined;
        }
        if (args.page) {
            // handle local
            // 造一个message，本地更新
            const message = {
                topic: TOPIC_MAP[args.method],
                page: args.page,
                shape: args.shape,
                value: args.value,
                from: graph.session.id
            };
            message && graph.syncSubscribedPage(message, true);
        }

        if (self.mute) {
          return undefined;
        }
        args.session = graph.collaborationSession;
        args.from = graph.cookie;
        args.tenant = graph.tenant;
        args.graph = graph.id;
        args.fromSession = graph.session;
        if (args.mode === undefined) {
            args.mode = PAGE_MODE.CONFIGURATION;
        }
        if (self.tag !== undefined) {
            args.tag = self.tag;
            delete self.tag;
        }
        if (callback === undefined) {
            return await self.communicator.send(args);
        } else {
            self.communicator.send(args, callback);
          return undefined;
        }
    };

    self.mute = false;

    self.connect = () => self.communicator.connect();
    self.close = () => {
        self.communicator.close();
        self.graph = null;
    }
    self.getStatus = () => self.communicator.status;

    return self;
};

export {collaboration};
