/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {pixelRateAdapter} from '../../common/util.js';
import {cursorDrawer} from './cursorDrawer.js';
import {PAGE_OPERATION_MODE} from '../../common/const.js';

const CURSOR_DEFAULT_SIZE = 30;
/**
 * 鼠标移动时交互层绘制
 * 绘制鼠标，鼠标圈定的范围
 * 辉子 2021
 */
const interactDrawer = (graph, page, div) => {
  let self = {};
  self.type = 'interact drawer';

  const sensorId = (p) => {
    return `interactLayer:${p.id}`;
  };

  const getZoom = () => {
    let parent = div.parentNode;
    let zoom = 1;
    while (parent) {
      if (parent.nodeName !== '#document') {
        const parentZoom = getComputedStyle(parent).zoom;
        if (parentZoom !== '') {
          zoom *= parseFloat(parentZoom);
        }
      }
      parent = parent.parentNode;
    }
    return zoom;
  };

  self.zoom = getZoom();

  /**
   * 刷新zoom.
   */
  self.refreshZoom = () => {
    self.zoom = getZoom();
  };

  /**
   * 交互层，这层配对interactDrawer
   */
  self.sensor = (() => {
    const id = sensorId(page);
    const sensor = graph.createDom(div, 'div', id, page.id);
    sensor.classList.add('interactLayer');
    // 如果不是父子关系，那么可能是display. 此时需要创建新的dom.
    if (!div.contains(sensor)) {
      sensor.style.zIndex = 2;
      div.appendChild(sensor);
    }
    return sensor;
  })();

  const selectionId = (p) => {
    return `selection:${p.id}`;
  };

  self.selection = (() => {
    const id = selectionId(page);

    // 如果不是父子关系，那么可能是display. 此时需要创建新的dom.
    const selection = graph.createDom(div, 'div', id, page.id);
    if (!self.sensor.contains(selection)) {
      selection.style.border = '1px dashed';
      selection.style.borderColor = 'gray';
      selection.style.background = 'rgba(232,232,232,0.1)';
      selection.style.pointerEvents = 'none';
      self.sensor.appendChild(selection);
    }
    return selection;
  })();

  /**
   * 闭包构造一个拖拽缩放的工具栏
   *
   * @type {{}}
   */
  self.positonBar = (() => {
    /**
     * 构造一个适配屏幕的工具
     *
     * @return {{}}
     */
    const fitTool = () => {
      const PAGE_FIT_SCREEN_SCALE_MIN = 0.1;
      const PAGE_FIT_SCREEN_SCALE_MAX = 1.0;
      const me = {};
      const button = graph.createDom(div, 'div', 'barToolsFit', page.id);
      button.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 500 500"><path xmlns="http://www.w3.org/2000/svg" fill="#121331" d="M57.294 187.526c-8.644 0-15.651-7.007-15.651-15.65v-52.083c0-20.117 16.367-36.483 36.484-36.483h52.083c8.644 0 15.651 7.007 15.651 15.65s-7.007 15.65-15.651 15.65H78.127a5.189 5.189 0 0 0-5.183 5.183v52.083c0 8.643-7.006 15.65-15.65 15.65zm72.917 229.169H78.128c-20.117 0-36.483-16.366-36.483-36.483v-50.781c0-8.643 7.006-15.65 15.65-15.65s15.65 7.007 15.65 15.65v50.781a5.19 5.19 0 0 0 5.183 5.183h52.083c8.644 0 15.65 7.007 15.65 15.65s-7.006 15.65-15.65 15.65zm291.668 0h-52.083c-8.644 0-15.651-7.007-15.651-15.65s7.007-15.65 15.651-15.65h52.083a5.189 5.189 0 0 0 5.183-5.183v-52.083c0-8.643 7.006-15.65 15.65-15.65s15.651 7.007 15.651 15.65v52.083c0 20.117-16.367 36.483-36.484 36.483zm20.833-229.168c-8.644 0-15.65-7.007-15.65-15.65v-52.083a5.19 5.19 0 0 0-5.183-5.184h-52.083c-8.644 0-15.651-7.006-15.651-15.649 0-8.643 7.007-15.65 15.651-15.65h52.083c20.117 0 36.484 16.366 36.484 36.483v52.083c0 8.643-7.007 15.65-15.651 15.65zm-62.499 166.668H119.795c-8.644 0-15.65-7.007-15.65-15.65V161.461c0-8.643 7.006-15.65 15.65-15.65h260.418c8.644 0 15.65 7.007 15.65 15.65v177.084c0 8.643-7.006 15.65-15.65 15.65zm-244.768-31.3h229.118V177.111H135.445v145.784z" class="primary"/></svg>`;
      button.style.display = 'flex';
      button.style.width = '22px';
      button.style.height = '22px';
      button.style.background = 'white';
      button.style.cursor = 'pointer';
      button.style.margin = '0px 3px 0px 3px';
      button.style.alignItems = 'center';
      button.style.justifyContent = 'center';
      button.onclick = () => {
        page.fitScreen(PAGE_FIT_SCREEN_SCALE_MIN, PAGE_FIT_SCREEN_SCALE_MAX);
      };

      /**
       * 获取工具栏组件，用于展示
       *
       * @return {*}
       */
      me.getComponent = () => button;

      /**
       * 刷新拖拽工具的样式
       */
      me.update = () => {
      };
      return me;
    };

    /**
     * 构造一个拖拽工具
     *
     * @return {{}}
     */
    const dragTool = () => {
      const me = {};
      const button = graph.createDom(div, 'div', 'barToolsDrag', page.id);
      button.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 14 16"><path fill="currentColor" fill-opacity="0.8" fill-rule="evenodd" d="M8.23 2.625v.059a.367.367 0 0 0-.002.04v4.543a.365.365 0 0 0 .73 0V2.772a.987.987 0 0 1 .258-.034h.014a.986.986 0 0 1 .972.986v5.16-3.917 2.4a.365.365 0 1 0 .73 0V4.044a.987.987 0 0 1 .256-.033h.014a.986.986 0 0 1 .972.986v4.688c0 .067-.003.133-.006.2a4.87 4.87 0 0 1-1.455 3.237v.426a.667.667 0 0 1-.666.666H5.538a.667.667 0 0 1-.666-.666v-.519L1.733 9.721a.867.867 0 0 1 .225-1.363l.082-.043a1.334 1.334 0 0 1 1.5.175l.753.66V3.866A1.13 1.13 0 0 1 5.82 2.81v4.797a.365.365 0 1 0 .73 0V2.625a.84.84 0 1 1 1.68 0ZM7.39.785c.699 0 1.307.39 1.618.964a1.987 1.987 0 0 1 2.062 1.264l.118-.003c1.097 0 1.986.889 1.986 1.986v4.648a5.87 5.87 0 0 1-1.46 3.874v.029c0 .92-.747 1.666-1.667 1.666H5.538c-.92 0-1.666-.746-1.666-1.666v-.12L1.008 10.41a1.867 1.867 0 0 1 .483-2.936l.082-.043a2.333 2.333 0 0 1 1.72-.182v-3.38a2.13 2.13 0 0 1 2.47-2.103A1.84 1.84 0 0 1 7.39.785Z" clip-rule="evenodd"></path></svg>`;
      button.style.display = 'flex';
      button.style.width = '22px';
      button.style.height = '22px';
      button.style.background = 'white';
      button.style.cursor = 'pointer';
      button.style.margin = '0px 3px 0px 3px';
      button.style.alignItems = 'center';
      button.style.justifyContent = 'center';
      button.onclick = () => {
        if (page.operationMode === PAGE_OPERATION_MODE.DRAG) {
          page.operationMode = PAGE_OPERATION_MODE.SELECTION;
        } else {
          page.operationMode = PAGE_OPERATION_MODE.DRAG;
        }
        me.update();
      };

      /**
       * 获取工具栏组件，用于展示
       *
       * @return {*}
       */
      me.getComponent = () => button;

      /**
       * 刷新拖拽工具的样式
       */
      me.update = () => {
        button.style.background = page.operationMode === PAGE_OPERATION_MODE.SELECTION ? 'white' : '#D9DCFA';
      };
      return me;
    };

    /**
     * 缩放工具
     *
     * @return {{}}
     */
    const zoomTool = () => {
      const me = {};

      const createZoomIn = () => {
        const button = graph.createDom(div, 'div', 'barToolsZoomIn', page.id);
        button.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 16 16"><path fill="#1D1C23" fill-opacity="0.8" d="M1.333 8c0-.368.299-.666.667-.666h12a.667.667 0 1 1 0 1.333H2a.667.667 0 0 1-.667-.666Z"></path></svg>`;
        button.style.display = 'flex';
        button.style.width = '22px';
        button.style.height = '22px';
        button.style.background = 'white';
        button.style.alignItems = 'center';
        button.style.margin = '0px 3px 0px 3px';
        button.onclick = () => {
          me.zoomTo(page.scaleX - 0.1);
        };
        return button;
      };

      const createZoomSlider = () => {
        const button = graph.createDom(div, 'input', 'barToolsZoomSlider', page.id);
        button.type = 'range';
        button.max = 100;
        button.min = 5;
        button.step = 5;
        button.value = 50;
        button.style.width = '80px';
        button.style.height = '22px';
        button.style.background = 'white';
        button.style.cursor = 'pointer';
        button.style.alignContent = 'center';
        button.style.margin = '3px';
        button.oninput = (value) => {
          me.sliderZoom(parseInt(button.value));
        };
        return button;
      };

      const createZoomText = () => {
        const button = graph.createDom(div, 'span', 'barToolsZoomText', page.id);
        button.style.width = '40px';
        button.style.height = '22px';
        button.style.background = 'white';
        button.style.alignContent = 'center';
        button.style.userSelect = 'none';
        button.style.margin = '3px';
        return button;
      };

      const createZoomOut = () => {
        const button = graph.createDom(div, 'div', 'barToolsZoomOut', page.id);
        button.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 16 16" style="align-items: center;"><path fill="#1D1C23" fill-opacity="0.8" d="M8 1.334a.667.667 0 0 0-.667.667v5.333H2a.667.667 0 0 0 0 1.333h5.333v5.334a.667.667 0 0 0 1.334 0V8.667H14a.667.667 0 1 0 0-1.333H8.667V2.001A.667.667 0 0 0 8 1.334Z"></path></svg>`;
        button.style.display = 'flex';
        button.style.width = '22px';
        button.style.height = '22px';
        button.style.background = 'white';
        button.style.alignItems = 'center';
        button.style.margin = '0px 3px 0px 3px';
        button.onclick = () => {
          me.zoomTo(page.scaleX + 0.1);
        };
        return button;
      };

      const zoomWrapper = graph.createDom(div, 'div', 'zoomWrapper', page.id);
      zoomWrapper.style.display = 'flex';
      zoomWrapper.style.alignItems = 'center';
      zoomWrapper.style.width = 'fit-content';
      zoomWrapper.style.height = 'fit-content';

      const zoomIn = createZoomIn();
      const zoomSlider = createZoomSlider();
      const zoomText = createZoomText();
      const zoomOut = createZoomOut();

      zoomWrapper.appendChild(zoomIn);
      zoomWrapper.appendChild(zoomSlider);
      zoomWrapper.appendChild(zoomText);
      zoomWrapper.appendChild(zoomOut);

      /**
       * 获取缩放工具组件
       * @return {*}
       */
      me.getComponent = () => zoomWrapper;

      /**
       * 缩放，内部方法
       *
       * @param scale
       */
      me.zoomTo = (scale) => {
        let centerX = page.width / page.scaleX / 2 - page.x;
        let centerY = page.height / page.scaleY / 2 - page.y;
        page.zoomTo(scale, scale, centerX, centerY);
        me.update();
      };

      /**
       * 刷新页面缩放比展示，内部方法
       */
      me.updateText = () => {
        zoomText.innerHTML = `${Math.round(page.scaleX * 100)}%`;
      };

      /**
       * 根据滑块计算缩放比，控制缩放，内部方法
       *
       * @param sliderValue
       */
      me.sliderZoom = (sliderValue) => {
        let scale;
        if (sliderValue <= 75) {
          scale = 1.0 - (50 - sliderValue) / 5 * 0.1;
        } else {
          scale = 1.5 + 5 * (sliderValue - 75) / 50;
        }
        me.zoomTo(scale);
      };

      /**
       * 响应外部变化，刷新滑块位置，内部方法
       */
      me.updateSlider = () => {
        let sliderValue;
        if (page.scaleX < 1.5) {
          sliderValue = 50 - (1.0 - page.scaleX) / 0.1 * 5;
        } else {
          sliderValue = (page.scaleX - 1.5) * 50 / 5 + 75;
        }
        zoomSlider.value = sliderValue;
      };

      /**
       * 更新缩放工具
       */
      me.update = () => {
        me.updateText();
        me.updateSlider();
      };

      me.update();
      return me;
    };

    const createSplitter = () => {
      const barSplitter = graph.createDom(div, 'div', 'barSplitter', page.id);
      barSplitter.style.width = '1px';
      barSplitter.style.height = '16px';
      barSplitter.style.background = 'lightGray';
      barSplitter.style.marginLeft = '5px';
      barSplitter.style.marginRight = '10px';
      barSplitter.style.alignSelf = 'center';
      return barSplitter;
    };

    const bars = {};
    bars.isDragging = false;
    bars.draggingBar = null;

    const barTools = graph.createDom(div, 'div', 'barTools', page.id);
    barTools.style.position = 'absolute';
    barTools.style.display = 'flex';
    barTools.style.alignItems = 'center';
    barTools.style.zIndex = 3;
    barTools.style.bottom = '20px';
    barTools.style.right = '20px';
    barTools.style.width = 'fit-content';
    barTools.style.height = 'fit-content';
    barTools.style.borderRadius = '12px';
    barTools.style.padding = '6px';
    barTools.style.boxShadow = '0 0 1px 0 rgba(0,0,0,.3),0 4px 14px 0 rgba(0,0,0,.1)';
    barTools.style.background = 'white';

    self.sensor.appendChild(barTools);
    let drag = dragTool();
    barTools.appendChild(drag.getComponent());

    let fit = fitTool();
    barTools.appendChild(fit.getComponent());

    const barSplitter = createSplitter();
    barTools.appendChild(barSplitter);

    let zoom = zoomTool();
    barTools.appendChild(zoom.getComponent());

    /**
     * 工具栏对外的更新方法，更新拖拽、缩放信息
     */
    bars.update = () => {
      if (!page.moveable || !page.canvasMoveAble) {
        barTools.style.display = 'none';
        return;
      }
      drag.update();
      zoom.update();
    };

    /**
     * 是否显示的开关
     *
     * @param isShow
     */
    bars.show = isShow => {
      barTools.style.display = isShow ? 'flex' : 'none';
    };
    return bars;
  })();

  /**
   * 闭包构造一个滚动条
   *
   * @type {{}}
   */
  self.scrollbar = (() => {
    const barSize = '10px';
    const barColor = 'lightgray';

    const bars = {};

    const bar = (type) => {
      let me = {};
      me.dragStartX = 0;
      me.dragStartY = 0;
      let container;

      me.init = () => {
        container = graph.createDom(div, 'div', `${type}Container`, page.id);
        container.style.position = 'absolute';
        container.style.display = 'block';
        container.style.zIndex = 3;
        me.containerStyle(container);
        me.container = container;
        self.sensor.appendChild(container);

        const mask = graph.createDom(div, 'div', `${type}ScrollbarMask`, page.id);
        mask.style.position = 'fixed';
        mask.style.width = '100%';
        mask.style.height = '100%';
        mask.style.left = 0;
        mask.style.top = 0;
        mask.style.display = 'none';
        mask.style.cursor = 'pointer';
        container.appendChild(mask);

        const scrollbar = graph.createDom(div, 'div', `${type}Scrollbar`, page.id);
        scrollbar.style.background = barColor;
        scrollbar.style.borderRadius = '12px';
        scrollbar.style.cursor = 'pointer';
        scrollbar.style.position = 'absolute';
        scrollbar.style.display = 'none';
        me.barStyle(scrollbar);
        me.scrollbar = scrollbar;
        container.appendChild(scrollbar);

        const handleMouseEnter = (e) => {
          me.mouseEnter = true;
          scrollbar.style.display = 'block';
          bars.update();
        };

        const handleMouseLeave = (e) => {
          me.mouseEnter = false;
          if (bars.isDragging) {
            return;
          }
          scrollbar.style.display = 'none';
        };

        const handleMouseDown = (e) => {
          bars.isDragging = true;
          bars.draggingBar = me;
          mask.style.display = 'block';
          me.dragStartX = e.clientX;
          me.dragStartY = e.clientY;
          e.stopPropagation();
          e.preventDefault();
        };

        const handleMouseUp = (e) => {
          bars.isDragging = false;
          bars.draggingBar = null;
          mask.style.display = 'none';
          if (!me.mouseEnter) {
            scrollbar.style.display = 'none';
          }
          e.stopPropagation();
          e.preventDefault();
        };

        const handleMouseMove = (e) => {
          if (!bars.isDragging) {
            return;
          }
          me.move(e);
          e.stopPropagation();
          e.preventDefault();
        };

        scrollbar.addEventListener('mousedown', (e) => handleMouseDown(e));
        scrollbar.addEventListener('mousemove', (e) => handleMouseMove(e));
        scrollbar.addEventListener('mouseup', (e) => handleMouseUp(e));

        container.addEventListener('mouseenter', (e) => handleMouseEnter(e));
        container.addEventListener('mouseleave', (e) => handleMouseLeave(e));

        mask.addEventListener('mousedown', (e) => handleMouseUp(e));
        mask.addEventListener('mousemove', (e) => handleMouseMove(e));
        mask.addEventListener('mouseup', (e) => handleMouseUp(e));
      };

      /**
       * 扩展API，用于设置bar容器的样式
       *
       * @param container scrollbar的父容器
       */
      me.containerStyle = (container) => {
      };
      /**
       * 扩展API，用于设置scrollbar的样式
       *
       * @param scrollbar 自身
       */
      me.barStyle = (scrollbar) => {
      };
      /**
       * 扩展API，用于更新bar
       *
       * @param begin 页面内容的开始位置
       * @param end 页面内容的结束位置
       */
      me.update = (begin, end) => {
      };
      /**
       * 扩展API，用于移动bar
       *
       * @param e
       */
      me.move = (e) => {
      };
      /**
       * 控制bar是否展示
       *
       * @param isShow
       */
      me.show = isShow => {
        container.style.display = isShow ? 'block' : 'none';
      };
      return me;
    };

    const hBar = (expand) => {
      const me = bar('horizontal');
      me.containerStyle = (container) => {
        container.style.width = `${div.clientWidth}px`;
        container.style.height = barSize;
        container.style.bottom = 0;
      };
      me.barStyle = (scrollbar) => {
        scrollbar.style.width = `${(div.clientWidth / 3)}px`;
        scrollbar.style.height = barSize;
      };
      me.update = (x1, x2) => {
        let max = -x1 + expand / page.scaleX;
        let min = -x2;
        let percent = 1 - (page.x - min) * 1.0 / (max - min);
        let left = (me.container.clientWidth - me.scrollbar.clientWidth) * percent;
        me.scrollbar.style.left = `${left}px`;
      };
      me.move = (e) => {
        let deltaX = e.clientX - me.dragStartX;
        let scrollLeft = parseFloat(me.scrollbar.style.left);
        if (!scrollLeft || isNaN(scrollLeft)) {
          scrollLeft = 0;
        }
        scrollLeft = Math.max(0, scrollLeft + deltaX);
        let percent = scrollLeft / (me.container.clientWidth - me.scrollbar.clientWidth);

        let frame = page.getShapeFrame();
        let x1 = frame.x1;
        let x2 = frame.x2;
        let max = -x1 + expand / page.scaleX;
        let min = -x2;
        let newX = (1 - percent) * (max - min) + min;
        page.moveTo(newX, page.y);
        me.update(x1, x2);
        me.dragStartX = e.clientX;
      };
      me.init();
      return me;
    };

    let vBar = (expand) => {
      const me = bar('vertical');
      me.containerStyle = (container) => {
        container.style.width = barSize;
        container.style.height = `${div.clientHeight}px`;
        container.style.right = 0;
      };
      me.barStyle = (scrollbar) => {
        scrollbar.style.width = barSize;
        scrollbar.style.height = `${(div.clientHeight / 3)}px`;
      };
      me.update = (y1, y2) => {
        let max = -y1 + expand / page.scaleY;
        let min = -y2;
        let percent = 1 - (page.y - min) * 1.0 / (max - min);
        let top = (me.container.clientHeight - me.scrollbar.clientHeight) * percent;
        me.scrollbar.style.top = `${top}px`;
      };
      me.move = (e) => {
        let deltaY = e.clientY - me.dragStartY;
        let scrollTop = parseFloat(me.scrollbar.style.top);
        if (!scrollTop || isNaN(scrollTop)) {
          scrollTop = 0;
        }
        scrollTop = Math.max(0, scrollTop + deltaY);
        let percent = scrollTop / (me.container.clientHeight - me.scrollbar.clientHeight);

        let frame = page.getShapeFrame();
        let y1 = frame.y1;
        let y2 = frame.y2;
        let max = -y1 + expand / page.scaleY;
        let min = -y2;
        let newY = (1 - percent) * (max - min) + min;
        page.moveTo(page.x, newY);
        me.update(y1, y2, expand);
        me.dragStartY = e.clientY;
      };
      me.init();
      return me;
    };

    bars.hBar = hBar(page.div.clientWidth);
    bars.vBar = vBar(page.div.clientHeight);

    /**
     * 更新滚动条
     */
    bars.update = () => {
      // 根据内容和视口大小调整滚动条
      const frame = page.getShapeFrame();
      bars.hBar.update(frame.x1, frame.x2);
      bars.vBar.update(frame.y1, frame.y2);
    };

    /**
     * 控制滚动条是否展示
     * @param isShow
     */
    bars.show = isShow => {
      bars.hBar.show(isShow);
      bars.hBar.show(isShow);
    };
    return bars;
  })();

  const cursorId = (p) => {
    return `cursor:${p.id}`;
  };

  self.cursor = (() => {
    const id = cursorId(page);

    // 如果不是父子关系，那么可能是display. 此时需要创建新的dom.
    return graph.createDom(div, 'canvas', id, page.id);
  })();

  self.pageIdChange = () => {
    graph.setElementId(self.sensor, sensorId(page));
    graph.setElementId(self.cursor, cursorId(page));
    graph.setElementId(self.selection, selectionId(page));
  };

  self.reset = () => {
    self.sensor.style.width = '100%';
    self.sensor.style.height = '100%';

    // 当图形被拖出画布时，可以出现滚动条.
    self.sensor.style.overflow = 'hidden';
    self.sensor.style.position = 'absolute';
    self.selection.style.position = 'absolute';
    let canMove = page.moveAble && page.canvasMoveAble;
    self.positonBar.show(canMove);
    self.scrollbar.show(canMove);
    const size = CURSOR_DEFAULT_SIZE;
    if (self.cursor.width !== size || self.cursor.height !== size) {
      self.cursor.width = self.cursor.height = size;
    }
    self.cursor.style.position = 'absolute';
    const context = self.cursor.getContext('2d');
    const scale = page.scaleX > 1 ? page.scaleX : 1;
    self.pixelRate = pixelRateAdapter(context, scale, scale);
    drawCursor(size / 2, context);
  };

  const drawCursor = (r, context) => {
    context.clearRect(0, 0, context.canvas.width, context.canvas.height);
    context.beginPath();
    context.fillStyle = 'lightgray';
    context.rect(r - 2, r - 2, 4, 4);
    context.fill();
    context.beginPath();
    context.fillStyle = 'steelBlue';
    let shape = null;
    if (page.isMouseDown) {
      shape = page.isMouseDown() ? page.mousedownShape : page.mouseInShape;
    }
    self.sensor.style.cursor = cursorDrawer.draw(context, r, r, page.cursor, page, shape);
  };

  self.drawDynamic = (x, y) => {
    const r = CURSOR_DEFAULT_SIZE / 2;
    if (!page.showCursor()) {
      self.cursor.style.visibility = 'hidden';
      return;
    } else {
      self.cursor.style.visibility = 'visible';
    }
    self.cursor.style.left = `${(x - r)}px`;
    self.cursor.style.top = `${(y - r)}px`;
    if (self.cursor.current !== page.cursor) {
      self.cursor.current = page.cursor;
      drawCursor(r, self.cursor.getContext('2d'));
    }
  };

  self.drawSelection = (x, y) => {
    if (ifHideSelection()) {
      self.selection.style.visibility = 'hidden';
      self.selection.style.width = self.selection.style.height = '1px';
      return;
    }
    let ox = (page.mousedownx + page.x) * page.scaleX;
    let oy = (page.mousedowny + page.y) * page.scaleY;

    if ((x - ox) !== 0 && (y - oy) !== 0) {
      self.selection.style.left = `${(x > ox ? ox : x)}px`;
      self.selection.style.top = `${(y > oy ? oy : y)}px`;
      self.selection.style.width = `${Math.abs(x - ox)}px`;
      self.selection.style.height = `${Math.abs(y - oy)}px`;
      self.selection.style.zIndex = page.maxIndex();
      self.selection.style.visibility = 'visible';
    }
  };

  const ifHideSelection = () => {
    return !page.isMouseDown() || page.ifHideSelection() || (page.mousedownShape !== page) || page.handAction();
  };

  self.draw = (position) => {
    let positionVal = position;
    if (positionVal === undefined || positionVal === null) {
      positionVal = {x: page.mousex, y: page.mousey};
    }
    let x = (positionVal.x + page.x) * page.scaleX;
    let y = (positionVal.y + page.y) * page.scaleY;
    self.cursor.style.zIndex = page.maxIndex() + 11;
    self.positonBar.update();
    if (page.operationMode === PAGE_OPERATION_MODE.SELECTION) {
      self.drawSelection(x, y);
    } else {
      self.scrollbar.update();
    }
    self.drawDynamic(x, y);
  };

  self.getInteract = () => self.sensor;
  self.reset();
  return self;
};

export {interactDrawer};