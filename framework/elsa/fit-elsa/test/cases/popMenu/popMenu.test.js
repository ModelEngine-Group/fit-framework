/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import "../../../common/extensions/arrayextension.js";
import {boundOfShapes} from "../../../core/popupMenu.js";


describe("popupMenu test cases", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });
    test("popupMenu#calculate frame of four shapes", () => {
        let shapes = [];
        shapes[0] = {x: 305, y: 369, width: 100, height: 50};
        shapes[1] = {x: 827, y: 402, width: 100, height: 80};
        shapes[2] = {x: 355, y: 363, width: 815, height: 107};
        shapes[3] = {x: 1176, y: 420, width: 100, height: 100};
        const t = boundOfShapes(shapes);
        expect(t).toStrictEqual({x: 297, y: 355, width: 987, height: 173});
    });

    test("popupMenu#calculate frame of one shape", () => {
        let shapes = [];
        shapes[0] = {x: 200, y: 200, width: 100, height: 100};
        const t = boundOfShapes(shapes);
        expect(t).toStrictEqual({x: 192, y: 192, width: 116, height: 116});
    });

});