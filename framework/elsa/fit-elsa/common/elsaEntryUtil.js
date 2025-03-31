/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {uuid} from "./util.js";

// 重新生成所有id，并更新container、reference等id信息
export const reGenerateId = (graph, data) => {
    let pageHandlers = [];
    let shapeHandlers = [];

    const replaceId = (arr, obj, idMap) => {
        arr.map(k => {
            const path = k.split('.');
            let pathValue = obj;
            path.map(p => {
                pathValue = pathValue[p];
            });
            const originId = pathValue;
            const newId = idMap.get(originId);
            if (newId) {
                let th = "";
                for (let i = 0; i < path.length; i++) {
                    th += "['" + path[i] + "']";
                }
                eval(`obj${th}='${newId}'`);
            }
        });
    }

    pageHandlers.push((page, idMap) => {
        const arr = ["graphId", "id", "properties.id", "properties.basePage"];
        replaceId(arr, page, idMap);
    });

    shapeHandlers.push((shape, idMap) => {
        const arr = ["id", "properties.id", "properties.container", "properties.referencePage"];
        replaceId(arr, shape, idMap);
    });

    shapeHandlers.push((shape, idMap) => {
        let referenceData = shape.properties.referenceData;
        if (!referenceData || Object.keys(referenceData).length === 0) {
            return;
        }
        const placedArr = referenceData.placed;
        for (let i = 0, u = placedArr.length; i < u; i++) {
            const originId = placedArr[i];
            const newId = idMap.get(originId);
            if (newId) {
                placedArr[i] = newId;
            }
        }

        Object.keys(referenceData).map(k => {
            if (k !== "placed") {
                const originId = k;
                const newId = idMap.get(originId);
                if (newId) {
                    const kValue = referenceData[originId];
                    delete referenceData[originId];
                    referenceData[newId] = kValue;
                }
            }
        })
    });

    const idMap = new Map();
    idMap.set(data.id, graph.id);
    data.id = graph.id;
    data.properties.id = graph.id;
    data.pages.map(p => {
        const pOldId = p.id;
        if (!idMap.get(pOldId)) {
            const pNewId = `elsa-page:${graph.uuid()}`;
            idMap.set(pOldId, pNewId);
        }
        pageHandlers.forEach(h => h(p, idMap));
        p.shapes.map(s => {
            const sOldId = s.id;
            if (!idMap.get(sOldId)) {
                const sNewId = uuid();
                idMap.set(sOldId, sNewId);
            }
            shapeHandlers.forEach(h => h(s, idMap));
        })
    });

    return data;
}