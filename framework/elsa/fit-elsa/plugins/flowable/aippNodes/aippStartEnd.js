/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {nameConfiguration} from "../configurations/nameConfiguration.js";
import {descriptionConfiguration} from "../configurations/descriptionConfiguration.js";
import {startFormConfiguration} from "../configurations/startFormConfiguration.js";
import {outputFormConfiguration} from "../configurations/outputFormConfiguration.js";
import {node} from "../nodes/node.js";
import {CURSORS, FONT_WEIGHT} from "../../../common/const.js";

/**
 * flowable开始节点
 * flowable的设计其实可以没有开始节点，每个节点都可以是数据输入节点
 * 辉子 2020
 */
const aippStart = (id, x, y, width, height, parent, drawer) => {
    let self = node(id, x, y, width, height, parent, false, drawer);
    self.type = "aippStart";

    self.autoWidth = true;
    self.autoHeight = true;
    self.editable = false;
    self.allowFromLink = true;
    self.allowToLink = false;
    self.pad = 14;
    self.fontSize = 14;
    self.text = "开始";
    self.padTop = self.padBottom = 14;
    self.fontWeight = FONT_WEIGHT.NORMAL;
    self.ignoreDefaultContextMenu = true;
    self.fontColor = "#000";
    self.borderWidth = 1;
    self.moveable = false;
    self.shadow = true;
    self.borderColor = "#D7D9E7";
    self.backColor = "linear-gradient(#edf6ff, #f7fcff)";
    self.shadowColor = "#eee"
    self.cornerRadius = 12;
    self.modeRegion.visible = false;

    /**
     * 获取节点配置.
     *
     * @override
     */
    self.getConfigurations = () => {
        const configs = [];
        configs.push(nameConfiguration(self, "text"));
        configs.push(descriptionConfiguration(self, "description"));
        configs.push(startFormConfiguration(self, "formConfig"));
        return configs;
    };


    /*
     * 运行态会覆写click方法，补充触发状态被点击时的事件，其他状态不触发。
     */
    self.click = () => {};

    // 鼠标悬停时变为指针
    self.onMouseMove = () => {
        self.page.cursor = CURSORS.HAND;
    };
    return self;
};
/**
 * 结束节点
 * 数据到此不再处理
 * huiz 2020
 */
const aippEnd = (id, x, y, width, height, parent, drawer) => {
    const WIDTH = 40;
    let self = aippStart(id, x, y, width, height, parent, drawer);
    self.type = "aippEnd";
    self.text = "结束";
    self.allowFromLink = false;
    self.allowToLink = true;

    /**
     * 获取节点配置.
     *
     * @override
     */
    self.getConfigurations = () => {
        const configs = [];
        configs.push(nameConfiguration(self, "text"));
        configs.push(descriptionConfiguration(self, "description"));
        configs.push(outputFormConfiguration(self, "formConfig"));
        return configs;
    };

    self.callback = {
        name: "通知回调",
        type: "general_callback",
        fitables: [
            "modelengine.fit.jober.aipp.fitable.AippFlowEndCallback"
        ]
    }

    return self;
};

export {aippStart, aippEnd};