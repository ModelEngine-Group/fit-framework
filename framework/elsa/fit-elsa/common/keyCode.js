/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 键盘按键类型工具类.
 */
export class KeyCode {
    static _NUMBERS = new Set().add("Digit0")
        .add("Digit1")
        .add("Digit2")
        .add("Digit3")
        .add("Digit4")
        .add("Digit5")
        .add("Digit6")
        .add("Digit7")
        .add("Digit8")
        .add("Digit9")
        .add("Numpad0")
        .add("Numpad1")
        .add("Numpad2")
        .add("Numpad3")
        .add("Numpad4")
        .add("Numpad5")
        .add("Numpad6")
        .add("Numpad7")
        .add("Numpad8")
        .add("Numpad9");
    static _ALPHA = new Set().add("KeyA")
        .add("KeyB")
        .add("KeyC")
        .add("KeyD")
        .add("KeyE")
        .add("KeyF")
        .add("KeyG")
        .add("KeyH")
        .add("KeyI")
        .add("KeyJ")
        .add("KeyK")
        .add("KeyL")
        .add("KeyM")
        .add("KeyN")
        .add("KeyO")
        .add("KeyP")
        .add("KeyQ")
        .add("KeyR")
        .add("KeyS")
        .add("KeyT")
        .add("KeyU")
        .add("KeyV")
        .add("KeyW")
        .add("KeyX")
        .add("KeyY")
        .add("KeyZ");
    static _SYMBOLS = new Set().add("NumpadMultiply")
        .add("NumpadAdd")
        .add("NumpadSubtract")
        .add("NumpadDecimal")
        .add("NumpadDivide")
        .add("Semicolon")
        .add("Equal")
        .add("Comma")
        .add("Minus")
        .add("Period")
        .add("Slash")
        .add("Backquote")
        .add("BracketLeft")
        .add("Backslash")
        .add("BracketRight")
        .add("Quote");

    /**
     * 判断是否是数字.
     *
     * @param key 键.
     */
    static isNumber(key) {
        return this._NUMBERS.has(key);
    }

    /**
     * 判断是否是字母.
     *
     * @param key 键.
     */
    static isAlpha(key) {
        return this._ALPHA.has(key);
    }

    /**
     * 判断是否是符号.
     *
     * @param key 键.
     */
    static isSymbol(key) {
        return this._SYMBOLS.has(key);
    }
}