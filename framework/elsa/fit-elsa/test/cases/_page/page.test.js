/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {page} from "../../../core/page.js";
import {EVENT_TYPE, PAGE_MODE} from "../../../common/const";
import {uuid} from "../../../common/util";
import 'core-js/stable';

const _getContext = () => {
    return {
        canvas: {
            style: {
                x: 200,
                y: 200
            }
        },
        scale: () => {
        },
        clearRect: () => {
        },
        beginPath: () => {
        },
        fillStyle: {},
        rect: () => {
        },
        fill: () => {
        },
    }
}

const _createZoom = () => {
    return {
        zoom: {}
    }
}

const _createDom = () => {
    return {
        style: {},
        classList: new Set(),
        appendChild: () => {
        },
        contains: () => {
            return true;
        },
        getContext: () => {
            return _getContext();
        },
        parentNode: {
            appendChild: () => {
            },
            getComputedStyle: () => {
                return _createZoom();
            }
        },
        getComputedStyle: () => {
            return _createZoom();
        }
    };
};

const _div = _createDom();

const _graph = {
    createDom: () => {
        return _createDom();
    },
    setting: {
        borderColor: "red"
    },
    newPageMode: PAGE_MODE.CONFIGURATION,
    uuid: () => uuid()
};

const _iDrawer = (graph, page, div) => {
    return {
        cursor: {}
    }
};

const _pDrawer = (page, div, x, y) => {
    return {
        cursor: {}
    }
};

const _aDrawer = (graph, page, div) => {
    return window;
};

const _position = {
    x: 100,
    y: 100,
}

const _page = page(_div, _graph, "test-page", "test-page", _iDrawer, _pDrawer, _aDrawer);

_page.keyActions = {
    attachCopyPaste: () => {
    }
}

describe("On Mouse Down", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    function initMouseDownShape() {
        _page.mousedownShape = {
            x: 0,
            y: 0,
            mouseOffsetX: 80,
            mouseOffsetY: 30,
            onReturnDrag: (_x, _offsetX, _y, _offsetY) => {
                _page.mousedownShape.x = _x - _offsetX;
                _page.mousedownShape.y = _y - _offsetY;
            }
        };
    }

    function clearMouseDownShape() {
        _page.mousedownShape = null;
    }

    test("mouse down when mouse leave and mouse down before mouse in.", () => {
        initMouseDownShape();
        _page.mouseLeave();
        expect(document.body.style.userSelect).toBe('none');
        _page.mouseIn(_position);
        expect(_page.mousedownShape).not.toBeNull();
        expect(_page.mousedownShape.x).toBe(20);
        expect(_page.mousedownShape.y).toBe(70);
        clearMouseDownShape();
    });

    test("mouse up when mouse leave and mouse down before mouse in.", () => {
        document.body.style.userSelect = null;
        _page.mouseLeave();
        expect(document.body.style.userSelect).toBe("");
        _page.mouseIn(_position);
        expect(_page.mousedownShape).toBeNull();
    });

    test("mouse up when mouse leave and mouse up before mouse in.", () => {
        document.body.style.userSelect = null;
        _page.mouseLeave();
        expect(document.body.style.userSelect).toBe("");
        _page.mouseIn(_position);
        expect(_page.mousedownShape).toBeNull();
    });
});

describe("Event listener", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("test change value when testDataChange event triggered", async () => {
        let testData = {value: 1};
        _page.addEventListener("testDataChange", (data) => {
            data.value = 10;
        });

        _page.triggerEvent({type: "testDataChange", value: testData});
        // 把_page.graph.fireGraphEvent mock掉
        _page.graph.fireEvent = jest.fn();
        await _page.fireEvent();
        expect(_page.graph.fireEvent).toBeCalled();
        expect(_page.graph.fireEvent).toBeCalledTimes(1);
        expect(testData.value).toEqual(10);
    });


    test("test change value when PAGE_HISTORY event triggered many times, but only one events", async () => {
        let testData = {
            page: _page.id, undo: true, redo: false
        };
        _page.addEventListener(EVENT_TYPE.PAGE_HISTORY, (data) => {
            data.redo = true;
        });

        _page.triggerEvent({
            type: EVENT_TYPE.PAGE_HISTORY, value: testData
        });

        _page.triggerEvent({
            type: EVENT_TYPE.PAGE_HISTORY, value: testData
        });

        _page.triggerEvent({
            type: EVENT_TYPE.PAGE_HISTORY, value: testData
        });


        // 把_page.graph.fireGraphEvent mock掉
        _page.graph.fireEvent = jest.fn();
        await _page.fireEvent();
        expect(_page.graph.fireEvent).toBeCalled();
        expect(_page.graph.fireEvent).toBeCalledTimes(1);
        expect(_page.graph.fireEvent).toHaveBeenCalledWith({
            type: EVENT_TYPE.PAGE_HISTORY, value: testData
        });
        expect(testData.redo).toEqual(true);
    });

    test("test change value when PAGE_HISTORY event triggered many times, and append a FOCUSED_SHAPE_CHANGE event", async () => {
        _page.addEventListener(EVENT_TYPE.FOCUSED_SHAPE_CHANGE, (data) => {
            data.redo = true;
        });

        _page.triggerEvent({
            type: EVENT_TYPE.FOCUSED_SHAPE_CHANGE, value: []
        });

        _page.triggerEvent({
            type: EVENT_TYPE.FOCUSED_SHAPE_CHANGE, value: []
        });

        _page.triggerEvent({
            type: EVENT_TYPE.FOCUSED_SHAPE_CHANGE, value: []
        });


        // 把_page.graph.fireGraphEvent mock掉
        _page.graph.fireEvent = jest.fn();
        await _page.fireEvent();
        expect(_page.graph.fireEvent).toBeCalled();
        expect(_page.graph.fireEvent).toBeCalledTimes(4);

        expect(_page.graph.fireEvent).toHaveBeenLastCalledWith({
            type: EVENT_TYPE.FOCUSED_SHAPES_CHANGE,
            value: []
        });
    });
});