/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {addCommand} from '../core/commands.js';
import {EVENT_TYPE} from '../common/const.js';

const BASE_OFFSET = 20;

/**
 * Elsa数据拷贝处理器.
 */
export class ElsaCopyHandler {
    static ELSA_COPY_MATCHER = /.*elsa\/(?<type>\w+)/;

    _helper;

    constructor(helper) {
        this._helper = helper;
    }

    /**
     * 处理elsa数据拷贝逻辑.
     *
     * @param event 拷贝事件对象.
     * @param items 拷贝的数据数组.
     * @param page 页面对象.
     */
    handle(event, items, page) {
        const elsaCopy = items.map(item => {
            return {item, match: ElsaCopyHandler.ELSA_COPY_MATCHER.exec(item.type)};
        }).find(m => !!m.match);
        if (!elsaCopy) {
            return;
        }
        const focused = page.getFocusedShapes();
        const arg = event.clipboardData.getData(elsaCopy.item.type);

        const isOneFocused = focused && focused.length === 1;
        const isPasteOk = focused.length > 0 &&
          focused[0].paste &&
          focused[0].paste(JSON.parse(arg)[0], page.mousex, page.mousey);
        const canNotPast = elsaCopy.match.groups.type !== "shape" && isOneFocused && isPasteOk;
        if (!canNotPast) {
            // 粘贴形状被失败，编程粘贴形状
            return this._helper.pasteShapes(arg, '', page, page.mousex, page.mousey);
        }
        return undefined;
    }
}

/**
 * plainText数据拷贝处理器.
 */
export class PlainCopyHandler {
    _helper;

    constructor(helper) {
        this._helper = helper;
    }

    /**
     * 处理plain/text拷贝逻辑.
     *
     * @param event 拷贝事件对象.
     * @param items 拷贝的数据数组.
     * @param page 页面对象.
     */
    handle(event, items, page) {
        const plainCopy = items.find(item => item.type.indexOf("text/plain") === 0);
        if (!plainCopy) {
            return;
        }
        const focused = page.getFocused();
        const arg = event.clipboardData.getData("text/plain");
        const html = event.clipboardData.getData("text/html");
        const gridShapes = this._convertGridShapes(html);
        const shapes = this._convertShapes(arg);
        if (gridShapes) {// 有可转化同行
            const isOneFocused = focused && focused.length === 1;
            const isPasteOk = focused.length > 0 && focused[0].paste && focused[0].paste(gridShapes[0]);
            const isGridPastOk = gridShapes.length === 1 && isOneFocused && isPasteOk;
            if (!isGridPastOk) {
                this._helper.pasteShapes(gridShapes, "", page, page.mousex, page.mousey);
            }
        } else if (shapes && shapes.length > 0) { // 兼容鼠标点击 触发的拷贝行为（如：点击共享单个图形、快捷工具栏复制按钮等）
            this._helper.pasteShapes(shapes, "", page, page.mousex, page.mousey);
        } else {
            this._pasteText(arg, arg, page, page.mousex, page.mousey);
        }
    }

    _pasteText(string, plainText, page, x, y) {
        if (page.isEditing()) {
            return;
        }
        let rect = page.createNew("text", x, y);
        rect.text = plainText;
        rect.borderWidth = 0;
        rect.width = rect.height = 100;
        rect.fontColor = "#000";
        rect.invalidate();
    }

    _convertGridShapes(arg) {
        if (!arg) {
            return null;
        }

        const domParser = new DOMParser();
        const res = domParser.parseFromString(arg, 'text/html');
        const htmlContent = res.querySelector("html").getAttribute("xmlns:x");
        const gridReg = /.*excel/;
        if (!gridReg.exec(htmlContent)) {
            return null;
        }
        // 暂未实现
        return null;
    }

    _convertShapes(arg) {
        const reg = /.*elsa/;
        const match = reg.exec(arg);
        if (!match) {
            return null;
        }

        const data = JSON.parse(arg);
        if (data.type && data.type === "elsa" && data.shapes.length > 0) {
            return data.shapes;
        }
        return null;
    }
}

/**
 * image数据拷贝处理器.
 */
export class ImageCopyHandler {
    _helper;

    constructor(helper) {
        this._helper = helper;
    }

    /**
     * 处理图片拷贝逻辑.
     *
     * @param event 拷贝事件对象.
     * @param items 拷贝的数据数组.
     * @param page 页面对象.
     */
    handle(event, items, page) {
        const imgCopy = items.find(item => item.type.indexOf("image") === 0);
        if (!imgCopy) {
            return;
        }
        this._pasteImage(imgCopy, "", page);
    }

    _pasteImage(item, plainText, page) {
        const self = this;
        const blob = item.getAsFile();
        const reader = new FileReader();
        reader.onload = (event) => {
            const sourceImg = new Image();
            sourceImg.src = event.target.result;
            sourceImg.onload = function () {
                let img = page.createNew("image", page.mousex, page.mousey);
                img.width = this.width;
                img.height = this.height;
                self._getImgUrl(event.target.result.split(',')[1]).then(data => {
                    img.src = data;

                    // 先取消之前focused图形的选中状态.
                    page.getFocusedShapes().forEach(s => s.unSelect());

                    // 选中当前图片.
                    img.select();
                    img.invalidate();
                    // 添加命令，支持撤销重做.
                    page.triggerEvent({type: EVENT_TYPE.SHAPE_ADDED, value: [img]});
                });
            }
        };
        reader.readAsDataURL(blob);
    };

    _getImgUrl(imageURI) {
        const convertBase64UrlToImgFile = (urlData, fileName, fileType) => {
            const bytes = window.atob(urlData); // 转换为byte
            // 处理异常,将ascii码小于0的转换为大于0
            const ab = new ArrayBuffer(bytes.length);
            const ia = new Int8Array(ab);
            let i;
            for (i = 0; i < bytes.length; i++) {
                ia[i] = bytes.charCodeAt(i);
            }
            // 转换成文件，添加文件的type，name，lastModifiedDate属性
            const blob = new Blob([ab], {type: fileType});
            blob.lastModifiedDate = new Date();
            blob.name = fileName;
            return blob;
        };

        return new Promise((resolve, reject) => {
            const fileName = (new Date()).getTime() + ".jpeg"; // 随机文件名
            const imgFile = convertBase64UrlToImgFile(imageURI, fileName, 'image/jpeg'); // 转换成file
            const formData = new FormData();
            formData.append('file', imgFile); // 放到表单中，此处的file要和后台取文件时候的属性名称保持一致
            const httpRequest = new XMLHttpRequest();
            httpRequest.open('post', window.location.origin + "/elsa-backend/common/upload");
            httpRequest.send(formData);
            httpRequest.onreadystatechange = () => {
                if (httpRequest.readyState === 4 && httpRequest.status === 200) {
                    try {
                        resolve(JSON.parse(httpRequest.responseText).data);
                    } catch {
                        reject(httpRequest.responseText);
                    }
                }
            };
        });
    }
}

/**
 * 拷贝工具类.
 */
export const copyPasteHelper = () => {
    const self = {};
    self.handlers = [
        new ElsaCopyHandler(self),
        new PlainCopyHandler(self),
        new ImageCopyHandler(self)
    ];
    self.shapeDataHelper = shapeDataHelper;

    /**
     * 粘贴.
     *
     * @param event 事件对象.
     * @param page 页面对象.
     * @returns {*[]} 粘贴后图形id列表.
     */
    self.paste = (event, page) => {
        const items = [...(event.clipboardData || event.originalEvent.clipboardData).items];
        return self.handlers.reduce((acc, handler) => {
            const result = handler.handle(event, items, page);
            if (Array.isArray(result)) {
                acc.push(...result); // 展平数组
            } else if (result !== undefined) {
                acc.push(result); // 处理非数组但有返回值的情况
            }
            return acc;
        }, []);
    };

    /**
     * 粘贴图形.
     *
     * @param string 字符串.
     * @param plainText 文本.
     * @param targetPage 目标页面对象.
     * @param x 横坐标.
     * @param y 纵坐标.
     */
    self.pasteShapes = (string, plainText, targetPage, x, y) => {
        const shapeDataArray = (typeof string === 'string') ? JSON.parse(string) : string;
        const sortedShapeDataArray = containerFirstSort(shapeDataArray);
        const idMap = new Map();
        const sdHelpers = sortedShapeDataArray.map(sd => self.shapeDataHelper(sd, targetPage, sortedShapeDataArray));

        // 共享等判断，并且决定是否需要生成新的id.
        sdHelpers.forEach(sdHelper => {
            if (!sdHelper.isExists() && sdHelper.data.shared) {
                return;
            }
            if (sdHelper.isContainerShared()) {
                sdHelper.data.inShared = true;
                return;
            }
            sdHelper.generateNewId(idMap);
        });

        // 计算偏移量.
        const minX = Math.min(...sortedShapeDataArray.map(c => c.x));
        const minY = Math.min(...sortedShapeDataArray.map(c => c.y));
        const offsetX = x !== undefined ? x - minX : BASE_OFFSET;
        const offsetY = y !== undefined ? y - minY : BASE_OFFSET;

        const sessions = {};
        const roots = [];
        const newShapes = [];

        // 开始拷贝图形，其实就是通过图形的数据创建出新的图形对象.
        sdHelpers.forEach(sdHelper => {
            if (sdHelper.isExists()) {
                return;
            }
            sdHelper.preProcessShapeData(plainText, idMap);
            const newShape = sdHelper.applyCreate(offsetX, offsetY, sessions);
            if (!newShape) {
                return;
            }
            !sessions[sdHelper.data.pasteSession] && (sessions[sdHelper.data.pasteSession] = newShape.container);// 第一个create成功，后面的都成功
            if (sdHelper.isRoot()) {
                sdHelper.data.container = sessions[sdHelper.data.pasteSession];
                roots.push(newShape);
            }
            sdHelper.updateAnimations();
            sdHelper.created(newShape);
            newShapes.push({shape: newShape});
        });

        // 先取消所有图形的选中状态.
        targetPage.sm.getShapes().forEach(shape => shape.unSelect());
        // 再invalidate一次，将children可能的影响施加
        roots.forEach(s => {
            s.reset();
            delete s.editBy;
            s.select();
        });
        // 添加命令，支持撤销重做.
        addCommand(targetPage, newShapes);
        return newShapes;
    };

    /*
     * 按照树状结构，调整待粘贴的图形顺序，保障container先创建.
     *
     * @param shapeDataArray 图形数据数组.
     * @returns {*[]} 排序之后的数组.
     */
    const containerFirstSort = (shapeDataArray) => {
        function addRecursively(rangedShapeData, data, containShapes) {
            rangedShapeData.push(data);
            const subList = containShapes[data.id];
            if (subList) {
                subList.forEach(shape => addRecursively(rangedShapeData, shape, containShapes));
            }
        }

        let rangedShapeData = [];
        let containShapes = {};
        let ids = new Set();
        shapeDataArray.forEach(data => {
            (!containShapes[data.container]) && (containShapes[data.container] = []);
            containShapes[data.container].push(data);
            ids.add(data.id);
        });

        shapeDataArray.forEach(data => {
            // 找到复制的根节点
            if (!ids.has(data.container)) {
                addRecursively(rangedShapeData, data, containShapes);
                data.container = "";
                // 复制的根节点如果是结构化文档类型，则需显示文本
                if (data.type === "docSection") {
                    data.hideText = false;
                }
            }
        });

        return rangedShapeData;
    };

    return self;
};

/**
 * 拷贝时的图形数据帮助器.
 *
 * @param data 待处理的图形数据.
 * @param targetPage 拷贝的目标page.
 * @param shapeDataArray 所有的图形数据所形成的数组.
 */
export const shapeDataHelper = (data, targetPage, shapeDataArray) => {
    const self = {};
    self.data = data;

    /**
     * 在目标page中是否已存在该图形.
     *
     * @return {*} true/false.
     */
    self.isExists = () => {
        return targetPage.sm.containsById(self.data.id);
    };

    /**
     * 图形所在的容器是否处于share状态.
     *
     * @return {*} true/false.
     */
    self.isContainerShared = () => {
        const container = shapeDataArray.find(s => s.id === self.data.container);
        return container && (container.shared || container.inShared);
    };

    /**
     * 通过数据创建对应的图形对象.
     *
     * @param offsetX x偏移量.
     * @param offsetY y偏移量.
     * @param sessions 会话.
     * @return {*} 图形对象.
     */
    self.applyCreate = (offsetX, offsetY, sessions) => {
        self.data.x += offsetX;
        self.data.y += offsetY;
        return targetPage.createNew(self.data.type,
            self.data.x,
            self.data.y,
            self.data.id,
            undefined,
            undefined,
            sessions[self.data.pasteSession] !== undefined,
            self.data
        );
    };

    /**
     * 创建之后.
     *
     * @param shape 通过data创建出的图形.
     */
    self.created = (shape) => {
        targetPage.shapeCreated(shape);
        shape.pasted && shape.pasted();
        shape.getContainer().shapeAdded(shape);
    };

    /**
     * 在deserialize时做一些处理.
     *
     * @param plainText 文本.
     */
    self.preProcessShapeData = (plainText) => {
        processLine();
        self.data.text = plainText !== "" ? plainText : self.data.text;
    };

    /**
     * 修改动画.
     */
    self.updateAnimations = () => {
        if (targetPage.animations && targetPage.animations.length > 0) {
            const dataAnimation = targetPage.animations.find(item => item.shape === self.oldId);
            dataAnimation && targetPage.animations.push({...dataAnimation, shape: self.data.id});
        }
    };

    const processLine = () => {
        // fromShape存在，但fromShape未一起拷贝，删除线的fromShape.
        if (self.data.fromShape && !shapeDataArray.contains(s => s.id === self.data.fromShape)) {
            delete self.data.fromShape;
        }

        // 同上.
        if (self.data.toShape && !shapeDataArray.contains(s => s.id === self.data.toShape)) {
            delete self.data.toShape;
        }
    };

    /**
     * 拷贝时需要更新当前图形数据的id字段.
     */
    self.generateNewId = (idMap) => {
        self.oldId = self.data.id;
        self.data.id = targetPage.graph.uuid();
        idMap.set(self.oldId, self.data.id);

        // 修改关联图形数据，比如线或子元素.
        shapeDataArray.filter(sd => sd !== self.data).forEach(sd => {
            sd.fromShape === self.oldId && (sd.fromShape = self.data.id);
            sd.toShape === self.oldId && (sd.toShape = self.data.id);
            sd.container === self.oldId && (sd.container = self.data.id);
        });

        // 设置共享.
        if (self.data.shared) {
            self.data.shared = false;
            self.data.sharedBy = undefined;
        }
    };

    /**
     * 是否是拷贝的根节点.
     */
    self.isRoot = () => {
        return self.data.container === "";
    };

    return self;
};
