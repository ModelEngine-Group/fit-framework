/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {line} from "../../../core/line.js";
import {lineHelper} from "../../../core/lineHelper.js";
import {DIRECTION, PAGE_MODE} from "../../../common/const.js";

// mock page.
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
const _line = line("test-line", 0, 0, 100, 100, _page, _drawer);
const _lineHelper = lineHelper();

describe("Broken Line Helper", () => {
    describe("WE", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's width is negative", () => {
            // prepare
            _line.width = -100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0].x).toBe(-50);
            expect(_line.brokenPoints[0].y).toBe(0);
            expect(_line.brokenPoints[1].x).toBe(-50);
            expect(_line.brokenPoints[1].y).toBe(50);
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("fromShape is above of toShape", () => {
            // prepare
            const _mockFromShape = {y: 50, height: 50};
            const _mockToShape = {y: 150};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: 50});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: 50});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 70, y: 50});
        });

        test("toShape is above of fromShape", () => {
            // prepare
            const _mockFromShape = {y: 100, height: 50};
            const _mockToShape = {y: 10, height: 50};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: -45});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: -45});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 70, y: 50});
        });

        test("fromShape and toShape overlap in y axis", () => {
            // prepare
            const _mockFromShape = {y: 0, height: 60};
            const _mockToShape = {y: 20, height: 70};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            const _mockFromShapeConnector = {x: 0, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_mockFromShapeConnector);

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: -42});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: -42});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 70, y: 50});
        });
    });

    describe("EW", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's width is positive", () => {
            // prepare
            _line.width = 100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 50, y: 50});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("fromShape is above of toShape", () => {
            // prepare
            const _mockFromShape = {y: 50, height: 50};
            const _mockToShape = {y: 150};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = -50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: 50});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -70, y: 50});
            expect(_line.brokenPoints[3]).toStrictEqual({x: -70, y: 50});
        });

        test("toShape is above of fromShape", () => {
            // prepare
            const _mockFromShape = {y: 100, height: 50};
            const _mockToShape = {y: 10, height: 50};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = -50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: -45});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -70, y: -45});
            expect(_line.brokenPoints[3]).toStrictEqual({x: -70, y: 50});
        });

        test("fromShape and toShape overlap in y axis.", () => {
            // prepare
            const _mockFromShape = {y: 40, height: 50};
            const _mockToShape = {y: 50, height: 60};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            const _mockFromShapeConnector = {x: 0, y: 25};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_mockFromShapeConnector);

            _line.width = -50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: -45});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -70, y: -45});
            expect(_line.brokenPoints[3]).toStrictEqual({x: -70, y: 50});
        });
    });

    describe("NS", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's height is negative", () => {
            // prepare
            _line.width = 100;
            _line.height = -50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -25});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 100, y: -25});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("fromShape is on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 50, width: 50};
            const _mockToShape = {x: 150};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 50, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 70});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 50, y: 70});
        });

        test("fromShape is on the right of toShape", () => {
            // prepare
            const _mockFromShape = {x: 150, width: 50};
            const _mockToShape = {x: 50, width: 50};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -50, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -50, y: 70});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 50, y: 70});
        });

        test("fromShape and toShape overlap in the X-axis direction", () => {
            // prepare
            const _mockFromShape = {x: 80, width: 50};
            const _mockToShape = {x: 50, width: 50};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _mockFromShapeConnector = {x: 25, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_mockFromShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 45, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 45, y: 70});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 50, y: 70});
        });
    });

    describe("SN", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's height is positive", () => {
            // prepare
            _line.width = 100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 25});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 100, y: 25});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("fromShape is on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 50, width: 50};
            const _mockToShape = {x: 150};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = -50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 50, y: 20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: -70});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 50, y: -70});
        });

        test("fromShape is on the right of toShape", () => {
            // prepare
            const _mockFromShape = {x: 150, width: 50};
            const _mockToShape = {x: 50, width: 50};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);
            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = -50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -50, y: 20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -50, y: -70});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 50, y: -70});
        });

        test("fromShape and toShape overlap in the X-axis direction", () => {
            // prepare
            const _mockFromShape = {x: 80, width: 50};
            const _mockToShape = {x: 50, width: 50};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _mockFromShapeConnector = {x: 25, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_mockFromShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = -50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(4);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 45, y: 20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 45, y: -70});
            expect(_line.brokenPoints[3]).toStrictEqual({x: 50, y: -70});
        });
    });

    describe("NN", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's height is positive", () => {
            // prepare
            _line.width = 100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 100, y: -20});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line's height is negative", () => {
            // prepare
            _line.width = 100;
            _line.height = -50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -70});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 100, y: -70});
        });
    });

    describe("SS", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's height is positive", () => {
            // prepare
            _line.width = 100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 70});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 100, y: 70});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line's height is negative", () => {
            // prepare
            _line.width = 100;
            _line.height = -50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 100, y: 20});
        });
    });

    describe("WW", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's width is positive", () => {
            // prepare
            _line.width = 100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: 50});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line's width is negative", () => {
            // prepare
            _line.width = -100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -120, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -120, y: 50});
        });
    });

    describe("EE", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line's width is positive", () => {
            // prepare
            _line.width = 100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 120, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 120, y: 50});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line's width is negative", () => {
            // prepare
            _line.width = -100;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(2);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: 50});
        });
    });

    describe("SE", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is on the right of toShapeConnector", () => {
            // prepare
            const _mockFromShape = {x: 10, y: 0, width: 0, height: 0};
            const _mockToShape = {x: 0, y: 10, width: 0, height: 0};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 10, y: 0};
            const _toShapeConnector = {x: 0, y: 10};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 50});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is on the left of toShapeConnector, fromShape is above toShape", () => {
            // prepare
            const _mockFromShape = {x: 10, y: 0, width: 0, height: 10};
            const _mockToShape = {x: 10, y: 30, width: 0, height: 0};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 10, y: 0};
            const _toShapeConnector = {x: 10, y: 10};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 10});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 70, y: 10});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: 50});
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is on the left of toShapeConnector, fromShape is not above toShape", () => {
            // prepare
            const _mockFromShape = {x: 10, y: 10, width: 0, height: 30};
            const _mockToShape = {x: 10, y: 30, width: 0, height: 20};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 10, y: 0};
            const _toShapeConnector = {x: 10, y: 10};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 30});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 70, y: 30});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: 50});
        });

        test("toShapeConnector is above fromShapeConnector, fromShape is on the right of toShape", () => {
            // prepare
            const _mockFromShape = {x: 633, y: 327, width: 80, height: 22};
            const _mockToShape = {x: 504, y: 302, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 22};
            const _toShapeConnector = {x: 80, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -64.5, y: 20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -64.5, y: 50});
        });

        test("toShapeConnector is above fromShapeConnector, fromShape is on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 377, y: 305, width: 80, height: 22};
            const _mockToShape = {x: 504, y: 302, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 22};
            const _toShapeConnector = {x: 80, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 70, y: 20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: 50});
        });
    });

    describe("ES", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("toShapeConnector is above fromShapeConnector, fromShapeConnector is on the left of toShapeConnector", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 438, y: 224, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.ES(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("toShapeConnector is above fromShapeConnector, fromShapeConnector is on the right of toShapeConnector, toShape is above fromShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 256, y: 248, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.ES(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: -21.5});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: -21.5});
        });

        test("toShapeConnector is above fromShapeConnector, fromShapeConnector is on the right of toShapeConnector, toShape is not above fromShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 278, y: 252, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.ES(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: 31});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 31});
        });

        test("toShapeConnector is below fromShapeConnector, fromShape is on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 530, y: 338, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.ES(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 26.5, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 26.5, y: 70});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 70});
        });

        test("toShapeConnector is below fromShapeConnector, fromShape is not on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 343, y: 355, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.ES(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: 70});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 70});
        });
    });

    describe("SW", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is on the left of toShapeConnector", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 566, y: 337, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 22};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 50});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is on the right of toShapeConnector, fromShape is above toShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 377, y: 343, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 22};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 15});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 30, y: 15});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 30, y: 50});
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is on the right of toShapeConnector, fromShape is not above toShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 316, y: 310, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 22};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 39});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 30, y: 39});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 30, y: 50});
        });

        test("toShapeConnector is above fromShapeConnector, toShapeConnector is on the right of fromShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 530, y: 238, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 22};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 66.5, y: 20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 66.5, y: 50});
        });

        test("toShapeConnector is above fromShapeConnector, toShapeConnector is not on the right of fromShape", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 290, y: 286, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 22};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.SW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -60, y: 20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -60, y: 50});
        });
    });

    describe("WS", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("toShapeConnector is above fromShapeConnector, fromShapeConnector is on the right of toShapeConnector", () => {
            // prepare
            const _mockFromShape = {x: 397, y: 291, width: 80, height: 22};
            const _mockToShape = {x: 256, y: 228, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("toShapeConnector is above fromShapeConnector, fromShapeConnector is on the left of toShapeConnector, toShape is above fromShape", () => {
            // prepare
            const _mockFromShape = {x: 327, y: 333, width: 80, height: 22};
            const _mockToShape = {x: 485, y: 258, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: -37.5});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: -37.5});
        });

        test("toShapeConnector is above fromShapeConnector, fromShapeConnector is on the left of toShapeConnector, toShape is not above fromShape", () => {
            // prepare
            const _mockFromShape = {x: 327, y: 333, width: 80, height: 22};
            const _mockToShape = {x: 484, y: 317, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: 31});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 31});
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is on the right toShape", () => {
            // prepare
            const _mockFromShape = {x: 327, y: 333, width: 80, height: 22};
            const _mockToShape = {x: 210, y: 388, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -18.5, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -18.5, y: 70});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 70});
        });

        test("toShapeConnector is below fromShapeConnector, fromShapeConnector is not on the right toShape", () => {
            // prepare
            const _mockFromShape = {x: 327, y: 333, width: 80, height: 22};
            const _mockToShape = {x: 450, y: 402, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 22};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WS(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: 100});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 100});
        });
    });

    describe("EN", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("fromShapeConnector is above toShapeConnector, fromShapeConnector is on the left of toShapeConnector", () => {
            // prepare
            const _mockFromShape = {x: 327, y: 333, width: 80, height: 22};
            const _mockToShape = {x: 511, y: 402, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("fromShapeConnector is above toShapeConnector, fromShapeConnector is on the right of toShapeConnector, fromShape is above toShape", () => {
            // prepare
            const _mockFromShape = {x: 327, y: 333, width: 80, height: 22};
            const _mockToShape = {x: 315, y: 414, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: 40.5});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 40.5});
        });

        test("fromShapeConnector is above toShapeConnector, fromShapeConnector is on the right of toShapeConnector, fromShape is not above toShape", () => {
            // prepare
            const _mockFromShape = {x: 324, y: 143, width: 80, height: 22};
            const _mockToShape = {x: 235, y: 168, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: -31});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: -31});
        });

        test("fromShapeConnector is below toShapeConnector, fromShape is on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 324, y: 143, width: 80, height: 22};
            const _mockToShape = {x: 464, y: 117, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 30, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 30, y: 30});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: 30});
        });

        test("fromShapeConnector is below toShapeConnector, fromShape is not on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 324, y: 143, width: 80, height: 22};
            const _mockToShape = {x: 318, y: 90, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 80, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 50;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.EN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 20, y: -31});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 50, y: -31});
        });
    });

    describe("NE", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("fromShapeConnector is below toShapeConnector, fromShapeConnector is on the right of toShapeConnector", () => {
            // prepare
            const _mockFromShape = {x: 312, y: 169, width: 80, height: 22};
            const _mockToShape = {x: 229, y: 107, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 80, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 80;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 80});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("fromShapeConnector is below toShapeConnector, fromShapeConnector is on the left of toShapeConnector, fromShape is below toShape", () => {
            // prepare
            const _mockFromShape = {x: 312, y: 169, width: 80, height: 22};
            const _mockToShape = {x: 373, y: 90, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 80, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 80;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -28.5});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 70, y: -28.5});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: 80});
        });

        test("fromShapeConnector is below toShapeConnector, fromShapeConnector is on the left of toShapeConnector, fromShape is not below toShape", () => {
            // prepare
            const _mockFromShape = {x: 403, y: 211, width: 80, height: 22};
            const _mockToShape = {x: 522, y: 199, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 80, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 80;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -32});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 70, y: -32});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 70, y: 80});
        });

        test("fromShapeConnector is below toShapeConnector, fromShape is on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 403, y: 211, width: 80, height: 22};
            const _mockToShape = {x: 217, y: 293, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 80, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 80;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -93, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -93, y: 80});
        });

        test("fromShapeConnector is below toShapeConnector, fromShape is not on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 403, y: 211, width: 80, height: 22};
            const _mockToShape = {x: 418, y: 309, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 80, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 80;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NE(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 75, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 75, y: 80});
        });
    });

    describe("NW", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("fromShapeConnector is on the left of toShape, toShapeConnector is above fromShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 22};
            const _mockToShape = {x: 495, y: 217, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: 90});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("fromShapeConnector is on the left of toShape, toShapeConnector is not above fromShape, fromShape is on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 22};
            const _mockToShape = {x: 490, y: 322, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 64, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 64, y: 90});
        });

        test("fromShapeConnector is on the left of toShape, toShapeConnector is not above fromShape, fromShape is not on the left of toShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 22};
            const _mockToShape = {x: 417, y: 352, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -60, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -60, y: 90});
        });

        test("fromShapeConnector is not on the left of toShape, fromShape not below toShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 22};
            const _mockToShape = {x: 243, y: 200, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -18.5});
            expect(_line.brokenPoints[1]).toStrictEqual({x: 30, y: -18.5});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 30, y: 90});
        });

        test("fromShapeConnector is not on the left of toShape, fromShape is not below toShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 22};
            const _mockToShape = {x: 255, y: 339, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 40, y: 0};
            const _toShapeConnector = {x: 0, y: 11};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 50;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.NW(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -167, y: -20});
            expect(_line.brokenPoints[2]).toStrictEqual({x: -167, y: 90});
        });
    });

    describe("WN", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("fromShapeConnector is above toShapeConnector, fromShapeConnector is on the right of toShapeConnector", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 22};
            const _mockToShape = {x: 249, y: 374, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 55;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(1);
            expect(_line.brokenPoints[0]).toStrictEqual({x: 55, y: 0});
            expect(_line.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_line.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("fromShapeConnector is above toShapeConnector, fromShapeConnector is not on the right of toShapeConnector, fromShape is above toShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 22};
            const _mockToShape = {x: 361, y: 368, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 55;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: 54.5});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 55, y: 54.5});
        });

        test("fromShapeConnector is above toShapeConnector, fromShapeConnector is not on the right of toShapeConnector, fromShape is not above toShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 40};
            const _mockToShape = {x: 479, y: 285, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 55;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: -40});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 55, y: -40});
        });

        test("fromShapeConnector is not above toShapeConnector, fromShapeConnector is on the right of toShape", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 40};
            const _mockToShape = {x: 239, y: 195, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 55;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -21.5, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -21.5, y: 70});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 55, y: 70});
        });

        test("fromShapeConnector is not above toShapeConnector, fromShapeConnector is on the left of toShape's right border", () => {
            // prepare
            const _mockFromShape = {x: 362, y: 259, width: 80, height: 40};
            const _mockToShape = {x: 507, y: 240, width: 80, height: 22};
            jest.spyOn(_line, "getFromShape").mockReturnValue(_mockFromShape);
            jest.spyOn(_line, "getToShape").mockReturnValue(_mockToShape);

            const _fromShapeConnector = {x: 0, y: 11};
            const _toShapeConnector = {x: 40, y: 0};
            jest.spyOn(_line, "getFromShapeConnector").mockReturnValue(_fromShapeConnector);
            jest.spyOn(_line, "getToShapeConnector").mockReturnValue(_toShapeConnector);

            _line.fromShape = "_fromShape";
            _line.toShape = "_toShape";

            _line.width = 55;
            _line.height = 90;
            _line.brokenPoints = [];

            // when
            _lineHelper.brokenLineHelper.WN(_line);

            // then
            expect(_line.brokenPoints.length).toBe(3);
            expect(_line.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_line.brokenPoints[1]).toStrictEqual({x: -20, y: -59});
            expect(_line.brokenPoints[2]).toStrictEqual({x: 55, y: -59});
        });
    });

    describe("E", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line.fromShapeConnector connect to east connector of shape, line is on the right of shape, line width is larger than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        })

        test("line.fromShapeConnector connect to east connector of shape, line is on the right of shape, line width is smaller than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.fromShapeConnector connect to east connector of shape, line is on the right of shape, line width is smaller than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.fromShapeConnector connect to east connector of shape, line is on the left of shape, line width is larger than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 20, y: 60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.fromShapeConnector connect to east connector of shape, line is on the left of shape, line width is larger than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 20, y: -60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 100, y: -60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.fromShapeConnector connect to east connector of shape, line is on the left of shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 20, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 20, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        })

        test("line.toShapeConnector connect to east connector of shape, line is on the right of shape, line width is larger than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.toShapeConnector connect to east connector of shape, line is on the right of shape, line width is smaller than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.toShapeConnector connect to east connector of shape, line is on the right of shape, line width is smaller than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: -120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.toShapeConnector connect to east connector of shape, line is on the left of shape, line width is larger than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 120, y: 60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 120, y: 120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.toShapeConnector connect to east connector of shape, line is on the left of shape, line width is larger than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: -60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 120, y: -60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 120, y: -120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.toShapeConnector connect to east connector of shape, line is on the left of shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.E(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 120, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 120, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        })
    });

    describe("N", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line.fromShapeConnector connect to north connector of shape, line is above of shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.fromShapeConnector connect to north connector of shape, line is above of shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, -100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.fromShapeConnector connect to north connector of shape, line is above of shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.fromShapeConnector connect to north connector of shape, line is under the shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: -20});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.fromShapeConnector connect to north connector of shape, line is under the shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: -50, y: -20});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: -50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.fromShapeConnector connect to north connector of shape, line is under the shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: -20});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: -20});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.toShapeConnector connect to north connector of shape, line is above of shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.toShapeConnector connect to north connector of shape, line is above of shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: -100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.toShapeConnector connect to north connector of shape, line is  above of shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 300, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.toShapeConnector connect to north connector of shape, line is under the shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: 60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.toShapeConnector connect to north connector of shape, line is under the shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: -50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: -50, y: 60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: -100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.toShapeConnector connect to north connector of shape, line is under the shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.N(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 100});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: 100});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });
    });

    describe("S", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line.fromShapeConnector connect to south connector of shape, line is above of shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: 20});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.fromShapeConnector connect to south connector of shape, line is above of shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: -50, y: 20});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: -50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.fromShapeConnector connect to south connector of shape, line is above of shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 20});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: 20});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.fromShapeConnector connect to south connector of shape, line is under the shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.fromShapeConnector connect to south connector of shape, line is under the shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.fromShapeConnector connect to south connector of shape, line is under the shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.toShapeConnector connect to south connector of shape, line is above of shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: 100});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 100, y: 100});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.toShapeConnector connect to south connector of shape, line is above of shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 200, 100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: -50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: -50, y: 100});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: -100, y: 100});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.toShapeConnector connect to south connector of shape, line is  above of shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 300, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 500, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 140});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: 140});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.toShapeConnector connect to south connector of shape, line is under the shape, line width is larger than height, line width is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.toShapeConnector connect to south connector of shape, line is under the shape, line width is larger than height, line width is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.width = -100;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: -100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.toShapeConnector connect to south connector of shape, line is under the shape, line width is smaller than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {y: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.S(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });
    });

    describe("W", () => {
        afterEach(() => {
            jest.restoreAllMocks();
        });

        test("line.fromShapeConnector connect to west connector of shape, line is on the right of shape, line width is larger than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: -20, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.fromShapeConnector connect to west connector of shape, line is on the right of shape, line width is smaller than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: -20, y: 60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 100, y: 60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.fromShapeConnector connect to west connector of shape, line is on the right of shape, line width is smaller than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: -20, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: -20, y: -60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 100, y: -60});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.fromShapeConnector connect to west connector of shape, line is on the left of shape, line width is larger than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.E);
        });

        test("line.fromShapeConnector connect to west connector of shape, line is on the left of shape, line width is smaller than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.N);
        });

        test("line.fromShapeConnector connect to west connector of shape, line is on the left of shape, line width is smaller than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.fromShapeConnector = {x: 10, y: 10};
            _mockLine.toConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getFromShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 100, y: 0});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.S);
        });

        test("line.toShapeConnector connect to west connector of shape, line is on the right of shape, line width is larger than height", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 80, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 80, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.W);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.toShapeConnector connect to west connector of shape, line is on the right of shape, line width is smaller than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 80, y: 60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 80, y: 120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.toShapeConnector connect to west connector of shape, line is on the right of shape, line width is smaller than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 800, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 50, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(3);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: -60});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 80, y: -60});
            expect(_mockLine.brokenPoints[2]).toStrictEqual({x: 80, y: -120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.toShapeConnector connect to west connector of shape, line is on the left of shape, line width is larger than height", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(2);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 50, y: 0});
            expect(_mockLine.brokenPoints[1]).toStrictEqual({x: 50, y: 80});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.E);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.toShapeConnector connect to west connector of shape, line is on the left of shape, line width is smaller than height, line height is positive", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = 120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: 120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.S);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });

        test("line.toShapeConnector connect to west connector of shape, line is on the left of shape, line width is smaller than height, line height is negative", () => {
            // prepare
            const _mockLine = line("test-line", 200, 800, 100, 80, _page, _drawer);
            _mockLine.height = -120;
            _mockLine.toShapeConnector = {x: 10, y: 10};
            _mockLine.fromConnector = {x: 50, y: 50};

            const _shape = {x: 300, width: 50}
            jest.spyOn(_mockLine, "getToShape").mockReturnValue(_shape);

            // when
            _lineHelper.brokenLineHelper.W(_mockLine);

            // then
            expect(_mockLine.brokenPoints.length).toBe(1);
            expect(_mockLine.brokenPoints[0]).toStrictEqual({x: 0, y: -120});
            expect(_mockLine.arrowBeginPoint.direction).toBe(DIRECTION.N);
            expect(_mockLine.arrowEndPoint.direction).toBe(DIRECTION.W);
        });
    });
});