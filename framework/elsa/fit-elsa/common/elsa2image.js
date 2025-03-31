/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

export const elsatoimage = () => {
    let self = {};

    const getNodeSize = (node, options) => {
        const width = (options && options.width) || node.clientWidth;
        const height = (options && options.height) || node.clientHeight;
        return {width, height};
    }

    const createImage = (id, url) => {
            const img = new Image();
            img.onload = () => {}
            img.src = url;
            img.id = id;
            return img;
    }

    const asyncCreateImage = (id, url) => {
        return new Promise((resolve, reject) => {
            const img = new Image();
            img.onload = () => {
                resolve(img);
            }
            img.onerror = reject;
            img.src = url;
            img.id = id;
        });
    }

    const nodeToSvgURL = (clonedNode, width, height) => {
        clonedNode.setAttribute('xmlns', 'http://www.w3.org/1999/xhtml');
        const nodeString = new XMLSerializer().serializeToString(clonedNode);
        const html = `<svg xmlns="http://www.w3.org/2000/svg" width="${width} height=${height}" viewBox="0 0 ${width} ${height}">
                        <foreignObject x="0" y="0" width="100%" height="100%" externalResourcesRequired="true">${encodeURIComponent(nodeString)}</foreignObject>
                        </svg>`;

        return 'data:image/svg+xml;charset=utf-8,' + html;
    }

    const cloneCss = (node, clone) => {
        const targetCss = clone.style;
        if (!targetCss) {
            return clone;
        }

        const sourceCss = window.getComputedStyle(node);
        if (sourceCss.cssText) {
            targetCss.cssText = sourceCss.cssText;
            targetCss.transformOrigin = sourceCss.transformOrigin;
            return clone;
        }

        Array.from(sourceCss).forEach((name) => {
            targetCss.setProperty(
                name,
                sourceCss.getPropertyValue(name),
                sourceCss.getPropertyPriority(name),
            );
        });

        return clone;
    }

    // 图片src为地址时，需转换为base64
    const imageToDataUri = (image) => {
        const {width, height} = getNodeSize(image);
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");
        canvas.width = width;
        canvas.height = height;
        ctx.drawImage(image, 0, 0, width, height);
        return canvas.toDataURL("image/png");
    }

    // 画布中非内容元素，无需拷贝，如协同编辑人数、鼠标、shape选中状态等
    const cloneAble = (node) => {
        const regex = /^(animation:)|^(region-sessionCount:)|^(cursor:elsa-page:)/;
        return !node.id || !node.id.match(regex);
    }

    const cloneOneself = (node) => {
        if (node instanceof HTMLImageElement) {
            return createImage(node.id, imageToDataUri(node));
        }

        if (node instanceof HTMLCanvasElement) {
            return createImage(node.id, node.toDataURL());
        }
        return node.cloneNode(false);
    }

    const cloneChildren = (node, clone, options) => {
        let children = node.childNodes;
        if (children.length > 0) {
            Array.from(children).forEach(c => {
                const clonedChild = cloneNode(c, options);
                clonedChild && clone.append(clonedChild);
            })
        }
        return clone;
    }

    const cloneNode = (node, options, isRoot) => {
        let clone = null;
        if (!isRoot && options.filter && !options.filter(node)) {
            return clone;
        }
        if (!cloneAble(node)) {
            return clone;
        }

        clone = cloneOneself(node, clone, options);
        clone = cloneChildren(node, clone, options);
        clone = cloneCss(node, clone);
        return clone;
    }

    /**
     * @param {Node} node - DOM node
     * @param {Object} options
     * @param {Number} options.width
     * @param {Number} options.height
     * @param {Number} options.quality(0-1)
     * @param {Function} options.filter - 过滤node中包含的元素
     * @return {Promise} - a PNG image data URL
     */
    self.toPng = async (node, options) => {
        if (node instanceof HTMLCanvasElement) {
            return node.toDataURL();
        }

        const canvas = await self.toCanvas(node, options);
        return canvas.toDataURL();
    }

    /**
     * @param {Node} node - DOM node
     * @param {Object} options
     * @param {Number} options.width
     * @param {Number} options.height
     * @param {Number} options.type（常见：text/plain、text/html、image/png）
     * @param {Number} options.quality(0-1)
     * @param {Function} options.filter - 过滤node中包含的元素
     * @return {Promise} - a PNG image blob
     */
    self.toBlob = async (node, options) => {
        const canvas = await self.toCanvas(node, options);
        const type = options.type ? options.type : 'image/png';
        const quality = options.quality ? options.quality : 1;

        return new Promise((resolve, reject) => {
            canvas.toBlob(resolve, type, quality);
        });
    }

    /**
     * @param node
     * @param options
     * @returns {Promise<HTMLCanvasElement>}
     */
    self.toCanvas = async (node, options) => {
        const {width, height} = getNodeSize(node, options);
        const svg = self.toSvg(node, options);
        const image = await asyncCreateImage(node.id, svg);

        let canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        canvas.getContext('2d').drawImage(image, 0, 0, width, height);
        return canvas;
    }

    /**
     * @param node
     * @param options
     * @returns {string}
     */
    self.toSvg = (node, options) => {
        const clonedNode = cloneNode(node, options);
        return nodeToSvgURL(clonedNode, node.clientWidth, node.clientHeight);
    }





    return self;
}