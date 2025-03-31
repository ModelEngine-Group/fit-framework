/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, DIVISION, DOCK_MODE, PARENT_DOCK_MODE} from '../common/const.js';

import {rectangle} from './rectangle.js';
import {containerDrawer} from './drawers/containerDrawer.js';

/**
 * 容器
 */
const container = (id, x, y, width, height, parent, drawer, initializing) => {
    const self = rectangle(id, x, y, width, height, parent, drawer === undefined ? containerDrawer : drawer);
    self.type = "container";
    self.dockAlign = ALIGN.TOP;
    self.division = DIVISION.NONE;// 摆放均匀分几列
    self.ifMaskItems = true;
    self.selectDisableArea = {
        x: 0, y: 0, getWidth: function () {
            return 0;
        }, getHeight: function () {
            return 0;
        }
    };// 不能选择区域，在这个区域container不能选择，为方便选择子shape
    self.dynamicAddItem = true;// 允许拖动shape到该container
    self.itemSpace = 5;// 子控件间距
    self.itemPad = [6, 6, 6, 6];// children pad to the border
    self.itemScroll = { x: 0, y: 0 };// children scroll offset
    self.text = "container";
    self.hideText = true;// dont display text;
    self.childAllowed = child => {
        return self.dynamicAddItem || !child.inDragging;
    };
    self.scrollAble = false;// 是否可以滚动
    self.scaleX = self.scaleY = 1;
    self.autoFit = false;

    self.scale = (scaleX, scaleY) => {
      let scaleXVal = scaleX;
      let scaleYVal = scaleY;
      if (scaleXVal === undefined) {
            return;
        }
      if (scaleYVal === undefined) {
        scaleYVal = scaleXVal;
        }

      if (scaleXVal > 5) {
        scaleYVal = 5;
        }
      if (scaleYVal > 5) {
        scaleYVal = 5;
        }

      self.scaleX = scaleXVal;
      self.scaleY = scaleYVal;

        self.drawer.transform && self.drawer.transform();
    };

    self.getEditRect = () => {
        return {
            x: self.x + self.borderWidth,
            y: self.y + self.borderWidth,
            width: self.width - self.getPadLeft() - self.getPadRight() - 2 * self.borderWidth,
            height: self.itemPad[2] - self.borderWidth - self.getPadTop() - self.getPadBottom()
        };
    };

    let remove = self.remove;
    self.remove = source => {
        const removed = [self];
        self.getShapes().forEach(s => {
            removed.push.apply(removed, s.remove(source));
        });
        remove.call(self, source);
        return removed;
    };

    self.getShapesArea = () => {
        let itemPad = self.itemPad;
        return {
            x: self.x + itemPad[0],
            y: self.y + itemPad[2],
            width: self.width - itemPad[0] - itemPad[1],
            height: self.height - itemPad[2] - itemPad[3]
        };
    };

    function _getShapes(condition) {
      let conditionVal = condition;
      (!conditionVal) && (conditionVal = s => true);
      return self.page.sm.getShapes(s => s.container === self.id && s !== self && conditionVal(s));
    }

    /**
     * 从page中得到所有父容器为该容器的shape列表
     * shape以index顺序排列
     */
    self.getShapes = (condition) => {
        return _getShapes(condition).orderBy(s => s.getIndex());
    };

    self.isMyBlood = item => {
        let parent = item;
        if (parent === self.page) {
            return false;
        }
        while (parent !== self) {
            parent = parent.getContainer();
            if (parent === self.page) {
                return false;
            }
        }
        return true;
    };

    let moveTo = self.moveTo;
    self.moveTo = (x, y, after) => {
        let oldx = self.x || 0;
        let oldy = self.y || 0;
        if (!moveTo.call(self, x, y, after)) {
            return false;
        }
        let deltaX = self.x - oldx;
        let deltaY = self.y - oldy;
        self.getShapes().forEach(s => {
            s.parentMoving = true;
            s.moveTo(s.x + deltaX, s.y + deltaY, after);
            delete s.parentMoving;
        });
        return true;
    };

    let invalidate = self.invalidate;
    self.invalidate = () => {
        if (self.drawer === undefined || !self.drawer.parent.id) {
            self.invalidateAlone();
        }

        // 如果是autoFit，那么先处理图形，再处理容器；否则，先处理容器，再处理图形。
        // 子容器reset之后，其父容器reset时修改了子容器的宽高，但子容器因为已reset过，无法再触发reset进行复位，导致画出的图形和容器大小不匹配
        if (self.autoFit) {
            self.getShapes().forEach(s => s.invalidate());
            self.arrangeShapes();
        } else {
            self.arrangeShapes();
            self.getShapes().forEach(s => s.invalidate());
        }
        invalidate.apply(self);
    };

    let reset = self.reset;
    self.reset = () => {
        // @maliya 和辉哥讨论后，在container reset时主动更新画布分区索引，方便容器中线条能及时找到父亲，避免发生变更线条父亲的错误。这是个临时修改方案，我认为容器在移动时，容器中的图形不应该去检测并修改父亲。
        self.indexCoordinate();

        // 如果是autoFit，那么先处理图形，再处理容器；否则，先处理容器，再处理图形。
        // 子容器reset之后，其父容器reset时修改了子容器的宽高，但子容器因为已reset过，无法再触发reset进行复位，导致画出的图形和容器大小不匹配
        if (self.autoFit) {
            self.getShapes().forEach(s => s.reset());
            self.arrangeShapes();
        } else {
            self.arrangeShapes();
            self.getShapes().forEach(s => s.reset());
        }
        reset.apply(self);
    }

    self.onConnectorDragged = connector => {
        if (connector === self.rotateDegree) {
            return;
        }
        if (self.dockMode !== DOCK_MODE.NONE || self.getShapes().find(s => s.pDock !== PARENT_DOCK_MODE.NONE)) {
            self.arrangeShapes();
        }
    };

    self.isAllowChildDrag = () => {
        return self.isChildDragable() || self.isDragableByChild();
    }

    self.isChildDragable = () => {
        return self.dockMode === DOCK_MODE.NONE || self.childDragable;
    }

    self.isDragableByChild = () => {
        return self.dockMode === DOCK_MODE.NONE || self.dragableByChild;
    }

    let isInDragging = self.isInDragging;
    self.isInDragging = () => {
        let inDragging = isInDragging.apply(self);
        if (inDragging) {
            return true;
        }
        // 没有设置dock模式，那就直接返回了
        if (DOCK_MODE.NONE === self.dockMode) {
            return false;
        }
        // 有对齐模式的情况下，看看孩子是否有在拖动中的
        return self.getShapes().exist(s => s.isInDragging());
    }

    self.hasChildFocused = () => {
        return self.getShapes().some(s => s.isFocused || (s.hasChildFocused && s.hasChildFocused()));
    }

    self.scroll = (deltaX, deltaY) => {
        if (!self.scrollAble) {
            return;
        }
        switch (self.dockMode) {
            case DOCK_MODE.HORIZONTAL:
                self.itemScroll.x += deltaX;
                break;
            case DOCK_MODE.VERTICAL:
                self.itemScroll.y += deltaY;
                break;
            case DOCK_MODE.NONE:
                self.itemScroll.x += deltaX;
                self.itemScroll.y += deltaY;
                break;
            default:
                break;
        }
        self.page.scrolling = true;
        self.invalidate();
        self.page.scrolling = false;
    };

    /**
     * 什么都不做，图形自由排布.
     */
    self.none = () => {};

    /**
     * 垂直排布图形.
     */
    self.vertical = () => {
        const area = self.getShapesArea();
        let lastX = area.x;
        let lastY = area.y;
        let totalHeight = self.itemPad[2] + self.itemPad[3];
        self.dockAlign !== ALIGN.TOP && (lastY = area.y + area.height);

        // 处理所有子图形.
        let shapes = self.getArrangeShapes().filter(s => !s.ignoreDock && s.getVisibility());
        const index = shapes.findIndex(s => s.isInDragging());
        if (index >= 0) {
            const draggingShape = shapes[index];
            const prev = index === 0 ? null : shapes[index - 1];
            const next = index === shapes.length - 1 ? null : shapes[index + 1];
            if (prev && draggingShape.y < prev.y + prev.height / 2) {
                shapes.swap(index - 1, index);
                self.page.swap(prev, draggingShape);
            }
            if (next && draggingShape.y + draggingShape.height > next.y + next.height / 2) {
                shapes.swap(index, index + 1);
                self.page.swap(draggingShape, next);
            }
        }

        shapes.forEach(s => {
            const height = self.calculateDivision(s, area);
            s.resize(area.width, height);
            if (self.dockAlign === ALIGN.TOP) {
                !s.isInDragging() && s.moveTo(lastX, lastY);
                lastY += s.height + self.itemSpace;
            } else {
                !s.isInDragging() && s.moveTo(lastX, lastY - s.height);
                lastY -= s.height + self.itemSpace;
            }
            totalHeight += s.height + self.itemSpace;
        });

        // 如果是autoFit，容器的高度要对所有图形的高度进行适配.
        if (self.autoFit) {
            self.height = self.itemPad[2] + self.itemPad[3]
                + shapes.sum(s => s.height)
                + (shapes.length - 1) * self.itemSpace;
            if (self.minHeight && self.minHeight > self.height) {
                self.height = self.minHeight;
            }
        }
    };

    /**
     * 水平排布自图形.
     */
    self.horizontal = () => {
        const area = self.getShapesArea();
        let lastX = area.x;
        let lastY = area.y;
        let totalWidth = self.itemPad[0] + self.itemPad[1];
        self.dockAlign !== ALIGN.LEFT && (lastX = area.x + area.width);

        // 处理所有子图形.
        self.getArrangeShapes().forEach(s => {
            const width = self.calculateDivision(s, area);
            s.resize(width, area.height);
            if (self.dockAlign === ALIGN.LEFT) {
                s.moveTo(lastX, lastY);
                lastX += s.width + self.itemSpace;
            } else {
                s.moveTo(lastX - s.width, lastY);
                lastX -= s.width + self.itemSpace;
            }
            totalWidth += s.width + self.itemSpace;
        });

        // 如果是autoFit，需要对容器进行resize.
        if (self.autoFit) {
            self.width = totalWidth;
        }
    };

    /**
     * 计算在容器中排布时，图形在容器中所占区域的大小.
     *
     * @param shape 图形对象.
     * @param area 容器所占区域.
     * @returns {number|*} 图形的宽度或高度.
     */
    self.calculateDivision = (shape, area) => {
        const key = self.dockMode === DOCK_MODE.VERTICAL ? "height" : "width";
        const division = self.division;
        return division === DIVISION.NONE ? shape[key] : (area[key] - self.itemSpace * (division - 1)) / division;
    };

    /**
     * 使子图形填充容器.
     */
    self.fill = () => {
        const area = self.getShapesArea();
        self.getArrangeShapes().forEach(s => {
            s.moveTo(area.x, area.y);
            s.resize(area.width, area.height);
        });
    };

    /**
     * 获取需要排布的图形列表.
     *
     * @returns {*} 图形列表.
     */
    self.getArrangeShapes = () => {
        return self.getShapes(s => s.getVisibility() && !s.ignoreDock && !s.isTypeof('line'));
    }

    const pDockModes = {};
    let arrangeShapes4parentDock = () => {
        let itemPad = self.itemPad;
        let left = itemPad[0];
        let right = itemPad[1];
        let top = itemPad[2];
        let bottom = itemPad[3];

        !pDockModes[PARENT_DOCK_MODE.LEFT] && (pDockModes[PARENT_DOCK_MODE.LEFT] = shape => {
            shape.moveTo(self.x + left, self.y + top);
            shape.resize(shape.width, self.height - bottom - top);
            left += shape.width + self.itemSpace;
        });
        !pDockModes[PARENT_DOCK_MODE.RIGHT] && (pDockModes[PARENT_DOCK_MODE.RIGHT] = shape => {
            shape.moveTo(self.x + self.width - right - shape.width, self.y + top);
            shape.resize(shape.width, self.height - bottom - top);
            right += shape.width + self.itemSpace;
        });
        !pDockModes[PARENT_DOCK_MODE.TOP] && (pDockModes[PARENT_DOCK_MODE.TOP] = shape => {
            shape.moveTo(self.x + left, self.y + top);
            shape.resize(self.width - left - right, self.height);
            top += shape.height + self.itemSpace;
        });
        !pDockModes[PARENT_DOCK_MODE.BOTTOM] && (pDockModes[PARENT_DOCK_MODE.BOTTOM] = shape => {
            shape.moveTo(self.x + left, self.y + self.height - bottom - shape.height);
            shape.resize(self.width - left - right, self.height);
            bottom += shape.height + self.itemSpace;
        });
        !pDockModes[PARENT_DOCK_MODE.FILL] && (pDockModes[PARENT_DOCK_MODE.FILL] = shape => {
            shape.moveTo(self.x + left, self.y + top);
            shape.resize(self.width - left - right, self.height - top - bottom);
        });

        // 将eval调用的方式修改，防止打包优化时，将eval代码去除掉的问题.
        self.getArrangeShapes().filter(s => s.pDock !== PARENT_DOCK_MODE.NONE)
            .forEach(s => pDockModes[s.pDock](s));
    };

    self.arrangeShapes = () => {
        if (!self.page.scrolling) {
            self[self.dockMode]();
        }
        arrangeShapes4parentDock();
    };

    let shapeChange = shape => {
        if (!self.isLoaded()) {
            return;
        }
        if (self.dockMode !== DOCK_MODE.NONE) {
            self.invalidate();
        }
        if (shape.pDock !== PARENT_DOCK_MODE.NONE) {
            self.invalidate();
        }
    }
    self.shapeAdded = (shape, preContainer) => shapeChange(shape);
    self.shapeRemoved = (shape, nextContainer) => shapeChange(shape);

    self.highLight = () => {
    };

    // ----------------------serialize & properties change detection----------------------
    self.addDetection(["id"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        self.page.sm.getShapes(s => s.container === preValue).forEach(s => s.container = value);
    });
    self.addDetection(["dockMode"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        self.invalidate();
    });
    //------------------------------------------------------------------------------------

    let get = self.get;
    self.get = field => {
        let specials = ["fontColor", "fontSize", "fontWeight", "fontFace", "hAlign", "lineHeight"];
        let value;
        if (specials.indexOf(field) >= 0) {
            value = self._data[field];
            if (value === undefined) {
                value = self.graph.setting["caption" + field];
            }
        } else {
            value = get.call(self, field);
            if (field === "backColor" && value === "transparent") {
                value = "rgba(255,255,255,0)";
            }
        }
        return value;
    };

    if (self.page !== undefined && !self.page.enableReacting && initializing !== undefined) {
        initializing(self);
    }

    /**
     * 当子图形进行dragTo时.
     *
     * @param child 子节点.
     * @param position 位置信息.
     */
    self.onChildDragTo = (child, position) => {
        if (self.dockMode === DOCK_MODE.VERTICAL) {
            position.deltaX = 0;
            if (child.y + position.deltaY < self.y || child.y + child.height + position.deltaY > self.y + self.height) {
                position.deltaY = 0;
            }
        }
        if (self.dockMode === DOCK_MODE.HORIZONTAL) {
            position.deltaY = 0;
            if (child.x + position.deltaX < self.x || child.x + child.width + position.deltaX > self.x + self.width) {
                position.deltaX = 0;
            }
        }
    };

    return self;
};

export { container};
