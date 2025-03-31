/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, DIRECTION, FONT_WEIGHT, INFO_TYPE, PROGRESS_STATUS, ELSA_NAME_SPACE} from '../common/const.js';
import {
  convertPositionWithParents,
  getPixelRatio,
  getRotatedCoordinate,
  isPointInRect,
  sleep,
} from '../common/util.js';

import {shape} from './shape.js';
import {connector} from './connector.js';
import * as regions from './hitRegion.js';

import {rectangleDrawer} from './drawers/rectangleDrawer.js';
import {layoutCommand} from './commands.js';

/**
 * 矩形
 * 辉子
 * 2020-01
 */
const rectangle = (id, x, y, width, height, parent, drawer = rectangleDrawer) => {
  let self = shape(id, x, y, width, height, parent, drawer);// drawer === undefined ? rectangleDrawer : drawer);
  self.type = 'rectangle';
  self.text = '';
  self.emphasizedOffset = 0;
  self.rotateConnector = null;
  self.regionXoffset = 0;
  self.regionYoffset = 0;

  // -------------consts---------------
  let STEP = 13;
  let OFFSET_Y = -7;

  const dragTo = self.dragTo;
  self.dragTo = (position) => {
    if (!self.moveable) {
      return false;
    }
    const dirty = dragTo.call(self, position);

    const x = position.x;
    const y = position.y;
    const deltaX = position.deltaX;
    const deltaY = position.deltaY;
    self.page.ignoreReact(() => {
      // 根据移动方向，决定调整顺序，否则，移动过快导致的某一方向移动的距离大于图形相应方向的尺寸时，resize的尺寸为负数
      if (deltaX > 0) {
        self.rightConnectorMoving(deltaX, deltaY, x - deltaX, y);
        self.leftConnectorMoving(deltaX, deltaY, x, y);
      } else {
        self.leftConnectorMoving(deltaX, deltaY, x, y);
        self.rightConnectorMoving(deltaX, deltaY, x - deltaX, y);
      }
      if (deltaY > 0) {
        self.bottomConnectorMoving(deltaX, deltaY, x - deltaX, y - deltaY);
        self.topConnectorMoving(deltaX, deltaY, x - deltaX, y);
      } else {
        self.topConnectorMoving(deltaX, deltaY, x - deltaX, y);
        self.bottomConnectorMoving(deltaX, deltaY, x - deltaX, y - deltaY);
      }
    });
    return dirty;
  };

  let transformDelta = (self, x, y, deltaX, deltaY) => {
    const p1 = convertPositionWithParents(self.getContainer(), x, y);
    const p2 = convertPositionWithParents(self.getContainer(), x - deltaX, y - deltaY);
    // ---------------------still not working for rotated shape in rotated container-------------------------
    // 度数转换为弧度制.
    const degree = self.rotateDegree * Math.PI / 180;

    // 计算圆心.
    const o = {x: self.width / 2 + self.x, y: self.height / 2 + self.y};

    // 计算p1相对圆心o旋转-degree之后的坐标p3
    const p3 = getRotatedCoordinate(p1.x, p1.y, o.x, o.y, -degree);

    // 计算p2相对圆心o旋转-degree之后的坐标p4
    const p4 = getRotatedCoordinate(p2.x, p2.y, o.x, o.y, -degree);

    // 取向量p4p3的一半.
    const dx = (p3.x - p4.x) / 2;
    const dy = (p3.y - p4.y) / 2;
    return {dx: dx, dy: dy};
  };

  const getMenuScript = self.getMenuScript;
  self.getMenuScript = () => {
    const menus = getMenuScript.call(self);
    if (menus === undefined) {
      return undefined;
    }
    menus.push({
      text: '评论', action: (shapeArg) => {
        let comment = shapeComment(shapeArg.x + (shapeArg.width / 2), shapeArg.y + (shapeArg.height / 2), shapeArg);
        comment.invalidate();
        comment.beginEdit();
      }, draw: (context) => {
        context.strokeStyle = 'dimgray';
        context.strokeRect(-4, -3, 9, 6);
        context.fillStyle = 'red';
        context.fillRect(-2, -0.5, 1, 1);
        context.fillRect(0, -0.5, 1, 1);
        context.fillRect(2, -0.5, 1, 1);

      },
    });
    if (self === self.page) {
      return menus;
    }
    menus.push({
      text: `${(self.emphasized ? '取消' : '')}关注`,
      action: shapeArg => shapeArg.emphasized = !shapeArg.emphasized,
      draw: (context) => {
        context.strokeStyle = 'dimgray';
        context.arc(0, 0, 4, 0, 2 * Math.PI);
        context.stroke();
        context.beginPath();
        context.fillStyle = 'red';
        context.arc(0, 0, 2, 0, 2 * Math.PI);
        context.fill();

      },
    });

    menus.push({});
    // 优先级
    const priorities = {
      text: '优先级别', menus: [], draw: (context) => {
        context.strokeStyle = 'red';
        context.strokeRect(-3, -3, 3, 1);
        context.strokeStyle = 'dimgray';
        context.strokeRect(-3, 0, 5, 1);
        context.strokeRect(-3, 3, 7, 1);
      },
    };
    for (let i = 1; i < 10; i++) {
      priorities.menus.push({
        text: i.toString(), action: shape => shape.priority = i, draw: (context) => {
          const x0 = -4;
          const y0 = -3;
          const y1 = 3;
          context.fillStyle = 'steelblue';
          context.moveTo(x0, y0);
          context.lineTo(x0 + (i) / 2, y0);
          context.lineTo(x0 + (i + 2) / 2, y1);
          context.lineTo(x0, y1);
          context.closePath();
          context.fill();
        },
      });
    }
    priorities.menus.push({text: '无', action: shapeArg => shapeArg.priority = 0});
    menus.push(priorities);
    // 信息
    const warns = {
      text: '提示信息', menus: [], draw: (context) => {
        context.fillStyle = 'red';
        context.fillRect(-4, -1, 2, 2);
        context.fillRect(-1, -1, 2, 2);
        context.fillRect(2, -1, 2, 2);
      },
    };
    warns.menus.push({
      text: '提示', action: shapeArg => shapeArg.infoType = INFO_TYPE.INFORMATION, draw: (context) => {
        context.fillStyle = 'steelblue';
        context.font = 'bold 11px Arial';
        context.fillText('?', -3, 4);
      },
    });
    warns.menus.push({
      text: '警告', action: shapeArg => shapeArg.infoType = INFO_TYPE.WARNING, draw: (context) => {
        context.fillStyle = 'darkorange';
        context.font = 'bold 11px Arial';
        context.fillText('!', -3, 4);
      },
    });
    warns.menus.push({
      text: '错误', action: shapeArg => shapeArg.infoType = INFO_TYPE.ERROR, draw: (context) => {
        context.fillStyle = 'red';
        context.font = 'bold 11px Arial';
        context.fillText('×', -4, 4);
      },
    });
    warns.menus.push({text: '无', action: shapeArg => shapeArg.infoType = INFO_TYPE.NONE});
    menus.push(warns);
    // 状态
    const status = {
      text: '状态设置', menus: [], draw: (context) => {
        context.fillStyle = 'red';
        context.fillRect(-3, -3, 2, 6);
        context.fillStyle = 'dimgray';
        context.moveTo(0, -3);
        context.lineTo(6, 0);
        context.lineTo(0, 3);
        context.closePath();
        context.fill();
      },
    };
    status.menus.push({
      text: '未开始', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.NOTSTARTED, draw: (context) => {
        context.arc(0, 0, 4, 0, 2 * Math.PI);
        context.stroke();
        context.beginPath();
        context.fillStyle = 'red';
        context.fillRect(-1.5, -1.5, 3, 3);
      },
    });
    status.menus.push({
      text: '进行中', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.DOING, draw: (context) => {
        const STEPS = 10;
        context.fillStyle = 'gray';
        for (let i = 0; i < STEPS; i++) {
          context.rotate(2 * Math.PI / STEPS);
          context.fillRect(3, -0.75, 3, 0.75);
        }
      },
    });
    status.menus.push({
      text: '运行中', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.RUNNING, draw: (context) => {
        context.fillStyle = 'steelblue';
        context.moveTo(-3, -3);
        context.lineTo(3, 0);
        context.lineTo(-3, 3);
        context.closePath();
        context.fill();
      },
    });
    status.menus.push({
      text: '暂停', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.PAUSE, draw: (context) => {
        context.fillStyle = 'red';
        context.fillRect(-3, -4, 2, 8);
        context.fillRect(1, -4, 2, 8);
      },
    });
    status.menus.push({
      text: '完成', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.COMPLETE, draw: (context) => {
        const x0 = -3;
        const y0 = 1;
        context.strokeStyle = 'green';
        context.moveTo(x0, y0);
        context.lineTo(x0 + 3, y0 + 3);
        context.lineTo(x0 + 7, y0 - 7);
        context.stroke();
      },
    });
    status.menus.push({
      text: '错误', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.ERROR, draw: (context) => {
        context.fillStyle = 'darkred';
        context.font = 'bold 11px Arial';
        context.fillText('×', -4, 4);
      },
    });
    status.menus.push({
      text: '未知', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.UNKNOWN, draw: (context) => {
        context.fillStyle = 'dimgray';
        context.font = 'bold 11px Arial';
        context.fillText('?', -3, 4);
      },
    });
    status.menus.push({text: '无', action: shapeArg => shapeArg.progressStatus = PROGRESS_STATUS.NONE});
    menus.push(status);
    // 进度
    const progress = {
      text: '进度设置', menus: [], draw: (context) => {
        context.fillStyle = 'green';
        context.strokeRect(-4, -2, 8, 4);
        context.fillRect(-3, -1, 4, 2);
      },
    };
    const setProgress = (shape, percent) => {
      shape.showedProgress = true;
      shape.progressPercent = percent;
    };
    progress.menus.push({
      text: '15%', action: shapeArg => setProgress(shapeArg, 0.15), draw: (context) => {
        context.strokeStyle = context.fillStyle = 'darkorange';
        context.strokeRect(-4, -2, 8, 4);
        context.fillRect(-3, -1, 1, 2);
      },
    });
    progress.menus.push({
      text: '30%', action: shapeArg => setProgress(shapeArg, 0.3), draw: (context) => {
        context.strokeStyle = context.fillStyle = 'orange';
        context.strokeRect(-4, -2, 8, 4);
        context.fillRect(-3, -1, 2, 2);
      },
    });
    progress.menus.push({
      text: '45%', action: shapeArg => setProgress(shapeArg, 0.45), draw: (context) => {
        context.strokeStyle = context.fillStyle = 'gold';
        context.strokeRect(-4, -2, 8, 4);
        context.fillRect(-3, -1, 3, 2);
      },
    });
    progress.menus.push({
      text: '60%', action: shapeArg => setProgress(shapeArg, 0.6), draw: (context) => {
        context.strokeStyle = context.fillStyle = 'lightgreen';
        context.strokeRect(-4, -2, 8, 4);
        context.fillRect(-3, -1, 4, 2);
      },
    });
    progress.menus.push({
      text: '75%', action: shapeArg => setProgress(shapeArg, 0.75), draw: (context) => {
        context.strokeStyle = context.fillStyle = 'green';
        context.strokeRect(-4, -2, 8, 4);
        context.fillRect(-3, -1, 5, 2);
      },
    });
    progress.menus.push({
      text: '90%', action: shapeArg => setProgress(shapeArg, 0.90)
      , draw: (context) => {
        context.strokeStyle = context.fillStyle = 'darkgreen';
        context.strokeRect(-4, -2, 8, 4);
        context.fillRect(-3, -1, 6, 2);
      },
    });
    progress.menus.push({text: '无', action: shapeArg => shapeArg.showedProgress = false});
    menus.push(progress);

    return menus;
  };
  self.initConnectors = () => {
    self.connectors = [];

    const MIN = 15;
    const CON_MIN = 20;
    let release = position => {
      if (self.isNew) {
        self.width < MIN && (self.width = MIN);
        self.height < MIN && (self.height = MIN);
        delete self.isNew;
        position.context.command = 'addShape';
        position.context.shapes = [{shape: self}];
      }

      /*
       * 在对图形进行了resize之后，需要重新渲染，主要是进行indexCoordinate()，否则，图形扩大的部分无法被选中.
       * 可查看shape.indexCoordinate()方法，因为性能，在page.isMouseDown()时，不进行索引重建.
       * 所以，这里需要主动触发.
       */
      self.invalidateAlone();
    };

    let visible = shapeArg => {
      return shapeArg.moveable;
    };
    // west,index:0
    let c0 = connector(self,
      (s, c) => c.frame.x - s.x - s.focusMargin,
      (s, c) => c.frame.y - s.y + c.frame.height / 2,
      s => DIRECTION.W,
      s => s.visible && visible(s) && self.getFrame().height >= CON_MIN,
      s => s.resizeable,
      s => true,
      release,
      s => false,
    );
    let c0moving = (self, deltaX, deltaY, x, y) => {
      let degree = self.rotateDegree * Math.PI / 180;
      let dx = transformDelta(self, x, y, deltaX, deltaY).dx;
      let dy = 0;
      let offset = getRotatedCoordinate(dx, dy, 0, 0, degree);

      self.moveTo(self.x + offset.x + dx, self.y + offset.y + dy);
      if (self.mousedownConnector !== null) {
        self.resize(self.width - dx * 2, self.height);
      }
    };
    c0.moving = (deltaX, deltaY, x, y) => c0moving(self, deltaX, deltaY, x, y);
    c0.type = 'left';
    self.leftConnectorMoving = c0.moving;
    // east,index:1
    let c1 = connector(self,
      (s, c) => c.frame.x - s.x + c.frame.width + s.focusMargin,
      (s, c) => c.frame.y - s.y + c.frame.height / 2,
      s => DIRECTION.E,
      s => s.visible && visible(s) && self.getFrame().height >= CON_MIN,
      s => s.resizeable,
      s => true,
      release,
      s => false,
    );
    let c1moving = (self, deltaX, deltaY, x, y) => {
      let degree = self.rotateDegree * Math.PI / 180;
      let dx = transformDelta(self, x, y, deltaX, deltaY).dx;
      let dy = 0;
      let offset = getRotatedCoordinate(dx, dy, 0, 0, degree);

      self.moveTo(self.x + offset.x - dx, self.y + offset.y - dy);
      if (self.mousedownConnector !== null) {
        self.resize(self.width + dx * 2, self.height);
      }
    };
    c1.moving = (deltaX, deltaY, x, y) => c1moving(self, deltaX, deltaY, x, y);
    c1.type = 'right';
    self.rightConnectorMoving = c1.moving;
    // north,index:2
    let c2 = connector(self,
      (s, c) => c.frame.x - s.x + c.frame.width / 2,
      (s, c) => c.frame.y - s.y - s.focusMargin,
      s => DIRECTION.N,
      s => s.visible && visible(s) && self.getFrame().width >= CON_MIN,
      s => s.resizeable,
      s => true,
      release,
      s => false,
    );
    let c2moving = (self, deltaX, deltaY, x, y) => {
      let degree = self.rotateDegree * Math.PI / 180;
      let dx = 0;
      let dy = transformDelta(self, x, y, deltaX, deltaY).dy;
      let offset = getRotatedCoordinate(dx, dy, 0, 0, degree);

      self.moveTo(self.x + offset.x + dx, self.y + offset.y + dy);
      if (self.mousedownConnector !== null) {
        self.resize(self.width, self.height - dy * 2);
      }
    };
    c2.moving = (deltaX, deltaY, x, y) => c2moving(self, deltaX, deltaY, x, y);
    c2.visible = true;
    c2.type = 'top';
    self.topConnectorMoving = c2.moving;
    // south,index:3
    let c3 = connector(self,
      (s, c) => c.frame.x - s.x + c.frame.width / 2,
      (s, c) => c.frame.y - s.y + c.frame.height + s.focusMargin,
      s => DIRECTION.S,
      s => s.visible && visible(s) && self.getFrame().width >= CON_MIN,
      s => s.resizeable,
      s => true,
      release,
      s => false,
    );
    let c3moving = (self, deltaX, deltaY, x, y) => {

      let degree = self.rotateDegree * Math.PI / 180;
      let dx = 0;
      let dy = transformDelta(self, x, y, deltaX, deltaY).dy;
      let offset = getRotatedCoordinate(dx, dy, 0, 0, degree);

      self.moveTo(self.x + offset.x - dx, self.y + offset.y - dy);
      if (self.mousedownConnector !== null) {
        self.resize(self.width, self.height + dy * 2);
      }
    };
    c3.moving = (deltaX, deltaY, x, y) => c3moving(self, deltaX, deltaY, x, y);
    c3.type = 'bottom';
    self.bottomConnectorMoving = c3.moving;
    // north-west,index:4
    const proportion = self.width / self.height;
    let getC4Delta = (deltaX, deltaY) => {
      let deltaXVal = deltaX;
      let deltaYVal = deltaY;
      if (Math.abs(deltaXVal) > Math.abs(deltaYVal)) {
        deltaYVal = deltaXVal / proportion;
      } else {
        deltaXVal = deltaYVal * proportion;
      }
      return {deltaX: deltaXVal, deltaY: deltaYVal};
    };
    let c4 = connector(self,
      (s, c) => c.frame.x - s.x - s.focusMargin,
      (s, c) => c.frame.y - s.y - s.focusMargin,
      s => DIRECTION.NW,
      s => s.visible && visible(s),
      s => s.resizeable,
      s => false,
      release,
      s => self.page.shiftKeyPressed,
      getC4Delta,
    );
    c4.moving = (deltaX, deltaY, x, y) => {
      c0moving(self, deltaX, deltaY, x, y);
      c2moving(self, deltaX, deltaY, x, y);
    };
    c4.type = 'leftTop';

    // south-west,index:5
    let getC5Delta = (deltaX, deltaY) => {
      let deltaXVal = deltaX;
      let deltaYVal = deltaY;
      if (Math.abs(deltaXVal) > Math.abs(deltaYVal)) {
        deltaYVal = -deltaXVal / proportion;
      } else {
        deltaXVal = -deltaYVal * proportion;
      }
      return {deltaX: deltaXVal, deltaY: deltaYVal};
    };
    let c5 = connector(self,
      (s, c) => c.frame.x - s.x - s.focusMargin,
      (s, c) => c.frame.y - s.y + c.frame.height + s.focusMargin,
      s => DIRECTION.SW,
      s => s.visible && visible(s),
      s => s.resizeable,
      s => false,
      release,
      s => self.page.shiftKeyPressed,
      getC5Delta,
    );
    c5.moving = (deltaX, deltaY, x, y) => {
      c0moving(self, deltaX, deltaY, x, y);
      c3moving(self, deltaX, deltaY, x, y);
    };
    c5.type = 'leftBottom';

    // north-east,index:6
    let getC6Delta = (deltaX, deltaY) => {
      let deltaXVal = deltaX;
      let deltaYVal = deltaY;
      if (Math.abs(deltaXVal) > Math.abs(deltaYVal)) {
        deltaYVal = -deltaXVal / proportion;
      } else {
        deltaXVal = -deltaYVal * proportion;
      }
      return {deltaX: deltaXVal, deltaY: deltaYVal};
    };
    let c6 = connector(self,
      (s, c) => c.frame.x - s.x + c.frame.width + s.focusMargin,
      (s, c) => c.frame.y - s.y - s.focusMargin,
      s => DIRECTION.NE,
      s => s.visible && visible(s),
      s => s.resizeable,
      s => false,
      release,
      s => self.page.shiftKeyPressed,
      getC6Delta,
    );
    c6.moving = (deltaX, deltaY, x, y) => {
      c1moving(self, deltaX, deltaY, x, y);
      c2moving(self, deltaX, deltaY, x, y);
    };
    c6.type = 'rightTop';
    // south-east,index:7
    let getC7Delta = (deltaX, deltaY) => {
      let deltaXVal = deltaX;
      let deltaYVal = deltaY;
      if (Math.abs(deltaXVal) > Math.abs(deltaYVal)) {
        deltaYVal = deltaXVal / proportion;
      } else {
        deltaXVal = deltaYVal * proportion;
      }
      return {deltaX: deltaXVal, deltaY: deltaYVal};
    };
    let c7 = connector(self,
      (s, c) => c.frame.x - s.x + c.frame.width + s.focusMargin,
      (s, c) => c.frame.y - s.y + c.frame.height + s.focusMargin,
      s => DIRECTION.SE,
      s => s.visible && visible(s),
      s => s.resizeable,
      s => false,
      release,
      s => self.page.shiftKeyPressed,
      getC7Delta);
    c7.moving = (deltaX, deltaY, x, y) => {
      c1moving(self, deltaX, deltaY, x, y);
      c3moving(self, deltaX, deltaY, x, y);
    };
    c7.type = 'rightBottom';
    // ??
    let c8 = connector(self,
      (s, c) => c.frame.x - s.x + c.frame.width / 2,
      (s, c) => c.frame.y - s.y - 13 - s.focusMargin,
      s => DIRECTION.R,
      s => s.visible && visible(s) && s.getRotateAble() && self.getFrame().width >= CON_MIN,
      s => s.getRotateAble(),
      s => false,
      release,
      s => false);
    c8.moving = (deltaX, deltaY, x, y) => {
      let cx = self.width / 2 + self.x;
      let cy = self.height / 2 + self.y;

      let pos = convertPositionWithParents(self.getContainer(), x, y);
      let x1 = pos.x;
      let y1 = pos.y;

      let deg = (Math.atan2(y1 - cy, x1 - cx) * 180 / Math.PI + 90);
      if (deg > 360) {
        deg -= 360;
      }
      self.rotateDegree = Math.round(deg / 10) * 10;
    };
    c8.type = 'rotate';
    self.rotateConnector = c8;

    self.createUndragableConnector({
      rx: 1 / 4,
      ry: 0,
      key: 'conn-1-0',
      ax: 'y',
      vector: -1,
      direction: DIRECTION.N,
    });
    self.createUndragableConnector({
      rx: 3 / 4,
      ry: 0,
      key: 'conn-3-0',
      ax: 'y',
      vector: -1,
      direction: DIRECTION.N,
    });
    self.createUndragableConnector({rx: 1 / 4, ry: 1, key: 'conn-1-4', ax: 'y', vector: 1, direction: DIRECTION.S});
    self.createUndragableConnector({rx: 3 / 4, ry: 1, key: 'conn-3-4', ax: 'y', vector: 1, direction: DIRECTION.S});
    self.createUndragableConnector({
      rx: 0,
      ry: 1 / 4,
      key: 'conn-0-1',
      ax: 'x',
      vector: -1,
      direction: DIRECTION.W,
    });
    self.createUndragableConnector({
      rx: 0,
      ry: 3 / 4,
      key: 'conn-0-3',
      ax: 'x',
      vector: -1,
      direction: DIRECTION.W,
    });
    self.createUndragableConnector({rx: 1, ry: 1 / 4, key: 'conn-4-1', ax: 'x', vector: 1, direction: DIRECTION.E});
    self.createUndragableConnector({rx: 1, ry: 3 / 4, key: 'conn-4-3', ax: 'x', vector: 1, direction: DIRECTION.E});
  };
  self.createUndragableConnector = ({rx, ry, key, ax, vector, direction}) => {
    let getx;
    let gety;
    if (typeof rx === 'function') {
      getx = rx;
    } else {
      getx = (s, c) => c.frame.x - s.x + c.frame.width * rx;
    }
    if (typeof ry === 'function') {
      gety = ry;
    } else {
      gety = (s, c) => c.frame.y - s.y + c.frame.height * ry;
    }

    const c = connector(self, getx, gety, s => {
      return {cursor: 'crosshair', key, color: 'white', ax, vector, value: direction.value};
    }, s => s.visible, s => s.resizeable, () => true, () => {
    }, s => false);
    c.type = 'connection';
    c.dragable = false;
    return c;
  };

  let resize = self.resize;
  self.resize = (width, height) => {
    if (width < 1 || height < 1) {
      return;
    }
    resize.apply(self, [width, height]);
  };
  self.beginDrag = () => {
    self.mousedownConnector = self.connectors.find(c => c.isType('rightBottom'));
    if (self.mousedownConnector === undefined) {
      self.mousedownConnector = null;
    }
    self.isNew = true;
  };

  let keyPressed = self.keyPressed;
  self.keyPressed = e => {
    const isCtrl = (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey);


    if (e.ctrlKey && e.shiftKey && (e.code === 'F8')) {
      if (self.shared) {
        if (self.sharedBy === self.page.graph.session.id) {
          layoutCommand(self.page, [{shape: self, shared: false, sharedBy: undefined}]).execute();
        } else {
          // 没关系，继续，不影响其他错误信息的处理.
        }
      } else {
        layoutCommand(self.page, [{shape: self, shared: true, sharedBy: self.page.graph.session.id}]).execute();
      }
    }

    if (isCtrl && (e.code === 'KeyE')) {
      layoutCommand(self.page, [{shape: self, emphasized: !self.emphasized}]).execute(self.page);
    }
    if (isCtrl && e.code.indexOf('Digit') >= 0) {
      layoutCommand(self.page, [{shape: self, priority: parseInt(e.code.substr(5, 1))}]).execute(self.page);
    }

    if (isCtrl && e.code === 'KeyI') {
      layoutCommand(self.page, [{
        shape: self,
        infoType: INFO_TYPE[self.infoType.next],
      }]).execute(self.page);
    }
    if (isCtrl && e.code === 'KeyG') {
      layoutCommand(self.page, [{shape: self, showedProgress: !self.showedProgress}]).execute(self.page);
    }
    if (isCtrl && e.code === 'KeyP') {
      e.preventDefault();
      layoutCommand(self.page, [{
        shape: self, progressStatus: PROGRESS_STATUS[self.progressStatus.next],
      }]).execute(self.page);
    }

    if (e.shiftKey && e.code === 'Digit2') {
      self.assign = (id, name) => {
        layoutCommand(self.page, [{shape: self, assignTo: {id, name}}]).execute(self.page);
      };
      self.assign('544938', 'huizi');
    }

    return keyPressed.call(self, e);
  };

  let updateRegionVisibility = () => {
    self.priorityRegion.visible = self.priority > 0;
    let infoType = self.infoType;
    self.infoTypeRegion.visible = infoType === undefined ? false : infoType.name !== INFO_TYPE.NONE.name;
    self.progressRegion.visible = self.showedProgress;
    let progressStatus = self.progressStatus;
    self.progressStatusRegion.visible = progressStatus === undefined ? false : progressStatus.name !== PROGRESS_STATUS.NONE.name;
    const assignTo = self.assignTo;
    self.assignToRegion.visible = assignTo !== undefined;
    (self.assignToRegion.visible) && (self.assignToRegion.text = assignTo.name);
  };

  let invalidate = self.invalidate;
  self.invalidate = () => {
    updateRegionVisibility();
    invalidate.apply(self);
  };

  self.priorityRegion = regions.priorityRegion(self,
    shape => shape.width - STEP + self.regionXoffset,
    () => OFFSET_Y + self.regionYoffset);

  self.infoTypeRegion = regions.infoTypeRegion(self,
    shape => shape.priorityRegion.getx(shape) - (self.priorityRegion.visible ? STEP : 0) + self.regionXoffset,
    () => OFFSET_Y + self.regionYoffset);

  self.progressRegion = regions.progressRegion(self,
    shape => shape.infoTypeRegion.getx(shape) - (self.infoTypeRegion.visible ? STEP : 0) + self.regionXoffset,
    () => OFFSET_Y + self.regionYoffset);

  self.progressStatusRegion = regions.progressStatusRegion(self,
    shape => shape.progressRegion.getx(shape) - (self.progressRegion.visible ? STEP : 0) + self.regionXoffset,
    () => OFFSET_Y + self.regionYoffset);

  self.assignToRegion = regions.assignToRegion(self,
    shape => {
      return shape.progressStatusRegion.getx(shape) -
        (self.progressStatusRegion.visible ? STEP : 0) +
        self.regionXoffset;
    },
    () => OFFSET_Y + self.regionYoffset);
  updateRegionVisibility();
  self.commentRegion = regions.shapeCommentRegion(self, item => 0, item => -5, () => 12, () => 12);
  self.sharedRegion = regions.sharedRegion(self, item => -3, item => self.height - 8, () => 16, () => 16);
  self.sharedButtonRegion = regions.sharedButtonRegion(self, item => 0, item => 5, () => 20, () => 20);

  self.addDetection(['progressStatus', 'emphasized', 'priority', 'infoType',
    'showedProgress'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    self.enableAnimation = self.progressStatus === PROGRESS_STATUS.DOING || self.progressStatus === PROGRESS_STATUS.RUNNING || self.emphasized;
    updateRegionVisibility();
    self.render();
  });
  return self;
};

const text = (id, x, y, width, height, parent, drawer) => {
  let self = rectangle(id, x, y, width, height, parent, drawer ? drawer : rectangleDrawer);
  self.text = '';
  self.borderWidth = 0;
  self.type = 'text';
  self.hAlign = ALIGN.LEFT;
  self.vAlign = ALIGN.TOP;
  self.backColor = 'transparent';
  let back = () => {
    return {
      x: self.x + self.borderWidth,
      y: self.y + self.borderWidth,
      width: self.width - (2 * self.borderWidth),
      height: self.height - (2 * self.borderWidth),
    };
  };
  self.drawer.containsBack = (x, y) => isPointInRect({x, y}, back());
  return self;
};

const shapeComments = () => {
  const MAX = 15;
  let self = {};
  self.pool = [];
  self.freePool = [];
  self.comments = [];
  self.cursor = 0;

  self.clear = () => {
    self.comments = [];
    self.pool = [];
    self.freePool = [];
    self.cursor = 0;
  };

  self.add = (shape, comment, mode) => {
    self.comments.push({
      shape,
      comment,
      mode,
    });
    if (self.freePool.length > 0) {
      self.cursor++;
      const shape = self.freePool.pop();
      shape.visible = true;
      shape.run(mode, comment);
      return;
    }

    if (self.pool.length < MAX) {
      self.cursor++;
      const com = shapeComment(shape.x, shape.y, shape);
      self.pool.push(com);
      com.stopped = () => {
        if (self.cursor < self.comments.length) {
          let c = self.comments[self.cursor];
          com.x = c.shape.x;
          com.y = c.shape.y;
          com.isInited = false;
          com.run(mode, c.comment);
          self.cursor++;
        } else {
          com.visible = false;
          com.isInited = false;
          self.freePool.push(com);
        }
      };
      com.run(mode, comment);
    }
  };
  return self;
};

/**
 * 对形状编写评论
 * 辉子 2021
 */
const shapeComment = (context => {
  return (x, y, host, id) => {
    if (context.page !== host.page.id) {
      context.bullet = [];
      context.pop = [];
      context.page = host.page.id;
    }
    const COMMENT_HEIGHT = 25;
    const FONT_SIZE = 12;
    const self = rectangle(id, x, y, 200, 25, host.page, shapeCommentDrawer);
    self.type = 'shapeComment';
    self.serializable = false;
    self.cornerRadius = 5; // / host.page.scaleY;
    self.minWidth = 100; // / host.page.scaleX;
    self.height = COMMENT_HEIGHT;// / host.page.scaleY;
    self.pad = 0;
    self.margin = 5;
    self.borderColor = 'gray';
    self.backColor = 'whitesmoke';
    self.fontColor = 'dimgray';
    self.fontWeight = FONT_WEIGHT.LIGHTER;
    self.hAlign = ALIGN.LEFT;
    self.text = '';
    self.container = host.page.id;
    self.host = host.id;
    self.fontSize = FONT_SIZE * getPixelRatio();// / host.page.scaleY;
    self.connectors = [];
    self.editedRemove = true;
    self.isDisplay = false;

    let triggerSource = '';
    self.editEnterPressed = async () => {
      await sleep(10);
      triggerSource = 'keyEnter';
      self.endEdit();
    };

    self.bullet = {
      init: () => {
        (!context.lastTime) && (context.lastTime = new Date().getTime());
        const now = new Date().getTime();
        const interval = now - context.lastTime;
        context.lastTime = now;
        const delay = interval < 500 ? 300 : 0;
        const container = self.getContainer();
        const colors = ['steelblue', 'darkorange', 'OLIVE', 'teal', 'darkred', 'green', '#EAC117', 'gray', 'darkgreen', '#CD7F32'];
        self.x = Math.ceil(50 * Math.random()) - self.page.x - delay;
        self.y = Math.ceil(container.y + 150 * Math.random()) - self.page.y;
        self.fontSize = 15 * getPixelRatio();
        self.height = COMMENT_HEIGHT * getPixelRatio();
        self.borderColor = self.fontColor = colors[Math.floor(Math.random() * colors.length)];
        self.backColor = 'rgba(0,0,0,0.03)';
        self.invalidate();
        self.bullet.x = self.x - self.page.x;
        self.bullet.end = container.x + container.width * getPixelRatio() - self.page.x;
      },
      run: () => {
        self.bullet.x += 3;
        self.drawer.parent.style.left = `${self.bullet.x}px`;
        if (self.bullet.x > self.bullet.end) {
          self.animate = undefined;
          self.stop('bullet');
        }
      },
    };

    self.pop = {
      init: () => {
        self.x = host.x;
        self.y = host.y + context.pop.sum(c => (c.height + 2));
        self.borderColor = 'lightrgray';
        self.backColor = 'lightyellow';
        self.fontColor = 'gray';
        self.invalidate();
        self.pop.alpha = 1;
      },
      run: () => {
        const top = host.y +
          (self.page.sm.getShapes(s => s.loginTag && s.loginTag < self.loginTag).length * (self.height + 2));
        self.drawer.parent.style.top = `${top}px`;
        self.drawer.parent.style.opacity = self.pop.alpha;
        self.pop.alpha -= 0.002;
        if (self.pop.alpha <= 0) {
          self.animate = undefined;
          self.stop('pop');
        }
      },
    };
    self.login = () => {
      self.loginTag = undefined;
      const all = self.page.sm.getShapes(s => s.loginTag);
      if (all.length === 0) {
        self.loginTag = 1;
      } else {
        self.loginTag = all.max(s => s.loginTag) + 1;
      }
    };
    self.logout = () => self.loginTag = undefined;
    /**
     * bullet or pop
     */
    self.run = (mode, comment) => {
      self.invalidate();
      self.login();
      self.page.sm.moveTop([self]);
      self.animate = () => {
        if (!self.isInited) {
          self.isInited = true;
          self.text = comment;
          self.autoWidth = true;
          self.minWidth = undefined;
          self.isDisplay = true;
          self[mode].init();
        }
        self[mode].run();
      };
    };

    self.stopped = () => {
      self.remove();
    };

    self.stop = mode => {
      self.logout();
      self.stopped();
    };

    /**
     * 评论写完后通知演示方，并且自身消失
     */
    self.edited = () => {
      if (triggerSource !== 'keyEnter') {
        return;
      }
      const textString = self.drawer.getEditor().getTextString();
      if (typeof textString === 'string' && textString.trim() === '') {
        return;
      }
      // 发送评论
      self.page.graph.collaboration.invoke({
        method: 'publish_comment',
        mode: self.page.mode,
        page: self.page.id,
        shape: host.id,
        value: textString,
      });

      self.page.ignoreReact(() => {
        self.unSelect();
      });
      if (self.editedRemove) {
        self.remove();
      } else {
        self.text = '';
        self.invalidate();
      }
      triggerSource = '';
    };

    let get = self.get;
    self.get = field => {
      if (field === 'editable') {
        return !self.isDisplay;
      }
      return get.call(self, field);
    };

    return self;
  };
})({bullet: [], pop: []});

const shapeCommentDrawer = (shape, div, x, y) => {
  const drawer = rectangleDrawer(shape, div, x, y);

  /**
   * 重写initialize方法.
   */
  drawer.initialize = () => {
    const editor = drawer.getEditor();
    editor.addForbiddenCommand('insertOrderedList');
    editor.addForbiddenCommand('insertUnorderedList');
  };

  /**
   * 重写renderText.
   * 1、一般来说，comment都是可编辑的.
   * 2、一般来说，comment应该一创建出来，就自动focus，方便编辑.
   */
  const renderText = drawer.renderText;
  drawer.renderText = () => {
    if (shape.type === 'shapeComment') {
      drawer.text.innerHTML = '';
      const span = document.createElement('span');
      span.style.color = shape.fontColor;
      span.style.display = 'inline-block';
      span.innerText = shape.text;
      drawer.text.append(span);
    } else {
      renderText.apply(drawer, [shape.isFocused]);
    }
  };
  return drawer;
};

const codeDisplayer = (id, x, y, width, height, parent) => {
  const self = text(id, x, y, width, height, parent);
  self.tag = 'page.pros';
  self.type = 'codeDisplayer';
  self.enableAnimation = true;
  self.fontSize = 30;
  self.fontFace = 'impact';
  self.fontWeight = FONT_WEIGHT.LIGHTER;
  self.backColor = 'rgba(255,255,255,0.1)';
  self.borderColor = 'steelBlue';
  self.width = self.height = 60;
  self.text = '';
  self.hidText = true;

  let step = -0.05;
  self.drawer.drawDynamic = (context, x, y) => {
    const code = `(page=> ${self.tag})(self.page)`;
    try {
      const text = eval(code).toString();
      context.font = `normal ${self.fontWeight} ${self.fontSize}px ${self.fontFace}`;
      const width = context.measureText(text).width;
      const g = context.createLinearGradient(-40 - width / 2, -self.fontSize * 2, 40 + width / 2, self.fontSize * 2);
      g.addColorStop(0, self.getBorderColor());
      g.addColorStop(step < 0 ? 0 : step, self.getBorderColor());
      g.addColorStop(0.05 + step, 'rgba(255,255,255,1)');
      g.addColorStop(((0.1 + step) > 1 ? 1 : (0.1 + step)), self.getBorderColor());
      g.addColorStop(1, self.getBorderColor());

      context.fillStyle = g;
      context.fillText(text, -width / 2, 10);
      step += 0.002;
      if (step > 0.9) {
        step = -0.05;
      }
    } catch (e) {
      // 没关系，继续，不影响其他错误信息的处理.
    }
  };
  self.serializedFields.delete('enableAnimation');
  return self;
};

export {rectangle, text, shapeComments, shapeComment, codeDisplayer};