/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 文本对齐，混搭了水平对齐和垂直对齐
 * 辉子
 */
export const ALIGN = {
  LEFT: 'left',
  RIGHT: 'right',
  TOP: 'top',
  BOTTOM: 'bottom',
  MIDDLE: 'center',
  FILL: 'fill',
  NONE: 'none',
};

/**
 * 文本是正常的，还是斜体的
 * 【style，weight等文本特性适用于整体文本，文本中的个别选中可以有自己的格式，这些格式有innerhtml控制，不受elsa 管理】
 * 辉子
 */
export const FONT_STYLE = {
  NORMAL: 'normal', ITALIC: 'italic',
};

/**
 * 文本是粗体还是正常体
 * 辉子
 */
export const FONT_WEIGHT = {
  BOLD: 'bold', LIGHTER: 'lighter', NORMAL: 'normal',
};

/**
 * 鼠标样式：有些样式用系统样式，有些样式elsa绘制
 * 辉子
 */
export const CURSORS = {
  DEFAULT: 'default',
  NONE: 'none',
  POINTER: 'pointer',
  MOVE: 'move',
  CROSSHAIR: 'crosshair',
  HAND: 'hand',
  PRESENTATION: 'presentation',
  PEN: 'pen',
  ERASER: 'eraser',
  TEXT: 'text',
  GRAB: 'grab',
  GRABBING: 'grabbing',
};

/**
 * 容器内shapes的排列方式
 * 辉子
 */
export const DOCK_MODE = {
  VERTICAL: 'vertical', // 纵向排列，横向撑满
  HORIZONTAL: 'horizontal', // 横向排列，纵向撑满
  FILL: 'fill', // 横向，纵向都撑满，适用于容器里只有一个shape
  NONE: 'none',
};

/**
 * shape在容器里的排列方式，与dock各司其职
 * 辉子
 */
export const PARENT_DOCK_MODE = {
  LEFT: 'left',
  RIGHT: 'right',
  TOP: 'top',
  BOTTOM: 'bottom',
  FILL: 'fill',
  NONE: 'none',
};

/**
 * connector方向，比如矩形左下角的拉动形成的鼠标方向是SW，右下角是SE
 * 辉子
 */
export const DIRECTION = {
  W: {cursor: 'ew-resize', key: 'W', color: 'whitesmoke', ax: 'x', vector: -1, value: 'W'},
  N: {cursor: 'ns-resize', key: 'N', color: 'whitesmoke', ax: 'y', vector: -1, value: 'N'},
  E: {cursor: 'ew-resize', key: 'E', color: 'whitesmoke', ax: 'x', vector: 1, value: 'E'},
  S: {cursor: 'ns-resize', key: 'S', color: 'whitesmoke', ax: 'y', vector: 1, value: 'S'},
  NE: {cursor: 'nesw-resize', key: 'NE', color: 'whitesmoke'},
  SW: {cursor: 'nesw-resize', key: 'SW', color: 'whitesmoke'},
  SE: {cursor: 'nwse-resize', key: 'SE', color: 'whitesmoke'},
  NW: {cursor: 'nwse-resize', key: 'NW', color: 'whitesmoke'},
  L: {cursor: 'crosshair', key: 'LINE', color: 'white'},
  T: {cursor: 'crosshair', key: 'TEXT ', color: 'lightgreen'},
  R: {cursor: 'pointer', key: 'ROTATE', color: 'yellow'},
  NC: {cursor: 'n-clip', key: 'NC', color: 'whitesmoke'},
  SC: {cursor: 's-clip', key: 'SC', color: 'whitesmoke'},
  EC: {cursor: 'e-clip', key: 'EC', color: 'whitesmoke'},
  D: {cursor: 'hand', key: 'D', color: 'red'},
  WC: {cursor: 'w-clip', key: 'WC', color: 'whitesmoke'},
  V: {cursor: 'ns-resize', key: 'V', color: 'lightyellow'},
  H: {cursor: 'ew-resize', key: 'H', color: 'lightyellow'},
  ROW: {cursor: 'row-resize', key: 'ROW', color: 'lightyellow'},
  COL: {cursor: 'col-resize', key: 'COL', color: 'lightyellow'},
  NONE: {cursor: 'not-allowed', key: 'none', color: 'green'},
};

/**
 * 线模式：直线，曲线，折线，用户不可修改曲度的自动绘制曲线（最难搞的就是线了）
 * 辉子
 */
export const LINEMODE = {
  STRAIGHT: {type: 'straight', next: () => LINEMODE.AUTO_CURVE},
  CURVE: {type: 'curve', next: () => LINEMODE.STRAIGHT},
  BROKEN: {type: 'broken', next: () => LINEMODE.CURVE},
  AUTO_CURVE: {type: 'auto_curve', next: () => LINEMODE.BROKEN},
};

/**
 * 全局唯一的编辑器id：任何shape进行编辑，都会唤起该编辑器
 * 辉子
 */
export const EDITOR_NAME = 'elsa-editor';

/**
 * 全局规定的connector尺寸，就是resize拉到的那个小方框的大小
 * 辉子
 */
export const CONNECTION_RADIUS = 12;

/**
 * 键盘移动一下shape的最小位移常量
 * 辉子
 */
export const SHAPE_MOVE_STEP = 1;

/**
 * 页面模式：配置模式，运行模式，演示模式
 * 辉子
 */
export const PAGE_MODE = {
  CONFIGURATION: 'configuration',
  DISPLAY: 'display',
  PRESENTATION: 'presentation',
  VIEW: 'view',
  RUNTIME: 'runtime',
  HISTORY: 'history',
};

/**
 * 全局常量：shape最长宽度
 * 辉子
 */
export const MIN_WIDTH = 2.1;
/**
 * esla所有shape的z-index基准值
 * 辉子
 */
export const Z_INDEX_OFFSET = 100;

/**
 * 每个shape都有几个自动的hitregion，info type是信息hitregion，在shape右上角绘制：信息，警告，错误
 * 辉子
 */
export const INFO_TYPE = {
  INFORMATION: {next: 'WARNING', name: 'information'},
  WARNING: {next: 'ERROR', name: 'warning'},
  ERROR: {next: 'NONE', name: 'error'},
  NONE: {next: 'INFORMATION', name: 'none'},
};

/**
 * 每个shape默认自动的进度hitregion，状态值有：......下面都可以读懂
 * 辉子
 */
export const PROGRESS_STATUS = {
  NOTSTARTED: {next: 'DOING', color: 'gray', name: 'NOTSTARTED'},
  DOING: {next: 'RUNNING', color: 'gray', name: 'DOING'},
  RUNNING: {next: 'PAUSE', color: 'lightseagreen', name: 'RUNNING'},
  PAUSE: {next: 'COMPLETE', color: 'salmon', name: 'PAUSE'},
  COMPLETE: {next: 'ERROR', color: 'green', name: 'COMPLETE'},
  ERROR: {next: 'NONE', color: 'red', name: 'ERROR'},
  NONE: {next: 'UNKNOWN', color: 'gray', name: 'NONE'},
  UNKNOWN: {next: 'NOTSTARTED', color: 'dimgray', name: 'UNKNOWN'},
};

/**
 * 脑图的显示模式
 * 辉子
 */
export const MIND_MODE = {
  MIND: 'mindCenter', ORG: 'organization', MINDLEFT: 'mindLeft', MINDRIGHT: 'mindRight',
};

/**
 * mind附件类型
 */
export const ATTACHED_THEME = {ATTACHED: 'attached', COMMENTS: 'comments'};
/**
 * xmind某个节点是否展开
 */
export const MIND_ITEM_STATUS = {
  EXPANDED: 'expanded', COLLAPSED: 'collapsed',
};

/**
 * xmind节点朝向，向左，向右，或者向下
 */
export const MIND_ITEM_DIRECTION = {
  LEFT: 'left', RIGHT: 'right', BOTTOM: 'bottom', CENTER: 'center',
};

/**
 * xmind节点高度
 */
export const MIND_ITEM_HEIGHT = 25;

/**
 * container排列shape，在vertical和horizontal情况下可以划分几列
 * 辉子
 */
export const DIVISION = {
  NONE: -1, TOW: 2, FOUR: 4, SIX: 6,
};

/**
 * presentation animation action:in or out
 * 辉子
 */
export const ANIMIATION_ACTION = {IN: 'in', OUT: 'out', CODE: 'code'};
/**
 * presentation animation trigger type: click trigger or follow last shape animation
 * 辉子
 */
export const ANIMATION_TRIGGER = {FOLLOW: 'follow', CLICK: 'click'};
/**
 * 文档里是段落还是正文
 */
export const DOCUMENT_FORMAT = {DOCUMENT: 'document', PARAGRAPH: 'paragraph', BODY: 'body', PAINTING: 'painting'};

export const FLOWABLE_STATE_STATUS = {
  PENDING: {color: ''}, NOT_STARTED: {color: 'lightgray'}, RUNNING: {color: 'steelblue'}, DONE: {color: 'green'},
};
export const FLOWABLE_TRIGGER_MODE = {
  AUTO: 'auto', MANUAL: 'manual', SCHEDULE: 'schedule',
};

export const FLOWABLE_PARALLEL_MODE = {
  ALL: 'all', EITHER: 'either',
};

export const EVENT_TYPE = {
  GRAPH_LOADED: 'graphLoaded',
  EDITOR_SELECTION_CHANGE: 'editorSelectionChange',
  FOCUSED_SHAPE_CHANGE: 'focusedShapeChange',
  CONTEXT_CREATE: 'contextCreate',
  SHAPE_ADDED: 'shapeAdded',
  FOCUSED_SHAPES_CHANGE: 'focusedShapesChange',
  SHAPE_MOVED: 'shapeMoved',
  SHAPE_MOVING: 'shapeMoving',
  SHAPE_RESIZED: 'shapeResized',
  SHAPE_RESIZING: 'shapeResizing',
  SHAPE_LONG_CLICK: 'shapeLongClick',
  PAGE_LONG_CLICK: 'pageLongClick',
  PAGE_HISTORY: 'pageHistory',
  PAGE_DIRTY: 'pageDirty',
  TOUCH_START: 'touchStart',
  TOUCH_END: 'touchEnd',
  FOCUSED_PAGE_CHANGE: 'focusedPageChange',
  REGION_CLICK: 'regionClick',
  // Aipp流程编排相关事件
  INSERT_NODE_REGION_CLICKED: 'insertNodeRegionClick',
  FLOWABLE_STATE_TYPE_CHANGE: 'flowableStateTypeChange',
  ERROR_OCCURRED: 'errorOccurred',
};

export const SHAPE_HIT_TYPE = {
  CONNECTOR: 'connector',
  BACK: 'back',
  BORDER: 'border',
  TEXT: 'text',
  REGION: 'region',
};

export const SELECTION_STRATEGY = {
  BRING_TO_FRONT: 0,
  HIGHLIGHT_BOUNDARIES: 1,
};

export const GRID_CELL_WIDTH = 100;
export const GRID_CELL_HEIGHT = 22;
export const GRID_TOOL_WIDTH = 20;
export const GRID_HEAD_WIDTH = 46;
export const GRID_HEAD_HEIGHT = 24;

export const TEXT_ATTRIBUTES = {
  BOLD: 'bold',
  ITALIC: 'italic',
  STRIKETHROUGH: 'strikethrough',
  UNDERLINE: 'underline',
  BACK_COLOR: 'fontBackgroundColor',
  FONT_COLOR: 'fontColor',
  FONT_FACE: 'fontFace',
  FONT_SIZE: 'fontSize',
  NUMBERED_LIST: 'numberedList',
  BULLETED_LIST: 'bulletedList',
};

export const ELSA_NAME_SPACE = 'modelengine.fit.elsa';

export const DEFAULT_FOLLOW_BAR_OFFSET = -60;

export const PAGE_OPERATION_MODE = {
  SELECTION: 'selection',
  DRAG: 'drag',
};
