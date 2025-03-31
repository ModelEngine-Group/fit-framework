/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {editorInterface} from "../editorInterface.js";
import {TEXT_ATTRIBUTES} from "../../common/const.js";
import {NormalizerFactory} from "./normalizers.js";

const COMMANDS_MAP = new Map();
COMMANDS_MAP.set(TEXT_ATTRIBUTES.BOLD, "bold");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.ITALIC, "italic");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.STRIKETHROUGH, "strikethrough");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.UNDERLINE, "underline");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.BACK_COLOR, "fontBackgroundColor");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.FONT_COLOR, "fontColor");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.FONT_FACE, "fontFamily");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.FONT_SIZE, "fontSize");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.NUMBERED_LIST, "numberedList");
COMMANDS_MAP.set(TEXT_ATTRIBUTES.BULLETED_LIST, "bulletedList");
COMMANDS_MAP.set("heading", "heading");
COMMANDS_MAP.set("paragraph", "paragraph");
COMMANDS_MAP.set("shape", "shape");
COMMANDS_MAP.set("resizeShape", "resizeShape");
COMMANDS_MAP.set("removeShape", "removeShape");

const EDITABLE_CLASSES = [
    "ck",
    "ck-content",
    "ck-editor__editable",
    "ck-rounded-corners",
    "ck-editor__editable_inline"
];

export const elsaCKEditor = (shape, ckEditor, rootName) => {
    const editableId = "editableArea:" + rootName;

    /*
     * 这里需要使用shape.pageId，因为取shape.page.id在异步的情况下可能会发生变化，比如:
     * 1、编辑pageA，此时图形异步渲染，还没开始渲染
     * 2、切换到pageB，此时pageA中的图形开始渲染，创建elsaCKEditor
     * 3、那么此时创建出来的dom就是pageB下的dom，而不是pageA的dom，此时数据就产生了错乱和污染
     * pageId是在shape被创建的时候就静态设置在shape上，因此不会发生变化，是准确的
     */
    let editable = shape.graph.getElement(shape.page.div, shape.pageId, editableId);
    if (!editable) {
        editable = shape.graph.createDom(shape.page.div, "div", editableId, shape.pageId);
        editable.style.overflowWrap = "break-word";
        editable.style.whiteSpace = "pre-wrap";
        editable.style.overflow = "visible";
        editable.style.outline = "none";
        editable.style.height = "100%";
        editable.style.width = "100%";
        editable.contentEditable = "true";
        EDITABLE_CLASSES.forEach(clz => editable.classList.add(clz));
        shape.drawer.text.appendChild(editable);
    }

    const self = editorInterface();
    self.editor = ckEditor;
    self.editable = editable;

    /**
     * 渲染.
     * 每次渲染都会重新创建一个编辑器对象(出于手动对shape进行text设置的考虑，shape.text = "xxx").
     *
     * @override
     */
    self.render = function () {
        let first = false;
        return (data) => {
            if (!first) {
                ckEditor.registerRoot(rootName, editable);
                first = true;
            }
            const input = {};
            input[rootName] = data;
            ckEditor.setData(input);
        };
    }();

    /**
     * 获取editable下的innerHtml文本.
     *
     * @returns {*} innerHtml文本.
     */
    self.getHtml = () => {
        return editable.innerHTML;
    };

    /**
     * 通过innerHtml的形式渲染文本.
     *
     * @param innerHtml innerHtml文本.
     */
    self.renderByInnerHtml = (innerHtml) => {
        editable.innerHTML = innerHtml;
    };

    /**
     * 对数据进行标准化操作，使其符合编辑器的规范.
     *
     * @param data 数据.
     * @returns {[{children: [{data: string}], name: string}]|*|*[]} 标准化后的数据.
     */
    self.normalize = (data) => {
        if (data === null || data === undefined) {
            return [];
        }

        if (typeof data !== "string" && !Array.isArray(data)) {
            throw new Error("elsaCKEditor#normalize: invalid data for editor.");
        }

        if (typeof data === "string") {
            return [{
                "name": "paragraph", "children": [{"data": data}]
            }];
        }

        return data;
    };

    /**
     * @inheritDoc
     * @override
     */
    self.focus = () => {
        ckEditor.focus();
    };

    /**
     * 渲染toolbar组件.
     *
     * @param div 待渲染的div.
     */
    self.renderToolbar = (div) => {
        ckEditor.renderToolbar(div);
    };

    /**
     * @inheritDoc
     * @override
     */
    self.format = (key, value = null, selectAllWhenIsNotFocused = true) => {
        if (forbiddenCommands.has(key)) {
          return undefined;
        }

        const isFocused = self.isFocused();
        const commandKey = COMMANDS_MAP.get(key);
        const command = ckEditor.commands.get(commandKey);

        /*
         * 这里有两种情况
         * 1、在执行命令前全选文本
         *      a、全选文本
         *      b、执行命令
         *      c、更新shape的文本属性
         *      d、恢复selection
         * 2、不需要徐全选文本
         *      a、执行命令
         *      b、全选文本
         *      c、更新shape的文本属性
         *      d、恢复selection
         * 两者的具备就是是否需要将文本作用于整个文本。而更新shape的文本属性，是需要全选文本才行的。
         */
        if (!isFocused && selectAllWhenIsNotFocused) {
            return selectAllText(() => {
                const result = command.execute(value ? value : {});
                updateShapeTextAttributes();
                return result;
            });
        } else {
            const result = command.execute(value ? value : {});
            selectAllText(() => updateShapeTextAttributes());
            return result;
        }
    };

    /**
     * 在ckeditor没有提供对整个root进行选中的api，因此这里需要手动处理
     * 1、将当前selection进行克隆，保留当前的现场
     * 2、创建一个覆盖整个root的range
     * 3、设置selection为新创建的range
     * 4、执行操作
     * 5、通过克隆的selection，恢复现场
     *
     * @param action 操作
     * @return {*}
     */
    const selectAllText = (action) => {
        const model = ckEditor.model;
        const selection = model.document.selection;
        const hasOwnRange = selection.hasOwnRange;
        const selectionCloned = ckEditor.cloneSelection(model.document.selection);
        const range = model.createRangeIn(model.document.getRoot(rootName));
        model.change(writer => writer.setSelection(range));
        ckEditor.manuallySelectAll = true;

        try {
            return action();
        } catch (e) {
            // 没关系，继续，不影响其他错误信息的处理.
          return undefined;
        } finally {
            ckEditor.manuallySelectAll = false;
            // 如果selection的hasOwnRange为false，则直接将选区设置为null.
            model.change(writer => writer.setSelection(hasOwnRange ? selectionCloned : null));
        }
    };

    const updateShapeTextAttributes = () => {
        const attributes = {};
        COMMANDS_MAP.forEach((v, k) => {
            const command = ckEditor.commands.get(v);
            const normalizer = NormalizerFactory.get(k);
            attributes[k] = normalizer ? normalizer.normalize(command.value, shape) : command.value;
        });
        shape.onTextAttributeChange(attributes);
    };

    /**
     * @inheritDoc
     * @override
     */
    self.isFormatted = (key) => {
        return self.getFormatValue(key);
    };

    /**
     * @inheritDoc
     * @override
     */
    self.getFormatValue = (key) => {
        const command = ckEditor.commands.get(COMMANDS_MAP.get(key));
        if (!command) {
            throw new Error(key + " is not support!");
        }
        const normalizer = NormalizerFactory.get(key);
        return normalizer ? normalizer.normalize(command.value, shape) : command.value;
    };

    /**
     * @inheritDoc
     * @override
     */
    const forbiddenCommands = new Set();
    self.addForbiddenCommand = (commandName) => {
        if (!commandName || commandName === "") {
            return;
        }
        forbiddenCommands.add(commandName);
    };

    /**
     * 有时候选区存在，但是编辑器不处于focused状态，此时需要判断这两个条件.
     * 参考:https://github.com/ckeditor/ckeditor5/issues/6485
     *
     * @inheritDoc
     * @override
     */
    self.isFocused = () => {
        const root = ckEditor.editing.view.document.getRoot(rootName);
        return ckEditor.editing.view.hasDomSelection && root && root.isFocused;
    };

    /**
     * @inheritDoc
     * @override
     */
    self.blur = () => {
        ckEditor.model.change(writer => writer.setSelection(null));
    };

    /**
     * @inheritDoc
     * @override
     */
    self.unmount = () => {
    };

    /**
     * @inheritDoc
     * @override
     */
    self.destroy = () => {
    };

    /**
     * @inheritDoc
     * @override
     */
    self.getTextString = () => {
        if (typeof shape.text === "string") {
            return shape.text;
        }
        return _getString({children: shape.text});
    };

    /**
     * 修改DragDrop标记.
     *
     * @param x 横坐标.
     * @param y 总坐标.
     */
    self.updateDragDropMarker = (x, y) => {
        const dragDropPlugin = ckEditor.plugins.get("ElsaDragDrop");
        dragDropPlugin.updateMarker(x, y);
    };

    /**
     * 删除DragDrop标记.
     */
    self.removeDragDropMarker = () => {
        const dragDropPlugin = ckEditor.plugins.get("ElsaDragDrop");
        dragDropPlugin.removeMarker();
    };

    /**
     * 将elsa中的position转换为编辑器的选区。
     *
     * @param position 坐标信息。
     * @return {*} 编辑器选区。
     */
    self.positionToSelection = (position) => {
        return ckEditor.positionToSelection(position);
    };

    /**
     * 设置占位符.
     *
     * @param placeholder 占位字符串.
     */
    self.setPlaceholder = (placeholder) => {
        ckEditor.setPlaceholder(rootName, placeholder);
    };

    const _getString = (node) => {
        if (node.children && Array.isArray(node.children)) {
            return node.children.map(_getString).join("");
        }
        return node.data ? node.data : "";
    }

    return self;
}