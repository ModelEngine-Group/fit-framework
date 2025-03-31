/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JSDOM, VirtualConsole} from "jsdom"
const virtualConsole = new VirtualConsole();
virtualConsole.on("error", () => {
    // resolve 'Could not parse CSS stylesheet' issue.
});
const dom = new JSDOM(``, {virtualConsole});
global.document = dom.window.document
global.window = dom.window
global.CanvasRenderingContext2D = class {};