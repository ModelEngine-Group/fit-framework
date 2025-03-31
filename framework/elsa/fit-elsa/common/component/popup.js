/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

//简单版本弹层, 本期还未提供默认按钮
export default class Popup {
    constructor(parent, title, content, okCallback, cancelCallback) {
        const self = this;
        self.parent = parent || document.body;
        self.title = title || "标题";
        self.content = content || "";
        self.okCallback = okCallback;
        self.cancelCallback = cancelCallback;
    }

    show = () => {
        const pop = document.createElement('div');
        pop.class = `elsa-popup`;
        pop.style.position = "fixed";
        pop.style.x = 0;
        pop.style.y = 0;
        pop.style.width = "100%";
        pop.style.height = "100%";
        pop.style.zIndex = "999";

        pop.innerHTML = `<div style="position:absolute;left:35%;top:35%;width:360px;height: 120px;background:#fff;color: #201f1e;padding:20px;line-height: 26px;box-shadow: 0 3px 6px -4px rgb(0 0 0 / 12%), 0 6px 16px 0 rgb(0 0 0 / 8%), 0 9px 28px 8px rgb(0 0 0 / 5%);">
                            <div style="font-size: 20px;line-height:30px;margin-bottom: 15px;">${this.title}</div>
                            <div style="display: flex;">
                                ${this.content}
                            </div>
    <!--                        <span style="position: absolute;right: 15px;top:16px;font-size: 19px;color: #333;" onclick="console.log('======================', this)">⊗</span>-->
                        </div>`;
        this.node = pop;
        this.parent.appendChild(pop);
    }

    cancel = () => {
        this.parent.removeChild(this.node);
        this.cancelCallback && this.cancelCallback();
    }
}