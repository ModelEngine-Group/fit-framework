/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

export const shapeFields = {
  get: type => {
    const fields = all[type];
    return fields ? fields : [];
  },
};
const all = {};
all.shape = ['id', 'x', 'y', 'width', 'height', 'backAlpha', 'globalAlpha', 'cornerRadius', 'dashWidth', 'margin', 'focusMargin', 'pad', 'tag', 'tagPattern', 'lineHeight', 'background', 'isPlaceHolder',
  'borderWidth', 'focusBorderWidth', 'text', 'description', 'font', 'rotateDegree', 'autoHeight', 'autoWidth', 'borderColor', 'focusBorderColor', 'mouseInBorderColor', 'mouseInFontColor', 'mouseInBackColor', 'backColor', 'headColor', 'wordSpacing',
  'letterSpacing', 'focusBackColor', 'focusFontColor', 'fontSize', 'fontFace', 'fontColor', 'fontWeight', 'fontStyle', 'hAlign', 'textAlign', 'vAlign', 'container', 'type', 'enableAnimation', 'hideText', 'shadow', 'focusShadow', 'shadowData', 'outstanding',
  'shadowColor', 'loadCode', 'clickCode', 'editingCode', 'editedCode', 'numberPressedCode', 'keyPressedCode', 'selectedCode', 'unSelectedCode', 'beforeRemoveCode', 'afterRemovedCode', 'resizedCode', 'movedCode', 'focusedCode',
  'unfocusedCode', 'containerChangedCode', 'textChangedCode', 'draggingCode', 'mouseUpCode', 'mouseDownCode', 'mouseMoveCode', 'draggingCode', 'animateCode', 'padLeft', 'padRight', 'padTop', 'padBottom', 'pDock', 'ignoreDock',
  'dynamicCode', 'ignorePageMode', 'visible', 'index', 'rotateAble', 'selectable', 'resizeable', 'deletable', 'moveable', 'dragable', 'deleteFromShare', 'readOnly', 'tags', 'properties', 'local', 'entangleIdE', 'title', 'desc',
  'bold', 'italic', 'strikethrough', 'underline', 'numberedList', 'bulletedList', 'allowLink', 'scrollLock', 'referenceId', 'namespace', 'textInnerHtml', 'version', 'minHeight'];

all.rectangle = ['emphasized', 'priority', 'infoType', 'progressStatus', 'progressPercent', 'showedProgress', 'emphasizeType', 'shineColor1', 'shineColor2', 'assignTo', 'shared', 'sharedBy'];

all.container = ['dockMode', 'dockAlign', 'itemPad', 'itemSpace', 'itemScroll', 'division'];

all.page = ['background', 'editable', 'timerCode', 'loadCode',
  'animationCode', 'bulletSpeed', 'shapesAs', 'displayPageNumber', 'displayDateTime',
  'displayHeader', 'displayFooter'];

// 全部删除以前的属性
all.page.delete = ['readOnly', 'width', 'height', 'x', 'y'];


all.line = ['fromShape', 'toShape', 'definedFromConnector', 'definedToConnector', 'endpointOffsetX', 'endpointOffsetY',
  'lineMode', 'curvePoint1', 'curvePoint2', 'beginArrow', 'beginArrowEmpty', 'endArrow', 'endArrowEmpty', 'beginArrowSize',
  'endArrowSize', 'lineWidth', 'borderColor', 'textX', 'textY', 'brokenPoints', 'arrowBeginPoint', 'arrowEndPoint',
  'allowSwitchLineMode'];

all.ellipse = ['lineWidth'];

all.grid = ['cells', 'rows', 'columns', 'borders', 'changes', 'groups', 'frozens', 'hiddens',
  'headBorderColor', 'headBackColor', 'headTextColor', 'headWidth', 'headHeight', 'cellWidth', 'cellHeight', 'minWidth',
  'minRow', 'minColumn', 'maxRow', 'maxColumn', 'data', 'withColumnHead', 'withRowHead', 'wrap'];


all.freeLine = ['lines', 'closed'];
all.freeLine.delete = ['x', 'y', 'width', 'height'];

all.reference = ['autoFit', 'keepOrigin', 'readOnly', 'referenceShape', 'referencePage', 'referenceData'];

all.vector = ['originWidth', 'originHeight', 'drawDynamicCode'];

all.svg = ['lines', 'originWidth', 'originHeight', 'lineWidth'];

all.icon = ['iconName'];

all.video = all.image = ['src'];

all.chart = ['cells', 'grid', 'range'];

all.htmlRadioBox = ['checked'];

all.htmlTree = ['nodeSpace'];
all.htmlTreeNode = ['parentNode', 'level', 'checked'];

all.node = ['triggerMode', 'runningTask', 'warningTask', 'completedTask', 'joberFilter', 'callback'];
all.state = ['task', 'jober', 'taskFilter', 'retryNum'];
all.event = ['conditionRule'];
all.start = [];
all.start.delete = ['width', 'height'];
all.aippState = ['task', 'isAgent', 'jober', 'taskFilter', 'retryNum', 'tags'];
all.aippEnd = ['callback', 'task', 'jober'];
all.aippStart = ['callback', 'task', 'jober'];

all.mind = ['mode'];
all.topic = ['status', 'direction', 'root', 'attaches', 'parent'];

all.presentation = ['isTemplate', 'basePage', 'animations', 'inMethod', 'outMethod', 'geoScale', 'isTerminal'];
all.presentationPage = ['isTemplate', 'basePage', 'animations', 'inMethod', 'outMethod', 'geoScale', 'isTerminal'];
all.frame = ['pros', 'cons', 'socialCode'];
all.frame.delete = ['x', 'y', 'borderColor'];

all.dependency = ['fromFitable'];
all.umlInterface = ['isExpanded'];

all.percent = ['value', 'qulifiedValue', 'goodValue', 'direction'];

//--------------------------deprecated 2023 huizi------------------------------
all.caption = ['agendaIndex'];

all.docPage = ['docWidth', 'minDocHeight'];

all.document = ['paragraphStyles', 'bodyStyle', 'pageId', 'docWidth'];

all.docSection = ['format'];

all.tableStatic = ['showTitle', 'showHead', 'showSequence', 'lightCellColor', 'heavyCellColor', 'decorateCode', 'adaptWidth', 'adaptHeight', 'showLock', 'showSort'];

all.column = ['tIndex', 'cellFontColor', 'cellFontSize', 'cellFontWeight'];

all.cell = ['tIndex'];

all.elsaLog = ['xoffset', 'yoffset'];
