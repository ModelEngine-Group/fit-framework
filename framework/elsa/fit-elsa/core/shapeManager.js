/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {shapesIndexChangedCommand} from './commands.js';

const UN_SERIALIZABLE_SHAPES_OFFSET = 500;

/**
 * 图形管理器.
 *
 * @param page 页面对象.
 * @return {{shapeMap: Map<any, any>, shapes: *[]}}
 */
export const shapeManager = (page) => {
  const self = {shapeMap: new Map(), shapes: []};

  /*
   * 主要用于存储非正常情况下产生的图形，比如contextToolbar，itemMenu等，他们不参与层级计算，出现时永远处于图形上方.
   * 这种图形，要么是朝生夕死，不用计算其index，只要处于图形上方即可；要么是始终存在，始终不会删除，也不存在调整顺序的问题.
   */
  self.unSerializableShapes = [];

  /**
   * 添加图形.
   *
   * @param shape 图形对象.
   */
  self.pushShape = (shape) => {
    self.shapeMap.set(shape.id, shape);
    if (shape.serializable) {
      shape.index = self.getShapeCount();
      self.shapes.push(shape);
    } else {
      shape.index = UN_SERIALIZABLE_SHAPES_OFFSET + self.unSerializableShapes.length;
      self.unSerializableShapes.push(shape);
    }
  };

  /**
   * 通过id获取图形.
   *
   * @param shapeId 图形唯一标识.
   * @return {any} 图形对象.
   */
  self.getShapeById = (shapeId) => {
    return self.shapeMap.get(shapeId);
  };

  /**
   * 通过条件获取图形.
   *
   * @param condition 条件.
   * @return {*} 图形对象.
   */
  self.findShapeBy = (condition) => {
    return self.shapes.find(condition) || self.unSerializableShapes.find(condition);
  };

  /**
   * 判断是否包含满足条件的图形.
   *
   * @param condition 条件函数.
   * @return {boolean} true/false.
   */
  self.contains = (condition) => {
    return self.shapes.contains(condition) || self.unSerializableShapes.contains(condition);
  };

  /**
   * 是否包含指定id的shape.
   *
   * @param id 图形id.
   * @return {boolean} true/false.
   */
  self.containsById = (id) => {
    const shape = self.shapeMap.get(id);
    return shape !== null && shape !== undefined;
  };

  /**
   * 获取图形列表.
   *
   * @param condition 图形筛选条件，非必填.
   * @return {*[]} 图形列表.
   */
  self.getShapes = (condition = () => true) => {
    const shapes = self.shapes.filter(condition);
    const unserializableShapes = self.unSerializableShapes.filter(condition);
    return [...shapes, ...unserializableShapes];
  };

  /**
   * 获取图形数量.
   *
   * @return {number}
   */
  self.getShapeCount = () => {
    return self.shapes.length;
  };

  /**
   * 清除所有图形.
   */
  self.clear = () => {
    self.shapes.forEach(s => s.remove());
    self.shapes = [];
    self.unSerializableShapes.forEach(s => s.remove());
    self.unSerializableShapes = [];
    self.shapeMap.clear();
  };

  /**
   * 所有的修改，都应该通过该方法进行处理.
   *
   * @param action 修改操作.
   * @param enableCollaboration 是否开启协同.
   * @param enableCommands 是否开启命令.
   */
  self.updateShapes = (action, enableCollaboration = false, enableCommands = false) => {
    const w = writer(self, page, enableCollaboration, enableCommands);
    action(w);
  };

  /**
   * 获取图形下标. *为了兼容老数据*
   *
   * @param shape 图形对象.
   * @returns {*} 图形下标.
   */
  self.getShapeIndex = (shape) => {
    const index = self.shapes.indexOf(shape);
    if (index !== shape.index) {
      shape.index = index;
    }
    return index;
  };

  return self;
};

/**
 * 修改器.
 *
 * @param sm shapeManager对象.
 * @param page 页面对象.
 * @param enableCollaboration 是否启用协同.
 * @param enableCommands 是否启动命令.
 * @return {{}} 修改器.
 */
const writer = (sm, page, enableCollaboration, enableCommands) => {
  const self = {};
  self.flushing = false;
  self.affectedShapes = [];
  self.enableCollaboration = enableCollaboration;
  self.enableCommands = enableCommands;

  /**
   * 删除图形，并会刷新受影响的图形.
   *
   * @param shape 待删除图形.
   */
  self.deleteShape = (shape) => {
    sm.shapeMap.delete(shape.id);
    if (shape.serializable) {
      const index = sm.shapes.indexOf(shape);
      sm.shapes.removeAt(index);
      flushIndexBetween(index, sm.getShapeCount() - 1);
    } else {
      sm.unSerializableShapes.remove(s => s === shape);
    }
  };

  /**
   * 在指定下标插入图形，并刷新受影响的图形.
   *
   * @param shape 图形.
   * @param index 下标.
   */
  self.insertShape = (shape, index) => {
    if (!shape.serializable) {
      throw new Error('unserializable shape don\'t support insert shape operation.');
    }
    if (index < 0 || index >= sm.getShapeCount()) {
      return;
    }
    sm.shapeMap.set(shape.id, shape);
    sm.shapes.insert(shape, index);
    flushIndexBetween(index, sm.getShapeCount() - 1);
  };

  /**
   * 上移一层，并刷新受影响的图形.
   *
   * @param shapes 待上移的图形列表.
   */
  self.moveUp = (shapes) => {
    if (!shapes || shapes.length === 0) {
      return;
    }
    shapes.filter(s => s.serializable).orderBy(s => s.index).reverse().forEach(s => {
      const nextIndex = s.index + 1;

      // shape已经在最顶层，则不做处理.
      if (nextIndex >= sm.getShapeCount()) {
        return;
      }

      const nextShape = sm.shapes[nextIndex];
      if (shapes.contains(ss => ss === nextShape)) {
        return;
      }

      self.swapShape(s, nextShape);
    });
    flushIndex();
  };

  /**
   * 下移一层.
   *
   * @param shapes 待下移的图形列表.
   */
  self.moveDown = (shapes) => {
    if (!shapes || shapes.length === 0) {
      return;
    }
    const minIndex = page.getMinIndex();
    shapes.filter(s => s.serializable).orderBy(s => s.index).forEach(s => {
      // shape已经在最底层，则不做处理.
      const prevIndex = s.index - 1;
      if (prevIndex < minIndex) {
        return;
      }

      // 如果下一个shape在shapes中，为了保持选中图形的相对顺序不变，这里不做处理.
      const prevShape = sm.shapes[prevIndex];
      if (shapes.contains(ss => ss === prevShape)) {
        return;
      }

      // 交换图形顺序，并且记录交换前后的index数据，主要用于提供给撤销重做或协同等场景使用.
      self.swapShape(s, prevShape);
    });
    flushIndex();
  };

  /**
   * 将图形移动到顶层，并刷新受影响的图形.
   *
   * @param shapes 图形列表.
   */
  self.moveTop = (shapes) => {
    if (!shapes || shapes.length === 0) {
      return;
    }
    const minIndex = Math.min(shapes.map(s => s.index));
    shapes.filter(s => s.serializable).orderBy(s => s.index).forEach(s => {
      sm.shapes.remove(ss => ss === s);
      sm.shapes.push(s);
    });

    flushIndexBetween(minIndex, sm.getShapeCount() - 1);
  };

  /**
   * 将图形移动到底层.
   *
   * @param shapes 图形列表.
   */
  self.moveBottom = (shapes) => {
    if (!shapes || shapes.length === 0) {
      return;
    }

    // 先把前minIndex个无法移动的图形取出.
    const minIndex = page.getMinIndex();
    const temp = [];
    for (let i = 0; i < minIndex; i++) {
      temp.push(sm.shapes.shift());
    }

    const maxIndex = Math.max(shapes.map(s => s.index));
    const serializableShapes = shapes.filter(s => s.serializable).orderBy(s => s.index);

    // 删除图形.
    sm.shapes.remove(s => serializableShapes.contains(ss => ss === s));

    // 将图形放到数组前面.
    sm.shapes = [...serializableShapes, ...sm.shapes];

    // 将无法移动的图形放回.
    sm.shapes = [...temp, ...sm.shapes];

    flushIndexBetween(minIndex, maxIndex);
  };

  /**
   * 移动图形到指定位置.
   *
   * @param shape 图形对象.
   * @param index 指定下标.
   */
  self.moveShapeTo = (shape, index) => {
    if (!shape.serializable) {
      throw new Error('unserializable shape don\'t support move shape operation.');
    }
    if (shape.index === index || index >= sm.getShapeCount()) {
      return;
    }
    sm.shapes.remove(s => s === shape);
    sm.shapes.insert(shape, index);

    // 添加受影响的图形，即调整区间范围内的图形.
    const start = Math.min(index, shape.index);
    const end = Math.max(index, shape.index);
    flushIndexBetween(start, end);
  };

  /**
   * 交换图形顺序.
   *
   * @param shapeA 图形a.
   * @param shapeB 图形b.
   * @return [] 返回图形A和B以及他们交换前后的index.
   */
  self.swapShape = (shapeA, shapeB) => {
    if (!shapeA.serializable || !shapeB.serializable) {
      throw new Error('unserializable shape don\'t support move shape operation.');
    }
    const aIndex = shapeA.index;
    const bIndex = shapeB.index;
    if (!isIndexInvalid(aIndex) || !isIndexInvalid(bIndex)) {
      return;
    }
    sm.shapes.swap(aIndex, bIndex);
    addAffectedIndexShape({shape: shapeA, index: bIndex});
    addAffectedIndexShape({shape: shapeB, index: aIndex});
    flushIndex();
  };

  // 刷新start和end之间的所有图形的index.
  const flushIndexBetween = (start, end) => {
    for (let i = start; i <= end; i++) {
      addAffectedIndexShape({shape: sm.shapes[i], index: i});
    }
    flushIndex();
  };

  const addAffectedIndexShape = (affectedShape) => {
    // 不处理不可以序列化的图形.
    if (!affectedShape.shape.serializable || affectedShape.shape.index === affectedShape.index) {
      return;
    }
    const as = self.affectedShapes.find(a => a.shape === affectedShape.shape);
    if (as) {
      // 更新到最新的index.
      as.index = affectedShape.index;
    } else {
      self.affectedShapes.push(affectedShape);
    }
  };

  /*
   * 批量处理删除插入图形等事件.
   */
  const flushIndex = () => {
    if (!self.flushing) {
      self.flushing = true;
      Promise.resolve().then(() => {
        // 过滤掉已被删除的图形.
        // 过滤掉index没有变化的图形.
        const dirties = self.affectedShapes.filter(a => a.shape)
          .filter(a => sm.shapeMap.has(a.shape.id))
          .filter(a => a.shape.index !== a.nextIndex)
          .map(a => {
            return {
              shape: a.shape, prevIndex: a.shape.index, nextIndex: a.index,
            };
          });

        // 如果启用，使用命令的方式执行刷新操作；否则，仅刷新图形即可.
        if (self.enableCommands) {
          shapesIndexChangedCommand(page, dirties).execute();
          self.enableCommands = false;
        } else {
          dirties.forEach(d => {
            d.shape.index = d.nextIndex;
            d.shape.invalidateAlone();
          });
        }

        if (self.enableCollaboration) {
          dirties.forEach(d => {
            page.graph.collaboration.invoke({
              method: 'change_shape_index',
              page: page.id,
              shape: d.shape.id,
              mode: page.mode,
              value: {fromIndex: d.prevIndex, toIndex: d.nextIndex},
            });
          });
          self.enableCollaboration = false;
        }
        self.flushing = false;
        self.affectedShapes = [];
      });
    }
  };

  const isIndexInvalid = (index) => {
    return index >= 0 && index < sm.getShapeCount();
  };

  return self;
};