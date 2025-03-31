/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, SELECTION_STRATEGY, Z_INDEX_OFFSET} from '../../common/const.js';
import {compareAndSet, isPointInRect, pixelRateAdapter} from '../../common/util.js';

/**
 * 绘制基类，默认使用html css绘制shape
 * 辉子 2020-02-20
 */
const drawer = (shape, div, x = 0, y = 0) => {
  let self = {};
  self.type = 'html drawer';
  self.shape = shape;
  self.createElement = (tagName, id, ignoreExisting) => {
    const node = shape.graph.createDom(div, tagName, id, shape.page.id, ignoreExisting);
    // @maliya 记录：这行代码会导致 浏览器调试面板 无法直接获取到指定dom
    node.style.pointerEvents = 'none';
    return node;
  };

  // for parent
  self.parent = self.createElement('div', shape.id);
  self.parent.innerHTML = ' ';
  self.parent.style.position = 'absolute';
  self.parent.style.boxSizing = 'border-box';

  // for text
  self.text = self.createElement('text', `textArea:${shape.id}`);
  self.text.style.zIndex = 999; // 使text始终处于最上方.
  (self.text.parentNode !== self.parent) && self.parent.appendChild(self.text);

  // 提高绘制性能
  self.perf = {};

  /**
   * 是否包含在文本区域内.
   * 判断时，需要去除padding：
   * 1、横坐标需加上左侧的padding
   * 2、宽度需减去左右的padding
   *
   * @param x 横坐标.
   * @param y 纵坐标.
   * @return {*|boolean} true/false.
   */
  self.containsText = (x, y) => {
    // 如果文本隐藏，则直接返回 false
    if (shape.hideText) {
      return false;
    }

    // 计算文本所在矩形区域的左上角坐标和宽高
    const textRect = {
      x: self.text.offsetLeft + shape.x + shape.borderWidth + shape.getPadLeft(),
      y: self.text.offsetTop + shape.y + shape.borderWidth,
      width: self.text.clientWidth - shape.getPadRight() - shape.getPadRight(),
      height: self.text.clientHeight,
    };

    // 检查给定的坐标是否在文本矩形区域内
    return isPointInRect({x, y}, textRect);
  };

  const inner = () => {
    const space = 2;
    return {
      x: shape.x + shape.borderWidth + space,
      y: shape.y + shape.borderWidth + space,
      width: shape.width - (2 * shape.borderWidth) - (2 * space),
      height: shape.height - (2 * shape.borderWidth) - (2 * space),
    };
  };
  const outer = () => {
    const space = 2;
    return {
      x: shape.x - space, y: shape.y - space, width: shape.width + (2 * space), height: shape.height + (2 * space),
    };
  };
  self.containsBorder = (x, y) => isPointInRect({x, y}, outer()) && !isPointInRect({x, y}, inner());
  self.containsBack = (x, y) => (isPointInRect({
    x, y,
  }, inner()) && shape.backAlpha > 0 && (shape.backColor !== 'transparent')) || self.background !== undefined;

  self.transform = () => {
    const transformNew = shape.rotateDegree === 0 ? '' : `rotate(${shape.rotateDegree}deg)`;
    compareAndSet(self.parent.style, 'transform', transformNew, () => {
      self.parent.style.transform = transformNew;
      let transform = self.parent.style.transform;
      self.parent.style.webkitTransform = transform;
      self.parent.style.mozTransform = transform;
      self.parent.style.msTransform = transform;
      self.parent.style.oTransform = transform;
      self.parent.style.transform = transform;
    });
    self.updateIfChange(self.parent.style, 'opacity', shape.globalAlpha, 'parent_opacity');
  };

  self.remove = () => {
    shape.clearCoordinateIndex();
    self.parent.remove();
  };

  self.move = () => {// react to shape.x,shape.y
    let deltaX = shape.x;
    let deltaY = shape.y;
    let finalX;
    let finalY;
    if (shape.width < 0) {
      deltaX += shape.width;
    }
    if (shape.height < 0) {
      deltaY += shape.height;
    }

    let container = shape.getContainer();
    container.drawer.appendChild(self);

    if (container !== shape.page) {
      deltaX = shape.x - container.x - container.borderWidth;
      deltaY = shape.y - container.y - container.borderWidth;

      if (shape.width < 0) {
        deltaX += shape.width;
      }
      if (shape.height < 0) {
        deltaY += shape.height;
      }

      finalX = deltaX + (shape.scrollLock.x ? 0 : container.itemScroll.x);
      finalY = deltaY + (shape.scrollLock.y ? 0 : container.itemScroll.y);
    } else {
      finalX = deltaX;
      finalY = deltaY;
    }
    finalX = Math.round(finalX) + (shape.float ? -shape.page.x : 0);
    finalY = Math.round(finalY) + (shape.float ? -shape.page.y : 0);

    self.parent.style.left = `${finalX}px`;
    self.parent.style.top = `${finalY}px`;
    self.parent.style.position = 'absolute';
    shape.indexCoordinate();
    return {x: finalX, y: finalY};
  };
  shape.getTextSize = () => {
    return {width: self.text.clientWidth, height: self.text.clientHeight};
  };

  self.backgroundRefresh = () => {
    self.parent.style.background = shape.getBackColor();
    if (shape.background === undefined || shape.background === '') {
      return;
    }
    self.parent.style.backgroundSize = '100% 100%';
    self.parent.style.backgroundImage = `url(${shape.background})`;
  };

  self.hide = () => {
    self.parent.style.display = 'none';
  };

  /**
   * 刷新index，决定图形的展示层级.
   */
  self.refreshIndex = () => {
    let index;
    if (shape.page.selectionStrategy === SELECTION_STRATEGY.BRING_TO_FRONT) {
      if (shape.isFocused) {
        index = 9999;
      } else {
        index = shape.getIndex();
      }
    } else {
      index = shape.getIndex();
    }
    self.parent.style.zIndex = index;
  };

  self.parentResize = (width, height) => {
    self.parent.style.display = 'flex';
    self.parent.style.justifyContent = shape.hAlign;
    self.parent.style.alignItems = toVAlign(shape.vAlign);
    self.refreshIndex();
    self.parent.className = shape.type;
    if (!shape.autoWidth) {
      self.parent.style.width = `${width}px`;
    }
    self.parent.style.height = `${height}px`;
    self.parent.style.overflow = shape.overflowHidden === true ? 'hidden' : 'visible';
  };

  const toVAlign = (align) => {
    switch (align) {
      case ALIGN.TOP:
        return 'start';
      case ALIGN.BOTTOM:
        return 'end';
      case ALIGN.MIDDLE:
        return 'center';
      default:
        return 'center';
    }
  };

  self.getEditor = () => {
    if (shape.hideText) {
      return null;
    }
    if (self.brush === null || self.brush === undefined) {
      self.brush = shape.graph.createEditor(shape);
    }
    return self.brush;
  };

  function set(obj, key, value) {
    obj[key] = value;
  }

  self.updateIfChange = (obj, key, value, perfKey) => {
    let perfKeyVal = perfKey;
    if (!perfKeyVal) {
      perfKeyVal = key;
    }
    if (self.perf[perfKeyVal] === value) {
      return false;
    }
    self.perf[perfKeyVal] = value;
    set(obj, key, value);
    return true;
  };

  self.updateCanvas = (width, height, canvasName) => {
    let widthVal = width;
    let heightVal = height;
    const margin = shape.margin;
    const borderWidth = shape.borderWidth;
    widthVal = Number(widthVal) + Number(borderWidth) + (2 * Number(margin));
    heightVal = Number(heightVal) + Number(borderWidth) + (2 * Number(margin));
    const isSizeChanged = self.perf[`${canvasName}_width`] !== widthVal ||
      self.perf[`${canvasName}_height`] !== heightVal;
    const isScaleChanged = self.perf[`${canvasName}_scaleX`] !== shape.page.scaleX ||
      self.perf[`${canvasName}_scaleY`] !== shape.page.scaleY;
    if (isSizeChanged || isScaleChanged) {
      self.perf[`${canvasName}_width`] = widthVal;
      self.perf[`${canvasName}_height`] = heightVal;
      self.perf[`${canvasName}_scaleX`] = shape.page.scaleX;
      self.perf[`${canvasName}_scaleY`] = shape.page.scaleY;
      self[canvasName].width = widthVal;
      self[canvasName].height = heightVal;
      self.pixelRate = pixelRateAdapter(self[canvasName].getContext('2d'), shape.page.scaleX, shape.page.scaleY, shape.page.graph.ignoreHighQuality);
    }
    return {width: widthVal, height: heightVal};
  };

  self.getTextPaddingLeft = () => {
    return `${shape.getPadLeft()}px`;
  };

  self.getTextPaddingRight = () => {
    return `${shape.getPadRight()}px`;
  };

  self.getTextPaddingTop = () => {
    return `${shape.getPadTop()}px`;
  };

  self.getTextPaddingBottom = () => {
    return `${shape.getPadBottom()}px`;
  };

  self.getTextWidth = () => {
    return `${(self.parent.clientWidth - shape.getPadLeft() - shape.getPadRight())}px`;
  };

  self.getTextPosition = () => null;

  self.getTextPadding = () => null;

  self.getTextLeft = () => null;

  self.getTextTop = () => null;

  self.getTextWhiteSpace = () => {
    return 'pre-wrap';
  };

  self.textResize = () => {
    if (self.text === undefined) {
      return;
    }

    self.updateIfChange(self.text.style, 'visibility', shape.hideText || (!shape.getVisibility()) ? 'hidden' : 'visible', 'text_visibility');
    if (self.getTextPosition()) {
      self.updateIfChange(self.text.style, 'position', self.getTextPosition(), 'text_position');
    }
    if (self.getTextPadding()) {
      self.updateIfChange(self.text.style, 'padding', self.getTextPadding(), 'text_padding');
    }
    if (self.getTextLeft()) {
      self.updateIfChange(self.text.style, 'left', self.getTextLeft(), 'text_left');
    }
    if (self.getTextTop()) {
      self.updateIfChange(self.text.style, 'top', self.getTextTop(), 'text_top');
    }
    self.updateIfChange(self.text.style, 'textAlign', shape.textAlign ? shape.textAlign : shape.hAlign, 'text_textAlign');
    self.updateIfChange(self.text.style, 'fontSize', `${shape.fontSize}px`, 'text_fontSize');
    self.updateIfChange(self.text.style, 'fontFamily', shape.fontFace, 'text_fontFamily');
    self.updateIfChange(self.text.style, 'fontWeight', shape.fontWeight, 'text_fontWeight');
    self.updateIfChange(self.text.style, 'color', shape.getFontColor(), 'text_fontColor');
    self.updateIfChange(self.text.style, 'lineHeight', shape.lineHeight, 'text_lineHeight');
    self.updateIfChange(self.text.style, 'paddingLeft', self.getTextPaddingLeft(), 'text_paddingLeft');
    self.updateIfChange(self.text.style, 'paddingRight', self.getTextPaddingRight(), 'text_paddingRight');
    self.updateIfChange(self.text.style, 'paddingTop', self.getTextPaddingTop(), 'text_paddingTop');
    self.updateIfChange(self.text.style, 'paddingBottom', self.getTextPaddingBottom(), 'text_paddingBottom');
    self.updateIfChange(self.text.style, 'letterSpacing', shape.letterSpacing === undefined ? '' : (`${shape.letterSpacing}px`), 'text_letterSpacing');
    self.updateIfChange(self.text.style, 'wordSpacing', shape.wordSpacing === undefined ? '' : (`${shape.wordSpacing}px`), 'text_wordSpacing');

    // 不设置成block，会导致一开始是inline，但图形整体渲染完成之后，又会默认变成block，因此图形的宽度会存在差异，如果此时连接有线，会导致线渲染异常。
    self.updateIfChange(self.text.style, 'display', 'block', 'text_display');
    if (!shape.autoWidth) {
      self.updateIfChange(self.text.style, 'width', '90%', 'text_width');
      self.updateIfChange(self.text.style, 'whiteSpace', '', 'pre-wrap');
      self.updateIfChange(self.text.style, 'wordWrap', '', 'break-word');
    } else {
      self.updateIfChange(self.text.style, 'whiteSpace', 'nowrap', 'auto_wrap');
      self.updateIfChange(self.text.style, 'wordBreak', 'keep-all');
      if (shape.maxWidth) {
        self.updateIfChange(self.text.style, 'max-width', `${shape.maxWidth}px`);
      }
      if (shape.minWidth) {
        self.updateIfChange(self.text.style, 'min-width', `${shape.minWidth}px`);
      }
    }
  };

  self.animationResize = (width, height) => {
    if ((shape.enableAnimation || shape.isFocused || shape.linking)) {// && !shape.inDragging) {
      if (self.animationCanvas === undefined) {
        self.animationCanvas = self.createElement('canvas', `animation:${shape.id}`);
        self.animationCanvas.style.position = 'absolute';
        (self.animationCanvas.parentNode !== self.parent) && self.parent.appendChild(self.animationCanvas);
      }

      const canvasSize = self.updateCanvas(width, height, 'animationCanvas');
      const dx = -(canvasSize.width - width) / 2 - shape.getBorderWidth();
      const dy = -(canvasSize.height - height) / 2 - shape.getBorderWidth();
      self.updateIfChange(self.animationCanvas.style, 'left', `${dx}px`, 'animationCanvas_left');
      self.updateIfChange(self.animationCanvas.style, 'top', `${dy}px`, 'animationCanvas_top');

      self.drawFocus();
    } else {
      if (self.animationCanvas !== undefined) {
        self.animationCanvas.remove();
        // 清理动态的canvas
        for (let key in self.perf) {
          if (key && key.startsWith('animationCanvas_')) {
            delete self.perf[key];
          }
        }
      }
      self.animationCanvas = undefined;
    }
  };

  self.calculateWidth = shape => {
    // * 注意 * text未mount到dom树中时，不进行autoWidth计算.
    if (!shape.autoWidth || self.text.offsetWidth === 0) {
      return Math.abs(shape.width);
    }
    const w = self.parent.offsetWidth + 2 * shape.borderWidth + 1;// + shape.getPadLeft() + shape.getPadRight();
    shape.width = shape.minWidth ? Math.max(shape.minWidth, w) : w;
    return shape.width;
  };

  self.resize = () => {// react to shape.width,shape.height
    self.textResize();
    const width = self.calculateWidth(shape);
    const height = Math.abs(shape.height);
    self.backgroundRefresh();
    self.parentResize(width, height);
    self.animationResize(width, height);
    if (shape.width < 0 || shape.height < 0) {
      self.move();
    }
    return {width, height};
  };

  self.drawFocus = context1 => {
    if (self.animationCanvas === undefined) {
      return;
    }
    self.animationCanvas.style.zIndex = parseInt(self.parent.style.zIndex) + 100;
    let context = context1;

    if (context1 === undefined) {
      context = self.animationCanvas.getContext('2d');
      context.save();
      context.clearRect(0, 0, self.animationCanvas.width / self.pixelRate.ratioX, self.animationCanvas.height / self.pixelRate.ratioY);
      // 这里把之前远点向左上方做了2px偏移去掉,保持原点为图形的正中心
      context.translate((self.animationCanvas.width / 2) / self.pixelRate.ratioX, (self.animationCanvas.height / 2) / self.pixelRate.ratioY);
    }

    if (shape.isFocused) {
      self.drawFocusFrame(context);
      self.drawConnectors(context, s => s.dragable);
    } else if (shape.linking) {
      self.drawLinkingFrame(context);
      self.drawConnectors(context, s => s.getConnectable());
    }
    if (context1 === undefined) {
      context.restore();
    }
  };

  self.drawConnectors = (context, condition) => {
    if (!shape.isInConfig()) {
      return;
    }
    let x1 = -shape.width / 2;
    let y1 = -shape.height / 2;
    shape.connectors
      .filter(c => c.getVisibility() && (!condition || condition(c)))
      .forEach(connector => connector.draw(context, x1, y1));
  };
  self.drawRegions = () => {
    let regions = shape.regions.filter(r => r.getVisibility() && shape.getVisibility());
    for (let i = 0; i < self.parent.childElementCount; i++) {
      const c = self.parent.children[i];
      (!regions.find(r => r.getId() === c.id)) && (c.id.indexOf('region-') >= 0) && (c.style.visibility = 'hidden');
    }
    regions.forEach(r => {
      if (!r.disableCanvas) {
        const canvas = self.createElement('canvas', r.getId());
        canvas.style.zIndex = shape.getIndex() + 1 + (r.index ? r.index : 1);
        canvas.style.visibility = 'visible';
        self.parent.appendChild(canvas);
        canvas.style.position = 'absolute';
        r.context = canvas.getContext('2d');
      }
      r.draw();
    });
  };
  self.initialize = () => {
    self.textInitialize();
  };

  /**
   * 文本初始化相关操作.
   */
  self.textInitialize = () => {
    if (shape.hideText) {
      self.text.remove();
      return;
    }

    const editor = self.getEditor();

    // 如果存在textInnerHtml，并且开启了enableTextCache，那么通过textInnerHtml来进行渲染
    if (shape.isEnableHtmlText() && shape.textInnerHtml !== null && shape.textInnerHtml !== undefined) {
      editor.renderByInnerHtml(shape.textInnerHtml);
    } else {
      if (shape.text !== '') {
        self.renderText();
      }
    }

    self.registerTextEvent();
  };

  /**
   * 注冊 text 相关事件.
   */
  self.registerTextEvent = () => {
  };

  /**
   * 触发该方法有两种途径
   * 1、初始化时触发
   * 2、手动设置text属性时触发
   *
   * @param autoFocus 是否自动聚焦.
   */
  self.renderText = (autoFocus) => {
    if (shape.hideText) {
      return;
    }
    const normalizedText = typeof shape.text === 'string' ? `<p>${shape.text}</p>` : shape.text;
    self.renderTextByEditor(normalizedText, autoFocus);
  };

  /**
   * 通过编辑器渲染文本.
   * 第一次通过编辑器渲染时，需要记录textInnerHtml，方便后续初始化时，通过textInnerHtml渲染，提高渲染性能.
   *
   * @type {(function(*, *): void)|*}
   */
  self.renderTextByEditor = (text, autoFocus) => {
    const editor = self.getEditor();
    editor.unmount();
    editor.render(text);
    autoFocus && editor.focus();
    if (shape.placeholder) {
      editor.setPlaceholder(shape.placeholder);
    }
  };

  /**
   * 禁用文本dom的pointerEvents.
   */
  self.disableTextPointerEvents = () => {
    self.text && (self.text.style.pointerEvents = 'none');
  };

  /**
   * 开启文本dom的pointerEvents.
   */
  self.enableTextPointerEvents = () => {
    self.text && (self.text.style.pointerEvents = 'auto');
  };

  /**
   * 判断文本dom是否处于禁用状态.
   *
   * @return {boolean} true/false.
   */
  self.isTextPointerEventsDisabled = () => {
    return self.text.style.pointerEvents === 'none';
  };

  self.getSnapshot = () => {
    return self.parent;
  };
  self.draw = function () {
    let inited = false;
    return () => {
      if (!inited) {
        self.initialize();
        inited = true;
      }
      self.resize();
      self.transform();
      if (!shape.getBound().visible) {
        return;
      }
      if (!shape.getVisibility()) {
        return;
      }
      self.drawRegions();
      self.drawBorder();
      self.drawStatic();
    };
  }();

  self.drawAnimation = () => {
    if (!shape.visible) {
      return;
    }
    if (self.animationCanvas === undefined) {
      self.animationResize();
    }

    if (self.animationCanvas === undefined) {
      return;
    }

    let context = self.animationCanvas.getContext('2d');
    context.save();
    context.clearRect(0, 0, context.canvas.width / self.pixelRate.ratioX, context.canvas.height / self.pixelRate.ratioY);
    context.translate(context.canvas.width / 2 / self.pixelRate.ratioX, context.canvas.height / 2 / self.pixelRate.ratioY);
    context.beginPath();
    self.drawDynamic(context, 0, 0);
    self.drawFocus(context);
    context.closePath();
    context.restore();

    shape.regions.filter(r => r.getVisibility()).forEach(r => {
      if (r.context !== null && r.drawDynamic) {
        r.drawDynamic(r.context, 0, 0);
      }
    });
    context.restore();
  };

  self.drawDynamic = (context, x, y) => {
  };
  self.drawStatic = () => {
  };
  self.drawFocusFrame = context => {
  };
  self.drawLinkingFrame = context => {
  };

  self.drawBorder = () => {
    // 如果borderWidth不存在，则不绘制边框.
    const borderWidth = shape.getBorderWidth();
    if (!borderWidth) {
      self.parent.style.border = '';
      return;
    }

    let border = `${borderWidth}px `;
    if (shape.dashWidth === 0) {
      border += 'solid ';
    } else {
      border += shape.dashWidth > 4 ? 'dashed' : 'dotted';
    }

    self.updateIfChange(self.parent.style, 'border', `${border} ${shape.getBorderColor()}`, 'parent_border');
    const borderRadius = `${shape.cornerRadius}px`;
    self.updateIfChange(self.parent.style, 'borderRadius', borderRadius, 'parent_borderRadius');
    if (self.text) {
      self.updateIfChange(self.text.style, 'borderRadius', borderRadius, 'text_borderRadius');
    }
    self.updateIfChange(self.parent.style, 'boxShadow', shape.getShadow(), 'parent_boxShadow');
  };

  /**
   * drawer里也应当有remove相关的生命周期，用于图形删除后释放对应的资源.
   */
  self.beforeRemove = () => {
    if (self.brush) {
      self.brush.destroy();
      self.brush = null;
      self.perf.text = null;
    }
  };

  return self;
};

const simpleDrawer = (shape, div, x, y) => {
  let self = drawer(shape, div, x, y);
  self.resize = () => {// react to shape.width,shape.height
    let width = Math.abs(shape.width) - 2 * (shape.borderWidth);
    let height = Math.abs(shape.height) - 2 * (shape.borderWidth);

    self.parent.style.visibility = shape.getVisibility() ? 'visible' : 'hidden';
    self.text.style.visibility = shape.hideText || (!shape.getVisibility()) ? 'hidden' : 'visible';

    if (self.parent.style.visibility === 'visible') {
      self.parent.id = `${shape.type}:${shape.id}`;
      self.parent.textContent = (shape.text === '' || shape.hideText) ? ' ' : shape.text;
      self.parent.style.zIndex = shpae.isFocused ? (shape.page.sm.getShapeCount() + Z_INDEX_OFFSET) : shape.index;
      self.parent.style.width = `${(width - shape.getPadLeft() - shape.getPadRight())}px`;
      self.parent.style.height = `${(height - shape.getPadTop() - shape.getPadBottom())}px`;
      self.parent.style.opacity = shape.globalAlpha;

      self.parent.style.fontFamily = shape.fontFace;
      self.parent.style.fontSize = `${shape.fontSize}px`;
      self.parent.style.fontWeight = shape.fontWeight;
      self.parent.style.color = shape.getFontColor();
      self.parent.style.textAlign = shape.textAlign ? shape.textAlign : shape.hAlign;
      self.parent.style.lineHeight = shape.lineHeight;
      self.parent.style.fontStyle = shape.fontStyle;
      self.parent.style.paddingLeft = `${shape.getPadLeft()}px`;
      self.parent.style.paddingRight = `${shape.getPadRight()}px`;
      self.parent.style.paddingTop = `${shape.getPadTop()}px`;
      self.parent.style.paddingBottom = `${shape.getPadBottom()}px`;

      self.parent.style.whiteSpace = 'pre-wrap';
      self.parent.style.wordWrap = 'break-word';
    }
    // for animation
    self.animationResize(width, height);
    return {width: width, height: height};
  };
  return self;
};

const customizedDrawer = (elementName, preConstruct) => {
  return (shape, div, x, y) => {
    let self = drawer(shape, div, x, y);
    self.parent.innerHTML = ' ';
    self.offset = 2;

    // for element
    shape.element = self.element = self.createElement(elementName, `custom:${shape.id}`);
    if (preConstruct) {
      preConstruct(self.element, self);
    }
    self.element.style.position = 'absolute';
    self.parent.appendChild(self.element);
    // 用于显示线的text
    self.parent.appendChild(self.text);

    let resize = self.resize;
    self.resize = () => {
      resize.apply(self);
      self.element.style.top = `${self.offset}px`;
      self.element.style.left = `${self.offset}px`;
      self.element.style.width = `${shape.width - (2 * self.offset)}px`;
      self.element.style.height = `${shape.height - (2 * self.offset)}px`;
      self.element.style.border = '0px';
    };
    return self;
  };
};

export {drawer, simpleDrawer, customizedDrawer};


