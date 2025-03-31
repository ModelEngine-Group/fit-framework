/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import 'core-js/stable';
import {jadeNode} from "../../../plugins/flowable/jadeNodes/jadeNode.js";
import {line} from "../../../core/line.js";
import {PAGE_MODE} from "../../../common/const.js";
import "../../../common/extensions/collectionExtension";
import "../../../common/extensions/arrayExtension";

let _page;
let _drawer;
let _jadeNode;

describe("getFormerNodesInfo", () => {
    beforeEach(() => {
        _page = {
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
            },
            getShapeById: function (id) {
                return _page.sm.getShapeById(id);
            }
        };
        _page.page = _page;
        _drawer = jest.fn(() => {
        });
        _jadeNode = jadeNode("test-jade-node", 0, 0, 100, 100, _page, _drawer);
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("no former node", () => {
        // prepare

        // when
        let actual = _jadeNode.getPreNodesInfo();

        // then
        expect(actual).toBeDefined();
        expect(Object.keys(actual).length).toBe(0);
    });

    test("has former nodes", () => {
        // prepare
        const _jadeNode1_1 = jadeNode("test-jade-node1-1", 0, 0, 100, 100, _page, _drawer);
        _jadeNode1_1.name = "jade-node1-1";
        jest.spyOn(_jadeNode1_1, "getParams").mockImplementationOnce(() => ["jade-node1-1-info"]);
        const _jadeNode1_2 = jadeNode("test-jade-node1-2", 0, 0, 100, 100, _page, _drawer);
        _jadeNode1_2.name = "jade-node1-2";
        jest.spyOn(_jadeNode1_2, "getParams").mockImplementationOnce(() => ["jade-node1-2-info"]);
        const _jadeNode2_1 = jadeNode("test-jade-node2-1", 0, 0, 100, 100, _page, _drawer);
        _jadeNode2_1.name = "jade-node2-1";
        jest.spyOn(_jadeNode2_1, "getParams").mockImplementationOnce(() => ["jade-node2-1-info"]);
        const _jadeNode2_2 = jadeNode("test-jade-node2-2", 0, 0, 100, 100, _page, _drawer);
        _jadeNode2_2.name = "jade-node2-2";
        jest.spyOn(_jadeNode2_2, "getParams").mockImplementationOnce(() => ["jade-node2-2-info"]);
        const _jadeNode2_3 = jadeNode("test-jade-node2-3", 0, 0, 100, 100, _page, _drawer);
        _jadeNode2_3.name = "jade-node2-3";
        jest.spyOn(_jadeNode2_3, "getParams").mockImplementationOnce(() => ["jade-node2-3-info"]);

        const _line1_1_2 = line("test-line-1-1-2", 0, 0, 100, 80, _page, _drawer);
        _line1_1_2.fromShape = "test-jade-node1-1";
        _line1_1_2.toShape = "test-jade-node1-2";
        const _line1_2_node = line("test-line-1-2-node", 0, 0, 100, 80, _page, _drawer);
        _line1_2_node.fromShape = "test-jade-node1-2";
        _line1_2_node.toShape = "test-jade-node";
        const _line2_1_2 = line("test-line-2-1-2", 0, 0, 100, 80, _page, _drawer);
        _line2_1_2.fromShape = "test-jade-node2-1";
        _line2_1_2.toShape = "test-jade-node2-2";
        const _line2_2_node = line("test-line-2-2-node", 0, 0, 100, 80, _page, _drawer);
        _line2_2_node.fromShape = "test-jade-node2-2";
        _line2_2_node.toShape = "test-jade-node";
        const _line2_2_3 = line("test-line-2-2-3", 0, 0, 100, 80, _page, _drawer);
        _line2_2_3.fromShape = "test-jade-node2-2";
        _line2_2_3.toShape = "test-jade-node2-3";
        // when
        let actual = _jadeNode.getPreNodesInfo();

        // then
        expect(actual).toBeDefined();
        expect(Object.keys(actual).length).toBe(4);
        expect(actual[0].name).toBe("jade-node1-2");
        expect(actual[0].params.length).toBe(1);
        expect(actual[0].params[0]).toBe("jade-node1-2-info");
        expect(actual[1].name).toBe("jade-node1-1");
        expect(actual[1].params.length).toBe(1);
        expect(actual[1].params[0]).toBe("jade-node1-1-info");
        expect(actual[2].name).toBe("jade-node2-2");
        expect(actual[2].params.length).toBe(1);
        expect(actual[2].params[0]).toBe("jade-node2-2-info");
        expect(actual[3].name).toBe("jade-node2-1");
        expect(actual[3].params.length).toBe(1);
        expect(actual[3].params[0]).toBe("jade-node2-1-info");
    });
});