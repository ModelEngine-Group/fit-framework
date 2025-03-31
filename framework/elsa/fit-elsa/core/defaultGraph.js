/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {graph} from './graph.js';
import ElsaEditor from '../editor/default/elsa-editor.js';
import {EVENT_TYPE} from '../common/const.js';
import {elsaCKEditor} from '../editor/default/elsaCKEditor.js';
import {editorCommand} from './commands.js';

/**
 * @inheritDoc
 */
const defaultGraph = (div, title) => {
  const self = graph(div, title);

  self.editor = null;
  const initialize = self.initialize;
  self.initialize = async () => {
    if (self.enableText) {
      // initialize本来就是异步的，因此，在这里面直接用await就行.防止在后续流程中使用editor时获取不到(比如演示的时候).
      self.editor = await ElsaEditor.create({}, {generateId: self.uuid});
      self.editor.addSelectionListener((selection) => {
        self.activePage.triggerEvent({
          type: EVENT_TYPE.EDITOR_SELECTION_CHANGE, value: selection,
        });
      });

      // 监听编辑器历史事件，创建对应的指令.
      self.editor.on('history:undo:event', (evt, operation) => {
        let rootName = self.editor.getRootNameByOperation(operation);
        if (rootName) {
          const manuallySelectAll = self.editor.manuallySelectAll;
          const shapeId = getShapeIdByRootName(rootName);
          const shape = self.activePage.getShapeById(shapeId);
          const focusedShapeIdSet = new Set(self.activePage.getFocusedShapes().map(s => s.id));
          editorCommand(self.activePage, self.editor, operation, shape, focusedShapeIdSet, manuallySelectAll);
        }
      });

      // 接管编辑器的快捷键功能.
      self.editor.keystrokes.set('Ctrl+Z', (keyEvtData, cancel) => {
        self.getHistory().undo();
        cancel();
      }, {priority: 'high'});

      // 接管编辑器的快捷键功能.
      self.editor.keystrokes.set('CTRL+SHIFT+Z', (keyEvtData, cancel) => {
        self.getHistory().redo();
        cancel();
      }, {priority: 'high'});
    }

    return initialize.apply(self);
  };

  const getShapeIdByRootName = (rootName) => {
    // elsa-page:2rt02i_5zx9or_configuration，分隔之后 ["elsa-page:2rt02i", "5zx9or", "configuration]
    const names = rootName.split('_');
    if (names.length !== 3) {
      return null;
    }
    return names[1];
  };

  /**
   * @inheritDoc
   * @override
   */
  self.createEditor = (shape) => {
    if (!self.enableText && !shape.hideText) {
      throw new Error('Graph disable text, shape must hide text.');
    }

    if (self.editor === null) {
      throw new Error('defaultGraph#createEditor: external editor is null.');
    }

    // 不同的页面中shape的id可能一样，因此，这里加上pageId保证唯一性.例如: elsa-page:2rt02i_5zx9or_configuration
    const editableName = `${shape.page.id}_${shape.id}_${shape.page.mode}`;
    const editor = elsaCKEditor(shape, self.editor, editableName);

    // 添加数据监听器.
    editor.editor.addDataListener(editableName, (prevData, data, isSetManually) => {
      /*
       * * 注意 *
       * 当手动对编辑器进行set操作时，此时不应该对图形的text进行设置。否则，在协同时会出现问题，例如：
       * 1、协同方a初始化页面，里面的图形会进行render，那么会触发该方法
       * 2、触发该方法后，会触发propertyChanged方法，生成dirties
       * 3、发送dirties到协同方b
       * 4、协同方b接收后，设置text，并且render，触发该方法，接着触发propertyChanged方法，生成dirties，又会发送协同消息给协同方a
       * 5、协同方a又重复上述操作
       */
      if (isSetManually) {
        return;
      }
      shape.page.ignoreReact(() => {
        shape.text = data;
        shape.textChanged && shape.textChanged();
        // 开启文本缓存，记录文本html.
        if (shape.isEnableHtmlText()) {
          shape.textInnerHtml = shape.drawer.getEditor().getHtml();
        }
      });
    });

    return editor;
  };

  return self;
};

export {defaultGraph};