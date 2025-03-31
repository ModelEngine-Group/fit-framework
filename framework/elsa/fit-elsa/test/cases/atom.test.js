/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Atom} from "../../core/atom";
import {PAGE_MODE} from "../../common/const";
import {MODE_MANAGER} from "../../common/mode/modeManager";

const testMethod = () => {
    return 1;
};

const testShape = (page) => {
    const self = new Atom();
    self.type = "shape";
    self.page = page;
    self.doSomething = testMethod;
    return self;
};

const testRectangle = (page) => {
    const self = testShape(page);
    self.type = "rectangle";
    return self;
};

describe("Set test cases", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("ModeManager#when value is function but not exist in mode manager", () => {
        const t = testShape({mode: PAGE_MODE.CONFIGURATION});
        expect(t.doSomething()).toStrictEqual(1);
    });

    test("ModeManager#when value is function and exist in mode manager", () => {
        MODE_MANAGER[PAGE_MODE.CONFIGURATION].overrideMethods["shape"]["doSomething"] = () => {
            return 2;
        };
        const t = testShape({mode: PAGE_MODE.CONFIGURATION});
        expect(t.doSomething()).toStrictEqual(2);
    });

    test("ModeManager#when value is type then override all mode manager methods", () => {
        MODE_MANAGER[PAGE_MODE.CONFIGURATION].overrideMethods["rectangle"] = {
            "doSomething": () => {
                return 3;
            }
        };
        const t = testRectangle({mode: PAGE_MODE.CONFIGURATION});
        expect(t.doSomething()).toStrictEqual(3);
    });
});