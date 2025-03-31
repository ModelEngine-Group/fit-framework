/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {uuid} from "./util.js";

const CHECK_MESSAGE_INTERVAL = 2000;
const disable = false;

/**
 * 与服务端通讯
 * 辉子 2021
 */
const communication = (graphId, sessionId, tabToken, baseUrl, baseDomain) => {
    if (disable) {
        return {
            invoke: method => {
            }, ping: () => {
            }, subscribe: page => {
            }, unSubscribe: page => {
            }, reconnectWebSocket: () => {
            }, checkMessages: page => {
            }
        }
    }
    let session = sessionId;
    const baseUrlVal = baseUrl === undefined ? 'http://127.0.0.1:8080' : baseUrl;
    const subscriptions = [];
    let self = {};
    const connector = ";";

    let Ajax = {
        get: function (url, callback) {
            const result = {};
            result.then = f => result.callback = f;
            // XMLHttpRequest对象用于在后台与服务器交换数据
            let xhr = new XMLHttpRequest();
            xhr.open('GET', url, true);
            xhr.onreadystatechange = function () {
                // readyState == 4说明请求已完成
                if (xhr.readyState === 4 && xhr.status === 200 || xhr.status === 304) {
                    // 从服务器获得数据
                    if (callback) {
                        callback.call(this, xhr.responseText)
                    } else {
                        result.callback && result.callback.call(this, xhr.responseText);
                    }
                }
            };
            xhr.send();
            if (callback) {
              return undefined;
            } else {
                return result;
            }
        }, // datat应为'a=a1&b=b1'这种字符串格式，在jq里如果data为对象会自动将对象转成这种字符串格式
        post: function (url, data, callback) {
            const result = {};
            result.then = f => result.callback = f;
            let xhr = new XMLHttpRequest();
            xhr.open("POST", url, true);
            // 添加http头，发送信息至服务器时内容编码类型
            xhr.setRequestHeader("Content-type", "application/json;charset-UTF-8");
            // xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4 && (xhr.status === 200 || xhr.status === 304)) {
                    if (callback) {
                        callback.call(this, xhr.responseText)
                    } else {
                        result.callback && result.callback.call(this, xhr.responseText);
                    }
                }
            };
            xhr.send(data);
            if (callback) {
              return undefined;
            } else {
                return result;
            }
        }
    };

    const ignoreStatus = [1000, 3010];
    let lastIndex = -1;
    self.lastPingTime = -1;
    self.webSocket = initWebsocket();

    function initWebsocket() {
        const baseDomainVal = baseDomain === undefined ? baseUrlVal.split('://')[1] : baseDomain;
        let socketId = uuid();
      let webSocket = new WebSocket(`ws://${baseDomainVal}/message/pageDataChange?session=${session}&token=${tabToken}&socketId=${socketId}&index=${lastIndex + 1}`);
        webSocket.onmessage = function (msg) {
            if (msg.data === 'ping') {
                self.lastPingTime = new Date().getTime();
                return;
            }
            let message = JSON.parse(msg.data);
            lastIndex = message.index;
            const subscription = subscriptions.find(s => s.id === message.page);
            subscription && subscription.publish(message);

            // const messages = JSON.parse(xx.data);
            // messages.forEach(msg => {
            //
            // })
        }
        webSocket.onclose = function (event) {
            self.lastPingTime = -1;
            if (ignoreStatus.findIndex(s => s === event.code) === -1) {
                self.webSocket = initWebsocket();
            }
        }
        webSocket.onerror = function (event) {
            // 没关系，继续，不影响其他错误信息的处理.
        }

        webSocket.onopen = function (event) {
            self.lastPingTime = new Date().getTime();
            if (self.holding && self.holding.length > 0) {
                sendPageData(self.holding);
                self.holding = [];
            }
        }
        return webSocket;
    }

    function sendPageData(args) {
        args.forEach(arg => {
            self.webSocket.send(JSON.stringify(arg));
        })

        /**
         * todo proton的先单独处理，后续优化掉
         *
         * @param arg
         * @param data
         * @returns {string}
         */
        function handleProton(arg, data) {
            let direction = '';
            if (arg.value.tag) {
                direction = arg.value.tag.direction;
                if (typeof (direction) === 'string') {
                    direction = parseFloat(direction);
                }
                direction = direction.toFixed(2);
            }
            let visible = arg.value.visible;
            if (visible === undefined) {
                visible = '';
            }
          let dataVal = `${data}${direction}#${arg.value.kissed ? 1 : 0}#${visible}`;
            if (arg.value.x) {
              dataVal = `${dataVal}#${arg.value.x}#${arg.value.y}`;
            }
            return dataVal;
        }
    }

    let methods = function () {
        let result = {};
        //{ page: pageid, shape: shapeid, skipSelf: dont publish myself, value: {type:line,x:0,y:0,pid:elsa-page} }
        result["publish_page_data"] = args => {
            if (self.webSocket.readyState !== self.webSocket.OPEN) {
                if (!self.holding) {
                    self.holding = [];
                }
                args.forEach(arg => self.holding.push(arg));
                return;
            }
            // let data = [];
            sendPageData(args);
        };
        // result["general_command"] = args => {
        //     Ajax.post(baseUrl + "/message/command", JSON.stringify(args), data => {
        //         //console.log("sent instant message: " + data);
        //     });
        // };

        result["publish_comment"] = args => {
          Ajax.post(`${baseUrlVal}/message/comment`, JSON.stringify(args), data => {
                // 没关系，继续.
            });
        };

        result["appreciate"] = args => {
          Ajax.post(`${baseUrlVal}/message/appreciation`, JSON.stringify(args), data => {
                // 没关系，继续.
            });
        };

        result["enter_next"] = args => {
          Ajax.post(`${baseUrlVal}/message/nextPage`, JSON.stringify(args), data => {
                // 没关系，继续.
            });
        };

        result["enter_previous"] = args => {
          Ajax.post(`${baseUrlVal}/message/previousPage`, JSON.stringify(args), data => {
                // 没关系，继续.
            });

        };
        //{page: "", shape: "", value: graph.serialize}
        result["register_graph"] = args => {
          Ajax.post(`${baseUrlVal}/message/graphRegister`, JSON.stringify(args), data => {
                // 没关系，继续.
            });
        };

        result["check_message"] = (graph, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/list?graph=${graph}&session=${session}&page=${pageId}`, data => {
                callback(JSON.parse(data).data);
            })
        };

        result["get_present_page_index"] = (graph, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/position?graph=${graph}&session=${session}`, data => {
                callback(JSON.parse(data).data);
            })
        };

        result["load_graph"] = (graph, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/graph?graph=${graph}`, data => {
                callback(decodeURIComponent(escape(window.atob(JSON.parse(data).data.substring(9)))));
            })
        };

        result["load_comments"] = (graph, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/comments?graph=${graph}&page=${pageId}`, data => {
                callback(JSON.parse(data).data);
            })
        };
        result["load_pros"] = (graph, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/pros?graph=${graph}&page=${pageId}`, data => {
                callback(JSON.parse(data).data);
            })
        };
        result["load_cons"] = (graph, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/cons?graph=${graph}&page=${pageId}`, data => {
                callback(JSON.parse(data).data);
            })
        };

        result["publish_prize"] = args => {
          Ajax.post(`${baseUrlVal}/message/publish_prize`, JSON.stringify(args), data => {
                // 没关系，继续.
            });
        };
        result["get_employees"] = (prize, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/get_employees?prize=${prize}`, data => {
                callback(JSON.parse(data).data);
            })
        };
        result["get_backup_candidates"] = (pageId, callback) => {
          Ajax.get(`${baseUrlVal}/message/getBackupCandidates`, data => {
                callback(JSON.parse(data).data);
            })
        };
        result["submit_workId"] = (wordId, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/game/submitWorkId?id=${wordId}`, data => {
                callback(JSON.parse(data));
            })
        };
        result["check_login"] = (wordId, token, pageId, callback) => {
          Ajax.get(`${baseUrlVal}/game/checkLogin?id=${wordId}&token=${token}`, data => {
                callback(JSON.parse(data));
            })
        };
        result["start_game"] = (sessionId) => {
          Ajax.get(`${baseUrlVal}/game/reset?id=${sessionId}`, data => {
                // 没关系，继续.
            })
        };
        return result;
    }();
    return {
        /**
         * 目前可枚举的方法：publish_comment, appreciate, enter_next, enter_previous,get_present_page_index,check_messages
         * 辉子 2021
         */
        invoke: async (method, args, pageId, callback) => {
            if (typeof (args) === "object") {
                if (Array.isArray(args)) {
                    args.forEach(arg => {
                        (!arg.fromSession) && (arg.fromSession = session);
                        // arg.graph = graph.id;
                        arg.graph = graphId;
                    })
                } else {
                    (!args.fromSession) && (args.fromSession = session);
                    // args.graph = graph.id;
                    args.graph = graphId;
                }
            }
            try {
                methods[method](args, pageId, callback);
            } catch (e) {
                // 没关系，继续，不影响其他错误信息的处理.
            }
        },

        /**
         * 目前可枚举的topic: comment, appreciation,next_page, previous_page
         */
        subscribe: (page) => {
            subscriptions.push(page);
        },

        unSubscribe: page => {
            subscriptions.remove(s => s.id === page.id);
        },

        lastCheckTime: new Date(),

        checkMessages: function (page) {
            let self = this;
            let timediff = (new Date().getTime()) - self.lastCheckTime.getTime();
            if (timediff < CHECK_MESSAGE_INTERVAL) {
                return;
            }

            self.invoke("check_message", graphId, page.id, messages => {
                messages.forEach(message => {
                    const subscription = subscriptions.find(s => s.id === page.id);
                    subscription && subscription.publish(message);
                })
                self.lastCheckTime = new Date();
            });
        },

        ping: function () {
            if (self.lastPingTime !== -1 && new Date().getTime() - self.lastPingTime > 5000) {
                this.reconnectWebSocket();
            }
        },

        reconnectWebSocket: () => {
            self.webSocket.close(3333, 'reconnect');
        }
    };

};

export {communication};