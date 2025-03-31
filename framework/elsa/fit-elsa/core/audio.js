/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { rectangle } from './rectangle.js';
import { rectangleDrawer } from './drawers/rectangleDrawer.js';
import { hitRegion } from './hitRegion.js';
import {video} from "./video.js";

function createAudioElementId(shapeId) {
    return "audio_player:" + shapeId;
}

/**
 * 音频播放器
 * 王成/陈潇文 2024
 */
const audio = (id, x, y, width, height, parent, drawer) => {
    let self = video(id, x, y, width, height, parent, drawer === undefined ? audioDrawer : drawer);
    self.type = "audio";
    return self;
};

const audioDrawer = (shape, div, x, y) => {
    let self = rectangleDrawer(shape, div, x, y);
    self.type = "audio drawer";

    self.textResize = (width, height) => {
        self.text.remove();
    };

    const parentResize = self.parentResize;
    self.parentResize = (width, height) => {
        const AUDIO_PLAYER = createAudioElementId(shape.id);
        parentResize.call(self, width, height);

        self.audio = self.createElement("audio", AUDIO_PLAYER);
        if (self.audio.parentNode !== self.parent) {
            self.parent.appendChild(self.audio);
            self.audio.src = shape.src;
        }
        self.audio.style.width = width + "px";
        self.audio.style.height = height + "px";
        self.audio.type = "audio/mp3";
    };

    return self;
};

export { audio };