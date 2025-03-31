/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const toolbarMenu = (type, config, target) => {
    const self = {};
    self.type = type;
    self.config = config;
    self.target = target;

    self.createDom = (m) => {
        if (!m || typeof m !== "object") {
          return undefined;
        }

        const menu = document.createElement("div");
        menu.classList.add("menu");

        menu.addEventListener("mousedown", (event) => {
            selectShapes(self.target);
            m.onClick && m.onClick(self.target);
            // 由于contextMenu是HTML原生和Elsa组件的组合体，为了避免HTML原生事件传入Elsa其他组件，必须拦截mousedown事件
            event.stopPropagation();
        });

        return menu;
    };

    return self;
}

const iconMenu = (type, config, target) => {
    const self = toolbarMenu(type, config, target);
    const createDom = self.createDom;
    self.createDom = (m) => {
        const menu = createDom.call(self, m);
        const button = document.createElement("span");
        button.innerHTML = m.showText ? `<span class="text">${m.text}</span>` : `<span class="icon-menu">${m.icon}</span>`;

        menu.appendChild(button);
        return menu;
    }

    return self;
};

const submenu = (type, config, target) => {
    const self = toolbarMenu(type, config, target);

    self.createDom = (m) => {
        const menu = document.createElement("div");
        menu.classList.add("menu");
        const button = document.createElement("span");
        button.innerHTML = m.showText ? `<span class="icon-menu">${m.icon}</span> <span class="text">${m.text}</span>` : `<span class="icon-menu">${m.icon}</span>`;
        menu.appendChild(button);

        // 添加一个状态变量来跟踪子菜单的创建状态
        let subMenuCreated = false;

        // 待处理：将事件行为提升到menu上，支持子类扩展
        menu.addEventListener("click", () => {
            selectShapes(self.target);

            // 如果子菜单已经被创建，跳过创建逻辑
            if (subMenuCreated) {
                return;
            }

            const subMenu = self.createSubMenu(menu, m.menu);
            if (m.render) {
                m.render(subMenu, target);
                subMenu.classList.add("customer-submenu");
            } else if (m.menu) {
                m.menu.forEach(m => {
                    const menu = iconMenu(m.type, m, self.target).createDom(m);
                    subMenu.appendChild(menu);
                })
            }
            menu.appendChild(subMenu);

            // 更新状态变量
            subMenuCreated = true;
        });

        self.createSubMenu = (parent, config) => {
            const container = document.createElement("div");
            container.classList.add("sub-menu-container", "vertical");

            return container;
        };

        return menu;
    }

    return self;
}

const selectShapes = (target) => {
    target.forEach(t => {
        t.select();
    });
}

const selectMenu = (type, config, target) => {
    const self = toolbarMenu(type, config, target);
    const createDom = self.createDom;
    self.createDom = (m) => {
        const menu = createDom.call(self, m);
        const select = document.createElement("select");
        select.classList.add("select-menu");
        self.config.options.forEach((option) => {
            const optElement = document.createElement("option");
            optElement.value = option;
            optElement.textContent = option;
            if (option === self.config.defaultValue) {
                optElement.selected = true;
            }
            select.appendChild(optElement);
        });

        select.addEventListener("change", function () {
            self.config.onChange && self.config.onChange(select.value, self.target);
            selectShapes(self.target);
        });
        menu.appendChild(select);
        return menu;
    }

    return self;
};

const colorPickerMenu = (type, config, target) => {
    const self = toolbarMenu(type, config, target);
    const createDom = self.createDom;
    self.createDom = (m) => {
        const menu = createDom.call(self, m);
        const input = document.createElement("input");
        input.type = "color";
        input.value = self.config.defaultValue;
        input.classList.add("input-menu");

        input.addEventListener("change", function () {
            self.config.onChange && self.config.onChange(input.value, self.target);
            selectShapes(self.target);
        });
        menu.appendChild(input);
        return menu;
    }
    return self;
};

const customMenu = (type, config, target) => {
    const self = toolbarMenu(type, config, target)

    const createDom = self.createDom;
    self.createDom = (m) => {
        const menu = createDom.call(self, m);
        if (m.render) {
            m.render(menu, target);
        }
        return menu;
    }
    return self;
};

export {iconMenu, selectMenu, colorPickerMenu, customMenu, submenu};