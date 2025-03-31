/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

Set.prototype.batchAdd = function (...data) {
    data.forEach(d => this.add(d));
}
Set.prototype.batchDelete = function (...data) {
    data.forEach(d => this.delete(d));
};