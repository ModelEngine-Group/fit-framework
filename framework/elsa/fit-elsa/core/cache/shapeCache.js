/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 用于shape的缓存.
 */
export default class ShapeCache {
    _cache = new Map();

    /**
     * 缓存shape.
     *
     * @param owner 图形dom所属的dom元素根节点.
     * @param pageId 图形所属的页面id.
     * @param shape 图形对象.
     */
    cache(owner, pageId, shape) {
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

        pageCache.set(shape.id, shape);
    };

    /**
     * 获取缓存的shape.
     *
     * @param owner 图形dom所属的dom元素根节点.
     * @param pageId 图形所属的页面id.
     * @param shapeId 图形的id.
     * @return {null|*} 未找到返回null，否则，返回图形对象.
     */
    get(owner, pageId, shapeId) {
        const ownerCache = this._cache.get(owner);
        if (!ownerCache) {
            return null;
        }

        const pageCache = ownerCache.get(pageId);
        if (!pageCache) {
            return null;
        }

        return pageCache.get(shapeId);
    }

    /**
     * 遍历所有的page.
     *
     * @param action 操作处理.
     */
    forEachPage(action) {
        this._cache.forEach((pageMap, owner) => {
            pageMap.forEach((shapeMap, pageId) => {
                action(owner, pageId, shapeMap);
            });
        });
    }

    /**
     * 清理page缓存.
     *
     * @param owner 图形dom所属的dom元素根节点.
     * @param pageId 页面id.
     */
    clearPage(owner, pageId) {
        const ownerCache = this._cache.get(owner);
        if (!ownerCache) {
            return;
        }

        const pageCache = ownerCache.get(pageId);
        if (!pageCache) {
            return;
        }
        pageCache.clear();
    }

    /**
     * 销毁缓存.
     */
    destroy() {
        this._cache.clear();
        this._cache = null;
    }

    /**
     * 清空缓存数据.
     */
    clear() {
        this._cache.clear();
    }
}