/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Atom} from './atom.js';
import {
  convertPositionWithParents,
  eventDebounce,
  getDistance,
  isNumeric,
  isPointInRect,
  isRectInRect,
  uuid,
} from '../common/util.js';
import {
  CURSORS,
  DEFAULT_FOLLOW_BAR_OFFSET,
  DOCK_MODE,
  EDITOR_NAME,
  EVENT_TYPE,
  MIN_WIDTH,
  PARENT_DOCK_MODE,
  SELECTION_STRATEGY,
} from '../common/const.js';
import {configurationFactory} from './configuration/configurationFactory.js';
import {layoutCommand} from './commands.js';
import {lockRegion} from './hitRegion.js';
import {imageSaver} from './thumb.js';
import {inPolygon} from '../common/graphics.js';
import {copyIcon, deleteIcon} from './svg/icons.js';

/**
 * @class
 * 形状基类
 * 所有形状的最终基类：最最重要的类
 * 背景，线条，颜色，文字。。。。。出自该形状
 * 该形状为抽象基类，绘画实现为空
 * 绘画实现是可替换方案：可以是canvas绘制，也可以是svg绘制
 * 辉子 2020-01-15
 */
const shape = (id, x, y, width, height, parent, drawer) => {
  let detections = [];
  let self = new Atom(detections);

  // ----------need to be serialized---------------------
  self.type = 'shape';

  /** 每个图形都有一个唯一的id，不指定时自动生成uuid */
  self.id = id ? id : parent.graph.uuid();

  if (parent !== undefined && parent !== null) {
    if (parent.newPageMode) {
      self.page = self;
      self.mode = parent.newPageMode;
    } else {
      self.graph = parent.graph;
      self.page = parent.page;
      self.pageId = parent.page.id;
      self.container = parent.id;
    }
  }

  self.typeChain = {
    parent: null, type: self.type,
  };

  self.isType = type => {
    return self.typeChain.type === type;
  };

  self.isTypeof = type => {
    let chain = self.typeChain;
    while (chain !== null) {
      if (chain.type === type) {
        return true;
      }
      chain = chain.parent;
    }
    return false;
  };

  self.getPadLeft = () => {
    const padLeft = self.padLeft;
    return padLeft !== null && padLeft !== undefined ? padLeft : self.pad;
  };
  self.getPadRight = () => {
    const padRight = self.padRight;
    return padRight ? padRight : self.pad;
  };
  self.getPadTop = () => {
    const padTop = self.padTop;
    return padTop ? padTop : self.pad;
  };
  self.getPadBottom = () => {
    const padBottom = self.padBottom;
    return padBottom ? padBottom : self.pad;
  };

  self.text = '';
  // ----------------static--------------
  self.namespace = 'elsa';
  self.serializable = true;
  self.defaultWidth = 100;
  self.defaultHeight = 100;
  self.shareAble = false; // 是否可以被不同的page引用
  /**
   * @maliya 2023.6.9 临时方案，为鸿蒙演示
   * 文档中批注功能，要求：批注的笔记可以跟随文字自适应变化，先只实现一根直线，不考虑圆
   */
  self.needLevitation = true;
  if (!parent.newPageMode) {
    self.drawer = drawer(self, self.page.div);
  }
  self.sharedParent = () => {
    if (self.container === '') {
      return self.savedSharedParent ? self.savedSharedParent : {id: ''};
    }
    let parentVal = self;
    while (!parentVal.isTypeof('page')) {
      parentVal = parentVal.getContainer();
      if (parentVal.shared) {
        return parentVal;
      }
    }
    return {id: ''};
  };
  /**
   * 绝对横坐标
   * 即使该shape处于某个container里，依然按照全局绝对坐标设置，也就是container移动时，该shape的坐标也发生移动，并不是相对container的相对不变坐标
   * 辉子
   */
  self.x = x;
  self.y = y;
  self.width = width;
  self.height = height;
  self.listeners = [];
  self.eventDenounce = eventDebounce();

  // ----------dynamic------------------
  self.selectedX = 0;
  self.selectedY = 0; // 记录shape被选中后的原始位置，方便shape拖动时计算
  self.isFocused = false;
  self.linking = false; // 是否正在被line连接
  self.linkingConnector = null;
  self.cursor = CURSORS.MOVE;
  self.allowTraced = true; // 允许使用ctrl+z
  self.isAutoSize = true;
  self.allowClickRun = false;
  self.allowMultiLineEdit = false;
  self.mousedownConnector = null;
  self.isEditing = () => {
    if (!self.drawer.text) {
      return false;
    }
    return self.isEdit;
  };

  self.setProperty = (property, value) => {
    self[property] = value;
    if (property === 'local') {
      self.setLocal(value);
    }
  };
  self.setLocal = localAction => {
  };

  // ---------------methods-----------------
  self.containerAllowed = () => true;
  self.getVisibility = () => {
    let v = self.inScreen() && self.visible && self.globalAlpha !== 0;
    if (self.getContainer() === self.page) {
      return v;
    } else {
      return self.getContainer().getVisibility() && v;
    }
  };
  self.getSelectable = () => {
    return self.selectable;
  };
  self.getIndex = () => {
    return self.page.sm.getShapeIndex(self);
  };
  self.cachedContainer = {id: undefined, shape: undefined};

  /**
   * 撤销删除图形修改为 新建图形（服务端需处理：若图形已存在，则覆盖）
   * 场景：1、删除图形 -> 撤销删除；2、删除图形 -> 删除图形所在页面 -> 撤销删除页面 -> 撤销删除图形;
   */
  self.undoRemove = (page, index) => {
    self.page = page;

    // container的detection中，会设置preContainer.
    self.preContainer && (self.container = self.preContainer);
    page.sm.updateShapes(writer => writer.moveShapeTo(self, index), true, false);
    self.invalidateAlone();
  };

  self.getContainer = () => {
    if (self.cachedContainer.id === self.container) {
      return self.cachedContainer.shape;
    }
    let p = self.page.id === self.container ?
      self.page :
      self.page.sm.findShapeBy(s =>
        s.id === self.container && s.childAllowed && s.childAllowed(self));
    if (p === undefined) {
      p = self.page.find((self.x + (self.width / 2), self.y + (self.height / 2), s => s.childAllowed));
    }
    self.cachedContainer.id = p.id;
    self.cachedContainer.shape = p;
    return p;
  };

  self.center = () => {
    return {x: (self.x + (self.width / 2)), y: (self.y + (self.height / 2))};
  };

  self.getFontString = () => {
    return `${self.fontStyle} ${self.fontWeight} ${self.fontSize}px ${self.fontFace}`;
  };

  self.getBorderColor = () => {
    let color = self.borderColor;
    if (self.page.mouseInShape === self) {
      color = self.mouseInBorderColor;
    }
    if (self.isFocused) {
      color = self.focusBorderColor;
    }
    return color;
  };

  /**
   * 回去shadow值.
   *
   * @return {*}
   */
  self.getShadow = () => {
    return self.isFocused ? self.focusShadow : self.shadow;
  };

  self.getFontColor = () => {
    let color = self.fontColor;
    if (self.page.mouseInShape === self) {
      color = self.mouseInFontColor;
    }
    if (self.isFocused) {
      color = self.focusFontColor;
    }
    return color;
  };

  self.getBackColor = () => {
    let color = self.backColor;
    if (self.isFocused) {
      color = self.focusBackColor;
    }
    if (self.isLocked()) {
      color = 'rgba(255,215,0,0.2)';
    }
    return color;
  };

  self.addGraphEventListener = (type, handler) => {
    self.listeners.push({type: type, handler: handler});
    self.page.graph.addEventListener(type, handler);
  };

  self.addPageEventListener = (type, handler) => {
    self.listeners.push({type: type, handler: handler});
    self.page.addEventListener(type, handler);
  };

  self.removeEventListeners = () => {
    self.listeners.forEach(listener => {
      self.page.graph.removeEventListener(listener.type, listener.handler);
      self.page.removeEventListener(listener.type, listener.handler);
    });
    // 删除当前shape持有的监听器
    self.listeners = [];
  };

  self.remove = source => {
    if (!self.page.disableReact && !self.beforeRemove()) {
      return [];
    }

    self.removeEventListeners();
    const removed = [];
    self.page.sm.getShapes((s) => s.fromShape === self.id).forEach((s) => s.fromShape = '');
    self.page.sm.getShapes((s) => s.toShape === self.id).forEach((s) => s.toShape = '');
    self.page.sm.updateShapes(writer => writer.deleteShape(self));
    if (self.page.animations && self.page.animations.clean) {
      self.page.animations.clean();
    }
    self.savedSharedParent = self.sharedParent();
    delete self.deleteFromShare;
    if (source && source !== self && source.shared) {
      self.deleteFromShare = true;
    }

    // 这里不确定在container已为空字符串时是否需要走下面的流程，暂时先不走，看有无问题.
    if (self.container !== '') {
      self.container = '';
      if (!self.page.disableReact) {
        self.afterRemoved();
        self.getContainer().shapeRemoved(self);
      }
      removed.push(self);
    }

    // 删除时需要触发FOCUSED_SHAPE_CHANGE事件.
    if (self.isFocused) {
      self.page.ignoreReact(() => {
        self.isFocused = false;
      });
      self.page.triggerEvent({type: EVENT_TYPE.FOCUSED_SHAPE_CHANGE, value: [self]});
    }

    /*
     * 这里修改的原因，是为了保证幂等，当container为空字符串时，也需要走一遍流程（container为空字符串不代表图形已被删除
     * 否则，会出现问题，问题场景如下：
     * 1、协同1删除图形
     * 2、协同2收到message
     *     a、在graph的subscriptions[page_shape_data_changed]中将图形对应的_data的container设置为""
     *     b、调用page.onMessage，此时将调用图形的remove()方法，但此时由于_data中的container已经为""，所以并不会
     *        执行删除dom的操作，并且也不会将图形对象从page.shapes数组中删除
     * 3、协同1撤销
     * 4、协同2收到shape_added的message
     *     a、在graph的subscriptions[shape_added]中，将message中的value推入到graph.pages.shapes中
     *     b、调用page.onMessage，由于图形对象未被删除可以获取到，因此不做任何操作.
     * 5、协同2收到"page_shape_data_changed"消息
     *     a、在graph的subscriptions[page_shape_data_changed]中将图形对应的_data的container设置为非空字符串
     *     b、调用page.onMessage，由于图形还在，设置_data中的container为非空字符串
     *     注意： 这里图形对象中的_data和graph.pages.shapes中的_data已不是同一个对象，一个是message中的value
     * 6、协同1重做
     * 7、协同2收到"page_shape_data_changed"消息
     *     a、在graph的subscriptions[page_shape_data_changed]中将图形对应的_data的container设置为""
     *     b、调用page.onMessage，调用图形的remove()方法，由于data对象已不一样，上一步并不会影响对象中的_data，
     *     此时图形的container还是非空字符串，所以可以删除成功
     * 8、协同1撤销
     * 9、协同2收到shape_added的message
     *     a、在graph的subscriptions[shape_added]中，将message中的value推入到graph.pages.shapes中
     *     b、调用page.onMessage，创建图形，并且_data就是message.value.
     *     注意，此时data是同一个对象了
     * 10、协同1重做
     *     a、在graph的subscriptions[page_shape_data_changed]中将图形对应的_data的container设置为""
     *     b、调用page.onMessage，此时将调用图形的remove()方法，但此时由于_data中的container已经为""，所以并不会
     *     执行删除dom的操作，并且也不会将图形对象从page.shapes数组中删除
     * 到这里，以上的步骤，就会随着重做和撤销而不断的循环，导致结果不一致。
     * 因此，会存在调用remove时，container已为空字符串的情况，因此，这里需要进行保护.
     */
    self.invalidate();
    return removed;
  };

  self.getBound = () => {
    const OFFSET = 10;
    const pos = {};
    pos.minx = self.x - self.margin + (self.width < 0 ? self.width : 0) - OFFSET;
    pos.miny = self.y - self.margin + (self.height < 0 ? self.height : 0) - OFFSET;
    pos.maxx = self.x + self.margin + (self.width > 0 ? self.width : 0) + (2 * OFFSET);
    pos.maxy = self.y + self.margin + (self.height > 0 ? self.height : 0) + (2 * OFFSET);

    const clipParent = (child, scrollx, scrolly) => {
      let scrollxVal = scrollx;
      let scrollyVal = scrolly;
      const parentVal = child.getContainer();
      if (parentVal === self.page) {
        return;
      }
      if (parentVal.ifMaskItems) {
        scrollxVal += parentVal.itemScroll.x;
        scrollyVal += parentVal.itemScroll.y;
        const pminx = parentVal.x + Math.max(parentVal.borderWidth, parentVal.itemPad[0]) - scrollxVal;
        const pminy = parentVal.y + Math.max(parentVal.borderWidth, parentVal.itemPad[2]) - scrollyVal;
        if (pminx > pos.minx) {
          pos.minx = pminx;
        }
        if (pminy > pos.miny) {
          pos.miny = pminy;
        }

        const pmaxx = parentVal.x + parentVal.width - Math.max(2 * parentVal.borderWidth, parentVal.itemPad[1]) - scrollxVal;
        const pmaxy = parentVal.y + parentVal.height - Math.max(2 * parentVal.borderWidth, parentVal.itemPad[3]) - scrollyVal;
        if (pmaxx < pos.maxx) {
          pos.maxx = pmaxx;
        }
        if (pmaxy < pos.maxy) {
          pos.maxy = pmaxy;
        }
      }
      clipParent(parentVal, scrollxVal, scrollyVal);
    };
    clipParent(self, 0, 0);
    const result = {x: pos.minx, y: pos.miny, width: pos.maxx - pos.minx, height: pos.maxy - pos.miny};
    result.visible = result.width > 0 && result.height > 0;
    return result;
  };

  self.getFrame = () => {
    return {x: self.x, y: self.y, width: self.width, height: self.height};
  };

  setContains(self);
  setConnector(self);
  setRegion(self);
  setMouseActions(self);
  setKeyAction(self);
  setCoordinateIndex(self);

  self.isMyBlood = item => item === self;

  /**
   * 拖动该形状
   * 触发moveTo
   * 但用于手动拖拽形状
   * 逻辑比moveTo复杂很多，因为拖动时shape.container可能在旋转状态
   * 辉子 2021
   */
  self.dragTo = (position) => {
    // 记录上下文，为创建positionCommand准备
    position.context.command = 'position';
    let dirty = position.context.shapes.find(s => s.shape === self);
    if (!dirty) {
      dirty = {shape: self, x: {}, y: {}, container: {}};
      dirty.x.preValue = self.x;
      dirty.y.preValue = self.y;
      dirty.container.preValue = self.container;
      position.context.shapes.push(dirty);
    }
    dirty.x.value = self.x + position.deltaX;
    dirty.y.value = self.y + position.deltaY;
    dirty.container.value = self.container;
    return dirty;
  };

  /**
   * 移动该形状:无所谓是代码移的还是手动移的
   */
  self.moveTo = (xVal, yVal, after) => {
    if (xVal === undefined || yVal === undefined) {
      return false;
    }
    if (xVal === self.x && yVal === self.y) {
      return false;
    }

    self.x = xVal;
    self.y = yVal;

    // 多次调用moveTo只执行一次.
    self.eventDenounce.debounce(() => {
      after ? after() : self.moved();
    });
    return true;
  };

  /**
   * 判断形状是否在屏幕可见范围
   * 如果不在可见范围，为提高性能，动画不绘制
   * 辉子 2020
   */
  self.inScreen = () => {
    return true;
  };

  /**
   * 移动到屏幕中心
   *
   * scaleX 缩放比例，不传则使用当前比例
   * scaleY 缩放比例，不传则使用当前比例
   */
  self.toScreenCenter = (scaleX, scaleY) => {
    let shapeFrame = self.getShapeFrame();
    let newCenterX = shapeFrame.x1 + ((shapeFrame.x2 - shapeFrame.x1) / 2);
    let newCenterY = shapeFrame.y1 + ((shapeFrame.y2 - shapeFrame.y1) / 2);
    let page = self.page;
    let _scaleX = scaleX;
    let _scaleY = scaleY;
    if (scaleX && scaleY) {
      page.zoomTo(scaleX, scaleY, newCenterX, newCenterY);
    } else {
      _scaleX = page.scaleX;
      _scaleY = page.scaleY;
    }
    page.moveTo((page.width / 2 / _scaleX) - newCenterX, (page.height / 2 / _scaleY) - newCenterY);
  };

  /**
   * 留着为esc键后的动作
   */
  self.restore = () => {
  };

  /**
   * 确定编辑区域
   * 双击编辑框在这个区域显示
   */
  self.getEditRect = () => {
    return {
      x: self.x + self.getPadLeft(),
      y: self.y + self.getPadTop(),
      width: self.width - self.getPadLeft() - self.getPadRight() - (2 * self.borderWidth),
      height: self.height - self.getPadTop() - self.getPadBottom() - (2 * self.borderWidth),
    };
  };

  // ---------------------------------绘制相关----------------------------------------------

  /**
   * 重新绘制自己
   * 辉子 2020
   */
  self.render = () => {
    if (self.drawer) {
      self.drawer.draw();
    }
  };

  /**
   * 在页shape上，表现跟invalidate基本一致
   */
  self.invalidateAlone = () => {
    if (self.page === undefined) {
      return;
    }
    if (self.page.disableInvalidate) {
      return;
    }
    if (self.container === '') {
      delete self.enableAnimation;
      self.drawer.remove();
      return;
    }

    if (!self.getVisibility()) {
      self.drawer.hide();
    } else {
      self.manageConnectors();
      self.drawer.move();
      self.drawer.transform();
      self.render();
    }
  };

  /**
   * 将自己失效
   * 绘制，移动，变形，表现跟refresh一致
   * 继承类会做子形状适配
   * 辉子 2020
   */
  self.invalidate = () => self.invalidateAlone();

  self.delayInvalidate = (action, ignore) => {
    let invalidate = self.invalidate;
    let invalidateAlone = self.invalidateAlone;
    let render = self.render;
    self.invalidate = () => {
    };
    self.invalidateAlone = () => {
    };
    self.render = () => {
    };
    action();
    self.invalidate = invalidate;
    self.invalidateAlone = invalidateAlone;
    self.render = render;
    if (ignore) {
      return;
    }
    self.invalidate();
  };

  /**
   * 复位
   * 辉子 2020
   */
  self.reset = () => {
    // 鼠标放开后位置状态回归原始
    if (self.width === MIN_WIDTH && self.height === MIN_WIDTH) {
      self.resize(self.defaultWidth, self.defaultHeight);
    }
    self.invalidateAlone();
  };

  /**
   * 去除文本的所有html格式
   * 辉子 2021
   */
  self.removeTextFormat = () => {
    let edit = document.getElementById(EDITOR_NAME);
    edit.innerHTML = self.text;
    self.text = edit.innerText;
  };

  /**
   * 是否被选中框选中
   * 辉子 2022
   */
  self.inSelection = rect => {
    return isRectInRect(self, rect);
  };

  /**
   * 选中该shape，选中后shape.isFocused = true;
   * 辉子 2020
   */
  self.select = (xVal, yVal) => {
    if (!self.getSelectable() || !self.getVisibility() || self.isFocused) {
      return;
    }
    self.selectX = xVal;
    self.selectY = yVal;
    self.isFocused = true;
    if (self.selected) {
      self.selected();
    }
  };

  /**
   * 去除该shape的选中状态
   * huiz 2020
   */
  self.unSelect = () => {
    if (!self.getSelectable() || !self.getVisibility() || !self.isFocused) {
      return;
    }
    self.isFocused = false;
    if (self.autoWidth || self.autoHeight) {
      self.indexCoordinate();
    }
    if (self.unselected) {
      self.unselected();
    }
  };

  /**
   * 得到focus相关所有形状
   */
  self.getRelated = () => [self];

  self.freeLineSelect = (points) => {
    let bound = self.getBound();
    let isPointsInBound = points.filter(point => isPointInRect({x: point[0], y: point[1]}, bound)).length > 0;
    if (isPointsInBound) {
      return true;
    }
    return inPolygon(points, self.x, self.y) ||
      inPolygon(points, self.x + self.width, self.y) ||
      inPolygon(points, self.x, self.y + self.height) ||
      inPolygon(points, self.x + self.width, self.y + self.height);
  };

  self.resize = (rWidth, rHeight) => {
    let widthVal = rWidth;
    let heightVal = rHeight;
    isNumeric(self.minWidth) && widthVal < self.minWidth && (widthVal = self.minWidth);
    isNumeric(self.minHeight) && heightVal < self.minHeight && (heightVal = self.minHeight);
    if (self.width === widthVal && self.height === heightVal) {
      return;
    }
    const preWidth = self.width;
    const preHeight = self.height;
    if (self.width !== widthVal) {
      self.width = widthVal;
    }
    if (self.height !== heightVal) {
      self.height = heightVal;
    }
    if (self.resized) {
      self.resized(preWidth, preHeight, widthVal, heightVal);
    }
  };

  /**
   * container的itemscroll
   * 子shape在container里可以有offset移动
   */
  self.getOffset = () => {
    const container = self.getContainer();
    return {
      x: container.getOffset().x + container.itemScroll.x,
      y: container.getOffset().y + container.itemScroll.y,
    };
  };

  /**
   * 返回图形默认的followBar位置和偏移量,这里可以根据图形在画布中的位置,动态调整其location
   *
   * @returns { location: string}
   */
  self.getFollowBarLocation = () => {
    return 'bottom';
  };

  /**
   * 获取followBar的偏移量,这里可以根据图形的宽高和位置,动态调整偏移量
   * @returns {number}
   */
  self.getFollowBarOffset = () => {
    return DEFAULT_FOLLOW_BAR_OFFSET;
  };

  /**
   * 得到画布中图形的默认上下文工具栏菜单
   * 说明：为了支持不同应用不同默认菜单功能，故在对应应用的graph上也能获取对应的图形默认上下文工具栏菜单
   * @maliya
   */
  self.getContextMenuScript = () => {
    return {
      menus: [
        {
          type: 'icon',
          name: 'copy',
          icon: copyIcon,
          text: '复制',
          group: 'base',
          onClick: function (target) {
            const event = new KeyboardEvent('keydown', {
              ctrlKey: true,
              keyCode: 68,
              code: 'KeyD',
            });
            document.dispatchEvent(event);
          },
        },
        {
          type: 'icon',
          name: 'delete',
          icon: deleteIcon,
          text: '删除',
          group: 'base',
          onClick: function (target) {
            // 将删除按钮映射到delete按键
            const event = new KeyboardEvent('keydown', {
              code: 'Delete',
            });
            document.dispatchEvent(event);
          },
        },
      ],
    };
  };

  /**
   * 得到該形狀的快捷菜單
   * 輝子 2022
   */
  self.getMenuScript = () => {
    if (!self.getSelectable()) {
      return undefined;
    }
    const setEraser = (shapeVal, size) => {
      if (shapeVal.isTypeof('freeLine')) {
        shapeVal.eraser = size;
      } else {
        shapeVal.page.sm.getShapes(s => s.isTypeof('freeLine')).forEach(s => s.eraser = size);
      }
    };

    // 手写
    const menus = [];
    menus.push({
      text: `${self.page.inHandDrawing ? '取消' : ''}手写`,
      action: () => {
        self.page.inHandDrawing = !self.page.inHandDrawing;
      }, draw: (context) => {
        context.strokeStyle = 'dimgray';
        context.strokeRect(-2, -2, 4, 6);
        context.strokeStyle = context.fillStyle = 'red';
        context.moveTo(-2, -2);
        context.lineTo(0, -5);
        context.lineTo(2, -2);
        context.stroke();
        context.fill();
      },
    });

    const erasers = {
      text: '擦除', menus: [], width: 30, draw: (context) => {
        context.strokeStyle = 'dimgray';
        context.strokeRect(-3, -2, 6, 5);
        context.strokeStyle = 'red';
        context.strokeRect(-3, -3, 6, 1);
      },
    };
    erasers.menus.push({
      text: '小', action: shapeArg => {
        setEraser(shapeArg, 3);
      }, draw: (context) => {
        context.fillStyle = 'red';
        context.fillRect(-2, -2, 4, 4);
      },
    });
    erasers.menus.push({
      text: '大', action: shapeArg => {
        setEraser(shapeArg, 6);
      }, draw: (context) => {
        context.fillStyle = 'dimgray';
        context.fillRect(-3, -3, 6, 6);
      },
    });
    menus.push(erasers);

    if (self === self.page) {
      return menus;
    }

    // 层级
    const layer = {
      text: '层级', menus: [], draw: (context) => {
        context.fillStyle = 'silver';
        context.fillRect(0, -3, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-1.5, -1.5, 6, 6);
        context.fillStyle = 'red';
        context.fillRect(-1.5, -1.5, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-3, 0, 6, 6);
        context.fillStyle = 'gray';
        context.fillRect(-3, 0, 6, 6);
      },
    };
    layer.menus.push({
      text: '向上一层',
      action: s => {
        s.page.sm.updateShapes(writer => {
          writer.moveUp([s]);
        }, true, true);
      },
      draw: (context) => {
        context.fillStyle = 'silver';
        context.fillRect(0, -3, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-1.5, -1.5, 6, 6);
        context.fillStyle = 'steelblue';
        context.fillRect(-1.5, -1.5, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-3, 0, 6, 6);
        context.fillStyle = 'red';
        context.fillRect(-3, 0, 6, 6);
      },
    });
    layer.menus.push({
      text: '向下一层',
      action: s => {
        s.page.sm.updateShapes(writer => {
          writer.moveDown([s]);
        }, true, true);
      },
      draw: (context) => {
        context.fillStyle = 'red';
        context.fillRect(0, -3, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-1.5, -1.5, 6, 6);
        context.fillStyle = 'steelblue';
        context.fillRect(-1.5, -1.5, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-3, 0, 6, 6);
        context.fillStyle = 'gray';
        context.fillRect(-3, 0, 6, 6);
      },
    });
    layer.menus.push({
      text: '到最顶层', action: s => {
        s.page.sm.updateShapes(writer => {
          writer.moveTop([s]);
        }, true, true);
      }, draw: (context) => {
        context.fillStyle = 'silver';
        context.fillRect(0, -3, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-1.5, -1.5, 6, 6);
        context.fillStyle = 'gray';
        context.fillRect(-1.5, -1.5, 6, 6);
        context.strokeStyle = 'white';
        context.fillStyle = 'red';
        context.strokeRect(-3, 0, 6, 6);
        context.fillRect(-3, 0, 6, 6);
      },
    });
    layer.menus.push({
      text: '到最底层', action: s => {
        s.page.sm.updateShapes(writer => {
          writer.moveBottom([s]);
        }, true, true);
      }, draw: (context) => {
        context.fillStyle = 'red';
        context.fillRect(0, -3, 6, 6);
        context.strokeStyle = 'white';
        context.strokeRect(-1.5, -1.5, 6, 6);
        context.fillStyle = 'silver';
        context.fillRect(-1.5, -1.5, 6, 6);
        context.strokeStyle = 'white';
        context.fillStyle = 'gray';
        context.strokeRect(-3, 0, 6, 6);
        context.fillRect(-3, 0, 6, 6);
      },
    });
    menus.push(layer);
    return menus;
  };

  /**
   * 将该形状保存为png文件
   */
  self.downloadImage = () => imageSaver(self.page, self.drawer.parent).download();

  /**
   * 将该文件转换成图形格式，没有下载保存
   */
  self.toImage = handle => {
    imageSaver(self, self.drawer.parent).toPng(handle);
  };
  self.getSnapshot = () => self.drawer.getSnapshot();

  /**
   * 鼠标拖动图形离开窗口后返回时调用，用于拖动图形到新的位置
   *
   * @param xVal 新位置的鼠标x坐标
   * @param mouseOffsetX x轴上鼠标位置距离图形(0,0)坐标偏移量
   * @param yVal 新位置的鼠标y坐标
   * @param mouseOffsetY y轴上鼠标位置距离图形(0,0)坐标偏移量
   * @param after 有值则不调用self.moved()
   */
  self.onReturnDrag = (xVal, mouseOffsetX, yVal, mouseOffsetY, after) => {
    self.moveTo(xVal - mouseOffsetX, yVal - mouseOffsetY, after);
  };

  // -----------------text operation---------------------------
  self.setSelectedTextForeColor = color => document.execCommand('foreColor', 'false', color);
  self.setSelectedTextFontSize = size => document.execCommand('fontSize', 'false', size);
  self.setSelectedTextBackColor = color => document.execCommand('hiliteColor', 'false', color);
  self.createUnorderList = () => document.execCommand('insertUnorderedList');
  self.createOrderList = () => document.execCommand('insertOrderedList');

  // ------------events---------------------
  self.editing = (edit, e) => self.runCode('editingCode');
  self.edited = editor => {
    self.runCode('editedCode');
  };

  self.onCut = (shapes) => {
  };

  /**
   * 调用select方法才会触发selected
   * 与focused不同，focsued是isFocused==true才会触发是时间
   * selected一定伴随focused，focused不一定有selected
   */
  self.selected = () => {
    self.runCode('selectedCode');
    if (!self.hideText) {
      self.beginEdit();
    }
  };

  /**
   * 调用unselect方法才会触发unselected
   * 与unfocused不同，unfocsued是isFocused==false才会触发是时间
   * unselected一定伴随unfocused，unfocused不一定有unselected
   */
  self.unselected = () => {
    self.runCode('unSelectedCode');
    if (!self.hideText) {
      self.endEdit();
    }
  };

  self.getFocused = () => {
    return self;
  };

  self.beforeRemove = source => {
    // 调用drawer的beforeRemove方法.
    self.drawer.beforeRemove(source);
    let result = self.runCode('beforeRemoveCode');
    return result === undefined ? true : result;
  };

  self.afterRemoved = source => {
    self.runCode('afterRemovedCode');
    // 销毁上下文菜单
    // 先销毁画布上的的上下文菜单
    if (self.page.contextToolbar) {
      self.page.contextToolbar.destroy();
    }
  };

  self.effectLines = () => {
    _effectLines(self);
  };
  self.effectGroup = () => {
    const parentVal = self.getContainer();
    if (parentVal.autoAlign) {
      parentVal.invalidate();
    }
  };

  self.resized = () => {
    if (self.page.disableReact) {
      return;
    }
    self.effectLines();
    self.effectGroup();
    self.invalidateAlone();
    self.runCode('resizedCode');
  };

  self.animate = () => {
    self.runCode('animateCode');
  };

  self.moved = () => {
    self.drawer.move();
    if (self.parentMoving) {
      return;
    }
    if (self.page.disableReact) {
      return;
    }
    self.effectGroup();
    self.runCode('movedCode');
  };

  /**
   * trigger when page.mode changed: configuration or runtime;
   */
  self.modeChanged = mode => {
  };

  /**
   * 当isFocused==true时触发
   */
  self.focused = () => {
    self.runCode('focusedCode');
  };

  /**
   * 当isFocused==false时触发
   */
  self.unfocused = () => {
    self.runCode('unfocusedCode');
  };

  self.containerChanged = (preValue, value) => self.runCode('containerChangedCode');

  self.textChanged = (value, preValue) => self.runCode('textChangedCode');

  self.getRotateAble = () => self.rotateAble && self.getContainer().dockMode === DOCK_MODE.NONE && self.pDock === PARENT_DOCK_MODE.NONE;

  /**
   * 用于界面配置时自动生成配置项
   * 这段代码可以放到配置portal里
   * 辉子 2021
   */
  self.configFactory = configurationFactory();
  self.getConfigurations = () => {
    return Array.from(self.serializedFields).map(f => self.configFactory.get(f, self)).filter(c => !!c);
  };

  setSerialize(self);

  setDetection(self, detections);

  self.ifInConfig = () => {
    return false;
  };

  self.isInConfig = () => {
    return ((self.page !== undefined && self.ifInConfig()) || self.ignorePageMode);
  };
  self.isLocked = () => {
    const editBy = (self.editBy && self.editBy.length > 0) ? self.editBy[0] : undefined;
    return self.page.graph.allowLock === true && (editBy !== undefined) && (editBy.id !== self.page.graph.session.id && self.isInConfig());
  };

  /**
   * 获取属性
   * 先取得自身该属性，如果没有 则集成page该属性，如果page也没有该属性，则集成graph该属性
   * 辉子 2021
   */
  self.get = field => {
    let value = self._data[field];
    if (value === undefined) {
      value = self.graph === undefined ? undefined : self.graph.setting[field];
    }
    switch (field) {
      // case "selectable":
      case 'deletable':
      case 'resizeable':
      case 'moveable':
      case 'editable':
        return value && self.isInConfig();
      case 'selectable':
        return value && !self.isLocked() && self.isInConfig();
      default:
        return value;
    }
  };

  /**
   * 获取边框宽度.
   *
   * @returns {*} 边框宽度.
   */
  self.getBorderWidth = () => {
    return self.isFocused ? self.focusBorderWidth : self.borderWidth;
  };

  // --------------load code---------------
  self.runCode = code => {
    _runCode(self, code);
  };
  self.isLoaded = () => detections.length > 0;

  self.load = (ignoreFilter = ((property) => {
  })) => {
    setCoEdit(self, ignoreFilter);
    self.runCode('loadCode');
  };

  // -------------initialization---------------------------
  self.initialize = args => {
  };
  self.initialized = args => {
  };

  /**
   * 当图形被创建之后的声明周期函数.
   * deserialize时，只会调用page.createShape方法，其中不会调用图形的initialize方法，因此有些逻辑不能只放在initialize中.
   * {@see page#createShape}
   */
  self.created = () => {
  };

  setTextOperation(self);
  self.ifDrawFocusFrame = () => {
    return false;
  };

  /**
   * 获取选中时的优先级.
   *
   * @param xVal 横坐标.
   * @param yVal 纵坐标.
   * @return {*|number} 优先级数值.
   */
  self.getSelectPriority = (xVal, yVal) => {
    if (self.page.selectionStrategy === SELECTION_STRATEGY.BRING_TO_FRONT) {
      if (self.isFocused) {
        return 10000;
      }
      // page的index是undefined，所以这里要做特殊处理.
      return self.index ? self.index : 0;
    } else {
      if (self.isFocused && self.priorityContains(xVal, yVal)) {
        return 10000;
      }
      // page的index是undefined，所以这里要做特殊处理.
      return self.index ? self.index : 0;
    }
  };

  setLocalCollaboration(self);
  return self;
};

const setLocalCollaboration = (shapeVal) => {
  /**
   * 接收到本地协同消息，发生了数据变化(例如，ppt场景下的主画布和缩略图之间的本地协同).
   *
   * @param data 变化的数据.
   */
  shapeVal.onMessageDataChange = (data) => {
    shapeVal.invalidateAlone();
  };
};

const _getString = (node) => {
  if (node.children) {
    return node.children.map(_getString).join('');
  }
  return node.data ? node.data : '';
};

/**
 * 设置文本相关操作.
 *
 * @param shapeVal 图形对象.
 */
const setTextOperation = (shapeVal) => {
  shapeVal.bold = false;
  shapeVal.italic = false;

  shapeVal.format = (key, value) => {
    if (shapeVal.hideText) {
      return;
    }
    const editor = shapeVal.drawer.getEditor();
    editor.format(key, value);
  };

  shapeVal.getFormatValue = (key) => {
    if (shapeVal.hideText) {
      return null;
    }
    const editor = shapeVal.drawer.getEditor();
    return editor.isFocused() ? editor.getFormatValue(key) : shapeVal.get(key);
  };

  /**
   * 当文本属性发生变化时调用.
   * attributes的格式为:
   * {
   *     bold: true,
   *     italic: false
   *     ...
   * }
   *
   * @param attributes 属性集合.
   */
  shapeVal.onTextAttributeChange = (attributes) => {
    const changedAttributes = {};
    Object.keys(attributes).forEach(key => {
      if (!shapeVal.serializedFields.has(key)) {
        return;
      }

      if (shapeVal[key] !== attributes[key]) {
        changedAttributes[key] = attributes[key];
      }
    });
    const layoutData = {shapeVal, ...changedAttributes};
    layoutCommand(shapeVal.page, [layoutData]).execute(shapeVal.page);
  };

  /**
   * 判断位置是否在文本中：
   *
   * @param position 位置信息.
   */
  shapeVal.isOnText = (position) => {
    return !shapeVal.hideText &&
      !shapeVal.drawer.isTextPointerEventsDisabled() &&
      shapeVal.drawer.containsText(position.x, position.y);
  };

  /**
   * shape处于编辑状态下，当enter键被按下后的事件
   * huiz 2020
   */
  shapeVal.editEnterPressed = () => {
  };

  /**
   * 开始编辑.
   * * 注意 * 一个图形在开始编辑之前，默认都是通过innerHtml进行渲染（提升性能）.只有第一次编辑过后，才通过编辑器渲染文本.
   *
   * @param x 横坐标.
   * @param y 纵坐标.
   * @param autoFocus 是否自动聚焦.
   * @return {*}
   */
  shapeVal.beginEdit = function () {
    let first = false;
    return (x, y, autoFocus = false) => {
      if (shapeVal.hideText || shapeVal.isEdit) {
        return;
      }

      if (!first) {
        shapeVal.drawer.renderTextByEditor(shapeVal.text, autoFocus);
        first = true;
      }
      shapeVal.isEdit = true;
      shapeVal.enableTextPointerEvents();
    };
  }();

  /**
   * 结束编辑
   * 将编辑后value赋值shape
   * huiz 2020
   */
  shapeVal.endEdit = () => {
    if (shapeVal.hideText || !shapeVal.isEdit) {
      return;
    }
    shapeVal.isEdit = false;
    shapeVal.edited(shapeVal.drawer.text);
    shapeVal.disableTextPointerEvents();
  };

  shapeVal.enableTextPointerEvents = () => {
    shapeVal.drawer.enableTextPointerEvents();
  };

  shapeVal.disableTextPointerEvents = () => {
    shapeVal.drawer.disableTextPointerEvents();
  };

  /**
   * 是否启用使用html来绘制文本.默认为true.
   * 这里使用方法的原因是为了兼容老数据，如果加入新的字段来进行控制，那么老数据中是没有该字段的。在不进行数据修正的情况下，通过增加方法来控制。
   *
   * @returns {boolean} true/false
   */
  shapeVal.isEnableHtmlText = () => {
    return true;
  };

  /**
   * 在图形编辑态的时候手动修改text。
   * @param text
   */
  shapeVal.setTextManually = (text) => {
    shapeVal.text = text;
    if (shapeVal.isEditing()) {
      shapeVal.drawer.renderText();
    }
  };

  /**
   * 获取图形文本数据(纯字符串).
   *
   * @returns {*|string|string} 字符串.
   */
  shapeVal.getShapeText = () => {
    if (typeof shapeVal.text === 'string') {
      return shapeVal.text;
    } else {
      return _getString({children: shapeVal.text});
    }
  };
};

const cachePool = (maxSize, createNew, resume, resumeFilter, ignoreInvalidate) => {
  const cacheWrapper = (self, resumeVal) => {
    self.idle = () => !self.visible;
    self.remove = () => {
      return self.visible = false;
    };
    self.resume = function (...args) {
      args.unshift(self);
      resumeVal.apply(this, args);
      self.visible = true;
    };
  };
  return {
    values: [], max: maxSize, show: function (...args) {
      let self = this;
      if (resumeFilter) {
        args.unshift(null);
      }
      const idle = self.values.find(s => {
        if (!s.idle()) {
          return false;
        }
        if (!resumeFilter) {
          return true;
        }
        args[0] = s;
        return resumeFilter.apply(this, args);
      });
      if (idle) {
        idle.delayInvalidate(() => {
          idle.resume(...args);
        }, ignoreInvalidate);
        return idle;
      }
      if (self.values.length < self.max) {
        let obj = createNew.apply(this, args);
        cacheWrapper(obj, resume);
        self.values.push(obj);
        return obj;
      }
      return undefined;
    },
  };
};

const _runCode = (shapeVal, code) => {
  if (shapeVal.get(code) === undefined) {
    return;
  }
  try {
    const c = `(async ${shapeVal.get(code)})(shape.page, shape);`;
    eval(c);
  } catch (e) {
    // 没关系，继续，不影响其他错误信息的处理.
  }
};

const _effectLines = (shapeVal) => {
  shapeVal.manageConnectors();
  shapeVal.page.sm.getShapes(s => s.fromShape === shapeVal.id || s.toShape === shapeVal.id)
    .forEach(s => s.onEffect && s.onEffect());
};

const convertScrollWithParents = (s, pos) => {
  const parent = s.getContainer();
  if (parent === s.page) {
    return;
  }
  pos.x -= parent.itemScroll.x;
  pos.y -= parent.itemScroll.y;
  convertScrollWithParents(parent, pos);
};

const setContains = shapeVal => {
  /**
   * 为鼠标命中某个形状
   * 判定某个坐标点是否可以捕获该形状
   * 依据几个方面捕获：坐标点是否在形状的框里 + （命中了背景 || 命中的边框 || 命中的文字 || 命中了点击区）
   * 各个命中方法在不同形状里可能有多态实现：rectangle，line，container
   * 辉子 2020
   */
  shapeVal.contains = (x, y) => {
    if (isNaN(x) || isNaN(y)) {
      return false;
    }

    let pos = convertPositionWithParents(shapeVal, x, y); // 处理旋转
    convertScrollWithParents(shapeVal, pos);

    if (!isPointInRect(pos, shapeVal.getBound())) {
      return false;
    }
    if (shapeVal.drawer.containsBorder && shapeVal.drawer.containsBorder(pos.x, pos.y)) {
      return true;
    }
    if (shapeVal.containsRegion(x, y)) {
      return true;
    }
    if (shapeVal.drawer.containsBack && shapeVal.drawer.containsBack(pos.x, pos.y)) {
      return true;
    }
    if (shapeVal.drawer.containsText && shapeVal.drawer.containsText(pos.x, pos.y)) {
      return true;
    }
    return false;
  };

  /**
   * 是否命中优先级高的区域.
   *
   * @param x 横坐标.
   * @param y 纵坐标.
   * @return {*|boolean} true/false.
   */
  shapeVal.priorityContains = (x, y) => {
    let pos = convertPositionWithParents(shapeVal, x, y); // 处理旋转
    convertScrollWithParents(shapeVal, pos);
    return shapeVal.drawer.containsBorder(pos.x, pos.y) ||
      shapeVal.containsRegion(x, y) ||
      shapeVal.getMouseOnConnector(x, y) !== null;
  };

  /**
   * 粗略找到形状，然后用contains精确匹配
   * 为提高find性能
   */
  shapeVal.roughContains = (x, y) => {
    if (isNaN(x) || isNaN(y)) {
      return false;
    }
    let pos = convertPositionWithParents(shapeVal, x, y);
    convertScrollWithParents(shapeVal, pos);
    return isPointInRect(pos, shapeVal.getBound());
  };
};

const setSerialize = shapeVal => {
  shapeVal.serialize = () => {
    return shapeVal._data;
  };

  shapeVal.serialized = () => {
  };
  shapeVal.deSerialized = () => {
  };
  // -------------------end of serialization----------------
  shapeVal.deSerialize = (serialized) => {
    shapeVal.page.ignoreReact(() => {
      let isReady = shapeVal.page.isReady;
      shapeVal.page.isReady = false;
      shapeVal._data = serialized;
      shapeVal.load();
      shapeVal.deSerialized(shapeVal);
      shapeVal.page.isReady = isReady;
    });
  };
};

const setDetection = (shapeVal, detections) => {
  shapeVal.clearDetections = () => detections.splice(0, detections.length);
  shapeVal.clearDetections();

  /**
   * 删除对某个属性的监听器.
   *
   * @param propertyKey 属性名.
   */
  shapeVal.removeDetection = (propertyKey) => {
    const tmpDetections = [...detections];
    tmpDetections.forEach((d, index) => {
      if (d.props.has(propertyKey)) {
        d.props.size === 1 ? detections.splice(index, 1) : d.props.delete(propertyKey);
      }
    });
  };

  shapeVal.addDetection(['visible', 'background'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    shapeVal.invalidateAlone();
  });

  shapeVal.addDetection(['editBy'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    if (value !== undefined && preValue !== undefined && value.length === preValue.length) {
      value.orderBy('id');
      preValue.orderBy('id');
      let identical = true;
      for (let i = 0; i < value.length; i++) {
        if (preValue[i].id !== value[i].id) {
          identical = false;
          break;
        }
      }
      if (identical) {
        return;
      }
    }
    if (value === undefined) {
      shapeVal.regions.remove(s => s.type === 'lock');
    } else {
      shapeVal.regions.remove(r => r.type === 'lock' && !value.contains(u => u.id === r.userId));
      value.filter(u => u.id !== shapeVal.page.graph.session.id).forEach((u, i) => {
        const region = shapeVal.regions.find(r => r.userId === u.id);
        if (region) {
          region.index = i;
        } else {
          lockRegion(shapeVal, u.id, u.name, i);
        }
      });
    }
    shapeVal.drawer.drawRegions();
  });

  shapeVal.addDetection(['shareAble'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    shapeVal.drawer.drawRegions();
  });

  shapeVal.addDetection(['isFocused'], (property, value, preValue) => {
    if (value !== preValue) {
      // 触发一次focused change事件，内容不重要
      shapeVal.page.triggerEvent({type: EVENT_TYPE.FOCUSED_SHAPE_CHANGE, value: [shapeVal]});
      shapeVal.page.triggerEvent({type: EVENT_TYPE.CONTEXT_CREATE, value: []});
    }

    shapeVal.isFocused = value && shapeVal.getSelectable();
    if (shapeVal.isFocused) {
      if (shapeVal.focused) {
        shapeVal.focused();
      }
      if (shapeVal.isInConfig()) {
        shapeVal.page.graph.session.page = shapeVal.page.id;
        shapeVal.page.graph.session.shape = shapeVal.id; // 用该属性代替lockedBy
      }
    } else {
      shapeVal.unfocused();
      if (shapeVal.page.graph.session.shape === shapeVal.id) {
        shapeVal.page.graph.session.shape = undefined;
      }
    }

    if (shapeVal.isFocused !== preValue) {
      shapeVal.invalidateAlone();
    }
  });

  shapeVal.addDetection(['text'], (property, value, preValue) => {
    if (shapeVal.page) {
      shapeVal.page.textChanged(shapeVal, value, preValue);
      shapeVal.textChanged(value, preValue);
    }
    if (shapeVal.drawer) {
      shapeVal.drawer.renderText();
    }
  });

  shapeVal.addDetection(['id'], (property, value, preValue) => {
    if (!shapeVal.isTypeof('container')) {
      return;
    }
    shapeVal.page.sm.getShapes(s => s.container === preValue).forEach(s => s.container = value);
    shapeVal.page.graph.resetElementId(value, preValue);
  });

  shapeVal.addDetection(['container'], (property, value, preValue) => {
    // 这里的第二个判断条件 value === ""的原因如下:
    // 1、在presentation的场景下，presentationPage中的childAllowed中会判断是否是presentationFrame
    // 2、如果被删除图形不是presentationFrame，那么就会调用shape.page.moveToContainer方法，重新给图形设置container.
    // 3、这样就会导致图形无法被删除.
    if (value === preValue || value === '') {
      return;
    }

    if (!shapeVal.containerAllowed(shapeVal.getContainer()) || !shapeVal.getContainer().childAllowed(shapeVal)) {
      shapeVal.page.moveToContainer(shapeVal, preValue);
      return;
    }

    if (preValue) {
      shapeVal.preContainer = preValue;
      const previousContainer = shapeVal.page.id === preValue ? shapeVal.page : shapeVal.page.getShapeById(preValue);
      if (previousContainer && previousContainer.isTypeof('container')) {
        previousContainer.shapeRemoved(shapeVal, value);
      }
    }

    shapeVal.getContainer().shapeAdded(shapeVal, preValue);
    shapeVal.containerChanged(preValue, value);
  });

  shapeVal.addDetection(['rotateDegree'], (property, value, preValue) => {
    shapeVal.drawer.transform();
  });

  shapeVal.addDetection(['backColor', 'backAlpha', 'cornerRadius', 'dashWidth'], (property, value, preValue) => {
    if (preValue === value) {
      return;
    }
    if (shapeVal.drawer !== undefined) {
      shapeVal.render();
    }
  });

  shapeVal.addDetection(['fontColor'], (property, value, preValue) => {
    if (preValue === value) {
      return;
    }
    if (shapeVal.drawer !== undefined) {
      if (shapeVal.type === 'shapeComment') {
        shapeVal.render();
      }
    }
  });

  shapeVal.addDetection(['background'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    shapeVal.drawer.backgroundRefresh();
  });

  shapeVal.addDetection(['inDragging', 'mousedownConnector'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }

    /*
     * * 注意 *
     * 当图形处于拖动状态下，或选中了connector时，需要将文本的pointerEvents事件设置为null
     * 否则，会导致鼠标不跟手，会导致坐标计算出现误差（具体原理暂不清楚）
     */
    if (value) {
      shapeVal.drawer.disableTextPointerEvents();
    } else {
      if (shapeVal.isFocused && shapeVal.isEditing()) {
        shapeVal.drawer.enableTextPointerEvents();
      }
    }
  });

  shapeVal.getDetections = () => detections;
};

const setCoEdit = (shapeVal, ignoreFilter) => {
  shapeVal.allowCoEdit = true; // 允许共享编辑，或者说是否需要共享编辑
  /**
   * 该shape是否已经修改
   */
  shapeVal.dirty = false;
  /**
   * 相应协同属性变化，默认重绘
   * 辉子 2021
   */
  shapeVal.reactCoEdit = () => shapeVal.invalidateAlone();

  // 判定该形状是否有属性改变了（若属性是数组或object，则无法响应变化，需手动触发该方法)
  shapeVal.propertyChanged = (property, value, preValue) => {
    if (value === preValue) {
      return false;
    }
    if (!shapeVal.page.isReady) {
      return false;
    }
    if (!shapeVal.serializable) {
      return false;
    }
    if (!shapeVal.serializedFields.has(property)) {
      return false;
    }
    if (ignoreFilter && ignoreFilter(property, value, preValue)) {
      return false;
    }

    shapeVal.dirty = true;

    // send message to share with co-editors
    if (!(shapeVal.allowCoEdit && shapeVal.page.allowCoEdit)) {
      return true;
    }
    if (shapeVal.page.ignoreCoEditFields && shapeVal.page.ignoreCoEditFields.contains(f => f === property)) {
      return true;
    }
    if (shapeVal.ignoreCoEditFields && shapeVal.ignoreCoEditFields.contains(f => f === property)) {
      return true;
    }

    let id = shapeVal.id;
    if (property === 'id') {
      id = preValue;
    }
    (!shapeVal.page.dirties) && (shapeVal.page.dirties = {});
    const sharedParent = shapeVal.sharedParent();
    if (!shapeVal.page.dirties[id]) {
      shapeVal.page.dirties[id] = {};

      /*
       * 这里需要使用shape身上静态的pageId属性，否则，在page.sendDirties()中，可能会取到错误的pageId.
       * 例如：
       * 1、打开page1，异步渲染图形或修改图形属性
       * 2、切换页面到page2，此时对图形属性的修改还在继续
       * 3、触发propertyChanged方法，生成dirties
       * 4、page.sendDirties()中获取到dirties，并设置pageId，此时的pageId是page2
       * 5、page2消费到了page1的改动信息，生成错误图形
       */
      shapeVal.page.dirties[id].pageId = shapeVal.pageId || (shapeVal.isTypeof('page') ? shapeVal.id : null);
      shapeVal.page.dirties[id].inShared = sharedParent.id; // 标识该shape是不是隶属shared shape，所有属性需要share
    }
    if (value === null) {
      (!shapeVal.page.dirties[id].nullProperties) && (shapeVal.page.dirties[id].nullProperties = []);
      shapeVal.page.dirties[id].nullProperties.push(property);
    } else {
      shapeVal.page.dirties[id][property] = value;
      // only happen when shape is in shared
      if (property === 'x' && sharedParent.id !== '' && sharedParent !== shapeVal.page) {
        shapeVal.page.dirties[id].dx = shapeVal.x - sharedParent.x;
      }
      if (property === 'y' && sharedParent.id !== '' && sharedParent !== shapeVal.page) {
        shapeVal.page.dirties[id].dy = shapeVal.y - sharedParent.y;
      }
    }
    shapeVal.addFixedDirtyProperties(id);
    return true;
  };
  shapeVal.addFixedDirtyProperties = (id) => {
    shapeVal.page.dirties[id].type = shapeVal.type;
  };
};

const setConnector = shapeVal => {
  shapeVal.connectors = undefined;

  /**
   * 找到鼠标在的connector
   * 鼠标命中优先级：connector，region，shape
   * 辉子 2020
   */
  shapeVal.getMouseOnConnector = (x, y, condition = () => true) => {
    if (!shapeVal.isFocused && !shapeVal.linking) {
      return null;
    }
    if (shapeVal.connectors === undefined) {
      return null;
    }
    let pos = convertPositionWithParents(shapeVal, x, y);
    pos.x -= shapeVal.x;
    pos.y -= shapeVal.y;
    // 不可见的connector，也不可以被拖拽
    let conn = shapeVal.connectors.reverse()
      .find(connector =>
        connector.getVisibility() &&
        isPointInRect(pos, connector.getHitRegion()) &&
        condition(connector),
      );
    return conn === undefined ? null : conn;
  };

  /**
   * 获取坐标距离xy最近的connector.
   *
   * @param x 横坐标.
   * @param y 纵坐标.
   * @param condition 条件函数.
   * @return {*|null}
   */
  shapeVal.getClosestConnector = (x, y, condition = () => true) => {
    if (!shapeVal.isFocused && !shapeVal.linking) {
      return null;
    }
    if (shapeVal.connectors === undefined) {
      return null;
    }
    let pos = convertPositionWithParents(shapeVal, x, y);
    pos.x -= shapeVal.x;
    pos.y -= shapeVal.y;

    let connector = null;
    let distance = null;
    shapeVal.connectors.reverse().filter(c => c.getVisibility() && condition(c)).forEach(c => {
      const d = getDistance(pos.x, pos.y, c.x, c.y);
      if (distance === null || d < distance) {
        distance = d;
        connector = c;
      }
    });

    return connector;
  };

  /**
   * 初始化connectors:创建出对应的connectors
   * 不同的shape的connctor是不一样，比如line是收尾各一个connector，所有以rectangle为基类的shape有9个connector（参考rectangle.initConnectors)
   * 辉子 2021
   */
  shapeVal.initConnectors = () => {
    shapeVal.connectors = [];
  };

  shapeVal.getConnectors = () => {
    if (shapeVal.connectors === undefined) {
      shapeVal.initConnectors();
    }
    return shapeVal.connectors;
  };

  /**
   * 重新整理connector的位置，在shape resize之后
   * 不重新创建connector
   */
  shapeVal.manageConnectors = () => {
    shapeVal.getConnectors().forEach(c => c.refresh());
  };
};

const setRegion = shapeVal => {
  shapeVal.regions = []; // 热区，可以点击，事件

  shapeVal.addRegion = (region, index) => {
    if (index === undefined) {
      shapeVal.regions.push(region);
    } else {
      shapeVal.regions.insert(region, index);
    }
    for (let i = 0; i < shapeVal.regions.length; i++) {
      shapeVal.regions[i].index = i;
    }
  };
  shapeVal.removeRegion = (index) => {
    shapeVal.regions.splice(index, 1);
    for (let i = 0; i < shapeVal.regions.length; i++) {
      shapeVal.regions[i].index = i;
    }
    shapeVal.drawer.drawRegions();
  };
  shapeVal.containsRegion = (x, y) => shapeVal.regions.find(r => r.visible && r.isMouseIn(x, y) && r.getSelectable()) !== undefined;

  /**
   * 找到鼠标在该shape命中的region
   */
  shapeVal.getHitRegion = (x, y, pCursor) => {
    const pos = convertPositionWithParents(shapeVal, x, y);

    // 这里需要遍历当前图形的父元素，看是否存在恰当的region.
    let region = null;
    let parent = shapeVal;
    while (!region && parent !== shapeVal.page) {
      region = parent.regions.reverse().find(r => r.getVisibility() && r.isMouseIn(pos.x, pos.y) && r.getSelectable(), null);
      parent.regions.reverse();
      parent = parent.getContainer();
    }

    // 如果有connector，不选中region
    if (shapeVal.getMouseOnConnector(x, y) !== null) {
      region = null;
    }
    if (!region) {
      region = {
        click: (xVal, yVal) => shapeVal.click(xVal, yVal),
        dbClick: (xVal, yVal) => shapeVal.dbClick(xVal, yVal),
        cursor: pCursor,
        editable: false,
        dubble: true,
        isMock: true,
      };
      return region;
    } else {
      return {
        click: (xVal, yVal) => {
          shapeVal.page.regionClick(region);
          region.click(xVal, yVal);
        }, dbClick: (xVal, yVal) => () => {
          shapeVal.page.regionDbClick(region);
          region.dbClick(xVal, yVal);
        }, cursor: region.cursor, editable: region.editable, text: region.text, type: region.type,
      };
    }
  };
};

const setMouseActions = (shapeVal) => {
  shapeVal.click = (x, y) => {
    if (shapeVal !== shapeVal.page) {
      shapeVal.page.playAudio('click');
    }
    shapeVal.runCode('clickCode');
  };
  shapeVal.dbClick = (x, y) => {
  };

  shapeVal.rotatePosition = position => {
    const pos = convertPositionWithParents(shapeVal, position.x, position.y);
    position.x1 = pos.x;
    position.y1 = pos.y;
    shapeVal.mouse = {x: position.x, y: position.y, x1: pos.x, y1: pos.y};
  };

  /**
   * 多选处理逻辑.
   *
   * @param position 位置信息.
   */
  const onSelectMultiply = (position) => {
    shapeVal.isFocused ? shapeVal.unSelect() : shapeVal.select(position.x, position.y);
  };

  /**
   * 单选处理逻辑.
   *
   * @param position 位置信息.
   */
  const onSelectIndividually = (position) => {
    const focusedShapes = shapeVal.page.getFocusedShapes();
    if (shapeVal.isFocused) {
      // 如果选中了图形的文本，则需要取消其他图形的选中状态.
      if (shapeVal.isOnText(position)) {
        focusedShapes.filter(s => s !== shapeVal).forEach(s => s.unSelect());

        // 如果选中了文本，需要将mousedownShape设置为null，防止图形在编辑时可以被拖动.
        shapeVal.page.mousedownShape = null;
      }
    } else {
      focusedShapes.forEach(s => s.unSelect());
      shapeVal.select();
    }
  };

  shapeVal.onMouseDown = position => {
    // 是否是多选操作.
    (position.e.shiftKey || position.e.ctrlKey) ? onSelectMultiply(position) : onSelectIndividually(position);
    shapeVal.mouseOffsetX = position.x - shapeVal.x;
    shapeVal.mouseOffsetY = position.y - shapeVal.y;
    shapeVal.mousedownConnector = shapeVal.getMouseOnConnector(position.x, position.y, c => c.dragable);
    if (shapeVal.mousedownConnector) {
      shapeVal.mousedownConnector.onMouseDown(position);
    }
    shapeVal.mousedownRegion = shapeVal.getHitRegion(position.x, position.y);
    shapeVal.runCode('mouseDownCode');
  };

  shapeVal.onLongClick = shapeVal.onMouseDown;

  shapeVal.onMouseUp = async position => {
    if (shapeVal.mousedownConnector !== null) {
      shapeVal.page.cancelClick = true;
      await shapeVal.mousedownConnector.release(position);
      shapeVal.mousedownConnector = null;
    }
    if (shapeVal.inDragging) {
      shapeVal.inDragging = false;
      shapeVal.reset(position.x, position.y);
      shapeVal.endDrag(shapeVal.page.find(position.x, position.y, s => s.isTypeof('container') && s !== shapeVal), position);
    }
    shapeVal.runCode('mouseUpCode');
  };
  shapeVal.onMouseMove = position => {
    shapeVal.runCode('mouseMoveCode');
    if (shapeVal.page.cursor === CURSORS.PEN) {
      return;
    }

    let conn = shapeVal.getMouseOnConnector(position.x, position.y);
    shapeVal.mouseOnConnector = conn;

    // 类型为connection的connector，在鼠标移动时，不应该进行处理，否则会导致鼠标移动绘制异常.
    if (conn !== null && conn.type !== 'connection') {
      shapeVal.page.cursor = conn.direction.cursor;
    } else {
      const r = shapeVal.getHitRegion(position.x, position.y, CURSORS.MOVE);
      if (r.isMock) {
        // 鼠标在图形的文本上，并且图形处于编辑态，才将cursor样式修改为TEXT.
        if (shapeVal.isOnText(position) && shapeVal.isEditing()) {
          shapeVal.page.cursor = CURSORS.TEXT;
          return;
        }

        if (!shapeVal.moveable) {
          // 如果图形的cursorStyle不存在，则设置为默认.
          if (shapeVal.cursorStyle) {
            shapeVal.page.cursor = shapeVal.cursorStyle;
          } else {
            shapeVal.page.cursor = CURSORS.DEFAULT;
          }
          return;
        }
      }

      shapeVal.mouseOnRegion = r;
      shapeVal.page.cursor = r.cursor;
      shapeVal.page.showTip(position.x, position.y, r.text, shapeVal);
    }
  };
  shapeVal.onMouseOut = () => {

  };
  shapeVal.onMouseIn = () => {

  };

  shapeVal.onMouseHold = position => {

  };

  shapeVal.getDragable = () => shapeVal.dragable && shapeVal.getContainer().isAllowChildDrag();

  shapeVal.beginDrag = () => {
  };

  shapeVal.getDockContainerThatPermitChildDraggable = () => {
    let container = shapeVal.getContainer();
    while (container !== shapeVal.page) {
      if (container.dockMode !== DOCK_MODE.NONE && container.isAllowChildDrag()) {
        return container;
      }
      container = container.getContainer();
    }
    return null;
  };

  shapeVal.endDrag = (target, position) => {
    const container = shapeVal.getDockContainerThatPermitChildDraggable();
    if (container) {
      container.invalidate();
      container.getShapes().forEach(child => {
        const dirty = position.context.shapes.find(s => s.shape === child);
        dirty.x.value = child.x;
        dirty.y.value = child.y;
        dirty.container.value = child.container;
      });
    }

    _effectLines(shapeVal);
  };
  shapeVal.dragging = (target, position) => {
    // 如果存在container处于DockMode模式，并且允许子元素被拖动，则需要记录其所有子元素的位置信息，以便后续的撤销重做.
    const container = shapeVal.getDockContainerThatPermitChildDraggable();
    if (container) {
      container.getShapes().forEach(child => {
        let dirty = position.context.shapes.find(s => s.shape === child);
        if (!dirty) {
          dirty = {shape: child, x: {}, y: {}, container: {}};
          dirty.x.preValue = child.x;
          dirty.y.preValue = child.y;
          dirty.container.preValue = child.container;
          position.context.shapes.push(dirty);
        }
      });
      container.invalidate();
    }
  };

  shapeVal.onConnectorDragged = connector => {
  };

  shapeVal.isInDragging = () => {
    return shapeVal.inDragging;
  };

  /**
   * resize图形或旋转图形.
   *
   * @param position 位置信息.
   * @param focusedShapes 选中的图形.
   */
  shapeVal.resizeOrRotate = (position, focusedShapes) => {
    shapeVal.page.cursor = shapeVal.mousedownConnector.direction.cursor;
    const originalX = position.x;
    const originalY = position.y;
    const xDiff = originalX - shapeVal.x - (shapeVal.width / 2);
    const yDiff = originalY - shapeVal.y - (shapeVal.height / 2);
    focusedShapes.forEach(s => {
      // 如果父元素在选中图形的列表里，则不处理当前图形.
      let parent = s.getContainer();
      while (parent !== shapeVal.page) {
        if (focusedShapes.includes(parent)) {
          return;
        } else {
          parent = parent.getContainer();
        }
      }
      s.mousedownConnector = s.connectors.find(c => c.isType(shapeVal.mousedownConnector.type) && c.dragable);
      position.x = xDiff + (s.width / 2) + s.x;
      position.y = yDiff + (s.height / 2) + s.y;
      if (s.mousedownConnector) {
        s.mousedownConnector.onMouseDrag(position);
      }
    });
    position.x = originalX;
    position.y = originalY;
  };

  /**
   * 响应鼠标拖拽事件.
   *
   * @param position 位置信息.
   */
  shapeVal.onMouseDrag = (position) => {
    if (!shapeVal.isInConfig()) {
      return;
    }

    if (!shapeVal.moveable) {
      shapeVal.unSelect();
      shapeVal.page.mousedownShape = shapeVal.page;
      return;
    }

    const shapes = shapeVal.page.getFocusedShapes().filter(s => s.getDragable());

    // resize或rotate选中的shapes
    if (shapeVal.mousedownConnector) {
      shapeVal.resizeOrRotate(position, shapes);
      return;
    }

    if (shapeVal.mousedownRegion && shapeVal.mousedownRegion.dragable) {
      shapeVal.mousedownRegion.onMouseDrag(position);
      return;
    }

    // 被影响的线.
    const lines = shapeVal.page.getEffectLines(shapes);
    const dragShapes = [...lines, ...shapes];

    // 移动选择的shapes
    shapeVal.inDragging = true;
    dragShapes.forEach(s => dragShape(s, position));
    shapeVal.dragging(shapeVal.page.find(position.x, position.y, s => s.isTypeof('container') && s !== shapeVal), position);
    shapeVal.runCode('draggingCode');
  };

  const dragShape = (s, position) => {
    let currentShape = s;
    let parent = currentShape.getContainer();
    if (parent === currentShape.page) {
      currentShape.dragTo(position);
      currentShape.page.moveToContainer(currentShape);
      return;
    }
    while (parent !== currentShape.page) {
      const noDock = parent.dockMode === DOCK_MODE.NONE && currentShape.pDock === PARENT_DOCK_MODE.NONE;
      if (parent.isChildDragable() || noDock) {
        parent.onChildDragTo(currentShape, position);
        currentShape.dragTo(position);
        currentShape.page.moveToContainer(currentShape);
        break;
      } else {
        currentShape = parent;
        parent = parent.getContainer();
      }
    }
  };
};

const setKeyAction = shapeVal => {
  shapeVal.numberPressed = () => shapeVal.runCode('numberPressedCode');

  shapeVal.keyPressed = e => {
    shapeVal.runCode('keyPressedCode');
    if ((e.ctrlKey || e.metaKey) && (e.code === 'KeyS')) {
      return false;
    }

    if ((e.ctrlKey || e.metaKey) && (e.code === 'KeyF')) {
      shapeVal.removeTextFormat();
      return false;
    }

    if (e.code === 'Escape') {
      shapeVal.unSelect();
      shapeVal.restore();
    }
    if (!shapeVal.moveable) {
      return false;
    }
    let direction = {x: 0, y: 0};
    let step = 2;
    let shiftStep = step * 10;
    if (e.key.indexOf('Left') >= 0) {
      direction.x = -step;
    }
    if (e.key.indexOf('Right') >= 0) {
      direction.x = step;
    }
    if (e.key.indexOf('Up') >= 0) {
      direction.y = -step;
    }
    if (e.key.indexOf('Down') >= 0) {
      direction.y = step;
    }
    if (e.shiftKey && e.key.indexOf('Left') >= 0) {
      direction.x = -shiftStep;
    }
    if (e.shiftKey && e.key.indexOf('Right') >= 0) {
      direction.x = shiftStep;
    }
    if (e.shiftKey && e.key.indexOf('Up') >= 0) {
      direction.y = -shiftStep;
    }
    if (e.shiftKey && e.key.indexOf('Down') >= 0) {
      direction.y = shiftStep;
    }
    if (direction.x !== 0 || direction.y !== 0) {
      const shapes = shapeVal.page.getFocusedShapes().filter(s => s.getDragable());
      const effectedLines = shapeVal.page.getEffectLines(shapes);
      const dragShapes = [...effectedLines, ...shapes];
      dragShapes.forEach(s => {
        s.dragTo({
          x: s.x + direction.x,
          deltaX: direction.x,
          y: s.y + direction.y,
          deltaY: direction.y,
          context: {shapes: []},
        });
      });
      return false;
    }
    return undefined;
  };
};

const setCoordinateIndex = shapeVal => {
  /**
   * 将形状放进位置缓存
   * 包括两个步骤，清除缓存和建立缓存
   * 辉子 2021
   */
  shapeVal.indexCoordinate = () => {
    if (shapeVal.page.isMouseDown()) {
      return;
    }
    shapeVal.clearCoordinateIndex();
    if (shapeVal.container === '') {
      return;
    }
    if (!shapeVal.getVisibility()) {
      return;
    }
    shapeVal.createCoordinateIndex();
  };

  /**
   * 清除所有自己的索引
   */
  shapeVal.clearCoordinateIndex = () => {
    // @maliya 增加数组类型判断，部分场景发现会有对象值，导致js报错
    const areas = shapeVal.areas && Array.isArray(shapeVal.areas) ? shapeVal.areas : [];
    areas.forEach(a => a.shapes.remove(s => s.id === shapeVal.id));
    shapeVal.areas = [];
  };

  shapeVal.createCoordinateIndex = () => {
    const STEP = shapeVal.page.areaStep;
    shapeVal.areas = [];
    const frame = shapeVal.getShapeFrame(true);
    let x1 = frame.x1;
    let y1 = frame.y1;
    const x2 = frame.x2;
    const y2 = frame.y2;
    x1 = Math.floor(x1 / STEP) * STEP;
    y1 = Math.floor(y1 / STEP) * STEP;
    for (let x = x1; x < x2; x += STEP) {
      for (let y = y1; y < y2; y += STEP) {
        const area = shapeVal.page.addShapeToAreas(x, y, shapeVal);

        // 双向关联，为删除性能.
        shapeVal.areas.push(area);
      }
    }
  };

  shapeVal.getShapeFrame = withMargin => {
    const margin = withMargin ? shapeVal.margin : 0;
    let x1 = (shapeVal.width > 0 ? shapeVal.x : (shapeVal.x + shapeVal.width)) - margin;
    let y1 = (shapeVal.height > 0 ? shapeVal.y : (shapeVal.y + shapeVal.height)) - margin;
    let x2 = x1 + Math.abs(shapeVal.width) + (2 * margin);
    let y2 = y1 + Math.abs(shapeVal.height) + (2 * margin);

    let p1 = convertPositionWithParents(shapeVal, x1, y1);
    let p2 = convertPositionWithParents(shapeVal, x2, y1);
    let p3 = convertPositionWithParents(shapeVal, x2, y2);
    let p4 = convertPositionWithParents(shapeVal, x1, y2);

    return {
      x1: Math.min(p1.x, p2.x, p3.x, p4.x),
      x2: Math.max(p1.x, p2.x, p3.x, p4.x),
      y1: Math.min(p1.y, p2.y, p3.y, p4.y),
      y2: Math.max(p1.y, p2.y, p3.y, p4.y),
    };
  };
};

export {shape, cachePool};
