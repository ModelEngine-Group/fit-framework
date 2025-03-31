/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {state} from '../nodes/state.js';
import {CURSORS, EVENT_TYPE, FONT_WEIGHT} from "../../../common/const.js";
import {contextMenu} from "../../../core/popupMenu.js";
import {nameConfiguration} from "../configurations/nameConfiguration.js";
import {descriptionConfiguration} from "../configurations/descriptionConfiguration.js";
import {inputFormConfiguration} from "../configurations/inputFormConfiguration.js";
import {BASE_EVENT_LENGTH} from "../common/const.js";
import {deleteFlowableNodeCommand} from "../commands/deleteFlowableNodeCommand.js";
import {agentRegion} from "../hitregions/hitregion.js";
import {promptConfiguration} from "../configurations/promptConfiguration.js";
import {codeConfiguration} from "../configurations/codeConfiguration.js";
import {logEnableConfiguration} from "../configurations/logEnableConfiguration.js";
import {aippSelectionConfiguration} from "../configurations/aippSelectionConfiguration.js";

/**
 * flowable通用数据处理节点
 * 辉子 2020
 */
const aippState = (id, x, y, width, height, parent, drawer) => {
    let self = state(id, x, y, width, height, parent, drawer);
    self.type = "aippState";
    self.moveable = false;
    self.pad = 14;
    self.fontSize = 14;
    self.padTop = self.padBottom = 14;
    self.fontColor = "#333";
    self.fontWeight = FONT_WEIGHT.NORMAL;
    self.borderWidth = 1;
    self.shadow = true;
    self.borderColor = "#D7D9E7";
    self.backColor = "linear-gradient(#edf6ff, #f7fcff)";
    self.shadowColor = "#eee"
    self.cornerRadius = 12;
    self.modeRegion.visible = false;

    self.agentRegion = agentRegion(self, s => 4, s => -20, () => 40, () => 20, 20);
    self.agentRegion.visible = false;
    /**
     * 获取节点配置.
     *
     * @override
     */
    self.getConfigurations = () => {
        const configs = [];
        configs.push(nameConfiguration(self, "text"));
        configs.push(descriptionConfiguration(self, "description"));
        configs.push(inputFormConfiguration(self, "formConfig"));
        if (self.tags && self.tags.includes('prompt')) {
            configs.push(promptConfiguration(self, "prompt"));
            configs.push(logEnableConfiguration(self, "isLogEnabled"));
        }
        if (self.tags && self.tags.includes('code')) {
            configs.push(codeConfiguration(self, "code"));
        }
        if (self.tags && self.tags.includes('agent')) {
            configs.push(aippSelectionConfiguration(self, "aippId"));
            configs.push(logEnableConfiguration(self, "isLogEnabled"));
        }
        return configs;
    };

    self.setFlowableContext = (context) => {
        self.text = context.name;
        delete context.name;
        for (const [key, value] of Object.entries(context)) {
            self[key] = value;
        }
        if (context.tags && context.tags.includes('agent')) {
            self.agentRegion.visible = self.isAgent = true;
        }
        return self;
    };

    /*
     * 编辑结束后重新将节点居中
     */
    const edited = self.edited;
    self.edited = editor => {
        edited.apply(self, [editor]);
        self.recenter();
    };

    /*
      * 获取任一事件的x坐标，此坐标必定为中轴线坐标。将节点的对称轴设置为此值
     */
    self.recenter = () => {
        self.resize();
        const centerX = self.page.shapes.find(s => s.type === 'aippEvent').x;
        self.moveTo(centerX - self.width / 2, self.y);
    };

    /**
     * 获取state对应的contextMenu列表.
     *
     * @override
     */
    self.getContextMenuScript = () => {
        return {
            location: "top",
            getOffset: () => -50,
            menus: [
                {
                    type: "icon",
                    name: "updateType",
                    text: "修改类型",
                    group: "base",
                    showText: true,
                    onClick: function (shapes) {
                        self.page.triggerEvent({type: EVENT_TYPE.FLOWABLE_STATE_TYPE_CHANGE, value: [shapes[0]]});
                    }
                },
                {
                    type: "icon",
                    name: "copy",
                    text: "复制",
                    group: "base",
                    showText: true,
                    onClick: function (shapes) {
                        const page = self.page;
                        // 允许复制粘贴新状态
                        self.moveable = true;
                        const event = new KeyboardEvent('keydown', {
                            ctrlKey: true,
                            keyCode: 68,
                            code: "KeyD"
                        });
                        document.dispatchEvent(event);
                        const newState = page.shapes[page.shapes.length - 1];
                        // 选择当前状态的流出事件
                        const rawEvent = page.shapes.find(s => s.type === 'aippEvent' && s.fromShape === self.id);

                        newState.moveTo(self.x, self.y + BASE_EVENT_LENGTH + newState.height);
                        newState.agentRegion.visible = newState.isAgent = self.isAgent;

                        // 在当前状态和复制状态中间插入新事件
                        page.createNew("aippEvent", 0, 0)
                            .enableNodeInsertion()
                            .connectFrom(newState.id, "S")
                            .connectTo(rawEvent.toShape, "N");

                        // 重定向流出事件
                        rawEvent.connectTo(newState.id, "N");

                        // 由于插入了新的state，在新state下方的其他state需要向下平移
                        self.page.shapes.filter(s => s !== newState && (s.type === "aippEnd" || s.type === "aippState") && s.y > newState.y - 30)
                            .map(s => s.moveTo(s.x, s.y + BASE_EVENT_LENGTH + newState.height));

                        // 刷新所有event
                        self.page.shapes.filter(s => s.type === "aippEvent").map(s => s.follow());

                        // 删除上下文菜单
                        page.shapes.filter(s => s.isType('contextMenu')).forEach(s => s.remove());

                        // 禁止状态被移动
                        self.moveable = false;
                        newState.moveable = false;

                        // 刷新页面
                        self.page.invalidate();
                    }
                },
                {
                    type: "icon",
                    name: "delete",
                    text: "删除",
                    group: "base",
                    showText: true,
                    onClick: function (shapes) {
                        const cmd = deleteFlowableNodeCommand(self.page, shapes.map(s => {
                            return { shape: s, focused: s.getFocused() };
                        }));
                        cmd.execute(self.page);
                    }
                }
            ]
        };
    };

    /*
     * 运行态会覆写click方法，补充触发状态被点击时的事件，其他状态不触发。
     */
    self.click = () => {};

    // 鼠标悬停时变为指针
    self.onMouseMove = () => {
        self.page.cursor = CURSORS.HAND;
    };
    self.serializedFields.batchAdd("jober", "icon", "formConfig");
    return self;
};

export {aippState};