/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const pluginMeta = (() => {
    const plugins = {};
    const self = {};
    self.add = (path, shapes) => {
        plugins[path] = shapes;
    };
    self.exists = (shape, graph) => {
        return graph.plugins[shape] !== undefined;
    }
    self.import = async (shape, graph) => {
        if (self.exists(shape, graph)) {
            return;
        }
        for (let p in plugins) {
            if (plugins[p].contains(s => s === shape)) {
                await graph.dynamicImport(p);
            }
        }
    };
    self.importBatch = async (shapes, graph) => {
        shapes.forEach(async s => {
            await self.import(s, graph);
        })
    }
    return self;
})();

// 需要持久化
pluginMeta.add("../plugins/flowable/nodes/node.js", ["flowable"]);
pluginMeta.add("../plugins/flowable/nodes/attachNodes.js", ["linker"]);
pluginMeta.add("../plugins/flowable/nodes/event.js", ["event"]);
pluginMeta.add("../plugins/flowable/aippNodes/aippEvent.js", ["aippEvent"]);
pluginMeta.add("../plugins/flowable/nodes/subflow.js", ["crossSender"]);
pluginMeta.add("../plugins/flowable/nodes/startEnd.js", ["start", "end"]);
pluginMeta.add("../plugins/flowable/aippNodes/aippStartEnd.js", ["aippStart", "aippEnd"]);
pluginMeta.add("../plugins/flowable/nodes/state.js", ["state"]);
pluginMeta.add("../plugins/flowable/nodes/parallel.js", ["parallel"]);
pluginMeta.add("../plugins/flowable/nodes/condition.js", ["condition"]);
pluginMeta.add("../plugins/mind/mind.js", ["mind"]);
pluginMeta.add("../plugins/mind/topic.js", ["topic"]);
pluginMeta.add("../plugins/mind/subTopic.js", ["subTopic"]);
pluginMeta.add("../plugins/dynamicForm/form.js", ["htmlDiv", "form", "htmlTable", "htmlLabel", "htmlText", "htmlInput", "htmlHr", "htmlComobox",
    "htmlRadioBox", "htmlListBox", "htmlCheckBox", "tab", "tabPage", "tree", "treeNode"]);
pluginMeta.add("../plugins/officeCommon/freeShape.js", ["freeShape"]);


export {pluginMeta};