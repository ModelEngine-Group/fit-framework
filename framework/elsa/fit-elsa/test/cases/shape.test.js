/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {shape} from "../../core/shape.js";
import {CURSORS, PAGE_MODE} from "../../common/const.js";
import {menu} from "../../core/popupMenu.js";
import "../../common/extensions/arrayExtension.js";
import {container} from "../../core/container.js";
import "../../common/extensions/collectionExtension.js";

const UUID = "123456";
const PARENT_UUID = "111111";

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

const _parentDrawer = jest.fn(() => {
});

const _graph = {
    createDom: () => {
        return {
            style: {}
        };
    },
    setting: {
        borderColor: "red"
    }
};

// mock shape's parent.
const _parent = container(PARENT_UUID, 30, 40, 300, 400, _page, _parentDrawer);
_parent.page = _page;
_parent.graph = _graph;

// mock drawer.Return an empty obj, add properties or methods when necessary.
const _drawer = jest.fn(() => {
});
const _shape = shape(UUID, 10, 20, 100, 200, _parent, _drawer);

// mock connector
const _connector = {
    release: async function () {
    }
};

/**
 * Base properties test cases.
 */
test("Basic properties", () => {
    expect(_shape.id).toBe(UUID);
    expect(_shape.type).toBe("shape");
    expect(_shape.typeChain).toStrictEqual({parent: null, type: "shape"});
    expect(_shape.text).toBe("");
    expect(_shape.namespace).toBe("elsa");
    expect(_shape.serializable).toBeTruthy();
    expect(_shape.defaultWidth).toEqual(100);
    expect(_shape.defaultHeight).toEqual(100);
    expect(_shape.shareAble).toBeFalsy();
    expect(_shape.needLevitation).toBeTruthy();
    expect(_shape.x).toEqual(10);
    expect(_shape.y).toEqual(20);
    expect(_shape.width).toEqual(100);
    expect(_shape.height).toEqual(200);
    expect(_shape.selectedX).toEqual(0);
    expect(_shape.selectedY).toEqual(0);
    expect(_shape.isFocused).toBeFalsy();
    expect(_shape.linking).toBeFalsy();
    expect(_shape.linkingConnector).toBeNull();
    expect(_shape.cursor).toBe(CURSORS.MOVE);
    expect(_shape.allowTraced).toBeTruthy();
    expect(_shape.isAutoSize).toBeTruthy();
    expect(_shape.allowClickRun).toBeFalsy();
    expect(_shape.allowMultiLineEdit).toBeFalsy();
    expect(_shape.mousedownConnector).toBeNull();
    expect(_shape.bold).toBeFalsy();
    expect(_shape.italic).toBeFalsy();
    expect(_shape.allowCoEdit).toBeUndefined();
    expect(_shape.dirty).toBeFalsy();
    expect(_shape.connectors).toBeUndefined();
    expect(_shape.regions).toStrictEqual([]);

    expect(_shape.page).toStrictEqual(_page);
    expect(_shape.pageId).toEqual(_page.id);
    expect(_shape.container).toEqual(PARENT_UUID);
});

test("Page Shapes", () => {
    expect(_page.sm.getShapeCount()).toBe(2);
});

/**
 * Basic functionality test cases.
 */
describe("Basic Functionality", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("test isType", () => {
        expect(_shape.isType("shape")).toBeTruthy();
        expect(_shape.isType("rectangle")).toBeFalsy();
    });

    test("test isTypeOf", () => {
        expect(_shape.isTypeof("shape")).toBeTruthy();
        expect(_shape.isTypeof("rectangle")).toBeFalsy();
    });

    /* -------------- test method sharedParent -------------- */
    test("sharedParent#when shape is removed", () => {
        // when shape is removed, then shared parent should be {id: ""}
        // * 注意 * spyOn don't work on property without getter, thereby set the property manually for mock purpose.
        const originContainer = _shape.container;
        _shape.container = "";
        expect(_shape.sharedParent()).toStrictEqual({id: ""});
        _shape.container = originContainer;
    });

    test("sharedParent#when shape is page", () => {
        jest.spyOn(_shape, "isTypeof").mockImplementationOnce((type) => true);
        expect(_shape.sharedParent()).toStrictEqual({id: ""});
    });

    test("sharedParent#when shape has parent and parent is shared", () => {
        jest.spyOn(_shape, "getContainer").mockReturnValue(_parent);
        let parentSharedStatus = _parent.shared;
        _parent.shared = true;
        expect(_shape.sharedParent()).toBe(_parent);
        _parent.shared = parentSharedStatus;
    });

    test("sharedParent#when shape has parent and parent is not shared and parent is page", () => {
        jest.spyOn(_shape, "getContainer").mockReturnValue(_parent);
        jest.spyOn(_parent, "isTypeof").mockReturnValueOnce(true);
        const parentSharedStatus = _parent.shared;
        _parent.shared = false;
        expect(_shape.sharedParent()).toStrictEqual({id: ""});
        _parent.shared = parentSharedStatus;
    });
});

/**
 * Mouse event functionality test cases.
 */
describe("Mouse Event Functionality", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    /* -------------- test method onMouseUp -------------- */
    test("onMouseUp#Async#when shape.mousedownConnector is null and shape is not in dragging", async () => {
        jest.spyOn(_shape, "get").mockReturnValue(undefined);
        await _shape.onMouseUp();
        expect(_page.cancelClick).toBeUndefined();
        expect(_shape.inDragging).toBeUndefined();
    });

    test("onMouseUp#Async#when shape.mousedownConnector is not null", async () => {
        jest.spyOn(_shape, "get").mockReturnValue(undefined);
        const releaseMock = jest.fn();
        jest.spyOn(_connector, "release").mockImplementationOnce(releaseMock);
        _shape.mousedownConnector = _connector;

        // invoke.
        await _shape.onMouseUp();

        // assets
        expect(_page.cancelClick).toBeTruthy();
        expect(_shape.mousedownConnector).toBeNull();

        // Method of connector is called once in method onMouseUp.
        expect(releaseMock).toHaveBeenCalled();
    });

    test("onMouseUp#Async#when shape is in dragging", async () => {
        // mocks.
        jest.spyOn(_shape, "get").mockReturnValue(undefined);
        const restMockFunc = jest.fn();
        jest.spyOn(_shape, "reset").mockImplementationOnce(restMockFunc);
        const findMockFunc = jest.fn();
        jest.spyOn(_page, "find").mockImplementationOnce(findMockFunc);
        jest.spyOn(_shape, "getContainer").mockReturnValue(_page);

        _shape.inDragging = true;
        const position = {x: 0, y: 0};
        await _shape.onMouseUp(position);

        expect(_shape.inDragging).toBeFalsy();
        expect(restMockFunc).toHaveBeenCalled();
        expect(findMockFunc).toHaveBeenCalled();
    });

    test("resizeOrRotate#focused shapes contains child and parent", () => {
        // prepare
        const position = {x: 10, y: 10};
        const focusedShapes = [];

        const parent = {
            connectors: [{
                isType: () => {
                    return true;
                },
                dragable: true,
                onMouseDrag: jest.fn(() => {})
            }],
            x: 50,
            y: 60,
            width: 100,
            height: 80,
            getContainer: () => {
                return _page
            }
        };

        const child = {
            connectors: [{
                isType: () => {
                    return true;
                },
                dragable: true,
                onMouseDrag: jest.fn(() => {})
            }],
            x: 60,
            y: 70,
            width: 60,
            height: 40,
            getContainer: () => {
                return parent;
            }
        };

        focusedShapes.push(parent, child);

        _shape.mousedownConnector = {
            type: "",
            direction: {
                cursor: "ew-resize"
            }
        };

        // when
        _shape.resizeOrRotate(position, focusedShapes);

        // then
        expect(_shape.page.cursor).toStrictEqual("ew-resize");
        expect(parent.connectors[0].onMouseDrag).toHaveBeenCalled();
        expect(child.connectors[0].onMouseDrag).toHaveBeenCalledTimes(0);
    });
});

/**
 * Menu functionality test case.
 */
describe("Menu functionality test case", () => {

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("menu childAllowed test case", () => {
        jest.spyOn(_shape, "get").mockReturnValue(undefined);
        jest.spyOn(_page, "indexOf").mockReturnValue(1);
        const _menuScript = [{
            text: "条件节点", action: (shape, x, y) => {
            }, draw: (context) => {
            }, drawer: () => {
                return {};
            }
        }];
        const _menu = menu(_shape, _menuScript, 120, 120, 0, () => {
            return {};
        });
        let oldShapeType = _shape.type;

        // _shape类型不是menuItem返回false
        expect(_menu.childAllowed(_shape)).toBeFalsy();
        _shape.type = "menuItem";
        expect(_menu.childAllowed(_shape)).toBeTruthy();
        _shape.type = oldShapeType;
    })
});

/**
 * On Mouse Down test case.
 */
describe("On Mouse Down", () => {
    const _position = {
        x: 100,
        y: 100,
        e: {
            shiftKey: 1,
            ctrlKey: 1,
            preventDefault: () => {
            }
        }
    }

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("record mouseOffsetX and mouseOffsetY.", () => {
        _shape.onMouseDown(_position);
        expect(_shape.mouseOffsetX).toBe(90);
        expect(_shape.mouseOffsetY).toBe(80);
    });
});