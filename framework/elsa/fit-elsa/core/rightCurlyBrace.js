/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {svg} from "./svg.js";
import {svgDrawer} from './drawers/svgDrawer.js';

/**
 * 右大括号
 */
const rightCurlyBrace = (id, x, y, width, height, parent) => {
    let self = svg(id, x, y, width, height, parent, svgDrawer);
    self.type = "rightCurlyBrace";
    self.getSvg = () => {
        return `<svg id=${self.type + "-svg:" + self.id} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 51.503 320.86" x='0px' y='0px'>
                        <defs><style>.a{fill:${(self.getBackColor())};}.b,.c{stroke: ${self.getBorderColor()};stroke-width:${self.borderWidth}}}</style></defs>
                        <g class="a" transform="translate(-58.001 -9.407)">
                        <path class="b" d="M9870,4358.267h0v-1c5.793-.357,10.545-.75,14.123-1.17a62.147,62.147,0,0,0,11.82-2.3,10.966,10.966,0,0,0,2.858-1.252l.149-.147a1.5,1.5,0,0,0,.024-.279v-150.5l.244-.282q.2-.238.407-.476l.062-.071.176-.205.075-.087c1.8-2.088,4.935-3.666,9.313-4.689-4.128-.716-7.112-1.911-8.869-3.55-.387-.36-.762-.724-1.115-1.081l-.292-.291V4043.6a1.5,1.5,0,0,0-.032-.321l-.045-.032a15.717,15.717,0,0,0-3.3-1.332,69.488,69.488,0,0,0-12.439-2.4c-3.414-.4-7.843-.77-13.162-1.1v-1c5.358.33,9.826.7,13.279,1.108a69.3,69.3,0,0,1,12.813,2.5,14.843,14.843,0,0,1,3.361,1.4c.288,0,.522.531.522,1.183v146.881c.354.357.719.711,1.086,1.054,1.669,1.555,4.608,2.685,8.736,3.359a60.554,60.554,0,0,0,9.269.687,53.343,53.343,0,0,0-9.474,1.185c-4.229.973-7.221,2.451-8.895,4.393l-.2.234-.188.219-.069.081-.263.305V4352.12c0,.652-.234,1.183-.522,1.183a9.7,9.7,0,0,1-2.881,1.338,61.274,61.274,0,0,1-12.333,2.447c-3.615.424-8.406.821-14.239,1.18Zm49.212-162.689h-.145c.938-.039,1.657-.047,2.1-.047.206,0,.322,0,.339,0a.531.531,0,0,1-.067,0C9920.739,4195.564,9919.991,4195.578,9919.213,4195.578Z" transform="translate(-9812 -4028)"/></g></svg>`;
    };

    /**
     * 重写获取配置方法.
     * 1、该图形不需要corerRadius配置.
     */
    const getConfigurations = self.getConfigurations;
    self.getConfigurations = () => {
        const configurations = getConfigurations.apply(self);
        configurations.remove(c => c.field === "cornerRadius");
        return configurations;
    }

    return self;
}

export {rightCurlyBrace};