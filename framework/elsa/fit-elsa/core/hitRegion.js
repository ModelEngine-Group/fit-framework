/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {isPointInRect, pixelRateAdapter, sleep, uuid} from '../common/util.js';
import {INFO_TYPE, PAGE_MODE, PROGRESS_STATUS} from '../common/const.js';
import Popup from '../common/component/popup.js';
import {layoutCommand} from './commands.js';

/**
 * shape上面的热区，这些热区可以自己的绘制，有自己的点击事件
 * 辉子 2020-02-25
 */
const hitRegion = (shape, getx, gety, getWidth = (s, r) => r.width, getHeight = (s, r) => r.height, index) => {
  let self = {};
  self.dragable = true;
  self.selectable = true;
  self.getSelectable = () => self.selectable;
  self.context = null;
  self.width = self.height = 24;

  self.getx = getx;
  self.gety = gety;
  self.getWidth = getWidth;
  self.getHeight = getHeight;

  self.isType = (type) => {
    self.type === type;
  };
  self.getBound = () => {
    return {
      x: self.getx(shape, self) + shape.x, y: self.gety(shape, self) + shape.y, width: self.getWidth(shape, self),
      height: self.getHeight(shape, self),
    };
  };
  self.isMouseIn = (x, y) => isPointInRect({x, y}, self.getBound()) && self.visible;

  self.getId = () => `region-${self.type}:${self.id}`;

  self.image = null;
  self.drawStatic = (context, x, y, width, height) => {
    if (self.image) {
      context.drawImage(self.image, x, y);
      return;
    }

    if (self.src && self.src !== '') {
      const img = new Image();
      img.src = self.src;
      img.onload = () => {
        self.image = img;
        context.drawImage(self.image, x, y);
      };
    }
  };

  /**
   * 设置src，同时清除image对象.
   *
   * @param src 源.
   */
  self.setSrc = (src) => {
    self.src = src;
    self.image = null;
  };

  self.drawDynamic = (context, x, y, width, height) => {
    context.clearRect(0, 0, width / self.pixelRate.ratioX, height / self.pixelRate.ratioY);
    self.drawStatic(context, x, y, width, height);
  };

  self.draw = (x = 0, y = 0) => {
    self.x = self.getx(shape, self);
    self.y = self.gety(shape, self);

    const width = self.getWidth(shape, self);
    const height = self.getHeight(shape, self);
    self.context.canvas.style.left = `${self.x - shape.borderWidth}px`;
    self.context.canvas.style.top = `${self.y - shape.borderWidth}px`;
    self.context.canvas.width = width;
    self.context.canvas.height = height;
    self.context.canvas.style.visibility = self.getVisibility() ? 'visible' : 'hidden';
    self.context.clearRect(0, 0, self.context.canvas.width, self.context.canvas.height);
    self.pixelRate = pixelRateAdapter(self.context, shape.page.scaleX, shape.page.scaleY, shape.page.graph.ignoreHighQuality);

    self.context.restore();
    self.context.save();
    self.context.scale(width / self.width, height / self.height);
    self.drawStatic(self.context, x, y, self.width, self.height);
    if (self.drawDynamic) {
      self.drawDynamic(self.context, x, y, self.width, self.height);
    }
    self.beginX = x;
    self.beginY = y;
  };

  self.shape = shape;
  self.click = () => shape.selectedRegion = self;
  self.dbClick = () => {
  };
  self.cursor = 'hand';
  self.id = uuid();
  self.type = 'hitRegion';
  self.tag = '';
  self.editable = false;
  self.visible = true;
  self.getVisibility = () => self.visible && shape.getVisibility();
  self.getEditRect = () => ({x: self.x, y: self.y, width: self.width, height: self.height});
  self.text = '';
  self.endEdit = text => {
    self.text = text;
    self.draw(self.beginX, self.beginY);
  };
  self.onMouseDrag = position => {
  };
  self.backColor = '';
  self.fontColor = '';
  self.getRateX = () => self.getWidth(shape, self) / self.width;
  self.getRateY = () => self.getHeight(shape, self) / self.height;
  shape.addRegion(self, index);
  return self;
};

const exampleRegion = (shape, getx, gety, getWidth, getHeight, index) => {
  let self = hitRegion(shape, getx, gety, getWidth, getHeight, index);
  self.drawStatic = (context, x, y) => {
    const r = 12;
    let x2 = x + r;
    let y2 = y + r;
    context.lineWidth = 1;
    context.beginPath();
    context.arc(x2, y2, 10, 0, 2 * Math.PI);
    context.strokeStyle = shape.getBorderColor();
    context.fillStyle = 'white';
    context.fill();
    context.stroke();
  };
  let drawArc = (angle, context) => {
    context.beginPath();
    context.strokeStyle = shape.getBorderColor();
    context.lineWidth = 3;
    context.arc(0, 0, 7, angle, angle + (0.15 * Math.PI));
    context.stroke();
  };

  let degree = 0;
  let step = Math.PI / 200;
  let drawDynamic = self.drawDynamic;
  self.drawDynamic = (context, x, y) => {
    drawDynamic.call(self, context, x, y);
    const r = 12;
    let x1 = x + r;
    let y1 = y + r;
    context.save();
    context.translate(x1, y1);
    context.rotate(degree);
    drawArc(0, context);
    drawArc((1 / 3) * Math.PI, context);
    drawArc((2 / 3) * Math.PI, context);
    drawArc(Math.PI, context);
    drawArc((4 / 3) * Math.PI, context);
    drawArc((5 / 3) * Math.PI, context);
    context.restore();
    degree += step;
    if (degree >= Math.PI * 2) {
      degree = 0;
    }
  };
  return self;
};

const countRegion = page => {
  let self = hitRegion(page, () => 2, () => 2);
  self.type = 'sessionCount';
  self.width = 150;
  self.height = 20;
  self.getVisibility = () => page.graph.collaboration.getStatus() === 'running';
  self.drawStatic = (context, x, y, width, height) => {
  };
  return self;
};

const lockRegion = (shape, userId, name, index) => {
  const oX = 4;
  const oY = -5;
  const width = 10;
  const getx = (s, r) => oX + (self.index * width);
  const gety = (s, r) => oY;
  let self = hitRegion(shape, getx, gety);
  self.type = 'lock';
  self.width = width;
  self.height = width;
  self.index = index;
  self.userId = userId;
  self.drawStatic = (context, x, y) => {
    context.strokeStyle = 'white';
    context.fillStyle = 'darkorange';

    context.beginPath();
    context.arc(x + 5, y + 10, 4, 0, 2 * Math.PI);
    context.fill();

    context.beginPath();
    context.arc(x + 5, y + 3, 3, 0, 2 * Math.PI);
    context.fill();
    context.stroke();

    self.text = `${name}(${userId}编辑...)`;
  };
  return self;
};

const priorityRegion = (shape, getx, gety, index) => {
  const getWidth = (s, r) => {
    r.width = 12;
    return r.width;
  };
  const getHeight = (s, region) => {
    region.height = 12;
    return region.height;
  };
  let self = hitRegion(shape, getx, gety, getWidth, getHeight, index);
  self.type = 'priority';
  self.text = '优先级';
  self.drawStatic = (context, x, y, width, height) => {
    let red = 255 - ((shape.priority - 1) * 25);
    let green = (shape.priority - 1) * 25;
    context.dynamicRect(x, y, width, height, 1, 'transparent', `RGB(${red},${green},0`, 4, 1);
    context.fillStyle = 'white';
    context.font = `normal bold ${(height - 2)}px arial`;
    const w = context.measureText(shape.priority).width;
    context.fillText(shape.priority, x + (height / 2) - (w / 2), y + height - 2);
  };
  self.drawDynamic = undefined;
  return self;
};

const infoTypeRegion = (shape, getx, gety, index) => {
  let self = priorityRegion(shape, getx, gety, index);
  self.type = 'information';
  self.text = '信息';
  self.drawStatic = (context, x, y, width, height) => {
    switch (shape.infoType.name) {
      case INFO_TYPE.INFORMATION.name: {
        context.fillStyle = 'steelblue';
        context.strokeStyle = 'white';
        context.font = `normal bold ${height - 1}px Arial`;
        context.beginPath();
        context.arc(x + (width / 2), y + (height / 2), width / 2, 0, 2 * Math.PI);
        context.fill();
        context.fillStyle = 'white';
        const w = context.measureText('i').width;
        context.fillText('i', x + (width / 2) - (w / 2), y + ((height * (height - 1)) / height));
        break;
      }
      case INFO_TYPE.WARNING.name: {
        context.beginPath();
        context.moveTo(x + (width / 2), y);
        context.lineTo(x + width, y + height - 1);
        context.lineTo(x, y + height - 1);
        context.closePath();
        context.fillStyle = 'yellow';
        context.strokeStyle = 'orange';
        context.fill();
        context.lineWidth = 1;
        context.stroke();
        context.beginPath();
        context.lineWidth = 2;
        context.moveTo(width / 2, height * 0.3);
        context.lineTo(width / 2, height * 0.8);
        context.stroke();
        break;
      }
      case INFO_TYPE.ERROR.name: {
        context.fillStyle = 'red';
        context.strokeStyle = 'white';
        context.beginPath();
        context.arc(x + (width / 2), y + (height / 2), (width / 2), 0, 2 * Math.PI);
        context.fill();
        context.drawLine(x + (width / 2),
          y + (height * 0.2),
          x + (width / 2),
          y + (height * 0.6),
          3,
          'white');
        context.arc(x + (width / 2), y + (height * 0.8), 2, 0, 2 * Math.PI);
        context.fillStyle = 'white';
        context.fill();
        break;
      }
      default:
        break;
    }
  };
  return self;
};
const progressRegion = (shape, getx, gety, index) => {
  let self = priorityRegion(shape, getx, gety, index);
  self.type = 'progress';
  self.text = '进度';
  self.drawStatic = (context, x, y, width, height) => {
    let x2 = x + (width / 2);
    let y2 = y + (height / 2);
    context.beginPath();
    context.arc(x2, y2, (width / 2), 0, 2 * Math.PI);
    context.fillStyle = 'white';
    context.fill();

    context.strokeStyle = context.fillStyle = 'darkred';
    context.lineWidth = 1;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x2 + (width / 2), y2);
    context.arc(x2,
      y2,
      (width / 2) - 1,
      1,
      1 + (2 * Math.PI * (shape.progressPercent === 0 ? 1 : shape.progressPercent)));
    context.closePath();
    context.fill();
    context.beginPath();
    context.arc(x2, y2, (width / 2) - 1, 0, 2 * Math.PI);
    context.stroke();
  };
  return self;
};

const assignToRegion = (shape, getx, gety, index) => {
  const self = priorityRegion(shape, getx, gety, index);
  self.type = 'assignTo';
  self.text = '--';
  self.drawStatic = (context, x, y, width, height) => {
    let x2 = x + (width / 2);
    let y2 = y + (height / 2);
    context.strokeStyle = 'white';
    context.fillStyle = 'darkgreen';

    context.beginPath();
    context.arc(x2, y + height, height / 2, 0, 2 * Math.PI);
    context.fill();

    context.beginPath();
    context.arc(x2, y2 - 2, 4, 0, 2 * Math.PI);
    context.fill();
    context.stroke();
  };
  return self;
};

const progressStatusRegion = (shape, getx, gety, index) => {
  let self = priorityRegion(shape, getx, gety, index);
  self.type = 'progressStatus';
  self.text = '状态';
  let degree = 0;
  let step = Math.PI / 100;
  let drawArc = (angle, context, radius) => {
    context.beginPath();
    context.strokeStyle = shape.progressStatus.color;
    context.lineWidth = 3;
    context.arc(0, 0, radius * 0.8, angle, angle + (0.15 * Math.PI));
    context.stroke();
  };
  self.drawDynamic = (context, x, y, width, height) => {
    let widthVal = width;
    let heightVal = height;
    widthVal === undefined && (widthVal = self.getWidth(shape, self));
    heightVal === undefined && (heightVal = self.getHeight(shape, self));
    context.clearRect(0, 0, context.canvas.width, context.canvas.height);
    self.drawStatic(context, x, y, widthVal, heightVal);
    let x1 = x + (widthVal / 2);
    let y1 = y + (heightVal / 2);
    switch (shape.progressStatus.name) {
      case PROGRESS_STATUS.RUNNING.name:
      case PROGRESS_STATUS.DOING.name: {
        context.save();
        context.translate(x1, y1);
        context.rotate(degree);
        const r = widthVal / 3;
        drawArc(0, context, r);
        drawArc((1 / 3) * Math.PI, context, r);
        drawArc((2 / 3) * Math.PI, context, r);
        drawArc(Math.PI, context, r);
        drawArc((4 / 3) * Math.PI, context, r);
        drawArc((5 / 3) * Math.PI, context, r);
        context.restore();
        degree += step * (shape.progressStatus.name === PROGRESS_STATUS.RUNNING.name ? 1 : 0.2);
        if (degree >= Math.PI * 2) {
          degree = 0;
        }
        break;
      }
      default:
        break;
    }
  };

  self.drawStatic = (context, x, y, width, height) => {
    let x2 = x + (width / 2);
    let y2 = y + (height / 2);
    context.lineWidth = 1;
    context.beginPath();
    context.arc(x2, y2, (width / 2) - context.lineWidth, 0, 2 * Math.PI);
    context.strokeStyle = shape.progressStatus.color;
    context.fillStyle = 'white';
    context.fill();
    context.stroke();

    switch (shape.progressStatus.name) {
      case PROGRESS_STATUS.NOTSTARTED.name: {
        context.beginPath();
        context.fillStyle = shape.progressStatus.color;
        context.rect(x + (width / 3), y + (height * 2 / 5), width / 3, height / 5);
        context.fill();
        context.stroke();
        break;
      }
      case PROGRESS_STATUS.PAUSE.name: {
        context.fillStyle = shape.progressStatus.color;
        context.fillRect((Number(width) / 3) - (Number(width) / 12), height / 4, Number(width) / 6, height / 2);
        context.fillRect(((width * 2) / 3) - (width / 12), height / 4, width / 6, height / 2);
        break;
      }
      case PROGRESS_STATUS.COMPLETE.name: {
        let sx1 = width / 4;
        let sy1 = height / 2;
        context.beginPath();
        context.moveTo(sx1, sy1);
        context.lineTo(sx1 + (width / 6), sy1 + (height / 4));
        context.lineTo(sx1 + (width / 2), sy1 - (height / 6));
        context.lineWidth = 2;
        context.strokeStyle = shape.progressStatus.color;
        context.stroke();
        break;
      }
      case PROGRESS_STATUS.ERROR.name: {
        let sx2 = width / 4;
        let sy2 = height / 4;
        context.lineWidth = 2;
        context.strokeStyle = shape.progressStatus.color;
        context.beginPath();
        context.moveTo(sx2, sy2);
        context.lineTo(sx2 + (width / 2), sy2 + (height / 2));
        context.stroke();
        context.beginPath();
        context.moveTo(sx2 + (width / 2), sy2);
        context.lineTo(sx2, sy2 + (height / 2));
        context.stroke();
        break;
      }
      case PROGRESS_STATUS.UNKNOWN.name: {
        context.lineWidth = 2;
        context.strokeStyle = shape.progressStatus.color;
        context.beginPath();
        context.arc(x + (width / 2), y + (height / 2), width / 4, 0, 2 * Math.PI);
        context.fillStyle = shape.progressStatus.color;
        context.fill();
        break;
      }
      default:
        break;
    }
  };
  return self;
};

const shapeCommentRegion = (shape, getx, gety, getWidth, getHeight, index) => {
  let self = hitRegion(shape, getx, gety, getWidth, getHeight, index);
  self.type = 'shape-comment';
  self.text = '评论';
  self.getVisibility = () => shape.comments !== undefined && shape.comments.length > 0 && self.visible && shape.getVisibility();

  self.click = async () => {
    if (shape.comments === undefined) {
      return;
    }
    shape.page.sm.getShapes(s => s.isType('shapeComment')).forEach(s => s.stop('pop'));
    shape.comments.forEach(async c => {
      shape.page.showComment(shape, c, 'pop');
      await sleep(100);
    });
  };

  self.drawStatic = (context, x, y) => {
    context.fillStyle = 'white';
    context.strokeStyle = 'green';
    context.lineWidth = 1;

    context.beginPath();
    context.rect(6, 2, 17, 12);
    context.fill();
    context.stroke();

    context.beginPath();
    context.rect(1, 7, 17, 12);
    context.fill();
    context.stroke();

    context.fillStyle = 'green';
    context.font = 'normal bold 12px Arial';
    context.fillText('...', 5, 14);

    context.beginPath();
    let x1 = 5;
    let y1 = 19;
    context.moveTo(x1, y1);
    context.lineTo(x1 - 3, y1 + 3);
    context.lineTo(x1 + 6, y1);
    context.closePath();
    context.fill();
  };
  return self;
};

const sharedRegion = (shape, getx, gety, getWidth, getHeight, index) => {
  let self = hitRegion(shape, getx, gety, getWidth, getHeight, index);
  self.type = 'shape-shared';
  self.text = '共享';
  self.getVisibility = () => shape.shared && shape.getVisibility();

  self.drawStatic = (context, x, y) => {
    const R = 4;
    context.strokeStyle = 'Teal';
    context.fillStyle = 'Teal';
    context.beginPath();
    context.arc(24 - R - 2, R + 2, R, 0, 2 * Math.PI);
    context.fill();
    context.beginPath();
    context.arc(R + 1, 12, R, 0, 2 * Math.PI);
    context.fill();
    context.beginPath();
    context.arc(24 - R - 2, 24 - R - 2, R, 0, 2 * Math.PI);
    context.fill();

    context.lineWidth = 2;
    context.beginPath();
    context.moveTo(24 - R - 2, R + 2);
    context.lineTo(R + 1, 12);
    context.lineTo(24 - R - 2, 24 - R - 2);
    context.stroke();
  };
};

const copyCot = (content) => {
  const pEle = document.createElement('p');
  pEle.innerHTML = content || '';
  document.body.appendChild(pEle);

  const range = document.createRange();
  window.getSelection().removeAllRanges();
  range.selectNode(pEle);
  window.getSelection().addRange(range);

  document.execCommand('Copy');
  document.body.removeChild(pEle);
  window.getSelection().removeAllRanges();
};

const sharedButtonRegion = (shape, getx, gety, getWidth, getHeight, index) => {
  let self = hitRegion(shape, getx, gety, getWidth, getHeight, index);
  self.type = 'shape-shared-button';
  self.text = '点击共享';
  self.src = '';

  self.getVisibility = () => {
    return shape.shareAble && shape.getVisibility();
  };

  self.click = () => {
    const container = shape.getContainer();
    container.borderWidth = 1;
    container.invalidateAlone();

    const btn = 'share-and-copy';
    const content = `<div style="display: flex;">
                            <div>复制并粘贴到其他文档中，即可完成当前内容的共享操作</div>
                            <button id="${btn}" style="width: 87px;height: 28px;background: #fff;border: 1px solid #ccc;border-radius: 3px;color: #0078d4;cursor: pointer;">共享复制</button>
                        </div>`;
    const pop = new Popup(shape.page.div, '共享', content);
    pop.show();

    shape.page.div.addEventListener('click', async (event) => {
      if (event.target.id === btn) {
        event.preventDefault();

        const c = shape.getContainer();
        c.shared = true;
        const copyResult = shape.page.onCopy([shape.getContainer()]);
        if (copyResult) {
          const data = {
            type: `elsa`,
            shapes: copyResult.data,
          };
          await navigator.clipboard.writeText(JSON.stringify(data)); // navigator仅支持https站点
        }
        pop.cancel();
        c.borderWidth = 0;
        layoutCommand(shape.page, [{shape: c, shared: true, sharedBy: shape.page.graph.session.id}]).execute();
      }
    });
  };
};


export {
  hitRegion,
  priorityRegion,
  infoTypeRegion,
  progressRegion,
  progressStatusRegion,
  shapeCommentRegion,
  lockRegion,
  assignToRegion,
  countRegion,
  sharedRegion,
  sharedButtonRegion,
};

