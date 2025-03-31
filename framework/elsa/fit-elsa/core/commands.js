/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {EVENT_TYPE} from '../common/const.js';

/**
 * command基类
 * history宿主
 * shapes是一个json array：[{shape:对象引用,width:{value:100,preValue:90},height:{value:100,preValue:90}}]
 */
const command = (host, shapes, type, init) => {
  const self = {shapes: serializeShapes(shapes)};
  self.type = type;

  /**
   * 执行命令
   */
  self.redo = (host) => {

  };
  /**
   * 回退命令
   */
  self.undo = (host) => {

  };

  /**
   * 在command创建时第一次执行，但不是所有command都执行该命令
   */
  self.execute = () => {
  };

  init && init();
  //把自己加入到history
  const h = host.graph ? host.graph : host;
  self.host = host.serialize();
  h.getHistory().addCommand(self, host);
  // host.getHistory().addCommand(self);
  return self;
};

/**
 * 多个命令组成一个命令
 * @param page
 * @param commands
 * @param strictOrder:是否按照严格顺序来执行，适用于有依赖关系的场景
 * @returns {{shapes: []}}
 */
const transactionCommand = (page, commands, strictOrder) => {
  const h = page.graph ? page.graph : page;
  const self = command(page, [], 'transaction', () => h.getHistory().commands.remove(c => commands.contains(c1 => c1 === c)));
  // self.type = "transaction";
  self.redo = host => {
    commands.forEach(c => c.redo(host));
  };

  self.undo = async host => {
    if (strictOrder) {
      const reversedCommands = [...commands].reverse();
      for (let i = 0; i < reversedCommands.length; i++) {
        const reversedCommand = reversedCommands[i];
        await reversedCommand.undo(host);
      }
    } else {
      commands.forEach(c => c.undo(host));
    }
  };

  self.execute = () => commands.forEach(c => c.execute());

  return self;
};

const serializeShapes = shapes => {
  if (!shapes) {
    return [];
  }
  return shapes.map(s => {//序列化shape，换页支持undo redo
    const serialized = {};
    for (let f in s) {
      if (f === 'shape') {
        serialized.shape = s.shape.id;
      } else {
        serialized[f] = s[f];
      }
    }
    return serialized;
  });
};

/**
 * 位置移动command
 * 辉子 2022
 */
const positionCommand = (page, shapes) => {
  const self = command(page, shapes, 'position');

  const execute0 = (host, shapeData, container, action) => {
    const shape = host.sm.getShapeById(shapeData.shape);
    if (!shape) {
      return;
    }
    // const direction = action.from === "undoMove" ? -1 : 1;
    // const x = shape.x + (shapeData.x.value - shapeData.x.preValue) * direction;
    // const y = shape.y + (shapeData.y.value - shapeData.y.preValue) * direction;

    shape.preMove && shape.preMove(action);
    const x = action.from === 'undoMove' ? shapeData.x.preValue : shapeData.x.value;
    const y = action.from === 'undoMove' ? shapeData.y.preValue : shapeData.y.value;
    shape.container = container;
    shape.moveTo(x, y);
    host.moveToContainer(shape);
    shape.moved(action);
  };

  self.redo = (host) => {
    self.shapes.forEach(s => {
      execute0(host, s, s.container.value, {from: 'redoMove', context: s});
    });
    page.triggerEvent({type: EVENT_TYPE.CONTEXT_CREATE, value: shapes});
  };

  self.undo = (host) => {
    self.shapes.forEach(s => {
      execute0(host, s, s.container.preValue, {from: 'undoMove', context: s});
    });
    page.triggerEvent({type: EVENT_TYPE.CONTEXT_CREATE, value: shapes});
  };

  self.execute = () => {
    self.shapes.forEach(shapeData => {
      const shape = page.sm.getShapeById(shapeData.shape);
      if (!shape) {
        return;
      }
      shapeData.container.preValue = shape.container;
      shape.moveTo(shapeData.x.value, shapeData.y.value);
      page.moveToContainer(shape);
      shapeData.container.value = shape.container;
    });
  };

  return self;
};

/**
 * 尺寸相关command
 * 辉子 2022
 */
const resizeCommand = (page, shapes) => {
  const self = command(page, shapes, 'resize');
  // self.type = "resize";

  self.execute = (host, property) => {
    if (host === undefined) {
      return;
    }
    const action = {from: (property === 'value' ? 'redoResize' : 'undoResize')};
    self.shapes.forEach(s => {
      const shape = host.sm.getShapeById(s.shape);
      if (!shape) {
        return;
      }
      shape.width = s.width[property];
      shape.height = s.height[property];

      /*
       * 当图形是容器时，进行resize的时候，需要同时移动其中的子图形，此时需要调用moveTo，否则会出现显示异常。
       * 比如：
       * 1、对container进行resize，子图形跟随移动
       * 2、撤销，子图形位置没有变化，导致无法看到或只能看到部分子图形.
       */
      shape.moveTo(s.x[property], s.y[property]);
      s.ox[property] !== undefined && (shape.textConnector.ox = s.ox[property]);
      s.oy[property] !== undefined && (shape.textConnector.oy = s.oy[property]);
      s.fromShape[property] !== undefined && (shape.fromShape = s.fromShape[property]);
      s.fromConn[property] !== undefined && (shape.definedFromConnector = s.fromConn[property]);
      s.toShape[property] !== undefined && (shape.toShape = s.toShape[property]);
      s.toConn[property] !== undefined && (shape.definedToConnector = s.toConn[property]);
      s.rotateDegree[property] !== undefined && (shape.rotateDegree = s.rotateDegree[property]);
      if (shape.isTypeof('line')) {
        shape.follow();
      }
      shape.invalidate(action);
    });
  };

  self.redo = (host) => {
    self.execute(host, 'value');
    page.triggerEvent({type: EVENT_TYPE.CONTEXT_CREATE, value: shapes});
  };

  self.undo = (host) => {
    self.execute(host, 'preValue');
    page.triggerEvent({type: EVENT_TYPE.CONTEXT_CREATE, value: shapes});
  };

  return self;
};

/**
 * 新增图形 command
 * 辉子 2022
 */
const addCommand = (page, shapes) => {
  const self = command(page, shapes, 'shapeAdd');
  self.saved = [];
  self.redo = (host) => {
    self.saved.forEach(s => {
      const shape = host.createNew(s.type, s.x, s.y, s.id);

      /*
       * 这里需要通过undoRemove来找到图形之前的container，并进行设置.
       * 否则，图形找到的container可能和撤销时的container不一致.
       */
      shape.undoRemove(host, shape.index);
    });
  };

  self.undo = (host) => {
    self.saved = [];
    self.shapes.forEach(s => {
      const shape = host.sm.getShapeById(s.shape);
      if (!shape) {
        return;
      }
      host.ignoreReact(() => shape.isFocused = false);
      const all = shape.remove();
      all.forEach(s => {
        self.saved.push({id: s.id, type: s.type, x: s.x, y: s.y});
      });
    });
  };

  return self;
};

/**
 * 图形下标改变批量接口.
 *
 * @override
 */
const shapesIndexChangedCommand = (page, shapes) => {
  const self = command(page, shapes, 'shapesIndexChange');

  /**
   * @override
   */
  self.redo = () => {
    self.shapes.forEach(s => {
      const shape = page.sm.getShapeById(s.shape);
      if (!shape) {
        return;
      }
      page.sm.updateShapes(writer => writer.moveShapeTo(shape, s.nextIndex));
    });
  };

  /**
   * @override
   */
  self.undo = () => {
    self.shapes.forEach(s => {
      const shape = page.sm.getShapeById(s.shape);
      if (!shape) {
        return;
      }
      page.sm.updateShapes(writer => writer.moveShapeTo(shape, s.prevIndex));
    });
  };

  /**
   * @override
   */
  self.execute = () => {
    shapes.forEach(s => {
      s.shape.index = s.nextIndex;
      s.shape.invalidateAlone();
    });
  };

  return self;
};

/**
 * 删除图形 command
 * 辉子 2022
 */
const deleteCommand = (page, shapes) => {
  let shapesVal = shapes;
  shapesVal = shapesVal.filter(s => s.shape.get('deletable')); // 筛选可以删除的
  const self = command(page, shapesVal, 'shapeDelete');

  self.redo = (host) => {
    self.shapes.forEach(s => {
      const shape = host.sm.getShapeById(s.shape);
      if (!shape) {
        return;
      }
      shape.remove();
    });
  };

  self.undo = (host) => {
    self.all.forEach(s => {
      let preIndex = s.index;
      let shape = host.sm.getShapeById(s.id);
      if (!shape) {//处理freeline这种特殊实际没删除形状的形状
        shape = host.createShape(s.type, s.x, s.y, s.id, s);
        // 删除图形后undo新增图形，需要通知协同服务器
        shape.serializable && host.graph.collaboration.invoke({
          method: 'new_shape', page: host.id, shape: shape.id, value: shape.serialize(), mode: host.mode,
        });
      }
      shape.undoRemove(host, preIndex);
    });

    self.lines.forEach(l => {
      const line = host.sm.getShapeById(l.line);
      if (!line) {
        return;
      }
      if (l.fromShape !== undefined) {
        line.fromShape = l.fromShape;
        line.definedFromConnector = l.definedFromConnector;
      }
      if (l.toShape !== undefined) {
        line.toShape = l.toShape;
        line.definedToConnector = l.definedToConnector;
      }
    });
  };

  self.execute = host => {
    self.all = [];//记录所有删除的shape：删除container会删除children，children需要被记下来
    self.shapes.forEach(s => {
      const shape = host.sm.getShapeById(s.shape);
      self.all.push.apply(self.all, shape.remove(shape).map(s => s.serialize()));
    });
    self.lines = [];//记录所有影响到的line
    page.sm.getShapes().forEach(l => {
      if (!l.isTypeof('line')) {
        return;
      }
      if (self.all.contains(s => s.id === l.preFromShape)) {
        self.lines.push({
          line: l.id, fromShape: l.preFromShape, definedFromConnector: l.preDefinedFromConnector,
        });
      }
      if (self.all.contains(s => s.id === l.preToShape)) {
        self.lines.push({line: l.id, toShape: l.preToShape, definedToConnector: l.preDefinedToConnector});
      }
    });
  };

  return self;
};

/**
 * 显示相关数据赋值command
 * 辉子 2022
 */
const layoutCommand = (page, shapes) => {
  const self = dataCommand(page, shapes, 'layout');
  // self.type = "layout";

  // self.redo = (host) => {
  //     self.execute(host, "value");
  // };

  // self.undo = (host) => {
  //     self.execute(host, "preValue");
  // };

  const execute = self.execute;
  self.execute = (host, property) => {
    const action = {from: (property === 'value' ? 'redoLayout' : 'undoLayout')};
    execute.call(self, host, property, s => s.invalidate(action));
  };

  return self;
};

/**
 * 简单数据赋值command
 * 辉子 2022
 */
const dataCommand = (page, shapes, type) => {
  const self = command(page, shapes, type ? type : 'dataChange');
  // self.type = "dataChange";

  self.redo = (host) => {
    self.execute(host, 'value');
  };

  self.undo = (host) => {
    self.execute(host, 'preValue');
  };

  self.execute = (host, property, after) => {
    let hostVal = host;
    !hostVal && (hostVal = page); // first time；
    self.shapes.forEach(s => {
      const shape = hostVal.sm.getShapeById(s.shape);
      if (!shape) {
        return;
      }

      for (let f in s) {
        if (f === 'shape') {
          continue;
        }
        if (!property) {
          const value = s[f] && (s[f].value || s[f].value === 0) ? s[f].value : s[f];
          (!s[f] || !s[f].value) && (s[f] = {});
          s[f].preValue = s[f].preValue ? s[f].preValue : shape.get(f);
          s[f].value = value;
        }
        let prop = shape;
        const fs = f.split('.');
        for (let i = 0; i < fs.length; i++) {
          if (i === fs.length - 1) {
            break;
          }
          prop = prop[fs[i]];
        }
        prop[fs[fs.length - 1]] = s[f][property ? property : 'value'];
        // s.shape[f] = s[f][property ? property : "value"];
      }
      if (after) {
        after(shape);
      } else {
        shape.invalidate();
      }
    });
  };

  return self;
};

/**
 * 新增page command， page操作的command history属于graph
 * 辉子 2022
 */
const pageAddedCommand = (graph, page, index) => {
  let indexVal = index;
  const self = command(graph, [], 'pageAdd');
  // self.type = "pageAdd";
  self.undo = () => {
    // 协作场景下index可能已经被改变，这样会删除错page，所以这里重新计算一次page index
    indexVal = graph.getPageIndex(page.id);
    graph.removePage(indexVal);
  };

  self.redo = (host) => {
    if (indexVal >= host.pages.length) {
      indexVal = undefined;
    }
    host.insertPage(page, indexVal);
    graph.fireEvent({type: EVENT_TYPE.FOCUSED_PAGE_CHANGE, value: page.id});
  };
  return self;
};

/**
 * page在graph里的位置变化command
 * 辉子 2022
 */
const pageIndexChangedCommand = (graph, fromIndex, toIndex) => {
  const self = command(graph, [], 'pageIndexChanged');
  self.undo = (host) => host.movePageIndex(toIndex, fromIndex);
  self.redo = (host) => host.movePageIndex(fromIndex, toIndex);
  return self;
};

/**
 * 删除 page command
 * @param {*} page
 * @param {*} index
 */
const pageRemovedCommand = (graph, page, index) => {
  let indexVal = index;
  const self = command(graph, [], 'pageRemove');
  // self.type = "pageRemove";
  self.undo = async (host) => {
    if (indexVal >= host.pages.length) {
      indexVal = undefined;
    }
    host.insertPage(page, indexVal);
    await graph.fireEvent({type: EVENT_TYPE.FOCUSED_PAGE_CHANGE, value: page.id});
  };
  self.redo = (host) => {
    // 协作场景下index可能已经被改变，这样会删除错page，所以这里重新计算一次page index
    indexVal = host.getPageIndex(page.id);
    host.removePage(indexVal);
  };
  return self;
};

const addFreeLineCommand = (page, shapes) => {
  const self = command(page, shapes, 'addFreeline');
  // self.type = "addFreeline";
  self.redo = (host) => {
    self.shapes.forEach(s => {
      const fline = page.sm.getShapeById(s.shape);
      // const fline = s.shape;
      if (fline) {
        fline.lines.push.apply(fline.lines, s.lines);
        fline.invalidate();
        fline.local = {action: 'add_free_lines', lines: s.lines};
      }

    });
  };

  self.undo = (host) => {
    self.shapes.forEach(s => {
      const fline = page.sm.getShapeById(s.shape);
      if (fline) {
        fline.lines.remove(l => s.lines.contains(l1 => l1.id === l.id));
        fline.invalidate();
        fline.local = {action: 'delete_free_lines', lines: s.lines};
      }
    });
  };
  return self;
};

const updateFreeLineCommand = (page, shapes) => {
  const self = command(page, shapes, 'updateFreeline');
  // self.type = "updateFreeline";
  const updateLines = (property) => {
    self.shapes.forEach(s => {
      const fline = page.sm.getShapeById(s.shape);
      // const fline = s.shape;
      s.lines[property].forEach(l => {
        const line = fline.lines.find(l1 => l1.id === l.id);
        line.points = l.points;
        line.bound = l.bound;
        line.delta = l.delta;
        line.scale = l.scale;
        line.rotate = l.rotate;
        delete line.pathData;
      });
      fline.invalidate();
      fline.local = {action: 'update_free_lines', lines: s.lines[property]};
    });
  };
  self.redo = (host) => {
    updateLines('value');
  };

  self.undo = (host) => {
    updateLines('preValue');
  };
  return self;
};

/**
 * freeLine手写笔的擦除历史
 * 辉子 2022
 */
const eraserComamnd = (page, shapes) => {
  const self = command(page, shapes, 'eraseFreeline');
  // self.type = "eraseFreeline";

  self.shapes.forEach((s, i) => {
    s.value = shapes[i].shape.lines;
  });

  const execute = (host, key) => {
    self.shapes.forEach(s => {
      const free = page.sm.getShapeById(s.shape);
      // const free = s.shape;
      free.lines = s[key];
      free.invalidate();
    });
  };
  self.redo = (host) => {
    execute(host, 'value');
  };

  self.undo = (host) => {
    execute(host, 'preValue');
  };

  return self;
};

/**
 * 编辑器指令。所有的操作统一有elsa管理，因此这里需要一个命令来触发编辑器的历史管理功能。
 *
 * @param page 页面对象。
 * @param editor 编辑器对象。
 * @param data 数据。
 * @param shape 图形对象.
 * @param focusedShapeIdSet 被选中的图形的id集合.
 * @param manuallySelectAll 是否是手动选中所有文本的场景.
 * @return {{shapes: ([]|*)}} 指令对象。
 */
const editorCommand = (page, editor, data, shape, focusedShapeIdSet, manuallySelectAll) => {
  const self = command(page, [], 'editorChange');
  self.data = data;
  self.redo = () => {
    focusShape();
    editor.commands.get('redo').execute();
    focusOrBlurEditor();
  };
  self.undo = () => {
    focusShape();
    editor.commands.get('undo').execute();
    focusOrBlurEditor();
  };

  function focusOrBlurEditor() {
    // 如果选中的图形只有一个，并且是当前图形自己，并且不是手动触发全选的操作，则需要将编辑器聚焦.
    // 否则，不需要聚焦，删除window的selection.
    // 如果只删除编辑器的selection，在文档的场景下，会导致光标聚焦在文档开头的问题.
    if (focusedShapeIdSet.size === 1 && focusedShapeIdSet.has(shape.id) && !manuallySelectAll) {
      editor.editing.view.focus();
    } else {
      // 这里需要将编辑器的选区设置为null
      // 如果选区不为null的话，当点击编辑区域时，会触发编辑器进行重新渲染，具体可查看ck源码中view的_render方法:
      // _render() {
      //     this.isRenderingInProgress = true;
      //     this.disableObservers();
      //     this._renderer.render();
      //     this.enableObservers();
      //     this.isRenderingInProgress = false;
      // }
      // 里面的_renderer中的render方法，会调用_updateSelection方法修改选区，若此时不为null，那么会出现选中文本效果
      editor.model.change(writer => writer.setSelection(null));

      // 光设置了selection为null，文本可能还是处于选中状态，这里需要手动删除window的selection数据.
      const selection = window.getSelection();
      if (selection) {
        selection.removeAllRanges();
      }
    }
  }

  const focusShape = () => {
    if (!shape.isFocused) {
      // 此时可能选中其他图形进行撤销，那么需要将其他图形先取消反选.
      page.getFocusedShapes().filter(s => !focusedShapeIdSet.has(s.id)).forEach(s => s.unSelect());
      shape.select();
    }
  };
  return self;
};

export {
  command,
  positionCommand,
  resizeCommand,
  addCommand,
  deleteCommand,
  dataCommand,
  layoutCommand,
  eraserComamnd,
  pageAddedCommand,
  pageIndexChangedCommand,
  pageRemovedCommand,
  transactionCommand,
  addFreeLineCommand,
  updateFreeLineCommand,
  editorCommand,
  shapesIndexChangedCommand,
};
