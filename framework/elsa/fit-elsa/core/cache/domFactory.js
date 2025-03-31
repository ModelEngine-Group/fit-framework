/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 用于dom的创建.
 */
export default class DomFactory {
    static SVG_TAG_NAME = new Set().add("svg")
        .add("path")
        .add("defs")
        .add("marker");

    _domCache;
    _enableCache;

    constructor(domCache, enableCache) {
        this._domCache = domCache;
        this._enableCache = enableCache;
    }

    /**
     * 创建一个dom.
     * 若在缓存中存在，则直接从缓存中获取.
     *
     * @param owner 待缓存元素所属的根节点.
     * @param tagName 元素名称.
     * @param pageId 页面的id.
     * @param id dom元素的id.
     * @param ignoreExisting 是否在缓存存在的情况下也重新创建.
     * @return {*} dom对象.
     */
    create({owner, tagName, pageId, id, ignoreExisting}) {
        let node = this._domCache.get(owner, pageId, id);
        if (!node || ignoreExisting) {
            node = this._createDom(tagName);
            node.id = id;
            this._enableCache && this._domCache.cache(owner, pageId, node);
        }
        return node;
    }

    /**
     * @private
     */
    _createDom(tagName) {
        if (DomFactory.SVG_TAG_NAME.has(tagName)) {
            return document.createElementNS('http://www.w3.org/2000/svg', tagName);
        }
        return document.createElement(tagName);
    }
}