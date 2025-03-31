/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import 'core-js/stable';
import {graph} from "../../core/graph.js";
import {EVENT_TYPE} from "../../common/const";
import {contextMenu} from "../../core/popupMenu";

// mock contextMenu
jest.mock('../../core/popupMenu', () => {
    const originalModule = jest.requireActual('../../core/popupMenu');
    return {
        __esModule: true, ...originalModule, contextMenu: jest.fn(() => {
        })
    };
});


const _dom = {
    style: {}
}
const _graph = graph(_dom, "");
_graph.activePage = {
    showContextMenu: () => {
        return false;
    },
    getFocusedShapes: () => jest.fn(() => {})
};

describe("event test cases", () => {
    afterEach(() => {
        jest.restoreAllMocks();
        jest.clearAllMocks();
    });

    test("CONTEXT_CREATE#page show context menu", async () => {
        jest.spyOn(_graph.activePage, "showContextMenu").mockReturnValue(true);
        await _graph.fireEvent({type: EVENT_TYPE.CONTEXT_CREATE, shapes: []});
        expect(contextMenu).toHaveBeenCalled();
    });

    test("CONTEXT_CREATE#page don't show context menu", async () => {
        await _graph.fireEvent({type: EVENT_TYPE.CONTEXT_CREATE, shapes: []});
        expect(contextMenu).toHaveBeenCalledTimes(0);
    });
});