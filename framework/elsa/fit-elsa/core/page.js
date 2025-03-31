/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {
  ALIGN,
  CURSORS,
  DOCK_MODE,
  EVENT_TYPE,
  FONT_STYLE,
  FONT_WEIGHT,
  MIN_WIDTH,
  PAGE_MODE,
  PAGE_OPERATION_MODE,
  PARENT_DOCK_MODE,
  SELECTION_STRATEGY,
} from '../common/const.js';
import {sleep, uuid} from '../common/util.js';

import {pageDrawer} from './drawers/pageDrawer.js';
import {animationDrawer} from './drawers/animationDrawer.js';
import {interactDrawer} from './drawers/interactDrawer.js';
import {bindMouseActions} from '../actions/mouseActions.js';
import {keyActions} from '../actions/keyActions.js';

import {container} from './container.js';
import {shapeComment, shapeComments} from './rectangle.js';
import {popupMenu} from './popupMenu.js';
import {pluginMeta} from './configuration/pluginMeta.js';
import {addCommand, deleteCommand, positionCommand} from './commands.js';
import {guideLineUtil} from '../common/guideLineUtil.js';
import {copyPasteHelper} from '../actions/copyPasteHelper.js';
import {shapeManager} from './shapeManager.js';

/**
 * 最顶层容器，代表了一页画
 * 一个graph可以有多页
 * 辉子 2020-02-20
 */
const wantedShape = (type, properties) => {
  const self = {};
  let shapeType = type;
  let shapeProperties = properties;

  self.isEmpty = () => {
    return shapeType === '';
  };

  self.clear = () => {
    shapeType = '';
    shapeProperties = undefined;
  };

  self.getType = () => {
    return shapeType;
  };

  self.getProperties = () => {
    return shapeProperties;
  };

  return self;
};

const page = (div, graph, name, id, iDrawer = interactDrawer, pDrawer = pageDrawer, aDrawer = animationDrawer) => {
  const PAGE_SCALE_MIN = 0.1;
  const PAGE_SCALE_MAX = 4.0;
  const RESOURCE_PATH = '../../resources/';
  const PAGE_MIN_WIDTH = div.clientWidth;
  const PAGE_MAX_WIDTH = 1024;
  const PAGE_MIN_HEIGHT = div.clientHeight;
  const PAGE_MAX_HEIGHT = 1024;
  div.innerHTML = '';
  const self = container(id, 0, 0, div.clientWidth, div.clientHeight, graph);
  self.clearDetections();
  self.type = 'page';
  self.itemPad = [0, 0, 0, 0];
  self.dockMode = DOCK_MODE.NONE;
  div && (div.style.overflow = 'hidden');
  /**
   * 该页面的所有形状，包括子形状，全部平铺开
   */
  (id === undefined) && (self.id = `elsa-page:${graph.uuid()}`);
  self.uuid = graph.uuid();
  self.sm = shapeManager(self);

  // shape在改page中显示为其他shape方式
  self.shapesAs = {};
  self.graph = graph;
  self.page = self;
  self.isDisplayName = true;
  self.borderColor = 'white';
  self.backColor = 'white';
  self.fontSize = 18;
  self.fontFace = 'arial';
  self.fontColor = '#ECD0A7';
  self.fontWeight = FONT_WEIGHT.BOLD;
  self.fontStyle = FONT_STYLE.NORMAL;
  self.gridSpace = 5;
  self.gridColor = 'white';
  self.focusFrameColor = 'gray';
  self.hAlign = ALIGN.LEFT;
  self.vAlign = ALIGN.TOP;
  self.groupType = 'group';
  self.enableSocial = false;
  /**
   * 将形状按坐标分块索引到不到的区域，在查找时可以迅速定位
   * 辉子 2021
   */
  self.areas = {};
  self.areaStep = 100;
  self.disableContextMenu = false;
  self.operationMode = PAGE_OPERATION_MODE.SELECTION;
  self.selectionStrategy = SELECTION_STRATEGY.BRING_TO_FRONT;
  self.mouseEvents = {
    mouseDown: {
      preventDefault: {
        exclude: ['INPUT', 'TEXTAREA'],
      },
    },
  };

  /**
   * 是否展示上下文菜单.
   *
   * @return {boolean} true/false.
   */
  self.showContextMenu = () => {
    if (self.disableContextMenu === null || self.disableContextMenu === undefined) {
      return true;
    }
    return !self.disableContextMenu;
  };

  self.coEditingCreateNew = (shapeType, x, y, idVal, values) => {
    return self.createNew(shapeType, x, y, idVal);
  };

  /**
   * isReady确定了所有shape的变化记录是否开始生效
   * 比如在页面初始化时可以不记录shape的变化记录，以免发送过多的变化请求，初始化结束后发送
   * isReady通过page.reset设置为true
   * 辉子 2021
   */
  self.isReady = false;
  /**
   * page中形状会响应键盘事件
   */
  self.keyPressAble = true;
  /**
   * 绘制承载的div，实际绘制可以是canvas，也可以是svg
   */
  self.div = div;
  /**
   * 是否显示鼠标
   * @type {boolean}
   */
  self.showCursor = () => true;
  /**
   * 交互画笔，话选择，框选，鼠标等于交互相关的绘制
   */
  self.interactDrawer = iDrawer(graph, self, div);
  /**
   * 基本画笔，画page的背景，网格，logo等
   */
  self.drawer = pDrawer(self, self.interactDrawer.sensor); // change parent from div to interact layer 2022 辉子
  /**
   * 动画层
   */
  self.animationDrawer = aDrawer(graph, self, div);
  /**
   * 是否允许缩放
   */
  self.scaleAble = true;
  /**
   * 是否可点击
   */
  self.shapeClickAble = true;
  /**
   * 画布是否可以移动
   */
  self.canvasMoveAble = true;
  self.enableHorizontalMove = true;
  self.enableVerticalMove = true;

  // ----------------------交互相关-------------------------
  /**
   * ctrl键是否按下了
   */
  self.ctrlKeyPressed = false;
  self.shiftKeyPressed = false;
  self.isKeyDown = false;

  /**
   * 是否按照page里shape的位置动态设置大小
   */
  self.dynamicResize = false;

  /**
   * 正在创建的shape
   */
  self.ongoingShape = null;

  /**
   * 编辑中shape
   */
  self.editingShape = () => {
    const editing = self.sm.findShapeBy(s => s.isEditing());
    if (editing === undefined) {
      return {
        endEdit: function () {
        },
      };
    } else {
      return editing;
    }
  };

  /**
   * 是否可以移动画布，就是所有形状都移动
   */
  self.moveable = true;
  /**
   * 是否允许水平移动
   */
  self.enableHorizontalMove = true;
  /**
   * 是否允许垂直移动
   */
  self.enableVerticalMove = true;
  /**
   * 是否正在缩放
   */
  self.zooming = false;
  /**
   * 将要生成的shape类型
   */
  self.wantedShape = wantedShape('');
  /**
   * 是否正在反序列化
   */
  self.disableReact = false;

  /**
   * setting for all shapes properties: bordercolor,fontsize.....
   */
  self.setting = {};
  self.container = self.id;
  self.text = name === undefined ? graph.title : name;

  setMouseActions(self);

  /**
   * 是否启用历史管理(重做撤销功能).
   *
   * @return {boolean} true/false.
   */
  self.enableHistory = () => {
    return false;
  };

  /**
   * 是否处于手写状态
   */
  self.inHandDrawing = false;
  self.handAction = () => self.inHandDrawing;
  self.readOnly = () => self.mode === PAGE_MODE.DISPLAY;
  self.handDrawingColor = undefined;
  self.handDrawingWidth = undefined;
  self.handDrawingAlpha = undefined;

  /**
   * 每次创建page，都会reset一下画布
   */
  self.active = () => {
    self.drawer.reset();
    self.interactDrawer.reset();

    self.width = div.clientWidth;
    self.height = div.clientHeight;
    self.mouseActions = bindMouseActions(self);
    self.keyActions = keyActions(self);
    self.keyActions.detachCopyPaste();
    self.keyActions.attachCopyPaste();
    self.reset();

    // display模式如果不开启动画，动态region等不能正常显示
    self.startAnimation();
  };

  /**
   * 动态获取page的left偏移量.
   *
   * @return {*} 偏移量的值.
   */
  self.getOffsetLeft = () => {
    return div.offsetLeft;
  };

  /**
   * 动态获取page的top偏移量.
   *
   * @return {*} 偏移量的值.
   */
  self.getOffsetTop = () => {
    return div.offsetTop;
  };

  /**
   * 清理所有图形
   */
  self.clearAllShapes = () => {
    const cmd = deleteCommand(self, self.sm.getShapes().map(s => {
      return {shape: s};
    }));
    cmd.execute();
  };

  /**
   * 批量删除图形.
   *
   * @param shapes 图形列表.
   */
  self.removeShapes = (shapes) => {
    if (!shapes || shapes.length === 0) {
      return;
    }
    const cmd = deleteCommand(self, shapes.map(s => {
      return {shape: s};
    }));
    cmd.execute(self);
  };

  /**
   * 清理page，包括图形和定时器等
   */
  self.clear = () => {
    self.drawer.container.innerHTML = '';
    self.sm.clear();

    // 需要清理掉areas信息，防止切换分页时，多个页面的shape冲突.
    self.areas = {};
    self.mouseInShape = self;
    self.timerCode = undefined;
    self.loadCode = undefined;
    self.dirties = undefined;
    self.dirty = false;
    self.commentsToShow = [];
    self.keyActions.detachCopyPaste();
    self.drawer.clear();
    self.graph.shapeCache.clearPage(div, self.id);
    self.graph.clearDomElements(div, self.id);
    self.commentManager.clear();
  };

  self.maxIndex = () => self.sm.getShapeCount();
  self.getMinIndex = () => 0;

  self.getShapeById = sId => self.sm.getShapeById(sId);
  /**
   * 得到某shape的index
   */
  self.indexOf = shape => {
    return shape.index;
  };

  /**
   * 交换两个图形的顺序.
   *
   * @param s1 图形1.
   * @param s2 图形2.
   */
  self.swap = (s1, s2) => {
    self.sm.swapShape(s1, s2);
  };

  /**
   * 是否有形状在被编辑
   */
  self.isEditing = () => {
    return self.sm.contains(s => s.isEditing());
  };

  /**
   * 新建某类型形状,可用于新建，也可以用于反序列化
   */
  self.createShape = (shapeType, x, y, idVal, ignoreLimit, parent) => {
    let newShape = null;
    let displayShapeType = shapeType;
    if (idVal && self.shapesAs[idVal]) {
      displayShapeType = self.shapesAs[idVal];
    }
    self.ignoreReact(() => {
      newShape = graph.createShape(div,
        idVal,
        displayShapeType,
        x,
        y,
        MIN_WIDTH,
        MIN_WIDTH,
        parent ? parent : self.find(x, y, s => s.isTypeof('container') && !s.denyManualAdd && s.serializable));
      // 图形创建成功后再加入shapeManager中，避免某些属性还未设置.
      self.sm.pushShape(newShape);
      if (!ignoreLimit) {
        for (; ;) {
          let containerVal = newShape.getContainer();
          if (containerVal.childAllowed(newShape) && newShape.containerAllowed(containerVal)) {
            break;
          }
          if (containerVal.isTypeof('page')) {
            newShape.ignoreChange(() => newShape.remove());
            newShape = null;
            // break;
            return;
          }
          newShape.container = containerVal.container;
        }
      }
      newShape.type = newShape.definedType ? newShape.definedType : shapeType;
      newShape.displayType = displayShapeType;
      newShape.getIndex();
      newShape.created();
      self.shapeCreated(newShape);
      self.invalidateInteraction();
    });
    return newShape;
  };

  const setNewShapeInitProperties = (properties, s) => {
    if (properties) {
      for (let p in properties) {
        if (s.serializedFields.has(p)) {
          s[p] = properties[p];
        }
      }
    }
  };

  /**
   * 新建图形，会调用初始化代码，为图形第一次生成用
   */
  self.createNew = (shapeType, x, y, idVal, properties, parent, ignoreLimit, data) => {
    let s = self.createShape(shapeType, x, y, idVal, ignoreLimit, parent);
    if (s === null) {
      return null;
    }

    self.ignoreReact(() => {
      // 这里会通过collaboration进行协同，不需要触发propertyChange.
      s.ignoreChange(() => {
        if (data) {
          data.index = s.index;
          !data.container && (data.container = s.container);
          s.deSerialize(data);
        }
        setNewShapeInitProperties(properties, s);
        s.load();
        s.invalidateAlone();
        s.initialize();
        s.initialized();
        const pDockNotNone = s.pDock !== undefined && s.pDock !== PARENT_DOCK_MODE.NONE;
        const cDockNotNone = s.getContainer().dockMode !== undefined && s.getContainer().dockMode !== DOCK_MODE.NONE;
        if (pDockNotNone || cDockNotNone) {
          s.getContainer().invalidate();
        }
        if (s.serializable) {
          self.graph.collaboration.invoke({
            method: 'new_shape', page: self.id, shape: s.id, value: s.serialize(), mode: self.mode,
          });
        }
      });
    });
    return s;
  };

  self.getFocusedShapes = () => self.sm.getShapes(s => s.isFocused);

  self.filterPositionShapes = (x, y) => {
    const key = `${Math.floor(x / self.areaStep)}-${Math.floor(y / self.areaStep)}`;
    const area = self.areas[key];
    return area ? area.shapes : [];
  };

  /**
   * 将图形添加到区域中，提升查询效率.
   *
   * @param x 横坐标.
   * @param y 纵坐标.
   * @param shape 图形对象.
   */
  self.addShapeToAreas = (x, y, shape) => {
    const step = shape.page.areaStep;
    const key = `${(x / step)}-${(y / step)}`;
    let area = self.areas[key];
    if (!area) {
      area = {l: x, t: y, r: x + step, b: y + step, shapes: []};
      self.areas[key] = area;
    }
    area.shapes.push(shape);
    return area;
  };

  /**
   * 在某个坐标下，满足conditoin的所有shape
   */
  self.findAll = (x, y, condition) => {
    let shapes = self.filterPositionShapes(x, y);
    if (shapes === undefined || shapes.length === 0) {
      return [self];
    }
    const baseCondition = (s) => s.getMouseOnConnector(x, y) !== null || s.contains(x, y);

    // 先看是否满足基本条件：可见;x,y命中
    const bases = shapes.filter(s => condition(s)).filter(s => baseCondition(s));
    if (bases.length === 0) {
      return [self];
    }

    let finals = bases;
    // 去除里面的parents
    const toRemove = [];
    finals.forEach(s => {
      let parent = s;
      while (parent.id !== self.page.id) {
        parent = parent.getContainer();

        // 父容器如未在候选列表中，则无需排除
        // 父容器在候选列表中，并且鼠标命中了connector和region，则无需排除
        if (!finals.includes(parent) || (parent.isFocused && parent.priorityContains(x, y))) {
          continue;
        }
        toRemove.push(parent);
      }
    });
    finals = finals.filter(s => !toRemove.includes(s));
    return finals;
  };

  /**
   * 在某个坐标下，满足condition的最符合条件shape
   */
  self.find = (x, y, condition) => {
    let conditionVal = condition;
    !conditionVal && (conditionVal = () => true);
    const shapes = self.findAll(x, y, conditionVal);
    if (shapes.length === 0) {
      return self;
    }
    if (shapes.length === 1) {
      return shapes[0];
    }
    const selectedShape = shapes.sort((sa, sb) => sb.getSelectPriority(x, y) - sa.getSelectPriority(x, y))[0];
    return selectedShape ? selectedShape : self; // 基本条件不满足就什么也没找到
  };

  self.handleComment = (message, shape) => {
    let shapeVal = shape;
    if (!shapeVal) {
      shapeVal = self.id === message.shape ? self :
        self.sm.findShapeBy(s => s.id === message.shape);
    }
    if (shapeVal) {
      if (shapeVal.comments === undefined) {
        shapeVal.comments = [];
      }
      shapeVal.comments.push(message.value);
      if (shapeVal.comments.length === 1) {
        shapeVal.drawer.drawRegions();
      }
      self.showComment(shapeVal, message.value, 'bullet');
    }
  };
  self.showTip = (x, y, text, shape) => {
    const tips = 'tips';
    let tip = self.sm.getShapeById(tips);
    if (text === undefined || text === '') {
      if (tip) {
        tip.remove();
      }
    } else {
      if (tip === undefined) {
        self.ignoreReact(() => {
          const t = shapeComment(x, y - 20, shape);
          t.id = tips;
          t.autoWidth = true;
          t.height = 22;
          t.borderColor = 'white';
          t.backColor = 'lightyellow';
          t.text = text;
          t.minWidth = undefined;
          t.editable = false;
          t.selectable = false;
          t.invalidate();
        });
      } else {
        tip.text = text;
        tip.moveTo(x, y - 20);
      }
    }
  };

  self.commentManager = shapeComments();
  /**
   * 浮出某comment
   * 辉子 2021
   */
  self.showComment = async (shape, comment, mode) => {
    if (comment.trim() === '') {
      return;
    }
    self.commentManager.add(shape, comment, mode);
  };

  const keyPressed = self.keyPressed;
  self.keyPressed = e => {
    const base = (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey);

    if (base && e.code === 'KeyP' && self.enableSocial) {
      let shape = self.find(self.mousex, self.mousey, s => s.serializable);
      const comment = self.ignoreReact(() => shapeComment(self.mousex, self.mousey, shape));
      comment.beginEdit();
      comment.invalidate();
      return false;
    }

    if (base && e.code === 'KeyW') {
      self.inHandDrawing = !self.inHandDrawing;
      return false;
    }

    if (e.code === 'KeyO') {
      self.x = self.y = 0;
      self.zoom();
      return false;
    }

    return self.modeKeyPressed(e, keyPressed);
  };

  /**
   * 用于不同模式下的按钮事件重写.
   *
   * @param e 事件对象.
   * @param keyPressedFunc 原始的keyPressed方法对象.
   * @return {boolean} true/false.默认返回true.
   */
  self.modeKeyPressed = (e, keyPressedFunc) => {
    return true;
  };

  self.initialize = () => {
    // init areas
    self.warmupAudio('click', 'click.mp3');
  };

  self.dispose = () => {
    self.stopAnimation();
    self.unRegistCommunication();
    self.div.innerHTML = '';
    self.disposed = true;
  };

  // -----------------------鼠标事件-------------------------
  const zoom = (scaleX, scaleY, x, y) => {
    if (scaleX < PAGE_SCALE_MIN || scaleY < PAGE_SCALE_MIN) {
      return false;
    }
    if (scaleX > PAGE_SCALE_MAX || scaleY > PAGE_SCALE_MAX) {
      return false;
    }
    let oldScaleX = self.scaleX;
    let oldScaleY = self.scaleY;
    self.scale(scaleX, scaleY);

    if (x !== undefined && y !== undefined) {
      // 以（0,0）为圆心缩放，拉回被移走的，加上上一次的移动；注意/ scaleX是因为已经缩放过，移动时需要还原回去
      let newX = -(((x * scaleX) - (x * oldScaleX)) / scaleX) + ((self.x * oldScaleX) / scaleX);
      let newY = -(((y * scaleY) - (y * oldScaleY)) / scaleY) + ((self.y * oldScaleY) / scaleY);
      self.moveTo(newX, newY);
    }
    self.interactDrawer.positonBar.update();
    return true;
  };

  /**
   * 画布放大缩小
   */
  self.zoomTo = (scaleX, scaleY, x, y) => {
    if (!self.mouseSensitive) {
      return;
    }
    if (!self.scaleAble) {
      return;
    }
    if (zoom(scaleX, scaleY, x, y)) {
      self.zoomed(self.scaleX, self.scaleY, self.x, self.y);
    }
  };

  /**
   * 画布放大缩小
   */
  self.zoom = (rate, x, y) => {
    if (!self.mouseSensitive) {
      return;
    }
    if (!self.scaleAble) {
      return;
    }
    if (rate === undefined) {
      self.scaleX = self.scaleY = 1;
      self.moveTo(0, 0);
    } else {
      let scaleX = self.scaleX + rate;
      let scaleY = self.scaleY + rate;
      if (!zoom(scaleX, scaleY, x, y)) {
        return;
      }
    }
    self.zoomed(self.scaleX, self.scaleY, self.x, self.y);
  };

  /**
   * 画布滚动
   */
  self.scroll = (offset, direction) => {
    let x = 0;
    let y = 0;
    direction === 'y' ? y = offset : x = offset;
    if (!self.canvasMoveAble) {
      return;
    }
    if (self.enableHorizontalMove) {
      self.x -= x;
    }
    if (self.enableVerticalMove) {
      self.y -= y;
    }
  };
  self.moveTo = (newX, newY) => {
    let newXVal = newX;
    let newYVal = newY;
    let expandX = self.div.clientWidth / self.scaleX;
    let expandY = self.div.clientHeight / self.scaleY;
    let frame = self.getShapeFrame();
    let tipSize = 20;
    if (newXVal > -frame.x1 + expandX - tipSize) {
      newXVal = -frame.x1 + expandX - tipSize;
    } else if (newXVal < -frame.x2 + tipSize) {
      newXVal = -frame.x2 + tipSize;
    } else {
      // 没问题，继续执行
    }
    if (newYVal > -frame.y1 + expandY - tipSize) {
      newYVal = -frame.y1 + expandY - tipSize;
    } else if (newYVal < -frame.y2 + tipSize) {
      newYVal = -frame.y2 + tipSize;
    } else {
      // 没问题，继续执行
    }
    self.x = newXVal;
    self.y = newYVal;
    self.drawer.transform();
  };

  /**
   * 适配屏幕居中
   *
   * @param minScale 最小缩放比
   * @param maxScale 最大缩放比
   */
  self.fitScreen = (minScale, maxScale) => {
    const shapeFrame = self.getShapeFrame();
    const xScale = self.width / Math.abs(shapeFrame.x2 - shapeFrame.x1);
    const yScale = self.height / Math.abs(shapeFrame.y2 - shapeFrame.y1);
    let scale = Math.min(xScale, yScale, maxScale ?? PAGE_SCALE_MAX);
    scale = Math.max(minScale ?? PAGE_SCALE_MIN, scale);
    self.toScreenCenter(scale, scale);
  };

  self.resize = () => {
    if (!self.dynamicResize) {
      return;
    }
    let shapes = self.getShapes();
    if (shapes.length === 0) {
      return;
    }
    let minX = shapes.min(s => s.x);
    let minY = shapes.min(s => s.y);
    let maxWidth = shapes.max(s => s.x + s.width);
    let maxHeight = shapes.max(s => s.y + s.height);

    shapes.forEach(s => s.moveTo(minX < 0 ? (s.x - minX) : s.x, minY < 0 ? (s.y - minY) : s.y));

    maxWidth += minX < 0 ? minX : 0;
    maxHeight += minY < 0 ? minY : 0;

    let width = maxWidth > PAGE_MAX_WIDTH ? PAGE_MAX_WIDTH : maxWidth + 10;
    let height = maxHeight > PAGE_MAX_HEIGHT ? PAGE_MAX_HEIGHT : maxHeight + 10;
    self.width = width < PAGE_MIN_WIDTH ? PAGE_MIN_WIDTH : width;
    self.height = height < PAGE_MIN_HEIGHT ? PAGE_MIN_HEIGHT : height;
    self.adaptLayout();
  };

  self.adaptLayout = () => {
    self.interactDrawer.reset();
    self.animationDrawer.reset();
  };

  self.moveToContainer = (s, specifiedContainer) => {
    // add to avoid container change in page initializing....  huizi 2022.07.08
    if (!self.isReady) {
      return;
    }

    // 如果指定了容器，那么直接设置为指定容器.
    if (specifiedContainer) {
      s.container = specifiedContainer;
      return;
    }

    // 当前的container符合基本条件就不动了
    const checkParent = self.find(s.x + (s.width / 2),
      s.y + (s.height / 2),
      s1 => s1.isTypeof('container') && s1 !== s);
    if (checkParent.id === s.container) {
      return;
    }

    // 否则找到符合可以move in的container
    const condition = s1 => {
      return s1.isTypeof('container') &&
        s1 !== s &&
        s.containerAllowed(s1) &&
        s1.childAllowed(s) &&
        !s1.denyManualAdd &&
        !s.isMyBlood(s1);
    };
    const parent = self.find(s.x + (s.width / 2), s.y + (s.height / 2), condition);

    /*
     * 满足下列三个条件才能设置container:
     * 1、parent不是当前容器
     * 2、允许parent成为图形的容器
     * 3、容器允许图形成为其孩子
     *
     * 这里还要判断第3点的原因是因为parent有可能是page，而page也可能存在childAllowed逻辑.
     * page不会走上面的condition逻辑，而是所有其他container都不符合条件时，直接返回page对象.
     * 因此这里要做个保底
     * * 注意 * 这里主要是规避在ppt场景中，图形被拖出frame层级发生变化的问题.
     */
    if (parent.id !== s.container && s.containerAllowed(parent) && parent.childAllowed(s)) {
      s.container = parent.id;
    }
  };

  /**
   * 形状成组
   */
  self.group = shapes => {
    if (shapes.length <= 1) {
      return;
    }
    let g = self.createNew(self.groupType, 0, 0);
    g.select();
    g.group(shapes);
  };

  /**
   * fill screen when display graph
   */
  self.fillScreen = (refresh) => {
  };

  self.cleanPresentationDiv = () => {
    let viewer = document.getElementById('present');
    if (viewer !== null) {
      viewer.parentNode.removeChild(viewer);
    }
    self.graph.closeCollaboration();
    self.close();
  };

  self.fullScreenCancelled = () => {
    self.isFullScreen = false;
    if (document.webkitIsFullScreen || document.mozFullScreen || document.msFullscreenElement !== undefined) {
      return;
    }
    div.style.width = `${self.originWidth}px`;
    div.style.height = `${self.originHeight}px`;
    self.width = self.originWidth;
    self.height = self.originHeight;
    self.graph.fullScreenCancelled(self.id);

    // 清理演示产生的div
    self.cleanPresentationDiv();

    // 删除监听器，否则，监听去会随着演示次数的增加而增加.导致该方法被调用多次.
    if (document.removeEventListener) {
      document.removeEventListener('fullscreenchange', self.fullScreenCancelled, false);
      document.removeEventListener('mozfullscreenchange', self.fullScreenCancelled, false);
      document.removeEventListener('MSFullscreenChange', self.fullScreenCancelled, false);
      document.removeEventListener('webkitfullscreenchange', self.fullScreenCancelled, false);
    }
  };

  /**
   * 取消全屏.
   */
  self.cancelFullScreen = () => {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    } else {
      if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      }
    }
  };

  self.fullScreen = async () => {
    self.isFullScreen = true;
    self.originWidth = self.width;
    self.originHeight = self.height;

    try {
      let element = div;
      if (element.requestFullscreen) {
        await element.requestFullscreen();
      } else if (element.mozRequestFullScreen) {
        await element.mozRequestFullScreen();
      } else if (element.webkitRequestFullscreen) {
        await element.webkitRequestFullscreen();
      } else {
        if (element.msRequestFullscreen) {
          await element.msRequestFullscreen();
        }
      }

      await sleep(300); // element.clientHeight会莫名其妙的延时，结果不对，只好在这里等300ms
      self.width = element.clientWidth;
      self.height = element.clientHeight;
    } catch (e) {
      // 没关系，继续，不影响其他错误信息的处理.
    }

    if (document.addEventListener) {
      document.addEventListener('fullscreenchange', self.fullScreenCancelled, false);
      document.addEventListener('mozfullscreenchange', self.fullScreenCancelled, false);
      document.addEventListener('MSFullscreenChange', self.fullScreenCancelled, false);
      document.addEventListener('webkitfullscreenchange', self.fullScreenCancelled, false);
    }
  };

  /**
   * 编辑该页
   * 辉子 2022
   */
  const editPage = async (data) => {
    await self.deSerialize(data);
    self.graph.activePage = self;

    // 需要提醒外部进行了页面编辑.
    self.graph.dirtied(self.graph.serialize(), {page: self.id, action: 'page_edited', session: self.graph.session});
  };

  /**
   * 显示该页
   * 辉子 2022
   */
  const displayPage = async data => {
    await self.deSerialize(data);
    self.fillScreen();
  };

  /**
   * 播放该页
   * 辉子 2022
   */
  const presentPage = async (data, afterPresent) => {
    await presentOrViewPage(data);
    if (afterPresent) {
      afterPresent(self);
    }
  };

  /**
   * 跟随播放
   * 辉子 2022
   */
  const viewPage = async (data, afterView) => {
    if (data !== undefined) { // 找不到数据，重新下载一次演讲内容
      await presentOrViewPage(data);
    } else {
      self.graph.viewPresent();
    }
    if (afterView) {
      afterView(self);
    }
  };

  let modes = {};
  modes[PAGE_MODE.VIEW] = viewPage;
  modes[PAGE_MODE.PRESENTATION] = presentPage;
  modes[PAGE_MODE.CONFIGURATION] = editPage;
  modes[PAGE_MODE.DISPLAY] = displayPage;

  self.take = async (data, callback) => {
    await modes[self.mode](data, callback);
    self.active();
  };

  const presentOrViewPage = async data => {
    self.stopAnimation();

    // 载入下一页，转换过程中page对象没有改变，而是将数据全部替换了
    await self.deSerialize(data);
    await self.fullScreen();
    self.fillScreen();
    self.startAnimation();
  };
  /**
   * 跟随播放时的下一步
   * 辉子 2022
   */
  self.moveNext = async () => {
    let nextPage = self.graph.getNextPage(self.id);
    if (nextPage === undefined) {
      return false;
    }
    await self.take(nextPage);
    return true;
  };

  /**
   * 跟随播放时的上一步
   * 辉子 2022
   */
  self.movePrevious = async () => {
    let previousPage = self.graph.getPreviousPage(self.id);
    if (previousPage === undefined) {
      return false;
    }
    await self.take(previousPage);
    return true;
  };

  /**
   * 是否允許演示動畫
   */
  self.allowPresent = () => self.mode === PAGE_MODE.PRESENTATION || self.mode === PAGE_MODE.VIEW;

  /**
   * select all
   */
  self.selectAll = () => {
    const shapes = self.sm.getShapes(s => s.container === self.id && s.getSelectable());
    shapes.forEach(s => s.select());
  };

  // ------------------invalidations------------------------
  self.invalidateInteraction = position => {
    self.interactDrawer.draw(position);
  };

  self.reactCoEdit = () => {
  };

  /**
   * 得到协作消息
   * 辉子 2022
   */
  self.onMessage = (message, localChange) => {
    if (!self.graph) {
      return;
    }
    // 发起者不用再被动更新（存在小概率风险一个page存在两个编辑页面）
    if (message.from === self.graph.session.id && (!localChange || self.graph.activePage === self)) {
      return;
    }

    // 不是本页（存在小概率风险有sharedShape导致不更新）
    if (message.page !== self.id) {
      return;
    }
    switch (message.topic) {
      case 'shape_added': {
        if (self.sm.contains(s => s.id === message.value.id)) {
          return;
        }
        pluginMeta.import(message.value.type, self.graph).then(() => {
          const newShape = self.createShape(message.value.type, message.value.x, message.value.y, message.value.id, true);
          newShape.deSerialize(message.value);
          newShape.ignoreChange(() => newShape.reset());
        });
        break;
      }
      case 'shape_index_changed': {
        const shape = self.sm.getShapeById(message.shape);
        self.sm.updateShapes(writer => writer.moveShapeTo(shape, message.value.toIndex));
        break;
      }
      case 'page_shape_data_changed': {
        message.value.forEach(async shapeData => {
          if (shapeData.shape === self.id) {
            self.ignoreChange(() => {
              for (let f in shapeData.properties) {
                if (Object.prototype.hasOwnProperty.call(shapeData, f)) {
                  self.setProperty(f, shapeData.properties[f]);
                }
              }
            });
            return;
          }
          // shape data
          let target = self.sm.findShapeBy(s => s.id === shapeData.shape || s.entangleId === shapeData.shape);

          // 如果shape没有找到，并且container不为空，说明删除的形状被恢复了,将缓存的形状加入
          if (target === undefined && shapeData.properties.container !== '' && shapeData.properties.container !== undefined) {
            let recover = self.graph.removedCache.shapes.find(s => s.id === shapeData.shape);
            if (recover === undefined) {
              const s = self.graph.activePage.sm.getShapeById(shapeData.shape);
              s && (recover = s.serialize());
            }
            if (recover !== undefined) {
              await pluginMeta.import(recover.type, self.graph);

              // 再找一遍，防止异步后的重复创建
              target = self.sm.findShapeBy(s => s.id === shapeData.shape);
              if (!target) {
                target = self.createShape(recover.type, recover.x, recover.y, recover.id);
                target.deSerialize(recover);
                target.ignoreChange(() => {
                  self.sm.updateShapes(writer => writer.moveShapeTo(target, target.index));
                });
              }
            }
          }

          if (target === undefined) {
            return;
          }

          if (shapeData.properties.container !== '') {
            /*
             * 1、在远程协同的场景下，这里对图形对象设置property，主要是为了触发detections，
             *    因为在subscriptions中已经对_data进行了修改.
             * 2、本地协同场景下，既起到触发detection的作用，也同时修改_data数据.
             */
            target.ignoreChange(() => {
              for (let f in shapeData.properties) {
                if (Object.prototype.hasOwnProperty.call(shapeData, f)) {
                  target.setProperty(f, shapeData.properties[f]);
                }
              }
            });
            target.onMessageDataChange(shapeData.properties);
          } else {
            target.ignoreChange(() => target.remove());
          }
        });
        break;
      }
      case 'comment': {
        self.handleComment(message, null);
        break;
      }
      case 'add_freeline_point': {
        const free1 = self.sm.findShapeBy(s => s.id === message.shape);
        if (free1) {
          message.value.forEach(point => {
            free1.addPoint(point.x, point.y, true);
          });
        }
        break;
      }
      case 'freeline_done': {
        const free2 = self.sm.findShapeBy(s => s.id === message.shape);
        if (free2) {
          free2.done(true);
        }
        break;
      }
      default:
        break;
    }
  };

  let invalidate = self.invalidate;
  self.invalidate = () => {
    if (self.disposed) {
      return;
    }

    self.resize();
    invalidate.apply(self);
    self.invalidateInteraction();
    self.drawer.draw();
    if (self.isDirty()) {
      self.serialize(); // make sure when page rendered, data is serialized in graph
    }
  };
  self.invalidateAlone = () => {
  };

  let reset = self.reset;
  self.reset = () => {
    let begin = new Date();
    self.isReady = false;
    reset.call(self);
    self.isReady = true;
    self.onLoaded();
  };
  let animation;
  self.startAnimation = () => {
    if (animation !== undefined) {
      return;
    }
    animation = window.requestAnimationFrame(self.invalidateAnimation);
    self.graph.lastPage = self;
    self.dirties = undefined;
  };
  self.stopAnimation = () => {
    if (animation === undefined) {
      return;
    }
    window.cancelAnimationFrame(animation);
    animation = undefined;
  };
  self.close = () => {
    self.stopAnimation();
    self.graph = null;
  };
  let timerCodeFunction;
  self.timer = (pageVal, time) => {
    if (self.timerCode && timerCodeFunction === undefined) {
      timerCodeFunction = eval(`(${self.timerCode})`);
    }
    if (timerCodeFunction) {
      timerCodeFunction(pageVal, time);
    }
  };

  // 共享其他图形中的shape
  self.referShape = (x, y, shapeData, pageId, graphId) => {
    const shapeType = shapeData.type;
    const idVal = shapeData.id;
    shapeData.shared = true;
    self.graph.collaboration.tag = {sharedPage: pageId, sharedGraph: graphId};
    self.createNew(shapeType, x, y, idVal, shapeData);
  };

  /**
   * 发送变更数据给协同服务器.
   */
  self.sendDirties = () => {
    const dirties = self.dirties;
    if (!dirties) {
      return;
    }

    const dirtyMap = new Map();
    Object.keys(dirties).forEach(shapeId => {
      const dirty = dirties[shapeId];
      const pageId = dirty.pageId;

      // 这里需要将pageId删除，否则，在传给协同服务器时，会在shape的属性上加上pageId（该属性不该被序列化）
      delete dirty.pageId;
      const dirtyMessage = {shape: shapeId, properties: dirty};
      let pageDirtyMessages = dirtyMap.get(pageId);
      if (!pageDirtyMessages) {
        pageDirtyMessages = [];
        dirtyMap.set(pageId, pageDirtyMessages);
      }

      // @maliya 暂时修改 待讨论正式方案后再优化：兼容协同场景下，调整dirties中page的basePage属性修改为第一个，避免page中引用图形属性修改在前，导致协同方还未来得及创建引用图形  就收到了引用图形的属性修改
      if (dirty.type === 'presentationPage') {
        pageDirtyMessages.unshift(dirtyMessage);
      } else {
        pageDirtyMessages.push(dirtyMessage);
      }
    });

    if (dirtyMap.has(self.id)) {
      /*
       * not sure if here is the best time to serialize
       * 这里需要判断pageId是否包含当前page的id，原因有两个：
       * 1、说明当前page没有改动，那么不需要序列化
       * 2、有可能当前page已经被删除，但是page实例还未回收(或activePage的id还没有变更)
       *    当新增一页，撤销，恢复，再撤销时，很有可能代码走到这里时，self.id是已被撤销的page的id.
       *    此时就会导致，命名已经撤销了，但是页面上还显示了该页面的数据，原因就是因为这里的serialize()之后，
       *    会调用serialized方法，serialized()方法中，会将数据重新插入到graph的pages中。
       */
      self.serialize();
    }

    dirtyMap.forEach((data, pageId) => {
      if (!graph.getPageDataById(pageId)) {
        return;
      }

      self.graph.collaboration.invoke({
        method: 'change_page_shape_data', page: pageId, value: data, mode: self.mode,
      });

      try {
        graph.dirtied(graph.serialize(), {
          page: pageId,
          action: 'page_data_changed',
          session: self.graph.session,
        });
      } catch (e) {
        // 没关系，继续，不影响其他错误信息的处理.
      }

      self.triggerEvent({type: EVENT_TYPE.PAGE_DIRTY, value: pageId});
    });

    self.dirties = undefined;
  };

  self.animate = () => {
    self.sm.getShapes().forEach(s => s.animate && s.animate());
  };
  self.actions = [];

  /**
   * 提供所有动画的频域时钟
   * huizi 2021
   */
  self.invalidateAnimation = async (stamp) => {
    while (self.actions.length > 0) {
      self.actions.shift()();
    }
    self.animate();
    self.animationDrawer.draw();
    self.sendDirties();
    self.fireEvent();
    try {
      if (self.timer) {
        self.timer(self, new Date());
      }
    } catch (e) {
      // 没关系，继续，不影响其他错误信息的处理.
    }
    animation = window.requestAnimationFrame(self.invalidateAnimation);
  };

  const warmAudio = {};

  self.warmupAudio = (nameVal, path) => {
    if (warmAudio[nameVal] !== undefined) {
      return;
    }
    const audioPlayer = new Audio();
    audioPlayer.src = RESOURCE_PATH + path;
    warmAudio[nameVal] = audioPlayer;
  };

  self.playAudio = () => {
    return undefined;
  };

  self.commentRegion.getVisibility = () => self.comments !== undefined && self.comments.length > 0;
  self.commentRegion.gety = () => 5;
  self.beginEdit = (x, y) => {
  };

  // ----------------------------------------------------

  // -----------------serialization----------------------
  self.shapeSerialized = shape => {
  };

  self.serialized = () => {
  };

  let serialize = self.serialize;
  /**
   * 序列化page所有数据
   * 辉子 2020
   */
  self.serialize = () => {
    let serialized = serialize.apply(self);
    serialized.dirty = self.dirty;
    serialized.isPage = true;
    serialized.mode = self.mode;
    serialized.shapes = self.sm.getShapes(s => s.serializable).map(s => {
      let sd = s.serialize();
      sd.dirty = s.dirty;
      self.shapeSerialized(sd);
      return sd;
    });
    self.serialized(serialized);
    return serialized;
  };

  self.shapeDeSerialized = shape => {
  };

  let deSerialized = self.deSerialized;
  self.deSerialized = pageVal => {
    deSerialized.apply(self, pageVal);
    self.interactDrawer.pageIdChange();
    self.drawer.pageIdChange();
    self.animationDrawer.pageIdChange();
  };

  let deSerialize = self.deSerialize;
  self.deSerialize = (serialized) => {
    const shapes = serialized.shapes.orderBy(s => s.index).filter(s => s.container !== '');
    self.ignoreReact(async () => {
      self.clear();
      deSerialize.apply(self, [serialized]);
      for (let i = 0; i < shapes.length; i++) {
        const s = shapes[i];

        // 这里需要缓存container，否则，在creatShape时可能找到错误的parent.导致_data里面的container都错了.
        // 找到错误的parent之后，在构造shape的过程中，会设置container，由于引用的是同一份数据，所以s的数据也会变化，
        // 再通过s进行deSerialize也是错误的container.
        const containerVal = s.container;
        let type = s.type;
        let createdShape = self.createShape(type, s.x, s.y, s.id, true);
        createdShape.deSerialize(s);
        createdShape.container = containerVal;
        self.shapeDeSerialized(createdShape);
      }
      zoom(self.scaleX, self.scaleY, self.x, self.y);
    });
  };

  /**
   * 页面是否dirty
   * 页面自身属性改变，或者页面任意shape属性改变
   * 辉子 2021
   */
  self.isDirty = () => self.dirty || self.sm.contains(s => s.dirty);
  self.dirty = false;
  //-------------------------------------------------------

  // --------------------------detection------------------------------
  self.addDetection(['id'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    self.sm.getShapes(s => s.container === preValue).forEach(s => s.container = value);
  });
  self.addDetection(['backColor', 'background', 'backgroundGrid'], (property, value, preValue) => {
    self.render();
  });

  // ----------------------------------------------------------------------------

  // ------------------------events-------------------------

  /**
   * page scale时的事件
   */
  self.zoomed = (rateX, rateY, baseX, baseY) => {
  };

  /**
   * page里新增shape时事件
   * 在新增shape,复制粘贴时触发
   */
  self.shapeCreated = shape => {
  };

  /**
   * page里某个shape开始编辑事件
   */
  self.shapeBeginEdit = shape => {
  };
  /**
   * page里某个shape的text改变了
   * 与shape.textChanged事件一致
   */
  self.textChanged = (shape, value, preValue) => {
  };

  self.dirtied = () => {
  };

  /**
   * 在page.deserialize开始时触发
   */
  let load = self.load;
  self.load = (pageVal) => {
    self.disableReact = false;
    load.call(self, pageVal);
  };

  /**
   * 在page.deserialize结束时触发
   */
  self.onLoaded = () => {
    if (self.graph.auxiliaryToolConfig.enableGuides) {
      self.guideLineInstance = guideLineUtil(self);
    }
  };

  setEventHandler(self);

  setCopyPaste(self);

  /**
   * 重写getConfigurations方法.page和shape的配置不一致.
   *
   * @return {*[]} 配置数组.
   */
  self.getConfigurations = () => {
    const backColorConfig = self.configFactory.get('backColor', self);
    backColorConfig.group[0].name = '设置背景格式';
    backColorConfig.group[1].name = '';
    return [backColorConfig];
  };

  /**
   * 执行时禁用react能力.【慎用，可能不对，因为内部react的调用是异步的】
   *
   * @param fn 待执行的方法.
   * @return {*} 方法的返回值.
   */
  self.ignoreReact = (fn) => {
    const disableReact = self.disableReact;
    self.disableReact = true;
    try {
      return fn();
    } finally {
      self.disableReact = disableReact;
    }
  };

  self.ignoreReactAsync = async (fn) => {
    const disableReact = self.disableReact;
    self.disableReact = true;
    try {
      return await fn();
    } finally {
      self.disableReact = disableReact;
    }
  };

  /**
   * 获取滚动条坐标.
   * 当画布中出现滚动条时，会出现scrollLeft和scrollTop，计算鼠标位置时，需要用到该坐标.
   * 否则，会导致鼠标位置计算错误，无法选中对应的图形.
   *
   * @return {{x: (*|string|number), y: (*|string|number)}} 坐标信息.
   */
  self.getScrollPosition = () => {
    return {
      x: self.interactDrawer.sensor.scrollLeft,
      y: self.interactDrawer.sensor.scrollTop,
    };
  };

  /**
   * 重新计算祖先节点的zoom.
   */
  self.refreshZoom = () => {
    self.interactDrawer.refreshZoom();
  };

  /**
   * 设置想要的插入的图形.当再次点击画布时，创建图形.
   *
   * @param shapeType 图形类型.
   * @param properties 图形属性.
   */
  self.want = (shapeType, properties) => {
    self.page.wantedShape = wantedShape(shapeType, properties);
  };

  /**
   * 获取page中所有shape总的边框，通常用于布局控制，获取page中的实际的内容位置
   *
   * @return {{y1: number, x1: number, y2: number, x2: number}} 坐标范围
   */
  self.getShapeFrame = () => {
    const frame = {x1: Number.MAX_VALUE, y1: Number.MAX_VALUE, x2: Number.MIN_VALUE, y2: Number.MIN_VALUE};
    self.sm.getShapes().forEach(shape => {
      let shapeFrame = shape.getShapeFrame();
      frame.x1 = Math.min(frame.x1, shapeFrame.x1);
      frame.y1 = Math.min(frame.y1, shapeFrame.y1);
      frame.x2 = Math.max(frame.x2, shapeFrame.x2);
      frame.y2 = Math.max(frame.y2, shapeFrame.y2);
    });
    return frame;
  };

  setKeyActions(self);

  self.ifHideSelection = () => {
    return true;
  };

  self.ifAddCommand = () => {
    return true;
  };

  /**
   * 获取受影响的线.
   *
   * @param focusedShapes 被选中的图形.
   * @return {T[]} 线的列表.
   */
  self.getEffectLines = (focusedShapes) => {
    return self.sm.getShapes(s => s.isTypeof('line')).filter(l => {
      return !focusedShapes.contains(s => s === l) &&
        focusedShapes.some(s => s === l || s.id === l.fromShape || s.id === l.toShape);
    });
  };

  /**
   * 删除被选中的shapes.
   */
  self.onDelete = () => {
    const toBeDeletedShapes = self.getFocusedShapes().filter(s => s.isInConfig());
    createDeleteCommand(self, toBeDeletedShapes);
    return toBeDeletedShapes;
  };

  return self;
};

const createDeleteCommand = (pageVal, shapes) => {
  const cmd = deleteCommand(pageVal, shapes.map(s => {
    return {shape: s, focused: s.getFocused()};
  }));
  cmd.execute(pageVal);
};

const setCopyPaste = pageVal => {
  pageVal.copyPasteHelper = copyPasteHelper();

  /**
   * 序列化被选中的shape，序列化后才好复制粘贴，反序列化成新的shape
   * 辉子
   */
  let serializeShapes = (shapes) => {
    let all = [];
    let addMyFamily = me => {
      if (all.find(s => s.id === me.id)) {
        return;
      }

      all.push(me);
      if (me.isTypeof('container')) {
        me.getShapes().forEach(s => addMyFamily(s));
      }
    };
    for (let i = 0; i < shapes.length; i++) {
      addMyFamily(shapes[i]);
    }
    return all.map(shape => shape.serialize()).filter(s => s != null);
  };

  pageVal.copy = shapeIds => {
    let shapes = shapeIds.map(shapeId => pageVal.getShapeById(shapeId));
    return serializeShapes(shapes);
  };

  pageVal.onCopy = (shapes) => {
    if (shapes.length === 0) {
      return undefined;
    }
    if (shapes.length === 1 && shapes[0].copy) {
      // 形状内拷贝
      const data = shapes[0].copy();
      if (data) {
        return {
          data: [data], type: data.type, cut: () => {
            shapes[0].cut(data);
          },
        };
      }
    }
    // 拷贝形状
    const result = pageVal.copyShapes(shapes);
    return {
      data: result.copied, type: 'shape', cut: () => {
        createDeleteCommand(pageVal, result.related);
      },
    };
  };

  /**
   * 拷贝图形.
   *
   * @param shapes 待拷贝的图形集合.
   * @return {{copied: *[], related: *[]}} 拷贝后的结果集.
   */
  pageVal.copyShapes = (shapes) => {
    const copiedShapes = [];
    const relatedShapes = [];
    shapes.forEach(shape => {
      const related = shape.getRelated();
      related.remove(r => relatedShapes.contains(s => s === r));
      if (related.length === 0) {
        return;
      }
      copiedShapes.push.apply(copiedShapes, serializeShapes(related));
      relatedShapes.push.apply(relatedShapes, related);
    });

    return {
      copied: copiedShapes, related: relatedShapes,
    };
  };

  /**
   * 按照树状结构，调整待粘贴的图形顺序，保障container先创建
   * @param shapesData
   * @param pastePage
   * @returns {*[]}
   */
  const reRangePasteShapeData = (shapesData, pastePage) => {
    /**
     * 递归的将子树的图形加入列表
     * @param rangedShapeData
     * @param data
     * @param containShapes
     */
    const addRecursively = (rangedShapeData, data, containShapes) => {
      rangedShapeData.push(data);
      const subList = containShapes[data.id];
      if (subList) {
        subList.forEach(shape => addRecursively(rangedShapeData, shape, containShapes));
      }
    };

    let rangedShapeData = [];
    let containShapes = {};
    let ids = new Set();
    shapesData.forEach(data => {
      (!containShapes[data.container]) && (containShapes[data.container] = []);
      containShapes[data.container].push(data);
      ids.add(data.id);
    });

    shapesData.forEach(data => {
      // 找到复制的根节点
      if (!ids.has(data.container)) {
        addRecursively(rangedShapeData, data, containShapes);
        // 根节点的父亲暂时设置为页面
        data.container = pastePage.id;
      }
    });

    return rangedShapeData;
  };

  /**
   * 粘贴elsa 体系中的任意shape
   * 辉子
   */
  let pasteShape = (shapesData, pastePage, calcX, calcY) => {
    let shapesDataVal = shapesData;
    pastePage.sm.getShapes(shape => shape.focused).forEach(shape => shape.unSelect());
    shapesDataVal = reRangePasteShapeData(shapesDataVal, pastePage);
    // 把由于copy重号的shape重新定义id
    shapesDataVal.forEach(data => {
      // 形状不重号
      const findSameShape = pastePage.sm.getShapeById(data.id);
      if (findSameShape === null) {
        return;
      }
      // 更新id
      let oldId = data.id;
      data.id = pastePage.graph.uuid();
      data.oldId = oldId;
      shapesDataVal.forEach(effectedShape => {
        if (effectedShape == null) {
          return;
        }
        if (effectedShape.fromShape === oldId) {
          effectedShape.fromShape = data.id;
        }
        if (effectedShape.toShape === oldId) {
          effectedShape.toShape = data.id;
        }
        if (effectedShape.container === oldId) {
          effectedShape.container = data.id;
        }
      });
    });

    let newShape = {};
    const newShapes = [];
    const topShapes = [];

    // 开始paste
    shapesDataVal.forEach(data => {
      // 如果已经paste，就不再paste
      if (pastePage.sm.getShapeById(data.id) != null) {
        return;
      }

      // 开始paste shape
      newShape = pastePage.createNew(data.type, data.x, data.y, data.id);
      newShape.oldId = data.oldId;
      data.x = calcX(data);
      data.y = calcY(data);
      newShape.deSerialize(data);

      // 找到当前位置的contaienr
      if (newShape.container === pastePage.id) {
        pastePage.moveToContainer(newShape);
        topShapes.push(newShape);
      }
      newShapes.push({shape: newShape});
    });

    // 再invalidate一次，将children可能的影响施加
    topShapes.forEach(s => {
      s.reset();
    });
    addCommand(pastePage, newShapes);
    return topShapes;
  };

  pageVal.paste = (shapeData, calcX, calcY) => {
    return pasteShape(shapeData, pageVal, calcX, calcY);
  };

  /**
   * 接收键盘的粘贴事件.
   *
   * @param event 事件对象.
   * @returns {*[]} 粘贴后图形id列表.
   */
  pageVal.onPaste = (event) => {
    return pageVal.copyPasteHelper.paste(event, pageVal);
  };
};

const setEventHandler = pageVal => {
  /**
   * 用于聚合同一类事件来进行批处理
   * 比如把focused事件聚合成一个发送
   *
   * @param type 事件类型
   * @param handle 事件处理动作，对事件进行聚合或者修改
   * @returns {{}}
   */
  let batchEventHandler = (type, handle) => {
    let handler = {};
    let events = [];
    handler.type = type;
    handler.add = event => {
      events.push(event);
    };
    handler.handle = () => {
      if (events.length === 0) {
        return null;
      }
      const resultEvents = handle(events);
      events = [];
      return resultEvents;
    };
    return handler;
  };

  /**
   * page的事件管理器，用于管理page的事件
   *
   * @type {{}}
   */
  let eventManager = function () {
    const DEFAULT_HANDLER = '--';
    let manager = {};
    let handlers = new Map();
    let eventListeners = {};

    /**
     * 添加一个聚合事件处理器
     *
     * @param handler 事件处理器
     */
    manager.addHandler = (handler) => {
      handlers.set(handler.type, handler);
    };

    /**
     * 添加一个待处理的事件
     *
     * @param event 事件
     */
    manager.accept = event => {
      if (pageVal.graph.eventAcceptPreHandler && !pageVal.graph.eventAcceptPreHandler(event)) {
        return;
      }
      // 交由聚合处理器，找不到则给到默认处理器
      let handler = handlers.get(event.type);
      if (!handler) {
        handler = handlers.get(DEFAULT_HANDLER);
      }
      handler.add(event);
    };

    /**
     * 将所有待处理的事件统一触发
     */
    manager.fire = () => {
      for (let handler of handlers.values()) {
        const resultEvents = handler.handle();
        if (resultEvents && resultEvents.length > 0) {
          resultEvents.forEach(event => {
            if (pageVal.graph.eventFirePreHandler && !pageVal.graph.eventFirePreHandler(event)) {
              return;
            }
            manager.fireGraphEvent(event);
            manager.firePageEvent(event);
          });
        }
      }
    };

    /**
     * 触发page事件
     *
     * @param event 事件
     * @returns {Promise<void>}
     */
    manager.firePageEvent = async (event) => {
      const listeners = eventListeners[event.type];
      if (listeners) {
        for (let i = 0; i < listeners.length; i++) {
          const listener = listeners[i];
          await listener(event.value);
        }
      }
    };

    /**
     * 触发graph事件
     *
     * @param event 事件
     */
    manager.fireGraphEvent = (event) => {
      pageVal.graph.fireEvent(event);
    };

    /**
     * 添加页面事件监听器.
     *
     * @param type 事件类型.
     * @param listener 事件监听器.
     */
    manager.addEventListener = (type, listener) => {
      !eventListeners[type] && (eventListeners[type] = []);
      eventListeners[type].push(listener);
    };

    /**
     * 移除事件监听器.
     *
     * @param type 事件类型.
     * @param listener 事件监听器.
     */
    manager.removeEventListener = (type, listener) => {
      const listeners = eventListeners[type];
      if (!listeners || listeners.length === 0) {
        return;
      }
      const index = listeners.findIndex(h => h === listener);
      listeners.splice(index, 1);
    };

    // 添加一个默认处理，不对事件做任何聚合处理，直接返回
    manager.addHandler(batchEventHandler(DEFAULT_HANDLER, events => events));
    return manager;
  }();

  /**
   * 添加一个shapes focused事件聚合处理器，在发送区间内，追加一个FOCUSED_SHAPES_CHANGE事件
   */
  eventManager.addHandler(batchEventHandler(EVENT_TYPE.FOCUSED_SHAPE_CHANGE, (events) => {
    events.push({type: EVENT_TYPE.FOCUSED_SHAPES_CHANGE, value: pageVal.getFocusedShapes()});
    return events;
  }));

  /**
   * 添加一个history事件聚合处理器，在发送区间内，无论收到多少个事件，只触发一次
   */
  eventManager.addHandler(batchEventHandler(EVENT_TYPE.PAGE_HISTORY, (events) => {
    return [{type: EVENT_TYPE.PAGE_HISTORY, value: events[events.length - 1].value}];
  }));

  /**
   * 事件生产方发出事件
   * @param event
   * @returns {Promise<void>}
   */
  pageVal.triggerEvent = async (event) => {
    eventManager.accept(event);
  };

  /**
   * 添加事件监听
   *
   * @param type 事件类型
   * @param listener 监听器
   */
  pageVal.addEventListener = (type, listener) => {
    eventManager.addEventListener(type, listener);
  };

  /**
   * 移除事件监听
   *
   * @param type 事件类型
   * @param listener 监听器
   */
  pageVal.removeEventListener = (type, listener) => {
    eventManager.removeEventListener(type, listener);
  };

  /**
   * 触发事件向外广播
   *
   * @returns {Promise<void>}
   */
  pageVal.fireEvent = async () => {
    eventManager.fire();
  };
};

const setMouseActions = (pageVal) => {
  /**
   * 当前鼠标x
   */
  pageVal.mousex = 0;
  /**
   * 当前鼠标y
   */
  pageVal.mousey = 0;
  /**
   * 当前鼠标点击x
   */
  pageVal.mousedownx = 0;
  /**
   * 当前鼠标点击y
   */
  pageVal.mousedowny = 0;
  /**
   * 当前鼠标样式
   */
  pageVal.cursor = 'default';
  /**
   * 当前鼠标命中的shape，默认是page自己，page就是自己的背景
   * 不管鼠标是否点击
   */
  pageVal.mouseInShape = pageVal;
  /**
   * 鼠标点击命中的shape
   */
  pageVal.mousedownShape = null;
  /**
   * 多点触控下，点击命中的shapes
   */
  pageVal.mousedownShapes = [];
  /**
   * 该页面是否鼠标敏感
   */
  pageVal.mouseSensitive = true;

  /**
   * 如果鼠标按下了，按了哪个button
   */
  pageVal.mouseButton = '';

  pageVal.freeLineType = 'freeLine';

  pageVal.isMouseDown = () => pageVal.mousedownShape !== null;

  pageVal.getShapeClickAble = () => {
    return pageVal.shapeClickAble;
  };

  /**
   * 点击事件
   */
  pageVal.mouseClick = position => {
    let found = pageVal.find(position.x, position.y, s => true);
    pageVal.onClick(position);
    if (!pageVal.getShapeClickAble()) {
      return;
    }
    const region = found.getHitRegion(position.x, position.y);
    region.click(position.x, position.y); // 如果没有hitregion，将会触发shape.click
    pageVal.triggerEvent({type: EVENT_TYPE.REGION_CLICK, value: {shape: found, region: region}});
  };

  /**
   * 双击主要是编辑
   */
  pageVal.dbClick = position => {
    pageVal.onDbClick(position);
    if (!pageVal.getShapeClickAble()) {
      return;
    }
    let found = pageVal.switchMouseInShape(position.x, position.y, s => s.getSelectable());
    if (found === pageVal) {
      if (pageVal.editable) {
        found.beginEdit(position.x, position.y);
      }
    } else {
      found.rotatePosition(position);
      found.getHitRegion(position.x, position.y).dbClick(position.x1, position.y1); // 如果没有hitregion，将会触发shape.click
    }
  };

  pageVal.mouseRightClick = position => {
    pageVal.onRightClick(position);
    if (pageVal.mode === PAGE_MODE.VIEW) {
      return undefined;
    }
    // 716演讲view模式暂时封掉快捷菜单，需要后续按item控制 辉子
    if (pageVal.showContextMenu()) {
      popupMenu(position.x, position.y, pageVal.switchMouseInShape(position.x, position.y, s => true));
    }
    return false;
  };

  /**
   * 响应鼠标down事件
   */
  pageVal.mouseDown = position => {
    // 右键不响应
    if (position.e.button === 2) {
      return;
    }
    pageVal.mousedownShape = pageVal.switchMouseInShape(position.x, position.y, s => s.getSelectable());
    if ((!pageVal.mouseSensitive && pageVal.readOnly()) || position.e.button > 0) {
      return;
    }
    if (pageVal.mouseInShape.type.indexOf('menu') >= 0) {
      return;
    }

    // 是否启动手写
    if (!pageVal.handAction(pageVal.mousedownShape)) {
      if (pageVal.wantedShape.isEmpty()) {
        pageVal.mousedownShape.rotatePosition(position);
        pageVal.mousedownShape.onMouseDown(position);
        if (pageVal.guideLineInstance) {
          pageVal.guideLineInstance.init();
        }
      } else {
        pageVal.onMouseDown(position);
      }
    } else {
      pageVal.getFocusedShapes().forEach(s => s.unSelect());
      if (!pageVal.inHandDrawing) {
        return;
      }
      pageVal.mousedownShape = pageVal.createNew(pageVal.freeLineType, position.x, position.y);
    }
  };

  /**
   * mouseup事件
   */
  pageVal.mouseUp = async position => {
    // 点击画布时，销毁画布上的的上下文菜单
    const shapeVal = pageVal.find(position.x, position.y);
    const mouseInShape = pageVal.mouseInShape;
    const inPageOrContext = mouseInShape.isTypeof('page') || mouseInShape.isTypeof('contextMenu');
    if (inPageOrContext && shapeVal.isTypeof('contextMenu')) {
      if (position.e.target !== shapeVal.getContainer().drawer.parent) {
        return false;
      }
    }

    // 右键不响应
    if (position.e.button === 2) {
      return undefined;
    }
    if (!pageVal.mouseSensitive) {
      return undefined;
    }
    if (pageVal.guideLineInstance) {
      pageVal.guideLineInstance.clear();
    }

    let mousedownShape = pageVal.mousedownShape;
    pageVal.mousedownShape = null;
    pageVal.invalidateInteraction();
    if (mousedownShape === null || mousedownShape === undefined) {
      return undefined;
    }
    mousedownShape.rotatePosition(position);
    await mousedownShape.onMouseUp(position);

    /*
     * 在对图形进行了拖动之后，需要对选中的其他图形重新渲染，主要是进行indexCoordinate()，否则，其他图形在拖动之后就无法被选中了.
     * 可查看shape.indexCoordinate()方法，因为性能，在page.isMouseDown()时，不进行索引重建.
     */
    const focusedShapes = pageVal.getFocusedShapes();
    focusedShapes.filter(focusedShape => focusedShape !== mousedownShape).forEach(s => s.invalidateAlone());
    return undefined;
  };

  pageVal.mouseOver = position => {
  };

  pageVal.mouseIn = position => {
    // 允许画布cut，copy，past操作
    pageVal.keyActions.attachCopyPaste();
    if (isMouseDownWhenMouseLeave) {
      document.body.style.userSelect = null;
      document.removeEventListener('mouseup', mouseUpWhenMouseLeave);
      isMouseDownWhenMouseLeave = false;
      if (pageVal.mousedownShape) {
        pageVal.mousedownShape.onReturnDrag(position.x, pageVal.mousedownShape.mouseOffsetX, position.y, pageVal.mousedownShape.mouseOffsetY);
      }
    }
  };

  pageVal.mouseOut = () => {
    if (!pageVal.mouseSensitive) {

    }
  };

  let isMouseDownWhenMouseLeave = false;

  /**
   * 当鼠标离开page dom时触发.
   */
  pageVal.mouseLeave = async () => {
    if (pageVal.isMouseDown()) {
      document.body.style.userSelect = 'none';
      isMouseDownWhenMouseLeave = true;
      document.addEventListener('mouseup', mouseUpWhenMouseLeave);
    }
  };

  const mouseUpWhenMouseLeave = async (e) => {
    await pageVal.mouseUp(pageVal.calculatePosition(e));
    pageVal.mousedownShape = null;
    document.body.style.userSelect = null;
  };

  pageVal.mouseHold = (position, shape) => {
    let shapeVal = shape;
    if (!shapeVal) {
      shapeVal = pageVal.mousedownShape;
    }
    if (shapeVal) {
      shapeVal.rotatePosition(position);
      shapeVal.onMouseHold(position);
    }
  };

  /**
   * 拖拽事件
   */
  pageVal.mouseDrag = (position) => {
    if (!pageVal.mouseSensitive) {
      return;
    }
    // 先销毁画布上的的上下文菜单
    if (pageVal.contextToolbar) {
      pageVal.contextToolbar.destroy();
    }
    pageVal.mousedownShape.rotatePosition(position);
    pageVal.mousedownShape.onMouseDrag(position);
    if (pageVal.guideLineInstance) {
      pageVal.guideLineInstance.clear();
      pageVal.guideLineInstance.showGuideLines(position);
    }
  };

  /**
   * 响应鼠标move事件
   */
  pageVal.mouseMove = position => {
    pageVal.switchMouseInShape(position.x, position.y, s => {
      if (pageVal.handAction(s)) {
        return s.isTypeof('freeLine');
      } else {
        return true;
      }
    });
    if (!pageVal.mouseSensitive) {
      return;
    }
    if (pageVal.wantedShape.isEmpty()) {
      pageVal.mouseInShape.rotatePosition(position);
      pageVal.mouseInShape.onMouseMove(position);
    } else {
      pageVal.onMouseMove(position);
    }
  };

  /**
   * 某个region.click事件
   */
  pageVal.regionClick = (region) => {
  };

  pageVal.regionDbClick = (region) => {
  };

  pageVal.onClick = position => {
    if (pageVal.cancelClick) {
      delete pageVal.cancelClick;
      return false;
    }
    const x = position.x + pageVal.x;
    const y = position.y + pageVal.y;
    if (x < 32 && y < 32) {
      pageVal.sm.getShapes(s => s.isType('shapeComment')).forEach(s => s.remove());
      if (pageVal.comments) {
        pageVal.comments.forEach(c => pageVal.showComment(pageVal, c, 'bullet'));
      }
    }
    return true;
  };

  pageVal.onDbClick = position => {
  };

  pageVal.onRightClick = position => {
  };

  pageVal.onLongClick = positon => {
  };

  /**
   * mousedown事件
   */
  pageVal.onMouseDown = position => {
    if (!pageVal.wantedShape.isEmpty()) {
      pageVal.getFocusedShapes().forEach(s => s.unSelect());
      pluginMeta.import(pageVal.wantedShape.getType(), pageVal.graph).then(() => {
        pageVal.mousedownShape = pageVal.createNew(pageVal.wantedShape.getType(), position.x, position.y, undefined, pageVal.wantedShape.getProperties());
        if (!pageVal.mousedownShape.disableNewResize) {
          pageVal.mousedownShape.beginDrag(position.x, position.y);
          pageVal.mousedownShape.select();
          pageVal.mousedownShape.resize(1, 1);
        }
        pageVal.mousedownShape.invalidateAlone();
        pageVal.wantedShape.clear();
      });
    } else {
      pageVal.getFocusedShapes().forEach(s => s.unSelect());
    }
  };

  /**
   * 设置鼠标样式
   */
  pageVal.setCursor = () => {
    if (pageVal.operationMode === PAGE_OPERATION_MODE.DRAG) {
      pageVal.cursor = pageVal.isMouseDown() ? CURSORS.GRABBING : CURSORS.GRAB;
      return;
    }
    pageVal.cursor = CURSORS.DEFAULT;
    (!pageVal.wantedShape.isEmpty()) && (pageVal.cursor = CURSORS.CROSSHAIR);
    (pageVal.inHandDrawing) && (pageVal.cursor = CURSORS.PEN);
  };

  pageVal.onMouseUp = position => {
    pageVal.setCursor();
    if (pageVal.operationMode === PAGE_OPERATION_MODE.DRAG) {
      return;
    }
    let rect = {};
    rect.x = (pageVal.mousedownx < pageVal.mousex ? pageVal.mousedownx : pageVal.mousex);
    rect.y = (pageVal.mousedowny < pageVal.mousey ? pageVal.mousedowny : pageVal.mousey);
    rect.width = Math.abs(pageVal.mousedownx - pageVal.mousex);
    rect.height = Math.abs(pageVal.mousedowny - pageVal.mousey);
    const focusedShapes = pageVal.sm.getShapes(shape => shape.inSelection(rect));
    const topShapes = [];
    focusedShapes.forEach(s => {
      if (!focusedShapes.some(s1 => s.container === s1.id)) {
        topShapes.push(s);
      }
    });
    topShapes.forEach(shape => shape.select());
  };

  /**
   * 鼠标移动
   */
  pageVal.onMouseMove = position => {
    pageVal.setCursor();
  };

  pageVal.onMouseDrag = (position) => {
    pageVal.setCursor();
    if (pageVal.operationMode === PAGE_OPERATION_MODE.DRAG) {
      pageVal.moveTo(pageVal.x + position.deltaX, pageVal.y + position.deltaY);
      return;
    }
    pageVal.invalidateInteraction(position);
  };

  /**
   * 鼠标点击时，得到的最符合条件的shape，没有condition，只要在坐标内，就可以得到
   */
  pageVal.switchMouseInShape = (x, y, condition) => {
    let found;
    if (pageVal.operationMode === PAGE_OPERATION_MODE.DRAG) {
      found = pageVal;
    } else {
      found = pageVal.find(x, y, condition ? condition : s => true);
      const parent = found.getContainer();
      const isNotFocused = !parent.isFocused && !found.isFocused;
      if (parent.containerFocusFirst && isNotFocused && !parent.hasChildFocused()) {
        found = parent;
      }
    }
    if (pageVal.mouseInShape !== found) {
      const pre = pageVal.mouseInShape;
      pageVal.mouseInShape = found;
      pre.onMouseOut(pre);
      pageVal.mouseInShape.onMouseIn(pageVal.mouseInShape, pre);
    }
    return found;
  };

  // 确认什么情况下需要监听mouseX的变化
  pageVal.addDetection(['mousex'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    pageVal.invalidateInteraction();
  });

  // 只监听mouseX的变化不够，cursor变化时也需要监听，否则有时鼠标从connector经过，也不会变成可拖动的样式.
  pageVal.addDetection(['cursor'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    pageVal.invalidateInteraction();
  });

  pageVal.addDetection(['wantedShape', 'ongoingShape', 'inHandDrawing', 'eraser'], (property, value, preValue) => {
    if (property !== 'wantedShape') {
      pageVal.wantedShape.clear();
    }
    if (property !== 'ongoingShape') {
      pageVal.ongoingShape = null;
    }
    if (property !== 'inHandDrawing') {
      pageVal.inHandDrawing = false;
    }
    if (pageVal.inputModeChanged) {
      pageVal.inputModeChanged(property, value);
    }
    pageVal.setCursor();
  });

  pageVal.addDetection(['operationMode'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    pageVal.setCursor();
    pageVal.invalidateInteraction();
  });
};

const setKeyActions = (pageVal) => {
  /**
   * 序列化被选中的shape，序列化后才好复制粘贴，反序列化成新的shape
   * 辉子
   */
  let serializeShapes = (shapes) => {
    let all = [];
    let addMyFamily = me => {
      if (all.find(s => s.id === me.id)) {
        return;
      }

      all.push(me);
      if (me.isTypeof('container')) {
        me.getShapes().forEach(s => addMyFamily(s));
      }
    };
    for (let i = 0; i < shapes.length; i++) {
      addMyFamily(shapes[i]);
    }
    const session = uuid();
    return all.map(shape => {
      const data = shape.serialize();
      data && (data.pasteSession = session);
      return data;
    }).filter(s => s != null);
  };

  const handlePageKeyPressed = (event, focused) => {
    if (!pageVal.keyPressAble) {
      return true;
    }

    let result = true;
    focused.forEach((s) => {
      if (!s.keyPressed) {
        return;
      }
      try {
        s.preX = s.x;
        s.preY = s.y;
        result = result && s.keyPressed(event);
      } catch (e) {
        // 没关系，继续，不影响其他错误信息的处理.
      }
    });
    if (result === false) {
      return false;
    }

    return pageVal.keyPressed(event);
  };

  pageVal.onkeyup = (e) => {
    pageVal.ctrlKeyPressed = false;
    pageVal.shiftKeyPressed = false;
    pageVal.invalidateInteraction();
    pageVal.isKeyDown = false;
    if (e.code === 'Space' && pageVal.moveAble && pageVal.canvasMoveAble) {
      pageVal.operationMode = PAGE_OPERATION_MODE.SELECTION;
      return false;
    }
    const focused = pageVal.getFocusedShapes();
    const isDirectionKey = e.key.indexOf('Left') >= 0 ||
      e.key.indexOf('Right') >= 0 ||
      e.key.indexOf('Up') >= 0 ||
      e.key.indexOf('Down') >= 0;
    if (focused.length > 0 && isDirectionKey) {
      const first = focused[0];
      if (first.preX === undefined || first.preY === undefined) {
        return undefined;
      }
      if (first.preX === first.x && first.preY === first.y) {
        return undefined;
      }
      positionCommand(pageVal, focused.map(s => {
        const shapes = {shape: s, x: {preValue: s.preX, value: s.x}, y: {preValue: s.preY, value: s.y}};
        delete s.preX;
        delete s.preY;
        return shapes;
      }));
    }
    return undefined;
  };

  pageVal.onkeydown = (e) => {
    if (e.key === 'Tab') {
      return false;
    }
    pageVal.isKeyDown = true;
    pageVal.graph.getHistory().clearBatchNo();
    if (document.activeElement !== document.body) {
      return true;
    }
    if (e.code === 'Space' && pageVal.moveAble && pageVal.canvasMoveAble) {
      pageVal.operationMode = PAGE_OPERATION_MODE.DRAG;
      return false;
    }

    let focused = pageVal.getFocusedShapes();
    pageVal.ctrlKeyPressed = e.ctrlKey || e.metaKey;
    pageVal.shiftKeyPressed = e.shiftKey;
    const result = handlePageKeyPressed(e, focused);
    if (!result) {
      return false;
    }

    // ctrl+S
    if (pageVal.ctrlKeyPressed && (e.code === 'KeyS')) {
      if (focused.length === 0) {
        return false;
      }
    }

    if (pageVal.readOnly()) {
      return undefined;
    }

    // Escape
    if (e.code === 'Escape') {
      pageVal.wantedShape.clear();
      pageVal.ongoingShape = null;
      pageVal.inHandDrawing = false;
      pageVal.sm.getShapes().forEach(s => delete s.eraser);
      if (pageVal.escaped) {
        pageVal.escaped();
      }
    }

    if (e.key === 'F5') {
      try {
        let viewer = document.getElementById('present');
        if (viewer === null) {
          viewer = document.createElement('div');
          viewer.id = 'present';
          viewer.style.background = 'red';
          pageVal.div.parentNode.appendChild(viewer);
        } else {
          viewer.innerHTML = '';
        }
      } catch (error) {
        // 没关系，继续，不影响其他错误信息的处理.
      }
      return false;
    }

    if (pageVal.ifKeyPressed(e) === false) {
      return false;
    }

    // zoom out
    if (e.shiftKey && (e.code === 'Equal')) {
      pageVal.zoom(0.1);
      return false;
    }
    // zoom in
    if (e.shiftKey && (e.code === 'Minus')) {
      pageVal.zoom(-0.1);
      return false;
    }
    // redo
    if (pageVal.ctrlKeyPressed && e.shiftKey && (e.code === 'KeyZ')) {
      pageVal.graph.getHistory().redo(pageVal);
      return false;
    }
    // undo
    if (pageVal.ctrlKeyPressed && (e.code === 'KeyZ')) {
      pageVal.graph.getHistory().undo(pageVal);
      return false;
    }

    // select all: ctrl+a
    if (pageVal.ctrlKeyPressed && (e.code === 'KeyA')) {
      pageVal.selectAll();
      return false;
    }
    // group: ctrl+g
    if (pageVal.ctrlKeyPressed && (e.code === 'KeyG')) {
      if (focused.length > 1) {
        pageVal.group(focused);
        return false;
      }
    }

    // delete
    if (e.code.indexOf('Backspace') >= 0 || e.code.indexOf('Delete') === 0) {
      const focusedShapes = pageVal.getFocusedShapes();
      if (focusedShapes.length > 0) {
        pageVal.onDelete();
      }
      return false;
    }

    // ctrl+D
    if (pageVal.ctrlKeyPressed && (e.code === 'KeyD' || e.keyCode === 68)) {
      const allowed = focused.filter(s => {
        if (s.duplicate) {
          s.duplicate();
        }
        return !s.duplicate;
      });
      if (allowed.length > 0) {
        let shapes = JSON.stringify(serializeShapes(allowed));
        pageVal.copyPasteHelper.pasteShapes(shapes, '', pageVal);
      }

      return false;
    }

    if (pageVal.focusMenuItem && pageVal.focusMenuItem.container !== '') {
      switch (e.code) {
        case 'ArrowLeft':
          pageVal.focusMenuItem.toParent();
          return false;
        case 'ArrowRight':
          pageVal.focusMenuItem.toChild();
          return false;
        case 'ArrowUp':
          pageVal.focusMenuItem.toPrevious();
          return false;
        case 'ArrowDown':
          pageVal.focusMenuItem.toNext();
          return false;
        case 'Enter':
          pageVal.focusMenuItem.click();
          return false;
        default:
          break;
      }
    }
    return undefined;
  };

  pageVal.ifKeyPressed = (e) => {
    if (!pageVal.keyPressed(e)) {
      return false;
    }
    return undefined;
  };
};

export {page, wantedShape};
