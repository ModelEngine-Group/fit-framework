/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, DOCK_MODE, FONT_WEIGHT} from '../common/const.js';

import {container} from './container.js';
import {hitRegion} from './hitRegion.js';
import {simpleContainerDrawer} from './drawers/containerDrawer.js';

const ROW_HEIGHT = 40;
/**
 * 通用table
 * 目标是达到excel的操作体验
 * 辉子 2021(废弃)
 */
const tableStatic = (id, x, y, width, height, parent) => {
  let self = container(id, x, y, width, height, parent);// , canvasContainerDrawer);
  self.type = 'tableStatic';
  self.dockMode = DOCK_MODE.HORIZONTAL;
  self.dockAlign = ALIGN.LEFT;
  self.showHead = true;
  self.showTitle = true;
  self.showSequence = true;
  self.itemSpace = -1;
  self.width = 750;
  self.height = 200;
  self.hideText = false;
  self.backAlpha = 1;
  self.pad = 2;
  self.text = 'table';
  self.ifMaskItems = true;
  self.adaptWidth = true;
  self.adaptHeight = false;
  self.scrollAble = true;
  self.childAllowed = child => child.isType('column') || child.isType('seqColumn');
  self.lightCellColor = 'rgba(255,255,255,0)';
  self.heavyCellColor = 'RGBA(255,255,255,0.2)';

  self.selectedRow = -1;
  self.selectedColumn = -1;
  self.showLock = true;
  self.showSort = true;
  self.rowHeight = 45;
  self.headHeight = 45;
  self.itemPad = [0, 0, 30, 0];

  self.fontColor = 'whitesmoke';
  self.fontWeight = FONT_WEIGHT.LIGHTER;
  self.fontSize = 15;
  self.fontFace = 'arial';

  self.getOrderedShapes = () => {
    let shapes = self.getShapes().orderBy(i => i.tIndex);
    if (shapes.length > 1) {
      for (let i = 1; i < shapes.length; i++) {
        self.page.sm.updateShapes(writer => writer.moveShapeTo(shapes[i], shapes[i - 1].getIndex()));
      }
    }
    return shapes;
  };
  let scroll = self.scroll;
  self.scroll = (deltaX, deltaY) => {
    if (!self.scrollAble) {
      return;
    }
    if (self.itemScroll.x + deltaX > 0) {
      return;
    }
    if (-(self.itemScroll.x + deltaX) > (self.getColumns().max(col => col.x + col.width) - self.x - self.width)) {
      return;
    }
    scroll.apply(self, [deltaX, deltaY]);

    let seqCol = self.getSeqColumn();
    let leftBase = seqCol.x + seqCol.width;
    let locked = self.getColumns().filter(c => c.lock);
    let unlocked = self.getColumns().filter(c => !c.lock);
    if (locked.length > 0) {
      leftBase = locked.max(c => c.x + c.width);
    }
    let left = unlocked.min(c => c.x);
    if (left > leftBase) {
      self.itemScroll.x -= left - leftBase + 1;
      self.invalidate();
    }
  };

  self.getColumns = () => self.getShapes().filter(col => col.isType('column')).orderBy(i => i.getIndex());
  self.getSeqColumn = () => self.getShapes().find(col => col.isType('seqColumn'));

  self.getRow = index => {
    const result = [];
    self.getColumns().forEach(c => {
      result.push(c.getShapes().find(s => s.tIndex === index));
    });

    return result;
  };
  self.initialize = (data) => {
    self.getShapes().forEach(s => s.remove());

    let rows = (data && data.length > 0) ? data.length : 5;
    let columns = (data && data.length > 0) ? data[0].length : 5;
    let seq = self.page.createNew('seqColumn', self.x + 1, self.y + 1);
    seq.container = self.id;
    seq.visible = self.showSequence;
    seq.tIndex = 0;
    for (let j = 0; j < rows; j++) {
      let c = self.page.createNew('seqCell', seq.x + 1, seq.y + 1);
      c.container = seq.id;
      c.height = self.rowHeight;
      c.tIndex = j;
    }
    let col = null;
    for (let i = 0; i < columns; i++) {
      col = self.page.createNew('column', self.x + 1, self.y + 1);
      col.container = self.id;
      col.text = `column ${self.getColumns().length}`;
      col.tIndex = i + 1;
      for (let j = 0; j < rows; j++) {
        let c = self.page.createNew('cell', col.x + 1, col.y + 1);
        c.container = col.id;
        c.height = self.rowHeight;
        c.text = (data && data.length > 0) ? data[j][i] : `cell ${j + 1}`;
        c.tIndex = j;
      }
    }
    self.page.sm.updateShapes(writer => writer.moveShapeTo(seq, col.getIndex()));
    self.invalidate();
  };

  self.newColumn = () => {
    let cols = self.getColumns();
    cols[cols.length - 1].newColumn();
  };

  const arrangeShapes = self.arrangeShapes;
  self.arrangeShapes = () => {
    arrangeShapes.call(self);
    self.arrange();
  };
  self.arrange = () => {
    let columns = self.getShapes().filter(s => s.getVisibility());
    if (self.adaptWidth) {
      self.width = columns.sum(s => s.width - 1) + self.getPadLeft() + self.getPadRight();
    }
    if (self.adaptHeight) {
      self.height = columns.max(col => col.getShapes().sum(c => c.height) + col.itemPad[2]) + self.itemPad[2] + self.pad;
    }
  };
  self.sort = (column, direction) => {
    let cells = column.getShapes();
    let sorted = cells[direction === 'up' ? 'orderByDesc' : 'orderBy']('text');
    let all = self.page.sm.getShapes(s => s.getTable && s.getTable() === self && s.getTable() !== s.getContainer());
    sorted.forEach((c, i) => {
      all.filter(s => s.tIndex === c.tIndex).forEach(s => {
        s.tIndex = i;
        all.remove(d => d === s);
      });
    });
    self.getSeqColumn().getShapes().forEach(s => s.lock = false);
    self.invalidate();
  };

  self.export = () => {
  };

  let keyPressed = self.keyPressed;
  self.keyPressed = e => {
    keyPressed.apply(self, [e]);
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && e.code === 'KeyN') {
      self.newColumn();
    }
  };

  const invalidate = self.invalidate;
  self.invalidate = () => {
    self.hideText = !self.showTitle;
    self.itemPad[2] = self.showTitle ? self.headHeight : self.pad;
    invalidate.call(self);
  };

  const drawDynamic = self.drawer.drawDynamic;
  self.drawer.drawDynamic = (context, x, y) => {
    if (self.decorate) {
      self.decorate(self, context);
    } else {
      drawDynamic.call(self.drawer, context, x, y);
    }
  };

  self.addDetection(['showTitle', 'showHead', 'showSequence'], (property, value, preValue) => {
    self.drawer.transform();
    self.getShapes().find(s => s.isType('seqColumn')).visible = self.showSequence;
    self.invalidate();
  });
  //----------------------------------------------------------------------------------

  return self;
};

const column = (id, x, y, width, height, parent) => {
  let self = container(id, x, y, width, height, parent);
  self.type = 'column';
  self.width = 150;
  self.text = 'column';
  self.dockMode = DOCK_MODE.VERTICAL;
  self.headHeight = 40;
  self.itemPad = [0, 0, 60, 0];
  self.itemSpace = -1;
  self.hideText = false;
  self.pad = 0;
  self.padTop = 2;
  self.fontColor = 'white';
  self.borderWidth = 1;
  self.cornerRadius = 0;
  self.rotateAble = false;
  self.ifMaskItems = true;
  self.moveable = false;
  self.headColor = 'steelblue';
  self.scrollLock = {x: false, y: true};
  self.scrollAble = true;
  self.regionYoffset = 7;
  self.backColor = 'transparent';
  self.padTop = 8;
  // ----------serialized
  self.isSeq = false;

  self.childAllowed = child => child.isType('cell');

  self.getTable = () => self.getContainer();
  self.onMouseDrag = position => {
    if (self.mousedownConnector !== null) {// move connector
      self.page.cursor = self.mousedownConnector.direction.cursor;
      self.mousedownConnector.onMouseDrag(position);
    } else {
      self.getContainer().scroll(position.deltaX, 0);
    }
  };

  let scroll = self.scroll;
  self.scroll = (deltaX, deltaY) => {
    if (!self.scrollAble) {
      return;
    }
    if (self.itemScroll.y + deltaY > 0) {
      return;
    }
    if (-(self.itemScroll.y + deltaY) > (self.getShapes().max(c => c.y + c.height) - self.getTable().y - self.getTable().height)) {
      return;
    }
    scroll.apply(self, [deltaX, deltaY]);

    let topBase = self.getShapesArea().y;
    let locked = self.getShapes().filter(c => c.scrollLock.y);
    let unlocked = self.getShapes().filter(c => !c.scrollLock.y);
    if (locked.length > 0) {
      topBase = locked.max(c => c.y + c.height - 1);
    }
    let top = unlocked.min(c => c.y);
    if (top > topBase) {
      self.itemScroll.y -= top - topBase;
      self.invalidate();
    }
  };

  self.beforeRemove = source => {
    if (source) {
      let container = self.getContainer();
      if (container.getColumns === undefined) {
        return true;
      } else {
        return self.getContainer().getColumns().length > 1;
      }
    } else {
      return true;
    }
  };
  self.afterRemoved = source => {
    if (!source) {
      return;
    }
  };

  self.getOrderedShapes = () => {
    let shapes = self.getShapes().orderBy(i => i.tIndex);
    if (shapes.length > 1) {
      for (let i = 1; i < shapes.length; i++) {
        self.page.sm.updateShapes(writer => writer.moveShapeTo(shapes[i], shapes[i - 1].getIndex()));
      }
    }
    return shapes;
  };

  const get = self.get;
  self.get = field => {
    if (field === 'headHeight') {
      let result = self.headHeight;
      if (result === undefined) {
        result = self.getTable().headHeight;
      }
      return result;
    } else {
      return get.call(self, field);
    }
  };

  self.newColumn = isClone => {
    let parent = self.getContainer();
    let col = self.page.createNew('column', self.x + 1, self.y + 1);
    col.container = self.container;
    col.width = self.width;
    col.text = `column ${parent.getColumns().length}`;
    self.getShapes().forEach(c => {
      let c1 = self.page.createNew('cell', col.x + 1, col.y + 1);
      c1.width = c.width;
      c1.tIndex = c.tIndex;
      c1.text = isClone ? c.text : '';
      if (isClone) {
        c1.height = c.height;
      }
      c1.container = col.id;
    });
    let columns = parent.getColumns();
    self.page.sm.updateShapes(writer => writer.moveShapeTo(parent.getSeqColumn(), columns.max(c => c.getIndex())));
    columns.filter(c => c.tIndex > self.tIndex).forEach(c => c.tIndex++);
    col.tIndex = self.tIndex + 1;

    self.getContainer().invalidate();
  };

  const invalidate = self.invalidate;
  self.invalidate = () => {
    const t = self.getTable();
    self.hideText = !t.showHead;
    self.itemPad[2] = t.showHead ? self.headHeight : 0;
    invalidate.call(self);
  };

  let keyPressed = self.keyPressed;
  self.keyPressed = e => {
    keyPressed.apply(self, [e]);
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && (e.key.indexOf('Left') >= 0)) {
      let columns = self.getContainer().getColumns();
      let idx = columns.indexOf(self);
      if (idx <= 0) {
        return;
      }
      self.page.sm.updateShapes(writer => writer.moveShapeTo(self, columns[idx - 1].getIndex()));
      self.getContainer().invalidate();
    }
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && (e.key.indexOf('Right') >= 0)) {
      let columns = self.getContainer().getColumns();
      let idx = columns.indexOf(self);
      if (idx >= columns.length - 1) {
        return;
      }
      self.page.sm.updateShapes(writer => writer.moveShapeTo(self, columns[idx + 1].getIndex()));
      self.getContainer().invalidate();
    }
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && e.code === 'KeyN') {
      self.newColumn();
    }
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && e.code === 'KeyD') {
      self.newColumn(true);
    }
  };
  self.sortRegion = sortRegion(self, shape => shape.width - 16, shape => 5);
  self.lockRegion = lockRegion(self, shape => 2, shape => 2, () => 12, () => 12);

  self.addDetection(['lock'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }

    let cols = self.getContainer().getColumns();
    self.scrollLock.x = value;

    let target = cols.find(c => c.tIndex === (self.tIndex + (value ? -1 : 1)));
    if (target !== undefined) {
      target.lock = value;
    }
    self.lockRegion.draw();
  });
  //----------------------------------------------------------------------------------

  return self;
};

const seqColumn = (id, x, y, width, height, parent) => {
  let self = column(id, x, y, width, height, parent);
  self.type = 'seqColumn';
  self.width = 36;
  self.text = '';
  self.editable = false;
  self.scrollLock = {x: true, y: true};
  self.keyPressed = e => {
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && e.code === 'KeyN') {
      self.newColumn();
    }
  };
  self.initConnectors = () => {
    self.connectors = [];
  };
  self.childAllowed = child => child.isType('seqCell');
  self.regions = [];

  const getVisibility = self.getVisibility;
  self.getVisibility = () => getVisibility.call(self) && self.getTable().showSequence;
  return self;
};

const cell = (id, x, y, width, height, parent) => {
  let EMPTY_COLOR = '--';
  let self = container(id, x, y, width, height, parent, simpleContainerDrawer);
  self.hideText = false;
  self.type = 'cell';
  self.height = ROW_HEIGHT;
  self.moveable = false;
  self.deletable = false;
  self.text = 'cell';
  self.cornerRadius = 0;
  self.borderColor = 'gray';
  self.regionYoffset = 7;
  self.flag = EMPTY_COLOR;
  self.stars = 0;
  self.framed = EMPTY_COLOR;
  self.containerAllowed = parent => parent.isType('column');
  self.scrollLock = {x: true, y: false};
  self.emphasizedOffset = 3;
  self.vAlign = ALIGN.MIDDLE;

  self.getTable = () => self.getContainer().getContainer();

  self.onMouseDrag = position => {
    let t = self.getTable();// .getContainer().getContainer();
    if (Math.abs(position.deltaX) < Math.abs(position.deltaY)) {
      // move vertical
      t.getColumns().forEach(col => col.scroll(0, position.deltaY));
      t.getSeqColumn().scroll(0, position.deltaY);
    } else {
      // move horizontal
      t.scroll(position.deltaX, 0);
    }
  };

  self.initConnectors = () => {
    self.connectors = [];
  };
  self.drawer.drawConnectors = (context) => {
  };

  let colors = {
    '--': 'green', green: 'orange', orange: 'red', red: 'steelblue', steelblue: 'gray', gray: '--',
  };
  let keyPressed = self.keyPressed;
  self.keyPressed = e => {
    keyPressed.apply(self, [e]);

    if (!e.metaKey && e.code === 'KeyF') {
      self.flag = colors[self.flag];
      self.invalidate(true);
    }
    if (!e.metaKey && e.code === 'KeyS') {
      self.stars++;
      if (self.stars === 6) {
        self.stars = 0;
      }
      self.invalidate(true);
    }
  };

  const get = self.get;
  self.get = field => {
    if (field === 'backColor') {
      const t = self.getTable();
      let color;
      if (self.backColor !== undefined) {
        color = self.backColor;
      } else {
        if ((self.tIndex % 2) === 0) {
          color = t.lightCellColor;
        } else {
          color = t.heavyCellColor;
        }
      }
      return self.tIndex === self.getTable().selectedRow ? 'wheat' : color;
    }

    const isFontField = field === 'fontSize' ||
      field === 'fontColor' ||
      field === 'fontWeight' ||
      field === 'fontFace';
    const isPadField = field === 'padTop' ||
      field === 'padBottom' ||
      field === 'padLeft' ||
      field === 'padRight';
    if (isFontField || isPadField) {
      let result = self[field];
      if (result === undefined) {
        result = self.getContainer()[`cell${field.capitalize()}`];
      }
      if (result === undefined) {
        result = self.getTable().get(field);
      }
      return result;
    }
    if (field === 'borderWidth') {
      return self.getContainer().get('borderWidth');
    }
    return get.call(self, field);
  };

  let invalidate = self.invalidate;
  self.invalidate = () => {
    const getCellBackColor = () => {
    };
    self.flagRegion.visible = self.flag !== EMPTY_COLOR;
    self.starRegion.visible = self.stars > 0;
    invalidate.apply(self);
  };

  let timer = 0;

  let drawDynamic = self.drawer.drawDynamic;
  self.drawer.drawDynamic = (context, x, y) => {
    drawDynamic.apply(self.drawer, [context, x, y]);
    if (self.framed === EMPTY_COLOR) {
      return;
    }
    let offset = 1;
    let x0 = x - self.width / 2;
    let y0 = y - self.height / 2;
    const lowCondition = timer >= 15 && timer <= 20;
    const highCondition = timer >= 30 && timer <= 35;
    if (lowCondition || highCondition) {
      offset = 3;
    }
    context.strokeStyle = self.framed;
    context.lineWidth = 1;
    context.beginPath();
    context.rect(x0 + offset, y0 + offset, self.width - 2 * offset, self.height - 2 * offset);
    context.stroke();
    timer++;
    if (timer > 90) {
      timer = 0;
    }
  };

  self.flagRegion = flagRegion(self, shape => shape.hAlign === ALIGN.LEFT ? shape.width - 25 : 0, () => 1);
  self.starRegion = starRegion(self, () => 0, shape => shape.height - 13);

  self.edited = () => {
    let container = self.getContainer().getContainer();
    let cells = container.getShapes().map(col => col.getShapes().find(c => Math.abs(c.y - self.y) < 2));
    let maxHeight = cells.max(c => c.getTextSize().height);
    maxHeight = maxHeight < ROW_HEIGHT ? ROW_HEIGHT : maxHeight;
    cells.forEach(c => c.height = maxHeight);
    container.invalidate();
  };

  return self;
};

const seqCell = (id, x, y, width, height, parent) => {
  let self = cell(id, x, y, width, height, parent);
  self.type = 'seqCell';
  self.hAlign = ALIGN.RIGHT;
  self.deletable = true;
  self.containerAllowed = parent => parent.isType('seqColumn');

  let invalidate = self.invalidate;
  self.invalidate = () => {
    self.text = self.tIndex + 1;
    invalidate.apply(self);
  };
  self.flagRegion = flagRegion(self, shape => shape.hAlign === ALIGN.LEFT ? shape.width - 25 : 0, () => 1, () => 16, () => 16);
  self.regions = [self.flagRegion];

  let dIdx = -1;
  self.beforeRemove = () => {
    dIdx = self.getContainer().getShapes().indexOf(self);
    return true;
  };
  self.afterRemoved = source => {
    if (!source) {
      return;
    }
    let root = self.getContainer().getContainer();
    if (root.getColumns === undefined) {
      return;
    }
    root.getColumns().forEach(col => {
      let c = col.getShapes()[dIdx];
      c.deletable = true;
      c.remove();
    });
    root.invalidate();
  };
  self.newRow = isClone => {
    let idx = self.getContainer().getShapes().indexOf(self);
    let root = self.getContainer().getContainer();
    const headCol = root.getSeqColumn();
    let cHead = self.page.createNew('seqCell', headCol.x + 1, headCol.y + 1);
    cHead.container = headCol.id;
    root.getColumns().forEach(col => {
      let c = self.page.createNew('cell', col.x + 1, col.y + 1);
      c.container = col.id;
      let pre = col.getShapes()[idx];
      c.text = isClone ? pre.text : '';
      if (isClone) {
        c.height = pre.height;
      }
      self.page.sm.updateShapes(writer => writer.moveShapeTo(c, pre.getIndex()));
    });
    root.invalidate();
    return idx;
  };
  let keyPressed = self.keyPressed;
  self.keyPressed = e => {
    keyPressed.apply(self, [e]);
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && e.code === 'KeyN') {
      self.newRow();
    }
    if ((self.page.ctrlKeyPressed || self.page.shiftKeyPressed) && e.code === 'KeyD') {
      self.newRow(true);
    }
  };

  self.lockRegion = lockRegion(self, shape => 2, shape => shape.height - 13, () => 12, () => 12);

  // --------------------------serialization & detection------------------------------
  self.addDetection(['isFocused'], (property, value, preValue) => {
    let table = self.getTable();
    if (value) {
      table.selectedRow = self.tIndex;
    } else {
      table.selectedRow = -1;
    }
    table.invalidate();
  });

  self.addDetection(['lock'], (property, value, preValue) => {
    if (value === preValue) {
      return;
    }
    let parent = self.getContainer();
    let cells = parent.getShapes();

    self.lockRegion.draw();
    let target = cells.find(c => c.tIndex === (self.tIndex + (value ? -1 : 1)));
    if (target !== undefined) {
      target.lock = value;
    }

    self.scrollLock.y = value;

    parent.getContainer().getColumns().forEach(col => {
      let cell = col.getShapes().find(c => c.tIndex === self.tIndex);
      cell.scrollLock.y = value;

    });
  });
  //------------------------------------------------------------------------------------
  return self;
};

const lockRegion = (shape, getx, gety, getWidth, getHeight, index) => {
  let self = hitRegion(shape, getx, gety, getWidth, getHeight, index);
  self.type = 'lock';
  shape.lock = false;

  self.getVisibility = () => shape.getTable().showLock;
  self.drawStatic = (context, x, y) => {
    if (!shape.getTable().showLock) {
      return;
    }
    let dx = 5;
    let dy = 7;
    let r = 4;
    let l1 = 3;
    let l2 = 14;
    let l3 = 10;
    context.strokeStyle = 'silver';
    context.lineWidth = 2;
    context.beginPath();
    context.moveTo(dx, dy);
    context.lineTo(dx, dy - l1);
    context.arc(dx + r, dy - l1, r, Math.PI, 0);
    context.lineTo(dx + 2 * r, dy);
    context.stroke();

    context.beginPath();
    context.rect(dx - l1, dy, l2, l3);
    context.fillStyle = (shape.lock) ? 'orange' : 'silver';
    context.fill();

  };
  self.click = () => shape.getTable().showLock && (shape.lock = !shape.lock);
  return self;
};

const flagRegion = (shape, getx, gety, getWidth, getHeight, index) => {
  let self = hitRegion(shape, getx, gety, getWidth, getHeight, index);
  self.visible = false;
  self.type = 'flag';
  self.drawStatic = (context, x, y) => {
    let dx = 10;
    let dy = 5;
    let w = 4;
    context.fillStyle = shape.flag;
    context.beginPath();
    context.moveTo(x + dx, y + dy);
    context.lineTo(x + dx + 3 * w, y + dy + w);
    context.lineTo(x + dx, y + dy + 2 * w);
    context.closePath();
    context.fill();

    context.beginPath();
    context.moveTo(x + dx - 2, y + dy);
    context.lineTo(x + dx - 2, y + dy + 3 * w);
    context.strokeStyle = 'gray';
    context.stroke();
    context.beginPath();
    context.moveTo(x + dx - 1, y + dy);
    context.lineTo(x + dx - 1, y + dy + 3 * w);
    context.strokeStyle = 'silver';
    context.stroke();

  };

  return self;
};

const starRegion = (shape, getx, gety, index) => {
  let self = hitRegion(shape, getx, gety, index);
  self.type = 'star';
  self.width = 100;
  self.height = 30;
  let drawStar = (ctx, x, y, points, radius1, radius2, alpha0) => {
    let pointsVal = points;
    let i;
    let angle;
    let radius;
    if (radius2 !== radius1) {
      pointsVal = 2 * pointsVal;
    }
    ctx.beginPath();
    for (i = 0; i <= pointsVal; i++) {
      angle = (i * 2 * Math.PI / pointsVal) - (Math.PI / 2) + alpha0;
      radius = i % 2 === 0 ? radius1 : radius2;
      ctx.lineTo(x + (radius * Math.cos(angle)), y + (radius * Math.sin(angle)));
    }
    angle = (2 * Math.PI / pointsVal) - (Math.PI / 2) + alpha0;
    radius = radius2;
    ctx.closePath();
    ctx.fill();
  };
  self.drawStatic = (context, x, y) => {
    if (shape.stars <= 0) {
      return;
    }
    let r1 = 5;
    let r2 = r1 / 2.8;
    let colors = ['LIGHTSALMON', 'orange', 'darkorange', 'SALMON', 'red'];
    context.strokeStyle = 'whitesmoke';
    for (let i = 0; i < shape.stars; i++) {
      context.fillStyle = colors[shape.stars - 1];
      drawStar(context, x + 8 + (i * 12), y + 6, 5, r1, r2, 0);
    }
  };
  return self;
};

const sortRegion = (shape, getx, gety, index) => {
  const NONE = '--';
  const UP = 'up';
  const DOWN = 'down';
  let SORTS = {
    '--': 'up', up: 'down', down: 'up',
  };
  let self = hitRegion(shape, getx, gety, index);
  self.type = 'sort';
  self.style = NONE;

  self.getVisibility = () => shape.getTable().showSort;
  self.drawStatic = (context, x, y) => {
    if (!shape.getTable().showSort) {
      return;
    }
    context.strokeStyle = self.style === UP ? 'green' : 'silver';
    context.beginPath();
    context.moveTo(x, y + 4);
    context.lineTo(x + 3, y);
    context.lineTo(x + 3, y + 10);
    context.stroke();

    context.strokeStyle = self.style === DOWN ? 'green' : 'silver';
    context.beginPath();
    context.moveTo(x + 7, y);
    context.lineTo(x + 7, y + 10);
    context.lineTo(x + 10, y + 6);
    context.stroke();
  };

  self.click = () => {
    if (!shape.getTable().showSort) {
      return;
    }
    shape.getContainer().getColumns().forEach(col => {
      if (col !== shape) {
        col.sortRegion.style = NONE;
      }
    });
    self.style = SORTS[self.style];
    shape.getContainer().sort(shape, self.style);
  };

  return self;
};

export {tableStatic, column, seqColumn, cell, seqCell};
