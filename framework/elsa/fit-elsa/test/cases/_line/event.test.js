/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {event} from "../../../plugins/flowable/nodes/event.js";
import {PAGE_MODE} from "../../../common/const.js";

const _page = {
    id: "pageId",
    shapes: [],
    disableReact: true,
    mode: PAGE_MODE.CONFIGURATION,
    find: function () {
    },
    indexOf: function () {
    }
};

_page.page = _page;

const _drawer = jest.fn(() => {
});

describe("adjustNextPosition test", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("event's absolute width > event's absolute height, event's width is positive", () => {
        // prepare
        const _event = event("test-event", 100, 50, 100, 100, _page, _drawer);
        _event.width = 200;
        _event.height = 100;
        _event.toConnector = {x: 55, y: 33};
        const _next = {width: 80, height: 40};

        // when
        const position = _event.getNextPosition(_next);

        // then
        expect(position).toStrictEqual({x: 155, y: 63});
    });

    test("event's absolute width > event's absolute height, event's width is negative", () => {
        // prepare
        const _event = event("test-event", 100, 50, 100, 100, _page, _drawer);
        _event.width = -200;
        _event.height = 100;
        _event.toConnector = {x: 55, y: 33};
        const _next = {width: 80, height: 40};

        // when
        const position = _event.getNextPosition(_next);

        // then
        expect(position).toStrictEqual({x: 75, y: 63});
    });

    test("event's absolute width < event's absolute height, event's width is positive", () => {
        // prepare
        const _event = event("test-event", 100, 50, 100, 100, _page, _drawer);
        _event.width = 100;
        _event.height = 200;
        _event.toConnector = {x: 55, y: 33};
        const _next = {width: 80, height: 40};

        // when
        const position = _event.getNextPosition(_next);

        // then
        expect(position).toStrictEqual({x: 115, y: 83});
    });

    test("event's absolute width < event's absolute height, event's width is positive", () => {
        // prepare
        const _event = event("test-event", 100, 50, 100, 100, _page, _drawer);
        _event.width = 100;
        _event.height = -200;
        _event.toConnector = {x: 55, y: 33};
        const _next = {width: 80, height: 40};

        // when
        const position = _event.getNextPosition(_next);

        // then
        expect(position).toStrictEqual({x: 115, y: 43});
    });
});