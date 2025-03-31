/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

String.prototype.replaceAll = function (search, replacement) {
    let self = this;
    return self.replace(new RegExp(search, 'g'), replacement);
};
String.prototype.trim = function () {
    return this.replace(/^\s+|\s+$/g, '');
};
String.prototype.capitalize = function () {
    return this.charAt(0).toUpperCase() + this.slice(1);
};