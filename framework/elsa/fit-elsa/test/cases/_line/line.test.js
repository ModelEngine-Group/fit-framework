/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {line} from "../../../core/line.js";
import {DIRECTION, PAGE_MODE} from "../../../common/const.js";

// mock page.
const _page = {
    id: "pageId",
    shapes: [],
    disableReact: true,
    mode: PAGE_MODE.CONFIGURATION,
    graph: {
        setting: {
            borderColor: "",
        }
    },
    find: function () {
    },
    indexOf: function () {
    },
    moveToContainer: function () {
    }
};

_page.page = _page;

const _drawer = jest.fn(() => {
});
let _line;

describe("onReturnDrag", () => {
    beforeEach(() => {
        _line = line("test-line", 0, 0, 100, 80, _page, _drawer);
        jest.spyOn(_line, "moveTo").mockImplementation((x, y) => {
            _line.x = x;
            _line.y = y;
            _line.width -= x;
            _line.height -= y;
        });
        jest.spyOn(_line, "resize").mockImplementation((width, height) => {
            _line.width = width;
            _line.height = height;
        })
        jest.spyOn(_line, "follow").mockImplementation(jest.fn());
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("mousedownConnector is fromConnector", () => {
        // prepare
        _line.initConnectors();
        _line.mousedownConnector = _line.connectors.find(c => c.isType('from'))

        // when
        expect(_line.to().x).toBe(100);
        expect(_line.to().y).toBe(80);
        _line.onReturnDrag(200, 5, 200, 5);

        // then
        expect(_line.from().x).toBe(195);
        expect(_line.from().y).toBe(195);
        expect(_line.to().x).toBe(100);
        expect(_line.to().y).toBe(80);
    });

    test("mousedownConnector is toConnector", () => {
        // prepare
        _line.initConnectors();
        _line.mousedownConnector = _line.connectors.find(c => c.isType('to'))

        // when
        expect(_line.to().x).toBe(100);
        expect(_line.to().y).toBe(80);
        _line.onReturnDrag(200, 5, 200, 5);

        // then
        expect(_line.from().x).toBe(0);
        expect(_line.from().y).toBe(0);
        expect(_line.to().x).toBe(200);
        expect(_line.to().y).toBe(200);
    });

    describe("mousedownConnector is controlPoint", () => {
        self.prepare = () => {
            jest.spyOn(_line, "invalidate").mockImplementation(jest.fn());
            _line.fromShape = "mockShape1";
            _line.toShape = "mockShape2";
            _line.initConnectors();
            _line.mousedownConnector = _line.connectors.find(c => c.isType('controlPoint-1'))
        }

        test("direction is H", () => {
            // prepare
            prepare();

            // when
            expect(_line.to().x).toBe(100);
            expect(_line.to().y).toBe(80);
            _line.controlPoints[0].direction = DIRECTION.H;
            _line.onReturnDrag(200, 5, 200, 5);

            // then
            expect(_line.from().x).toBe(0);
            expect(_line.from().y).toBe(0);
            expect(_line.to().x).toBe(100);
            expect(_line.to().y).toBe(80);
            expect(_line.controlPoints[0].x).toBe(50);
            expect(_line.controlPoints[0].y).toBe(40);
            expect(_line.controlPoints[0].offsetX).toBe(200);
            expect(_line.controlPoints[0].offsetY).toBe(0);
            expect(_line.brokenPoints[0].x).toBe(200);
            expect(_line.brokenPoints[0].y).toBe(0);
            expect(_line.brokenPoints[1].x).toBe(200);
            expect(_line.brokenPoints[1].y).toBe(80);
        });

        test("direction is V", () => {
            // prepare
            prepare();

            // when
            expect(_line.to().x).toBe(100);
            expect(_line.to().y).toBe(80);
            _line.controlPoints[0].direction = DIRECTION.V;
            _line.onReturnDrag(200, 5, 200, 5);

            // then
            expect(_line.from().x).toBe(0);
            expect(_line.from().y).toBe(0);
            expect(_line.to().x).toBe(100);
            expect(_line.to().y).toBe(80);
            expect(_line.controlPoints[0].x).toBe(50);
            expect(_line.controlPoints[0].y).toBe(40);
            expect(_line.controlPoints[0].offsetX).toBe(0);
            expect(_line.controlPoints[0].offsetY).toBe(200);
            expect(_line.brokenPoints[0].x).toBe(50);
            expect(_line.brokenPoints[0].y).toBe(200);
            expect(_line.brokenPoints[1].x).toBe(50);
            expect(_line.brokenPoints[1].y).toBe(200);
        });
    });
});

describe("initConnectors", () => {
    beforeEach(() => {
        _line = line("test-line", 0, 0, 100, 80, _page, _drawer);
        _line.fromShape = "mockShape1";
        _line.toShape = "mockShape2";
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("mousedownConnector is fromConnector", () => {
        // prepare

        // when
        _line.initConnectors();

        // then
        expect(_line.controlPoints[0].x).toBe(50);
        expect(_line.controlPoints[0].y).toBe(40);
    });

    test("mousedownConnector is fromConnector", () => {
        // prepare
        _line.width = 5;
        _line.height = 5;

        // when
        _line.initConnectors();

        // then
        expect(_line.controlPoints[0]).toBeUndefined();
    });
});