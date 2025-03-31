/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {container} from './container.js';
import {containerDrawer} from './drawers/containerDrawer.js';
import {deepClone} from "../common/util.js";

/**
 * 引用
 * 引用是一个容器，可以引用“其他graph/page”里的任意图形，比如我可以引用你在ppt里画的一幅图，在我的ppt里显示
 * 引用的图形没有数据，所以每次显示都会得到原始图形的最新改动
 * 暂时不考虑版本问题，如果只想得到某个snapshot，那么就不要引用，选择拷贝粘贴
 */
const reference = (id, x, y, width, height, parent, drawer = containerDrawer) => {
    let self = container(id, x, y, width, height, parent, drawer);// drawer === undefined ? containerDrawer : drawer);
    self.type = "reference";
    self.dashWidth = 5;
    self.width = 400;
    self.height = 300;
    self.backColor = "rgba(0,0,0,0)";
    self.hideText = true;
    self.pad = 0;
    self.itemPad = [0, 0, 0, 0];
    self.autoFit = true;
    self.keepOrigin = true;
    self.readOnly = true;
    self.uniqRef = true; // 是否是该页唯一对某shape的引用，如果是，引用shape的id号就不用改变

    /**
     * 引用的shape id
     */
    self.referenceShape = "";
    /**
     * 引用的页面id
     */
    self.referencePage = "";
    /**
     * 自定义改变引用shape的属性存储
     */
    self.referenceData = {};

    /**
     * 重载复制的shape的一些行为
     * 当复制元素的属性变化时，
     * @param shape
     */
    let savePolyData = shape => {
        // if (self.readOnly) {
        //     return;
        // }
        shape.addDetection(shape.serializedFields, (property, value, preValue) => {
            if (shape.readOnly || value === preValue) {
                return;
            }
            if (self.referenceData[shape.id] === undefined) {
                self.referenceData[shape.id] = {};
            }
            self.referenceData[shape.id][property] = value;
        });
        shape.moved = () => {
            if (self.referenceData[shape.id] === undefined) {
                self.referenceData[shape.id] = {};
            }
            self.referenceData[shape.id].x = shape.x;
            self.referenceData[shape.id].y = shape.y;
        };
        shape.clearPoly = () => {
            if (self.referenceData[shape.id] !== undefined) {
                self.referenceData[shape.id] = undefined;
            }
        };
    };

    /**
     * 使用多态数据覆写拷贝的图形数据
     * 以实现在被引用的场景具备不同的属性和行为
     *
     * @param shape
     */
    let restorePolyData = shape => {
        if (shape.readOnly) {
            return;
        }
        let properties = self.referenceData[shape.id];
        if (properties === undefined) {
            return;
        }
        let value = self.page.disableReact;
        self.page.disableReact = true;
        for (let f in properties) {
            shape[f] = properties[f];
        }
        self.page.disableReact = value;
        shape.invalidate();
    };

    self.referenced = shape => {
        savePolyData(shape);
        restorePolyData(shape);
    };

    /**
     * 查找类型为referenceVector的子shape
     * @returns {*}
     */
    self.getVector = () => self.getShapes().find(s => s.isType('referenceVector'));

    self.childAllowed = s => s.serializable;

    self.filterRefered = shapesData=>shapesData;
    self.checkReferedShape = shape=> true;
    /**
     * 设置引用内容
     * 通常在指定引用的page和shape时执行
     */
    self.refer = () => {
        let data = self.loadReferenceData();
        if (!data || data.shapes.length === 0) {
            return;
        }

        let vector = self.getVector();
        if (!vector) {
            return;
        }

        vector.getShapes().forEach(s => s.remove());
        const shapes = self.filterRefered(data.shapes);
        let minX;
        let minY;
        shapes.forEach(s => {
            if (s.id === self.id || s.id === vector.id) {
                return;
            }

            const clonedS = deepClone(s);

            /*
             * 由于后端存储数据库中默认shapeId是唯一的，如果不重新生成id，会导致模板page中和普通page中的图形id重复，导致在保存到后端
             * 存储服务器时，图形的数据被覆盖.所以这里暂时新生成一个uuid.
             */
            clonedS.id = s.isPlaceHolder ? self.graph.uuid() : s.id;
            let s1 = self.page.createShape(clonedS.type, clonedS.x, clonedS.y, clonedS.id);
            const index = s1.index;

            /*
             * 这里有2个场景需要clone数据.
             * 1、当图形的isPlaceHolder有值时，此时图形不能引用模板中的_data，否则会导致数据错乱.因为所有页面的图形引用的是模板中的同一个_data.
             * 2、当s是frame时，需要修改其container，此时，若不clone，则会导致模板中数据的container改变，创建新页面时，数据会错乱.
             *    具体，可查看step1处代码.
             */
            s1.deSerialize(clonedS);

            // @maliya 先将引用模板的图形设定为不可序列化，避免部分属性发生修改 向协同服务器发送不必要的变更
            s1.serializable = false;
            s1.index = index;
            s1.referenceId = s.id;
            s1.invalidate();

            if (!self.checkReferedShape(s1)) {
                return;
            }
            
            self.page.ignoreReact(() => {
                // 修改被引用的shape的父容器，如果原shape的如容器是页面或者被引用的shape，则父容器修改为vector，否则修改为后缀加|vectorId
                // step1.
                if (s1.container === self.referencePage || s1.id === self.referenceShape) {
                    s1.container = vector.id;
                } else {
                    if (!self.uniqRef) {
                        s1.container += "|" + vector.id;
                    }
                }
                if (!self.uniqRef) {
                    s1.id += "|" + vector.id;
                    // 修改line的指向
                    if (s1.fromShape) {
                        s1.fromShape += "|" + vector.id;
                    }
                    if (s1.toShape) {
                        s1.toShape += "|" + vector.id;
                    }
                }
                if (s1.container === vector.id) {
                    if (minX === undefined || minX > s1.x) {
                        minX = s1.x;
                    }
                    if (minY === undefined || minY > s1.y) {
                        minY = s1.y;
                    }
                }
                if (clonedS.readOnly === undefined || clonedS.readOnly === null) {
                    s1.readOnly = true;
                }
                s1.isFocused = false;
                s1.selectable = !s1.readOnly && s1.refer === undefined && s1.type !== "referenceVector";
                if (s1.refer !== undefined) {
                    s1.backColor = "RGBA(255,255,255,0)";
                }
                s1.focusBorderColor = "gray";
                s1.containerAllowed = parent => {
                    // 如果当前图形的容器是page，那么其parent肯定不可能和模板中配置的container一致.
                    if (s1.getContainer().isTypeof("page")) {
                        return true;
                    }

                    // 不可以换container
                    return parent.id === clonedS.container;
                }
                if (s1.childAllowed !== undefined) {
                    s1.childAllowed = () => false;
                }
                self.referenced(s1);
            });
        });
        vector.invalidate();
        if (minX !== undefined && minY !== undefined) {
            if (self.keepOrigin) {
                vector.getShapes().forEach(s => s.moveTo(s.x + vector.x - minX + vector.getPadLeft(), s.y + vector.y - minY + vector.getPadTop()));
            } else {
                self.fit();
            }
        }
    };

    /**
     * 调整引用的shape尺寸，以适应自身大小
     */
    self.fit = () => {
        if (self.keepOrigin) {
            return;
        }
        if (self.page.isMouseDown()) {
            return;
        }
        let vector = self.getVector();
        if (vector === undefined) {
            return;
        }

        let shapes = vector.getShapes();
        if (shapes.length === 0) {
            return;
        }

        let minX = shapes.min(s => s.x);
        let minY = shapes.min(s => s.y);
        let maxWidth = shapes.max(s => s.x + s.width) - minX;
        let maxHeight = shapes.max(s => s.y + s.height) - minY;

        vector.scaleX = (self.width - 2 * vector.pad) / maxWidth;
        vector.scaleY = (self.height - 2 * vector.pad) / maxHeight;
        if (self.autoFit) {
            vector.scaleX = vector.scaleY = vector.scaleX > vector.scaleY ? vector.scaleY : vector.scaleX;
        }

        let ox = (vector.width - 2 * vector.pad - maxWidth * vector.scaleX) / (2 * vector.scaleX);
        let oy = (vector.height - 2 * vector.pad - maxHeight * vector.scaleY) / (2 * vector.scaleY);
        shapes.forEach(s => s.moveTo(s.x + vector.x - minX + vector.pad / vector.scaleX + ox, s.y + vector.y - minY + vector.pad / vector.scaleY + oy));
    }

    let onMouseUp = self.onMouseUp;
    self.onMouseUp = position => {
        self.fit();
        onMouseUp.call(self, position);
    }

    /**
     * 初始化，创建一个referenceVector的子shape
     * 所有被引用的shape，实际存放于该referenceVector
     */
    self.initialize = () => {
        if (self.referencePage === "") {
            const vector = self.page.createNew("referenceVector", self.x, self.y);
            vector.container = self.id;
            vector.serializable = self.serializable;
        }
    };

    let invalidateAlone = self.invalidateAlone;
    self.invalidateAlone = () => {
        let vector = self.getVector();
        if (vector !== undefined) {
            vector.moveTo(self.x + 1, self.y + 1);
        }
        invalidateAlone.call(self);
        self.fit();
    }

    self.getBorderWidth = () => {
        return 0;
    }

    self.modeChanged = mode => self.borderWidth = self.getBorderWidth();

    /**
     * 加载引用的数据
     * 从page中查找其引用的shape，并递归找到其所有的子shape
     *
     * @returns {{shapes: []}}
     */
    self.loadReferenceData = () => {
        let data = { shapes: [] };
        let p = self.page.graph.getPageDataById(self.referencePage);
        if (p === undefined) {
            return data;
        }

        let addShape = shapedata => {
            if (shapedata === undefined) {
                return;
            }
            data.shapes.push(shapedata);
            p.shapes.filter(s => s.container === shapedata.id).forEach(c => addShape(c));
        }
        if (self.referenceShape) {
            addShape(p.shapes.find(s => s.id === self.referenceShape));
        } else {
            data.shapes = p.shapes;
        }
        return data;
    };

    // --------------------------serialization & detection------------------------------
    self.addDetection(["referenceShape"], (property, value, preValue) => {
        self.refer();
        self.invalidate();
    });
    self.addDetection(["readOnly"], (property, value, preValue) => self.refer());
    //-----------------------------------------------------------------------------------

    return self;
};

/**
 * 继承自容器
 *
 * @param id
 * @param x
 * @param y
 * @param width
 * @param height
 * @param parent
 * @param drawer
 * @returns {Atom}
 */
const referenceVector = (id, x, y, width, height, parent, drawer = containerDrawer) => {
    let self = container(id, x, y, width, height, parent, drawer);
    self.type = "referenceVector";
    self.borderWidth = 0;
    self.hideText = true;
    self.backColor = "RGBA(255,255,200,0)";
    self.text = "reference";
    self.selectable = false;
    self.denyManualAdd = true;
    self.ifMaskItems = false;
    self.width = self.height = 10;
    self.childAllowed = s => !s.serializable;
    self.deSerialized = () => {
        if (self.page.sm.getShapeById(self.container) === undefined) {
            return;
        }
        self.getContainer().refer();
    };

    let load = self.load;
    self.load = ignoerFilter => {
        load.call(self, p => true)
    };

    return self;
};

export { reference, referenceVector };