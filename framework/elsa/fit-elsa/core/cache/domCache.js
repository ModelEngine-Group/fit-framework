/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 用于dom的缓存.
 */
export default class DomCache {
    _cache = new Map();

    /**
     * 缓存dom元素.
     *
     * @param owner 待缓存元素所属的根节点.
     * @param pageId 页面id.
     * @param element 待缓存元素.
     */
    cache(owner, pageId, element) {
        let ownerCache = this._cache.get(owner);
        if (!ownerCache) {
            ownerCache = new Map();
            this._cache.set(owner, ownerCache);
        }

        let pageCache = ownerCache.get(pageId);
        if (!pageCache) {
            pageCache = new Map();
            ownerCache.set(pageId, pageCache);
        }

        pageCache.set(element.id, element);
    }

    /**
     * 获取缓存的dom元素.
     *
     * @param owner 待缓存元素所属的根节点.
     * @param pageId 页面id.
     * @param domId 待缓存元素id.
     * @return {null|*} 若不存在，则返回null，否则返回对应元素.
     */
    get(owner, pageId, domId) {
        const ownerCache = this._cache.get(owner);
        if (!ownerCache) {
            return null;
        }
        const pageCache = ownerCache.get(pageId);
        return pageCache ? pageCache.get(domId) : null;
    }

    /**
     * 批量缓存元素.
     *
     * @param owner 待缓存元素所属的根节点.
     * @param pageId 页面id.
     * @param elements 待缓存元素集合.
     */
    cacheElements(owner, pageId, elements) {
        if (!elements || elements.length === 0) {
            return;
        }
        elements.forEach(e => this.cache(owner, pageId, e));
    }

    /**
     * 清理某一页的dom元素.
     *
     * @param owner 元素所属的根节点.
     * @param pageId 页面id.
     */
    clearElementsByPageId(owner, pageId) {
        let ownerCache = this._cache.get(owner);
        if (!ownerCache) {
            return;
        }

        let pageCache = ownerCache.get(pageId);
        if (!pageCache) {
            return;
        }
        pageCache.clear();
    }

    /**
     * 遍历所有的dom元素.
     *
     * @param action 操作类型.
     */
    forEachDom(action) {
        this._cache.forEach((ownerCache) => {
            ownerCache.forEach((pageCache) => {
                pageCache.forEach((v, k) => {
                    action(k, v);
                })
            });
        });
    }
}