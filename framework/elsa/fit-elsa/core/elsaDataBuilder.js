/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const proxy = () => {
    return new Proxy({}, {
        get: function (target, propKey, receiver) {
            let value = target[propKey];
            if (!value instanceof Function) {
                return value;
            }
            if (propKey.substr(0, 3) === "set") {// set property
                return v => {
                    let property = propKey.substr(3);
                    property = property.substr(0, 1).toLowerCase() + property.substr(1);
                    if (target[property] && typeof target[property] === "object") {
                        target[property] = Object.assign(target[property], v);
                    } else {
                        target[property] = v;
                    }
                }
            } else {
                return value;
            }
        }
    });
};
const graphBuilder = () => {
    return {
        new: (type, id) => {
            const self = proxy();
            self.type = type;
            self.id = id;
            self.pages = [];
            self.properties = {
                "setting":{
                    "borderColor":"#686868",
                    "backColor":"#F5F5F5",
                    "headColor":"steelblue",
                    "fontColor":"#000",
                    "captionfontColor":"whitesmoke",
                    "fontFace":"arial",
                    "captionfontFace":"arial black",
                    "fontSize":24,
                    "captionfontSize":14,
                    "fontStyle":"normal",
                    "captionfontStyle":"normal",
                    "fontWeight":"lighter",
                    "captionfontWeight":"lighter",
                    "hAlign":"center",
                    "vAlign":"center",
                    "captionhAlign":"middle",
                    "lineHeight":1.5,
                    "captionlineHeight":1,
                    "focusBorderColor":"#686868",
                    "focusFontColor":"steelblue",
                    "focusBackColor":"#F5F5F5",
                    "mouseInColor":"orange",
                    "mouseInBorderColor":"#686868",
                    "mouseInFontColor":"steelblue",
                    "mouseInBackColor":"#F5F5F5",
                    "borderWidth":1,
                    "globalAlpha":1,
                    "backAlpha":1,
                    "cornerRadius":0,
                    "dashWidth":0,
                    "autoText":false,
                    "autoHeight":false,
                    "autoWidth":false,
                    "margin":25,
                    "pad":10,
                    "code":"",
                    "rotateDegree":0,
                    "shadow":false,
                    "shadowData":"2px 2px 4px",
                    "outstanding":false,
                    "pDock":"none",
                    "dockMode":"none",
                    "priority":0,
                    "infoType":{
                        "next":"INFORMATION",
                        "name":"none"
                    },
                    "progressStatus":{
                        "next":"UNKNOWN",
                        "color":"gray",
                        "name":"NONE"
                    },
                    "progressPercent":0.65,
                    "showedProgress":false,
                    "itemPad":[
                        5,
                        5,
                        5,
                        5
                    ],
                    "itemScroll":{
                        "x":0,
                        "y":0
                    },
                    "scrollLock":{
                        "x":false,
                        "y":false
                    },
                    "resizeable":true,
                    "selectable":true,
                    "rotateAble":true,
                    "editable":true,
                    "moveable":true,
                    "dragable":true,
                    "visible":true,
                    "deletable":true,
                    "allowLink":true,
                    "shared":false,
                    "strikethrough":false,
                    "underline":false,
                    "numberedList":false,
                    "bulletedList":false,
                    "enableAnimation":false,
                    "enableSocial":true,
                    "emphasized":false,
                    "bulletSpeed":1,
                    "focusMargin":0
                }
            };

            const initPage = {
                graph: (id, page) => {
                    // 保存页面的frame id，方便新增图形时设置图形的默认container
                    p.frame = id;
                    page.shapes = [];
                    page.properties = {
                        "shapesAs": {},
                        "type":"page"
                    };
                },
                presentation: (id, page) => {
                    const frameId = uuid();
                    const vectorId = uuid();
                    page.frame = frameId;
                    page.shapes = [
                        {
                            "id":frameId,
                            "properties":{
                                "id": frameId,
                                "container":id,
                                "shared":false,
                                "keepOrigin":true,
                                "preInShared":"",
                                "itemSpace":5,
                                "selectable":false,
                                "referenceShape":"",
                                "deletable":false,
                                "hideText":true,
                                "type":"presentationFrame",
                                "referencePage":"",
                                "dockAlign":"top",
                                "pad":0,
                                "lockedBy":{},
                                "dragable":true,
                                "borderWidth":0,
                                "text":"maliya",
                                "autoFit":true,
                                "height":900,
                                "itemPad":[
                                    0,
                                    0,
                                    0,
                                    0
                                ],
                                "comments":[],
                                "visible":true,
                                "moveable":false,
                                "index":100,
                                "readOnly":true,
                                "referenceData":{},
                                "backColor":"white",
                                "width":1600,
                                "inShared":"",
                                "dashWidth":0,
                                "resizeable":true
                            }
                        },
                        {
                            "id":vectorId,
                            "properties":{
                                "id": vectorId,
                                "container":frameId,
                                "shared":false,
                                "itemSpace":5,
                                "selectable":true,
                                "deletable":true,
                                "hideText":true,
                                "type":"referenceVector",
                                "dockAlign":"top",
                                "lockedBy":{

                                },
                                "dragable":true,
                                "borderWidth":0,
                                "text":"reference",
                                "height":2.1,
                                "itemPad":[
                                    6,
                                    6,
                                    6,
                                    6
                                ],
                                "comments":[

                                ],
                                "visible":true,
                                "moveable":true,
                                "index":102,
                                "backColor":"RGBA(255,255,200,0)",
                                "ignorePageMode":true,
                                "x":0,
                                "width":2.1,
                                "y":0,
                                "resizeable":true
                            }
                        },
                    ];
                    page.properties = {
                        "shapesAs": {},
                        "type":"presentationPage"
                    };
                }
            };

            self.addPage = id => addPage(self, id)

            self.toData = () => {
                const data = {};
                toData(data, self);
                data.pages = [];
                self.pages.forEach(page => {
                    const pageData = {};
                    data.pages.push(pageData);
                    toData(pageData, page);
                    pageData.shapes = [];
                    page.shapes.forEach(shape => {
                        const shapeData = {};
                        pageData.shapes.push(shapeData);
                        toData(shapeData, shape);
                    })
                })
                return data;
            };

            const toData = (parentData, parent) => {
                for (let field in parent) {
                    if (parent[field] instanceof Function) {
                        continue;
                    }
                    parentData[field] = parent[field];
                }
            };

            const addPage = (graph, id) => {
                const p = proxy();
                p.id = id;
                p.graphId = graph.id;
                initPage[graph.type](id, p);

                p.addShape = (type, id, container) => {
                    return addShape(p, type, id, container);
                };
                graph.pages.push(p);
                return p;
            };

            const addShape = (page, type, id, container) => {
                const s = proxy();
                s.id = id;
                s.properties = {
                    id,
                    type,
                    container: container ? container : page.frame,
                    text: ""
                };

                page.shapes.push(s);
                return s;
            }
            return self;
        }
    };
};

let uuid = function (isLong) {
    if (isLong) {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            let r = Math.random() * 16 | 0;
          let v = (c === 'x') ? r : ((r & 0x3) | 0x8);
            return v.toString(16);
        });
    } else {
        let firstPart = (Math.random() * 46656) | 0;
        let secondPart = (Math.random() * 46656) | 0;
        firstPart = ("000" + firstPart.toString(36)).slice(-3);
        secondPart = ("000" + secondPart.toString(36)).slice(-3);
        return firstPart + secondPart;
    }
};

export { graphBuilder }